package com.trace.chain.listener;

import com.rabbitmq.client.Channel;
import com.trace.chain.service.ChainService;
import com.trace.common.domain.event.CarbonReportEvent;
import java.io.IOException;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 碳足迹报告上链监听器
 */
@Slf4j
@Component
public class CarbonReportChainListener {

    @Resource
    private ChainService chainService;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "chain.notary.carbon.queue")
    public void onEvent(Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            CarbonReportEvent event = (CarbonReportEvent) rabbitTemplate.getMessageConverter().fromMessage(message);
            String txId = chainService.notarize(com.trace.common.constant.Constants.CHAIN_BIZ_CARBON,
                    String.valueOf(event.getReportId()), event.getReportHash());
            channel.basicAck(tag, false);
            log.info("碳报告已上链 reportId={} txId={}", event.getReportId(), txId);
        } catch (Exception e) {
            log.error("碳报告上链失败: {}", e.getMessage(), e);
            channel.basicNack(tag, false, false);
        }
    }
}
