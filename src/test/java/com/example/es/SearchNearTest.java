package com.example.es;

import com.example.es.entity.ESUserLocationSearch;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.range.GeoDistanceAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.range.ParsedGeoDistance;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = EsApplication.class)
public class SearchNearTest {

    /**
     * https://blog.csdn.net/weixin_38399962/article/details/108128178
     */
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 通过指定点搜索附近的人 , 要求可以过滤年龄, 结果按照距离进行排序, 并且展示她/他距离你多远
     *
     * @throws IOException
     */
    @Test
    public void test() throws IOException {
        ESUserLocationSearch locationSearch = new ESUserLocationSearch();
        //纬度
        locationSearch.setLat(new Double("12"));
        //经度
        locationSearch.setLon(new Double("24"));
        //搜索范围(单位千米)
        locationSearch.setDistance(1000);
        SearchRequest searchRequest = new SearchRequest(new String[]{"test"}, buildDsl(locationSearch));
        SearchResponse searchResponse = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        parseResult(searchResponse);
    }

    private SearchSourceBuilder buildDsl(ESUserLocationSearch locationSearch) {
        Integer distance = locationSearch.getDistance();
        Double lat = locationSearch.getLat();
        Double lon = locationSearch.getLon();
        Integer ageGte = locationSearch.getAgeGte();
        Integer ageLt = locationSearch.getAgeLt();

        // 先构建查询条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 距离搜索条件
        if (distance != null && lat != null && lon != null) {
            boolQueryBuilder.filter(QueryBuilders.geoDistanceQuery("location")
                    .distance(distance, DistanceUnit.KILOMETERS)
                    .point(lat, lon)
            );
        }

        // 过滤年龄条件
        if (ageGte != null && ageLt != null) {
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("age").gte(ageGte).lt(ageLt));
        }
        // 地理位置排序
        GeoDistanceSortBuilder sortBuilder = SortBuilders.geoDistanceSort("location", lat, lon).geoDistance(GeoDistance.PLANE).unit(DistanceUnit.KILOMETERS);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQueryBuilder);
        sourceBuilder.sort(sortBuilder);
        sourceBuilder.from(0);
        sourceBuilder.size(10);
        System.out.println(sourceBuilder.toString());
        //arc ,最慢但是最精确。
        //plane:这种方式是将地球看成是平面，所以这种方式相对于arc快一些，但是不是很精确。
        //sloppy_arc:使用 Haversine formula 来计算距离。它比 arc 计算方式快 4 到 5 倍，并且距离精度达 99.9%。这也是默认的计算方式
        return sourceBuilder;
    }


    private void parseResult(SearchResponse response) {
        System.out.println("总共有" + response.getHits().getTotalHits().value + "人");
        for (SearchHit hit : response.getHits()) {
            System.out.println("搜索结果:" + hit.getSourceAsString());
            System.out.println("距离:" + hit.getSortValues()[0] + "千米");
        }
    }


    private void parseResult2(SearchResponse response) {
        System.out.println("附近总共有" + response.getHits().getTotalHits().value + "个酒店");
        Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();
        ParsedGeoDistance geoAggs = (ParsedGeoDistance) aggregationMap.get("myaggs");
        for (Range.Bucket bucket : geoAggs.getBuckets()) {
            System.out.println(bucket.getKeyAsString()+"米有"+bucket.getDocCount()+"个酒店");

        }

        
    }


    /**
     * 举例我当前坐标的几个范围内的酒店的数量，比如说举例我0~100m有几个酒店，100m~300m有几个酒店，300m以上有几个酒店
     *
     * @throws IOException
     */
    @Test
    public void test2() throws IOException {
        ESUserLocationSearch locationSearch = new ESUserLocationSearch();
        //纬度
        locationSearch.setLat(new Double("12"));
        //经度
        locationSearch.setLon(new Double("24"));

        SearchRequest searchRequest = new SearchRequest(new String[]{"test"}, buildDsl2(locationSearch));
        SearchResponse searchResponse = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        parseResult2(searchResponse);
    }


    private SearchSourceBuilder buildDsl2(ESUserLocationSearch locationSearch) {
        GeoPoint geoPoint = new GeoPoint(locationSearch.getLat(), locationSearch.getLon());
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        GeoDistanceAggregationBuilder distanceAggregationBuilder = AggregationBuilders.geoDistance("myaggs", geoPoint);
        distanceAggregationBuilder.field("location");
        distanceAggregationBuilder.unit(DistanceUnit.KILOMETERS);
        distanceAggregationBuilder.distanceType(GeoDistance.PLANE);
        //2种添加方式
        distanceAggregationBuilder.addRange(0,100);
        distanceAggregationBuilder.addRange(100,300);
        GeoDistanceAggregationBuilder.Range range1=new GeoDistanceAggregationBuilder.Range(null,new Double("300"),null);
        distanceAggregationBuilder.addRange(range1);
        sourceBuilder.size(0);
        sourceBuilder.aggregation(distanceAggregationBuilder);
        System.out.println(sourceBuilder.toString());
        return sourceBuilder;
    }


}
