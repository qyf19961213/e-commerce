package com.muke.service.impl;

import com.alibaba.fastjson.JSON;
import com.muke.account.AddressInfo;
import com.muke.account.BalanceInfo;
import com.muke.common.TableId;
import com.muke.dao.EcommerceOrderDao;
import com.muke.entity.EcommerceOrder;
import com.muke.feign.AddressClient;
import com.muke.feign.NotSecuredBalanceClient;
import com.muke.feign.NotSecuredGoodsClient;
import com.muke.feign.SecuredGoodsClient;
import com.muke.filter.AccessContext;
import com.muke.goods.DeductGoodsInventory;
import com.muke.goods.SimpleGoodsInfo;
import com.muke.order.LogisticsMessage;
import com.muke.order.OrderInfo;
import com.muke.service.IOrderService;
import com.muke.source.LogisticsSource;
import com.muke.vo.PageSimpleOrderDetail;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * <h1>订单相关服务接口实现</h1>
 */
@Slf4j
@Service
@EnableBinding(LogisticsSource.class)
public class OrderServiceImpl implements IOrderService {

    /**
     * 表的 dao 接口
     */
    @Autowired
    private EcommerceOrderDao orderDao;

    /**
     * Feign 客户端
     */
    @Autowired
    private AddressClient addressClient;
    @Autowired
    private SecuredGoodsClient securedGoodsClient;
    @Autowired
    private NotSecuredBalanceClient notSecuredBalanceClient;
    @Autowired
    private NotSecuredGoodsClient notSecuredGoodsClient;

    /**
     * SpringCloud Stream 的发射器
     */
    @Autowired
    private LogisticsSource logisticsSource;


    /**
     * <h2>创建订单: 这里会涉及到分布式事务</h2>
     * 创建订单会涉及到多个步骤和校验, 当不满足情况时直接抛出异常;
     * 1. 校验请求对象是否合法
     * 2. 创建订单
     * 3. 扣减商品库存
     * 4. 扣减用户余额
     * 5. 发送订单物流消息 SpringCloud Stream + Kafka
     */
    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public TableId createOrder(OrderInfo orderInfo) {

        // h获取地址信息
        AddressInfo addressInfo = addressClient.getAddressInfoByTablesId(
                new TableId(Collections.singletonList(
                        new TableId.Id(orderInfo.getUserAddress())
                ))).getData();
        // 1. 校验请求对象是否合法（商品信息不需要校验，扣减库存会做校验）
        if (CollectionUtils.isEmpty(addressInfo.getAddressItems())) {
            throw new RuntimeException("user address is not exist: "
                    + orderInfo.getUserAddress());
        }
        EcommerceOrder newOrder = orderDao.save(
                new EcommerceOrder(
                        AccessContext.getLoginUserInfo().getId(),
                        orderInfo.getUserAddress(),
                        JSON.toJSONString(orderInfo.getOrderItems())
                )
        );
        log.info("create order success: [{}], [{}]",
                AccessContext.getLoginUserInfo().getId(), newOrder.getId());

        // 3. 扣减商品库存
        if (
                !notSecuredGoodsClient.deductGoodsInventory(
                        orderInfo.getOrderItems()
                                .stream()
                                .map(OrderInfo.OrderItem::toDeductGoodsInventory)
                                .collect(Collectors.toList())
                ).getData()
        ) {
            throw new RuntimeException("deduct goods inventory failure");
        }

        // 4. 扣减用户账户余额
        // 4.1 获取商品信息， 计算总价格
        List<SimpleGoodsInfo> goodsInfos = notSecuredGoodsClient.getSimpleGoodsInfoByTableId(
                new TableId(
                        orderInfo.getOrderItems()
                                .stream()
                                .map(a -> new TableId.Id(a.getGoodsId()))
                                .collect(Collectors.toList())
                )
        ).getData();
        Map<Long, SimpleGoodsInfo> goodsIdGoodsInfo = goodsInfos.stream()
                .collect(Collectors.toMap(SimpleGoodsInfo::getId, Function.identity()));
        long balance = 0;
        for (OrderInfo.OrderItem orderItem : orderInfo.getOrderItems()
        ) {
            balance += goodsIdGoodsInfo.get(orderItem.getGoodsId()).getPrice()
                    * orderItem.getCount();
        }
        assert balance > 0;

        // 4.2 填写总价格，扣减账户余额
        BalanceInfo balanceInfo = notSecuredBalanceClient.deductBalance(
                new BalanceInfo(AccessContext.getLoginUserInfo().getId(), balance)
        ).getData();
        if (null == balanceInfo) {
            throw new RuntimeException("deduct user balance failure");
        }
        log.info("deduct user balance: [{}], [{}]", newOrder.getId(),
                JSON.toJSONString(balanceInfo));

        // 5. 发送订单物流消息 SpringCloud Stream + Kafka
        LogisticsMessage logisticsMessage = new LogisticsMessage(
                AccessContext.getLoginUserInfo().getId(),
                newOrder.getId(),
                orderInfo.getUserAddress(),
                //没有备注信息
                null
        );
        if (!logisticsSource.logisticsOutput().send(
                MessageBuilder.withPayload(JSON.toJSONString(logisticsMessage)).build()
        )) {
            throw new RuntimeException("send logistics message failure");
        }
        log.info("send create order message to kafka with stream: [{}]",
                JSON.toJSONString(logisticsMessage));

        // 返回订单 id
        return new TableId(Collections.singletonList(new TableId.Id(newOrder.getId())));
    }

