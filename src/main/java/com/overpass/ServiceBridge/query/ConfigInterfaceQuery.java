package com.overpass.ServiceBridge.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * @author wqb
 * @title: InterfaceQuery
 * @description: 来源配置
 * @date 2021-11-5 17:51:12
 */
@Builder
@Data
public class ConfigInterfaceQuery {

    /** 目标数据 **/
    @Schema(description = "服务地址")
    private String httpUrlStr;

    @Schema(description = "请求参数")
    private Map<String, Object> requestBody;

    @Schema(description = "请求方式", defaultValue = "POST", allowableValues = {"GET", "POST"})
    private String method;

    @Schema(description = "返回内容字段")
    private String respDataField = "content";

    @Schema(description = "状态值字段")
    private String status = "status";

    @Schema(description = "数据集字段")
    private String list = "list";

    @Schema(description = "是否通过接口分页(0否 1是)")
    private Integer needPage = 1;
    @Schema(description = "分页大小")
    private Integer pageSize = 100;

    @Schema(description = "末页判断：(1)无需判断*;(2)直接读取isLastPage:isLastPage;(3)表达式计算")
    private String isLastPageFields; // = "isLastPage:isLastPage"


    /** 源数据 **/
    @Schema(description = "请求入参")
    private String requestParams;

    private InterfaceQuery interfaceQuery;







    @Schema(description = "业务线")
    private Integer businessType;

    @Schema(description = "服务名")
    private String serviceName = "tc-ocs-v1";

    @Schema(description = "接口路径")
    private String interfacePath = "/report/reportOrderList";

    @Schema(description = "固定条件传参")
    private String conditionParams;

    @Schema(description = "业务开始时间字段")
    private String bussStartTimeField = "finishStartTime";

    @Schema(description = "业务截止时间字段")
    private String bussEndTimeField = "finishEndTime";

    @Schema(description = "业务开始时间")
    private String bussStartTime;

    @Schema(description = "业务截止时间")
    private String bussEndTime;

    @Schema(description = "货主字段")
    private String shopIdField;
}

