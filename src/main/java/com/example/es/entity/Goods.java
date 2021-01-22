package com.example.es.entity;

import lombok.Data;
import org.joda.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Document(indexName = "goods")
@Data
//@Setting(settingPath="Goods.json")
//@Mapping(mappingPath="Goods.json")
public class Goods {


    @Id
    private Long skuId;

    private String pic;

    //type="text" 分词 ,type="keyword" 不分词
//    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;

//    @Field(type = FieldType.Double)
    private BigDecimal price;

    // 销量
//    @Field(type = FieldType.Long)
    private Long sales;
    // 是否有货
//    @Field(type = FieldType.Boolean)
    private Boolean store;
    // 新品
//    @Field(type = FieldType.Date)
    private LocalDateTime createTime;

//    @Field(type = FieldType.Long)
    private Long brandId;

//    @Field(type = FieldType.Keyword)
    private String brandName;

//    @Field(type = FieldType.Long)
    private Long categoryId;

//    @Field(type = FieldType.Keyword)
    private String categoryName;

    /**
     * es嵌套用法
     */
//    @Field(type = FieldType.Nested)
    private List<SearchAttrValue> attrs;

}
