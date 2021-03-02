package com.example.es.strategy;

import org.springframework.beans.factory.InitializingBean;
/**
 * 策略设计模式 接口
 */
public interface  Handler  extends InitializingBean {

     void test(String name);
}
