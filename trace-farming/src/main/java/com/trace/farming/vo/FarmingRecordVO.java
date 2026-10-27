package com.trace.farming.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

/**
 * 农事记录展示视图（溯源时间线 / 农户端列表）
 */
@Data
public class FarmingRecordVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long batchId;
    private String batchNo;
    private Long farmerId;

    /** 农事类型编码：FERTILIZE / IRRIGATE / MACHINE ... */
    private String farmingType;
    /** 农事类型中文描述，如「施肥」「灌溉」 */
    private String farmingTypeDesc;

    private String materialName;
    private BigDecimal amount;
    private String unit;
    private LocalDate workDate;
    private BigDecimal areaMu;
    private String images;
    private String remark;
    private String source;
    private String syncToken;

    /** AI 校验状态：WAITING / NORMAL / ABNORMAL */
    private String checkStatus;
    /** AI 校验结论 */
    private String checkRemark;
    /** 智能体建议（消费者/农户可读） */
    private String aiAdvice;

    /** 业务哈希（链上存证后回写） */
    private String recordHash;
    /** 是否已上链存证 */
    private Integer chained;
}
