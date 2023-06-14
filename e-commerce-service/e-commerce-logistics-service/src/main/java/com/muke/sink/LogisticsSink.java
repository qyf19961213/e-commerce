package com.muke.sink;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

/**
 * <h1>自定义物流信息接收器(Sink)</h1>
 * */
public interface LogisticsSink {

    /** 输入信道名称 */
    String INPUT = "logisticsInput";

    /**
     * <h2>物流 Sink -> logisticsInput</h2>
     * */
    @Input(LogisticsSink.INPUT)
    SubscribableChannel logisticsInput();
}
