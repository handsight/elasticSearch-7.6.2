package com.example.es;

import com.example.es.entity.SearchParamVo;
import com.example.es.response.SearchResponseVo;
import com.example.es.util.ChineseToPinYinUtil;
import com.example.es.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
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
public class keyWordTest {
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    //https://blog.csdn.net/weixin_43834662/article/details/92601532
    @Test
   public void test() throws IOException {
        SearchParamVo paramVo = new SearchParamVo();


        /**
         * 数据 刘德华 刘斌 张三 李四 刘德志
         * 输入 刘    -->刘德华 刘斌 刘德志  李四
         * 输入 刘德  -->刘德华  刘德志
         * 输入 刘德华 -->刘德华
         * 输入 l         刘斌 李四 刘德华 刘德志
         * 输入 li        刘斌 李四 刘德华 刘德志
         * 输入 liu       刘斌 刘德华 刘德志
         * 输入 liud      刘德华  刘德志
         * 输入 liude     刘德华  刘德志
         * 输入 liudeh    刘德华
         * 输入 liudehu   刘德华
         * 输入 liudehua  刘德华
         * 输入 ld        刘德华  刘德志
         * 输入 ldh       刘德华
         * 输入 d         刘德华   刘德志
         * 输入 de        刘德华   刘德志
         * 输入 dehua     刘德华
         * 输入 dh        刘德华
         * 输入 h         刘德华
         * 输入 hua       刘德华
         * 输入 d志       刘德志
         * 输入 劉德      刘德华 刘德志
         * 输入 刘德h     刘德华
         *
         *
         *
         *数据 观注我 关注我 我关注 系统学ES就关注我
         * 输入 关zwo 关注我 系统学ES就关注我
         *
         *
         */
        //TODO 输入词去掉 ，；号不然报错，对字数做限制百度不超过38个
//        paramVo.setKeyword("中国馆共分为国家馆和地区馆两部分国家馆主体造型雄浑有力犹如华冠高耸天下粮仓地区馆平台基座汇聚人流寓意社泽神州富庶四方国家馆和地区馆的整体布局隐喻天地交泰万物咸亨中国馆以大红色为主要元素充分体现了中国自古以来以红色为主题的理念更能体现出喜庆的气氛让游客叹为观止");
//        paramVo.setKeyword("关zwo");
//        paramVo.setKeyword("劉德h");
        paramVo.setKeyword("de");
//        paramVo.setKeyword("liud");
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
            System.out.println("搜索结果:"+hit.getSourceAsString());
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField ik = highlightFields.get("title.ik");
            if (ik != null) {
                System.out.println(Arrays.toString(ik.getFragments()));
            }

            HighlightField ngram = highlightFields.get("title.ngram");
            if (ngram != null) {
                System.out.println(Arrays.toString(ngram.getFragments()));
            }

            HighlightField fullPinyin = highlightFields.get("title.full_pinyin");
            if (fullPinyin != null) {
                System.out.println(Arrays.toString(fullPinyin.getFragments()));
            }

            HighlightField simplePinyin = highlightFields.get("title.simple_pinyin");
            if (simplePinyin != null) {
                System.out.println(Arrays.toString(simplePinyin.getFragments()));
            }

        }

