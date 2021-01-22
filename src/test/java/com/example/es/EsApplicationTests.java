package com.example.es;

import com.alibaba.fastjson.JSON;
import com.example.es.entity.*;
import com.example.es.response.BrandEntity;
import com.example.es.response.CategoryEntity;
import com.example.es.response.SearchResponseAttrVo;
import com.example.es.response.SearchResponseVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.assertj.core.util.Lists;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EsApplication.class)
class EsApplicationTests {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    ElasticsearchRestTemplate restTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;


    //https://www.elastic.co/guide/cn/elasticsearch/guide/current/_finding_multiple_exact_values.html
    @Test
   public void test1() {
        List<Goods> list = Lists.newArrayList();
        Goods g1 = new Goods();
        g1.setSkuId(1L);
        g1.setPic("111");
        g1.setTitle("小米刘德华手机");
        g1.setBrandId(1L);
        g1.setBrandName("小米");
        g1.setCategoryId(1L);
        g1.setCategoryName("手机");
        g1.setPrice(new BigDecimal(100));
        g1.setSales(10L);
        g1.setStore(true);
        g1.setCreateTime(LocalDateTime.now());
        List<SearchAttrValue> attrs1 = Lists.newArrayList();
        SearchAttrValue a1 = new SearchAttrValue(1l, "尺寸", "3英寸");
        SearchAttrValue a2 = new SearchAttrValue(2l, "像素", "100万像素");
        SearchAttrValue a3 = new SearchAttrValue(3l, "内存", "1G");
        SearchAttrValue a4 = new SearchAttrValue(3l, "内存", "2G");
        attrs1.add(a1);
        attrs1.add(a2);
        attrs1.add(a3);
        attrs1.add(a4);
        g1.setAttrs(attrs1);

        Goods g2 = new Goods();
        g2.setSkuId(2L);
        g2.setPic("222");
        g2.setTitle("小米刘斌电视");
        g2.setBrandId(1L);
        g2.setBrandName("小米");
        g2.setCategoryId(2L);
        g2.setCategoryName("电视");
        g2.setPrice(new BigDecimal(200));
        g2.setSales(20L);
        g2.setStore(true);
        g2.setCreateTime(LocalDateTime.now());
        List<SearchAttrValue> attrs2 = Lists.newArrayList();
        SearchAttrValue a11 = new SearchAttrValue(1l, "尺寸", "5英寸");
        SearchAttrValue a22 = new SearchAttrValue(2l, "像素", "200万像素");
        SearchAttrValue a33 = new SearchAttrValue(3l, "内存", "2G");
        attrs2.add(a11);
        attrs2.add(a22);
        attrs2.add(a33);
        g2.setAttrs(attrs2);


        Goods g3 = new Goods();
        g3.setSkuId(3L);
        g3.setPic("333");
        g3.setTitle("华为刘德志电视");
        g3.setBrandId(2L);
        g3.setBrandName("华为");
        g3.setCategoryId(2L);
        g3.setCategoryName("电视");
        g3.setPrice(new BigDecimal(300));
        g3.setSales(30L);
        g3.setStore(true);
        g3.setCreateTime(LocalDateTime.now());
        List<SearchAttrValue> attrs3 = Lists.newArrayList();
        SearchAttrValue a111 = new SearchAttrValue(1l, "尺寸", "8英寸");
        SearchAttrValue a222 = new SearchAttrValue(2l, "像素", "800万像素");
        SearchAttrValue a333 = new SearchAttrValue(3l, "内存", "8G");
        attrs3.add(a111);
        attrs3.add(a222);
        attrs3.add(a333);
        g3.setAttrs(attrs3);


        Goods g4 = new Goods();
        g4.setSkuId(4L);
        g4.setPic("444");
        g4.setTitle("华为手机");
        g4.setBrandId(2L);
        g4.setBrandName("华为");
        g4.setCategoryId(1L);
        g4.setCategoryName("手机");
        g4.setPrice(new BigDecimal(400));
        g4.setSales(40L);
        g4.setStore(true);
        g4.setCreateTime(LocalDateTime.now());
        List<SearchAttrValue> attrs4 = Lists.newArrayList();
        SearchAttrValue a1111 = new SearchAttrValue(1l, "尺寸", "16英寸");
        SearchAttrValue a2222 = new SearchAttrValue(2l, "像素", "1600万像素");
        SearchAttrValue a3333 = new SearchAttrValue(3l, "内存", "16G");
        attrs4.add(a1111);
        attrs4.add(a2222);
        attrs4.add(a3333);
        g4.setAttrs(attrs4);

        list.add(g1);
        list.add(g2);
        list.add(g3);
        list.add(g4);
        goodsRepository.saveAll(list);
        System.out.println(11111);
    }

