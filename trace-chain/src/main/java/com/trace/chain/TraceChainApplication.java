package com.trace.chain;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 联盟链存证服务
 */
@SpringBootApplication(scanBasePackages = "com.trace")
@MapperScan("com.trace.chain.mapper")
@EnableDiscoveryClient
public class TraceChainApplication {

    public static void main(String[] args) {
        SpringApplication.run(TraceChainApplication.class, args);
    }
}
