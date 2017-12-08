package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;


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

        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);
        return ServiceResponse.createBySuccess("登录成功",user);
    }

    @Override
    public ServiceResponse regist(User user) {
        ServiceResponse validResponse = this.checkValid(user.getUsername(),Const.USERNAME);
        if (!validResponse.isSuccess()){
            return validResponse;
        }

        validResponse = this.checkValid(user.getUsername(),Const.EMAIL);
        if (!validResponse.isSuccess()){
            return validResponse;
        }

        user.setRole(Const.Role.ROLE_CUSTOM);
        int result = userMapper.insert(user);
        if (result == 0){
            return ServiceResponse.createByError("注册失败");
        }
        return ServiceResponse.createBySuccess("注册成功");
    }

    @Override
    public ServiceResponse checkValid(String str,String type){
        if (org.apache.commons.lang3.StringUtils.isNotBlank(type)){
            //开始验证
            if (Const.USERNAME.equals(type)){
                int result = userMapper.checkUsername(str);
                if (result > 0){
                    return ServiceResponse.createByError("用户名已存在");
                }
            }

            if (Const.EMAIL.equals(type)){
                int result = userMapper.checkEmail(str);
                if (result > 0){
                    return ServiceResponse.createByError("邮箱已存在");
                }
            }
        }else{
            return ServiceResponse.createByError("参数错误");
        }
        return ServiceResponse.createBySuccess("校验成功");
    }

    @Override
    public ServiceResponse<String> selectQuestion(String username){
        ServiceResponse response = this.checkValid(username,Const.USERNAME);
        if (response.isSuccess()){
            return ServiceResponse.createByError("用户不存在");
        }
        String question = userMapper.selectQuestion(username);
        if (StringUtils.isNotBlank(question)){
            return ServiceResponse.createBySuccess(question);
        }
        return ServiceResponse.createByError("此用户没有设置密保问题");
    }

    @Override
    public ServiceResponse<String> checkAnswer(String username,String question,String answer){
        int resultCount = userMapper.checkAnswer(username,question,answer);
        if (resultCount > 0){
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return ServiceResponse.createBySuccess(forgetToken);
        }
        return ServiceResponse.createByError("密保答案错误");
    }

}
