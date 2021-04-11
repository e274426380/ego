package com.ego.api;

import com.ego.entity.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/category")
public interface CategoryApi {
    @GetMapping("/list/ids")
    public ResponseEntity<List<Category>> queryListByIds(@RequestParam("ids") List<Long> ids);
    /**
     *根据ids查询分类列表
     * @param ids
     * @return
     */
    @GetMapping("lists")
    public ResponseEntity<List<Category>> queryCategoryByIds(@RequestParam("ids")List<Long> ids);
}
