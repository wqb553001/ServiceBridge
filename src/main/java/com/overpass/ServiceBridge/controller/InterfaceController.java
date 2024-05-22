package com.overpass.ServiceBridge.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.overpass.ServiceBridge.utils.Result;
import com.overpass.ServiceBridge.vo.Config;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@Tag(name = "服务中心-接口", description = "接口管理")
@Log4j2
@RestController
@RequestMapping("overpass/service-bridge/interface")
public class InterfaceController {

    private static JSONObject map = new JSONObject();
    private static String INTERFACE_FLAG = "interface-";
    private static String FLOW_FLAG = "flow-";

    @ResponseBody
    @PostMapping("/add")
    public Result add(@RequestBody Config config){
        log.info("访问接口 add（）参数为：{}", config.getBaseFormData());
        JSONArray parse = (JSONArray)JSONArray.parse(config.getBaseFormData());

        if(CollectionUtil.isNotEmpty(parse)){
            parse.forEach(e -> {
                JSONObject interfaceJson = (JSONObject)e;
                log.info("interfaceJson:{}", interfaceJson);
                String flowKey = FLOW_FLAG + interfaceJson.get("flowId").toString();
                String interfaceKey = INTERFACE_FLAG + interfaceJson.get("id").toString();
                if(map.containsKey(flowKey)){
                    JSONObject flowJsonObject = map.getJSONObject(flowKey);
                    if(flowJsonObject.containsKey(interfaceKey)){
                        // 更新
                        flowJsonObject.getJSONObject(interfaceKey).putAll(interfaceJson);
                    }else{
                        // 新增
                        flowJsonObject.put(interfaceKey, interfaceJson);
                    }
                }else{
                    JSONObject newFlowJsonObject = new JSONObject();
                    newFlowJsonObject.put(interfaceKey, interfaceJson);
                    map.put(flowKey, newFlowJsonObject);
                }
            });
        }
        log.info("参数：{}", parse);
        return new Result();
    }

    @ResponseBody
    @PostMapping("/update")
    public Result update(@RequestBody Config config){
        log.info("访问接口 update（）参数为：{}", config.getBaseFormData());
        JSONObject interfaceJson = (JSONObject)JSONObject.parse(config.getBaseFormData());

        String flowKey = FLOW_FLAG + interfaceJson.get("flowId").toString();
        String interfaceKey = INTERFACE_FLAG + interfaceJson.get("id").toString();
        if(map.containsKey(flowKey)) {
            map.getJSONObject(flowKey).put(interfaceKey, interfaceJson);
        }else{
            JSONObject newFlowJsonObject = new JSONObject();
            newFlowJsonObject.put(interfaceKey, interfaceJson);
            map.put(flowKey, newFlowJsonObject);
        }

        return new Result();
    }


    @ResponseBody
    @GetMapping("/findByIdAndFlowId")
    public Result findByIdAndFlowId(@RequestParam String id, @RequestParam String flowId){
        log.info("访问接口 findByIdAndFlowId（）参数为：id:{}；flowId:{}", id, flowId);
        String flowKey = FLOW_FLAG + flowId;
        String interfaceKey = INTERFACE_FLAG + id;
        JSONObject some = new JSONObject();
        if(map.containsKey(flowKey)){
            JSONObject flowInterfaceJsonObject = map.getJSONObject(flowKey);
            if(flowInterfaceJsonObject.containsKey(interfaceKey)){
                some = flowInterfaceJsonObject.getJSONObject(interfaceKey);
            }
        }
        log.info("findByIdAndFlowId（）返回值：{}", some);
        return new Result(some);
    }

    @ResponseBody
    @GetMapping("/deleteByIdAndFlowId")
    public Result deleteByIdAndFlowId(@RequestParam String id, @RequestParam String flowId){
        log.info("访问接口 deleteByIdAndFlowId（）参数为：id:{}；flowId:{}", id, flowId);
        String flowKey = FLOW_FLAG + flowId;
        String interfaceKey = INTERFACE_FLAG + id;
        if(map.containsKey(flowKey)){
            JSONObject flowInterfaceJsonObject = map.getJSONObject(flowKey);
            if(flowInterfaceJsonObject.containsKey(interfaceKey)){
                flowInterfaceJsonObject.remove(interfaceKey);
            }
        }
        log.info("移除接口：id:{}，flowId:{}", id, flowId);
        return new Result();
    }

    @ResponseBody
    @GetMapping("/findByFlowId")
    public Result findByFlowId(@RequestParam String flowId){
        log.info("访问接口 findAll（）参数为：{}", flowId);
        String flowKey = FLOW_FLAG + flowId;
        Collection<Object> values = new JSONArray();
        if(map.containsKey(flowKey)){
            values = map.getJSONObject(flowKey).values();
        }
        log.info("findByFlowId（{}）返回值：{}", flowId, values);
        return new Result(values);
    }
}
