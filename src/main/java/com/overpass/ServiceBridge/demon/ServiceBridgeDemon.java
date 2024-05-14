package com.overpass.ServiceBridge.demon;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.overpass.ServiceBridge.ho.ConfigHO;
import com.overpass.ServiceBridge.query.ConfigInterfaceQuery;
import com.overpass.ServiceBridge.remote.RemoteClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceBridgeDemon {
    @Autowired
    private RemoteClient remoteClient;

    public void handle(String paramStr){
        ConfigHO configHO = JSON.parseObject(paramStr, ConfigHO.class);
        ConfigInterfaceQuery configInterfaceQuery = new ConfigInterfaceQuery();
        JSONArray totalListJSON = new JSONArray();
        remoteClient.pageQuery(totalListJSON, configInterfaceQuery, configInterfaceQuery.getName());
        remoteClient.onceQuery(totalListJSON, null, configInterfaceQuery, configInterfaceQuery.getName());
    }
}
