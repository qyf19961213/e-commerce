package com.muke;

import com.muke.conf.DataSourceProxyAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * <h1>订单微服务启动入口</h1>
 * */
@EnableJpaAuditing
@SpringBootApplication
@EnableCircuitBreaker
@EnableFeignClients
@EnableDiscoveryClient
@Import(DataSourceProxyAutoConfiguration.class)
public class OrderApplication {

    public static void main(String[] args) {

        SpringApplication.run(OrderApplication.class, args);
    }
}
