package com.trace.report.client;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 批次精简视图
 */
@Data
public class BatchLiteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String batchNo;
    private Long farmerId;
    private String productName;
    private String originProvince;
    private String originCity;
    private String originDetail;
    private BigDecimal actualYield;
    private String status;
}
