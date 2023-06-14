package com.muke.filter.factory;

import com.muke.filter.HeaderTokenGatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class HeaderTokenGatewayFilterFactory
        extends AbstractGatewayFilterFactory<Object> {

    @Override
    public GatewayFilter apply(Object config) {
        return new HeaderTokenGatewayFilter();
    }
}
