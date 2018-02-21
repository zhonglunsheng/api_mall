package com.mmall.controller.common.interceptor;

import com.mmall.common.Const;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AutorityInterceptor implements HandlerInterceptor{
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //请求controller中的方法名
        HandlerMethod handlerMethod = (HandlerMethod)handler;
        //解析handlerMethod
        String methodName = handlerMethod.getMethod().getName();
        String className = handlerMethod.getBean().getClass().getSimpleName();

        if (StringUtils.equals(className,"UserManagerController") && StringUtils.equals(methodName,"login")){
            log.info("权限拦截器拦截到请求,className:{},methodName:{}",className,methodName);
            return true;
        }

        User user = null;
        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isNotEmpty(loginToken)){
            user = JsonUtil.string2Obj(RedisShardedPoolUtil.get(loginToken),User.class);
        }

        if (user == null || user.getRole().intValue() != Const.Role.ROLE_ADMIN){
            response.reset();
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/json;charset=utf-8");
            PrintWriter out = response.getWriter();

            if (user == null){
                if (StringUtils.equals(className,"ProductManagerController") || StringUtils.equals(methodName,"richtextImgUpload")){
                    Map<String,String> resultMap = new HashMap<>();
                    resultMap.put("success","false");
                    resultMap.put("msg","用户未登录");
                    out.print(JsonUtil.object2String(resultMap));
                }else{
                    out.print("拦截器拦截，用户未登录");
                }
            }else{
                if (StringUtils.equals(className,"ProductManagerController") || StringUtils.equals(methodName,"richtextImgUpload")){
                    Map<String,String> resultMap = new HashMap<>();
                    resultMap.put("success","false");
                    resultMap.put("msg","无权限操作");
                    out.print(JsonUtil.object2String(resultMap));
                }else{
                    out.print("拦截器拦截，用户无权限操作");
                }
            }
            out.flush();
            out.close();
            return false;
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("postHandle");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        log.info("afterCompletion");
    }
}
