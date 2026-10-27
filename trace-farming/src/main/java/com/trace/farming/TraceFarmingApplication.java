package com.trace.farming;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 农事记录服务
 */
@SpringBootApplication(scanBasePackages = "com.trace")
@MapperScan("com.trace.farming.mapper")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.trace.farming.client")
public class TraceFarmingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TraceFarmingApplication.class, args);
    }
}
