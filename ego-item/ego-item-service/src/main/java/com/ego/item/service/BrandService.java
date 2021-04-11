package com.ego.item.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ego.EgoException;
import com.ego.ExceptionEnum;
import com.ego.common.entity.PageResult;
import com.ego.entity.Brand;
import com.ego.item.mapper.BrandMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 〈〉
 *
 * @author coach tam
 * @email 327395128@qq.com
 * @create 2020/9/17
 * @since 1.0.0
 * 〈坚持灵活 灵活坚持〉
 */
@Service
@Slf4j
public class BrandService {

    @Resource
    private BrandMapper brandMapper;
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        // 新增品牌信息
        this.brandMapper.insert(brand);
        // 新增品牌和分类中间表
        for (Long cid : cids) {
            this.brandMapper.insertCategoryBrand(cid, brand.getId());
        }
    }
    @Transactional(readOnly = true)
    public PageResult<Brand> queryBrandByPageAndSort(
            Integer page, Integer rows, String sortBy, Boolean desc, String key) {

        // 过滤条件
        QueryWrapper<Brand> queryWrapper = new QueryWrapper<>();

        if (StringUtils.isNotBlank(key)) {
            queryWrapper.like("name", key)
                    .or()
                    .eq("letter", key);
        }
        if (StringUtils.isNotBlank(sortBy)) {
            // 排序
            queryWrapper.orderBy(StringUtils.isNotBlank(sortBy),desc,sortBy);
        }
        // 查询
        Page<Brand> pageInfo = brandMapper.selectPage(new Page<>(page, rows), queryWrapper);
        // 返回结果
        return new PageResult<Brand>(pageInfo.getTotal(), pageInfo.getRecords());
    }
    @Transactional(readOnly = true)
    public List<Brand> findListByCid(Long cid) {
        List<Brand> result = null;
        try {
            result = brandMapper.selectListByCid(cid);
        } catch (Exception e) {
            EgoException.error(log, ExceptionEnum.QUERY_BRAND_EXCEPTION,e);
        }
        return result;
    }

    public List<Brand> queryBrandByIds(List<Long> ids) {
        return this.brandMapper.selectBatchIds(ids);
    }
}
