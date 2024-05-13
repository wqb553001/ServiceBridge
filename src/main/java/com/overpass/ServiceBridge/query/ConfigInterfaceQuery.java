package com.overpass.ServiceBridge.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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
    @Schema(description = "服务名称")
    private String name;

    @Schema(description = "服务地址")
    private String httpUrlStr;

    @Schema(description = "请求方式", defaultValue = "POST", allowableValues = {"GET", "POST"})
    private String method;

    @Schema(description = "header", example = "token=abcdef;Content-Type=application/json")
    private String headers;

    @Schema(description = "请求入参:固定参数", example = "orderStatus = 1;flow = 1")
    private String requestParams;

    @Schema(description = "结果集字段", example = "list")
    private String dataContentField;

    @Schema(description = "成功状态值，字段取值判断条件", example = "status = 0")
    private String isSuccessfulCondition;

    /** 分页 **/
    @Schema(description = "分页：0-分页全取；1-取1条；2-取指定数量")
    private int limit;
    @Schema(description = "指定条数")
    private int limitNum;
    @Schema(description = "下一页策略：1-递增页号;2-指针顺取", example = "1",allowableValues = {"1", "2"} )
    private int nextPageStrategy;
    @Schema(description = "下一页", example = "page = page + 1")
    private String nextPageField;
    @Schema(description = "每页取数", example = "pageNum = 200")
    private String pageNumField;
    @Schema(description = "末页判断：(1)无需判断*;(2)直接读取isLastPage:isLastPage;(3)表达式计算")
    private String isLastPageCondition; // = "isLastPage:isLastPage"


    /** 分组聚合 **/
    @NoArgsConstructor
    @Data
    public class GroupMerge{
        private int opt;
        private int time;
        private String groupField;
        private String mergeField;
    }

    private String[] groupFields; // 未了方便写主干代码，先拿出来，后续再放回

    private GroupMerge groupMerge = new GroupMerge();
    private List<GroupMerge> groupMerges = new ArrayList<>();

    private InterfaceQuery interfaceQuery;







    @Schema(description = "返回内容字段集", example = "1s: status,code,msg;\n" +
            "1e: content;\n" +
            "2a-content: list;\n" +
            "2s-content: total;\n" +
            "2e-content: page;")
    private String respDataFields;

    @Schema(description = "数据集字段")
    private String list;

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
}

