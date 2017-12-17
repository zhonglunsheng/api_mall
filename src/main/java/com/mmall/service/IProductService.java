package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;

/**
 * Created by zhonglunsheng on 2017/12/11.
 */
public interface IProductService {
    ServiceResponse<String> saveOrUpdateProduct(Product product);

    ServiceResponse<String> setSalesStatus(Integer productId, Integer status);

    ServiceResponse<ProductDetailVo> getDetail(Integer productId);

    ServiceResponse<PageInfo> getProductList(Integer pageNum, Integer pageSize);

    ServiceResponse<PageInfo> searchProductList(String productName, Integer productId,Integer pageNum, Integer pageSize);
}
