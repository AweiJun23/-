package com.trace.carbon.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.trace.common.domain.BaseEntity;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 农业碳排放因子（国标基准库，按农事类型 + 物料维护）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("carbon_factor")
public class CarbonFactor extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 农事类型编码 */
    private String farmingType;

    /** 物料名称（如：尿素、柴油、电力） */
    private String materialName;

    /** 计量单位 */
    private String unit;

    /** 排放因子（kgCO2e / 单位） */
    private BigDecimal factorValue;

    /** 标准来源（如 GB/T XXXXX） */
    private String standardSource;

    /** 备注 */
    private String remark;
}
