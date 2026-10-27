package com.trace.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 公共基础服务：文件存储（农事现场照片）、统一日志
 */
@SpringBootApplication(scanBasePackages = "com.trace")
@EnableDiscoveryClient
public class TraceFileApplication {

    public static void main(String[] args) {
        SpringApplication.run(TraceFileApplication.class, args);
    }
}
