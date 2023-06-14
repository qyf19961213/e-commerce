package com.muke.config;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 自定义异步任务线程池，异步任务异常捕获处理器
 * @Author：qyf
 * @Date：2023/4/14 0014  16:18
 */
@Slf4j
@EnableAsync
@Configuration
public class AsyncPoolConfig implements AsyncConfigurer {

    /**
     * 将自定义的线程池注入到 spring 容器中
     */
    @Bean
    @Override
    public Executor getAsyncExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(20);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("Qinyi-Async-");

        //等待所有任务结果后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        // 定义拒绝策略
        executor.setRejectedExecutionHandler(
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        // 初始化线程池，初始化 core 线程
        return executor;
    }

    /**
     * 指定系统中的异步任务在出现异常时使用到的处理器
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return null;
    }

    /**
     * 异常任务异常捕获处理器
     */
    @SuppressWarnings("all")
    class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

        @Override
        public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {

            throwable.printStackTrace();
            log.error("Async Error: [{}], Method: [{}], Param: [{}]",
                    throwable.getMessage(), method.getName(),
                    JSON.toJSONString(objects));
            // TODO 发送邮件或是短信，做进一步的报价处理
        }
    }
}
