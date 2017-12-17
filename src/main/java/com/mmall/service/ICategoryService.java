package com.mmall.service;

import com.mmall.common.ServiceResponse;
import com.mmall.pojo.Category;

import java.util.List;

/**
 * Created by zhonglunsheng on 2017/12/9.
 */
public interface ICategoryService {
    ServiceResponse<String> addCategory(String name, Integer parentId);

    ServiceResponse<String> updateCategoryName(Integer id, String name);

    ServiceResponse<List<Category>> getChildrenParallelCategory(Integer id);

    ServiceResponse<List<Integer>> getChildrenCategoryById(Integer id);
}
