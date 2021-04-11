package com.ego.client;

import com.ego.EgoSearchService;
import com.ego.GoodsRepository;
import com.ego.entity.Goods;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EgoSearchService.class)
public class ElasticsearchTest {

    @Resource
    private GoodsRepository goodsRepository;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Test
    public void createIndex() {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(Goods.class);
        indexOperations.putMapping(indexOperations.createMapping(Goods.class));
    }
}
