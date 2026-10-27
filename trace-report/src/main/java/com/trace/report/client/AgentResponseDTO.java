package com.trace.report.client;

import lombok.Data;

import java.io.Serializable;

/**
 * 智能体响应精简视图
 */
@Data
public class AgentResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String agent;
    private String content;
    private Object data;
    private boolean fallback;
}
