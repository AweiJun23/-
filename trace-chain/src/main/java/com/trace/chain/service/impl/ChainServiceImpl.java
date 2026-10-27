package com.trace.chain.service.impl;

import com.trace.chain.client.dto.ChainVerifyLiteDTO;
import com.trace.chain.core.LocalChainNode;
import com.trace.chain.entity.ChainRecord;
import com.trace.chain.mapper.ChainRecordMapper;
import com.trace.chain.service.ChainService;
import java.time.LocalDateTime;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 联盟链存证业务实现
 */
@Service
public class ChainServiceImpl implements ChainService {

    @Resource
    private ChainRecordMapper chainRecordMapper;

    /** 账本落盘路径（生产环境应使用共享存储或联盟链节点） */
    private static final String LEDGER_PATH = "./data/chain-ledger.log";

    private LocalChainNode node;

    @PostConstruct
    public void init() {
        node = new LocalChainNode(LEDGER_PATH);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String notarize(String bizType, String bizId, String bizHash) {
        LocalChainNode.NotarizeResult result = node.notarize(bizType, bizId, bizHash);

        // 本地库仅作为索引，原始账本在轻节点中
        ChainRecord record = new ChainRecord();
        record.setBizType(bizType);
        record.setBizId(bizId);
        record.setBizHash(bizHash);
        record.setTxId(result.getTxId());
        record.setBlockHeight(result.getHeight());
        record.setChainTime(result.getChainTime());
        record.setStatus("CONFIRMED");
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        record.setDeleted(0);
        record.setVersion(0);
        chainRecordMapper.insert(record);
        return result.getTxId();
    }

    @Override
    public ChainVerifyLiteDTO verifyByBiz(String bizType, String bizId, String expectedHash) {
        ChainRecord record = chainRecordMapper.selectByBiz(bizType, bizId);
        ChainVerifyLiteDTO dto = new ChainVerifyLiteDTO();
        dto.setBizType(bizType);
        dto.setBizId(bizId);
        if (record == null) {
            dto.setVerified(false);
            dto.setReason("未找到链上存证记录");
            return dto;
        }
        BeanUtils.copyProperties(record, dto);
        if (StringUtils.hasText(expectedHash)) {
            boolean match = expectedHash.equals(record.getBizHash());
            dto.setVerified(match);
            dto.setReason(match ? "哈希一致，数据未被篡改" : "哈希不一致，数据可能被篡改");
        } else {
            dto.setVerified(true);
            dto.setReason("已上链存证，可溯源");
        }
        return dto;
    }
}
