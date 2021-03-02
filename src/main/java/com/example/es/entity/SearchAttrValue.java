package com.example.es.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
public class SearchAttrValue {

    //属性id,属性名，属性值
//    @Field(type = FieldType.Long)
    private Long attrId;
//    @Field(type = FieldType.Keyword)
    private String attrName;
//    @Field(type = FieldType.Keyword)
    private String attrValue;

    public static void main(String[] args) {
        System.out.println(10%2);
    }
}
