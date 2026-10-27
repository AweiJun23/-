package com.trace.chain.client.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 链上存证验真结果精简视图
 * <p>
 * 与 trace-batch 端的同名 DTO 字段保持一致：verified / reason 供 H5 验真展示，
 * 其余字段供内部聚合接口复用。
 */
@Data
public class ChainVerifyLiteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String bizType;
    private String bizId;
    private String bizHash;
    private String txId;
    private Long blockHeight;
    private LocalDateTime chainTime;
    private String status;
    /** 验真是否通过（链上哈希与传入哈希一致） */
    private Boolean verified;
    /** 未通过原因 */
    private String reason;
}
