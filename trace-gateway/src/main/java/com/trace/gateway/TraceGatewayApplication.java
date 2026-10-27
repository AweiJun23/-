package com.trace.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 网关启动类
 * <p>
 * 注意：网关为 WebFlux 环境，排除 JDBC 自动配置以免误报数据源缺失。
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class TraceGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(TraceGatewayApplication.class, args);
    }
}
