package com.minqiliang.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.minqiliang.entity.Orders;

public interface OrderService extends IService<Orders> {
    void submit(Orders orders);
}
