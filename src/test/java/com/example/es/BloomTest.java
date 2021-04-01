package com.example.es;

import com.example.es.redis.MyBloomFilter;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.assertj.core.util.Lists;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class BloomTest {

    /**
     * 加上集合存在100w数据
     */
    private static Integer size = 1000000;


    //https://maplefire.blog.csdn.net/article/details/114282921
    @Test
    public void test1() {
        //1创建一个布隆过滤器,误判概率0.01
        BloomFilter<Integer> integerBloomFilter = BloomFilter.create(Funnels.integerFunnel(), 2 << 28,0.01);


        //2模拟从数据库查询订单id
        List<Integer> orderIds= Lists.newArrayList();
        for (int i = 0; i <1000000 ; i++) {
            orderIds.add(i);
        }

        // 3.订单id添加布隆过滤器中
        for (int i = 0; i < orderIds.size(); i++) {
            integerBloomFilter.put(orderIds.get(i));
        }


        //4.测试不包含的数据
        ArrayList<Integer> integers = new ArrayList<>();
        for (int j = size; j < size + 1000000; j++) {
            // 检查元素在集合中是否存在，存在返true
            if (integerBloomFilter.mightContain(j)) {
                // 存放误判的结果，方便后期统计
                integers.add(j);
            }
        }
        //布隆过滤器误判的结果：98
        System.out.println("布隆过滤器误判的结果：" + integers.size());
    }


    //https://blog.csdn.net/qq_33709582/article/details/108407706
    @Test
    public void test2() {
        MyBloomFilter myNewBloomFilter = new MyBloomFilter();
//        myNewBloomFilter.add("张学友");
//        myNewBloomFilter.add("郭德纲");
//        myNewBloomFilter.add("蔡徐鸡");
//        myNewBloomFilter.add(666);
//        System.out.println(myNewBloomFilter.isContain("张学友"));//true
//        System.out.println(myNewBloomFilter.isContain("张学友 "));//false
//        System.out.println(myNewBloomFilter.isContain("张学友1"));//false
//        System.out.println(myNewBloomFilter.isContain("郭德纲"));//true
//        System.out.println(myNewBloomFilter.isContain("蔡徐老母鸡"));//false
//        System.out.println(myNewBloomFilter.isContain(666));//true
//        System.out.println(myNewBloomFilter.isContain(888));//false

        for (int i = 0; i < 1000000; i++) {
            myNewBloomFilter.add(i);
        }

        ArrayList<Integer> integers1 = new ArrayList<>();
        ArrayList<Integer> integers2 = new ArrayList<>();
        for (int j = size; j < size + 1000000000; j++) {
            // 检查元素在集合中是否存在，存在返true
            if (myNewBloomFilter.isContain(j)) {
                // 存放误判的结果，方便后期统计
                integers1.add(j);
            }else {
                integers2.add(j);
            }
        }
        System.out.println("布隆过滤器误判的结果：" + integers1.size());
        System.out.println("布隆过滤器正确的结果：" + integers2.size());
    }
}
