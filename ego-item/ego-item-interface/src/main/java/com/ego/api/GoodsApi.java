package com.ego.api;

import com.ego.common.entity.PageResult;
import com.ego.entity.SpuBO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/spu")
public interface GoodsApi {

    @GetMapping("/pageForEs")
    public ResponseEntity<PageResult<SpuBO>> page(
            @RequestParam(value = "saleable",required = false)Boolean saleable,
            @RequestParam("page")Integer page,
            @RequestParam("rows")Integer rows
    );
    /**
     * 根据id查询商品
     * @param id
     * @return
     */
    @GetMapping("/spubo/{id}")
    SpuBO queryGoodsById(@PathVariable("id") Long id);
}
