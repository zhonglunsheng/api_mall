package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by zhonglunsheng on 2018/1/4.
 */
@Controller
@RequestMapping("/cart/")
public class CartController {

    @Autowired
    private ICartService iCartService;

    /**
     * 添加商品到购物车
     * @param session
     * @param count
     * @param productId
     * @return 返回购物车信息
     */
    @RequestMapping("add.do")
    @ResponseBody
    public ServiceResponse<CartVo> add(HttpSession session, Integer count, Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServiceResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGMENT.getCode(),ResponseCode.ILLEGAL_ARGMENT.getDesc());
        }
        return iCartService.add(user.getId(),count,productId);
    }
}
