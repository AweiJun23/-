package com.trace.batch.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.trace.common.domain.BaseEntity;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 农产品批次
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product_batch")
public class ProductBatch extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 批次编号（全局唯一，扫码入口标识） */
    private String batchNo;

    /** 所属农户 */
    private Long farmerId;

    /** 所属合作社 */
    private Long coopId;

    /** 产品名称 */
    private String productName;

    /** 产地省 */
    private String originProvince;

    /** 产地市 */
    private String originCity;

    /** 产地详细地址 */
    private String originDetail;

    /** 种植开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate plantStartDate;

    /** 种植结束日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate plantEndDate;

    /** 种植面积（亩） */
    private BigDecimal areaMu;

    /** 预估产量（kg） */
    private BigDecimal expectedYield;

    /** 实际产量（kg） */
    private BigDecimal actualYield;

    /** 状态：GROWING/HARVESTED/STORED */
    private String status;

    /** 二维码 Base64（或访问地址） */
    private String qrCode;

    /** 二维码访问地址 */
    private String qrUrl;

    /** 流转信息（JSON 字符串，记录收购/运输/上架节点） */
    private String flowInfo;
}
