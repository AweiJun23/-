package com.trace.carbon.service.impl;

import com.trace.carbon.client.AgentClient;
import com.trace.carbon.client.BatchClient;
import com.trace.carbon.client.BatchYieldDTO;
import com.trace.carbon.client.CarbonAdviceRequest;
import com.trace.carbon.entity.CarbonRecordEmission;
import com.trace.carbon.entity.CarbonReport;
import com.trace.carbon.entity.CarbonReportItem;
import com.trace.carbon.entity.CarbonFactor;
import com.trace.carbon.mapper.CarbonFactorMapper;
import com.trace.carbon.mapper.CarbonRecordEmissionMapper;
import com.trace.carbon.mapper.CarbonReportItemMapper;
import com.trace.carbon.mapper.CarbonReportMapper;
import com.trace.carbon.service.CarbonService;
import com.trace.carbon.vo.CarbonReportVO;
import com.trace.common.constant.Constants;
import com.trace.common.domain.event.CarbonReportEvent;
import com.trace.common.domain.event.FarmingRecordEvent;
import com.trace.common.enums.FarmingType;
import com.trace.common.exception.BizException;
import com.trace.common.enums.ResultCode;
import com.trace.common.result.R;
import com.trace.common.util.HashUtil;
import com.trace.common.util.NumberUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 碳足迹核算业务实现
 */
@Service
public class CarbonServiceImpl implements CarbonService {

    private static final Logger log = LoggerFactory.getLogger(CarbonServiceImpl.class);
    private static final int SCALE = 4;

