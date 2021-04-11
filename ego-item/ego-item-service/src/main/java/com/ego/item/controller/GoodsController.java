package com.ego.item.controller;

import com.ego.common.entity.PageResult;
import com.ego.entity.Sku;

import com.ego.entity.SpuBO;
import com.ego.entity.SpuDetail;
import com.ego.item.service.GoodsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/spu")
public class GoodsController {

    @Resource
    private GoodsService goodsService;

    /**
     * 分页查询SPU
     * @param page
     * @param rows
     * @param key
     * @return
     */
    @GetMapping("/page")
    public ResponseEntity<PageResult<SpuBO>> page(
            @RequestParam("key")String key,
            @RequestParam(value = "saleable",required = true)Boolean saleable,
            @RequestParam("page")Integer page,
            @RequestParam("rows")Integer rows
    ){
        return ResponseEntity.ok(goodsService.page(key, saleable, page, rows));
    }

    /**
     * 新增商品
     * @param spu
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBO spu) {
        goodsService.save(spu);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/detail/{id}")
    public ResponseEntity<SpuDetail> querySpuDetailById(@PathVariable("id") Long id) {
        SpuDetail detail = this.goodsService.querySpuDetailById(id);
        if (detail == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(detail);
    }
    /**
     * 根据id查询商品
     * @param id
     * @return
     */
    @GetMapping("/spubo/{id}")
    public ResponseEntity<SpuBO> queryGoodsById(@PathVariable("id") Long id){
        SpuBO spuBo=this.goodsService.queryGoodsById(id);
        if (spuBo == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(spuBo);
    }
    @GetMapping("/skus")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@RequestParam("spuId") Long spuId) {
        List<Sku> skus = this.goodsService.querySkuBySpuId(spuId);
        if (skus == null || skus.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(skus);
    }

    /**
     * 新增商品
     * @param spu
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBO spu) {
        try {
            this.goodsService.update(spu);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /**
     * 逻辑删除商品
     * @param id
     * @return
     */
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam("id") Long id) {
        goodsService.deleteSpuById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/pageForEs")
    public ResponseEntity<PageResult<SpuBO>> page(
            @RequestParam(value = "saleable", required = false) Boolean saleable,
            @RequestParam("page") Integer page,
            @RequestParam("rows") Integer rows
    ) {
        return ResponseEntity.ok(goodsService.page(saleable, page, rows));
    }
}