        return responseVo;

    }

    private SearchSourceBuilder buildDsl2(SearchParamVo paramVo) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        QueryBuilder boolQuery = chineseAndPinYinSearch(paramVo.getKeyword());
        sourceBuilder.query(boolQuery);

        HighlightBuilder highlighter = new HighlightBuilder();
        highlighter.field("title.custom_analyzer").field("title.ik").field("title.ngram").field("title.edge_ngram").field("title.full_pinyin").field("title.simple_pinyin").preTags("<font style='color:red'>").postTags("</font>");
        // 使用该选项根本不会分割文本，高亮显示字段的全部内容
        highlighter.numOfFragments(0);
        sourceBuilder.highlighter(highlighter).highlighter();
        System.out.println(sourceBuilder.toString());
        return sourceBuilder;
    }


    //中文、拼音混合搜索
    private QueryBuilder chineseAndPinYinSearch(String words){

        //使用dis_max直接取多个query中，分数最高的那一个query的分数即可
        DisMaxQueryBuilder disMaxQueryBuilder= QueryBuilders.disMaxQuery();

        QueryBuilder customSearchBuilder=QueryBuilders.matchQuery("title.custom_analyzer",words).analyzer("customIndexAnalyzer").boost(6f);


        /**
         * 纯中文搜索，不做拼音转换,采用edge_ngram分词(优先级最高)
         * 权重* 5
         */
        QueryBuilder normSearchBuilder=QueryBuilders.matchQuery("title.ngram",words).analyzer("ngramSearchAnalyzer").boost(5f);


        /**
         * 纯中文搜索，不做拼音转换,采用ngram分词(优先级最高)
         * 权重* 4
         */
        QueryBuilder edgeNormSearchBuilder=QueryBuilders.matchQuery("title.edge_ngram",words).analyzer("edgeNgramSearchAnalyzer").boost(4f);

        /**
         * 拼音简写搜索
         * 1、分析key，转换为简写  case:  南京东路==>njdl，南京dl==>njdl，njdl==>njdl
         * 2、搜索匹配，必须完整匹配简写词干
         * 3、如果有中文前缀，则排序优先
         * 权重*1
         */
        String firstChar = ChineseToPinYinUtil.toFirstCharLowCase(words);
        TermQueryBuilder pingYinSampleQueryBuilder = QueryBuilders.termQuery("title.simple_pinyin", firstChar);

        /**
         * 拼音简写包含匹配，如 njdl可以查出 "城市公牛 南京东路店"，虽然非南京东路开头
         * 权重*0.8
         */
        QueryBuilder  pingYinSampleContainQueryBuilder=null;
        if(firstChar.length()>1){
            pingYinSampleContainQueryBuilder=QueryBuilders.wildcardQuery("title.simple_pinyin", "*"+firstChar+"*").boost(0.8f);
        }

        /**
         * 拼音全拼搜索
         * 1、分析key，获取拼音词干   case :  南京东路==>[nan,jing,dong,lu]，南京donglu==>[nan,jing,dong,lu]
         * 2、搜索查询，必须匹配所有拼音词，如南京东路，则nan,jing,dong,lu四个词干必须完全匹配
         * 3、如果有中文前缀，则排序优先
         * 权重*1
         */
        BoolQueryBuilder pingYinFullQueryBuilder=null;
        if(words.length()>1&&words.length()<10){
            pingYinFullQueryBuilder = QueryBuilders.boolQuery();
            pingYinFullQueryBuilder.must(QueryBuilders.matchPhraseQuery("title.full_pinyin", words).analyzer("fullPinyinSearchAnalyzer"));
            String chineseStr = CommonUtil.getChineseStr(words);
            if (StringUtils.isNotEmpty(chineseStr)) {
                //去掉 str 中的非中文字 这里应该用标准分词器过滤中文的同义词
                MatchQueryBuilder fullPinyinSearchAnalyzer = QueryBuilders.matchQuery("title.full_pinyin", chineseStr)
                        .analyzer("standard").minimumShouldMatch("100%");
                pingYinFullQueryBuilder.filter(fullPinyinSearchAnalyzer);
            }
        }

        /**
         * 完整包含关键字查询(优先级最低，只有以上四种方式查询无结果时才考虑）
         * 权重*0.8
         */
        QueryBuilder containSearchBuilder=QueryBuilders.matchQuery("title", words).analyzer("ikSearchAnalyzer").minimumShouldMatch("100%");

        disMaxQueryBuilder
                .add(customSearchBuilder);
//                .add(normSearchBuilder)
//                .add(edgeNormSearchBuilder)
//                .add(pingYinSampleQueryBuilder)
//                .add(containSearchBuilder);

        //以下两个对性能有一定的影响，故作此判定，单个字符不执行此类搜索
//        if(pingYinFullQueryBuilder!=null){
//            disMaxQueryBuilder.add(pingYinFullQueryBuilder);
//        }
//        if(pingYinSampleContainQueryBuilder!=null){
//            disMaxQueryBuilder.add(pingYinSampleContainQueryBuilder);
//        }
        return disMaxQueryBuilder;
    }
}
