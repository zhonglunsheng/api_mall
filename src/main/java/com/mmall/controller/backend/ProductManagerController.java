package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * Created by zhonglunsheng on 2017/12/11.
 */
@Controller
@RequestMapping("/manage/product")
public class ProductManagerController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IProductService iProductService;

    /**
     * 新增或更新产品
     * @param session
     * @param product
     * @return
     */
    @RequestMapping(value = "product_save.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String> productSave(HttpSession session, Product product){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServiceResponse.createByErrorMessage("请先登录");
        }

        ServiceResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            return iProductService.saveOrUpdateProduct(product);
        }else{
            return ServiceResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 更新产品状态
     * @param session
     * @param productId
     * @param status
     * @return
     */
    @RequestMapping(value = "set_sale_status.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String> setSaleStatus(HttpSession session, Integer productId , Integer status){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServiceResponse.createByErrorMessage("请先登录");
        }

        ServiceResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            return iProductService.setSalesStatus(productId,status);
        }else{
            return ServiceResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 获取商品详情
     * @param session
     * @param productId
     * @return
     */
    @RequestMapping(value = "get_detal.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<ProductDetailVo> getDetal(HttpSession session, Integer productId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServiceResponse.createByErrorMessage("请先登录");
        }

        ServiceResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            return iProductService.getDetail(productId);
        }else {
            return ServiceResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 获取商品列表
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "get_list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<PageInfo> getList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServiceResponse.createByErrorMessage("请先登录");
        }

        ServiceResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            return iProductService.getProductList(pageNum,pageSize);
        }else {
            return ServiceResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 搜索商品
     * @param session
     * @param productName
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "search_list.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<PageInfo> searchList(HttpSession session,String productName,Integer productId,@RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum, @RequestParam(value = "pageSize",defaultValue = "10") Integer pageSize){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServiceResponse.createByErrorMessage("请先登录");
        }

        ServiceResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            return iProductService.searchProductList(productName,productId,pageNum,pageSize);
        }else {
            return ServiceResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

}
