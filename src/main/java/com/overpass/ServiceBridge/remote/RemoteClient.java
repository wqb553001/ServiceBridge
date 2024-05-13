package com.overpass.ServiceBridge.remote;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.overpass.ServiceBridge.constans.PublicConstants;
import com.overpass.ServiceBridge.query.ConfigInterfaceQuery;
import com.overpass.ServiceBridge.utils.DataUtil;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Log4j2
public class RemoteClient {

    @Autowired
    private RestTemplate restTemplate;


    //    @NacosValue(value = "${nexneo-behave.recommend.recd.num:10}")      // 后续，需要支持，不同商家，不同配置，放入 redis
    private int recdNum;


    @Data
    private class RequestCondition{
        private boolean continuePage = false;
        private HttpHeaders requestHeaders = new HttpHeaders();
        private Map<String, Object> requestBody;
        private HttpEntity<Map<String, Object>> requestEntity;
        private String httpUrlStr;
        private String method;

        RequestCondition(){
            this.requestEntity = new HttpEntity<>(requestBody, requestHeaders);
        }
        RequestCondition(Map<String, Object> requestBody){
            this.requestBody = requestBody;
            this.requestEntity = new HttpEntity<>(requestBody, requestHeaders);
        }
        RequestCondition(Map<String, Object> requestBody, HttpHeaders requestHeaders){
            this.requestBody = requestBody;
            this.requestEntity = new HttpEntity<>(requestBody, requestHeaders);
        }
    }

    private RequestCondition buildNewRequestCondition(ConfigInterfaceQuery configInterfaceQuery){
        HttpHeaders requestHeaders = new HttpHeaders();
        Map<String, Object> requestBody = new HashMap<>();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        // 固定参数
        if(StringUtils.isNotBlank(configInterfaceQuery.getRequestParams())) requestBody.putAll(DataUtil.strToMap(configInterfaceQuery.getRequestParams()));

        // header
        if(StringUtils.isNotBlank(configInterfaceQuery.getHeaders())) {
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.setAll(DataUtil.strToMap(configInterfaceQuery.getHeaders()));
            requestHeaders.putAll(body);
        }
        RequestCondition requestCondition = new RequestCondition(requestBody, requestHeaders);

        requestCondition.setHttpUrlStr(configInterfaceQuery.getHttpUrlStr());
        requestCondition.setMethod(configInterfaceQuery.getMethod());

        return requestCondition;
    }

    // 需要分页 请求
    public void pageQuery(ConfigInterfaceQuery configInterfaceQuery, String configInfo){
        RequestCondition requestCondition = this.buildNewRequestCondition(configInterfaceQuery);
        Map<String, Object> requestBody = requestCondition.getRequestBody();
        // 每页显示数
        if(StringUtils.isNotBlank(configInterfaceQuery.getPageNumField()))  requestBody.putAll(DataUtil.strToMap(configInterfaceQuery.getPageNumField(), "="));
        boolean flag = this.onceQuery(requestCondition, configInterfaceQuery, configInfo);
        while (requestCondition.continuePage && flag){
            flag = this.onceQuery(requestCondition, configInterfaceQuery, configInfo);
        }

    }

