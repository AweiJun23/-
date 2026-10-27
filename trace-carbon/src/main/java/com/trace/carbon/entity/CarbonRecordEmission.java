package com.trace.carbon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 单条农事记录的碳排放计算明细（核算服务内部表）
 */
@Data
@TableName("carbon_record_emission")
public class CarbonRecordEmission implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    /** 农事记录ID */
    private Long recordId;
    /** 批次ID */
    private Long batchId;
    /** 农事类型 */
    private String farmingType;
    /** 物料 */
    private String materialName;
    /** 用量 */
    private BigDecimal amount;
    /** 单位 */
    private String unit;
    /** 采用因子 */
    private BigDecimal factorValue;
    /** 该记录排放（kgCO2e） */
    private BigDecimal emission;
    private Integer deleted;
}
