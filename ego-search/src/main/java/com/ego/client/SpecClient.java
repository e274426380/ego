package com.ego.client;

import com.ego.api.SpecApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "ItemService")
public interface SpecClient extends SpecApi {
}
