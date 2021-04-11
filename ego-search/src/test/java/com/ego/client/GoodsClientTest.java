package com.ego.client;

import com.ego.common.entity.PageResult;
import com.ego.entity.SpuBO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * Created by 不要剁我爪 on 2021/4/2.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class GoodsClientTest {

    @Resource
    private GoodsClient goodsClient;

    @Test
    public void page() {
        PageResult<SpuBO> page = goodsClient.page(true, 1, 10).getBody();
        System.out.println(page);
    }
}
