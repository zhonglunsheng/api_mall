package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private IFileService iFileService;

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
            return iProductService.searchProductByIdAndName(productName,productId,pageNum,pageSize);
        }else {
            return ServiceResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * SpringMvc图片上传
     * @param session
     * @param file
     * @param request
     * @return
     */
    @RequestMapping(value = "upload.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse upload(HttpSession session, @RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServiceResponse.createByErrorMessage("请先登录");
        }

        ServiceResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file,path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;

            Map<String,String> fileMap = Maps.newHashMap();
            fileMap.put("uri",targetFileName);
            fileMap.put("url",url);
            return ServiceResponse.createBySuccess(fileMap);
        }else {
            return ServiceResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     *wangEditor富文本图片上传
     * @param session
     * @param file
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "richtext_img_upload.do",method = RequestMethod.POST)
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request,HttpServletResponse response){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        Map resultMap = Maps.newHashMap();
        if (user == null){
            resultMap.put("errno",1);
            return resultMap;
        }
        if (iUserService.checkAdminRole(user).isSuccess()){
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file,path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            List<String> urlList = Lists.newArrayList();
            urlList.add("http://blog.i20forever.cn/content/templates/limh.me/images/random/tb10.jpg");
            /*{
                // errno 即错误代码，0 表示没有错误。
                //       如果有错误，errno != 0，可通过下文中的监听函数 fail 拿到该错误码进行自定义处理
                errno: 0,

                        // data 是一个数组，返回若干图片的线上地址
                        data: [
                '图片1地址',
                        '图片2地址',
                        '……'
                ]
            }*/
            if (StringUtils.isBlank(targetFileName)){
                resultMap.put("errno",1);
                return resultMap;
            }
            resultMap.put("errno",0);
            resultMap.put("data",urlList);
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return resultMap;
        }else {
            resultMap.put("errno",1);
            return resultMap;
        }
    }
}
