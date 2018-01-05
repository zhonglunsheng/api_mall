package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhonglunsheng on 2018/1/4.
 */
@Service("iCartService")
public class CartService implements ICartService{

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public ServiceResponse<CartVo> add(Integer userId,Integer count,Integer productId){
        if (productId == null || count==null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGMENT.getCode(),ResponseCode.ILLEGAL_ARGMENT.getDesc());
        }

        Cart cart = cartMapper.selectByUserIdProductId(userId,productId);
        if (cart == null){
            //购物车不存在该产品，新增记录
            Cart cartItem = new Cart();
            cartItem.setUserId(userId);
            cartItem.setProductId(productId);
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartMapper.insert(cartItem);
        }else{
            //购物车存在该产品，增加数量
            count = cart.getQuantity()+count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        //返回购物车信息
        return this.list(userId);
    }

    @Override
    public ServiceResponse<CartVo> list(Integer userId){
        //将要返回的Cart数据封装成一个对象并返回
        return ServiceResponse.createBySuccess(this.getCartVo(userId));
    }

    public CartVo getCartVo(Integer userId){
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartMapper.selectByUserId(userId);
        List<CartProductVo> cartProductVoList = new ArrayList<>();

        BigDecimal totalPrice = new BigDecimal("0");

        for (Cart cartItem:
             cartList) {
            CartProductVo cartProductVo = new CartProductVo();
            cartProductVo.setUserId(cartItem.getUserId());
            cartProductVo.setProductId(cartItem.getProductId());
            cartProductVo.setId(cartItem.getId());

            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            if (product != null){
                cartProductVo.setProductName(product.getName());
                cartProductVo.setProductMainImage(product.getMainImage());
                cartProductVo.setProductSubtitle(product.getSubtitle());
                cartProductVo.setProductStatus(product.getStatus());
                cartProductVo.setProductPrice(product.getPrice());
                cartProductVo.setProductStock(product.getStock());

                int buyLimitCount = 0;
                if (product.getStock() >= cartItem.getQuantity()){
                    //库存充足
                    buyLimitCount = cartItem.getQuantity();
                    cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                }else{
                    //库存不够
                    buyLimitCount = product.getStock();
                    cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);

                    Cart updatCartForQuantity = new Cart();
                    updatCartForQuantity.setId(cartItem.getId());
                    updatCartForQuantity.setQuantity(buyLimitCount);
                    cartMapper.updateByPrimaryKeySelective(updatCartForQuantity);
                }
                cartProductVo.setQuantity(buyLimitCount);
                //计算产品总价
                cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartProductVo.getQuantity()));
                cartProductVo.setProductChecked(cartItem.getChecked());
                cartProductVoList.add(cartProductVo);
            }
            if (cartProductVo.getProductChecked() == Const.Cart.CHECKED){
                totalPrice = BigDecimalUtil.add(totalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
            }
        }
        cartVo.setCartTotalPrice(totalPrice);
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setCartProductVoList(cartProductVoList);

        return cartVo;
    }


    public boolean getAllCheckedStatus(Integer userId){
        if (userId == null){
            return false;
        }

        return cartMapper.selectByUserIdCheckStatus(userId) == 0;
    }
}
