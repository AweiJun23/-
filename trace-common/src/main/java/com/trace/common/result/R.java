package com.trace.common.result;

import com.trace.common.enums.ResultCode;
import java.io.Serializable;

/**
 * 统一响应体
 */
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private String msg;
    private T data;
    private long timestamp = System.currentTimeMillis();

    public R() {
    }

    public R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(ResultCode.ERROR.getCode(), msg, null);
    }

    public static <T> R<T> fail(ResultCode code) {
        return new R<>(code.getCode(), code.getMsg(), null);
    }

    public static <T> R<T> fail(ResultCode code, String msg) {
        return new R<>(code.getCode(), msg, null);
    }

    public static <T> R<T> build(int code, String msg, T data) {
        return new R<>(code, msg, data);
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public long getTimestamp() { return timestamp; }

    /** 响应是否成功 */
    public boolean isSuccess() { return code == ResultCode.SUCCESS.getCode(); }
    /** 响应是否失败 */
    public boolean isFail() { return code != ResultCode.SUCCESS.getCode(); }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
