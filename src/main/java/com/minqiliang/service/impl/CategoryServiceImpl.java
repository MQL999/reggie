package com.minqiliang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minqiliang.common.CustomException;
import com.minqiliang.entity.Category;
import com.minqiliang.entity.Dish;
import com.minqiliang.entity.Setmeal;
import com.minqiliang.mapper.CategoryMapper;
import com.minqiliang.service.CategoryService;
import com.minqiliang.service.DishService;
import com.minqiliang.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    @Override
    public void remove(Long id) {
        // 查询分类包含的菜品数
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId, id);
        int dishCount = dishService.count(queryWrapper);

        // 如果包含菜品，则抛出异常
        if (dishCount > 0) {
           throw  new CustomException("当前分类下关联了菜品，不能删除！");
        }

        // 查询分类包含的套餐数
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int setmealCount = setmealService.count(setmealLambdaQueryWrapper);

        // 如果包含的有套餐，则抛出异常
        if (setmealCount > 0) {
            throw  new CustomException("当前分类下关联了套餐，不能删除！");
        }

        // 不包含菜品和套餐，删除分类
        super.removeById(id);
    }
}
