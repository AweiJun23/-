package com.trace.chain.listener;

import com.rabbitmq.client.Channel;
import com.trace.chain.service.ChainService;
import com.trace.common.domain.event.FarmingRecordEvent;
import com.trace.common.util.HashUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 农事记录上链监听器
 */
@Slf4j
@Component
public class FarmingRecordChainListener {

    @Resource
    private ChainService chainService;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "chain.notary.farming.queue")
    public void onEvent(Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            FarmingRecordEvent event = (FarmingRecordEvent) rabbitTemplate.getMessageConverter().fromMessage(message);
            String hash = HashUtil.sha256(event.getRecordId() + "|" + event.getBatchId() + "|"
                    + event.getFarmingType() + "|" + event.getMaterialName() + "|"
                    + event.getAmount() + "|" + event.getUnit() + "|" + event.getWorkDate() + "|"
                    + event.getAreaMu() + "|" + event.getSource());
            String txId = chainService.notarize(com.trace.common.constant.Constants.CHAIN_BIZ_FARMING,
                    String.valueOf(event.getRecordId()), hash);
            channel.basicAck(tag, false);
            log.info("农事记录已上链 recordId={} txId={}", event.getRecordId(), txId);
        } catch (Exception e) {
            log.error("农事记录上链失败: {}", e.getMessage(), e);
            channel.basicNack(tag, false, false);
        }
    }
}
