package com.ego.item.service;

import com.ego.entity.Specification;
import com.ego.item.mapper.SpecificationMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SpecificationService {

    @Resource
    private SpecificationMapper specificationMapper;

    public Specification queryById(Long id) {
        return this.specificationMapper.selectById(id);
    }
}
