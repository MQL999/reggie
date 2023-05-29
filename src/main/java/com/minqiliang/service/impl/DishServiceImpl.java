package com.minqiliang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minqiliang.common.CustomException;
import com.minqiliang.dto.DishDto;
import com.minqiliang.entity.Dish;
import com.minqiliang.entity.DishFlavor;
import com.minqiliang.entity.Setmeal;
import com.minqiliang.mapper.DishMapper;
import com.minqiliang.service.DishFlavorService;
import com.minqiliang.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Value("${reggie.basePath}")
    private String basePath;

    @Transactional
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        // 保存菜品基本信息到菜品表
        this.save(dishDto);
        // 保存口味信息到口味表
        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((flavor) -> {
            flavor.setDishId(dishId);
            return flavor;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {
        // 构造dishDto对象
        DishDto dishDto = new DishDto();
        // 查询菜品的基本信息
        Dish dish = this.getById(id);
        // 条件构造器
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        // 添加条件
        queryWrapper.eq(DishFlavor::getDishId, id);
        // 查询菜品对应的口味信息
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);
        // 把dish的基本属性值复制给dishDto
        BeanUtils.copyProperties(dish, dishDto);
        // 给dishDto设置flavors值
        dishDto.setFlavors(list);
        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        // 更新基本的菜品信息
        this.updateById(dishDto);
        // 条件构造器
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        // 添加条件
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        // 删除原有的口味数据
        dishFlavorService.remove(queryWrapper);
        // 添加口味数据
        List<DishFlavor> flavors = dishDto.getFlavors();
        if (!flavors.isEmpty()) {
            // 保存口味信息到口味表
            Long dishId = dishDto.getId();
            flavors = flavors.stream().map((flavor) -> {
                flavor.setDishId(dishId);
                return flavor;
            }).collect(Collectors.toList());
            dishFlavorService.saveBatch(flavors);
        }
    }

    @Override
    @Transactional
    public void removeByIdWithFlavor(List<Long> ids) {
        // 查询如果菜品是起售状态，不允许删除
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(Dish::getId, ids);
        dishLambdaQueryWrapper.eq(Dish::getStatus, 1);
        int count = this.count(dishLambdaQueryWrapper);
        // 抛出异常，不允许删除
        if (count > 0) {
            throw new CustomException("菜品正在售卖中，不能删除！");
        }
        // 删除套餐对应的图片资源
        ids.forEach(id -> {
            Dish dish = this.getById(id);
            String image = dish.getImage();
            String filePath = basePath + image;
            new File(filePath).delete();
        });
        this.removeByIds(ids);
        // 条件构造器
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        // 添加条件
        queryWrapper.in(DishFlavor::getDishId, ids);
        // 删除菜品对应的口味数据
        dishFlavorService.remove(queryWrapper);
    }


}
