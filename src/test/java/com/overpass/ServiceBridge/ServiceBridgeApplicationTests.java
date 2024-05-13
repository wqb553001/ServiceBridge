package com.overpass.ServiceBridge;

import com.overpass.ServiceBridge.ho.ConfigHO;
import com.overpass.ServiceBridge.query.ConfigInterfaceQuery;
import com.overpass.ServiceBridge.remote.RemoteClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class ServiceBridgeApplicationTests {
//    @Autowired
//    ServiceBridgeDemon serviceBridgeDemon;
    @Autowired
    RemoteClient remoteClient;

	@Test
	void contextLoads() {
        ConfigHO configHO = ConfigHO.builder().statusValue("200").build();
        String httpUrlStr = "http://localhost:8088/nexneo/api/data";
        Map<String, Object> requestBody = new HashMap<>();
        String method = "POST";
        ConfigInterfaceQuery configInterfaceQuery = ConfigInterfaceQuery.builder()
                .httpUrlStr(httpUrlStr)
                .method(method)
                .respDataFields("content:content.list")
                .build();
        String configInfo = "测试";
        remoteClient.onceQuery(null, configInterfaceQuery, configInfo);
	}

}
