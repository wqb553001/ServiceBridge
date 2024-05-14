package com.overpass.ServiceBridge.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.overpass.ServiceBridge.utils.Result;
import com.overpass.ServiceBridge.vo.InterfaceConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

@Tag(name = "服务中心", description = "服务桥梁")
@Log4j2
@RestController
@RequestMapping("overpass/service-bridge")
public class ServiceBridgeController {


    @ResponseBody
    @PostMapping("/manage")
    @Operation(hidden = true)
    public Result manage(@RequestBody InterfaceConfig interfaceConfig){
        log.info("访问 获取 推荐数据接口 manage（）参数为：{}", interfaceConfig.getBaseFormData());
        JSONObject parse = (JSONObject)JSONObject.parse(interfaceConfig.getBaseFormData());
        if(CollectionUtil.isNotEmpty(parse))
            parse.entrySet().forEach(e->{
                log.info("entry:{}", e);
            });
        log.info("参数：{}", parse);
        return new Result();
    }
}
