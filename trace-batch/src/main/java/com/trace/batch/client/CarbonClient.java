package com.trace.batch.client;

import com.trace.batch.client.dto.CarbonReportLiteDTO;
import com.trace.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 调用碳足迹核算服务，拉取批次碳报告
 */
@FeignClient(name = "trace-carbon", url = "${feign.target.carbon:http://127.0.0.1:8084}")
public interface CarbonClient {

    @GetMapping("/api/carbon/internal/report/{batchId}")
    R<CarbonReportLiteDTO> getReport(@PathVariable("batchId") Long batchId);
}
