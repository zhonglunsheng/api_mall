package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Shipping;

/**
 * Created by zhonglunsheng on 2018/1/6.
 */
public interface IShippingService {
    ServiceResponse add(Integer userId, Shipping shipping);

    ServiceResponse delete(Integer userId, String shippingIds);

    ServiceResponse update(Integer userId, Shipping shipping);

    ServiceResponse getShipping(Integer userId, Integer shippingId);

    ServiceResponse<PageInfo> list(Integer userId, Integer pageNum, Integer pageSize);
}
