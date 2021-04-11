package com.ego.service;

import com.ego.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;

@Service
public class GoodsHtmlService {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private TemplateEngine templateEngine;

    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsHtmlService.class);

    /**
     * 创建html页面
     *
     * @param spuId
     * @throws Exception
     */
    public void createHtml(Long spuId) {
        PrintWriter writer = null;

        //获取页面数据
        Map<String,Object> spuMap = this.goodsService.loadModel(spuId);
        //创建Thymeleaf上下文对象
        Context context = new Context();
        //把数据放入上下文对象
        context.setVariables(spuMap);

        //创建输出流
        //windows地址格式：    D:\\nginx-1.12.2\\html\\item\\
        //Linux:/usr/local/var/www/item/
        File file = new File("D:\\1JavaTools\\nginx-1.17.6\\html\\item\\"+spuId+".html");
        try {
            writer = new PrintWriter(file);
            //执行页面静态化方法
            templateEngine.process("item",context,writer);
        } catch (FileNotFoundException e) {
            LOGGER.error("页面静态化出错：{}"+e,spuId);
        }finally {
            if (writer != null){
                writer.close();
            }
        }
    }

    /**
     * 新建线程处理页面静态化
     * @param spuId
     */
    public void asyncExecute(Long spuId) {
        ThreadUtils.execute(() ->createHtml(spuId));
    }


}
