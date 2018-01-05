package com.mmall.service;

import com.mmall.common.ServiceResponse;
import com.mmall.vo.CartVo;

/**
 * Created by zhonglunsheng on 2018/1/4.
 */
public interface ICartService {
    ServiceResponse<CartVo> add(Integer userId, Integer count, Integer productId);

    ServiceResponse<CartVo> list(Integer userId);
}
