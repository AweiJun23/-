package com.trace.batch.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trace.batch.client.ChainClient;
import com.trace.batch.client.CarbonClient;
import com.trace.batch.client.FarmingClient;
import com.trace.batch.client.dto.CarbonReportLiteDTO;
import com.trace.batch.client.dto.ChainVerifyLiteDTO;
import com.trace.batch.client.dto.FarmingLiteDTO;
import com.trace.batch.entity.ProductBatch;
import com.trace.batch.mapper.ProductBatchMapper;
import com.trace.batch.service.BatchService;
import com.trace.batch.service.dto.BatchTraceVO;
import com.trace.batch.service.dto.CreateBatchRequest;
import com.trace.batch.util.QrCodeGenerator;
import com.trace.common.constant.Constants;
import com.trace.common.enums.ResultCode;
import com.trace.common.exception.BizException;
import com.trace.common.result.R;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 农产品批次业务实现
 */
@Service
public class BatchServiceImpl implements BatchService {

    private static final Logger log = LoggerFactory.getLogger(BatchServiceImpl.class);

    private static final String BASE_TRACE_URL = "http://localhost:5174/h5/trace?batchNo=";

    @Resource
    private ProductBatchMapper batchMapper;
    @Resource
    private FarmingClient farmingClient;
    @Resource
    private CarbonClient carbonClient;
    @Resource
    private ChainClient chainClient;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBatch(CreateBatchRequest request) {
        ProductBatch batch = new ProductBatch();
        BeanUtils.copyProperties(request, batch);
        if (!StringUtils.hasText(batch.getStatus())) {
            batch.setStatus(Constants.BATCH_STATUS_GROWING);
        }
        batch.setBatchNo(genBatchNo(request.getFarmerId()));
        batch.setQrUrl(BASE_TRACE_URL + batch.getBatchNo());
        batch.setDeleted(0);
        batch.setVersion(0);
        batch.setCreateTime(LocalDateTime.now());
        batch.setUpdateTime(LocalDateTime.now());
        batchMapper.insert(batch);
        // 生成二维码并回写
        try {
            String base64 = QrCodeGenerator.generateBase64(batch.getQrUrl(), 240);
            batch.setQrCode(base64);
            batchMapper.updateById(batch);
        } catch (Exception e) {
            log.warn("二维码生成失败 batchNo={}: {}", batch.getBatchNo(), e.getMessage());
        }
        return batch.getId();
    }

    @Override
    public ProductBatch getById(Long id) {
        return batchMapper.selectById(id);
    }

    @Override
    public ProductBatch internalGet(Long id) {
        return batchMapper.selectById(id);
    }

    @Override
    public ProductBatch getByBatchNo(String batchNo) {
        ProductBatch batch = batchMapper.selectByBatchNo(batchNo);
        if (batch == null) {
            throw new BizException(ResultCode.NOT_FOUND, "批次不存在");
        }
        return batch;
    }

    @Override
    public byte[] qrImage(String batchNo) {
        ProductBatch batch = getByBatchNo(batchNo);
        try {
            String base64 = QrCodeGenerator.generateBase64(batch.getQrUrl(), 240);
            return java.util.Base64.getDecoder().decode(base64.split(",")[1]);
        } catch (Exception e) {
            throw new BizException(ResultCode.ERROR, "二维码生成失败");
        }
    }

    @Override
    public String qrBase64(String batchNo) {
        ProductBatch batch = getByBatchNo(batchNo);
        try {
            return QrCodeGenerator.generateBase64(batch.getQrUrl(), 240);
        } catch (Exception e) {
            throw new BizException(ResultCode.ERROR, "二维码生成失败");
        }
    }

    @Override
    public BatchTraceVO traceAggregate(String batchNo) {
        ProductBatch batch = getByBatchNo(batchNo);
        BatchTraceVO vo = new BatchTraceVO();
        BeanUtils.copyProperties(batch, vo);

        // 1. 农事时间线
        R<List<FarmingLiteDTO>> farmingResp = farmingClient.listByBatch(batch.getId());
        vo.setFarmingList(farmingResp.isSuccess() ? farmingResp.getData() : null);

        // 2. 碳足迹报告
        R<CarbonReportLiteDTO> carbonResp = carbonClient.getReport(batch.getId());
        vo.setCarbonReport(carbonResp.isSuccess() ? carbonResp.getData() : null);

        // 3. 链上验真：批次 + 农事 + 碳报告
        R<ChainVerifyLiteDTO> batchVerify = chainClient.verifyByBiz(Constants.CHAIN_BIZ_BATCH, String.valueOf(batch.getId()));
        vo.setBatchVerified(batchVerify.isSuccess() && Boolean.TRUE.equals(batchVerify.getData().getVerified()));
        if (carbonResp.isSuccess() && carbonResp.getData() != null
                && StringUtils.hasText(carbonResp.getData().getReportHash())) {
            R<ChainVerifyLiteDTO> carbonVerify = chainClient.verifyByBiz(Constants.CHAIN_BIZ_CARBON, String.valueOf(carbonResp.getData().getReportId()));
            vo.setCarbonVerified(carbonVerify.isSuccess() && Boolean.TRUE.equals(carbonVerify.getData().getVerified()));
        } else {
            vo.setCarbonVerified(false);
        }
        return vo;
    }

    @Override
    public List<ProductBatch> listByCoop(Long coopId) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductBatch> qw
                = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (coopId != null) {
            qw.eq(ProductBatch::getCoopId, coopId);
        }
        qw.eq(ProductBatch::getDeleted, 0)
          .orderByDesc(ProductBatch::getCreateTime);
        return batchMapper.selectList(qw);
    }

    private String genBatchNo(Long farmerId) {        String ts = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String suffix = farmerId == null ? "0" : String.valueOf(farmerId);
        return "AG" + ts + suffix;
    }
}
