package com.trace.report;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 补贴报表服务
 */
@SpringBootApplication(scanBasePackages = "com.trace")
@MapperScan("com.trace.report.mapper")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.trace.report.client")
public class TraceReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(TraceReportApplication.class, args);
    }
}
