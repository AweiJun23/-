package com.trace.farming.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.Data;

/**
 * 农事记录提交请求（小程序 / APP 单条录入）
 */
@Data
public class SubmitRecordRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "批次 id 不能为空")
    private Long batchId;

    /** 批次编号（冗余，便于前端带出） */
    private String batchNo;

    @NotNull(message = "农事类型不能为空")
    private String farmingType;

    private String materialName;

    @NotNull(message = "用量不能为空")
    @Positive(message = "用量必须大于 0")
    private BigDecimal amount;

    /** 单位：kg / L / kWh / mu */
    private String unit;

    @NotNull(message = "作业日期不能为空")
    private LocalDate workDate;

    private BigDecimal areaMu;

    /** 现场照片 URL 列表 */
    private List<String> images;

    /** APP 离线同步令牌，用于批量同步幂等去重 */
    private String syncToken;

    private String remark;
}
