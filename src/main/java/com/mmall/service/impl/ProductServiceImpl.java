package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.omg.CORBA.Object;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by zhonglunsheng on 2017/12/11.
 */
@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ServiceResponse<String> saveOrUpdateProduct(Product product){
        if (product !=null){
            if (StringUtils.isNotBlank(product.getMainImage())){
                String[] subImages = product.getSubImages().split(",");
                //默认第一张为主图
                product.setMainImage(subImages[0]);
            }
            if (product.getId() != null){
                //更新
                int rowCount = productMapper.updateByPrimaryKeySelective(product);
                if (rowCount > 0){
                    return ServiceResponse.createBySuccessMessage("产品更新成功");
                }else{
                    return ServiceResponse.createByErrorMessage("产品更新失败");
                }
            }else{
                //新增
                int rowCount = productMapper.insertSelective(product);
                if (rowCount > 0){
                    return ServiceResponse.createBySuccessMessage("产品新增成功");
                }else{
                    return ServiceResponse.createByErrorMessage("产品新增失败");
                }
            }
        }else {
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGMENT.getCode(),ResponseCode.ILLEGAL_ARGMENT.getDesc());
        }
    }

    @Override
    public ServiceResponse<String> setSalesStatus(Integer productId ,Integer status){
        if (status != null && productId!=null){
            Product product = new Product();
            product.setId(productId);
            product.setStatus(status);

            int rowCount = productMapper.updateByPrimaryKeySelective(product);
            if (rowCount > 0){
                return ServiceResponse.createBySuccessMessage("产品状态更新成功");
            }else{
                return ServiceResponse.createByErrorMessage("产品状态更新失败");
            }

        }else{
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGMENT.getCode(),ResponseCode.ILLEGAL_ARGMENT.getDesc());
        }
    }

    @Override
    public ServiceResponse<ProductDetailVo> getDetail(Integer productId){
        if (productId == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGMENT.getCode(),ResponseCode.ILLEGAL_ARGMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null){
            return ServiceResponse.createByErrorMessage("商品不存在或已下架");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServiceResponse.createBySuccess(productDetailVo);
    }

    public ProductDetailVo assembleProductDetailVo(Product product){
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setName(product.getName());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));

        Category category = categoryMapper.selectByPrimaryKey(product.getId());
        if (category == null){
            productDetailVo.setParentCategoryId(0);
        }else{
            productDetailVo.setParentCategoryId(category.getParentId());
        }
        return productDetailVo;
    }

    @Override
    public ServiceResponse<PageInfo> getProductList(Integer pageNum,Integer pageSize){
        //startPage->sql->pageHelper
        PageHelper.startPage(pageNum,pageSize);
        List<Product> products = productMapper.getProductList();
        List<ProductListVo> productListVos = Lists.newArrayList();
        for (Product product:
             products) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVos.add(productListVo);
        }

        PageInfo pageResult = new PageInfo(products);
        pageResult.setList(productListVos);
        return ServiceResponse.createBySuccess(pageResult);
    }

    public ProductListVo assembleProductListVo(Product product){
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }

    @Override
    public ServiceResponse<PageInfo> searchProductList(String productName, Integer productId, Integer pageNum, Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        if (StringUtils.isNotBlank(productName)){
            productName=new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> products = productMapper.searchProductByNameAndIdList(productName,productId);
        List<ProductListVo> productListVos = Lists.newArrayList();
        for (Product product:
                products) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVos.add(productListVo);
        }

        PageInfo pageResult = new PageInfo(products);
        pageResult.setList(productListVos);
        return ServiceResponse.createBySuccess(pageResult);
    }
}
