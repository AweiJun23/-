package com.trace.farming.dto;

import java.io.Serializable;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * APP 离线批量同步请求
 * <p>
 * 农村弱网场景下，APP 端將农事表单暂存在本地 SQLite，联网后一次性提交多条。
 */
@Data
public class OfflineSyncRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** APP 本地生成，用于幂等去重 */
    private String syncToken;

    @Valid
    @NotEmpty(message = "同步记录不能为空")
    private List<SubmitRecordRequest> records;
}
