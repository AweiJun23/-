package com.trace.agent.listener;

import com.rabbitmq.client.Channel;
import com.trace.agent.client.CheckResultDTO;
import com.trace.agent.client.FarmingClient;
import com.trace.agent.client.dto.AgentResponse;
import com.trace.agent.service.AgentService;
import com.trace.common.domain.event.FarmingRecordEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * AI 智能体分析监听器：消费农事记录事件，校验异常并回写结果
 */
@Slf4j
@Component
public class AiAgentListener {

    @Resource
    private AgentService agentService;
    @Resource
    private FarmingClient farmingClient;
    @Resource
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "ai.agent.queue")
    public void onEvent(Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        try {
            FarmingRecordEvent event = (FarmingRecordEvent) rabbitTemplate.getMessageConverter().fromMessage(message);

            Map<String, Object> record = new HashMap<>();
            record.put("recordId", event.getRecordId());
            record.put("farmingType", event.getFarmingType());
            record.put("materialName", event.getMaterialName());
            record.put("amount", event.getAmount());
            record.put("unit", event.getUnit());
            record.put("workDate", event.getWorkDate());
            record.put("areaMu", event.getAreaMu());

            AgentResponse resp = agentService.validateFarming(List.of(record));
            boolean abnormal = resp.getContent() != null
                    && (resp.getContent().contains("异常") || resp.getContent().contains("abnormal")
                    || resp.getContent().contains("冲突"));

            CheckResultDTO dto = new CheckResultDTO();
            dto.setRecordId(event.getRecordId());
            dto.setStatus(abnormal ? "ABNORMAL" : "NORMAL");
            dto.setRemark(abnormal ? resp.getContent() : "校验通过");
            dto.setAiAdvice(resp.getContent());
            farmingClient.writeBackCheckResult(dto);

            channel.basicAck(tag, false);
            log.info("AI 校验完成 recordId={} status={}", event.getRecordId(), dto.getStatus());
        } catch (Exception e) {
            log.error("AI 校验消费失败: {}", e.getMessage(), e);
            channel.basicNack(tag, false, false);
        }
    }
}
