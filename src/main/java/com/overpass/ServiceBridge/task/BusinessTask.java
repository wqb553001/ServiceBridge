package com.overpass.ServiceBridge.task;


import com.overpass.ServiceBridge.demon.ServiceBridgeDemon;
import com.overpass.ServiceBridge.utils.DataUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description 基础业务数据自动拉取
 * @Author wqb
 * @CreateTime 2024-4-17 10:58:45
 * @Version 1.0
 **/
@Log4j2
@Component
public class BusinessTask extends IJobHandler {

    @Autowired
    private ServiceBridgeDemon serviceBridgeDemon;


    /**
     * 【基础业务数据】拉取
     *  参数示例 ： {"bussDate":"2021-09-23", "branchId":3, "outStoreIds":[1,2,3], "finishStartTime":"2021-09-28 00:00:00","finishEndTime":"2021-09-28 23:59:59", "cloudWarehouseFlag":0, "checkFlag":0}
     * @return
     * @throws Exception
     */
    @Override
    @XxlJob("serviceBridge")
    public void execute() throws Exception {
        String paramStr = XxlJobHelper.getJobParam();           // 获取参数
        log.info("【基础业务数据】自动拉取开始,调度入参：{}", paramStr);
        try {
            // 获取数据
            this.serviceBridgeDemon.handle(paramStr);
            log.info("执行完成");
            XxlJobHelper.handleSuccess();                       // 设置任务结果
        }catch (Exception e){
            XxlJobHelper.log("【基础业务数据】自动拉取：异常，请核查！参数：{}", paramStr);
            log.info("【基础业务数据】自动拉取开始,任务异常。异常，请核查！参数：{}", paramStr);
            XxlJobHelper.log("【基础业务数据】自动拉取,任务异常：" + e);
            log.info(DataUtil.getStackTraceInfo(e));
        }
        log.info("【基础业务数据】自动拉取结束...");
    }


    /**
     * 分摊调度
     *  参数示例 ： {"bussDate":"2021-09-23", "branchId":3, "outStoreIds":[1,2,3], "finishStartTime":"2021-09-28 00:00:00","finishEndTime":"2021-09-28 23:59:59", "cloudWarehouseFlag":0, "checkFlag":0}
     *  cloudWarehouseFlag  : 云仓服务 0非云仓 1云仓 （默认 null）
     *  ignoreCheck         ：0不忽略；1忽略检查（默认不可忽略）
     * @param paramStr
     * @return
     * @throws Exception
     */
//    @XxlJob("businessTask")
//    public ReturnT<String> execute(String paramStr) throws Exception {
//        // 分摊
//        log.info("【基础业务数据】自动拉取开始,调度入参：{}", paramStr);
//        try {
//            // 分摊
//            this.serviceBridgeDemon.getDataWithMethod(paramStr);
//        }catch (Exception e){
////            XxlJobLogger.log("【配置化生成费用单】自动分摊：异常，请核查！参数：{}", paramStr);
//            log.info("【基础业务数据】自动拉取开始,任务异常。异常，请核查！参数：{}", paramStr);
////            XxlJobLogger.log("【配置化生成费用单】自动分摊开始,任务异常：" + e);
//            log.info(DataUtil.getStackTraceInfo(e));
//            return ReturnT.FAIL;
//        }
//        log.info("【基础业务数据】自动拉取结束...");
//        return ReturnT.SUCCESS;
//    }

}