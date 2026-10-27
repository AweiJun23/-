package com.trace.carbon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 碳足迹核算服务
 */
@SpringBootApplication(scanBasePackages = "com.trace")
@MapperScan("com.trace.carbon.mapper")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.trace.carbon.client")
public class TraceCarbonApplication {

    public static void main(String[] args) {
        SpringApplication.run(TraceCarbonApplication.class, args);
    }
}
