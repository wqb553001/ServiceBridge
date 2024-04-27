package com.overpass.ServiceBridge.utils;

public enum ResultCodeEnum {
    OK(200, "OK")
    ,ERROR(1, "ERROR")
    ,NO_DATA(2, "无匹配数据")
    ,PARAMS_NOT_VALID_ERROR(400, "参数校验未通过")
    ,UNAUTHORIZED(401, "未认证")
    ,FORBIDDEN(403, "无权限")
    ,REGISTRATION(406, "访问服务地址，未登记")
    ,NO_REGISTER(405, "token，未录入，拒绝访问")
    ,BUSINESS_ERROR(566, "业务异常")
    ,REC_HOME_ERROR(577, "推荐算法接口异常")
    ,SYSTEM_ERROR(500, "系统异常")

    ;

    private int code;
    private String message;

    private ResultCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}