    /****
     * 单次 请求
     *
     * 接口请求，需要考虑的问题：
     * 1.请求地址（URL） 及 请求方式（POST/GET）。
     * 2.返回值，是一个 Entity 对象 还是 数组[]。
     * 3.Entity对象属性是否存在 多层嵌套取值。
     * 4.数组[] 多数情况存在分页，分页方式 及 最后一页 的判断。
     * 5.
     * 6.
     *
     * @param requestCondition
     * @param configInterfaceQuery
     * @param configInfo
     */
    public boolean onceQuery(RequestCondition requestCondition, ConfigInterfaceQuery configInterfaceQuery, String configInfo){
        log.info("--<<--query()------开始-----begin---");
        long startTime = System.currentTimeMillis();
        JSONArray totalListJSON = new JSONArray();
        if(Objects.isNull(requestCondition))  requestCondition = this.buildNewRequestCondition(configInterfaceQuery);
        //执行 POST 请求
        ResponseEntity responseEntity = null;
        try {
//            switch (PublicConstants.Method.getEnumByName(method)){
//                case POST:
//                    log.info("接口URL：{}，参数：{}", httpUrlStr, JSON.toJSONString(requestEntity));
//                    entries = restTemplate.postForObject(httpUrlStr, requestEntity, JSONObject.class);
//                    break;
//                case GET:
//                    entries = restTemplate.getForObject(URI.create(handleGetMethodParam(httpUrlStr, requestBody)), JSONObject.class);
//                    break;
//            }
            // 初次猜测结果集为 JSONObject 类型
            responseEntity = this.retryRestTemplateExchange(requestCondition, JSONObject.class);
        }catch (RestClientException e) { // e.getCause().getCause() instanceof MismatchedInputException
            Object ex = e.getCause();
            while (!(Objects.isNull(ex) || (ex instanceof MismatchedInputException))){ // MismatchedInputException extends JsonMappingException
                ex = ((Exception)ex).getCause();
            }
            if(Objects.nonNull(ex) && ex instanceof MismatchedInputException){
                // 再次以 JSONArray 类型 尝试识别结果集
                responseEntity = this.retryRestTemplateExchange(requestCondition, JSONArray.class);
            }
        }

        if(Objects.isNull(responseEntity) || !responseEntity.getStatusCode().is2xxSuccessful()){
            // fail  TODO:记录异常
            DataUtil.recordLogAndThrow(configInfo+"执行请求，异常，请核查！");
            return false;
        }

        // success
        Integer resultSize = this.explainResultData(totalListJSON, requestCondition, responseEntity, configInterfaceQuery, configInfo);

        log.info("-->>--onceQuery()------完成------end----记录数：{}，总用时：{} 毫秒", resultSize, System.currentTimeMillis() - startTime);
        return true;
    }

    // 异常重试
    @Retryable(retryFor = {Exception.class},backoff = @Backoff(delay = 2000L,multiplier = 1),maxAttempts = 3)
    public ResponseEntity retryRestTemplateExchange(RequestCondition requestCondition, Class clazz){
        long startTime = System.currentTimeMillis();
        HttpMethod method = HttpMethod.valueOf(requestCondition.getMethod().toUpperCase());
        String httpUrlStr = requestCondition.getHttpUrlStr();
        if(method == HttpMethod.GET) httpUrlStr = DataUtil.handleGetMethodUrlSuffix(requestCondition.getHttpUrlStr(), requestCondition.getRequestEntity().getBody());
        log.info("--<<--retryRestTemplateExchange()------开始-begin---请求参数：{}；返回值类型：{}；", JSONObject.toJSONString(requestCondition), clazz.getTypeName());
        ResponseEntity responseEntity = restTemplate.exchange(httpUrlStr, method, requestCondition.getRequestEntity(), clazz, new HashMap<>());
        log.info("-->>--retryRestTemplateExchange()------完成--end---总用时：{} 毫秒；返回类型：{} | 结果集：{}", System.currentTimeMillis() - startTime, clazz.getTypeName(), JSONObject.toJSONString(responseEntity));
        return responseEntity;
    }

