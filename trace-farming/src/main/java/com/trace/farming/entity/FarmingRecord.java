package com.trace.farming.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.trace.common.domain.BaseEntity;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 农事记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("farming_record")
public class FarmingRecord extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 批次 id */
    private Long batchId;

    /** 批次编号（冗余，便于 MQ 消费端免查库） */
    private String batchNo;

    /** 农户 id */
    private Long farmerId;

    /** 农事类型：FERTILIZE / IRRIGATE / MACHINE / PESTICIDE / FILM / HARVEST / PACKAGE / TRANSPORT */
    private String farmingType;

    /** 物料名称：尿素 / 柴油 / 灌溉用电 等 */
    private String materialName;

    /** 用量 */
    private BigDecimal amount;

    /** 单位：kg / L / kWh / mu */
    private String unit;

    /** 作业日期 */
    private LocalDate workDate;

    /** 作业面积（亩） */
    private BigDecimal areaMu;

    /** 现场照片，多个以逗号分隔 */
    private String images;

    /** 备注 */
    private String remark;

    /** 提交来源端：MINIAPP / APP */
    private String source;

    /** APP 离线同步令牌，用于批量同步幂等去重（同一批记录使用相同 token） */
    private String syncToken;

    /** AI 校验状态：WAITING / NORMAL / ABNORMAL */
    private String checkStatus;

    /** AI 校验结论 */
    private String checkRemark;

    /** 智能体生成的农事优化/校验建议（面向消费者与农户展示） */
    private String aiAdvice;

    /** 该记录的业务哈希（由存证服务回写） */
    private String recordHash;

    /** 是否已上链存证 */
    private Integer chained;
}
