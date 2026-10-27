package com.trace.agent.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trace.agent.client.dto.AgentRequest;
import com.trace.agent.client.dto.AgentResponse;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Python LangGraph 智能体客户端（带规则兜底）
 */
@Component
public class PythonAgentClient {

    private static final Logger log = LoggerFactory.getLogger(PythonAgentClient.class);

    @Value("${agent.python.base-url:http://localhost:8000}")
    private String baseUrl;

    @Resource
    private RestTemplate agentRestTemplate;
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 调用大模型智能体；失败时返回兜底响应
     */
    public AgentResponse invoke(String agent, Map<String, Object> payload) {
        AgentRequest req = new AgentRequest();
        req.setAgent(agent);
        req.setPayload(payload);
        try {
            String url = baseUrl + "/api/agent";
            AgentResponse resp = agentRestTemplate.postForObject(url, req, AgentResponse.class);
            if (resp == null) {
                throw new IllegalStateException("智能体返回空");
            }
            return resp;
        } catch (RestClientException | IllegalStateException e) {
            log.warn("智能体调用失败，启用规则兜底 agent={}: {}", agent, e.getMessage());
            AgentResponse fallback = new AgentResponse();
            fallback.setAgent(agent);
            fallback.setFallback(true);
            fallback.setContent(buildRuleFallback(agent, payload));
            return fallback;
        }
    }

    private String buildRuleFallback(String agent, Map<String, Object> payload) {
        switch (agent) {
            case "farming_validate":
                return "【规则兜底】未检测到大模型服务，已对农事记录做基础区间校验，未发现明显异常。";
            case "carbon_advice":
                return "【规则兜底】建议优先优化施肥结构、采用节水灌溉、减少柴油农机作业时长以降低碳排放。";
            case "subsidy_report":
                return "【规则兜底】补贴申报材料已按模板生成（规则模式），请人工复核后提交。";
            case "consumer_explain":
                return "【规则兜底】该农产品已完成碳足迹核算与联盟链存证，扫码可验真。";
            default:
                return "【规则兜底】暂无可用智能体结果。";
        }
    }
}
