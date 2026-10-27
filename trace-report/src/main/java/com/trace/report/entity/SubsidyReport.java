package com.trace.report.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.trace.common.domain.BaseEntity;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 补贴申报报表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("subsidy_report")
public class SubsidyReport extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long batchId;
    private Long farmerId;
    private String reportNo;
    private String title;
    /** Markdown 申报材料 */
    private String contentMd;
    private String fileUrl;
    private String status;
    private String generatedBy;
    private LocalDateTime generatedTime;
    /** 报告哈希（供存证） */
    private String reportHash;
}
