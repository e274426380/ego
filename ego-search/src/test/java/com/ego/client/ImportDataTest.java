package com.ego.client;

import com.ego.EgoSearchService;
import com.ego.GoodsRepository;
import com.ego.entity.Goods;
import com.ego.entity.SpuBO;
import com.ego.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;
@RunWith(SpringRunner.class)
@SpringBootTest(classes = EgoSearchService.class)
public class ImportDataTest {
    @Resource
    private GoodsClient goodsClient;
    @Resource
    private SearchService searchService;
    @Resource
    private GoodsRepository goodsRepository;
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Test
    public void createIndex() {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(Goods.class);
        indexOperations.putMapping(indexOperations.createMapping(Goods.class));
    }
    @Test
    public void ImportDataFromMysql() {
        int size=100;
        int page=1;
        do {
            //1.循环分批次远程读取mysql数据
            List<SpuBO> spuBOList = goodsClient.page(true, page++, size).getBody().getItems();
            size=spuBOList.size();

            //2.将spu转换成goods
            List<Goods> goodsList = spuBOList.stream().map(spuBO -> {
                // 返回  goods
                return searchService.buildGoods(spuBO);
            }).collect(Collectors.toList());
            //3.批量将goods存入es
            goodsRepository.saveAll(goodsList);
        } while (size == 100);
    }
}
