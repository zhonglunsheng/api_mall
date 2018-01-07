package com.mmall.dao;

import com.mmall.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    int deleteByUserIdShippingId(@Param(value = "userId") Integer userId, @Param(value = "shippingIdList") List<String> shippingIdList);

    List<Shipping> selectByUserId(Integer userId);

    Shipping selectByUserIdShippingId(@Param(value = "userId") Integer userId, @Param(value = "shippingId") Integer shippingId);
}