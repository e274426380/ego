package com.ego.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ego.entity.Spu;
import com.ego.entity.SpuBO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SpuMapper extends BaseMapper<Spu> {
//    List<SpuBO> selectSpuPage(@Param("key") String key,@Param("saleable") Boolean saleable,
//                           @Param("pageNo")Integer pageNo,@Param("rows") Integer rows);









    /**
     * 分页查询数据(为同步数据到es中,需要返回skus ，spuDetail)
     * @param saleable
     * @param beginRows
     * @param rows
     * @return
     */
    List<SpuBO> selectPageForES(Boolean saleable, Integer beginRows, Integer rows);

    List<SpuBO> selectSpuPage(String key, Boolean saleable, Integer page, Integer rows);

    /**
     * 根据spu_id查询到spuBO完整数据
     * @param spuId
     * @return
     */
    SpuBO selectSpuBoById(Long spuId);
}