    @Test
   public void test2() throws IOException {
        SearchParamVo paramVo = new SearchParamVo();
        paramVo.setKeyword("刘德华");
        List<Long> brandId = Lists.newArrayList();
        brandId.add(1L);
        brandId.add(2L);
        paramVo.setBrandId(brandId);
        List<Long> categoryId = Lists.newArrayList();
        categoryId.add(1L);
        paramVo.setCid(categoryId);
        paramVo.setPriceFrom(new BigDecimal("100"));
        paramVo.setPriceTo(new BigDecimal("500"));
        paramVo.setStore(true);
        List<String> props = Lists.newArrayList();
        props.add("3:1G-16G");
        paramVo.setProps(props);
        SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, buildDsl(paramVo));
        SearchResponse searchResponse = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        // 解析结果集
        SearchResponseVo responseVo = this.parseResult(searchResponse);
        responseVo.setPageNum(paramVo.getPageNum());
        responseVo.setPageSize(paramVo.getPageSize());
    }

    /**
     * 解析结果集
     * @return
     */
    private SearchResponseVo parseResult(SearchResponse response) {
        SearchResponseVo responseVo=new SearchResponseVo();
        SearchHits hits = response.getHits();
        responseVo.setTotal(hits.getTotalHits().value);

        SearchHit[] hitsHits = hits.getHits();
        List<Goods> goodsList = Stream.of(hitsHits).map(hitsHit -> {
            // 获取内层hits的_source 数据
            String goodsJson = hitsHit.getSourceAsString();
            Goods goods = JSON.parseObject(goodsJson, Goods.class);

            // 获取高亮的title覆盖掉普通title
            Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
            HighlightField highlightField = highlightFields.get("title");
            String highlightTitle = highlightField.getFragments()[0].toString();
            goods.setTitle(highlightTitle);
            return goods;
        }).collect(Collectors.toList());
        responseVo.setGoodsList(goodsList);

        // 聚合结果集的解析
        Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();
        // 1. 解析聚合结果集，获取品牌》
        ParsedLongTerms brandIdAgg = (ParsedLongTerms)aggregationMap.get("brandIdAgg");
        List<? extends Terms.Bucket> buckets = brandIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(buckets)){
            List<BrandEntity> brands = buckets.stream().map(bucket -> {
                BrandEntity brandEntity = new BrandEntity();
                // 获取brandIdAgg中的key，这个key就是品牌的id
                Long brandId = bucket.getKeyAsNumber().longValue();
                brandEntity.setId(brandId);
                // 解析品牌名称的子聚合，获取品牌名称
                Map<String, Aggregation> brandAggregationMap =bucket.getAggregations().asMap();
                ParsedStringTerms brandNameAgg = (ParsedStringTerms)brandAggregationMap.get("brandNameAgg");
                brandEntity.setName(brandNameAgg.getBuckets().get(0).getKeyAsString());
                return brandEntity;
            }).collect(Collectors.toList());
            responseVo.setBrands(brands);
        }

        // 2. 解析聚合结果集，获取分类
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms)aggregationMap.get("categoryIdAgg");
        List<? extends Terms.Bucket> categoryIdAggBuckets = categoryIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(categoryIdAggBuckets)){
            List<CategoryEntity> categories = categoryIdAggBuckets.stream().map(bucket -> { // {id: 225, name: 手机}
                CategoryEntity categoryEntity = new CategoryEntity();
                // 获取bucket的key，key就是分类的id
                Long categoryId = bucket.getKeyAsNumber().longValue();
                categoryEntity.setId(categoryId);
                // 解析分类名称的子聚合，获取分类名称
                ParsedStringTerms categoryNameAgg = bucket.getAggregations().get("categoryNameAgg");
                categoryEntity.setName(categoryNameAgg.getBuckets().get(0).getKeyAsString());
                return categoryEntity;
            }).collect(Collectors.toList());
            responseVo.setCategories(categories);
        }

        // 3. 解析聚合结果集，获取规格参数
        ParsedNested attrAgg = (ParsedNested)aggregationMap.get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> attrIdAggBuckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(attrIdAggBuckets)) {
            List<SearchResponseAttrVo> filters = attrIdAggBuckets.stream().map(bucket -> {
                SearchResponseAttrVo responseAttrVo = new SearchResponseAttrVo();
                // 规格参数id
                responseAttrVo.setAttrId(bucket.getKeyAsNumber().longValue());
                // 规格参数的名称
                ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");
                responseAttrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
                // 规格参数值
                ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attrValueAgg");
                List<? extends Terms.Bucket> attrValueAggBuckets = attrValueAgg.getBuckets();
                if (!CollectionUtils.isEmpty(attrValueAggBuckets)){
                    List<String> attrValues = attrValueAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                    responseAttrVo.setAttrValues(attrValues);
                }
                return responseAttrVo;
            }).collect(Collectors.toList());
            responseVo.setFilters(filters);
        }

        // 总命中的记录数
        return  responseVo;
    }

    private SearchSourceBuilder buildDsl(SearchParamVo paramVo) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //1构建bool查询
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        /**
         * GET /goods/_search
         * {
         *   "query": {
         *     "bool": {
         *       "must": [
         *         {
         *           "match": {
         *             "title": {
         *               "query": "华为",
         *               "operator": "and"
         *             }
         *           }
         *         }
         *       ]
         *     }
         *   }
         * }
         *   因为需求是and的关系 所有构建了bool查询 match查询title字段，操作费AND
         *   `match`类型查询，会把查询条件进行分词，然后进行查询,多个词条之间是or的关系
         *   需要更精确查找，这个关系变成`and`
         */
        //2匹配查询
        boolQuery.must( QueryBuilders.queryStringQuery(paramVo.getKeyword()).defaultOperator(Operator.AND).field("title").field("title.ngram").field("title.pinyin"));
