package com.muke.service;

import com.alibaba.fastjson.JSON;
import com.muke.dao.EcommerceLogisticsDao;
import com.muke.entity.EcommerceLogistics;
import com.muke.order.LogisticsMessage;
import com.muke.sink.LogisticsSink;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;

/**
 * 物流服务实现
 * @Author：qyf
 * @Date：2023/4/26 0026  9:38
 */
@Slf4j
@EnableBinding(LogisticsSink.class)
public class LogisticsServiceImpl {

    @Autowired
    private EcommerceLogisticsDao ecommerceLogisticsDao;

    /**
     * 订阅监听订单微服务发送的物流消息
     * @param payload
     */
    @StreamListener("logisticsInput")
    public void consumeLogisticsMessage(@Payload Object payload){

        log.info("receive and consume logistics message: [{}]", payload.toString());
        LogisticsMessage logisticsMessage = JSON.parseObject(
                payload.toString(),LogisticsMessage.class
        );
        EcommerceLogistics ecommerceLogistics = ecommerceLogisticsDao.save(
                new EcommerceLogistics(
                        logisticsMessage.getUserId(),
                        logisticsMessage.getOrderId(),
                        logisticsMessage.getAddressId(),
                        logisticsMessage.getExtraInfo()
                )
        );
        log.info("consume logistics message success: [{}]",ecommerceLogistics.getId());
    }
}
