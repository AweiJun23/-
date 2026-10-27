package com.trace.chain.service;

import com.trace.chain.client.dto.ChainVerifyLiteDTO;

/**
 * 联盟链存证业务接口
 */
public interface ChainService {

    /** 对业务数据哈希上链，返回交易ID */
    String notarize(String bizType, String bizId, String bizHash);

    /** 按业务查询验真结果；expectedHash 非空时校验是否被篡改 */
    ChainVerifyLiteDTO verifyByBiz(String bizType, String bizId, String expectedHash);
}
