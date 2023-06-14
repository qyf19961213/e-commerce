package com.muke.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h1>消息传递对象: SpringCloud Stream + Kafka/RocketMQ</h1>
 * */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QinyiMessage {

    private Integer id;
    private String projectName;
    private String org;
    private String author;
    private String version;

    /**
     * <h2>返回一个默认的消息, 方便使用</h2>
     * */
    public static QinyiMessage defaultMessage() {

        return new QinyiMessage(
                1,
                "e-commerce-stream-client",
                "imooc.com",
                "Qinyi",
                "1.0"
        );
    }
}
