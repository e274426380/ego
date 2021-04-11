package com.ego.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Created by 不要剁我爪 on 2021/3/31.
 */
@Data
@TableName("tb_specification")
public class Specification {
    @TableId(type = IdType.INPUT)
    private Long categoryId;
    private String specifications;
}
