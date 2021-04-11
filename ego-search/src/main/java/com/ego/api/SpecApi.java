package com.ego.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/spec")
public interface SpecApi {

    /**
     * 查询商品分类对应的规格参数模板
     * @param cid
     * @return
     */
    @GetMapping("/{cid}")
    public ResponseEntity<String> querySpecByCid(@PathVariable("cid") Long cid);

}
