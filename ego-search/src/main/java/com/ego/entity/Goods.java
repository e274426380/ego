package com.ego.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Builder
@Data
@Document(indexName = "goods", shards = 1, replicas = 0)
public class Goods {
    @Id
    private Long id; // spuId
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String all; // 所有需要被搜索的信息，包含标题，分类，甚至品牌
    @Field(type = FieldType.Keyword, index = false)
    private String subTitle;// 卖点
    @Field(type = FieldType.Long,index = false)
    private Long brandId;// 品牌id
    @Field(type = FieldType.Long,index = false)
    private Long cid1;// 1级分类id
    @Field(type = FieldType.Long,index = false)
    private Long cid2;// 2级分类id
    @Field(type = FieldType.Long,index = false)
    private Long cid3;// 3级分类id
    @Field(type = FieldType.Date,format = DateFormat.date_time)
    private Date createTime;// 创建时间
    @Field(type = FieldType.Long,index = false)
    private List<Long> price;// 每个sku的价格，方便排序
    @Field(type = FieldType.Keyword, index = false)
    private String skus;// sku信息的json结构
    @Field(type = FieldType.Object,index = true)
    private Map<String, Object> specs;// 可搜索的规格参数，key是参数名，值是参数值
}
