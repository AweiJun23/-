package com.trace.carbon.client;

import com.trace.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 调用批次服务获取产量信息（用于单位产量碳排计算）
 */
@FeignClient(name = "trace-batch", url = "${feign.target.batch:http://127.0.0.1:8083}")
public interface BatchClient {

    @GetMapping("/api/batch/internal/{id}")
    R<BatchYieldDTO> getBatch(@PathVariable("id") Long id);
}
