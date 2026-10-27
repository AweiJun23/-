package com.trace.carbon.client;

import com.trace.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 调用 AI 智能体调度服务，生成低碳优化建议
 */
@FeignClient(name = "trace-agent", url = "${feign.target.agent:http://127.0.0.1:8086}")
public interface AgentClient {

    @PostMapping("/api/agent/carbon/advice")
    R<String> carbonAdvice(@RequestBody CarbonAdviceRequest request);
}
