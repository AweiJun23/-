package com.trace.common.constant;

/**
 * 消息队列常量定义
 */
public class MqConstants {

    /** 农事记录事件：fanout 广播交换机 */
    public static final String EXCHANGE_FARMING_EVENT = "farming.event.exchange";

    /** 碳足迹报告生成事件：fanout 广播交换机 */
    public static final String EXCHANGE_CARBON_REPORT = "carbon.report.exchange";

    /** 队列：碳足迹核算 */
    public static final String QUEUE_CARBON_CALC = "carbon.calc.queue";
    /** 队列：AI 智能体分析 */
    public static final String QUEUE_AI_AGENT = "ai.agent.queue";
    /** 队列：联盟链存证（农事记录） */
    public static final String QUEUE_CHAIN_NOTARY_FARMING = "chain.notary.farming.queue";
    /** 队列：联盟链存证（碳足迹报告） */
    public static final String QUEUE_CHAIN_NOTARY_CARBON = "chain.notary.carbon.queue";

    private MqConstants() {
    }
}
