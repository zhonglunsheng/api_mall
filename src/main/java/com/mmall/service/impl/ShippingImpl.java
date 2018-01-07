package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Splitter;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhonglunsheng on 2018/1/6.
 */
@Service("iShippingService")
public class ShippingImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ServiceResponse add(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        int rowCount = shippingMapper.insert(shipping);
        if (rowCount > 0){
            Map resultMap = new HashMap();
            resultMap.put("shippingId",shipping.getId());
            return ServiceResponse.createBySuccess("收获地址添加成功",resultMap);
        }
        return ServiceResponse.createByErrorMessage("收获地址添加失败");
    }

    @Override
    public ServiceResponse delete(Integer userId, String shippingIds){
        List<String> shippingIdList = Splitter.on(",").splitToList(shippingIds);
        if (shippingIdList != null || shippingIdList.size()!=0){
            int rowCount = shippingMapper.deleteByUserIdShippingId(userId,shippingIdList);
            if (rowCount > 0){
                return ServiceResponse.createBySuccessMessage("收获地址删除成功");
            }
        }
        return ServiceResponse.createByErrorMessage("收获地址删除失败");
    }

    @Override
    public ServiceResponse update(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        int rowCount = shippingMapper.updateByPrimaryKey(shipping);
        if (rowCount > 0){
            return ServiceResponse.createBySuccessMessage("收获地址更新成功");
        }

        return ServiceResponse.createByErrorMessage("收获地址更新失败");
    }

    @Override
    public ServiceResponse getShipping(Integer userId, Integer shippingId){
        Shipping shipping = shippingMapper.selectByUserIdShippingId(userId,shippingId);
        if (shipping != null){
            return ServiceResponse.createBySuccess("获取信息成功",shipping);
        }
        return ServiceResponse.createByErrorMessage("无法查询该地址");
    }

    @Override
    public ServiceResponse<PageInfo> list(Integer userId, Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServiceResponse.createBySuccess(pageInfo);
    }
}
