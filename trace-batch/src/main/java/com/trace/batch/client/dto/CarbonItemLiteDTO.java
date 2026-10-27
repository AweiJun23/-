package com.trace.batch.client.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 碳足迹分项明细精简视图
 */
@Data
public class CarbonItemLiteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String farmingType;
    private String materialName;
    private BigDecimal amount;
    private String unit;
    private BigDecimal factorValue;
    private BigDecimal emission;
    private BigDecimal proportion;
}
