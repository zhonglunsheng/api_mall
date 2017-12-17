package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
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
            return ServiceResponse.createByErrorMessage("用户名不存在");
        }

        String passwordMd5 = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username,passwordMd5);
        if (user == null){
            return ServiceResponse.createByErrorMessage("用户名或密码错误");
        }

        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);
        return ServiceResponse.createBySuccess("登录成功",user);
    }

    @Override
    public ServiceResponse<String> regist(User user) {
        ServiceResponse validResponse = this.checkValid(user.getUsername(),Const.USERNAME);
        if (!validResponse.isSuccess()){
            return validResponse;
        }

        validResponse = this.checkValid(user.getEmail(),Const.EMAIL);
        if (!validResponse.isSuccess()){
            return validResponse;
        }

        user.setRole(Const.Role.ROLE_CUSTOM);
        int result = userMapper.insert(user);
        if (result == 0){
            return ServiceResponse.createByErrorMessage("注册失败");
        }
        return ServiceResponse.createBySuccessMessage("注册成功");
    }

    @Override
    public ServiceResponse<String> checkValid(String str,String type){
        if (org.apache.commons.lang3.StringUtils.isNotBlank(type)){
            //开始验证
            if (Const.USERNAME.equals(type)){
                int result = userMapper.checkUsername(str);
                if (result > 0){
                    return ServiceResponse.createByErrorMessage("用户名已存在");
                }
            }

            if (Const.EMAIL.equals(type)){
                int result = userMapper.checkEmail(str);
                if (result > 0){
                    return ServiceResponse.createByErrorMessage("邮箱已存在");
                }
            }
        }else{
            return ServiceResponse.createByErrorMessage("参数错误");
        }
        return ServiceResponse.createBySuccessMessage("校验成功");
    }

    @Override
    public ServiceResponse<String> selectQuestion(String username){
        ServiceResponse response = this.checkValid(username,Const.USERNAME);
        if (response.isSuccess()){
            return ServiceResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestion(username);
        if (StringUtils.isNotBlank(question)){
            return ServiceResponse.createBySuccess(question);
        }
        return ServiceResponse.createByErrorMessage("此用户没有设置密保问题");
    }

    @Override
    public ServiceResponse<String> checkAnswer(String username,String question,String answer){
        int resultCount = userMapper.checkAnswer(username,question,answer);
        if (resultCount > 0){
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return ServiceResponse.createBySuccess(forgetToken);
        }
        return ServiceResponse.createByErrorMessage("密保答案错误");
    }

    @Override
    public ServiceResponse<String> restPassword(String username, String passwordNew, String forgetToken){
        ServiceResponse response = this.checkValid(username,Const.USERNAME);
        if (response.isSuccess()){
            return ServiceResponse.createByErrorMessage("用户不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if (StringUtils.isBlank(token)){
            return ServiceResponse.createByErrorMessage("token为空");
        }
        if (StringUtils.equals(token,forgetToken)){
            String passwordMd5 = MD5Util.MD5EncodeUtf8(passwordNew);
            int resultCount = userMapper.restPassword(username,passwordMd5);
            if (resultCount > 0)
            return ServiceResponse.createBySuccessMessage("重置密码成功");
        }else{
            return ServiceResponse.createByErrorMessage("token错误或过期请重新获取");
        }
        return ServiceResponse.createByErrorMessage("重置密码失败");
    }

    @Override
    public ServiceResponse<String> updatePassword(String passwordOld, String passwordNew, User user){
        String passwordOldMd5 = MD5Util.MD5EncodeUtf8(passwordOld);
        int resultCount = userMapper.checkPassword(user.getId(),passwordOldMd5);
        if (resultCount == 0){
            return ServiceResponse.createByErrorMessage("旧密码错误");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0){
            return ServiceResponse.createBySuccessMessage("密码更新成功,下次登录生效");
        }
        return ServiceResponse.createByErrorMessage("密码更新失败");
    }

    @Override
    public ServiceResponse<User> updateInformation(User user){
        //id不可变 username不可变 email需唯一
        int resultCount = userMapper.checkEmailByUserId(user.getId(),user.getEmail());
        if (resultCount > 0){
            return ServiceResponse.createByErrorMessage("该邮箱已绑定其他用户");
        }

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setPhone(user.getPhone());
        updateUser.setEmail(user.getEmail());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0){
            return ServiceResponse.createBySuccess("用户信息更新成功",updateUser);
        }
        return ServiceResponse.createByErrorMessage("用户信息更新失败");
    }

    @Override
    public ServiceResponse<User> getInformation(User user){
        User resultUser = userMapper.selectByPrimaryKey(user.getId());
        if (resultUser == null){
            return ServiceResponse.createByErrorMessage("没有此用户");
        }
        resultUser.setPassword(StringUtils.EMPTY);
        return ServiceResponse.createBySuccess(resultUser);
    }



    @Override
    public ServiceResponse<String> checkAdminRole(User user){
        if (user.getRole() == Const.Role.ROLE_ADMIN){
            return ServiceResponse.createBySuccess();
        }else{
            return ServiceResponse.createByError();
        }
    }

}
