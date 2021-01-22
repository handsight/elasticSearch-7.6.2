package com.example.es.entity;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.math.BigDecimal;
import java.util.List;

public interface ItemRepository extends ElasticsearchRepository<Item,Long> {

    List<Item> findByPriceBetween(BigDecimal v1, BigDecimal v2);
}
