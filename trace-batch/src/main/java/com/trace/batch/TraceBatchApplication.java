package com.trace.batch;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 农产品批次服务
 */
@SpringBootApplication(scanBasePackages = "com.trace")
@MapperScan("com.trace.batch.mapper")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.trace.batch.client")
public class TraceBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(TraceBatchApplication.class, args);
    }
}
