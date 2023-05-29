package com.minqiliang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minqiliang.common.R;
import com.minqiliang.dto.DishDto;
import com.minqiliang.dto.SetmealDto;
import com.minqiliang.entity.Category;
import com.minqiliang.entity.Dish;
import com.minqiliang.entity.DishFlavor;
import com.minqiliang.service.CategoryService;
import com.minqiliang.service.DishFlavorService;
import com.minqiliang.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
        // 删除缓存
        Long categoryId = dishDto.getCategoryId();
        String key = "dish_" + categoryId + "_1";
        redisTemplate.delete(key);
        return R.success("添加成功！");
    }

    /**
     * 分页查询菜品数据
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize, String name) {
        // 创建分页器
        Page<Dish> dishPage = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        // 添加查询条件
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(name != null, Dish::getName, name)
                        .orderByDesc(Dish::getUpdateTime);
        dishService.page(dishPage, lambdaQueryWrapper);
        // 拷贝属性
        BeanUtils.copyProperties(dishPage, dishDtoPage, "records");
        // 获取dishPage的记录
        List<Dish> records = dishPage.getRecords();
        // 修改dishPage的记录内容
        List<DishDto> dishDtoList = records.stream().map(item -> {
            DishDto dishDto = new DishDto();
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }
            BeanUtils.copyProperties(item, dishDto);
            return dishDto;
        }).collect(Collectors.toList());

        // 设置dishDtoPage的记录为dishPage修改后的记录
        dishDtoPage.setRecords(dishDtoList);

        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
        // 删除缓存
        Long categoryId = dishDto.getCategoryId();
        String key = "dish_" + categoryId + "_1";
        redisTemplate.delete(key);
        return R.success("修改成功");
    }

    /**
     * 批量或单个修改起售状态
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatusByIds(@PathVariable Integer status, @RequestParam List<Long> ids) {
        ids.forEach(id -> {
            LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Dish::getId, id)
                    .set(Dish::getStatus, status);
            dishService.update(updateWrapper);
        });

        // 删除缓存
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(Dish::getId,ids);
        List<Dish> dishList = dishService.list(dishLambdaQueryWrapper);
        dishList.forEach((dish)->{
            Long categoryId = dish.getCategoryId();
            String key = "dish_"+ categoryId + "_1";
            redisTemplate.delete(key);
        });
        return R.success("修改成功");
    }

    /**
     * 批量或单个删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> removeBYId(@RequestParam List<Long> ids) {
        log.info(ids.toString());
        dishService.removeByIdWithFlavor(ids);
        return R.success("删除成功");
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> getByCategoryId(Long categoryId){
        String key = "dish_" + categoryId + "_1";
        List<DishDto> dishDtoList = null;
        // 先从redis查询数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        if (dishDtoList != null){
            // 如果存在数据，则直接返回数据
            return R.success(dishDtoList);
        }

        // 如果不存在，则从数据库查询，并且保存到redis
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        // 添加查询条件
        queryWrapper.eq(Dish::getCategoryId,categoryId);
        // 添加条件，查询状态为起售的菜品
        queryWrapper.eq(Dish::getStatus,1);
        // 添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishList = dishService.list(queryWrapper);
        dishDtoList = dishList.stream().map(dish -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish, dishDto);
            Long dishId = dish.getId();
            LambdaQueryWrapper<DishFlavor> flavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            flavorLambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> dishFlavors = dishFlavorService.list(flavorLambdaQueryWrapper);
            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).collect(Collectors.toList());

        // 如果不存在，则从数据库查询，并且保存到redis
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }
}
