package com.trace.agent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 调用 Python LangGraph 智能体服务的 HTTP 客户端
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate agentRestTemplate() {
        return new RestTemplate();
    }
}
