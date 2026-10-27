package com.trace.farming.client;

import com.trace.common.result.R;
import com.trace.farming.client.dto.BatchLiteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 调用农产品批次服务，校验批次归属与状态
 */
@FeignClient(name = "trace-batch", url = "${feign.target.batch:http://127.0.0.1:8083}")
public interface BatchClient {

    @GetMapping("/api/batch/internal/{batchId}")
    R<BatchLiteDTO> getBatch(@PathVariable("batchId") Long batchId);
}
