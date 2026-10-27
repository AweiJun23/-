package com.trace.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * AI 智能体调度服务
 */
@SpringBootApplication(scanBasePackages = "com.trace")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.trace.agent.client")
public class TraceAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(TraceAgentApplication.class, args);
    }
}
