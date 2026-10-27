package com.trace.farming.service.impl;

import com.trace.common.constant.Constants;
import com.trace.common.context.UserContext;
import com.trace.common.domain.LoginUser;
import com.trace.common.domain.event.FarmingRecordEvent;
import com.trace.common.enums.FarmingType;
import com.trace.common.enums.ResultCode;
import com.trace.common.exception.BizException;
import com.trace.common.result.R;
import com.trace.farming.client.BatchClient;
import com.trace.farming.client.dto.BatchLiteDTO;
import com.trace.farming.dto.OfflineSyncRequest;
import com.trace.farming.dto.SubmitRecordRequest;
import com.trace.farming.entity.FarmingRecord;
import com.trace.farming.mapper.FarmingRecordMapper;
import com.trace.farming.service.FarmingService;
import com.trace.farming.vo.FarmingRecordVO;
import java.math.BigDecimal;
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
 * 农事记录业务实现
 */
@Service
public class FarmingServiceImpl implements FarmingService {

    private static final Logger log = LoggerFactory.getLogger(FarmingServiceImpl.class);

    @Resource
    private FarmingRecordMapper farmingRecordMapper;
    @Resource
    private BatchClient batchClient;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitSingle(SubmitRecordRequest request, String source) {
        LoginUser loginUser = UserContext.get();
        validateBatch(request.getBatchId(), loginUser);
        FarmingRecord record = buildRecord(request, loginUser, source);
        farmingRecordMapper.insert(record);
        publish(record);
        return record.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int syncBatch(OfflineSyncRequest request, String source) {
        LoginUser loginUser = UserContext.get();
        List<FarmingRecord> records = new ArrayList<>();
        for (SubmitRecordRequest item : request.getRecords()) {
            // 幂等：同一 syncToken 只入库一次，防止弱网重复提交
            Long exist = farmingRecordMapper.countBySyncToken(request.getSyncToken());
            if (exist != null && exist > 0) {
                log.info("离线同步令牌已存在，跳过: {}", request.getSyncToken());
                return 0;
            }
            validateBatch(item.getBatchId(), loginUser);
            records.add(buildRecord(item, loginUser, source));
        }
        for (FarmingRecord record : records) {
            farmingRecordMapper.insert(record);
            publish(record);
        }
        return records.size();
    }

    @Override
    public List<FarmingRecordVO> listByBatch(Long batchId) {
        List<FarmingRecord> list = farmingRecordMapper.selectByBatchId(batchId);
        List<FarmingRecordVO> vos = new ArrayList<>(list.size());
        for (FarmingRecord r : list) {
            vos.add(toVO(r));
        }
        return vos;
    }

    @Override
    public void writeBackCheckResult(Long recordId, String status, String remark, String aiAdvice) {
        FarmingRecord record = farmingRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BizException(ResultCode.NOT_FOUND, "农事记录不存在");
        }
        record.setCheckStatus(status);
        record.setCheckRemark(remark);
        record.setAiAdvice(aiAdvice);
        record.setUpdateTime(LocalDateTime.now());
        farmingRecordMapper.updateById(record);
    }

    // ---------------------- private ----------------------

    private void validateBatch(Long batchId, LoginUser loginUser) {
        if (batchId == null) {
            throw new BizException(ResultCode.PARAM_ERROR, "批次ID不能为空");
        }
        R<BatchLiteDTO> resp = batchClient.getBatch(batchId);
        if (resp == null || !resp.isSuccess() || resp.getData() == null) {
            throw new BizException(ResultCode.PARAM_ERROR, "批次不存在或不可达");
        }
        BatchLiteDTO batch = resp.getData();
        // 农户只能操作自己名下的批次；收购商/管理员可查看被授权农户
        if (loginUser != null && Constants.ROLE_FARMER.equals(loginUser.getRole())) {
            if (!batch.getFarmerId().equals(loginUser.getUserId())) {
                throw new BizException(ResultCode.FORBIDDEN, "无权操作该批次");
            }
        }
    }

    private FarmingRecord buildRecord(SubmitRecordRequest req, LoginUser loginUser, String source) {
        FarmingType type = FarmingType.of(req.getFarmingType());
        FarmingRecord record = new FarmingRecord();
        record.setBatchId(req.getBatchId());
        record.setBatchNo(req.getBatchNo());
        record.setFarmerId(loginUser == null ? null : loginUser.getUserId());
        record.setFarmingType(type.getCode());
        record.setMaterialName(req.getMaterialName());
        record.setAmount(req.getAmount() == null ? BigDecimal.ZERO : req.getAmount());
        record.setUnit(resolveUnit(type, req.getUnit()));
        record.setWorkDate(req.getWorkDate());
        record.setAreaMu(req.getAreaMu());
        record.setImages(req.getImages() == null ? null : String.join(",", req.getImages()));
        record.setSource(StringUtils.hasText(source) ? source : Constants.CHANNEL_APP);
        record.setSyncToken(req.getSyncToken());
        record.setCheckStatus("WAIT");
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setDeleted(0);
        record.setVersion(0);
        return record;
    }

    private String resolveUnit(FarmingType type, String unit) {
        if (StringUtils.hasText(unit)) {
            return unit;
        }
        switch (type) {
            case FERTILIZE:
            case PESTICIDE:
                return "kg";
            case IRRIGATE:
                return "m3";
            case MACHINE:
                return "L";
            case FILM:
                return "kg";
            default:
                return "次";
        }
    }

    private void publish(FarmingRecord record) {
        FarmingRecordEvent event = new FarmingRecordEvent();
        event.setRecordId(record.getId());
        event.setBatchId(record.getBatchId());
        event.setBatchNo(record.getBatchNo());
        event.setFarmerId(record.getFarmerId());
        event.setFarmingType(record.getFarmingType());
        event.setMaterialName(record.getMaterialName());
        event.setAmount(record.getAmount());
        event.setUnit(record.getUnit());
        event.setWorkDate(record.getWorkDate());
        event.setAreaMu(record.getAreaMu());
        event.setSource(record.getSource());
        event.setSyncToken(record.getSyncToken());
        event.setEventId(UUID.randomUUID().toString());
        event.setOccurTime(LocalDateTime.now());
        try {
            rabbitTemplate.convertAndSend(com.trace.common.constant.MqConstants.EXCHANGE_FARMING_EVENT, "", event);
            log.info("农事记录已发布事件 recordId={}", record.getId());
        } catch (Exception e) {
            log.warn("发布农事事件失败（消息中间件不可用，已忽略）recordId={}", record.getId(), e);
        }
    }

    private FarmingRecordVO toVO(FarmingRecord r) {
        FarmingRecordVO vo = new FarmingRecordVO();
        BeanUtils.copyProperties(r, vo);
        vo.setFarmingTypeDesc(FarmingType.of(r.getFarmingType()).getDesc());
        return vo;
    }
}
