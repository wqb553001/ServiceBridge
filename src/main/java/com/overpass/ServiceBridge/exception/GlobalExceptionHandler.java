package com.overpass.ServiceBridge.exception;

import com.overpass.ServiceBridge.utils.Result;
import com.overpass.ServiceBridge.utils.ResultCodeEnum;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;


@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Order(Ordered.HIGHEST_PRECEDENCE) // 优先级最高
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result methodArgsNotValidExceptionHandler(MethodArgumentNotValidException ex) {
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        StringBuilder errorMsg = new StringBuilder();
        errors.forEach(error -> errorMsg.append(error.getDefaultMessage()).append("; "));
        log.warn("参数异常，msg ->", ex);
        return Result.ERROR(ResultCodeEnum.PARAMS_NOT_VALID_ERROR.getCode(), errorMsg.toString());
    }

    @Order(Ordered.HIGHEST_PRECEDENCE + 2) // 优先级最高-2
    @ExceptionHandler(value = BusinessException.class)
    public Result businessExceptionHandler(BusinessException ex) {
        log.warn("参数异常，msg ->", ex);
        return Result.ERROR(ResultCodeEnum.BUSINESS_ERROR.getCode(), ResultCodeEnum.BUSINESS_ERROR.getMessage());
    }

    @Order(Ordered.LOWEST_PRECEDENCE - 20) // 优先级最低 + 20
    @ExceptionHandler(value = NoResourceFoundException.class)
    public Result<Object> handleNoResourceFoundException(Exception ex){
        log.info("【WARN】系统抛 NoResourceFoundException 异常：{}", ex.getMessage());
        return Result.ERROR(ResultCodeEnum.ERROR.getMessage());
    }

    @Order(Ordered.LOWEST_PRECEDENCE - 10) // 优先级最低 + 10
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public Result<Object> handleHttpRequestMethodNotSupportedException(Exception ex){
        log.warn("【ERROR】系统抛 HttpRequestMethodNotSupportedException 异常：{}", ex.getMessage());
        return Result.ERROR(ResultCodeEnum.ERROR.getMessage());
    }

    // Order 不写，默认优先级最低
    @ExceptionHandler(value = Exception.class)
    public Result<Object> handleException(Exception ex){
        log.error("系统发生异常：", ex);
        return Result.ERROR(ResultCodeEnum.ERROR.getMessage());
    }

}