package com.java.test;

import org.junit.Test;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by zhonglunsheng on 2018/1/20.
 */
public class  JavaTest <T> {

    static class test{

    }

    public static void main(String[] args) {
       new test();
    }
    @Test
    public void print(){
        String s1 = "Programming";
        String s2 = new String("Programming");

    /*    String s3 = "Program";
        String s4 = "ming";
        String s5 = "Program" + "ming";
        String s6 = s3 + s4;
        System.out.println(s1 == s2);
        System.out.println(s1 == s5);
        System.out.println(s1 == s6);
        System.out.println(s1 == s6.intern());
        System.out.println(s2 == s2.intern());*/
        System.out.println(s2.intern() == s1);
    }

    @Test
    public void add(){
        int i,j,s=0,n=10;
        //(n+n-1+n-2+...+1)≈(n^2)/2
        for(i=1;i<=n;i++) {
            for(j=i;j<=n;j++) {
                s++;
            }
        }


        System.out.println(s++);
    }

    @Test
    public void eq(){
        Student s1 = new Student();
        s1.setName("xx");
        s1.setNum(1);

        Student s2 = new Student();
        s2.setName("xx");
        s2.setNum(1);

       // Student s2 = s1;
        System.out.println(s1);
        System.out.println(s2);

        s2.setNum(3);
        System.out.println(s2);

    }

    public void swap(Integer array[]){
        Integer temp =  array[0];
        array[0] = array[1];
        array[1] =  temp;
    }

    @Test
    public void test(){
        Integer a = new Integer(1);
        Integer b = new Integer(3);
        Integer[] array = new Integer[]{a,b};
        swap(array);
        a = array[0];
        b = array[1];
        System.out.println(a);
        System.out.println(b);

        int c = 3;
        String.valueOf(c);
    }

    @Test
    public void anclass(){
        JButton jButton = new JButton("xxx");
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

}

class Student{
    private int num;
    private String name;

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
