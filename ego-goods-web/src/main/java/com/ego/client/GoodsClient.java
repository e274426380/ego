package com.ego.client;

import com.ego.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "ItemService")
public interface GoodsClient extends GoodsApi {
}
//@FeignClient("ItemService")
//@RequestMapping("/goods")
//public interface GoodsClient {
//
//    @GetMapping("/spu/pageForEs")
//    public ResponseEntity<PageResult<SpuBO>> page(
//            @RequestParam(value = "saleable",required = false)Boolean saleable,
//            @RequestParam("page")Integer page,
//            @RequestParam("rows")Integer rows
//    );
//
//}
