package com.ego.item.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ego.entity.Category;
import com.ego.item.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

	/**
     * 根据parentId查询子类目
     * @param pid
     * @return
     */
    public List<Category> queryCategoryListByParentId(Long pid) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper();
        queryWrapper.eq("parent_id", pid);
        return this.categoryMapper.selectList(queryWrapper);
    }
    public List<Category> queryByBrandId(Long bid) {
        return this.categoryMapper.queryByBrandId(bid);
    }

    @Transactional(readOnly = true)
    public List<Category> findListByIds(List<Long> ids) {
        return categoryMapper.selectBatchIds(ids);
    }

    public List<Category> queryAllCategoryLevelByCid3(Long id) {
        List<Category> categoryList = new ArrayList<>();
        Category category = this.categoryMapper.selectById(id);
        while (category.getParentId() != 0){
            categoryList.add(category);
            category = this.categoryMapper.selectById(category.getParentId());
        }
        categoryList.add(category);
        return categoryList;
    }

    public List<Category> queryCategoryByIds(List<Long> ids) {
        return categoryMapper.selectBatchIds(ids);
    }
}
