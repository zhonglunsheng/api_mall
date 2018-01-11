package com.mmall.service;

import com.mmall.common.ServiceResponse;

import java.util.Map;

/**
 * Created by zhonglunsheng on 2018/1/10.
 */
public interface IOrderService {
    ServiceResponse pay(Long orderNo, Integer userId, String path);

    ServiceResponse aliCallback(Map<String, String> params);

    ServiceResponse aliRefund(Integer userId, Long orderNo);

    ServiceResponse queryOrderPayStatus(Integer userId, Long orderNo);
}
