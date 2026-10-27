package com.trace.report.client;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 碳报告精简视图
 */
@Data
public class CarbonReportLiteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long reportId;
    private BigDecimal totalEmission;
    private BigDecimal unitEmission;
    private String aiAdvice;
    private String reportHash;
}
