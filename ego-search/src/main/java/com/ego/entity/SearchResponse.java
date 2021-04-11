package com.ego.entity;

import com.ego.common.entity.PageResult;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SearchResponse extends PageResult<Goods> {

    private List<Category> categories;// 分类过滤条件

    private List<Brand> brands;// 品牌过滤条件

    private List<Map<String,Object>> specs; // 规格参数过滤条件

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Brand> getBrands() {
        return brands;
    }

    public void setBrands(List<Brand> brands) {
        this.brands = brands;
    }

    public List<Map<String, Object>> getSpecs() {
        return specs;
    }

    public void setSpecs(List<Map<String, Object>> specs) {
        this.specs = specs;
    }

    public SearchResponse() {
        super();
    }

    public SearchResponse(List<Goods> items, Long total, Long page, Integer rows, List<Category> categories, List<Brand> brands) {
        super(total, page, items);
        this.categories = categories;
        this.brands = brands;
    }
    @Builder
    public SearchResponse(List<Goods> items, Long total, Long page, Integer rows, List<Category> categories, List<Brand> brands, List<Map<String, Object>> specs) {
        super(total, page, items);
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }
}
