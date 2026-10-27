package com.trace.carbon.vo;

import com.trace.carbon.entity.CarbonReportItem;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 碳足迹报告视图
 */
@Data
public class CarbonReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long reportId;
    private Long batchId;
    private String batchNo;
    private BigDecimal totalEmission;
    private BigDecimal unitEmission;
    private String aiAdvice;
    private String reportHash;
    private String status;
    private LocalDateTime reportTime;
    private List<CarbonReportItem> items;
}