    @Override
    public PageSimpleOrderDetail getSimpleOrderDetailByPage(int page) {
        if (page <= 1) {
            //默认第一页
            page = 1;
        }

        // 这里分页的规则是：1页10条数据，按照 id 倒序排列
        Pageable pageable = PageRequest.of(page - 1, 10,
                Sort.by("id").descending());
        Page<EcommerceOrder> orderPage = orderDao.findAllByUserId(
                AccessContext.getLoginUserInfo().getId(), pageable
        );
        List<EcommerceOrder> orders = orderPage.getContent();

        // 如果是空，直接返回空数组
        if (CollectionUtils.isEmpty(orders)) {
            return new PageSimpleOrderDetail(Collections.emptyList(), false);
        }
        // 获取当前订单中所有的 goodsId，这个 set 不可能为空或者是 null，否则，代码一定有 bug
        Set<Long> goodsIdsInOrders = new HashSet<>();
        orders.forEach(o -> {
            List<DeductGoodsInventory> goodsAndCount = JSON.parseArray(
                    o.getOrderDetail(), DeductGoodsInventory.class
            );
            goodsIdsInOrders.addAll(goodsAndCount.stream()
                    .map(DeductGoodsInventory::getGoodsId)
                    .collect(Collectors.toList()));
        });

        assert CollectionUtils.isNotEmpty(goodsIdsInOrders);

        // 是否还有更多页：总页数是否大于当前给定的页
        boolean hasMore = orderPage.getTotalPages() > page;

        // 获取商品信息
        List<SimpleGoodsInfo> goodsInfos = securedGoodsClient.getSimpleGoodsInfoByTableId(
                new TableId(goodsIdsInOrders.stream()
                        .map(TableId.Id::new).collect(Collectors.toList()))
        ).getData();

        // 获取地址信息
        AddressInfo addressInfo = addressClient.getAddressInfoByTablesId(
                new TableId(orders.stream()
                .map(o->new TableId.Id(o.getAddressId()))
                .distinct().collect(Collectors.toList()))
        ).getData();

        // 组装订单中的商品，地址信息 -> 订单信息
        return new PageSimpleOrderDetail(
                assembleSimpleOrderDetail(orders, goodsInfos, addressInfo),
                hasMore
        );
    }

    /**
     * <h2>组装订单详情</h2>
     * */
    private List<PageSimpleOrderDetail.SingleOrderItem> assembleSimpleOrderDetail(
            List<EcommerceOrder> orders, List<SimpleGoodsInfo> goodsInfos,
            AddressInfo addressInfo
    ) {
        // goodsId -> SimpleGoodsInfo
        Map<Long, SimpleGoodsInfo> id2GoodsInfo = goodsInfos.stream()
                .collect(Collectors.toMap(SimpleGoodsInfo::getId, Function.identity()));
        // addressId -> AddressInfo.AddressItem
        Map<Long, AddressInfo.AddressItem> id2AddressItem = addressInfo.getAddressItems()
                .stream().collect(
                        Collectors.toMap(AddressInfo.AddressItem::getId, Function.identity())
                );

        List<PageSimpleOrderDetail.SingleOrderItem> result = new ArrayList<>(orders.size());
        orders.forEach(o -> {

            PageSimpleOrderDetail.SingleOrderItem orderItem =
                    new PageSimpleOrderDetail.SingleOrderItem();
            orderItem.setId(o.getId());
            orderItem.setUserAddress(id2AddressItem.getOrDefault(o.getAddressId(),
                    new AddressInfo.AddressItem(-1L)).toUserAddress());
            orderItem.setGoodsItems(buildOrderGoodsItem(o, id2GoodsInfo));

            result.add(orderItem);
        });

        return result;
    }

    /**
     * <h2>构造订单中的商品信息</h2>
     * */
    private List<PageSimpleOrderDetail.SingleOrderGoodsItem> buildOrderGoodsItem(
            EcommerceOrder order, Map<Long, SimpleGoodsInfo> id2GoodsInfo
    ) {

        List<PageSimpleOrderDetail.SingleOrderGoodsItem> goodsItems = new ArrayList<>();
        List<DeductGoodsInventory> goodsAndCount = JSON.parseArray(
                order.getOrderDetail(), DeductGoodsInventory.class
        );

        goodsAndCount.forEach(gc -> {

            PageSimpleOrderDetail.SingleOrderGoodsItem goodsItem =
                    new PageSimpleOrderDetail.SingleOrderGoodsItem();
            goodsItem.setCount(gc.getCount());
            goodsItem.setSimpleGoodsInfo(id2GoodsInfo.getOrDefault(gc.getGoodsId(),
                    new SimpleGoodsInfo(-1L)));

            goodsItems.add(goodsItem);
        });

        return goodsItems;
    }
}
