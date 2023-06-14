package com.muke;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * <h1>Nacos Client 工程启动入口</h1>
 * */
@EnableFeignClients
@RefreshScope
@EnableDiscoveryClient
@SpringBootApplication
public class NacosClientApplication {

    public static void main(String[] args) {

        SpringApplication.run(NacosClientApplication.class, args);
    }
}
