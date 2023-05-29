package com.minqiliang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.minqiliang.dto.DishDto;
import com.minqiliang.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
    // 新增菜品，并添加菜品对应的口味
    void saveWithFlavor(DishDto dishDto);

    // 获取菜品信息，包括菜品口味信息
    DishDto getByIdWithFlavor(Long id);

    // 更新菜品信息，包括菜品口味
    void updateWithFlavor(DishDto dishDto);

    // 批量或单个删除菜品
    void removeByIdWithFlavor(List<Long> ids);
}
