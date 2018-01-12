package com.mmall.service;

import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Order;
import com.mmall.vo.OrderVo;

import java.util.List;
import java.util.Map;

/**
 * Created by zhonglunsheng on 2018/1/10.
 */
public interface IOrderService {
    ServiceResponse createOrder(Integer userId, Integer shippingId);

    ServiceResponse cancelOrder(Long orderNo);

    ServiceResponse getOrderProduct(Integer userId, Long orderNo);

    ServiceResponse getOrderDetail(Integer userId, Long orderNo);

    ServiceResponse getOrderList(Integer userId, int pageNum, int pageSize);

    ServiceResponse managerSearchOrder(Long orderNo, int pageNum, int pageSize);

    ServiceResponse managerSendGoods(Long orderNo);

    ServiceResponse pay(Long orderNo, Integer userId, String path);

    ServiceResponse aliCallback(Map<String, String> params);

    ServiceResponse aliRefund(Integer userId, Long orderNo);

    ServiceResponse queryOrderPayStatus(Integer userId, Long orderNo);
}
