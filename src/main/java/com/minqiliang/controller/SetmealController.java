package com.minqiliang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minqiliang.common.R;
import com.minqiliang.dto.SetmealDto;
import com.minqiliang.entity.Category;
import com.minqiliang.entity.Setmeal;
import com.minqiliang.entity.SetmealDish;
import com.minqiliang.service.CategoryService;
import com.minqiliang.service.SetmealDishService;
import com.minqiliang.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 获取套餐的分页信息
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<SetmealDto>> page(Integer page, Integer pageSize, String name) {
        // 创建分页器
        Page<Setmeal> p = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        // 添加查询条件
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        // 查询数据
        setmealService.page(p, queryWrapper);

        // 拷贝属性值
        BeanUtils.copyProperties(p, dtoPage, "records");
        // 获取记录
        List<Setmeal> records = p.getRecords();
        // 修改记录
        List<SetmealDto> dtoList = records.stream().map(record -> {
            // 创建dto对象
            SetmealDto setmealDto = new SetmealDto();
            // 获取分类id
            Long categoryId = record.getCategoryId();
            // 根据分类id获取分类
            Category category = categoryService.getById(categoryId);
            // 将dto的分类名称设置为获取到的分类的名称
            if (category != null) {
                setmealDto.setCategoryName(category.getName());
            }
            // 把原先记录的属性值拷贝给dto
            BeanUtils.copyProperties(record, setmealDto);
            return setmealDto;
        }).collect(Collectors.toList());
        // 给dtoPage设置记录
        dtoPage.setRecords(dtoList);
        return R.success(dtoPage);
    }

    /**
     * 添加套餐
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        setmealService.saveWithSetmealDish(setmealDto);
        return R.success("添加成功");
    }

    /**
     * 通过id获取套餐信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id) {
        SetmealDto setmealDto = setmealService.getWithSetmealDish(id);
        return R.success(setmealDto);
    }

    /**
     * 更新套餐数据
     *
     * @param setmealDto
     * @return
     */
    @PutMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> updateWithSetmealDish(@RequestBody SetmealDto setmealDto) {
        setmealService.updateWithSetmealDish(setmealDto);
        return R.success("修改成功");
    }

    /**
     * 通过id批量或单个修改发售状态
     *
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> updateStatus(@PathVariable Integer status, @RequestParam List<Long> ids) {
        LambdaUpdateWrapper<Setmeal> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Setmeal::getId, ids)
                .set(Setmeal::getStatus, status);
        setmealService.update(updateWrapper);
        return R.success("修改成功");
    }

    /**
     * 通过id批量删除（也可以单个删除）
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids) {
        setmealService.removeByIdsWithSetmealDish(ids);
        return R.success("删除成功");
    }

    /**
     * 根据分类id查询套餐信息
     * @param categoryId
     * @param status
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "'setmeal_' + #categoryId + '_' + #status")
    public R<List<SetmealDto>> list(Long categoryId,Integer status){
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,categoryId)
                .eq(Setmeal::getStatus,status).orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = setmealService.list(setmealLambdaQueryWrapper);
        List<SetmealDto> setmealDtoList = setmealList.stream().map(setmeal -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(setmeal, setmealDto);
            Long setmealId = setmeal.getId();
            LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SetmealDish::getSetmealId, setmealId);
            List<SetmealDish> setmealDishList = setmealDishService.list(queryWrapper);
            setmealDto.setSetmealDishes(setmealDishList);
            return setmealDto;
        }).collect(Collectors.toList());
        return R.success(setmealDtoList);
    }
}
