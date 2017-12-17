package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by zhonglunsheng on 2017/12/9.
 */
@Controller
@RequestMapping("/manage/category")
public class CategoryManagerController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 添加分类
     * @param session
     * @param name
     * @param parentId
     * @return
     */
    @RequestMapping(value = "add_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String> addCategory(HttpSession session, String name, @RequestParam(value = "parentId",defaultValue = "0")int parentId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServiceResponse.createByErrorMessage("请先登录");
        }

        ServiceResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            return iCategoryService.addCategory(name,parentId);
        }else{
            return ServiceResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 更新分类名
     * @param session
     * @param id
     * @param name
     * @return
     */
    @RequestMapping(value = "update_category_name.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse<String> updateCategoryName(HttpSession session, Integer id, String name){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServiceResponse.createByErrorMessage("请先登录");
        }

        ServiceResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            return iCategoryService.updateCategoryName(id,name);
        }else{
            return ServiceResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    /**
     * 查询平级下子节点
     * @param session
     * @param parentId
     * @return
     */
    @RequestMapping(value = "get_children_parallel_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse getChildrenParallelCategory(HttpSession session,@RequestParam(value = "parentId",defaultValue = "0") Integer parentId){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServiceResponse.createByErrorMessage("请先登录");
        }

        ServiceResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            return iCategoryService.getChildrenParallelCategory(parentId);
        }else{
            return ServiceResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }

    @RequestMapping(value = "get_category_and_deep_children_category.do",method = RequestMethod.POST)
    @ResponseBody
    public ServiceResponse getCategoryAndDeepChildrenCategory(HttpSession session,Integer id){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null){
            return ServiceResponse.createByErrorMessage("请先登录");
        }

        ServiceResponse response = iUserService.checkAdminRole(user);
        if (response.isSuccess()){
            return iCategoryService.getChildrenCategoryById(id);
        }else{
            return ServiceResponse.createByErrorMessage("无权限操作，需要管理员权限");
        }
    }


}
