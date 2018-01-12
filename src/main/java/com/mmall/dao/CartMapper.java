package com.mmall.dao;

import com.mmall.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    Cart selectByUserIdProductId(@Param(value = "userId") Integer userId, @Param(value = "productId") Integer productId);

    List<Cart> selectByUserId(Integer userId);

    int selectByUserIdCheckStatus(Integer userId);

    int deleteByUserIdProductIds(@Param(value = "userId") Integer userId,@Param(value = "productIdList") List<String> productIdList);

    int updateByUserIdCheckedStatus(@Param(value = "userId") Integer userId,@Param(value = "productId") Integer productId,@Param(value = "checkedStatus") Integer checkedStatus);

    int getQuantityByUserId(@Param(value = "userId") Integer userId);

    List<Cart> selectCheckByUserId(Integer userId);
}