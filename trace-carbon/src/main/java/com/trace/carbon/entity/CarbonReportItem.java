package com.trace.carbon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 碳足迹分项明细
 */
@Data
@TableName("carbon_report_item")
public class CarbonReportItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private Long reportId;
    private String farmingType;
    private String materialName;
    private BigDecimal amount;
    private String unit;
    private BigDecimal factorValue;
    private BigDecimal emission;
    /** 占比（%） */
    private BigDecimal proportion;
    private Integer deleted;
}
