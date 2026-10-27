package com.trace.farming.config;

import com.trace.common.constant.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 农事记录服务 MQ 配置（生产侧）
 * <p>
 * 采用 fanout 广播交换机：一条农事记录被保存后，并行触发三条链路
 * —— 碳足迹核算、AI 智能体分析、联盟链哈希存证，彼此互不阻塞。
 */
@Configuration
public class MqConfig {

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public FanoutExchange farmingEventExchange() {
        // durable=true：MQ 重启后交换机与消息不丢失
        return new FanoutExchange(MqConstants.EXCHANGE_FARMING_EVENT, true, false);
    }

    @Bean
    public Queue carbonCalcQueue() {
        return new Queue(MqConstants.QUEUE_CARBON_CALC, true);
    }

    @Bean
    public Queue aiAgentQueue() {
        return new Queue(MqConstants.QUEUE_AI_AGENT, true);
    }

    @Bean
    public Queue chainNotaryQueue() {
        return new Queue(MqConstants.QUEUE_CHAIN_NOTARY_FARMING, true);
    }

    @Bean
    public Binding bindCarbonCalc() {
        return BindingBuilder.bind(carbonCalcQueue()).to(farmingEventExchange());
    }

    @Bean
    public Binding bindAiAgent() {
        return BindingBuilder.bind(aiAgentQueue()).to(farmingEventExchange());
    }

    @Bean
    public Binding bindChainNotary() {
        return BindingBuilder.bind(chainNotaryQueue()).to(farmingEventExchange());
    }
}
