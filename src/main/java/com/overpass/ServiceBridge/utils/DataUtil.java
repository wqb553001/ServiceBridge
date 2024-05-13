package com.overpass.ServiceBridge.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.Options;
import com.overpass.ServiceBridge.constans.PublicConstants;
import com.overpass.ServiceBridge.exception.BusinessException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.logging.log4j.core.util.Assert;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @ClassName DataUtil
 * @Description: 基本信息字段填充
 * @Author wqb
 * @Date 2020/12/20 18:00
 * @Version V1.0
 **/
@Log4j2
public class DataUtil {

    /**
     * 数据转化 并 封装到 result 中
     * @param src       源数据
     * @param target    目标数据
     * @return
     */
    public static Result copyToResult(Object src, Object target){
        if(src == null){
            return new Result(ResultCodeEnum.ERROR, null);
        }
        BeanUtil.copyProperties(src, target);
        return new Result(ResultCodeEnum.OK, target);
    }

    /**
     * 数据转化 并 封装到 result 中
     * @param sourceList     源数据集
     * @param targetList    目标数据集
     * @return
     */
    public static Result copyListToResult(List sourceList, List targetList, Class targetClazz){
        if(CollectionUtils.isEmpty(sourceList)){
            return new Result(ResultCodeEnum.ERROR, null);
        }
        try {
            Iterator var3 = sourceList.iterator();

            while(var3.hasNext()) {
                Object items = var3.next();
                Object target = targetClazz.newInstance();
                BeanUtils.copyProperties(items, target);
                targetList.add(target);
            }
        } catch (Exception e) {
            String errorStr = "数据集转化异常：sourceList = " + sourceList;
            log.error(errorStr);
            e.printStackTrace();
            throw new RuntimeException(errorStr);
        }

        return new Result(ResultCodeEnum.OK, targetList);
    }

    /**
     * @Description: 判断是否有重复【可通过已用的实例进行理解】
     * @Param: id   比较的id 【新增或修改，新增id为 isNull，修改id为 nonNull】
     * @Param: ids  依据重复检查条件，查询出的所有id集
     * @Return: true:存在重复；false:未重复
     * @Author: wqb
     * @Date: 2021/1/12 10:50
    **/
    public static boolean haveRepeated(Integer id, List<Integer> oldIds){
        // 未查出任何历史数据，判定未重复
        if(CollectionUtils.isEmpty(oldIds)) return false;
        // 修改：不可改成其他已存在的
        if(Objects.nonNull(id) && !CollectionUtils.isEmpty(oldIds)) {
            // 检查 是否存在 除 [原id] 之外的id存在
            return !CollectionUtils.isEmpty(oldIds.stream().filter(oldId -> !oldId.equals(id)).collect(Collectors.toList()));
        }
        // 新增：不可与任何值重复（不应该查出任何数据，应该为空）
        return Objects.isNull(id) && !CollectionUtils.isEmpty(oldIds);
    }

    /**
     * @Description: strContent StringUtils.isBlank(strContent) 判断为空，则记录 error 日志后，抛 RuntimeException 异常
     * @Param: [errorMsg：异常信息]
     * @Return: void
     * @Author: wqb
     * @Date: 2021-1-20 20:35:22
    **/
    public static void strIsBlankRecordLogAndThrow(String strContent, String errorMsg){
        if(StringUtils.isBlank(strContent)) recordLogAndThrow(errorMsg);
    }

    /**
     * @Description: 记录 error 日志后，抛 RuntimeException 异常
     * @Param: [errorMsg：异常信息]
     * @Return: void
     * @Author: wqb
     * @Date: 2021-1-20 20:35:22
    **/
    public static void recordLogAndThrow(String errorMsg, Object... var2){
        log.error(errorMsg, var2);
        throw new RuntimeException(errorMsg);
    }

    /**
     * @Description: 记录 error 日志后，抛 RuntimeException 异常
     * @Param: [errorMsg：异常信息]
     * @Return: void
     * @Author: wqb
     * @Date: 2021-1-20 20:35:22
    **/
    public static void recordLogAndThrowBusiness(String errorMsg, Object... var2){
        log.error(errorMsg, var2);
        throw new BusinessException(errorMsg);
    }

    /**
     * @Description: 判断[数据集]是否为空，若为空：则记录 error 日志后，抛 RuntimeException 异常
     * @Param: [dataList：数据集]
     * @Return: void
     * @Author: wqb
     * @Date: 2021/1/20 15:12
    **/
    public static void judgeCollectEmptyToRecordLogAndThrow(Collection<?> collection){
        judgeCollectEmptyToRecordLogAndThrow(collection, "数据集为空。");
    }

