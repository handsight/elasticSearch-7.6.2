package com.example.es;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = EsApplication.class)
public class SuggestTest {

    /**
     * https://blog.csdn.net/zmx729618/article/details/80415984
     */
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void test() throws IOException {
        String keyword = "中华人民共和国";
        SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, buildDsl(keyword));
        SearchResponse searchResponse = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

    }
    private SearchSourceBuilder buildDsl(String keyword)  {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.matchQuery("title.title_suggest", keyword).operator(Operator.AND));
        sourceBuilder.query(boolQuery);
        System.out.println(sourceBuilder.toString());
        return sourceBuilder;
    }
}
