package com.mmall.service;

import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;

/**
 * Created by zhonglunsheng on 2017/12/6.
 */
public interface IUserService {

    ServiceResponse<User> login(String username, String password);

    ServiceResponse regist(User user);

    ServiceResponse checkValid(String str, String type);

    ServiceResponse<String> selectQuestion(String username);

    ServiceResponse<String> checkAnswer(String username, String question, String answer);
}