    /**
     * @Description: 判断[数据集]是否为空，若为空：则记录 error 日志后，抛 RuntimeException 异常
     * @Param: [collection：数据集]
     * @Param: [errorMsg, 异常消息]
     * @Return: void
     * @Author: wqb
     * @Date: 2021/1/20 15:12
    **/
    public static void judgeCollectEmptyToRecordLogAndThrow(Collection<?> collection, String errorMsg){
        if(CollectionUtils.isEmpty(collection)){
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
    }

    /**
     * @Description: 判断[数据集]是否为空，若为空：则记录 error 日志后，抛 NotFoundCostSchemeException 异常自定义异常
     * @Param: [collection：数据集]
     * @Param: [errorMsg, 异常消息]
     * @Return: void
     * @Author: wqb
     * @Date: 2021/1/20 15:12
    **/
    public static void judgeCollectEmptyToRecordLogAndThrowBusinessException(Collection<?> collection, String errorMsg){
        if(CollectionUtils.isEmpty(collection)){
            log.error(errorMsg);
            throw new BusinessException(errorMsg);
        }
    }

    /**
     * @Description: 判断[数据集]是否为空，若为空：则记录 error 日志后，抛 RuntimeException 异常
     * @Param: [map：数据集]
     * @Param: [errorMsg, 异常消息]
     * @Return: void
     * @Author: wqb
     * @Date: 2021/1/20 15:12
    **/
    public static void judgeCollectEmptyToRecordLogAndThrow(Map<?, ?> map, String errorMsg){
        if(CollectionUtils.isEmpty(map)){
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
    }

    public static Boolean giveUpConditionJudge(JSONObject jsonObject, String giveUpConditionField, String configInfo){
        if(StringUtils.isNotBlank(giveUpConditionField)){
            Object giveUpCondition = new Object();
            if(giveUpConditionField.contains(":")){
                String startFields = giveUpConditionField.substring(0, giveUpConditionField.indexOf(":"));
                String endFields = giveUpConditionField.substring(giveUpConditionField.lastIndexOf(":")+1);
                if(DataUtil.isNumber(startFields) && Integer.valueOf(startFields) == 0){
                    giveUpCondition = DataUtil.operation(jsonObject, endFields, true, false, null, configInfo);
                }else{
                    giveUpCondition = DataUtil.operation(jsonObject, endFields, false, false, null, configInfo);
                }
            }else {
                giveUpCondition = DataUtil.operation(jsonObject, giveUpConditionField, true, false, null, configInfo);
            }
            log.info("放弃生成条件判断结果：{} 条件：{}", giveUpCondition, giveUpConditionField);
            if(giveUpCondition instanceof Boolean){
                return (Boolean) giveUpCondition;
            }
            DataUtil.recordLogAndThrow("判断条件配置，无法计算出 Boolean 类型值，请核对配置！"+giveUpConditionField);
        }
        return null;
    }

    /**
     * @Description: 日期字符串 转化输出 指定格式的 日期字符串
     * @param data(时间字符串)
     * @param formatStr(目标格式)
     * @Return: java.lang.String
     * @Author: wqb
     * @Date: 2021/1/20 16:07
    **/
    public static String dateStrToDateStr(String data, String formatStr){
        if(StringUtils.isBlank(data) || "null".equals(data)) {
            log.error("异常数据处理。格式化数据，源数据为:{}, formatStr{}",data, formatStr);
            return "9999-01-01";
        }
        String result = data;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatStr);//注意月份是MM
        try {
            Date date = simpleDateFormat.parse(data);
            result = simpleDateFormat.format(date);  //2019-09-02
        }catch (ParseException e){
            if(StringUtils.isNumeric(data)){
                try {
                    result = transferLongToDate(simpleDateFormat, Long.valueOf(data));
                }catch (Exception fe){
                    log.error("[数值型字符串日期]转[{}日期格式]，异常。原数据为：{}", formatStr, data);
                    fe.printStackTrace();
                }
            }else {
                log.error("[字符串日期]转[{}日期格式]，异常。原数据为：{}", formatStr, data);
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * long转date
     * @param dateLong
     * @return
     */
    public static Date longToDate(long dateLong){
        Date date = new Date(dateLong);
        return date;
    }

    /**
     * date转string
     * @param date
     * @return
     */
    public static String dateToString(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String str = sdf.format(date);
        return str ;
    }



    /**
     * long先转date后转string
     * @param dateLong
     * @return
     */
    public static String dateLongToString(long dateLong){
        return dateToString(longToDate(dateLong));
    }


    /**
     * 把毫秒转化成日期
     * @param dateFormat(日期格式)
     * @param millSec(毫秒数)
     * @return
     */
    private static String transferLongToDate(String dateFormat, Long millSec){
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Date date= new Date(millSec);
        return sdf.format(date);
    }

    /**
     * 把毫秒转化成日期
     * @param dateFormat(日期格式)
     * @param millSec(毫秒数)
     * @return
     */
    private static String transferLongToDate(SimpleDateFormat dateFormat, Long millSec){
        Date date= new Date(millSec);
        return dateFormat.format(date);
    }

    /**
     * 比较两个时间相差的月份
     * @param beginLocalDate
     * @param endLocalDate
     * @return
     */
    public static Integer monthBetweenLocalDate(LocalDate beginLocalDate, LocalDate endLocalDate) {
        Long lMonths = DateUtil.betweenMonth(Date.from(beginLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()), Date.from(endLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()), Boolean.FALSE);
        return lMonths.intValue();
    }

    /**
     * 判断 是否为 Integer 类型的字符串
     * @param str
     * @return
     */
    public static boolean isInteger(String str) {
        if(StringUtils.isBlank(str)) return false;
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    /**
     * 判断 是否为 数字 的字符串
     * @param numberStr
     * @return
     */
    public static boolean isNumber(String numberStr){
        return NumberUtils.isCreatable(numberStr);
    }

    /**
     * 截取指定长度的字符串
     *
     * @param str 原字符串
     * @param len 长度
     * @return 如果str为null，则返回null；如果str长度小于len，则返回str；如果str的长度大于len，则返回截取后的字符串
     */
    public static String subStrByStrAndLen(String str, int len) {
        return null != str ? str.substring(0, str.length() > len ? len : str.length()) : null;
    }

    /**
     * @Description: 将 [待删字符串] 从 [原字符串]中，移除。有则移除；没有则，返回原字符串。有没有的判断规则是，逗号分隔的字符串相比较。
     * @Param: [oldStr]     原字符串
     * @Param: [subStr]     待删字符串
     * @Return: java.lang.String
     * @Author: wqb
     * @Date: 2021/2/24 19:07
    **/
    public static String subStrByStr(String oldStr, String subStr){
        if(StringUtils.isBlank(oldStr) || StringUtils.isBlank(subStr)) return "";
        String[] split = oldStr.split(",");
        if(split == null || split.length < 1) return "";
        List<String> stringListOld = Arrays.asList(split);
        List<String> stringList = new ArrayList<>(stringListOld.size());
        stringList.addAll(stringListOld);
        Iterator<String> it = stringList.iterator();
        while (it.hasNext()){
            String value = it.next();
            if (subStr.equals(value)) {
                it.remove();
            }
        }
        if(CollectionUtils.isEmpty(stringList)) return "";
        return StringUtils.join(stringList,",");
    }

    /**
     * @Description: 将[新字符串] 追加到 [原字符串]中，用逗号分隔；发现重复则不添加，返回原字符串。
     * @Param: [oldStr]         原字符串
     * @Param: [appendStr]      新字符串
     * @Return: java.lang.String
     * @Author: wqb
     * @Date: 2021/2/24 19:06
    **/
    public static String appendStrToStr(String oldStr, String appendStr){
        if(StringUtils.isBlank(oldStr)){
            return appendStr;
        }
        if(StringUtils.isBlank(appendStr)) {
            return oldStr;
        }
        String[] split = oldStr.split(",");
        if(split == null || split.length < 1) return appendStr;
        List<String> stringList = Arrays.asList(split);
        if(stringList.contains(appendStr)) return oldStr;
        if(CollectionUtils.isEmpty(stringList)) return appendStr;

        return oldStr+","+appendStr;
    }

    /**
     * 把列表转换为树结构
     *
     * @param originalList 原始list数据
     * @return 组装后的集合
     */
    public static <T> List<T> getTree(List<T> originalList) throws Exception {
        String keyName = "id";
        return getTree(originalList, keyName);
    }

    /**
     * 把列表转换为树结构
     *
     * @param originalList  原始list数据
     * @param keyName       作为唯一标示的字段名称
     * @return              组装后的集合
     */
    public static <T> List<T> getTree(List<T> originalList, String keyName) throws Exception {
        String parentFieldName = "pid";
        String childrenFieldName = "children";

        // 获取根节点，即找出父节点为空的对象
        List<T> topList = new ArrayList<>();
        for (int i = 0; i < originalList.size(); i++) {
            T t = originalList.get(i);
            String parentId = BeanUtil.getProperty(t, parentFieldName);
            if (StringUtils.isBlank(parentId) || "0".equals(parentId)) {
                topList.add(t);
            }
        }

        // 将根节点从原始list移除，减少下次处理数据
        originalList.removeAll(topList);

        // 递归封装树
        fillTree(topList, originalList, keyName, parentFieldName, childrenFieldName);

        return topList;
    }

    /**
     * 封装树
     *
     * @param parentList        要封装为树的父对象集合
     * @param originalList      原始list数据
     * @param keyName           作为唯一标示的字段名称
     * @param parentFieldName   模型中作为parent字段名称
     * @param childrenFieldName 模型中作为children的字段名称
     */
    public static <T> void fillTree(List<T> parentList, List<T> originalList, String keyName, String parentFieldName, String childrenFieldName) throws Exception {
        for (int i = 0; i < parentList.size(); i++) {
            List<T> children = fillChildren(parentList.get(i), originalList, keyName, parentFieldName, childrenFieldName);
            if (children.isEmpty()) {
                continue;
            }
            originalList.removeAll(children);
            fillTree(children, originalList, keyName, parentFieldName, childrenFieldName);
        }
    }

    /**
     * 封装子对象
     *
     * @param parent            父对象
     * @param originalList      待处理对象集合
     * @param keyName           作为唯一标示的字段名称
     * @param parentFieldName   模型中作为parent字段名称
     * @param childrenFieldName 模型中作为children的字段名称
     */
    public static <T> List<T> fillChildren(T parent, List<T> originalList, String keyName, String parentFieldName, String childrenFieldName) throws Exception {
        List<T> childList = new ArrayList<>();
        String parentId = BeanUtil.getProperty(parent, keyName);
        for (int i = 0; i < originalList.size(); i++) {
            T t = originalList.get(i);
            String childParentId = BeanUtil.getProperty(t, parentFieldName);
            if (parentId.equals(childParentId)) {
                childList.add(t);
            }
        }
        if (!childList.isEmpty()) {
            FieldUtils.writeDeclaredField(parent, childrenFieldName, childList, true);
        }
        return childList;
    }

    /**
     * 根据父节点id,获取子树
     * @param pid           父节点id
     * @param originalList  数据集
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> List<T> getChildrenTreeByParentId(String pid, List<T> originalList) throws Exception {
        String keyName = "id";
        String parentFieldName = "pid";
        String childrenFieldName = "children";
        List<T> topList = new ArrayList<>();
        if(StringUtils.isBlank(pid)) {
            for (int i = 0; i < originalList.size(); i++) {
                T t = originalList.get(i);
                pid = BeanUtil.getProperty(t, parentFieldName);
                if (StringUtils.isBlank(pid) || "0".equals(pid)) {
                    topList.add(t);
                }
            }
        }else{
            for (int i = 0; i < originalList.size(); i++) {
                T t = originalList.get(i);
                if (pid.equals(BeanUtil.getProperty(t, parentFieldName))) {
                    topList.add(t);
                }
            }
        }

        // 递归封装树topList
         fillTree(topList, originalList, keyName, parentFieldName, childrenFieldName);
        return topList;
    }

    /**
     * JSONArray 依据 已知指定字段 分组
     * @param jsonArray
     * @param fieldName
     * @return
     */
    public static Map<Object, JSONObject> jsonArrayToMap(JSONArray jsonArray, String fieldName){
        Map<Object, JSONObject> result = new HashMap();
        for(Object obj:jsonArray){
            JSONObject jsonObject = (JSONObject) obj;
            result.put(String.valueOf(jsonObject.get(fieldName)), jsonObject);
        }
        return result;
    }

    public static void jsonCopy(JSONObject targetJson, JSONObject sourceJson){
        if(Objects.isNull(sourceJson)) return;
        if(Objects.isNull(targetJson)) targetJson = new JSONObject();
        for(Map.Entry entry:sourceJson.entrySet()){
            targetJson.put((String)entry.getKey(), entry.getValue());
        }
    }

    /**
     *  从 json 对象 读取数据字段
     * @param jsonObj       json 数据源
     * @param objField      读取对象字段
     * @param defaultVal
     * @return
     */
    public static String explainJsonWithField(JSONObject jsonObj, String objField, String defaultVal, String configInfo){
        String resultVal = null;
        if(objField.equals("?")){
            if(StringUtils.isBlank(defaultVal)) DataUtil.recordLogAndThrow(configInfo + "固定参数不可为空，请核查！");
            return defaultVal;
        }
        if (objField.startsWith("#")) { // 代表 需要计算，通过计算表达式 计算所得
            // #表达式处理
            objField = objField.trim();
            objField = objField.substring(1);
            Object operationVal = DataUtil.operation(jsonObj, objField, true, null, configInfo);
//            log.info("explainField() end --->>> outJson：{}; inJson：{}; fieldStyle：{}; isNum:{}; solid:{}", outJson, inJson, fieldStyle, isNum, solid);
            return String.valueOf(operationVal);
        }
        if (objField.contains(".")) {
            String[] multiValues = objField.split("\\.");
            String multiValue = multiValues[0];
            JSONObject multiObj = jsonObj.getJSONObject(multiValue);
            for (int i = 1; i < multiValues.length - 1; i++) {
                multiObj = multiObj.getJSONObject(multiValues[i]);
            }
            resultVal = multiObj.getString(multiValues[multiValues.length - 1]);
        } else {
            resultVal = jsonObj.getString(objField);
        }

//        if(StringUtils.isBlank(resultVal)) DataUtil.recordLogAndThrow("字段"+objField+"读取为null，请核对配置类型是否为 a 或 a.b.c 格式，不可为 a:b 格式。如果格式正确，请检查数据源是否具有相应值。");
        if(StringUtils.isBlank(resultVal)) {
            log.info(configInfo + "字段 {} 读取为null，请核对配置类型是否为 a 或 a.b.c 格式，不可为 a:b 格式。如果格式正确，请检查数据源是否具有相应值。", objField);
        }
//        log.info("explainJsonWithField() ==== jsonObj：{}；objField：{}；resultVal：{}", jsonObj, objField, resultVal);
        return resultVal;
    }

    public static void explainResponse(JSONObject outJson, JSONObject inJson, String fields, String solid, String configInfo){
        String[] split = fields.split(",");
        explainResponse(outJson, inJson, split, solid, configInfo);
    }

    public static void explainResponse(JSONObject outJson, JSONObject inJson, String[] split, String solid, String configInfo){
        try {
            for (String fieldStyle : split) {
                if(fieldStyle.startsWith("*")){
                    outJson.put(fieldStyle.substring(1,2), fieldStyle.substring(2));
                    continue;
                }
                DataUtil.explainField(outJson, inJson, fieldStyle, true, solid, configInfo);
            }
        }catch(Exception e){
            log.info(configInfo + "发生异常：{}", e.getMessage());
            log.info(DataUtil.getStackTraceInfo(e));
        }
    }

    public static void explainFieldToMap(Map<String, Object> outMap, JSONObject inJson, String fieldStyle, boolean isNum, String solid, String configInfo){
        JSONObject outJson = new JSONObject();
        DataUtil.explainField(outJson, inJson, fieldStyle, isNum, solid, configInfo);
        for (String key : outJson.keySet()) {
            outMap.put(key, outJson.get(key));
        }
    }

    public static void explainField(JSONObject outJson, JSONObject inJson, String fieldStyle, boolean isNum, String solid, String configInfo){
//        log.info("explainField() begin ---<<< inJson：{}; fieldStyle：{}; isNum:{}; solid:{}", inJson, fieldStyle, isNum, solid);
        if (!fieldStyle.contains(":")) {
            outJson.put(fieldStyle, inJson.getString(fieldStyle));
//            log.info("explainField() end --->>> outJson：{}; fieldStyle：{}; isNum:{}; solid:{}", outJson, fieldStyle, isNum, solid);
            return;
        }
        String innerFieldKey = fieldStyle.substring(0, fieldStyle.indexOf(":"));  // 返回指定字符在字符串中第一次出现处的索引，如果此字符串中没有这样的字符，则返回 -1
        String innerFieldValue = fieldStyle.substring(fieldStyle.indexOf(":") + 1);
        if (StringUtils.isBlank(innerFieldKey)) {
            DataUtil.recordLogAndThrow(configInfo + "返回值读取配置 key 有误，请检查格式！参照格式：key1:value1,key2:value2.value22");
        }
        if (StringUtils.isBlank(innerFieldValue)) {
            DataUtil.recordLogAndThrow(configInfo + "返回值读取配置 value 有误，请检查格式！参照格式：key1:value1,key2:value2.value22");
        }
        innerFieldKey = innerFieldKey.trim();
        innerFieldValue = innerFieldValue.trim();
        if(innerFieldValue.startsWith("$")){
            outJson.put(innerFieldKey, innerFieldValue.substring(1));
//            log.info("explainField() end --->>> outJson：{}; inJson：{}; fieldStyle：{}; isNum:{}; solid:{}", outJson, inJson, fieldStyle, isNum, solid);
            return;
        }
        if (innerFieldValue.startsWith("#")) { // 代表 需要计算，通过计算表达式 计算所得
            // #表达式处理
            Object operationVal = DataUtil.operation(inJson, innerFieldValue, isNum, solid, configInfo);
            outJson.put(innerFieldKey, operationVal);
//            log.info("explainField() end --->>> outJson：{}; fieldStyle：{}; isNum:{}; solid:{}", outJson, fieldStyle, isNum, solid);
            return;
        }
        if (innerFieldValue.startsWith("@")) { // 代表 需要拼接，通过拼接、计算表达式 计算所得
            // #表达式处理
            Object operationVal = DataUtil.operation(inJson, innerFieldValue, false, false, solid, configInfo);
            outJson.put(innerFieldKey, operationVal);
//            log.info("explainField() end --->>> outJson：{}; fieldStyle：{}; isNum:{}; solid:{}", outJson, fieldStyle, isNum, solid);
            return;
        }
        if (innerFieldValue.startsWith("%")) { /** 有时间考虑，使用 常规通用 的数据结构 **/
            if (innerFieldValue.length() < 3 && !DataUtil.isNumber(innerFieldValue.substring(1, 2)))
                DataUtil.recordLogAndThrow(configInfo + "格式化配置有问题，请检查配置！当前为：" + innerFieldValue);
            Integer style = Integer.valueOf(innerFieldValue.substring(1, 2));
            PublicConstants.ExplainStyleEnum enumByCode = PublicConstants.ExplainStyleEnum.getEnumByCode(style);
            innerFieldValue = innerFieldValue.substring(2);
            innerFieldValue = DataUtil.explainJsonWithField(inJson, innerFieldValue, solid, configInfo);
            switch (enumByCode) {
                case STYLE_DATE:  // 日期处理：1-yyyy-MM-dd
                    try {
                        innerFieldValue = DateUtil.parse(innerFieldValue).toDateStr();
                    }catch(Exception ex){
                        innerFieldValue = DateUtil.date(Long.valueOf(innerFieldValue)).toDateStr();
                    }
                    break;
                case STYLE_DATETIME:  // 日期处理：2-yyyy-MM-dd hh:mm:ss
                    try {
                        innerFieldValue = DateUtil.parse(innerFieldValue).toString();
                    }catch(Exception ex){
                        innerFieldValue = DateUtil.date(Long.valueOf(innerFieldValue)).toString();
                    }
                    break;
                case STYLE_2_HALF_UP:  // 数值处理：3-四舍五入保留2位小数
                    innerFieldValue = new BigDecimal(innerFieldValue).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    break;
                case STYLE_2_DOWN:  // 数值处理：4-保留2位其余截去
                    innerFieldValue = new BigDecimal(innerFieldValue).setScale(2, BigDecimal.ROUND_DOWN).toString();
                    break;
            }
        } else {
            innerFieldValue = DataUtil.explainJsonWithField(inJson, innerFieldValue, solid, configInfo);
        }
        outJson.put(innerFieldKey, innerFieldValue);
//        log.info("explainField() end --->>> outJson：{}; fieldStyle：{}; isNum:{}; solid:{}", outJson, fieldStyle, isNum, solid);
    }

    // 运算
    public static Object operation(JSONObject json, String objFieldStyle, boolean isNum, String solid, String configInfo){
        return operation(json, objFieldStyle, isNum, true, solid, configInfo);
    }

    public static Object operation(JSONObject json, String objFieldStyle, boolean isNum, boolean setScale, String solid, String configInfo){
        String substring = objFieldStyle.contains("@")?(objFieldStyle.substring(objFieldStyle.indexOf("@")+1)):objFieldStyle.substring(objFieldStyle.lastIndexOf("#")+1);
        substring = substring.trim(); // 去除 前后空格
        String expression = substring.substring(0, substring.lastIndexOf("|"));
        String expressionFields = substring.substring(substring.lastIndexOf("|")+1);
        if(StringUtils.isBlank(expression)) DataUtil.recordLogAndThrow(configInfo + "依据|截取后，表达式为空，请核查！");
        if(StringUtils.isBlank(expressionFields)) DataUtil.recordLogAndThrow(configInfo + "依据|截取后，取值字段为空，请核查！");
        String[] valueFields = expressionFields.split("&");
        JSONObject response = new JSONObject();
        DataUtil.explainResponse(response, json, valueFields, solid, configInfo);
        Map<String, Object> paramsMap = new HashMap<>();
        for (Map.Entry entry : response.entrySet()) {
            Object val = entry.getValue();
            if(Objects.isNull(val)) DataUtil.recordLogAndThrow(configInfo + "字段: "+entry.getKey()+" 未取到值，请核查！");
            if(isNum) {
                paramsMap.put((String) entry.getKey(), new BigDecimal((String) val));
                continue;
            }
            paramsMap.put((String)entry.getKey(), val);
        }
        return calculator(expression, isNum, setScale, paramsMap, configInfo);
    }

    // 计算器
    public static Object calculator(String expression, boolean isNum, Map<String, Object> paramsMap, String configInfo){
        return calculator(expression, isNum, true, paramsMap, configInfo);
    }

    public static Object calculator(String expression, boolean isNum, boolean setScale, Map<String, Object> paramsMap, String configInfo){
//        log.info("calculator() begin ===== <<< expression：{}；isNum：{}；paramsMap：{}", expression, isNum, JSON.toJSONString(paramsMap));
        if(StringUtils.isBlank(expression)) DataUtil.recordLogAndThrow(configInfo + "表达式为空，请核查！");
        AviatorEvaluatorInstance instance = AviatorEvaluator.newInstance();
        if(isNum) instance.setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_DECIMAL, true);
        Expression compiledExp = instance.compile(expression);
        // 执行表达式
        Object execute = compiledExp.execute(paramsMap);
        if(setScale) execute = ((BigDecimal)execute).setScale(6, BigDecimal.ROUND_HALF_UP);  // 四舍五入-保留6位小数【默认】
        return execute;
    }

    /**
     * 获取e.printStackTrace() 的具体信息，赋值给String 变量，并返回
     *
     * @param e
     *            Exception
     * @return e.printStackTrace() 中 的信息
     */
    public static String getStackTraceInfo(Exception e) {
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            e.printStackTrace(pw);//将出错的栈信息输出到printWriter中
            pw.flush();
            sw.flush();
            return sw.toString();
        } catch (Exception ex) {
            return "printStackTrace()转换错误";
        } finally {
            if (sw != null) {
                try {
                    sw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (pw != null) {
                pw.close();
            }
        }
    }

    /**
     * 集合转字符串，去除 普通 toString()，随着元素的增多，字符串内元素间隔空格增大的问题。
     *
     * @param c
     * @return
     */
    public static String collectionToStringNoSpaces(Collection c) {
        Iterator<Collection> it = c.iterator();
        if (! it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;) {
            Object next = it.next();
            sb.append(next == c ? "(this Collection)" : next);
            if (! it.hasNext())
                return sb.append(']').toString();
            sb.append(',');
        }
    }

    /**
     * 字符串追加，超过指定数值，则丢弃前面部分。
     *
     * Examples:
     *  oldStrArray: [5060, 5061, 5061, 5062]
     *  appendTailStr: 5063
     *  limitLength:4
     * returns [5061, 5061, 5062, 5063]
     *
     * @param oldStrArray
     * @param appendTailStr
     * @param limitLength
     * @return
     */
    public static String appendStringLimitLength(String oldStrArray, String appendTailStr, int limitLength){
        String typeVal = oldStrArray.replace("]", ","+appendTailStr+"]");
        if(typeVal.split(",").length>limitLength){
            typeVal = "["+ typeVal.substring(typeVal.indexOf(",")+1);
        }
        return typeVal;
    }

    /**
     * 处理 Get 请求方式 Url 参数拼接
     *  1.如果 httpUrlStr 中有了 "?" 连接符，且不是最后一个字符，则用 "&" 连接所有参数拼接在后。
     *  2.如果 httpUrlStr 中没有 "?" 连接符，则用 "&" 连接所有参数，并将第一个 "&" 替换成 "?" 进行拼接。
     *  3.如果 httpUrlStr 中有了 "?" 连接符，且正好是最后一个字符，则用 "&" 连接所有参数，并将第一个 "&" 替换成 "?" 进行拼接。
     * @param httpUrlStr：url 地址
     * @param requestBody：请求参数
     * @return
     * @Author: wqb
     * @Date: 2024-4-19 17:29:40
     */
    public static String handleGetMethodUrlSuffix(String httpUrlStr, Map<String, Object> requestBody){
        if(CollectionUtil.isEmpty(requestBody)) return httpUrlStr;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry: requestBody.entrySet()) {
            sb.append("&" + Assert.requireNonEmpty(entry.getKey(), "形参，不能为空！") + "=" + Assert.requireNonEmpty(entry.getValue(), entry.getKey()+"，对应的值，不能为空！"));
        }
        if(!httpUrlStr.contains("?")){
            // url 中没有"?"连接符，则将上面的 字符串第一个字符串"&"，替换为"?"
            sb.deleteCharAt(0);
            sb.insert(0, '?');
            return httpUrlStr + sb.toString();
        }
        // url 中已有"?"连接符
        if(httpUrlStr.endsWith("?"))
        {   // 最后一个字符正好为"?"，则将 首个"&"去除，然后拼接
            sb.deleteCharAt(0);
            return httpUrlStr + sb.toString();
        }
        // url 中已有"?"连接符，最后一个不是 "?"，则直接拼接
        return httpUrlStr + sb.toString();
    }

    /**
     *
     * @param inputJSON
     * @param fields
     * @return
     */
    // fields:  content.list
    public static void readMultiLayerJSON(JSONArray outputJSONOArray, JSONObject inputJSON, String fields) {
        JSONObject result = new JSONObject();
        if(!fields.contains(".")){
            outputJSONOArray.add(inputJSON.get(fields));
            return;
        }
        if(fields.contains(".")){
            String headField = fields.substring(0, fields.indexOf("."));
        }

        inputJSON.keySet().forEach(key -> {
            Object value = inputJSON.get(key);
            if (value instanceof JSONObject) {
                // 如果值是JSONObject，则递归读取
                readMultiLayerJSON(outputJSONOArray, (JSONObject) value, null);
            } else {
                // 如果不是JSONObject，直接放入结果
                result.put(key, value);
            }
        });
        return ;
    }

    /**
     * 字符串 转 HashMap
     *
     * @param inputStr  （其他默认参数说明 delimiterRegex 分隔符 default: ";"  connectorRegex 连接符 default: "="）
     * @return
     */
    public static Map<String, String> strToMap(String inputStr) {
        return strToMap(inputStr, ";", "=");
    }

    /**
     * 字符串 转 HashMap
     *
     * @param inputStr
     * @param connectorRegex 连接符 default: "="   (其他默认参数说明 delimiterRegex 分隔符 default: ";")
     * @return
     */
    public static Map<String, String> strToMap(String inputStr, String connectorRegex) {
        return strToMap(inputStr, ";", connectorRegex);
    }

    /**
     * 字符串 转 HashMap
     *
     * @param inputStr
     * @param delimiterRegex    分隔符 default: ";"
     * @param connectorRegex    连接符 default: "="
     * @return
     */
    public static Map<String, String> strToMap(String inputStr, String delimiterRegex, String connectorRegex) {
        inputStr = inputStr.replace("\n","");   // 去除 换行符
        inputStr = inputStr.replace(" ","");    // 去除 空格
        return Stream.of(inputStr.split(delimiterRegex))
                .map(kv -> kv.split(connectorRegex))
                .filter(kv -> kv.length == 2)
                .collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
    }

    public static void explainKeys(JSONArray totalListJSON, JSONObject outputJSONObject, JSONObject inputJSONObject,
                                        Map<String, String> fieldsMap) {
        DataUtil.explainKeys(totalListJSON, outputJSONObject, inputJSONObject, fieldsMap, 1, "", "");
    }

    public static void explainKeys(JSONArray totalListJSON, JSONObject outputJSONOArray, JSONObject inputJSONObject,
                                        Map<String, String> fieldsMap, String contentField) {
        DataUtil.explainKeys(totalListJSON, outputJSONOArray, inputJSONObject, fieldsMap, 1, "", contentField);
    }

    private static void explainKeys(JSONArray totalListJSON, JSONObject outputJSONObject, JSONObject inputJSONObject,
                                        Map<String, String> fieldsMap, int level, String eField, String contentField) {
        String sLevelKey = (level==1) ? (level + "s") : (level + "s" + "-" + eField);
        String eLevelKey = (level==1) ? (level + "e") : (level + "e" + "-" + eField);
        String aLevelKey = (level==1) ? (level + "a") : (level + "a" + "-" + eField);
        // 全取时，全收录
        if((level==1)&&StringUtils.isBlank(contentField)) totalListJSON.add(outputJSONObject);

        // 单值
        if(fieldsMap.containsKey(sLevelKey)){
            String sKeys = fieldsMap.get(sLevelKey);
            if(!sKeys.contains(",")){
                DataUtil.explainKey(outputJSONObject, inputJSONObject, sKeys);
            }else{
                String[] sKeyFields = sKeys.split(",");
                for(String sKeyField: sKeyFields){
                    DataUtil.explainKey(outputJSONObject, inputJSONObject, sKeyField);
                }
            }
        }

        // 实体对象
        if(fieldsMap.containsKey(eLevelKey)){
            String eKeys = fieldsMap.get(eLevelKey);
            int eLevel = level + 1;
            if(!eKeys.contains(",")){
                DataUtil.explainKeys(totalListJSON, outputJSONObject, (JSONObject) inputJSONObject.get(eKeys), fieldsMap, eLevel, eKeys, contentField);
            }else{
                String[] sKeyFields = eKeys.split(",");
                for(String sKeyField: sKeyFields){
                    DataUtil.explainKeys(totalListJSON, outputJSONObject, (JSONObject) inputJSONObject.get(sKeyField), fieldsMap, eLevel, sKeyField, contentField);
                }
            }
        }

        // 数组
        if(fieldsMap.containsKey(aLevelKey)){
            int aLevel = level + 1;
            String eKeys = fieldsMap.get(aLevelKey);
            if(!eKeys.contains(",")){
                DataUtil.explainArray(totalListJSON, outputJSONObject, inputJSONObject, fieldsMap, aLevel, eKeys, contentField);
            }else{
                String[] sKeyFields = eKeys.split(",");
                for(String sKeyField: sKeyFields){
                    DataUtil.explainArray(totalListJSON, outputJSONObject, inputJSONObject, fieldsMap, aLevel, sKeyField, contentField);
                }
            }
        }
    }

    public static void explainArray(JSONArray totalListJSON, JSONObject outputJSONObject, JSONObject inputJSONObject, Map<String, String> fieldsMap, int level, String sKeyField, String contentField) {
        JSONArray jsonArray = inputJSONObject.getJSONArray(sKeyField);
        if(CollectionUtil.isNotEmpty(jsonArray)){
            Object jsonObj = jsonArray.get(0);
            if(!(jsonObj instanceof JSONObject)){
                outputJSONObject.put(sKeyField, jsonArray);
            }else {
                JSONArray innerJsonArray = new JSONArray();
                // 结果集 合并存放
                outputJSONObject.put(sKeyField, innerJsonArray);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject innerJSONObject = new JSONObject();
                    innerJsonArray.add(innerJSONObject);
                    DataUtil.explainKeys(totalListJSON, innerJSONObject, (JSONObject) jsonArray.get(i), fieldsMap, level, sKeyField, contentField);
                }
                // 结果集 单独存放
                if(StringUtils.equals(sKeyField, contentField)) {
                    totalListJSON.addAll(innerJsonArray);
                    if(outputJSONObject.containsKey(contentField)){
                        // 外层 去除 结果集，避免重复存放
                        outputJSONObject.remove(contentField);
                    }
                }
            }
        }else{
            outputJSONObject.put(sKeyField, Lists.newArrayList());
        }
    }

    public static void explainKey(JSONObject outputJSONObject, JSONObject inputJSONObject, String eKeys) {
        if(!eKeys.contains(",")){
            DataUtil.explainSimpleField(outputJSONObject, inputJSONObject, eKeys);
        }else{
            String[] sKeyFields = eKeys.split(",");
            for(String sKeyField: sKeyFields){
                DataUtil.explainSimpleField(outputJSONObject, inputJSONObject, sKeyField);
            }
        }
    }

    public static void explainSimpleField(JSONObject outputJSONObject, JSONObject inputJSONObject, String eKeys) {
        if(!eKeys.contains("=")){
            outputJSONObject.put(eKeys, inputJSONObject.get(eKeys));
        }else{
            // "oldField=newField"
            String oldField = eKeys.substring(0, eKeys.indexOf("="));
            String newField = eKeys.substring(eKeys.indexOf("=")+1);
            outputJSONObject.put(newField, inputJSONObject.get(oldField));
        }
    }

    public static boolean judge(JSONObject inputJSONObject, String condition, String configInfo) {
        if(!condition.contains("#")){
            String keyField = condition.substring(0, condition.indexOf("="));
            String valField = condition.substring(condition.lastIndexOf("=")+1);
            if(StringUtils.equals(valField.toLowerCase(), "true") || StringUtils.equals(valField.toLowerCase(), "false")){
                if(inputJSONObject.containsKey(keyField)) {
                    Object o = inputJSONObject.get(keyField);
                    String oStr = o.toString().toLowerCase();
                    if(StringUtils.equals(oStr, valField.toLowerCase())){
                        return true;
                    }
                }
            }
        }

        try {
            Object operation = DataUtil.operation(inputJSONObject, condition, true, false, null, configInfo);
            if (operation instanceof Boolean && Boolean.TRUE.equals(operation)) {
                return true;
            }
        }catch (Exception e){
            DataUtil.recordLogAndThrow(configInfo + "judge() 判断条件配置，无法计算出 Boolean 类型值，请核对配置！"+condition);
        }

        return false;
    }

    public void explainData(JSONArray outputJSONArray, JSONObject outputJSONObject, JSONArray inputJSONArray, String fields, String contentField){
        inputJSONArray.stream().forEach(json-> explainData(outputJSONArray, outputJSONObject, (JSONObject)json, fields, contentField));
    }

    /***
     * 属性层级类型
     * a.b
     * a.b.c
     * a.b.d
     * a.b.e.f
     * a.b.e.g
     *
     * field
     * 1s: status,code,msg;
     * 1e: content;
     * 2a-content: list;
     * 2s-content: total;
     * 2e-content: page;
     * 3s-page: pageNum,pageSize,size,pages,prePage,nextPage,isFirstPage,isLastPage;
     * 3s-list: id,branchId,ownerId,orderNo,outStoreId,finishTime,createTime,payPriceTotal;
     * 3e-list: order,wms;
     * 4s-wms: wmsId,wmsName;
     * 4s-order: orderNo,orderState,memberId;
     *
     * e：代表 有子属性，取子属性
     * a：代表 为数组类型，遍历取
     * s：代表 单属性，直取
     * 重命名 连接符为 “=”
     * fields：.status,msg,content,total.list[].id,branchId,ownerId,orderNo,orderState,outStoreId,originalPriceTotal,createTime,finishTime
     * @param totalListJSON
     * @param inputJSONObject
     * @param fields
     */
    public static void explainData(JSONArray totalListJSON, JSONObject outputJSONObject, JSONObject inputJSONObject, String fields, String contentField){
        Map<String, String> fieldsMap = DataUtil.strToMap(fields, ":");
        DataUtil.explainKeys(totalListJSON, outputJSONObject, inputJSONObject, fieldsMap, contentField);
        log.debug("执行结果：\n" + JSON.toJSONString(outputJSONObject));
    }

    /***
     * JSONArray 取指定数量的元素
     *
     * @param outputJSONArray
     * @param inputJSONArray
     * @param limit
     * @return
     */
    public static int jsonArrayLimit(JSONArray outputJSONArray, JSONArray inputJSONArray, int limit) {
        if(limit==0) return 0;
        if(CollectionUtil.isEmpty(inputJSONArray)) return 0;
        int size = inputJSONArray.size();
        if(limit > size) {
            outputJSONArray.addAll(inputJSONArray);
            return size;
        }
        for(int i=0;i<limit;i++){
            outputJSONArray.add(inputJSONArray.get(i));
        }
        return limit;
    }

    /***
     * 根据连接符 进行取值（默认连接符为 = ）
     * @param outMap
     * @param inputJSONObject
     * @param condition
     */
    public static void explainJsonToMap(Map<String, Object> outMap, JSONObject inputJSONObject, String condition) {
        String connectorRegex = "=";
        if(condition.contains(connectorRegex)){
            String newKey = condition.substring(0, condition.indexOf(connectorRegex));
            String oldKey = condition.substring(condition.indexOf(connectorRegex)+1);
            outMap.put(newKey, inputJSONObject.get(oldKey));
        }
    }

    /***
     * 根据连接符 进行取值
     * @param outMap
     * @param inputJSONObject
     * @param condition
     * @param connectorRegex
     */
    public static void explainJsonToMap(Map<String, Object> outMap, JSONObject inputJSONObject, String condition, String connectorRegex) {
        if(condition.contains(connectorRegex)){
            String newKey = condition.substring(0, condition.indexOf(connectorRegex));
            String oldKey = condition.substring(condition.indexOf(connectorRegex)+1);
            outMap.put(newKey, inputJSONObject.get(oldKey));
        }
    }

    /**
     * 根据 聚合类型 执行聚合操作
     *
     * @param oneJson
     * @param jsonList          同组对象集
     * @param fields            需求聚合字段集
     * @param mergeStyleEnum    聚合类型 枚举
     * @param configInfo
     */
    public static void mergeByStyle(JSONObject oneJson, List<JSONObject> jsonList, String[] fields, PublicConstants.MergeStyleEnum mergeStyleEnum, String configInfo) {
        if(CollectionUtil.isEmpty(oneJson)) DataUtil.recordLogAndThrow(configInfo + "聚合时，取分组后第一个对象，为空，请核查！");
        if(org.bouncycastle.util.Arrays.isNullOrEmpty(fields)) return;
        switch (mergeStyleEnum){
            case AVG:
                JSONObject avg = DataUtil.avg(jsonList, fields, configInfo);
                oneJson.putAll(avg);
                break;
            case MAX:
                JSONObject max = DataUtil.max(jsonList, fields, configInfo);
                oneJson.putAll(max);
                break;
            case MIN:
                JSONObject min = DataUtil.min(jsonList, fields, configInfo);
                oneJson.putAll(min);
                break;
            case SUM:
                JSONObject sum = DataUtil.sum(jsonList, fields, configInfo);
                oneJson.putAll(sum);
                break;
        }
    }

    /**
     * 平均数
     *
     * @param jsonList
     * @param avgFields     需求平均值字段集
     * @param configInfo
     * @return
     */
    public static JSONObject avg(List<JSONObject> jsonList, String[] avgFields, String configInfo){
//        JSONObject oneJson = jsonList.get(0);
        JSONObject oneJson = new JSONObject();
        if(avgFields.length==1){
            String field = avgFields[0];
            BigDecimal sum = jsonList.stream().map(e -> NumberUtil.toBigDecimal((String) e.get(field))).reduce(BigDecimal.ZERO, BigDecimal::add);
            oneJson.put(field, NumberUtil.div(sum, jsonList.size()).setScale(2, RoundingMode.DOWN));                    // TODO: 精度配置

            return oneJson;
        }

        Map<String, BigDecimal> fieldMap = new HashMap<>();
        for(JSONObject json : jsonList){
            for (String field: avgFields) {
                fieldMap.put(field, NumberUtil.add(fieldMap.getOrDefault(field, BigDecimal.ZERO), NumberUtil.toBigDecimal((String) json.get(field))));
            }
        }
        for(Map.Entry<String, BigDecimal> entry : fieldMap.entrySet()){
            oneJson.put(entry.getKey(), entry.getValue().divide(new BigDecimal(jsonList.size()), 2, RoundingMode.DOWN));    // TODO: 精度配置
        }

        return oneJson;
    }

    /**
     * 最小值
     *
     * @param jsonList
     * @param minFields     需求最小值字段集
     * @param configInfo
     * @return
     */
    public static JSONObject min(List<JSONObject> jsonList, String[] minFields, String configInfo){
//        JSONObject oneJson = jsonList.get(0);
        JSONObject oneJson = new JSONObject();
        if(minFields.length==1){
            String field = minFields[0];
            JSONObject jsonObject = jsonList.stream().collect(Collectors.minBy((s1, s2) -> NumberUtil.sub(NumberUtil.toBigDecimal((String) s1.get(field)), NumberUtil.toBigDecimal((String) s2.get(field))).intValue())).get();
            oneJson.put(field, jsonObject.get(field));
            return oneJson;
        }

//        JSONObject oneJson = jsonList.get(0);
        Map<String, BigDecimal> fieldMap = new HashMap<>();
        for(JSONObject json : jsonList){
            for (String field: minFields) {
                int flag = NumberUtil.compare(fieldMap.getOrDefault(field, BigDecimal.ZERO).doubleValue(), NumberUtil.toBigDecimal((String) json.get(field)).doubleValue());
                if(flag>0){ // a > b,取 b进行覆盖；否则，保留原小数
                    fieldMap.put(field, NumberUtil.toBigDecimal((String) json.get(field)));
                }
            }
        }
        for(Map.Entry<String, BigDecimal> entry : fieldMap.entrySet()){
            oneJson.put(entry.getKey(), entry.getValue());
        }

        return oneJson;
    }

    /**
     * 最大值
     *
     * @param jsonList
     * @param maxFields     需求最大值字段集
     * @param configInfo
     * @return
     */
    public static JSONObject max(List<JSONObject> jsonList, String[] maxFields, String configInfo){
//        JSONObject oneJson = jsonList.get(0);
        JSONObject oneJson = new JSONObject();
        if(maxFields.length==1){
            String field = maxFields[0];
            JSONObject jsonObject = jsonList.stream().collect(Collectors.minBy((s1, s2) -> NumberUtil.sub(NumberUtil.toBigDecimal((String) s1.get(field)), NumberUtil.toBigDecimal((String) s2.get(field))).intValue())).get();
            oneJson.put(field, jsonObject.get(field));
            return oneJson;
        }

//        JSONObject oneJson = jsonList.get(0);
        Map<String, BigDecimal> fieldMap = new HashMap<>();
        for(JSONObject json : jsonList){
            for (String field: maxFields) {
                int flag = NumberUtil.compare(fieldMap.getOrDefault(field, BigDecimal.ZERO).doubleValue(), NumberUtil.toBigDecimal((String) json.get(field)).doubleValue());
                if(flag<0){// a < b,取 b进行覆盖；否则，保留原大数
                    fieldMap.put(field, NumberUtil.toBigDecimal((String) json.get(field)));
                }

            }
        }
        for(Map.Entry<String, BigDecimal> entry : fieldMap.entrySet()){
            oneJson.put(entry.getKey(), entry.getValue());
        }

        return oneJson;
    }

    /**
     * 累加值
     *
     * @param jsonList
     * @param sumFields     需求累加值字段集
     * @param configInfo
     * @return
     */
    public static JSONObject sum(List<JSONObject> jsonList, String[] sumFields, String configInfo){
//        JSONObject oneJson = jsonList.get(0);
        JSONObject oneJson = new JSONObject();
        if(sumFields.length==1){
            String field = sumFields[0];
            BigDecimal sum = jsonList.stream().map(e -> NumberUtil.toBigDecimal((String) e.get(field))).reduce(BigDecimal.ZERO, BigDecimal::add);
            oneJson.put(field, sum);

            return oneJson;
        }

        Map<String, BigDecimal> fieldMap = new HashMap<>();
        for(JSONObject json : jsonList){
            for (String field: sumFields) {
                fieldMap.put(field, NumberUtil.add(fieldMap.getOrDefault(field, BigDecimal.ZERO), NumberUtil.toBigDecimal((String) json.get(field))));
            }
        }
        for(Map.Entry<String, BigDecimal> entry : fieldMap.entrySet()){
            oneJson.put(entry.getKey(), entry.getValue());
        }

        return oneJson;
    }


    public static boolean isNumberObject(JSONObject obj, String[] fields) {
        for(String field : fields){
            Object val = obj.get(field);
            if(!(val instanceof Number) && !NumberUtils.isCreatable((String) val)){
                return false;
            }
        }
        return true;
    }

}
