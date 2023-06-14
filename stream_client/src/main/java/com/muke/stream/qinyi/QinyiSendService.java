package com.muke.stream.qinyi;

import com.alibaba.fastjson.JSON;
import com.muke.stream.qinyi.QinyiSource;
import com.muke.vo.QinyiMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;


/**
 * 使用自定义的通信信道 QinyiSource 实现消息的发送
 * @Author：qyf
 * @Date：2023/4/23 0023  16:03
 */
@Slf4j
@EnableBinding(QinyiSource.class)
public class QinyiSendService {

    @Autowired
    private QinyiSource source;

    public void sendMessage(QinyiMessage message){

        String _message = JSON.toJSONString(message);
        log.info("in QinyiSendService send message: [{}]", _message);

        // Spring Messaging, 统一消息的编程模型，是 Stream 组件的重要组成部分之一
        source.qinyiOutput().send(MessageBuilder.withPayload(_message).build());
    }
}
