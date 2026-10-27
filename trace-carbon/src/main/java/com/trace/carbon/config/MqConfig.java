package com.trace.carbon.config;

import com.trace.common.constant.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 碳足迹核算服务 MQ 配置
 */
@Configuration
public class MqConfig {

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /** 碳报告事件交换机（核算 -> 联盟链存证） */
    @Bean
    public FanoutExchange carbonReportExchange() {
        return new FanoutExchange(MqConstants.EXCHANGE_CARBON_REPORT, true, false);
    }

    @Bean
    public Queue chainNotaryCarbonQueue() {
        return new Queue(MqConstants.QUEUE_CHAIN_NOTARY_CARBON, true);
    }

    @Bean
    public Binding bindChainNotaryCarbon() {
        return BindingBuilder.bind(chainNotaryCarbonQueue()).to(carbonReportExchange());
    }
}