//        boolQuery.must(QueryBuilders.matchQuery("title", paramVo.getKeyword()).operator(Operator.AND));
        //3 must多个写法
//        BoolQueryBuilder sub = QueryBuilders.boolQuery();
//        sub.must(QueryBuilders.termQuery("brandId", "1"));
//        sub.must(QueryBuilders.termQuery("brandId", "2"));
//        boolQuery.filter(sub);
        /**
         * GET /goods/_search
         * {
         *   "query": {
         *     "bool": {
         *       "must": [
         *       ],
         *       "filter": [
         *         {
         *           "terms": {
         *             "brandId": [
         *               "1",
         *               "2"
         *             ]
         *           }
         *         },
         *         {
         *           "terms": {
         *             "categoryId": [
         *               "1"
         *             ]
         *           }
         *         }
         *       ]
         *     }
         *   }
         * }
         * `term` 查询被用于精确值 匹配，这些精确值可能是数字、时间、布尔或者那些**未分词**的字符串。
         *  `terms` 查询和 term 查询一样，但它允许你指定多值进行匹配。如果这个字段包含了指定值中的任何一个值
         */
        //3品牌及分类过滤
        boolQuery.filter(QueryBuilders.termsQuery("brandId", paramVo.getBrandId()));
        boolQuery.filter(QueryBuilders.termsQuery("categoryId", paramVo.getCid()));
        /**
         * GET /goods/_search
         * {
         *   "query": {
         *     "bool": {
         *       "must": [
         *       ],
         *       "filter": [
         *         {
         *           "range": {
         *             "price": {
         *               "gte": 100,
         *               "lte": 500
         *             }
         *           }
         *         }
         *       ]
         *     }
         *   }
         * }
         */
        //4价格区间过滤
        BigDecimal priceFrom = paramVo.getPriceFrom();
        BigDecimal priceTo = paramVo.getPriceTo();
        if (priceFrom != null || priceTo != null) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
            if (priceFrom != null) {
                rangeQuery.gte(priceFrom);
            }
            if (priceTo != null) {
                rangeQuery.lte(priceTo);
            }
            boolQuery.filter(rangeQuery);
        }
        /**
         * GET /goods/_search
         * {
         *   "query": {
         *     "bool": {
         *       "must": [
         *       ],
         *       "filter": [
         *         {
         *           "term": {
         *             "store": "false"
         *           }
         *         }
         *       ]
         *     }
         *   }
         * }
         */
        //5是否有货
        Boolean store = paramVo.getStore();
        if (store != null) {
            boolQuery.filter(QueryBuilders.termQuery("store", store));
        }
        /**
         * GET /goods/_search
         * {
         *   "query": {
         *     "bool": {
         *       "must": [
         *       ],
         *       "filter": [
         *         {
         *           "nested": {
         *             "path": "attrs",
         *             "query": {
         *               "bool": {
         *                 "must": [
         *                   {
         *                     "term": {
         *                       "attrs.attrId": {
         *                         "value": "3"
         *                       }
         *                     }
         *                   },
         *                   {
         *                     "terms": {
         *                       "attrs.attrValue": [
         *                         "1G",
         *                         "16G"
         *                       ]
         *                     }
         *                   }
         *                 ]
         *               }
         *             }
         *           }
         *        }
         *     }
         *   }
         * }
         */
        // 6 规格参数的过滤 [{props=5:高通-麒麟},{6:骁龙865-硅谷1000}]
        List<String> props = paramVo.getProps();
        if (!CollectionUtils.isEmpty(props)) {
            props.forEach(prop -> {
                String[] attrs = StringUtils.split(prop, ":");
                if (attrs != null && attrs.length == 2) {
                    String attrId = attrs[0];//5
                    String attrValueString = attrs[1];//高通-麒麟
                    String[] attrValues = StringUtils.split(attrValueString, "-");

                    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                    boolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                    boolQueryBuilder.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                    //嵌套查询
                    boolQuery.filter(QueryBuilders.nestedQuery("attrs", boolQueryBuilder, ScoreMode.None));
                }
            });
        }
        sourceBuilder.query(boolQuery);
        /**
         * GET /goods/_search
         * {
         *   "query": {
         *   },
         *   "sort": [
         *     {
         *       "price": {
         *         "order": "desc"
         *       }
         *     }
         *   ]
         * }
         */
        // 7 构建排序 0-默认，得分降序；1-按价格升序；2-按价格降序；3-按创建时间降序；4-按销量降序
        Integer sort = paramVo.getSort();
        String field = "";
        SortOrder order = null;
        switch (sort) {
            case 1:
                field = "price";
                order = SortOrder.ASC;
                break;
            case 2:
                field = "price";
                order = SortOrder.DESC;
                break;
            case 3:
                field = "createTime";
                order = SortOrder.DESC;
                break;
            case 4:
                field = "sales";
                order = SortOrder.DESC;
                break;
            default:
                field = "_score";
                order = SortOrder.DESC;
                break;
        }
        sourceBuilder.sort(field, order);

        //8. 构建分页
        Integer pageNum = paramVo.getPageNum();
        Integer pageSize = paramVo.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);
        // 9. 构建高亮
