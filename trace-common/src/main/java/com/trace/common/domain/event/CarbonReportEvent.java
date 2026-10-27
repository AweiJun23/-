package com.trace.common.domain.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 碳足迹报告生成事件（核算服务 -> 联盟链存证服务）
 */
public class CarbonReportEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventId;
    private Long reportId;
    private Long batchId;
    private String batchNo;
    private BigDecimal totalEmission;
    private BigDecimal unitEmission;
    private String reportHash;
    private LocalDateTime reportTime;
    private LocalDateTime occurTime;

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public BigDecimal getTotalEmission() { return totalEmission; }
    public void setTotalEmission(BigDecimal totalEmission) { this.totalEmission = totalEmission; }
    public BigDecimal getUnitEmission() { return unitEmission; }
    public void setUnitEmission(BigDecimal unitEmission) { this.unitEmission = unitEmission; }
    public String getReportHash() { return reportHash; }
    public void setReportHash(String reportHash) { this.reportHash = reportHash; }
    public LocalDateTime getReportTime() { return reportTime; }
    public void setReportTime(LocalDateTime reportTime) { this.reportTime = reportTime; }
    public LocalDateTime getOccurTime() { return occurTime; }
    public void setOccurTime(LocalDateTime occurTime) { this.occurTime = occurTime; }
}
