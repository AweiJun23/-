package com.trace.agent.service;

import com.trace.agent.client.dto.AgentResponse;
import java.util.List;
import java.util.Map;

/**
 * AI 智能体调度业务接口
 */
public interface AgentService {

    /** 农事数据异常校验 */
    AgentResponse validateFarming(List<Map<String, Object>> records);

    /** 低碳种植优化建议 */
    AgentResponse carbonAdvice(List<Map<String, Object>> items);

    /** 补贴申报材料生成 */
    AgentResponse subsidyReport(Map<String, Object> payload);

    /** 消费者碳标签解读 */
    AgentResponse consumerExplain(Map<String, Object> payload);
}
