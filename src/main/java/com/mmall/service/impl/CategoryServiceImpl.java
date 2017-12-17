package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Created by zhonglunsheng on 2017/12/9.
 */
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService{

    @Autowired
    private CategoryMapper categoryMapper;

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Override
    public ServiceResponse<String> addCategory(String name, Integer parentId){
        if (StringUtils.isBlank(name) || parentId==null){
            return ServiceResponse.createByErrorMessage("参数错误");
        }
        Category category = new Category();
        category.setName(name);
        category.setParentId(parentId);
        category.setStatus(true);

        int rowCount = categoryMapper.insert(category);
        if (rowCount > 0){
            return ServiceResponse.createBySuccess();
        }
        return ServiceResponse.createByErrorMessage("添加品类失败");
    }

    @Override
    public ServiceResponse<String> updateCategoryName(Integer id, String name){
        if (id == null || StringUtils.isBlank(name)){
            return ServiceResponse.createByErrorMessage("参数错误");
        }

        Category category = new Category();
        category.setId(id);
        category.setName(name);

        int rowCount = categoryMapper.updateCategoryName(id,name);
        if (rowCount > 0){
            return ServiceResponse.createBySuccess();
        }
        return ServiceResponse.createByErrorMessage("更新品类名失败");
    }

    @Override
    public ServiceResponse<List<Category>> getChildrenParallelCategory(Integer parentId){
        if (parentId == null){
            logger.info("未找到当前分类的子类");
        }

        List<Category> categories = categoryMapper.getChildrenParallelCategory(parentId);
        return ServiceResponse.createBySuccess(categories);
    }

    @Override
    public ServiceResponse<List<Integer>> getChildrenCategoryById(Integer id){
        Set<Category> set = Sets.newHashSet();
        getDeepChildrenCategory(set,id);

        List<Integer> categoryIdList = Lists.newArrayList();
        for (Category c:
             set) {
            categoryIdList.add(c.getId());
        }
        return ServiceResponse.createBySuccess(categoryIdList);
    }


    //先传一个要查找的id，然后添加到set里面，查出以这个id为父节点的所有子节点，遍历调用自己，直到遍历结束
    public Set<Category> getDeepChildrenCategory(Set<Category> set,Integer id){
        Category category = categoryMapper.selectByPrimaryKey(id);
        if (category !=null){
            set.add(category);
        }

        List<Category> categories = categoryMapper.getChildrenParallelCategory(id);
        for (Category c:
             categories) {
            getDeepChildrenCategory(set,c.getId());
        }
        return set;
    }
}
