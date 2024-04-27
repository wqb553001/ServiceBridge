package com.overpass.ServiceBridge.utils;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
public class ThreadPoolUtil {

    @Value("${threadPoolCount:5}")                      // 并发处理，线程池线程数
    private Integer threadPoolCount;
    private static Integer sysThreadPoolCount;

    private  ExecutorService threadPool = null;
    @PostConstruct
    public void init(){
        // CPU密集型：核心线程数 = CPU核数 + 1
        // IO密集型：核心线程数 = CPU核数 * 2
        // 工作站的CPU一般为 十八核
        int availableProcessors = Runtime.getRuntime().availableProcessors(); //获取当前机器(服务器)的核数
        if(threadPoolCount > 2 * availableProcessors){// 设置安全阈值
            log.warn("线程数配置指导：【线程数 < CPU核数 * 2】 当前服务器核心数为：{}; 配置线程数：{} ; 已自动调整为最大值：{}", availableProcessors, threadPoolCount, 2 * availableProcessors);
            threadPoolCount = 2 * availableProcessors;
        }
        sysThreadPoolCount = threadPoolCount;
        log.info("当前服务器CPU核心数：{}; 线程数配置为：{}", availableProcessors, sysThreadPoolCount);
        threadPool = Executors.newFixedThreadPool(sysThreadPoolCount);
    }
}
