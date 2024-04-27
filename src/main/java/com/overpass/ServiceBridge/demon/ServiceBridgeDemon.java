package com.overpass.ServiceBridge.demon;

import com.alibaba.fastjson.JSON;
import com.overpass.ServiceBridge.ho.ConfigHO;
import org.springframework.stereotype.Component;

@Component
public class ServiceBridgeDemon {

    public void handle(String paramStr){
        ConfigHO configHO = JSON.parseObject(paramStr, ConfigHO.class);

    }
}
