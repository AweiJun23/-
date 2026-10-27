package com.trace.report.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 补贴申报视图
 */
@Data
public class SubsidyReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long batchId;
    private Long farmerId;
    private String reportNo;
    private String title;
    private String contentMd;
    private String status;
    private String reportHash;
    private LocalDateTime generatedTime;
}
