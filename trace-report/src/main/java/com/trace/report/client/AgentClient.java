package com.trace.report.client;

import com.trace.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 调用 AI 智能体服务
 */
@FeignClient(name = "trace-agent")
public interface AgentClient {

    @PostMapping("/api/agent/subsidy/report")
    R<AgentResponseDTO> subsidyReport(@RequestBody java.util.Map<String, Object> payload);
}
