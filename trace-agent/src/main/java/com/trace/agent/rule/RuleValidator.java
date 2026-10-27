package com.trace.agent.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * AI 输出规则引擎（二次校验）
 * <p>
 * 设计原则：数值类（碳排放量、补贴金额、产量）由微服务硬编码公式计算，
 * AI 只负责文本分析、建议生成、文档整理；本校验拦截 AI 可能"编造"的精确数值。
 */
@Component
public class RuleValidator {

    /** 匹配中文/阿拉伯精确数字（含小数、单位） */
    private static final Pattern NUMERIC = Pattern.compile(
            "[约]?\\d+(\\.\\d+)?\\s*(kgCO2e|吨|公斤|千克|元|万元|公斤/亩|kg/亩)");

    private static final int MAX_LEN = 2000;

    /**
     * 校验 AI 文本，返回违规说明列表（空列表表示通过）
     */
    public List<String> validate(String agent, String text) {
        List<String> violations = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            violations.add("AI 返回内容为空");
            return violations;
        }
        if (text.length() > MAX_LEN) {
            violations.add("AI 返回内容超过长度上限 " + MAX_LEN);
        }
        // 数值类智能体不得输出精确碳排放/补贴数值（由微服务计算）
        if ("carbon_advice".equals(agent) || "subsidy_report".equals(agent)
                || "farming_validate".equals(agent)) {
            if (NUMERIC.matcher(text).find()) {
                violations.add("AI 输出包含疑似精确数值，已按规则拦截（数值须由微服务计算）");
            }
        }
        return violations;
    }

    /** 是否通过校验 */
    public boolean pass(String agent, String text) {
        return validate(agent, text).isEmpty();
    }
}
