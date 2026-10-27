package com.trace.agent.client.dto;

import java.io.Serializable;
import lombok.Data;

/**
 * 智能体响应
 */
@Data
public class AgentResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String agent;
    /** 生成的自然语言内容 */
    private String content;
    /** 结构化结果（异常校验结果、报表路径等） */
    private Object data;
    /** 是否由规则兜底生成（大模型不可用） */
    private boolean fallback;
}
