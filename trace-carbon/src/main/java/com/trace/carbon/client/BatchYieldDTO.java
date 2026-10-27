package com.trace.carbon.client;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 批次产量视图
 */
@Data
public class BatchYieldDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String batchNo;
    private BigDecimal actualYield;
    private BigDecimal areaMu;
}
