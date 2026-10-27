package com.trace.farming.service;

import com.trace.farming.dto.OfflineSyncRequest;
import com.trace.farming.dto.SubmitRecordRequest;
import com.trace.farming.vo.FarmingRecordVO;
import java.util.List;

/**
 * 农事记录业务接口
 */
public interface FarmingService {

    /**
     * 单条农事记录提交（小程序 / APP 在线提交）
     */
    Long submitSingle(SubmitRecordRequest request, String source);

    /**
     * 离线批量同步（APP 弱网缓存后联网回传）
     */
    int syncBatch(OfflineSyncRequest request, String source);

    /**
     * 查询某批次的全部农事记录（溯源时间线）
     */
    List<FarmingRecordVO> listByBatch(Long batchId);

    /**
     * AI 校验结果回写（由 agent 服务调用）
     */
    void writeBackCheckResult(Long recordId, String status, String remark, String aiAdvice);
}
