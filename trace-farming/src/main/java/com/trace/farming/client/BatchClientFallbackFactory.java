package com.trace.farming.client;

import com.trace.common.result.R;
import com.trace.farming.client.dto.BatchLiteDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 批次服务降级占位（默认未启用 Hystrix，保留以便后续接入熔断）
 */
@Component
public class BatchClientFallbackFactory {

    private static final Logger log = LoggerFactory.getLogger(BatchClientFallbackFactory.class);

    public R<BatchLiteDTO> unavailable() {
        log.warn("批次服务调用降级");
        return R.fail("批次服务暂不可用");
    }
}
