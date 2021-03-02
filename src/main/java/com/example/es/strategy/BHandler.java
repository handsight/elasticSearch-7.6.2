package com.example.es.strategy;

import org.springframework.stereotype.Component;

@Component
public class BHandler implements Handler{
    @Override
    public void test(String name) {
        System.out.println("执行B。。。。。。。。。");
        int num=1/0;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //注册A
        Factory.register("B", this);
    }
}
