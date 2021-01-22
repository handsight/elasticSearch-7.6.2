package com.example.es;

import com.example.es.entity.SearchParamVo;
import com.example.es.response.SearchResponseVo;
import com.example.es.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EsApplication.class)
public class keyWord参考 {

    @Autowired
    private RestHighLevelClient restHighLevelClient;


    //https://gitee.com/moufangcai/learningEs/blob/master/es-demo/src/main/java/com/fangcai/es/practise2_qq_user_search/service/QqUserServiceImpl.java
    @Test
   public void test() throws IOException {
        SearchParamVo paramVo = new SearchParamVo();
        paramVo.setKeyword("注wo");

        SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, buildDsl2(paramVo));
        SearchResponse searchResponse = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        // 解析结果集
        SearchResponseVo responseVo = this.parseResult(searchResponse);
        responseVo.setPageNum(paramVo.getPageNum());
        responseVo.setPageSize(paramVo.getPageSize());
    }

    private SearchResponseVo parseResult(SearchResponse response) {

        SearchResponseVo responseVo=new SearchResponseVo();
        SearchHits hits = response.getHits();
        responseVo.setTotal(hits.getTotalHits().value);
        for(SearchHit hit : hits){
            System.out.println(hit.getSourceAsString());

            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField nameHighLight = highlightFields.get("title.title_pinyin");
            if (nameHighLight != null) {
                System.out.println(Arrays.toString(nameHighLight.getFragments()));
            }
        }

        return responseVo;

    }

    private SearchSourceBuilder buildDsl2(SearchParamVo paramVo) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 保证检索结果必须包含中文字
        String chineseStr = CommonUtil.getChineseStr(paramVo.getKeyword());
        if (StringUtils.isNotEmpty(chineseStr)) {
            boolQuery.filter(QueryBuilders.matchQuery("title.title_pinyin", chineseStr)
                    .analyzer("standard").minimumShouldMatch("100%"));
        }

        // 中文 + 全拼 + 首字母 混合检索
        boolQuery.must(QueryBuilders.matchPhrasePrefixQuery("title.title_pinyin",paramVo.getKeyword()));
        sourceBuilder.query(boolQuery);
        HighlightBuilder highlighter = new HighlightBuilder();
        highlighter.field("title.title_pinyin").preTags("<font style='color:red'>").postTags("</font>");
        // 使用该选项根本不会分割文本，高亮显示字段的全部内容
        highlighter.numOfFragments(0);
        sourceBuilder.highlighter(highlighter);
        System.out.println(sourceBuilder.toString());
        return sourceBuilder;
    }
}
