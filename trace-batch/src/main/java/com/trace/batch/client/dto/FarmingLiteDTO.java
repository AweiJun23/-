package com.trace.batch.client.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 农事记录精简视图（跨服务聚合用）
 */
@Data
public class FarmingLiteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long recordId;
    private Long batchId;
    private String farmingType;
    private String farmingTypeDesc;
    private String materialName;
    private BigDecimal amount;
    private String unit;
    private LocalDate workDate;
    private String source;
    private String checkStatus;
    private String checkRemark;
    private String aiAdvice;
}
