package com.overpass.ServiceBridge.exception;


import com.overpass.ServiceBridge.utils.ResultCodeEnum;

/**
 * 自定义：业务异常
 */
public class BusinessException extends RuntimeException {

    private Integer state;
    private Integer code;
    private String message;
    public BusinessException(String msg) {
        super(msg);
        this.state = ResultCodeEnum.ERROR.getCode();
        this.code = ResultCodeEnum.ERROR.getCode();
        this.message = msg;
    }

    public BusinessException(ResultCodeEnum codeEnum) {
        this.state = codeEnum.getCode();
        this.code = codeEnum.getCode();
        this.message = codeEnum.getMessage();
    }

    public BusinessException(Integer state, String message) {
        this.state = state;
        this.code = state;
        this.message = message;
    }

    public BusinessException(Integer state, Integer code, String message) {
        this.state = state;
        this.code = code;
        this.message = message;
    }

    public Integer getState() {
        return this.state;
    }

    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
