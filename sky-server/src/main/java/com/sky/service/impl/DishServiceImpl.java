package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.FlavorMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Resource
    DishMapper dishMapper;

    @Resource
    FlavorMapper flavorMapper;

    @Resource
    SetmealDishMapper setmealDishMapper;

    @Override
    // 多表操作，保证一致性
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.save(dish);

        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0) {
            flavors.stream().forEach(flavor->{
                flavor.setDishId(dishId);
            });
            flavorMapper.saveBatch(flavors);
        }
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        PageResult pageResult = new PageResult(page.getTotal(), page.getResult());
        return pageResult;
    }

    @Override
    // 多表操作，保证一致性
    @Transactional
    public void deleteBatch(List<Long> ids) {

        // 在售的不能删除
        ids.stream().forEach(id->{
            Dish dish = dishMapper.getById(id);
            if(StatusConstant.ENABLE == dish.getStatus()) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        });

        // 套餐内不能删除
        List<Long> list = setmealDishMapper.queryByDishId(ids);
        if(list!=null && list.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);
        }

        // 删除
        // 从dish表删除
        dishMapper.deleteBatch(ids);

        // 从dish_flavor表删除
        flavorMapper.deleteBatchByDishId(ids);


    }

    @Override
    public DishVO getById(Long id) {
        System.out.println("id is" + id);
        Dish dish = dishMapper.getById(id);
        System.out.println("WHE"+dish.toString());
        List<DishFlavor> flavors = flavorMapper.getByDishId(id);
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    @Override
    @Transactional
    public void update(DishVO dishVO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishVO, dish);
        dishMapper.update(dish);
        List<DishFlavor> flavors = dishVO.getFlavors();

        // 根据菜品Id删除所有口味
        List<Long> list = new ArrayList<>();
        list.add(dishVO.getId());
        flavorMapper.deleteBatchByDishId(list);

        // 加入口味
        // 设置flavor的dishId
        flavors.stream().forEach(flavor->{
            flavor.setDishId(dishVO.getId());
        });
        flavorMapper.saveBatch(flavors);

    }
}
