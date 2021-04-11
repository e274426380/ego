package com.ego.service;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.ego.EgoException;
import com.ego.ExceptionEnum;
import com.ego.GoodsRepository;
import com.ego.client.BrandClient;
import com.ego.client.CategoryClient;
import com.ego.client.SpecClient;
import com.ego.common.entity.PageResult;
import com.ego.common.utils.JsonUtils;
import com.ego.common.utils.NumberUtils;
import com.ego.entity.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.metrics.ParsedStats;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;


import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchService {

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Resource
    private GoodsRepository goodsRepository;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private BrandClient brandClient;

    @Resource
    private CategoryClient categoryClient;
    @Resource
    private SpecClient specClient;
    /**
     * 将spuBo转换成Goods，用于存储到es
     * @param spuBO
     * @return
     */
    public Goods buildGoods(SpuBO spuBO)  {

        Goods result = null;
        try {
            List<Sku> skusList = spuBO.getSkus();
            //将List转成json
            String skus = objectMapper.writeValueAsString(skusList);

            //只获取sku中的price，并装到一个新的list中,List<Sku>->List<Long>
            List<Long> prices = skusList.stream().map(sku -> sku.getPrice()).collect(Collectors.toList());

            //解析规格参数，将可搜索的规格参数存入specs中
            Map<String, Object> specs = new HashMap<>();

            //获取到字符串 规格参数
            String specifications = spuBO.getSpuDetail().getSpecifications();
            //将 字符串 规格参数 -> 对象
            List<Map<String,Object>> specList = objectMapper.readValue(specifications, new TypeReference<List<Map<String, Object>>>() {});

            //便利specList，解析需要搜索的规格参数
            specList.forEach(map->{
                List<Map<String,Object>> params = (List<Map<String, Object>>) map.get("params");
                params.forEach(param->{
                    //只要可搜索的规格参数
                    if ((Boolean) param.get("searchable")) {
                        Object v = param.get("v");
                        //有v就存v，没有就存options
                        if (v != null) {
                            specs.put(param.get("k").toString(), v);
                        }
                        else {
                            specs.put(param.get("k").toString(), param.get("options"));
                        }
                    }
                });
            });
            result = Goods.builder()
                    .price(prices)
                    .specs(specs)
                    .skus(skus)
                    .subTitle(spuBO.getSubTitle())
                    .all(spuBO.getTitle() + " " + spuBO.getCategoryNames() + " " + spuBO.getBrandName())
                    .createTime(spuBO.getCreateTime())
                    .brandId(spuBO.getBrandId())
                    .cid1(spuBO.getCid1())
                    .cid2(spuBO.getCid2())
                    .cid3(spuBO.getCid3())
                    .id(spuBO.getId())
                    .build();
        } catch (Exception e) {
            EgoException.error(log, ExceptionEnum.BUILD_GOODS_EXCEPTION,e);
        }
        return result;
    }

    public PageResult<Goods> search(SearchRequest request) {
        String key = request.getKey();
        PageResult<Goods> result = new PageResult<>();
        // 判断是否有搜索条件，如果没有，直接返回null。不允许搜索全部商品
        if (StringUtils.isBlank(key)) {
            return null;
        }

        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        // 1、对key进行全文检索查询
        queryBuilder.withQuery(QueryBuilders.matchQuery("all", key).operator(Operator.AND));

        // 2、通过sourceFilter设置返回的结果字段,我们只需要id、skus、subTitle
        queryBuilder.withSourceFilter(new FetchSourceFilter(
                new String[]{"id","skus","subTitle"}, null));

        // 3、分页
        // 准备分页参数
        int page = request.getPage();
        int size = request.getSize();
        queryBuilder.withPageable(PageRequest.of(page - 1, size));
        //4、排序
        String sortBy=request.getSortBy();
        Boolean desc=request.getDescending();
        if(StringUtils.isNotBlank(sortBy)){
            //如果不为空，则进行排序
            queryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(desc? SortOrder.DESC:SortOrder.ASC));
        }
        // 5、查询，获取结果
        try {
            SearchHits<Goods> search = elasticsearchRestTemplate.search(queryBuilder.build(), Goods.class);

            //将搜索返回的SearchHit对象 转换成 Goods对象
            List<Goods> items = search.getSearchHits()
                    .stream()
                    .map(goodsSearchHit -> goodsSearchHit.getContent())
                    .collect(Collectors.toList());

            result.setItems(items);
            result.setTotalPage((search.getTotalHits()/size)+1);
//        result.setSize(size);
            result.setTotal(search.getTotalHits());
        }catch (Exception e){
            System.out.println(e);
        }


        return result;
    }
    /**
     * 商品全文检索
     * @param searchRequest
     * @return
     */
    public SearchResponse page(SearchRequest searchRequest) {
        SearchResponse.SearchResponseBuilder builder = SearchResponse.builder();
        try {
            String key = searchRequest.getKey();
            if (StringUtils.isEmpty(key)) {
                EgoException.error(log,ExceptionEnum.BAD_KEY_REQUEST);
            }
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            //添加搜索条件

            QueryBuilder basicQuery = buildBaiscQuery(searchRequest);
            queryBuilder.withQuery(basicQuery);
            //添加分页条件
            queryBuilder.withPageable(PageRequest.of(searchRequest.getPage()-1,searchRequest.getSize()));
            //添加聚合条件
            //cid3,brandId(词条聚合)
            queryBuilder.addAggregation(AggregationBuilders.terms("分类").field("cid3"));
            queryBuilder.addAggregation(AggregationBuilders.terms("品牌").field("brandId"));

            //执行查询
            SearchHits<Goods> searchHits = elasticsearchRestTemplate.search(queryBuilder.build(), Goods.class);

            //将searchHits转成List<Goods>
            List<Goods> items = searchHits.getSearchHits().stream()
                    .map(searchHit -> searchHit.getContent())
                    .collect(Collectors.toList());

            builder
                    .items(items)
                    .rows(searchRequest.getSize())
                    .page(searchRequest.getPage().longValue())
                    .total(searchHits.getTotalHits());

            List<Category> categories = getCategoryAgg(searchHits);
            builder.categories(categories);

            List<Brand> brands = getBrandAgg(searchHits);
            builder.brands(brands);
            //根据第一个类别查询其他过滤条件
            if (CollectionUtils.isNotEmpty(categories)) {
                List<Map<String,Object>> specs = getSpecs(categories,basicQuery);
                builder.specs(specs);
            }
        } catch (Exception e) {
            EgoException.error(log,ExceptionEnum.GOODS_PAGE_ERROR,e);
        }
        return builder.build();
    }

    /**
     * 获取其他过滤条件
     * @param categories
     * @param basicQuery
     * @return
     */
    private List<Map<String, Object>> getSpecs(List<Category> categories, QueryBuilder basicQuery) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            //1.根据第一个分类，查询出对应的可搜索的过滤条件。
            //map<参数名字,单位>存储数字型参数
            Map<String, String> numberSpecMap = new HashMap<>();
            //List<参数名字>存储字符型参数
            List<String> strSpecList = new ArrayList<>();
            identifySpec(numberSpecMap, strSpecList,categories);
            //2.字符型采用词条聚合
            NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
            nativeSearchQueryBuilder.withQuery(basicQuery);
            strSpecList.forEach(name->{
                nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs."+name+".keyword"));
            });
            //查询出每个数字型参数的间隔
            Map<String,Double> numberSpecInterval = getNumberSpecInterval(numberSpecMap,basicQuery);
            //3.数字型采用阶梯聚合
            numberSpecMap.forEach((name,unit)->{
                //只有间隔>0，才聚合查询
                if(numberSpecInterval.get(name)>0)
                {
                    nativeSearchQueryBuilder.addAggregation(AggregationBuilders.histogram(name).field("specs."+name).interval(numberSpecInterval.get(name)));
                }
            });
            //4.开始聚合查询
            SearchHits<Goods> searchHits = elasticsearchRestTemplate.search(nativeSearchQueryBuilder.build(), Goods.class);
            //5.解析字符聚合结果
            strSpecList.forEach(name->{
                Map<String, Object> map = new HashMap<>();
                map.put("name",name);
                ParsedStringTerms agg = searchHits.getAggregations().get(name);

                List<String> options = agg.getBuckets().stream()
                        .map(bucket -> bucket.getKey().toString())
                        .filter(k->StringUtils.isNotEmpty(k))
                        .collect(Collectors.toList());
                map.put("options",options);
                result.add(map);
            });
            //6.解析数字聚合结果
            numberSpecMap.forEach((name,unit)->{
                //只获取间隔>0的数据
                if(numberSpecInterval.get(name)>0){
                    Map<String, Object> map = new HashMap<>();
                    map.put("name",name);

                    ParsedHistogram agg = searchHits.getAggregations().get(name);

                    List<String> options = agg.getBuckets().stream()
                            .map(bucket -> {
                                Double begin = (Double) bucket.getKey();
                                Double end = begin + numberSpecInterval.get(name);
                                //判断是否整型
                                if(begin.intValue() == begin ){
                                    return begin.intValue()+"_"+end;
                                }
                                else {

                                    return NumberUtils.scale(begin,1)+"_"+NumberUtils.scale(end,1);
                                }
                            })
                            .filter(k->StringUtils.isNotEmpty(k))
                            .collect(Collectors.toList());
                    map.put("options",options);
                    map.put("unit",unit);
                    result.add(map);
                }
            });
        } catch (Exception e) {
            EgoException.error(log,ExceptionEnum.QUERY_OTHER_AGG_EXCEPTION,e);
        }
        return result;
    }

    /**
     * 识别出该类型下所有可搜索的 字符和数字 参数
     * @param numberSpecMap
     * @param strSpecList
     * @param categories
     */
    private void identifySpec(Map<String, String> numberSpecMap, List<String> strSpecList, List<Category> categories) {
        //根据类别id查询到对应的规格参数 json
        String specifications = specClient.querySpecByCid(categories.get(0).getId()).getBody();
        //将json->对象
        List<Map<String, Object>> specificationList = JsonUtils.nativeRead(specifications, new TypeReference<List<Map<String, Object>>>() {});
        //遍历规格参数，识别字符参数和数字参数
        specificationList.forEach(specMap->((List<Map<String,Object>>)specMap.get("params")).forEach(param->{
            //是否可搜索
            if ((boolean)param.get("searchable")) {
                String k = param.get("k").toString();
                //判断是什么类型
                if (param.get("numerical")!=null && (boolean) param.get("numerical")) {
                    numberSpecMap.put(k,param.get("unit").toString());
                }
                else {
                    strSpecList.add(k);
                }
            }
        }) );

    }

    /**
     * 查询每个数字型参数间隔
     * @return
     * @param numberSpecMap
     * @param basicQuery
     */
    private Map<String, Double> getNumberSpecInterval(Map<String, String> numberSpecMap, QueryBuilder basicQuery) {
        Map<String, Double> result = new HashMap<>();
        try {
            NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
            nativeSearchQueryBuilder.withQuery(basicQuery);
            //不要数据
            nativeSearchQueryBuilder.withPageable(PageRequest.of(0, 1));
            //添加每个数字型参数的stats聚合
            numberSpecMap.forEach((name,unit)->{
                nativeSearchQueryBuilder.addAggregation(AggregationBuilders.stats(name).field("specs."+name));
            });
            //执行聚合查询
            SearchHits<Goods> searchHits = elasticsearchRestTemplate.search(nativeSearchQueryBuilder.build(), Goods.class);

            //解析每个数字型参数的stats聚合结果
            numberSpecMap.keySet().forEach(name->{
                ParsedStats aggregation = searchHits.getAggregations().get(name);
                result.put(name, NumberUtils.getInterval(aggregation.getMin(), aggregation.getMax(), aggregation.getSum()));
            });
        } catch (Exception e) {
            EgoException.error(log,ExceptionEnum.QUERY_INTERVAL_EXCEPTION);
        }
        return result;
    }
    /**
     * 构建带过滤条件的基本查询
     * @param searchRequest
     * @return
     */
    private QueryBuilder buildBaiscQuery(SearchRequest searchRequest) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //基本查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all",searchRequest.getKey()).operator(Operator.AND));
        //过滤条件构造器
        BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
        //整理过滤条件
        Map<String,String> filter = searchRequest.getFilters();
        for (Map.Entry<String,String> entry : filter.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String regex = "^(\\d+[.]?\\d*)_(\\d+[.]?\\d*)$";
            //判断是否需要范围查询
            if (value.matches(regex)) {
                Double[] nums = NumberUtils.searchNumber(value, regex);
                //数值类型进行范围查询   lt:小于  gte:大于等于
                filterQueryBuilder.must(QueryBuilders.rangeQuery("specs." + key).gte(nums[0]).lt(nums[1]));
            } else {
                //商品分类和品牌要特殊处理
                if (key.equals("分类"))
                {
                    key = "cid3";
                }
                else if(key.equals("品牌"))
                {
                    key = "brandId";
                }
                else{
                    key = "specs." + key + ".keyword";
                }
                //字符串类型，进行term查询
                filterQueryBuilder.must(QueryBuilders.termQuery(key, value));
            }
        }
        //添加过滤条件
        queryBuilder.filter(filterQueryBuilder);
        return queryBuilder;
    }

    private QueryBuilder buildBaiscQuery(String key) {
        return QueryBuilders.matchQuery("all", key);
    }

    /**
     * 获取品牌聚合结果
     * @param searchHits
     * @return
     */
    private List<Brand> getBrandAgg(SearchHits<Goods> searchHits) {
        ParsedLongTerms agg = searchHits.getAggregations().get("品牌");
        List<Long> idList = agg.getBuckets().stream()
                .map(bucket -> (Long)bucket.getKey())
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idList)) {
            return null;
        }
        //通过feign批量查询类别集合
        return brandClient.queryBrandByIds(idList);
    }

    /**
     * 获取分类聚合结果
     * @param searchHits
     * @return
     */
    private List<Category> getCategoryAgg(SearchHits<Goods> searchHits) {
        ParsedLongTerms agg = searchHits.getAggregations().get("分类");
        List<Long> idList = agg.getBuckets().stream()
                .map(bucket -> (Long)bucket.getKey())
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(idList)) {
            return null;
        }
        //通过feign批量查询类别集合
        return categoryClient.queryListByIds(idList).getBody();
    }
}
