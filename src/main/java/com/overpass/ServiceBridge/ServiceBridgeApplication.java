package com.overpass.ServiceBridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

@EnableRetry
@EnableAsync
@EnableDiscoveryClient
@SpringBootApplication
public class ServiceBridgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceBridgeApplication.class, args);
	}

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }


}
