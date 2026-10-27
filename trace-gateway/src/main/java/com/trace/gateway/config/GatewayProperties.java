package com.trace.gateway.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 网关白名单 / 限流参数
 */
@Component
@ConfigurationProperties(prefix = "trace.gateway")
public class GatewayProperties {

    /** 免鉴权路径 */
    private List<String> whitelist;
    /** 限流配置 */
    private RateLimit rateLimit = new RateLimit();

    public static class RateLimit {
        /** 每秒生成令牌数 */
        private int replenishRate = 20;
        /** 令牌桶容量 */
        private int burstCapacity = 50;

        public int getReplenishRate() { return replenishRate; }
        public void setReplenishRate(int replenishRate) { this.replenishRate = replenishRate; }
        public int getBurstCapacity() { return burstCapacity; }
        public void setBurstCapacity(int burstCapacity) { this.burstCapacity = burstCapacity; }
    }

    public List<String> getWhitelist() { return whitelist; }
    public void setWhitelist(List<String> whitelist) { this.whitelist = whitelist; }
    public RateLimit getRateLimit() { return rateLimit; }
    public void setRateLimit(RateLimit rateLimit) { this.rateLimit = rateLimit; }
}
