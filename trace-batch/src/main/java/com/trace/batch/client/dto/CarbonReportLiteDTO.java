package com.trace.batch.client.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 碳足迹报告精简视图（跨服务聚合用）
 */
@Data
public class CarbonReportLiteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long reportId;
    private Long batchId;
    private BigDecimal totalEmission;
    private BigDecimal unitEmission;
    private String aiAdvice;
    private String reportHash;
    private LocalDateTime reportTime;
    private List<CarbonItemLiteDTO> items;
}
