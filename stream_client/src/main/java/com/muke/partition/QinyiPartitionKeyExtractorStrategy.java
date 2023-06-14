package com.muke.partition;

import com.alibaba.fastjson.JSON;
import com.muke.vo.QinyiMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.binder.PartitionKeyExtractorStrategy;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * <h1>自定义从 Message 中提取 partition key 的策略</h1>
 * */
@Slf4j
@Component
public class QinyiPartitionKeyExtractorStrategy implements PartitionKeyExtractorStrategy {

    @Override
    public Object extractKey(Message<?> message) {

        QinyiMessage qinyiMessage = JSON.parseObject(
                message.getPayload().toString(), QinyiMessage.class
        );
        // 自定义提取 key
        String key = qinyiMessage.getProjectName();
        log.info("SpringCloud Stream Qinyi Partition Key: [{}]", key);
        return key;
    }
}
