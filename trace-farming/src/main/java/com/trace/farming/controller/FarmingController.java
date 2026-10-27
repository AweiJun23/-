package com.trace.farming.controller;

import com.trace.common.context.UserContext;
import com.trace.common.exception.BizException;
import com.trace.common.result.R;
import com.trace.common.enums.ResultCode;
import com.trace.farming.dto.OfflineSyncRequest;
import com.trace.farming.dto.SubmitRecordRequest;
import com.trace.farming.service.FarmingService;
import com.trace.farming.vo.FarmingRecordVO;
import java.util.List;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 农事记录接口
 */
@RestController
@RequestMapping("/api/farming")
public class FarmingController {

    @Resource
    private FarmingService farmingService;

    /** 单条农事记录提交 */
    @PostMapping("/record")
    public R<Long> submit(@Valid @RequestBody SubmitRecordRequest request,
                          @RequestHeader(value = "X-Source", required = false) String source) {
        return R.ok(farmingService.submitSingle(request, source));
    }

    /** 离线批量同步（APP 弱网缓存后联网回传） */
    @PostMapping("/sync")
    public R<Integer> sync(@Valid @RequestBody OfflineSyncRequest request,
                           @RequestHeader(value = "X-Source", required = false) String source) {
        return R.ok(farmingService.syncBatch(request, source));
    }

    /** 查询批次农事记录（溯源时间线） */
    @GetMapping("/list/{batchId}")
    public R<List<FarmingRecordVO>> list(@PathVariable("batchId") Long batchId) {
        return R.ok(farmingService.listByBatch(batchId));
    }

    /** 内部接口：供批次服务聚合溯源时间线 */
    @GetMapping("/internal/list/{batchId}")
    public R<List<FarmingRecordVO>> internalList(@PathVariable("batchId") Long batchId) {
        return R.ok(farmingService.listByBatch(batchId));
    }

    /** 内部接口：AI 校验结果回写 */
    @PostMapping("/internal/check-result")
    public R<Void> writeBack(@RequestBody CheckResultRequest request) {
        if (UserContext.get() == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        farmingService.writeBackCheckResult(request.getRecordId(), request.getStatus(),
                request.getRemark(), request.getAiAdvice());
        return R.ok();
    }

    /** 内部请求体 */
    public static class CheckResultRequest {
        private Long recordId;
        private String status;
        private String remark;
        private String aiAdvice;

        public Long getRecordId() { return recordId; }
        public void setRecordId(Long recordId) { this.recordId = recordId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public String getAiAdvice() { return aiAdvice; }
        public void setAiAdvice(String aiAdvice) { this.aiAdvice = aiAdvice; }
    }
}
