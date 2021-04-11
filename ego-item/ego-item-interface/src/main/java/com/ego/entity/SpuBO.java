package com.ego.entity;

import lombok.Data;

import java.util.List;

@Data
public class SpuBO extends Spu {
    /**
     * 商品分类名称
     */
    private String categoryNames;
    /**
     * 品牌名称
     */
    private String brandName;
    private List<Sku> skus;
    private SpuDetail spuDetail;

    public SpuBO() {
    }
}
