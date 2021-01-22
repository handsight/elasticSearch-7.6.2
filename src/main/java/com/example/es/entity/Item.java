package com.example.es.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

import java.math.BigDecimal;
@Document(indexName = "item")
@Data
@AllArgsConstructor
public class Item {
    private Long id;
    //标题
    private String title;
    // 分类
    private String category;

    // 品牌
    private String brand;

    // 价格
    private BigDecimal price;


}
