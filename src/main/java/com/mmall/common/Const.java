package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by zhonglunsheng on 2017/12/6.
 */
public class Const {

    public static final String CURRENT_USER = "currentUser";

    public static final String USERNAME = "username";

    public static final String EMAIL = "email";

    public interface Role{
        int ROLE_CUSTOM = 0;
        int ROLE_ADMIN = 1;
    }

    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }
    public enum ProductStateEnum {
        ON_SALE(1,"在线");
        private int status;
        private String desc;

        ProductStateEnum(int status, String desc){
            this.status=status;
            this.desc=desc;
        }
        public int getStatus() {
            return status;
        }
        public String getDesc() {
            return desc;
        }
    }



}
