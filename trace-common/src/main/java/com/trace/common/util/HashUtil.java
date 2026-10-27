package com.trace.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA256 摘要工具 —— 联盟链哈希存证的核心
 * <p>
 * 设计要点：仅把业务数据的 SHA256 摘要写入联盟链，原始数据留在本地业务库，
 * 兼顾存证防篡改能力与存储成本。
 */
public class HashUtil {

    private HashUtil() {
    }

    /**
     * 计算 SHA256 摘要（64 位十六进制小写）
     */
    public static String sha256(String plain) {
        if (plain == null) {
            plain = "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * 二次摘要：先取 sha256 再签名盐值，用于模拟联盟链交易 id
     */
    public static String chainTxId(String bizHash, long timestamp) {
        return sha256(bizHash + "|" + timestamp);
    }
}
