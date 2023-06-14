package com.muke.stream.qinyi;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

/**
 * 自定义输出信道
 * @Author：qyf
 * @Date：2023/4/23 0023  16:36
 */
public interface QinyiSource {

    String OUTPUT = "qinyiOutput";

    /**
     * 输出信道的名称是 qinyiOutput, 需要使用 Stream 绑定器在 yml 文件中声明
     * @return
     */
    @Output(QinyiSource.OUTPUT)
    MessageChannel qinyiOutput();
}
