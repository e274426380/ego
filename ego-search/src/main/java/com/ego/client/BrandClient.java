package com.ego.client;

import com.ego.api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ItemService")
public interface BrandClient extends BrandApi {
}
