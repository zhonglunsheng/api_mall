package com.mmail.Cart;

import org.junit.Test;

import java.math.BigDecimal;

/**
 * Created by zhonglunsheng on 2018/1/5.
 */
public class BigDecimalTest {

    @Test
    public void Test1(){
        System.out.println(0.06+0.01);
        System.out.println(1.0-0.42);
        System.out.println(4.015*100);
        System.out.println(303.1/1000);
    }

    @Test
    public void Test2(){
        BigDecimal b1 = new BigDecimal(0.05);
        BigDecimal b2 = new BigDecimal(0.01);
        System.out.println(b1.add(b2));
    }

    @Test
    public void Test3(){
        BigDecimal b1 = new BigDecimal("0.05");
        BigDecimal b2 = new BigDecimal("0.01");
        System.out.println(b1.add(b2));
    }
}
