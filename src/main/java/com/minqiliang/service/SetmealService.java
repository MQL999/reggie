package com.minqiliang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.minqiliang.dto.SetmealDto;
import com.minqiliang.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    // 保存套餐，并且添加上套餐包含的菜品
    void saveWithSetmealDish(SetmealDto setmealDto);

    SetmealDto getWithSetmealDish(Long id);

    void updateWithSetmealDish(SetmealDto setmealDto);

    void removeByIdsWithSetmealDish(List<Long> ids);
}
