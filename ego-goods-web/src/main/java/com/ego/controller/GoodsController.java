package com.ego.controller;

import com.ego.service.GoodsHtmlService;
import com.ego.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("item")
public class GoodsController {
    @Autowired
    private GoodsService goodsService;
    @Autowired
    private GoodsHtmlService goodsHtmlService;
    /**
     * 跳转到商品详情页
     * @param model
     * @param id
     * @return
     */
    @GetMapping("{id}.html")
    public String toItemPage(Model model, @PathVariable("id")Long id){
        //加载数据
        Map<String, Object> modelMap = this.goodsService.loadModel(id);
        //把数据放入模型中
        model.addAllAttributes(modelMap);
        //生成静态页面
        this.goodsHtmlService.asyncExecute(id);
        return "item";
    }
}