    @Resource
    private CarbonFactorMapper factorMapper;
    @Resource
    private CarbonRecordEmissionMapper emissionMapper;
    @Resource
    private CarbonReportMapper reportMapper;
    @Resource
    private CarbonReportItemMapper itemMapper;
    @Resource
    private BatchClient batchClient;
    @Resource
    private AgentClient agentClient;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void computeOnFarmingEvent(FarmingRecordEvent event) {
        // 1. 单条记录排放核算
        BigDecimal emission = calcSingle(event);
        if (emission == null) {
            log.warn("无匹配排放因子，跳过核算 recordId={} type={} material={}",
                    event.getRecordId(), event.getFarmingType(), event.getMaterialName());
            return;
        }
        upsertEmission(event, emission);

        // 2. 聚合批次全部农事排放 -> 报告
        List<CarbonRecordEmission> emissions = emissionMapper.selectByBatchId(event.getBatchId());
        BigDecimal total = BigDecimal.ZERO;
        for (CarbonRecordEmission e : emissions) {
            total = total.add(NumberUtil.nz(e.getEmission()));
        }

        BigDecimal actualYield = fetchYield(event.getBatchId());
        BigDecimal unitEmission = actualYield != null && actualYield.compareTo(BigDecimal.ZERO) > 0
                ? total.divide(actualYield, SCALE, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        // 3. 报告分项占比
        List<CarbonReportItem> items = new ArrayList<>();
        BigDecimal finalTotal = total;
        emissions.forEach(e -> {
            CarbonReportItem item = new CarbonReportItem();
            item.setFarmingType(e.getFarmingType());
            item.setMaterialName(e.getMaterialName());
            item.setAmount(e.getAmount());
            item.setUnit(e.getUnit());
            item.setFactorValue(e.getFactorValue());
            item.setEmission(e.getEmission());
            BigDecimal prop = finalTotal.compareTo(BigDecimal.ZERO) > 0
                    ? e.getEmission().divide(finalTotal, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO;
            item.setProportion(NumberUtil.scale(prop, 2));
            item.setDeleted(0);
            items.add(item);
        });

        // 4. 持久化报告（覆盖式更新）
        CarbonReport report = reportMapper.selectLatestByBatchId(event.getBatchId());
        if (report == null) {
            report = new CarbonReport();
            report.setCreateTime(LocalDateTime.now());
        }
        report.setBatchId(event.getBatchId());
        report.setBatchNo(event.getBatchNo());
        report.setTotalEmission(NumberUtil.scale(total, SCALE));
        report.setUnitEmission(NumberUtil.scale(unitEmission, SCALE));
        report.setStatus("GENERATED");
        report.setReportTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());
        report.setDeleted(0);
        report.setVersion(0);
        if (report.getId() == null) {
            reportMapper.insert(report);
        } else {
            reportMapper.updateById(report);
        }

        // 明细先清后插
        for (CarbonReportItem it : items) {
            it.setReportId(report.getId());
            itemMapper.insert(it);
        }

        // 5. 计算哈希（供链上存证）
        String hash = buildReportHash(report, items);
        report.setReportHash(hash);
        reportMapper.updateById(report);

        // 6. AI 低碳建议（规则引擎校验后写入）
        try {
            R<String> adviceResp = agentClient.carbonAdvice(buildAdviceRequest(report, items));
            if (adviceResp.isSuccess() && StringUtils.hasText(adviceResp.getData())) {
                if (adviceResp.getData().length() <= 2000) {
                    report.setAiAdvice(adviceResp.getData());
                    reportMapper.updateById(report);
                } else {
                    log.warn("AI 建议超出长度阈值，已丢弃以便规则校验兜底");
                }
            }
        } catch (Exception e) {
            log.warn("调用 AI 建议接口失败，使用规则兜底: {}", e.getMessage());
        }

        // 7. 发布碳报告事件 -> 联盟链存证
        CarbonReportEvent reportEvent = new CarbonReportEvent();
        reportEvent.setEventId(UUID.randomUUID().toString());
        reportEvent.setReportId(report.getId());
        reportEvent.setBatchId(report.getBatchId());
        reportEvent.setBatchNo(report.getBatchNo());
        reportEvent.setTotalEmission(report.getTotalEmission());
        reportEvent.setUnitEmission(report.getUnitEmission());
        reportEvent.setReportHash(report.getReportHash());
        reportEvent.setReportTime(report.getReportTime());
        reportEvent.setOccurTime(LocalDateTime.now());
        try {
            rabbitTemplate.convertAndSend(com.trace.common.constant.MqConstants.EXCHANGE_CARBON_REPORT, "", reportEvent);
            log.info("碳足迹报告已生成并发布 batchId={} total={}", report.getBatchId(), total);
        } catch (Exception e) {
            log.warn("发布碳报告事件失败（消息中间件不可用，已忽略）batchId={}", report.getBatchId(), e);
        }
    }

    @Override
    public CarbonReportVO getReport(Long batchId) {
        CarbonReport report = reportMapper.selectLatestByBatchId(batchId);
        if (report == null) {
            throw new BizException(ResultCode.NOT_FOUND, "碳足迹报告不存在");
        }
        return toVO(report);
    }

    @Override
    public CarbonReportVO getReportByBatchNo(String batchNo) {
        CarbonReport report = reportMapper.selectByBatchNo(batchNo);
        if (report == null) {
            throw new BizException(ResultCode.NOT_FOUND, "碳足迹报告不存在");
        }
        return toVO(report);
    }

    // ---------------------- private ----------------------

    private BigDecimal calcSingle(FarmingRecordEvent event) {
        CarbonFactor factor = factorMapper.selectByTypeAndMaterial(event.getFarmingType(), event.getMaterialName());
        if (factor == null) {
            return null;
        }
        BigDecimal amount = NumberUtil.nz(event.getAmount());
        return amount.multiply(NumberUtil.nz(factor.getFactorValue())).setScale(SCALE, RoundingMode.HALF_UP);
    }

    private void upsertEmission(FarmingRecordEvent event, BigDecimal emission) {
        CarbonRecordEmission exist = emissionMapper.selectByRecordId(event.getRecordId());
        if (exist == null) {
            CarbonRecordEmission e = new CarbonRecordEmission();
            e.setRecordId(event.getRecordId());
            e.setBatchId(event.getBatchId());
            e.setFarmingType(event.getFarmingType());
            e.setMaterialName(event.getMaterialName());
            e.setAmount(NumberUtil.nz(event.getAmount()));
            e.setUnit(event.getUnit());
            e.setFactorValue(factorMapper.selectByTypeAndMaterial(event.getFarmingType(), event.getMaterialName()).getFactorValue());
            e.setEmission(emission);
            e.setDeleted(0);
            emissionMapper.insert(e);
        } else {
            exist.setEmission(emission);
            exist.setAmount(NumberUtil.nz(event.getAmount()));
            exist.setFactorValue(factorMapper.selectByTypeAndMaterial(event.getFarmingType(), event.getMaterialName()).getFactorValue());
            emissionMapper.updateById(exist);
        }
    }

    private BigDecimal fetchYield(Long batchId) {
        try {
            R<BatchYieldDTO> resp = batchClient.getBatch(batchId);
            if (resp.isSuccess() && resp.getData() != null) {
                return resp.getData().getActualYield();
            }
        } catch (Exception e) {
            log.warn("获取产量失败 batchId={}: {}", batchId, e.getMessage());
        }
        return null;
    }

    private String buildReportHash(CarbonReport report, List<CarbonReportItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append(report.getBatchId()).append("|").append(report.getTotalEmission()).append("|")
                .append(report.getUnitEmission()).append("|");
        for (CarbonReportItem it : items) {
            sb.append(it.getFarmingType()).append(":").append(it.getEmission()).append(";");
        }
        sb.append(report.getCreateTime());
        return HashUtil.sha256(sb.toString());
    }

    private CarbonAdviceRequest buildAdviceRequest(CarbonReport report, List<CarbonReportItem> items) {
        CarbonAdviceRequest req = new CarbonAdviceRequest();
        req.setBatchId(report.getBatchId());
        req.setBatchNo(report.getBatchNo());
        List<CarbonAdviceRequest.ItemSummary> summaries = new ArrayList<>();
        for (CarbonReportItem it : items) {
            CarbonAdviceRequest.ItemSummary s = new CarbonAdviceRequest.ItemSummary();
            s.setFarmingType(FarmingType.of(it.getFarmingType()).getDesc());
            s.setMaterialName(it.getMaterialName());
            s.setEmission(it.getEmission());
            s.setProportion(it.getProportion());
            summaries.add(s);
        }
        req.setItems(summaries);
        return req;
    }

    private CarbonReportVO toVO(CarbonReport report) {
        CarbonReportVO vo = new CarbonReportVO();
        BeanUtils.copyProperties(report, vo);
        vo.setReportId(report.getId());
        vo.setItems(itemMapper.selectByReportId(report.getId()));
        return vo;
    }
}
