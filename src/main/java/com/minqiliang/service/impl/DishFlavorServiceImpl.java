package com.minqiliang.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minqiliang.entity.DishFlavor;
import com.minqiliang.mapper.DishFlavorMapper;
import com.minqiliang.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
