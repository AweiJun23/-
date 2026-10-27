package com.trace.agent.client.dto;

import java.io.Serializable;
import java.util.Map;
import lombok.Data;

/**
 * 智能体请求
 */
@Data
public class AgentRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 智能体类型：farming_validate / carbon_advice / subsidy_report / consumer_explain */
    private String agent;

    /** 业务负载 */
    private Map<String, Object> payload;
}
