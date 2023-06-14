package com.muke.stream.qinyi;

import com.alibaba.fastjson.JSON;
import com.muke.vo.QinyiMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;

/**
 * <h1>使用自定义的输入信道实现消息的接收</h1>
 * */
@Slf4j
@EnableBinding(QinyiSink.class)
public class QinyiReceiveService {

    /** 使用自定义的输入信道接收消息 */
    @StreamListener(QinyiSink.INPUT)
    public void receiveMessage(@Payload Object payload) {

        log.info("in QinyiReceiveService consume message start");
        QinyiMessage qinyiMessage = JSON.parseObject(payload.toString(), QinyiMessage.class);
        log.info("in QinyiReceiveService consume message success: [{}]",
                JSON.toJSONString(qinyiMessage));
    }
}
