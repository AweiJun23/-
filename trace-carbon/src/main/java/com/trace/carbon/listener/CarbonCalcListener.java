package com.trace.carbon.listener;

import com.rabbitmq.client.Channel;
import com.trace.carbon.service.CarbonService;
import com.trace.common.domain.event.FarmingRecordEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 碳足迹核算监听器：消费农事记录事件，异步生成碳报告
 */
@Slf4j
@Component
public class CarbonCalcListener {

    @Resource
    private CarbonService carbonService;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "carbon.calc.queue")
    public void onEvent(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            FarmingRecordEvent event = (FarmingRecordEvent) rabbitTemplate.getMessageConverter()
                    .fromMessage(message);
            carbonService.computeOnFarmingEvent(event);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("碳核算消费失败，进入死信: {}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
