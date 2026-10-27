package com.trace.agent.client;

import com.trace.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 调用农事记录服务，回写 AI 校验结果
 */
@FeignClient(name = "trace-farming", url = "${feign.target.farming:http://127.0.0.1:8082}")
public interface FarmingClient {

    @PostMapping("/api/farming/internal/check-result")
    R<Void> writeBackCheckResult(@RequestBody CheckResultDTO dto);
}
