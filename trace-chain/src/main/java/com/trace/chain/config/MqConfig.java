package com.trace.chain.config;

import com.trace.common.constant.MqConstants;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 联盟链存证服务 MQ 配置（仅声明消费队列）
 */
@Configuration
public class MqConfig {

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue chainNotaryFarmingQueue() {
        return new Queue(MqConstants.QUEUE_CHAIN_NOTARY_FARMING, true);
    }

    @Bean
    public Queue chainNotaryCarbonQueue() {
        return new Queue(MqConstants.QUEUE_CHAIN_NOTARY_CARBON, true);
    }
}
