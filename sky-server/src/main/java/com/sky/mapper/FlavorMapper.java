package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FlavorMapper {

    @AutoFill(OperationType.INSERT)
    void saveBatch(List<DishFlavor> flavors);
}
