package com.example.es;


import com.example.es.strategy.Factory;
import com.example.es.strategy.Handler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = EsApplication.class)
public class StrategyTest {


    //https://www.cnblogs.com/cb1186512739/p/14264616.html
    @Test
    public void test() throws IOException {
        Handler handler = Factory.getInvokeStrategy("B");
        handler.test(null);


    }

}
