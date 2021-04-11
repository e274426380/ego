package com.ego.client;

import com.ego.api.BrandApi;
import com.ego.entity.Brand;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient("ItemService")
public interface BrandClient extends BrandApi {

}
