package com.trace.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 用户权限服务启动类
 * <p>
 * scanBasePackages 指向 com.trace 根目录，以加载 trace-common 中的
 * 全局异常处理、鉴权拦截器、MyBatis-Plus 公共配置等自动装配能力。
 */
@EnableDiscoveryClient
@MapperScan("com.trace.auth.mapper")
@SpringBootApplication(scanBasePackages = "com.trace")
public class TraceAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(TraceAuthApplication.class, args);
    }
}
