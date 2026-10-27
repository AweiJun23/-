package com.trace.report.client;

import com.trace.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 调用批次服务
 */
@FeignClient(name = "trace-batch")
public interface BatchClient {

    @GetMapping("/api/batch/internal/{id}")
    R<BatchLiteDTO> getBatch(@PathVariable("id") Long id);
}
