package com.muke.feign.hystrix;

import com.alibaba.fastjson.JSON;
import com.muke.account.AddressInfo;
import com.muke.common.TableId;
import com.muke.feign.AddressClient;
import com.muke.vo.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Collections;

/**
 * <h1>账户服务熔断降级兜底策略</h1>
 * */
@Slf4j
@Component
public class AddressClientHystrix implements AddressClient {

    @RequestMapping(value = "/ecommerce-account-service/address/address-info-by-table-id", method = RequestMethod.POST)
    @Override
    public CommonResponse<AddressInfo> getAddressInfoByTablesId(TableId tableId) {

        log.error("[account client feign request error in order service] get address info" +
                "error: [{}]", JSON.toJSONString(tableId));
        return new CommonResponse<>(
                -1,
                "[account client feign request error in order service]",
                new AddressInfo(-1L, Collections.emptyList())
        );
    }
}
