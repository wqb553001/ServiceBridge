package com.overpass.ServiceBridge.remote;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.overpass.ServiceBridge.ho.ConfigHO;
import com.overpass.ServiceBridge.query.ConfigInterfaceQuery;
import com.overpass.ServiceBridge.utils.DataUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;

@Component
@Log4j2
public class RemoteClient {

    @Autowired
    private RestTemplate restTemplate;


    //    @NacosValue(value = "${nexneo-behave.recommend.recd.num:10}")      // 后续，需要支持，不同商家，不同配置，放入 redis
    private int recdNum;

    /****
     * 接口请求，需要考虑的问题：
     * 1.请求地址（URL） 及 请求方式（POST/GET）。
     * 2.返回值，是一个 Entity 对象 还是 数组[]。
     * 3.Entity对象属性是否存在 多层嵌套取值。
     * 4.数组[] 多数情况存在分页，分页方式 及 最后一页 的判断。
     * 5.
     * 6.
     *
     * @param configHO
     * @param httpUrlStr
     * @param requestBody
     * @param method
     * @param configInterfaceQuery
     * @param configInfo
     */
    public void query(ConfigHO configHO, String httpUrlStr, Map<String, Object> requestBody, String method, ConfigInterfaceQuery configInterfaceQuery, String configInfo){
        log.info("--<<--query()------开始-----begin---");
        long startTime = System.currentTimeMillis();
        JSONArray totalListJSON = new JSONArray();
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, requestHeaders);
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
            responseEntity = this.retryRestTemplateExchange(httpUrlStr, HttpMethod.valueOf(method.toUpperCase()), requestEntity, JSONObject.class, requestBody);
        }catch (RestClientException e) { // e.getCause().getCause() instanceof MismatchedInputException
            Object ex = e.getCause();
            while (!(Objects.isNull(ex) || (ex instanceof MismatchedInputException))){ // MismatchedInputException extends JsonMappingException
                ex = ((Exception)ex).getCause();
            }
            if(Objects.nonNull(ex) && ex instanceof MismatchedInputException){
                // 再次以 JSONArray 类型 尝试识别结果集
                responseEntity = this.retryRestTemplateExchange(httpUrlStr, HttpMethod.valueOf(method.toUpperCase()), requestEntity, JSONArray.class, requestBody);
            }
        }

        if(Objects.isNull(responseEntity) || !responseEntity.getStatusCode().is2xxSuccessful()){
            // fail  TODO:记录异常
            return;
        }

        // success
        JSONObject midResp = new JSONObject();
        Integer resultDataSize = this.explainResultData(totalListJSON, midResp, responseEntity, requestBody, configInterfaceQuery, configInfo);

        log.info("-->>--request()------完成------end----总用时：{} 毫秒", System.currentTimeMillis() - startTime);

    }

    // 异常重试
    @Retryable(retryFor = {Exception.class},backoff = @Backoff(delay = 2000L,multiplier = 1),maxAttempts = 3)
    public ResponseEntity retryRestTemplateExchange(String httpUrlStr, HttpMethod method, HttpEntity<Map<String, Object>> requestEntity, Class clazz, Map<String, Object> requestBody){
        long startTime = System.currentTimeMillis();
        log.info("--<<--restTemplateExchange()------开始-begin---请求地址：{}；请求方式：{}；请求参数：{}；返回值类型：{}；提供参数：{}", httpUrlStr, method.name(), JSONObject.toJSONString(requestEntity), clazz.getTypeName(), JSONObject.toJSONString(requestBody));
        ResponseEntity responseEntity = restTemplate.exchange(httpUrlStr, method, requestEntity, clazz, requestBody);
        log.info("-->>--restTemplateExchange()------完成--end---总用时：{} 毫秒；返回 {} 类型结果：{}", System.currentTimeMillis() - startTime, clazz.getTypeName(), JSONObject.toJSONString(responseEntity));
        return responseEntity;
    }

    // 结果集解析 totalListJSON, currentListJSON
    private Integer explainResultData(JSONArray totalListJSON, JSONObject midResp, ResponseEntity responseEntity, Map<String, Object> requestBody, ConfigInterfaceQuery configInterfaceQuery, String configInfo){
        Integer size = 0;
        Object responseBodyObj = responseEntity.getBody();
        JSONObject responseBodyJSONObject = null;
        JSONArray responseBodyJSONArray = null;
        JSONArray currentDataJSONArray = new JSONArray();
        JSONObject contentJSON = new JSONObject();
        if(responseBodyObj.getClass() == JSONArray.class){
            responseBodyJSONArray = (JSONArray)responseBodyObj;

            totalListJSON.addAll(currentDataJSONArray);
            return currentDataJSONArray.size();
        }
        if(responseBodyObj.getClass() == JSONObject.class){
            responseBodyJSONObject = (JSONObject)responseBodyObj;

        }
        try {
            String respDataField = configInterfaceQuery.getRespDataField();


            if(StringUtils.isEmpty(respDataField)){
                log.info(configInfo + "respDataField.isEmpty()");

                /**
                 * [{"tenantId":"t1001","userId":"u1001"},{"tenantId":"t1002","userId":"u1002"},{"tenantId":"t1003","userId":"u1003"},{"tenantId":"t1004","userId":"u1004"}]
                 * **/
                currentDataJSONArray = responseBodyJSONObject.getJSONArray(configInterfaceQuery.getRespDataField());    // "content:[]"
            }else{
                log.info(configInfo + "respDataField.isNotEmpty()");
//                contentJSON = responseBodyJSONObject.getJSONObject(configInterfaceQuery.getRespDataField());    // "content"
                currentDataJSONArray = responseBodyJSONObject.getJSONArray(respDataField.substring(respDataField.indexOf(":")));
                if(Objects.nonNull(currentDataJSONArray)) {
                    totalListJSON.addAll(currentDataJSONArray);
//                    String isLastPageFields = configInterfaceQuery.getIsLastPageFields();
//                    DataUtil.explainField(midResp, contentJSON, isLastPageFields,false,null);
                }  // "list"
            }
            if(CollectionUtil.isNotEmpty(currentDataJSONArray)) {
                int dataSize = currentDataJSONArray.size();
                if(contentJSON.containsKey("total") && contentJSON.containsKey("pageSize") &&
                        (contentJSON.getInteger("total")>contentJSON.getInteger("pageSize"))) dataSize = 1; // 有数据，继续试取
                totalListJSON.addAll(currentDataJSONArray);
                if(requestBody.containsKey("isStreamData") && (Boolean)requestBody.get("isStreamData")) {
                    String streamDataPageKey = (String) requestBody.get("streamDataPageKey");
                    String streamDataSrcKey = (String) requestBody.get("streamDataSrcKey");
                    JSONObject lastDataJson = currentDataJSONArray.getJSONObject(currentDataJSONArray.size()-1);    // 最后一条记录
                    if(!lastDataJson.containsKey(streamDataSrcKey)) DataUtil.recordLogAndThrow("流式取数，未取得下一页的起始标识key");
                    requestBody.put(streamDataPageKey, lastDataJson.get(streamDataSrcKey)); // 从最后一条记录中，取出 下一页流式标识key的值。
                    requestBody.put("pageNum", 0);  // 恢复初始化，后面程序会增加 1
                }
                return dataSize;
            }
//            log.info("当前解析所得：{}", currentDataJSONArray);
//            log.info("结果集当前累计收集：{}", totalListJSON);
        }catch(ClassCastException e){
//            log.info("list String 解析所得：" + currentDataJSONArray);
            log.error(configInfo + "接口解析有误，无法依据接口返回值解析，请核查！");
        }
        return size;
    }



    private void explainData(JSONArray outputJSONArray, JSONArray inputJSONArray, String fields){
        inputJSONArray.stream().forEach(json-> explainData(outputJSONArray, (JSONObject)json, fields));
    }


    /***
     * 属性层级类型
     * a.b
     * a.b.c
     * a.b.d
     * a.b.e.f
     * a.b.e.g
     *
     * fields：.status,msg,content,total.list[].id,branchId,ownerId,orderNo,orderState,outStoreId,originalPriceTotal,createTime,finishTime
     * @param outputJSONArray
     * @param inputJSONObject
     * @param fields
     */
    private void explainData(JSONArray outputJSONArray, JSONObject inputJSONObject, String fields){
        JSONObject headJSON = new JSONObject();
        headJSON.put("f1", inputJSONObject.get("f1-v1"));
        headJSON.put("f2", inputJSONObject.get("f2-v2"));

    }

    // 解析结果 并判断 是否为末页
    private boolean explainDataJudgeIsLastPage(JSONArray totalListJSON, String jsonStr, Map<String, Object> requestBody, ConfigInterfaceQuery interfaceQuery, String configInfo){
        JSONObject json = (JSONObject)JSON.parse(jsonStr);
        log.info(configInfo + "返回值-json解析 所得：{}", json.toJSONString());
//        log.info("content String 解析所得：" + contentStr);
        JSONObject response = new JSONObject();
        // 解析结果集
        Integer resultDataSize = this.explainResultData(totalListJSON, response, ResponseEntity.ok(json), requestBody, interfaceQuery, configInfo);

        // 判断返回值状态
        this.judgeReturnStatus(json, interfaceQuery);

        String isLastPageFields = interfaceQuery.getIsLastPageFields();
        log.info(configInfo + "isLastPageFields：{}", isLastPageFields);
        if(StringUtils.isBlank(isLastPageFields)) DataUtil.recordLogAndThrow("翻页-末页判断，未配置，请检查。");
        if("*".equals(isLastPageFields)) { // (1)无需判断*
            log.info(configInfo + "isLastPageFields = *：无需判断");
            return true;
        }
        String fieldKey = isLastPageFields.substring(0, isLastPageFields.indexOf(":"));  // 返回指定字符在字符串中第一次出现处的索引，如果此字符串中没有这样的字符，则返回 -1

        Object isLastPageObj = new Object();
        if(response.containsKey(fieldKey)) {
            isLastPageObj = response.get(fieldKey);
        }else{
            DataUtil.recordLogAndThrow(configInfo + "分页判断参数配置错误，请核对！");
        }
        log.info(configInfo + "分页取值返回：{}", isLastPageObj);
        boolean isLastPage = false;
        if(Objects.nonNull(isLastPageObj)){
            try {
                Boolean isLastPageFlag = Boolean.valueOf(String.valueOf(isLastPageObj));
                isLastPage = (isLastPageFlag).booleanValue();
                requestBody.put("pageNum", (int)requestBody.get("pageNum")+1);
                return isLastPage;
            }catch(Exception ex){
                DataUtil.recordLogAndThrow(configInfo + "分页判断转化无效，转化前："+isLastPageObj);
            }
        }

        log.info(configInfo + "explainData()判断结果: isLastPage: {}", isLastPage);
        if(resultDataSize > 0){
            // 查询到数据集，继续试查
            requestBody.put("pageNum", (int)requestBody.get("pageNum")+1);
            return false;
        }
        log.info(configInfo + "未查询到数据了，结束查询。");
        return true;
    }

    private void judgeReturnStatus(Object obj1, Object obj2){

    }
}
