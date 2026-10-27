package com.trace.carbon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.trace.common.domain.BaseEntity;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 碳足迹报告（按批次聚合）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("carbon_report")
public class CarbonReport extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long batchId;
    private String batchNo;

    /** 全流程总排放（kgCO2e） */
    private BigDecimal totalEmission;

    /** 单位产量碳排放（kgCO2e / kg 产品） */
    private BigDecimal unitEmission;

    /** AI 低碳优化建议 */
    private String aiAdvice;

    /** 报告哈希（供链上存证） */
    private String reportHash;

    /** 报告状态 */
    private String status;

    private LocalDateTime reportTime;
}
