package com.trace.common.enums;

/**
 * 农事类型枚举：与 AI 智能体、碳排因子库共用一套编码
 */
public enum FarmingType {

    /** 施肥 */
    FERTILIZE("FERTILIZE", "施肥"),
    /** 灌溉 */
    IRRIGATE("IRRIGATE", "灌溉"),
    /** 农机作业 */
    MACHINE("MACHINE", "农机作业"),
    /** 农药喷洒 */
    PESTICIDE("PESTICIDE", "农药施用"),
    /** 农膜使用 */
    FILM("FILM", "农膜使用"),
    /** 收获 */
    HARVEST("HARVEST", "收获"),
    /** 包装 */
    PACKAGE("PACKAGE", "包装"),
    /** 运输 */
    TRANSPORT("TRANSPORT", "运输");

    private final String code;
    private final String desc;

    FarmingType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    public static FarmingType of(String code) {
        for (FarmingType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        throw new IllegalArgumentException("未知农事类型: " + code);
    }

    public static boolean contains(String code) {
        for (FarmingType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }
}
