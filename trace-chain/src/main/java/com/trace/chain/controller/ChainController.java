package com.trace.chain.controller;

import com.trace.chain.client.dto.ChainVerifyLiteDTO;
import com.trace.chain.service.ChainService;
import com.trace.common.result.R;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 联盟链存证接口
 */
@RestController
@RequestMapping("/api/chain")
public class ChainController {

    @Resource
    private ChainService chainService;

    /** 公开验真：传入业务数据与链上哈希比对 */
    @PostMapping("/verify")
    public R<ChainVerifyLiteDTO> verify(@RequestBody VerifyRequest request) {
        return R.ok(chainService.verifyByBiz(request.getBizType(), request.getBizId(), request.getDataHash()));
    }

    /** 内部接口：供批次服务聚合验真 */
    @GetMapping("/internal/by-biz/{bizType}/{bizId}")
    public R<ChainVerifyLiteDTO> internalVerify(@PathVariable("bizType") String bizType,
                                                @PathVariable("bizId") String bizId,
                                                @RequestParam(value = "expectedHash", required = false) String expectedHash) {
        return R.ok(chainService.verifyByBiz(bizType, bizId, expectedHash));
    }

    /** 按业务数据哈希直接查询 */
    @GetMapping("/verify/hash")
    public R<ChainVerifyLiteDTO> verifyByHash(@RequestParam("bizType") String bizType,
                                              @RequestParam("bizId") String bizId) {
        return R.ok(chainService.verifyByBiz(bizType, bizId, null));
    }

    public static class VerifyRequest {
        private String bizType;
        private String bizId;
        private String dataHash;

        public String getBizType() { return bizType; }
        public void setBizType(String bizType) { this.bizType = bizType; }
        public String getBizId() { return bizId; }
        public void setBizId(String bizId) { this.bizId = bizId; }
        public String getDataHash() { return dataHash; }
        public void setDataHash(String dataHash) { this.dataHash = dataHash; }
    }
}
