package com.mmall.common;


import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * Created by zhonglunsheng on 2017/12/6.
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
//保证序列化json的时候，如果是null的对象，key也会消失
public class ServiceResponse<T> implements Serializable{

    private int status;
    private String msg;
    private T data;

    private ServiceResponse(int status){
        this.status = status;
    }

    private ServiceResponse(int status, String msg){
        this.status = status;
        this.msg = msg;
    }

    private ServiceResponse(int status, T data){
        this.status = status;
        this.data = data;
    }

    private ServiceResponse(int status, String msg, T data){
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    @JsonIgnore
    public boolean isSuccess(){
        return this.status == ResponseCode.SUCCESS.getCode();
    }
    public int getStatus(){
        return status;
    }
    public String getMsg(){
        return msg;
    }
    public T getData(){
        return data;
    }

    public static <T> ServiceResponse<T> createBySuccess(){
        return new ServiceResponse<T>(ResponseCode.SUCCESS.getCode());
    }

    public static <T> ServiceResponse<T> createBySuccessMessage(String msg){
        return new ServiceResponse<T>(ResponseCode.SUCCESS.getCode(),msg);
    }

    public static <T> ServiceResponse<T> createBySuccess(T data){
        return new ServiceResponse<T>(ResponseCode.SUCCESS.getCode(),data);
    }

    public static <T> ServiceResponse<T> createBySuccess(String msg, T data){
        return new ServiceResponse<T>(ResponseCode.SUCCESS.getCode(),msg,data);
    }



    public static <T> ServiceResponse<T> createByError(){
        return new ServiceResponse<T>(ResponseCode.ERROR.getCode());
    }

    public static <T> ServiceResponse<T> createByError(String errorMsg){
        return new ServiceResponse<T>(ResponseCode.ERROR.getCode(),errorMsg);
    }

    public static <T> ServiceResponse<T> createByError(T data){
        return new ServiceResponse<T>(ResponseCode.ERROR.getCode(),data);
    }

    public static <T> ServiceResponse<T> createByError(String errorMsg, T data){
        return new ServiceResponse<T>(ResponseCode.ERROR.getCode(),errorMsg,data);
    }






}
