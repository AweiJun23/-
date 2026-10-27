package com.trace.report.client;

import com.trace.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 调用碳足迹核算服务
 */
@FeignClient(name = "trace-carbon")
public interface CarbonClient {

    @GetMapping("/api/carbon/internal/report/{batchId}")
    R<CarbonReportLiteDTO> getReport(@PathVariable("batchId") Long batchId);
}
