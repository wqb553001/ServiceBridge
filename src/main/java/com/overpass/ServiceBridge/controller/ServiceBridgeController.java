package com.overpass.ServiceBridge.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.overpass.ServiceBridge.utils.Result;
import com.overpass.ServiceBridge.vo.InterfaceConfig;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Tag(name = "服务中心", description = "服务桥梁")
@Log4j2
@RestController
@RequestMapping("overpass/service-bridge")
public class ServiceBridgeController {

    private static JSONObject map = new JSONObject();
    private static String INTERFACE_FLAG = "interface-";

    @ResponseBody
    @PostMapping("/interface/add")
    public Result add(@RequestBody InterfaceConfig interfaceConfig){
        log.info("访问接口 add（）参数为：{}", interfaceConfig.getBaseFormData());
        JSONArray parse = (JSONArray)JSONArray.parse(interfaceConfig.getBaseFormData());

        if(CollectionUtil.isNotEmpty(parse)){
            parse.forEach(e -> {
                JSONObject interfaceJson = (JSONObject)e;
                log.info("interfaceJson:{}", interfaceJson);
                String key = INTERFACE_FLAG + interfaceJson.get("id").toString();
                if(map.containsKey(key)){
                    map.getJSONObject(key).putAll(interfaceJson);
                }else{
                    map.put(key, interfaceJson);
                }
            });
        }
        log.info("参数：{}", parse);
        return new Result();
    }

    @ResponseBody
    @PostMapping("/interface/update")
    public Result update(@RequestBody InterfaceConfig interfaceConfig){
        log.info("访问接口 update（）参数为：{}", interfaceConfig.getBaseFormData());
        JSONObject parse = (JSONObject)JSONObject.parse(interfaceConfig.getBaseFormData());
        map.put(INTERFACE_FLAG + parse.get("id").toString(), parse);
        return new Result();
    }


    @ResponseBody
    @GetMapping("/interface/findById")
    public Result findInterfaceById(@RequestParam String interfaceId){
        log.info("访问接口 findById（）参数为：{}", interfaceId);
        JSONObject some = map.getJSONObject(INTERFACE_FLAG + interfaceId);
        log.info("findById（）返回值：{}", some);
        return new Result(some);
    }

    @ResponseBody
    @GetMapping("/interface/deleteById")
    public Result deleteInterfaceById(@RequestParam String interfaceId){
        log.info("访问接口 deleteInterfaceById（）参数为：{}", interfaceId);
        String key = INTERFACE_FLAG + interfaceId;
        if(map.containsKey(key)) map.remove(key);
        log.info("移除：{}", key);
        return new Result();
    }

    @ResponseBody
    @GetMapping("/interface/findAll")
    public Result findAll(@RequestParam Integer status){
        log.info("访问接口 findAll（）参数为：{}", status);
        Collection<Object> values = map.values();
        log.info("findAll（）返回值：{}", values);
        return new Result(values);
    }
}
