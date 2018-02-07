package com.mmall.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
@Slf4j
@Component
public class ExceptionResolver implements HandlerExceptionResolver{

    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        ModelAndView modelAndView = new ModelAndView(new MappingJacksonJsonView());
        modelAndView.addObject("status","1");
        modelAndView.addObject("msg","接口异常,详情请查看日志异常信息");
        modelAndView.addObject("data",e.getMessage());
        log.error("{} Exception ",httpServletRequest.getRequestURI(),e);
        return modelAndView;
    }
}
