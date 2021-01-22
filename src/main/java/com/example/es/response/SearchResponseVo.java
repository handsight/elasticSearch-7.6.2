package com.example.es.response;

import com.example.es.entity.Goods;
import lombok.Data;

import java.util.List;

@Data
public class SearchResponseVo {


    // 分页
    private Integer pageNum;
    private Integer pageSize;
    private Long total;

    // 当前页数据
    private List<Goods> goodsList;

    private List<BrandEntity> brands;

    private  List<CategoryEntity> categories;

    private List<SearchResponseAttrVo> filters;
}
