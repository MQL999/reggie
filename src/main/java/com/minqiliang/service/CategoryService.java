package com.minqiliang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.minqiliang.entity.Category;

public interface CategoryService extends IService<Category> {
    void remove(Long id);
}
