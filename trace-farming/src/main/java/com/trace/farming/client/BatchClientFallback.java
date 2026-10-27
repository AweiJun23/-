package com.trace.farming.client;

import com.trace.common.enums.ResultCode;
import com.trace.common.result.R;
import com.trace.farming.client.dto.BatchLiteDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 批次服务降级处理
 * <p>
 * 批次服务不可用时，农事记录仍可提交（弱依赖），仅跳过归属校验，
 * 事后由定时任务补偿校验，避免农户在田间登记时录入失败。
 */
@Component
public class BatchClientFallback implements BatchClient {

    private static final Logger log = LoggerFactory.getLogger(BatchClientFallback.class);

    @Override
    public R<BatchLiteDTO> getBatch(Long batchId) {
        log.warn("批次服务不可用，跳过批次归属校验 batchId={}", batchId);
        return R.fail(ResultCode.AI_SERVICE_ERROR, "批次服务暂时不可用");
    }
}
