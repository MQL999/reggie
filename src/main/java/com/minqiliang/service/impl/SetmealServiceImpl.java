package com.minqiliang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minqiliang.common.CustomException;
import com.minqiliang.dto.SetmealDto;
import com.minqiliang.entity.Setmeal;
import com.minqiliang.entity.SetmealDish;
import com.minqiliang.mapper.SetmealMapper;
import com.minqiliang.service.SetmealDishService;
import com.minqiliang.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Value("${reggie.basePath}")
    private String basePath;

    @Override
    @Transactional
    public void saveWithSetmealDish(SetmealDto setmealDto) {
        // 保存套餐的基本信息
        this.save(setmealDto);
        // 获取套餐的id
        Long id = setmealDto.getId();
        // 获取套餐的菜品数据
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        // 给菜品数据添加对应的套餐id
        List<SetmealDish> setmealDishList = setmealDishes.stream().map(setmealDish -> {
            setmealDish.setSetmealId(id);
            return setmealDish;
        }).collect(Collectors.toList());
        // 保存菜品信息
        setmealDishService.saveBatch(setmealDishList);
    }

    @Override
    public SetmealDto getWithSetmealDish(Long id) {
        // 查询套餐的基本信息
        Setmeal setmeal = this.getById(id);
        // 创建dto对象
        SetmealDto setmealDto = new SetmealDto();
        // 添加查询菜品的条件
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
        // 把套餐的基本信息拷贝给dto
        BeanUtils.copyProperties(setmeal, setmealDto);
        // 给dto设置菜品数据
        setmealDto.setSetmealDishes(setmealDishes);
        return setmealDto;
    }

    @Override
    @Transactional
    public void updateWithSetmealDish(SetmealDto setmealDto) {
        // 更新基本的套餐信息
        this.updateById(setmealDto);
        // 移除原来的菜品
        Long id = setmealDto.getId();
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        setmealDishService.remove(queryWrapper);
        // 保存修改过的菜品
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        if (!setmealDishes.isEmpty()) {
            setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(id));
            setmealDishService.saveBatch(setmealDishes);
        }
    }

    @Override
    @Transactional
    public void removeByIdsWithSetmealDish(List<Long> ids) {
        // 查询如果套餐是起售状态，不允许删除
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId, ids);
        setmealLambdaQueryWrapper.eq(Setmeal::getStatus, 1);
        int count = this.count(setmealLambdaQueryWrapper);
        // 抛出异常，不允许删除
        if (count > 0) {
            throw new CustomException("套餐正在售卖中，不能删除！");
        }
        // 删除套餐对应的图片资源
        ids.forEach(id->{
            Setmeal setmeal = this.getById(id);
            String image = setmeal.getImage();
            String filePath = basePath + image;
            new File(filePath).delete();
        });
        // 删除套餐的基本信息
        this.removeByIds(ids);
        // 删除套餐对应的菜品
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(queryWrapper);
    }
}
