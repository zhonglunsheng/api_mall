package com.mmall.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.text.SimpleDateFormat;

@Slf4j
public class JsonUtil {
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        //对象所有字段全部列入
        objectMapper.setSerializationInclusion(Inclusion.ALWAYS);
        //取消默认转换时间戳形式
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,false);
        //忽略空bean转换json的错误
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS,false);
        //忽略在json字符串存在，但在java对象不存在对应属性的情况，防止错误
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,false);
        //将时间转换为固定格式 yyyy-MM-dd HH:ss:mm
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));
    }

    public static <T> String object2String(T obj){
        if (obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String)obj : objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("json对象转字符串出现异常",e);
            return null;
        }
    }

    public static <T> String object2StringPretty(T obj){
        if (obj == null){
            return null;
        }
        try {
            return obj instanceof String ? (String)obj : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            log.warn("对象转字符串出现异常",e);
            return null;
        }
    }

    public static <T> T string2Obj(String str, Class<T> clazz){
        if (StringUtils.isEmpty(str) || clazz == null){
            return null;
        }
        try {
            return clazz.equals(String.class) ? (T)str : objectMapper.readValue(str,clazz);
        } catch (Exception e) {
            log.warn("字符串转对象出现异常",e);
            return null;
        }
    }

    public static <T> T string2Obj(String str, TypeReference<T> typeReference){
        if (StringUtils.isEmpty(str) || typeReference == null){
            return null;
        }
        try {
            return (T)(typeReference.getType().equals(String.class) ? (T)str : objectMapper.readValue(str,typeReference));
        } catch (Exception e) {
            log.warn("字符串转对象出现异常",e);
            return null;
        }
    }

    public static <T> T string2Obj(String str, Class<?> collectionClass, Class<?> ... elementClass){
       JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass,elementClass);
        try {
            return objectMapper.readValue(str,javaType);
        } catch (Exception e) {
            log.warn("字符串转对象出现异常",e);
            return null;
        }

    }


}
