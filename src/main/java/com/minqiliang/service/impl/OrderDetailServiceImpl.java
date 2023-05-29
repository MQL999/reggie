package com.minqiliang.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minqiliang.entity.OrderDetail;
import com.minqiliang.mapper.OrderDetailMapper;
import com.minqiliang.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
