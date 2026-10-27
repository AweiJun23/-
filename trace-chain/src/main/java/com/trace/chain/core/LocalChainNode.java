package com.trace.chain.core;

import com.trace.common.util.HashUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 联盟链轻节点模拟器（仅存哈希摘要）
 * <p>
 * 采用哈希链结构：每个存证区块记录前一区块哈希，形成不可篡改的链式结构。
 * 真实环境可替换为长安链 / FISCO BCOS 等联盟链轻节点 SDK，接口保持一致。
 */
public class LocalChainNode {

    private static final Logger log = LoggerFactory.getLogger(LocalChainNode.class);

    private final String ledgerPath;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, ChainEntry> index = new LinkedHashMap<>();
    private final AtomicLong height = new AtomicLong(0);
    private String prevHash = "0".repeat(64);

    public LocalChainNode(String ledgerPath) {
        this.ledgerPath = ledgerPath;
        load();
    }

    /** 对业务数据哈希进行存证，返回交易凭证 */
    public NotarizeResult notarize(String bizType, String bizId, String bizHash) {
        lock.writeLock().lock();
        try {
            long h = height.incrementAndGet();
            String txId = HashUtil.sha256(prevHash + "|" + bizType + "|" + bizId + "|" + bizHash + "|" + System.nanoTime());
            ChainEntry entry = new ChainEntry();
            entry.setHeight(h);
            entry.setBizType(bizType);
            entry.setBizId(bizId);
            entry.setBizHash(bizHash);
            entry.setTxId(txId);
            entry.setPrevHash(prevHash);
            entry.setChainTime(LocalDateTime.now());
            index.put(key(bizType, bizId), entry);
            appendLine(entry);
            prevHash = txId;
            return new NotarizeResult(txId, h, entry.getChainTime());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /** 查询某业务存证 */
    public ChainEntry query(String bizType, String bizId) {
        lock.readLock().lock();
        try {
            return index.get(key(bizType, bizId));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Data
    public static class ChainEntry {
        private long height;
        private String bizType;
        private String bizId;
        private String bizHash;
        private String txId;
        private String prevHash;
        private LocalDateTime chainTime;
    }

    @Data
    public static class NotarizeResult {
        private final String txId;
        private final long height;
        private final LocalDateTime chainTime;
    }

    private String key(String bizType, String bizId) {
        return bizType + "#" + bizId;
    }

    private void appendLine(ChainEntry e) {
        try (PrintWriter pw = new PrintWriter(
                new FileWriter(ledgerPath, StandardCharsets.UTF_8, true))) {
            pw.println(e.getHeight() + "|" + e.getBizType() + "|" + e.getBizId() + "|"
                    + e.getBizHash() + "|" + e.getTxId() + "|" + e.getPrevHash() + "|" + e.getChainTime());
        } catch (IOException ex) {
            log.error("联盟链账本追加失败: {}", ex.getMessage(), ex);
        }
    }

    private void load() {
        File f = new File(ledgerPath);
        if (!f.exists()) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length < 7) {
                    continue;
                }
                ChainEntry e = new ChainEntry();
                e.setHeight(Long.parseLong(p[0]));
                e.setBizType(p[1]);
                e.setBizId(p[2]);
                e.setBizHash(p[3]);
                e.setTxId(p[4]);
                e.setPrevHash(p[5]);
                e.setChainTime(LocalDateTime.parse(p[6]));
                index.put(key(e.getBizType(), e.getBizId()), e);
                height.set(Math.max(height.get(), e.getHeight()));
                prevHash = e.getTxId();
            }
            log.info("联盟链轻节点账本恢复完成，区块高度={}", height.get());
        } catch (Exception ex) {
            log.warn("联盟链账本恢复失败: {}", ex.getMessage());
        }
    }
}
