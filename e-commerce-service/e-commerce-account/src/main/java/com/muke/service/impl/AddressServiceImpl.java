package com.muke.service.impl;

import com.alibaba.fastjson.JSON;
import com.muke.account.AddressInfo;
import com.muke.common.TableId;
import com.muke.dao.EcommerceAddressDao;
import com.muke.entity.EcommerceAddress;
import com.muke.filter.AccessContext;
import com.muke.service.IAddressService;
import com.muke.vo.LoginUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1>用户地址相关服务接口实现</h1>
 * */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AddressServiceImpl implements IAddressService {

    private final EcommerceAddressDao addressDao;

    public AddressServiceImpl(EcommerceAddressDao addressDao) {
        this.addressDao = addressDao;
    }


    @Override
    public TableId createAddressInfo(AddressInfo addressInfo) {

        LoginUserInfo loginUserInfo = AccessContext.getLoginUserInfo();

        //将传递的对象转换成实体对象（需校验）
        List<EcommerceAddress> ecommerceAddresses = addressInfo.getAddressItems().stream()
                .map(a -> EcommerceAddress.to(loginUserInfo.getId(),a))
                .collect(Collectors.toList());

        //保存到数据表并把返回记录的id给调用方
        List<EcommerceAddress> saveRecords = addressDao.saveAll(ecommerceAddresses);
        List<Long> ids = saveRecords.stream()
                .map(EcommerceAddress::getId).collect(Collectors.toList());
        log.info("create address info: [{}], [{}]",loginUserInfo.getId(),
                JSON.toJSON(ids));
        return new TableId(
                ids.stream().map(TableId.Id::new).collect(Collectors.toList())
        );
    }

    @Override
    public AddressInfo getCurrentAddressInfo() {

        LoginUserInfo loginUserInfo = AccessContext.getLoginUserInfo();

        //根据 userId 查询到的用户的地址信息，再实现转换
        List<EcommerceAddress> ecommerceAddresses = addressDao.findAllByUserId(
          loginUserInfo.getId()
        );
        List<AddressInfo.AddressItem> addressItems = ecommerceAddresses.stream()
            .map(EcommerceAddress::toAddressItem)
            .collect(Collectors.toList());

        return new AddressInfo(loginUserInfo.getId(),addressItems);
    }

    @Override
    public AddressInfo getAddressInfoById(Long id) {
        EcommerceAddress ecommerceAddress = addressDao.findById(id).orElse(null);
        if(null == ecommerceAddress){
            throw new RuntimeException("address is not exist");
        }

        return new AddressInfo(
                ecommerceAddress.getUserId(),
                Collections.singletonList(ecommerceAddress.toAddressItem())
        );
    }

    @Override
    public AddressInfo getAddressInfoByTableId(TableId tableId) {

        List<Long> ids = tableId.getIds().stream()
                .map(TableId.Id::getId).collect(Collectors.toList());

        List<EcommerceAddress> ecommerceAddresses = addressDao.findAllById(ids);
        if(CollectionUtils.isEmpty(ecommerceAddresses)){
            return new AddressInfo(-1L, Collections.emptyList());
        }

        List<AddressInfo.AddressItem> addressItems = ecommerceAddresses.stream()
                .map(EcommerceAddress::toAddressItem)
                .collect(Collectors.toList());
        AddressInfo addressInfo = new AddressInfo(
                ecommerceAddresses.get(0).getUserId(), addressItems
        );
        return addressInfo;
    }
}