    /***
     * 解析返回值
     *
     *  1.判断结果是否有效
     *  2.收集读取数据集
     *  3.判断是否需要分页
     *      3.1 需要分页，则设置下一页请求参数
     */
    private Integer explainResultData(JSONArray totalListJSON, RequestCondition requestCondition, ResponseEntity responseEntity, ConfigInterfaceQuery configInterfaceQuery, String configInfo){
        /** 默认初始化刷新：停止分页 **/
        requestCondition.setContinuePage(false);
        Object responseBodyObj = responseEntity.getBody();
        Map<String, Object> requestBody = requestCondition.getRequestBody();
        JSONObject responseBodyJSONObject;
        JSONArray responseBodyJSONArray;
        if(responseBodyObj instanceof JSONArray){
            JSONArray currentDataJSONArray = new JSONArray();
            responseBodyJSONArray = (JSONArray)responseBodyObj;
            JSONObject outputJSONObject = new JSONObject();
            boolean flag = false;
            for (int i = 0; i < responseBodyJSONArray.size(); i++) {
                if(Objects.isNull(responseBodyJSONArray.get(i))) continue;
                flag = true;
                DataUtil.explainData(currentDataJSONArray, outputJSONObject, (JSONObject)responseBodyJSONArray.get(i), configInterfaceQuery.getRespDataFields(), configInterfaceQuery.getDataContentField());
                // 结果异常，抛异常
                if(!DataUtil.judge(outputJSONObject, configInterfaceQuery.getIsSuccessfulCondition(), configInfo)) DataUtil.recordLogAndThrow(configInfo+"结果状态判断为：不可用。");
            }
            if(!flag){// 没有任何数据
                DataUtil.recordLogAndThrow(configInfo+"没有任何数据");
            }
            totalListJSON.addAll(currentDataJSONArray);
            return currentDataJSONArray.size();
        }

        if(responseBodyObj instanceof JSONObject) {
            JSONArray currentDataJSONArray = new JSONArray();
            responseBodyJSONObject = (JSONObject) responseBodyObj;
            JSONObject outputJSONObject = new JSONObject();
            DataUtil.explainData(currentDataJSONArray, outputJSONObject, responseBodyJSONObject, configInterfaceQuery.getRespDataFields(), configInterfaceQuery.getDataContentField());
            if (!DataUtil.judge(outputJSONObject, configInterfaceQuery.getIsSuccessfulCondition(), configInfo))
                DataUtil.recordLogAndThrow(configInfo + "结果状态判断为：不可用。");
            // 分页：0-分页全取；1-取1条；2-取指定数量
            if (configInterfaceQuery.getLimit() == 1) {
                totalListJSON.add(currentDataJSONArray.get(0));
                return 1;
            }
            // 2-取指定数量
            if (configInterfaceQuery.getLimit() == 2)
                return DataUtil.jsonArrayLimit(totalListJSON, currentDataJSONArray, configInterfaceQuery.getLimitNum());
            // 0-分页全取
            totalListJSON.addAll(currentDataJSONArray);
            int currentSize = currentDataJSONArray.size();
            // 末页判断：是末页，结束分页
            if (DataUtil.judge(outputJSONObject, configInterfaceQuery.getIsLastPageCondition(), configInfo))
                return currentSize;

            /** 非末页，分页继续 ：刷新继续分页标识 **/
            requestCondition.setContinuePage(true);
            // 下一页策略：1-递增页号;2-指针顺取
            int nextPageStrategy = configInterfaceQuery.getNextPageStrategy();
            String nextPageField = configInterfaceQuery.getNextPageField();
            if(StringUtils.isBlank(nextPageField)) DataUtil.recordLogAndThrow(configInfo+"分页取类型下，下一页字段配置不能为空！");
            if (nextPageStrategy == 1) { // 1-递增页号;
                // 将下一页的页号，放入 requestBody 中
                DataUtil.explainFieldToMap(requestBody, outputJSONObject, nextPageField, true, "", configInfo);
                return currentSize;
            }
            // 2-指针顺取
            DataUtil.explainJsonToMap(requestBody, currentDataJSONArray.getJSONObject(currentDataJSONArray.size() - 1), nextPageField);
            return currentSize;
        }
        return 0;
    }

    /**
     * 分组聚合
     *
     * @param groupMergeList        分组聚合后，结果集
     * @param currentDataJSONArray  分组聚合前，数据集
     * @param configInterfaceQuery
     * @param configInfo
     */
    private void groupMerge(List<JSONObject> groupMergeList, JSONArray currentDataJSONArray, ConfigInterfaceQuery configInterfaceQuery, String configInfo){
        String[] groupFields = configInterfaceQuery.getGroupFields();
        if(Arrays.isNullOrEmpty(groupFields)) {
            log.debug(configInfo + "未配置分组字段，不必进行【后置分组】操作。");
            return;
        }
        /** 分组 **/
        Map<String, List<Object>> collect = currentDataJSONArray.stream().collect(Collectors.groupingBy(json -> {
            JSONObject inputJSON = (JSONObject)json;
            String groupKey = "groupKey";
            for(int i=0;i<groupFields.length-1;i++){
                groupKey += ("-" + inputJSON.get(groupFields[i]));
            }
            return groupKey;
        }));

        // 数据类型校验：取一条，进行验证
        if(!DataUtil.isNumberObject((JSONObject)currentDataJSONArray.get(0), groupFields)) DataUtil.recordLogAndThrow(configInfo + "存在不是数字的字段，无法进行聚合操作，请核查！");
//        List<JSONObject> groupMergeList = new ArrayList<>();

        /** 聚合 **/
        ConfigInterfaceQuery.GroupMerge groupMerge = configInterfaceQuery.getGroupMerges().get(0);
        collect.values().forEach(jsonObjects ->{
            List<JSONObject> jsonList = jsonObjects.stream()
                    .map(obj -> (JSONObject) obj) // 强制类型转换
                    .collect(Collectors.toList());
            JSONObject oneJson = jsonList.get(0);
            PublicConstants.MergeStyleEnum mergeStyleEnum = PublicConstants.MergeStyleEnum.getEnumByCode(groupMerge.getOpt());
            DataUtil.mergeByStyle(oneJson, jsonList, groupFields, mergeStyleEnum, configInfo);
            groupMergeList.add(oneJson);
        });
    }



