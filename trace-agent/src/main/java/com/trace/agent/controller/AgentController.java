package com.trace.agent.controller;

import com.trace.agent.client.dto.AgentResponse;
import com.trace.agent.service.AgentService;
import com.trace.common.result.R;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 智能体调度接口
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    @Resource
    private AgentService agentService;

    @PostMapping("/farming/validate")
    public R<AgentResponse> validateFarming(@RequestBody List<Map<String, Object>> records) {
        return R.ok(agentService.validateFarming(records));
    }

    @PostMapping("/carbon/advice")
    public R<String> carbonAdvice(@RequestBody CarbonAdviceBody body) {
        AgentResponse resp = agentService.carbonAdvice(convert(body));
        return R.ok(resp.getContent());
    }

    @PostMapping("/subsidy/report")
    public R<AgentResponse> subsidyReport(@RequestBody Map<String, Object> payload) {
        return R.ok(agentService.subsidyReport(payload));
    }

    @PostMapping("/consumer/explain")
    public R<AgentResponse> consumerExplain(@RequestBody Map<String, Object> payload) {
        return R.ok(agentService.consumerExplain(payload));
    }

    private List<Map<String, Object>> convert(CarbonAdviceBody body) {
        // 将分项转换为 agent 期望的 items 结构
        List<Map<String, Object>> items = new java.util.ArrayList<>();
        if (body.getItems() != null) {
            for (CarbonAdviceBody.ItemSummary s : body.getItems()) {
                Map<String, Object> m = new java.util.HashMap<>();
                m.put("farmingType", s.getFarmingType());
                m.put("materialName", s.getMaterialName());
                m.put("emission", s.getEmission());
                m.put("proportion", s.getProportion());
                items.add(m);
            }
        }
        return items;
    }
}
