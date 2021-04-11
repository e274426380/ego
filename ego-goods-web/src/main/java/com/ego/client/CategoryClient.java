package com.ego.client;

import com.ego.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("ItemService")
public interface CategoryClient extends CategoryApi {
}
