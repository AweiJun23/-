package com.trace.agent.client;

import lombok.Data;

import java.io.Serializable;

/**
 * 农事记录校验结果回写
 */
@Data
public class CheckResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long recordId;
    private String status;
    private String remark;
    private String aiAdvice;
}
