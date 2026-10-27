package com.trace.agent.controller;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 低碳建议请求体（与 trace-carbon 的 CarbonAdviceRequest 字段对齐）
 */
@Data
public class CarbonAdviceBody implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long batchId;
    private String batchNo;
    private List<ItemSummary> items;

    @Data
    public static class ItemSummary implements Serializable {
        private static final long serialVersionUID = 1L;
        private String farmingType;
        private String materialName;
        private BigDecimal emission;
        private BigDecimal proportion;
    }
}
