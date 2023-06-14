package com.muke;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * <h1>基于 SpringCloud Stream 构建消息驱动微服务</h1>
 * */
@EnableDiscoveryClient
@SpringBootApplication
public class ConsumerApplication {

    public static void main(String[] args) {

        SpringApplication.run(ConsumerApplication.class, args);
    }
}
