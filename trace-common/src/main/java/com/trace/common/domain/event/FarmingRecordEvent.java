package com.trace.common.domain.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 农事记录已提交事件（MQ 广播体）
 * <p>
 * 农事记录服务保存原始记录后发布该事件，由以下三方并行消费：
 * 碳足迹核算服务 / AI 智能体调度服务 / 联盟链存证服务
 */
public class FarmingRecordEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 农事记录 id */
    private Long recordId;
    /** 批次 id */
    private Long batchId;
    /** 批次编号 */
    private String batchNo;
    /** 农户 id */
    private Long farmerId;
    /** 农事类型：FERTILIZE / IRRIGATE / MACHINE / PESTICIDE / FILM / HARVEST / PACKAGE */
    private String farmingType;
    /** 物料名称，如：尿素、柴油、农膜 */
    private String materialName;
    /** 用量 */
    private BigDecimal amount;
    /** 单位：kg / L / kWh / mu */
    private String unit;
    /** 作业时间 */
    private LocalDate workDate;
    /** 作业面积（亩） */
    private BigDecimal areaMu;
    /** 提交来源端：MINIAPP / APP */
    private String source;
    /** APP 离线同步令牌（幂等去重） */
    private String syncToken;
    /** 事件唯一 id */
    private String eventId;
    /** 事件发生时间 */
    private LocalDateTime occurTime;

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public Long getFarmerId() { return farmerId; }
    public void setFarmerId(Long farmerId) { this.farmerId = farmerId; }
    public String getFarmingType() { return farmingType; }
    public void setFarmingType(String farmingType) { this.farmingType = farmingType; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }
    public BigDecimal getAreaMu() { return areaMu; }
    public void setAreaMu(BigDecimal areaMu) { this.areaMu = areaMu; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getSyncToken() { return syncToken; }
    public void setSyncToken(String syncToken) { this.syncToken = syncToken; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public LocalDateTime getOccurTime() { return occurTime; }
    public void setOccurTime(LocalDateTime occurTime) { this.occurTime = occurTime; }
}
