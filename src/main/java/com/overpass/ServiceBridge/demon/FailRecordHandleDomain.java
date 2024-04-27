//package com.overpass.ServiceBridge.demon;
//
//import cn.hutool.core.collection.CollectionUtil;
//import cn.hutool.core.date.DateUtil;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.overpass.ServiceBridge.constans.PublicConstants;
//import com.overpass.ServiceBridge.mapper.FailRecordDOMapper;
//import com.overpass.ServiceBridge.utils.DataUtil;
//import com.overpass.ServiceBridge.utils.Result;
//import com.overpass.ServiceBridge.utils.ResultCodeEnum;
//import lombok.extern.log4j.Log4j2;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Objects;
//
///**
// * @program: settlement
// * @description: POP-货主-供应商对账单相关操作domain
// * @author: wqb
// * @create: 2021-8-12 10:03:29
// */
//@Component
//@Log4j2
//public class FailRecordHandleDomain {
//    @Autowired
//    private ConfigCostDomain configCostDomain;
//    @Autowired
//    private FailRecordDOMapper failRecordDOMapper;
//
//    /**
//     * @Description 费用任务失败记录-列表查询
//     * @param feeTaskFailRecordVO
//     * @Return Result<PageList<PopReconciliationBO>>
//     * @Author wqb
//     * @Date 2022-2-10 10:46:12
//     **/
//    public Result<List<FeeTaskFailRecordBO>> list(FeeTaskFailRecordVO feeTaskFailRecordVO) {
//        FeeTaskFailRecordQuery feeTaskFailRecordQuery = new FeeTaskFailRecordQuery();
//        this.paramHandle(feeTaskFailRecordVO);
//        DataUtil.copyToResult(feeTaskFailRecordVO, feeTaskFailRecordQuery);
//
//        List<FeeTaskFailRecordDO> feeTaskFailRecordDOS = failRecordDOMapper.selectList(feeTaskFailRecordQuery);
//        if(CollectionUtil.isEmpty(feeTaskFailRecordDOS)) return new Result<>(ResultCodeEnum.NO_DATA, null);
//        List<FeeTaskFailRecordBO> feeTaskFailRecordBOList = new ArrayList(feeTaskFailRecordDOS.size());
//        DataUtil.copyListToResult(feeTaskFailRecordDOS, feeTaskFailRecordBOList, FeeTaskFailRecordBO.class);
//        feeTaskFailRecordBOList.forEach(record->record.setHandledFlagName(PublicConstants.HandledFlagEnum.getName(record.getHandledFlag())));
//        return new Result(ResultCodeEnum.OK, feeTaskFailRecordDOS);
//    }
//
//
//    private void paramHandle(FeeTaskFailRecordVO feeTaskFailRecordVO){
//        // 公司
//        String companyInfoIdsStr = feeTaskFailRecordVO.getCompanyInfoIdStr();
//        if(org.apache.commons.lang.StringUtils.isNotBlank(companyInfoIdsStr)) {
//            String[] companyInfoIdArray = companyInfoIdsStr.split(",");
//            if(companyInfoIdArray != null && companyInfoIdArray.length > 0){
//                List<Integer> companyInfoIdList = new ArrayList<>();
//                for(String companyInfoIdStr:companyInfoIdArray){
//                    if(org.apache.commons.lang.StringUtils.isNotBlank(companyInfoIdStr)) companyInfoIdList.add(Integer.valueOf(companyInfoIdStr));
//                }
//                if(CollectionUtil.isNotEmpty(companyInfoIdList)) feeTaskFailRecordVO.setCompanyInfoIdList(companyInfoIdList);
//            }
//        }
//        // 货主
//        String shopIdsStr = feeTaskFailRecordVO.getShopIdStr();
//        if(org.apache.commons.lang.StringUtils.isNotBlank(shopIdsStr)) {
//            String[] shopIdArray = shopIdsStr.split(",");
//            if(shopIdArray != null && shopIdArray.length > 0){
//                List<Integer> shopIdList = new ArrayList<>();
//                for(String shopIdStr:shopIdArray){
//                    if(org.apache.commons.lang.StringUtils.isNotBlank(shopIdStr)) shopIdList.add(Integer.valueOf(shopIdStr));
//                }
//                if(CollectionUtil.isNotEmpty(shopIdList)) feeTaskFailRecordVO.setShopIdList(shopIdList);
//            }
//        }
//    }
//
//    /**
//     * @Description 自动循环处理 失败任务-异步
//     * @Return Result
//     * @Author wqb
//     * @Date 2022-2-9 16:37:55
//     **/
//    @Async
//    public void asyncAutoHandle(String paramStr) {
//        log.info("自动循环处理 失败任务");
//        FeeTaskFailRecordQuery feeTaskFailRecordQuery = new FeeTaskFailRecordQuery();
//        FeeTaskFailRecordVO feeTaskFailRecordVO = JSON.parseObject(paramStr, FeeTaskFailRecordVO.class);
//        if(Objects.nonNull(feeTaskFailRecordVO)) DataUtil.copyToResult(feeTaskFailRecordVO, feeTaskFailRecordQuery);
//        this.handleAndFailRecord(feeTaskFailRecordQuery, 0, "系统");
//    }
//
//    /**
//     * @Description 手动处理异常记录-异步
//     * @param feeTaskFailRecordVO
//     * @Return cn.huimin100.erp.commons.model.Result
//     * @Author wqb
//     * @Date 2022-2-9 16:32:30
//     **/
//    @Async
//    public void asyncOpt(FeeTaskFailRecordVO feeTaskFailRecordVO, ErpUser erpUser) {
//        FeeTaskFailRecordQuery feeTaskFailRecordQuery = new FeeTaskFailRecordQuery();
//        DataUtil.copyToResult(feeTaskFailRecordVO, feeTaskFailRecordQuery);
//        this.handleAndFailRecord(feeTaskFailRecordQuery, erpUser.getUserId(), erpUser.getUserName());
//    }
//
//    // 获取异常记录 并 处理
//    private boolean handleAndFailRecord(FeeTaskFailRecordQuery feeTaskFailRecordQuery, Integer userId, String userName) {
//        if(Objects.isNull(feeTaskFailRecordQuery.getHandledFlag())) feeTaskFailRecordQuery.setHandledFlag(PublicConstants.HandledFlagEnum.NO.getCode());
//        List<FeeTaskFailRecordDO> feeTaskFailRecordDOS = failRecordDOMapper.selectList(feeTaskFailRecordQuery);
//        if (CollectionUtil.isEmpty(feeTaskFailRecordDOS)) return true;
//        for (FeeTaskFailRecordDO record : feeTaskFailRecordDOS) {
//            log.info("重试异常记录。configInfo：{}，requestParams:{}，groupTypeField：{}; TaskBussEndTime：{}; ", record.getConfigInfo(), record.getRequestParams(), record.getGroupTypeField(), DateUtil.formatDateTime(record.getTaskBussStartTime()), DateUtil.formatDateTime(record.getTaskBussEndTime()));
//            String requestParams = record.getRequestParams();
//            if (StringUtils.isBlank(requestParams)) {
//                log.warn("异常记录，请求参数 为空，请核查异常记录！记录：{}", JSON.toJSONString(record));
//                continue;
//            }
//            JSONObject paramJson = (JSONObject) JSON.parse(requestParams);
//            paramJson.put("feeTypes", Arrays.asList(record.getFeeType())); // 重试，每次只处理 异常类型
//            // 调取 创建费用单据 重试接口
//            configCostDomain.createFee(paramJson.toJSONString()); // 默认都是处理成功了，不成功，会在生成流程中，触发生成新的失败记录。
//            record.setUpdateTime(null);
//            record.setHandledFlag(PublicConstants.HandledFlagEnum.FINISH.getCode());
//            record.setUpdaterId(userId);
//            record.setUpdaterName(userName);
//            record.setHandledTimes(record.getHandledTimes() + 1);
//            failRecordDOMapper.updateByPrimaryKeySelective(record);
//        }
//        return false;
//    }
//
//
//}