    public static void main(String[] args) {
        boolean isLastPage = false;
        JSONObject arapSaveVOJson = new JSONObject();
//        arapSaveVOJson.put("feeAmount", 2);
//        arapSaveVOJson.put("feeAmount", Double.valueOf(2));
//        arapSaveVOJson.put("feeAmount", BigDecimal.valueOf(2));
//        arapSaveVOJson.put("feeAmount", Integer.valueOf(2));
        arapSaveVOJson.put("feeAmount", String.valueOf(2));
        arapSaveVOJson.put("isLastPage", true);
        Object feeAmount = arapSaveVOJson.get("feeAmount");
        if(feeAmount instanceof Number){
            System.out.println("执行结果为 true");
        }else{
            System.out.println("执行结果为 false");
        }


        String endFields = "#feeAmount==0|feeAmount";
        endFields = "#isLastPage==true|isLastPage";
//        endFields = "#feeAmount!=feeAmount1|feeAmount&feeAmount1";
//        Object operation = DataUtil.operation(arapSaveVOJson, endFields, true, false, null);
        Object operation = DataUtil.operation(arapSaveVOJson, endFields, true, false, null, "configInfo");

        System.out.println(operation);
        String eKeys = "a=b";
        String substringA = eKeys.substring(0, eKeys.indexOf("="));
        String substringB = eKeys.substring(eKeys.indexOf("=")+1);
        System.out.println(substringA);
        System.out.println(substringB);
        System.out.println("==========================");
        String inputStr = "{\n" +
                "    \"status\": 0,\n" +
                "    \"code\": 0,\n" +
                "    \"msg\": \"ok\",\n" +
                "    \"content\": {\n" +
                "        \"total\": 3,\n" +
                "        \"list\": [\n" +
                "            {\n" +
                "                \"id\": 70583,\n" +
                "                \"branchId\": 97,\n" +
                "                \"ownerId\": 10010038,\n" +
                "                \"orderNo\": \"PC144533676116308736\",\n" +
                "                \"outStoreId\": 673,\n" +
                "                \"finishTime\": \"2021-11-16 17:22:28\",\n" +
                "                \"createTime\": \"2021-10-20 14:47:00\",\n" +
                "                \"payPriceTotal\": 3,\n" +
                "\t\t\t\t\"order\": {\n" +
                "\t\t\t\t\t\"orderNo\": \"PC144533676116308736\",\n" +
                "\t\t\t\t\t\"orderState\": 41,\n" +
                "\t\t\"array\":[1,2,3,4],\n" +
                "\t\t\t\t\t\"memberId\": 684894\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t\"wms\": {\n" +
                "\t\t\t\t\t\"wmsId\": 1001,\n" +
                "\t\t\t\t\t\"wmsName\": \"河北仓\"\n" +
                "\t\t\t\t}\n" +
                "            },\n" +
                "            {\n" +
                "                \"id\": 70584,\n" +
                "                \"branchId\": 97,\n" +
                "                \"ownerId\": 10010048,\n" +
                "                \"orderNo\": \"PC144533676116308746\",\n" +
                "                \"outStoreId\": 674,\n" +
                "                \"finishTime\": \"2021-11-16 17:22:28\",\n" +
                "                \"createTime\": \"2021-10-20 14:47:00\",\n" +
                "                \"payPriceTotal\": 4,\n" +
                "\t\t\t\t\"order\": {\n" +
                "\t\t\t\t\t\"orderNo\": \"PC144533676116308746\",\n" +
                "\t\t\t\t\t\"orderState\": 44,\n" +
                "\t\t\"array\":[1,2,3,4],\n" +
                "\t\t\t\t\t\"memberId\": 684894\n" +
                "\t\t\t\t\t\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\t\"wms\": {\n" +
                "\t\t\t\t\t\"wmsId\": 1004,\n" +
                "\t\t\t\t\t\"wmsName\": \"河北仓\"\n" +
                "\t\t\t\t}\n" +
                "            }\n" +
                "        ],\n" +
                "\t\t\"array\":[1,2,3,4],\n" +
                "\t\t\"emptyArray\":[],\n" +
                "\t\t\"page\": {\n" +
                "\t\t\t\"pageNum\": 1,\n" +
                "\t\t\t\"pageSize\": 100,\n" +
                "\t\t\t\"size\": 3,\n" +
                "\t\t\t\"pages\": 1,\n" +
                "\t\t\t\"prePage\": 0,\n" +
                "\t\t\t\"nextPage\": 2,\n" +
                "\t\t\t\"isFirstPage\": true,\n" +
                "\t\t\t\"isLastPage\": true\n" +
                "\t\t}\n" +
                "    }\n" +
                "}";

        inputStr = "\n" +
                "        {\n" +
                "            \"status\": 0,\n" +
                "                \"code\": 0,\n" +
                "                \"msg\": \"ok\",\n" +
                "                \"content\": {\n" +
                "            \"total\": 3,\n" +
                "                    \"list\": [\n" +
                "            {\n" +
                "                \"id\": 70583,\n" +
                "                    \"branchId\": 97,\n" +
                "                    \"ownerId\": 10010038,\n" +
                "                    \"orderNo\": \"PC144533676116308736\",\n" +
                "                    \"outStoreId\": 673,\n" +
                "                    \"finishTime\": \"2021-11-16 17:22:28\",\n" +
                "                    \"createTime\": \"2021-10-20 14:47:00\",\n" +
                "                    \"payPriceTotal\": 3,\n" +
                "                    \"order\": {\n" +
                "                \"orderNo\": \"PC144533676116308736\",\n" +
                "                        \"orderState\": 41,\n" +
                "                        \"memberId\": 684894,\n" +
                "                        \"goods\": [\n" +
                "                {\n" +
                "                    \"id\": 1001,\n" +
                "                    \"gNo\": \"G1001\",\n" +
                "                    \"gName\": \"冬装1001\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"id\": 1002,\n" +
                "                    \"gNo\": \"G1002\",\n" +
                "                    \"gName\": \"冬装1002\"\n" +
                "                }\n" +
                "\t\t\t\t\t]\n" +
                "            },\n" +
                "                \"wms\": {\n" +
                "                \"wmsId\": 1001,\n" +
                "                        \"wmsName\": \"河北仓\"\n" +
                "            }\n" +
                "            },\n" +
                "            {\n" +
                "                \"id\": 70584,\n" +
                "                    \"branchId\": 97,\n" +
                "                    \"ownerId\": 10010048,\n" +
                "                    \"orderNo\": \"PC144533676116308746\",\n" +
                "                    \"outStoreId\": 674,\n" +
                "                    \"finishTime\": \"2021-11-16 17:22:28\",\n" +
                "                    \"createTime\": \"2021-10-20 14:47:00\",\n" +
                "                    \"payPriceTotal\": 4,\n" +
                "                    \"order\": {\n" +
                "                \"orderNo\": \"PC144533676116308746\",\n" +
                "                        \"orderState\": 44,\n" +
                "                        \"memberId\": 684894,\n" +
                "                        \"goods\": [\n" +
                "                {\n" +
                "                    \"id\": 2001,\n" +
                "                    \"gNo\": \"G2001\",\n" +
                "                    \"gName\": \"冬装2001\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"id\": 2002,\n" +
                "                     \"gNo\": \"G2002\",\n" +
                "                     \"gName\": \"冬装1002\"\n" +
                "                }\n" +
                "\t\t\t\t\t]\n" +
                "\n" +
                "            },\n" +
                "                \"wms\": {\n" +
                "                \"wmsId\": 1004,\n" +
                "                        \"wmsName\": \"河北仓\"\n" +
                "            }\n" +
                "            }\n" +
                "        ],\n" +
                "            \"array\":[1,2,3,4],\n" +
                "            \"emptyArray\":[],\n" +
                "            \"page\": {\n" +
                "                \"pageNum\": 1,\n" +
                "                        \"pageSize\": 100,\n" +
                "                        \"size\": 3,\n" +
                "                        \"pages\": 1,\n" +
                "                        \"prePage\": 0,\n" +
                "                        \"nextPage\": 2,\n" +
                "                        \"isFirstPage\": true,\n" +
                "                        \"isLastPage\": true\n" +
                "            }\n" +
                "        }\n" +
                "        }";
        String fields = "1e: content;\n" +
                "1s: status,code,msg;\n" +
                "2a-content: list;\n" +
                "2e-content: page;\n" +
                "2s-content: total,array,emptyArray;\n" +
                "3s-page: pageNum,pageSize,size,pages,prePage,nextPage,isFirstPage,isLastPage;\n" +
                "3e-list: order,wms;\n" +
                "3s-list: id,branchId,ownerId,orderNo;\n" +
                "4s-wms: wmsId,wmsName;\n" +
                "4s-order: orderNo,orderState,memberId;";
//        fields = "1e: content;\n" +
//                "2a-content: list;\n" +
//                "3s-list: id,branchId,ownerId,orderNo;\n";
//        fields = "1e: content;\n" +
//                "2a-content: list;\n" +
//                "3e-list: order,wms;\n" +
//                "3s-list: branchId,ownerId,orderNo;\n" +
//                "4s-wms: wmsId,wmsName;\n" +
//                "4s-order: orderNo,orderState,memberId;";
//        fields = "1e: content;\n" +
//                "2a-content: list;\n" +
//                "2s-content: total,array,emptyArray;\n" +
//                "3e-list: order,wms;\n" +
//                "4s-wms: wmsId,wmsName;\n" +
//                "4s-order: orderNo,orderState,memberId,array;";
//        fields = "1e: content;\n" +
//                "2e-content: page;\n" +
//                "2s-content: total,array,emptyArray;\n" +
//                "3s-page: pageNum,pageSize,size,pages,prePage,nextPage,isFirstPage,isLastPage;\n" +
//                "3e-list: order,wms;\n" +
//                "3s-list: id,branchId,ownerId,orderNo;\n" +
//                "4s-wms: wmsId,wmsName;\n" +
//                "4s-order: orderNo,orderState,memberId;";
//        fields = "1e: content;\n" +
//                "2e-content: page;\n" +
//                "3s-page: pageNum,pageSize,size,pages,prePage,nextPage,isFirstPage,isLastPage;\n";
        fields = "1e: content;\n" +
                "1s: status,code,msg;\n" +
                "2a-content: list;\n" +
                "2e-content: page;\n" +
                "2s-content: total,array,emptyArray;\n" +
                "3s-page: pageNum,pageSize,size,pages,prePage,nextPage,isFirstPage,isLastPage;\n" +
                "3e-list: order,wms;\n" +
                "3s-list: id,branchId,ownerId,orderNo,outStoreId,finishTime,createTime,payPriceTotal;\n" +
                "4s-wms: wmsId,wmsName;\n" +
                "4a-order: goods;\n" +
                "4s-order: orderNo,orderState,memberId;\n" +
                "5s-goods: id,gNo,gName;";
        JSONObject outputJSONObject = new JSONObject();
        JSONArray totalListJSON = new JSONArray();
        JSONObject inputJSONObject = (JSONObject)JSONObject.parse(inputStr);
        Map<String, String> fieldsMap = DataUtil.strToMap(fields, ":");
//        DataUtil.explainKeys(totalListJSON, outputJSONObject, inputJSONObject, fieldsMap);
        DataUtil.explainKeys(totalListJSON, outputJSONObject, inputJSONObject, fieldsMap, "");
        System.out.println("执行结果：\n" + JSON.toJSONString(outputJSONObject));
        System.out.println("执行结果集：\n" + JSON.toJSONString(totalListJSON));
    }

}
