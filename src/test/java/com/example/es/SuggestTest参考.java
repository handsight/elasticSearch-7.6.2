package com.example.es;

import org.assertj.core.util.Lists;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.elasticsearch.search.suggest.completion.FuzzyOptions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = EsApplication.class)
public class SuggestTest参考 {

    /**
     * https://blog.csdn.net/baifanwudi/article/details/88662561
     */
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void test() throws IOException {
        String keyword = "中华人民共和国";
        SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, buildDsl(keyword));
        SearchResponse searchResponse = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        Suggest suggestions = searchResponse.getSuggest();
        List<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> results = suggestions.getSuggestion("title_suggest_A").getEntries();

        List<String> total = Lists.newArrayList();
        for (Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> op : results) {
            List<? extends Suggest.Suggestion.Entry.Option> options = op.getOptions();
            for (Suggest.Suggestion.Entry.Option pp : options) {
                System.out.println("猜想值："+pp.getText());
                total.add(pp.getText().toString());
            }
        }
        System.out.println(total.toString());
    }
    private SearchSourceBuilder buildDsl(String keyword)  {
        FuzzyOptions.Builder builder = FuzzyOptions.builder();
        builder.setFuzzyMinLength(1);
        builder.setFuzziness(Fuzziness.AUTO);
        FuzzyOptions fuzzy = builder.build();
        CompletionSuggestionBuilder stationName = SuggestBuilders.completionSuggestion("title_suggest").prefix(keyword,fuzzy);
        stationName.skipDuplicates(true);
        stationName.size(5);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("title_suggest_A", stationName);
        sourceBuilder.suggest(suggestBuilder);
        System.out.println(sourceBuilder.toString());
        return sourceBuilder;
    }
}
