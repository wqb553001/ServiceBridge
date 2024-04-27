package com.overpass.ServiceBridge.utils;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import static com.overpass.ServiceBridge.utils.ResultCodeEnum.*;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "返回信息 BO", name = "返回值 BO")
public class Result<T> implements Serializable {
    private int code;
    private String message;
    private T data;

    public Result(ResultCodeEnum codeEnum, T data) {
        this.code = codeEnum.getCode();
        this.message = codeEnum.getMessage();
        this.data = data;
    }

    public Result(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result(T data) {
        this.code = OK.getCode();
        this.message = OK.getMessage();
        this.data = data;
    }

    public static Result OK(){
        return new Result(OK, null);
    }

    public static Result ERROR(){
        return new Result(ERROR, null);
    }

    public static Result ERROR(String message){
        return new Result(ERROR.getCode(), message);
    }

    public static Result ERROR(ResultCodeEnum codeEnum){
        return new Result(codeEnum, null);
    }

    public static <T> Result<T> SUCCESS(T data){
        return new Result<>(data);
    }

    public static Result ERROR(int code, String message){
        return new Result(code, message);
    }

}
