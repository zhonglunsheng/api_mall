package com.mmall.service.impl;

import com.mmall.common.ServiceResponse;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Created by zhonglunsheng on 2017/12/6.
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService{

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServiceResponse<User> login(String username, String password) {
        int result = userMapper.checkUsername(username);
        if (result==0){
            return ServiceResponse.createByError("用户名不存在");
        }

        User user = userMapper.selectLogin(username,password);
        if (user == null){
            return ServiceResponse.createByError("用户名或密码错误");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServiceResponse.createBySuccess("登录成功",user);
    }
}
