package com.example.es.strategy;

import org.springframework.stereotype.Component;

@Component
public class AHandler  implements Handler{
    @Override
    public void test(String name) {
        System.out.println("执行A。。。。。。。。。");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //注册A
        Factory.register("A", this);
    }
}
