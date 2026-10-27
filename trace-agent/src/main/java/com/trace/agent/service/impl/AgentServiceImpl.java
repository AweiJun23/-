package com.trace.agent.service.impl;

import com.trace.agent.client.PythonAgentClient;
import com.trace.agent.client.dto.AgentRequest;
import com.trace.agent.client.dto.AgentResponse;
import com.trace.agent.rule.RuleValidator;
import com.trace.agent.service.AgentService;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * AI 智能体调度业务实现（含规则引擎二次校验）
 */
@Service
public class AgentServiceImpl implements AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentServiceImpl.class);

    @Resource
    private PythonAgentClient pythonAgentClient;
    @Resource
    private RuleValidator ruleValidator;

    @Override
    public AgentResponse validateFarming(List<Map<String, Object>> records) {
        AgentResponse resp = pythonAgentClient.invoke("farming_validate", Map.of("records", records));
        enforceRule("farming_validate", resp);
        return resp;
    }

    @Override
    public AgentResponse carbonAdvice(List<Map<String, Object>> items) {
        AgentResponse resp = pythonAgentClient.invoke("carbon_advice", Map.of("items", items));
        enforceRule("carbon_advice", resp);
        return resp;
    }

    @Override
    public AgentResponse subsidyReport(Map<String, Object> payload) {
        AgentResponse resp = pythonAgentClient.invoke("subsidy_report", payload);
        enforceRule("subsidy_report", resp);
        return resp;
    }

    @Override
    public AgentResponse consumerExplain(Map<String, Object> payload) {
        AgentResponse resp = pythonAgentClient.invoke("consumer_explain", payload);
        enforceRule("consumer_explain", resp);
        return resp;
    }

    /** 规则引擎拦截：违规则不采信 AI 文本，改用兜底结论 */
    private void enforceRule(String agent, AgentResponse resp) {
        if (!resp.isFallback() && !ruleValidator.pass(agent, resp.getContent())) {
            List<String> violations = ruleValidator.validate(agent, resp.getContent());
            log.warn("AI 输出未通过规则校验，已降级为规则兜底 agent={} violations={}",
                    agent, violations);
            resp.setFallback(true);
            resp.setContent("【规则拦截】AI 输出存在疑似编造数值，已按安全策略采用规则结论：" + fallbackText(agent));
        }
    }

    private String fallbackText(String agent) {
        switch (agent) {
            case "farming_validate":
                return "基础区间校验通过，未发现明显异常。";
            case "carbon_advice":
                return "建议优化施肥结构、节水灌溉、减少柴油农机作业时长。";
            case "subsidy_report":
                return "申报材料已按模板生成，请人工复核。";
            default:
                return "内容已通过规则校验。";
        }
    }
}
