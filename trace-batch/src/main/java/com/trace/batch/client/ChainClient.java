package com.trace.batch.client;

import com.trace.batch.client.dto.ChainVerifyLiteDTO;
import com.trace.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 调用联盟链存证服务，核验数据真实性
 */
@FeignClient(name = "trace-chain", url = "${feign.target.chain:http://127.0.0.1:8085}")
public interface ChainClient {

    @GetMapping("/api/chain/internal/by-biz/{bizType}/{bizId}")
    R<ChainVerifyLiteDTO> verifyByBiz(@PathVariable("bizType") String bizType,
                                      @PathVariable("bizId") String bizId);
}
