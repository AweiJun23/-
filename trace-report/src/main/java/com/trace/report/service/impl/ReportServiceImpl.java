package com.trace.report.service.impl;

import com.trace.common.enums.ResultCode;
import com.trace.common.exception.BizException;
import com.trace.common.result.R;
import com.trace.common.util.HashUtil;
import com.trace.report.client.AgentClient;
import com.trace.report.client.AgentResponseDTO;
import com.trace.report.client.BatchClient;
import com.trace.report.client.BatchLiteDTO;
import com.trace.report.client.CarbonClient;
import com.trace.report.client.CarbonReportLiteDTO;
import com.trace.report.entity.SubsidyReport;
import com.trace.report.mapper.SubsidyReportMapper;
import com.trace.report.service.ReportService;
import com.trace.report.vo.SubsidyReportVO;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 补贴报表业务实现
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Resource
    private BatchClient batchClient;
    @Resource
    private CarbonClient carbonClient;
    @Resource
    private AgentClient agentClient;
    @Resource
    private SubsidyReportMapper reportMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generate(Long batchId, Long farmerId) {
        R<BatchLiteDTO> batchResp = batchClient.getBatch(batchId);
        if (!batchResp.isSuccess() || batchResp.getData() == null) {
            throw new BizException(ResultCode.NOT_FOUND, "批次不存在");
        }
        BatchLiteDTO batch = batchResp.getData();
        R<CarbonReportLiteDTO> carbonResp = carbonClient.getReport(batchId);
        CarbonReportLiteDTO carbon = carbonResp.isSuccess() ? carbonResp.getData() : null;

        Map<String, Object> payload = new HashMap<>();
        payload.put("batchNo", batch.getBatchNo());
        payload.put("productName", batch.getProductName());
        payload.put("origin", buildOrigin(batch));
        payload.put("actualYield", batch.getActualYield());
        payload.put("carbon", carbon);

        R<AgentResponseDTO> agentResp = agentClient.subsidyReport(payload);
        String content = (agentResp.isSuccess() && agentResp.getData() != null
                && agentResp.getData().getContent() != null)
                ? agentResp.getData().getContent()
                : "（智能体不可用，按模板生成）绿色农产品补贴申报材料：批次" + batch.getBatchNo();

        SubsidyReport report = new SubsidyReport();
        report.setBatchId(batchId);
        report.setFarmerId(farmerId == null ? batch.getFarmerId() : farmerId);
        report.setReportNo("SB" + System.currentTimeMillis());
        report.setTitle("绿色低碳农产品补贴申报-" + batch.getBatchNo());
        report.setContentMd(content);
        report.setReportHash(HashUtil.sha256(content + "|" + report.getReportNo()));
        report.setStatus("GENERATED");
        report.setGeneratedBy("AGENT");
        report.setGeneratedTime(LocalDateTime.now());
        report.setCreateTime(LocalDateTime.now());
        report.setUpdateTime(LocalDateTime.now());
        report.setDeleted(0);
        report.setVersion(0);
        reportMapper.insert(report);
        return report.getId();
    }

    @Override
    public SubsidyReportVO latest(Long batchId) {
        SubsidyReport report = reportMapper.selectLatestByBatchId(batchId);
        if (report == null) {
            throw new BizException(ResultCode.NOT_FOUND, "暂无申报材料");
        }
        SubsidyReportVO vo = new SubsidyReportVO();
        BeanUtils.copyProperties(report, vo);
        return vo;
    }

    /** 拼接产地：省 + 市 + 详细地址，空段自动忽略 */
    private String buildOrigin(BatchLiteDTO batch) {
        StringBuilder sb = new StringBuilder();
        if (org.springframework.util.StringUtils.hasText(batch.getOriginProvince())) {
            sb.append(batch.getOriginProvince());
        }
        if (org.springframework.util.StringUtils.hasText(batch.getOriginCity())) {
            sb.append(batch.getOriginCity());
        }
        if (org.springframework.util.StringUtils.hasText(batch.getOriginDetail())) {
            sb.append(batch.getOriginDetail());
        }
        return sb.length() > 0 ? sb.toString() : "待补充";
    }
}
