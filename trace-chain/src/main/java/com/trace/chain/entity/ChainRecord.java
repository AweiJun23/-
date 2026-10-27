package com.trace.chain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.trace.common.domain.BaseEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 链上存证记录（仅哈希摘要）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("chain_record")
public class ChainRecord extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 业务类型：FARMING_RECORD / CARBON_REPORT */
    private String bizType;

    /** 业务主键 */
    private String bizId;

    /** 业务数据哈希 */
    private String bizHash;

    /** 链上交易ID */
    private String txId;

    /** 区块高度 */
    private Long blockHeight;

    /** 上链时间 */
    private LocalDateTime chainTime;

    /** 状态 */
    private String status;
}
