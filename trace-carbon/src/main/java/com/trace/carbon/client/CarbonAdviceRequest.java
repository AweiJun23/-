package com.trace.carbon.client;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 低碳优化建议请求
 */
@Data
public class CarbonAdviceRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long batchId;
    private String batchNo;
    /** 各分项：农事类型/物料/排放/占比 */
    private List<ItemSummary> items;

    @Data
    public static class ItemSummary implements Serializable {
        private static final long serialVersionUID = 1L;
        private String farmingType;
        private String materialName;
        private java.math.BigDecimal emission;
        private java.math.BigDecimal proportion;
    }
}
