package com.trace.batch.client.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 链上存证验真结果精简视图
 */
@Data
public class ChainVerifyLiteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String bizType;
    private String bizId;
    private String bizHash;
    private String txId;
    private LocalDateTime chainTime;
    private Boolean verified;
    private String reason;
}
