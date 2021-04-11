package com.ego.item.service;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ego.EgoException;
import com.ego.ExceptionEnum;
import com.ego.common.entity.PageResult;
import com.ego.entity.*;
import com.ego.item.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GoodsService {

    @Resource
    private SpuMapper spuMapper;
    @Resource
    private StockMapper stockMapper;
    @Resource
    private SpuDetailMapper spuDetailMapper;
    @Resource
    private SkuMapper skuMapper;

    @Resource
    private CategoryService categoryService;

    @Resource
    private BrandMapper brandMapper;

//    @Transactional(readOnly = true)
//    public PageResult<SpuBO> page(String key, Boolean saleable, Integer page, Integer rows) {
//        PageResult<SpuBO> result = new PageResult<>();
//        try {
//            //查询总记录数
//            Integer total = spuMapper.selectCount(
//                    new QueryWrapper<Spu>()
//                            .eq(saleable != null, "saleable", saleable)
//                            .like(StringUtils.isNotEmpty(key), "title", key)
//            );
//            result.setTotal(Long.valueOf(total));
//            //分页查询数据
//            List<SpuBO> items = spuMapper.selectSpuPage(key, saleable, page, rows);
//            result.setItems(items);
//
//        } catch (Exception e) {
//            EgoException.error(log, ExceptionEnum.GOODS_PAGE_ERROR, e);
//        }
//        return result;
//    }

    @Transactional(readOnly = true)
    public PageResult<SpuBO> page(String key, Boolean saleable, Integer page, Integer rows) {
        PageResult<SpuBO> pageResult = new PageResult<>();
        try {
            Integer total = spuMapper.selectCount(
                    new QueryWrapper<Spu>().eq(saleable != null, "saleable", saleable)
                            .like(StringUtils.isNotEmpty(key), "title", key)
            );
            pageResult.setTotal(Long.valueOf(total));
            List<SpuBO> spuBOS = spuMapper.selectSpuPage(key, saleable, page, rows);
            pageResult.setItems(spuBOS);
        } catch (Exception e) {
            log.error("分页查询失败", e);
            EgoException.error(log, ExceptionEnum.GOODS_PAGE_ERROR, e);
        }
        return pageResult;
    }

    @Transactional(readOnly = true)
    public PageResult<SpuBO> page(Boolean saleable, Integer page, Integer rows) {
        PageResult<SpuBO> result = new PageResult<>();
        try {
            //查询总记录数
            Integer total = spuMapper.selectCount(
                    new QueryWrapper<Spu>()
                            .eq(saleable != null, "saleable", saleable)
            );
            result.setTotal(Long.valueOf(total));
            //分页查询数据
            List<SpuBO> items = spuMapper.selectPageForES(saleable, page, rows);
            result.setItems(items);

        } catch (Exception e) {
            EgoException.error(log, ExceptionEnum.GOODS_PAGE_ERROR, e);
        }
        return result;
    }

    //    @Transactional(rollbackFor = EgoException.class)
//    public void save(SpuBO SpuBO) {
//        try {
//            //1.保存spu
//            SpuBO.setValid(true);
//            SpuBO.setCreateTime(new Date());
//            SpuBO.setLastUpdateTime(SpuBO.getCreateTime());
//            SpuBO.setSaleable(true);
//
//            spuMapper.insert(SpuBO);
//
//            Long spuId = SpuBO.getId();
//            //2.保存spu_detail
//            SpuDetail spuDetail = SpuBO.getSpuDetail();
//            //维护关系
//            spuDetail.setSpuId(spuId);
//
//            spuDetailMapper.insert(spuDetail);
//            //3.保存所有sku以及对应库存
//            saveSkuAndStock(SpuBO);
//        } catch (Exception e) {
//            log.error("新增商品异常", e);
//            throw new EgoException(ExceptionEnum.GOODS_SAVE_ERROR);
//        }
//    }
    @Transactional(rollbackFor = EgoException.class)
    public void save(SpuBO spu) {
        try {
            spu.setCreateTime(new Date());
            spu.setLastUpdateTime(new Date());
            spu.setSaleable(true);
            spu.setValid(true);
            spuMapper.insert(spu);
            //保存detail
            SpuDetail spuDetail = spu.getSpuDetail();
            spuDetail.setSpuId(spu.getId());
            spuDetailMapper.insert(spuDetail);
            //保存sku，stock
            saveSkuAndStock(spu);
        } catch (Exception e) {
            log.error("新增spu失败", e);
            throw new EgoException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
    }

    private void saveSkuAndStock(SpuBO spuBO) {

            List<Sku> skus = spuBO.getSkus();
            for (Sku sku : skus) {
                sku.setCreateTime(new Date());
                sku.setLastUpdateTime(new Date());
                sku.setId(spuBO.getId());
                skuMapper.insert(sku);
                //stock
                Stock stock = sku.getStock();
                stock.setSkuId(sku.getId());
                stockMapper.insert(stock);
            }
    }


//    private void saveSkuAndStock(SpuBO SpuBO) {
//        List<Sku> skus = SpuBO.getSkus();
//        Date now = new Date();
//        for (Sku sku : skus) {
//            sku.setCreateTime(now);
//            sku.setLastUpdateTime(now);
//            //维护关系
//            sku.setSpuId(SpuBO.getId());
//            skuMapper.insert(sku);
//
//            //4.stock
//            Stock stock = sku.getStock();
//            //维护关系
//            stock.setSkuId(sku.getId());
//            stockMapper.insert(stock);
//        }
//    }

    /**
     * 根据spuid查询SpuDetail<一对一关系>
     *
     * @param spuId
     * @return
     */
    @Transactional(readOnly = true)
    public SpuDetail querySpuDetailById(Long spuId) {
        try {
            spuDetailMapper.selectById(spuId);
        } catch (Exception e) {
            EgoException.error(log, ExceptionEnum.SPU_DETAIL_NOT_FOUND, e);
        }
        return this.spuDetailMapper.selectById(spuId);
    }

    /**
     * 根据spuid查询sku<一对多关系>
     *
     * @param spuId
     * @return
     */
    public List<Sku> querySkuBySpuId(Long spuId) {
        List<Sku> skus = null;
        try {
            // 查询sku
            Map<String, Object> map = new HashMap<>(1);
            map.put("spu_id", spuId);
            skus = this.skuMapper.selectByMap(map);
            for (Sku sku : skus) {
                // 同时查询出库存
                sku.setStock(this.stockMapper.selectById(sku.getId()));
            }
            return skus;
        } catch (Exception e) {
            EgoException.error(log, ExceptionEnum.BAD_REQUEST, e);
        }
        return skus;
    }

    //    @Transactional(rollbackFor = EgoException.class)
//    public void update(SpuBO spu) {
//        try {
//            // 查询以前sku
//            List<Sku> skus = this.querySkuBySpuId(spu.getId());
//            // 如果以前存在，则删除
//            if (!CollectionUtils.isEmpty(skus)) {
//                List<Long> skuIdList = skus.stream().map(s -> s.getId()).collect(Collectors.toList());
//                // 删除以前库存
//                this.stockMapper.deleteBatchIds(skuIdList);
//
//                // 删除以前的sku
//                this.skuMapper.deleteBatchIds(skuIdList);
//
//            }
//            // 新增sku和库存
//            saveSkuAndStock(spu);
//
//            // 更新spu
//            spu.setLastUpdateTime(new Date());
//            this.spuMapper.updateById(spu);
//
//            // 更新spu详情
//            this.spuDetailMapper.updateById(spu.getSpuDetail());
//        } catch (Exception e) {
//            log.error("更新商品异常", e);
//            throw new EgoException(ExceptionEnum.GOODS_UPDATE_ERROR);
//        }
//    }
    @Transactional(rollbackFor = EgoException.class)
    public void update(SpuBO spu) {
        try{

            List<Sku> skus = this.querySkuBySpuId(spu.getId());
            if(!CollectionUtils.isEmpty(skus)){
                List<Long> skuList = skus.stream().map(s -> s.getId()).collect(Collectors.toList());
                //删除以前的库存
                this.stockMapper.deleteBatchIds(skuList);
                //删除sku
                this.skuMapper.deleteBatchIds(skuList);
            }
            //更新sku和库存
            saveSkuAndStock(spu);
            //更新spu
            spu.setLastUpdateTime(new Date());
            this.spuMapper.updateById(spu);
            //更新detail
            this.spuDetailMapper.updateById(spu.getSpuDetail());
        }
        catch (Exception e){
            log.error("更新商品异常",e);
            throw new EgoException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
    }


    //    /**
//     * 逻辑删除商品
//     *
//     * @param id spu主键
//     */
//    @Transactional(rollbackFor = EgoException.class)
//    public void deleteSpuById(Long id) {
//        try {
//            Spu spu = new SpuBO();
//            spu.setId(id);
//            //状态设置为"已删除"
//            spu.setValid(false);
//            spuMapper.updateById(spu);
//        } catch (Exception e) {
//            log.error("删除商品异常", e);
//            throw new EgoException(ExceptionEnum.DELETE_GOODS_ERROR);
//        }
//    }
    @Transactional(rollbackFor = EgoException.class)
    public void deleteSpuById(Long id) {
       try{
           Spu spu = new Spu();
           spu.setId(id);
           spu.setValid(false);
           spuMapper.updateById(spu);
       }
       catch (Exception e){
            log.error("删除商品异常",e);
            throw new EgoException(ExceptionEnum.DELETE_GOODS_ERROR);
       }



    }
//
//    /**
//     * 根据id查询商品信息
//     * @param id
//     * @return
//     */
//    public SpuBO queryGoodsById(Long id) {
//        /**
//         * 第一页所需信息如下：
//         * 1.商品的分类信息、所属品牌、商品标题、商品卖点（子标题）
//         * 2.商品的包装清单、售后服务
//         */
//        Spu spu=this.spuMapper.selectById(id);
//        SpuDetail spuDetail = this.spuDetailMapper.selectById(spu.getId());
//
//
//        QueryWrapper<Sku> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("spu_id", spu.getId());
//        List<Sku> skuList = this.skuMapper.selectList(queryWrapper);
//
//        List<Long> skuIdList = new ArrayList<>();
//        for (Sku sku : skuList){
//            skuIdList.add(sku.getId());
//        }
//
//        List<Stock> stocks = this.stockMapper.selectBatchIds(skuIdList);
//
//        for (Sku sku:skuList){
//            for (Stock stock : stocks){
//                if (sku.getId().equals(stock.getSkuId())){
//                    sku.setStock(stock);
//                }
//            }
//        }
//
//        return SpuBO.builder()
//                .id(spu.getId())
//                .brandId(spu.getBrandId())
//                .cid1(spu.getCid1())
//                .cid2(spu.getCid2())
//                .cid3(spu.getCid3())
//                .brandId(spu.getBrandId())
//                .title(spu.getTitle())
//                .subTitle(spu.getSubTitle())
//                .saleable(spu.getSaleable())
//                .valid(spu.getValid())
//                .createTime(spu.getCreateTime())
//                .lastUpdateTime(spu.getLastUpdateTime())
//                .skus(skuList)
//                .spuDetail(spuDetail).build();
//    }
    /**
     * 根据id查询商品信息
     * @param id
     * @return
     */
    public SpuBO queryGoodsById(Long id) {
        return this.spuMapper.selectSpuBoById(id);
    }
}
