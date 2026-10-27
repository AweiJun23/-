package com.trace.batch.client;

import com.trace.batch.client.dto.FarmingLiteDTO;
import com.trace.common.result.R;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 调用农事记录服务，拉取批次时间线
 */
@FeignClient(name = "trace-farming", url = "${feign.target.farming:http://127.0.0.1:8082}")
public interface FarmingClient {

    @GetMapping("/api/farming/internal/list/{batchId}")
    R<List<FarmingLiteDTO>> listByBatch(@PathVariable("batchId") Long batchId);
}