//        sourceBuilder.highlighter(new HighlightBuilder().field("title").preTags("<font style='color:red'>").postTags("</font>"));
        sourceBuilder.highlighter(new HighlightBuilder().field("title").field("title.ngram").field("title.pinyin").preTags("<font style='color:red'>").postTags("</font>"));
        /**
         * GET /goods/_search
         * {
         *   "aggs": {
         *     "brandIdAgg": {
         *       "terms": {
         *         "field": "brandId"
         *       },
         *       "aggs": {
         *         "brandNameAgg": {
         *           "terms": {
         *             "field": "brandName"
         *           }
         *         }
         *       }
         *     },
         *     "categoryIdAgg": {
         *       "terms": {
         *         "field": "categoryId"
         *       },
         *       "aggs": {
         *         "categoryNameAgg": {
         *           "terms": {
         *             "field": "categoryName"
         *           }
         *         }
         *       }
         *     }
         *   }
         * }
         */
        //10聚合品牌和分类
        sourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
                .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName")));
        sourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));



        /**
         * GET /goods/_search
         * {
         *   "query": {
         *   },
         *   "aggs": {
         *     "attr_agg": {
         *       "nested": {
         *         "path": "attrs"
         *       },
         *       "aggs": {
         *         "attrIdAgg": {
         *           "terms": {
         *             "field": "attrs.attrId"
         *           },
         *           "aggs": {
         *             "attrNameAgg": {
         *               "terms": {
         *                 "field": "attrs.attrName"
         *               }
         *             },
         *             "attrValueAgg": {
         *               "terms": {
         *                 "field": "attrs.attrValue"
         *               }
         *             }
         *           }
         *         }
         *       }
         *     },
         *     "categoryIdAgg": {
         *       "terms": {
         *         "field": "categoryId"
         *       },
         *       "aggs": {
         *         "categoryNameAgg": {
         *           "terms": {
         *             "field": "categoryName"
         *           }
         *         }
         *       }
         *     }
         *   }
         * }
         */
        // 11构建规格参数的嵌套聚合
        sourceBuilder.aggregation(AggregationBuilders.nested("attr_agg","attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));
        // 6. 构建结果集过滤 参数1只显示那个字段，参数2排除那些字段
        sourceBuilder.fetchSource(new String[]{"skuId", "title", "price", "defaultImage"}, null);

        System.out.println(sourceBuilder.toString());
        return sourceBuilder;
    }





    @Test
    void contextLoads() {
        List<Item> list = new ArrayList<>();
        list.add(new Item(1L, "小米手机7", "手机", "小米", new BigDecimal(100)));
        list.add(new Item(2L, "坚果手机R1", "手机", "锤子", new BigDecimal(200)));
        list.add(new Item(3L, "华为META10", "手机", "华为", new BigDecimal(300)));
        list.add(new Item(4L, "小米Mix2S", "手机", "小米", new BigDecimal(400)));
        list.add(new Item(5L, "荣耀V10", "手机", "华为", new BigDecimal(500)));
        itemRepository.saveAll(list);
    }

    @Test
    void contextLoads2() {
        List<Item> list = this.itemRepository.findByPriceBetween(new BigDecimal(100), new BigDecimal(400));
        for (Item item : list) {
            System.out.println("item = " + item);
        }
    }


    @Test
    void contextLoads3() {
        NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder();
        QueryStringQueryBuilder queryString = QueryBuilders.queryStringQuery("xm");
        queryString.field("title").field("title.ngram").field("title.pinyin").defaultOperator(Operator.AND);
        searchQuery.withQuery(queryString);
        Iterable<Item> items = this.itemRepository.search(searchQuery.build());
        items.forEach(System.out::println);
    }

    @Test
    void contextLoads4() {
        NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder();
        QueryStringQueryBuilder queryString = QueryBuilders.queryStringQuery("xm");
        queryString.field("title").field("title.ngram").field("title.pinyin").defaultOperator(Operator.AND);
        searchQuery.withQuery(queryString);
        Iterable<Item> items = this.itemRepository.search(searchQuery.build());
        items.forEach(System.out::println);
    }


    @Test
    void contextLoads5() {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 不查询任何结果
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));
        // 1、添加一个新的聚合，聚合类型为terms，聚合名称为brands，聚合字段为brand
        queryBuilder.addAggregation(
                AggregationBuilders.terms("brands").field("brand"));
        // 2、查询,需要把结果强转为AggregatedPage类型
        AggregatedPageImpl<Item> aggPage = (AggregatedPageImpl<Item>) this.itemRepository.search(queryBuilder.build());
        // 3、解析
        // 3.1、从结果中取出名为brands的那个聚合，
        // 因为是利用String类型字段来进行的term聚合，所以结果要强转为StringTerm类型
        ParsedStringTerms agg = (ParsedStringTerms) aggPage.getAggregation("brands");

        List<ParsedTerms.ParsedBucket> buckets = (List<ParsedTerms.ParsedBucket>) agg.getBuckets();
        for (ParsedTerms.ParsedBucket bucket : buckets) {
            // 3.4、获取桶中的key，即品牌名称
            System.out.println(bucket.getKeyAsString());
            // 3.5、获取桶中的文档数量
            System.out.println(bucket.getDocCount());
        }

    }

}
