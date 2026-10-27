package com.trace.common.exception;

import com.trace.common.enums.ResultCode;
import com.trace.common.result.R;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public R<Void> handleBiz(BizException e, HttpServletRequest request) {
        log.warn("业务异常 uri={} msg={}", request.getRequestURI(), e.getMessage());
        return R.build(e.getCode(), e.getMessage(), null);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public R<Void> handleValid(Exception e, HttpServletRequest request) {
        String msg = ResultCode.PARAM_ERROR.getMsg();
        if (e instanceof MethodArgumentNotValidException) {
            msg = ((MethodArgumentNotValidException) e).getBindingResult()
                    .getFieldError() == null ? msg
                    : ((MethodArgumentNotValidException) e).getBindingResult().getFieldError().getDefaultMessage();
        }
        log.warn("参数校验失败 uri={} msg={}", request.getRequestURI(), msg);
        return R.build(ResultCode.PARAM_ERROR.getCode(), msg, null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public R<Void> handleIllegalArgument(IllegalArgumentException e) {
        return R.build(ResultCode.PARAM_ERROR.getCode(), e.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 uri={}", request.getRequestURI(), e);
        return R.fail(ResultCode.ERROR, "系统繁忙，请稍后重试");
    }
}
