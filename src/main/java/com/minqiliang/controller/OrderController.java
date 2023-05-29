package com.minqiliang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minqiliang.common.R;
import com.minqiliang.dto.OrdersDto;
import com.minqiliang.entity.OrderDetail;
import com.minqiliang.entity.Orders;
import com.minqiliang.service.OrderDetailService;
import com.minqiliang.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     *
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        orderService.submit(orders);
        return R.success("下单成功！");
    }

    @GetMapping("/page")
    public R<Page<Orders>> page(Integer page, Integer pageSize) {
        Page<Orders> ordersPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(ordersPage,queryWrapper);
        return R.success(ordersPage);
    }

    @PutMapping
    public R<String> update(@RequestBody Orders orders) {
        LambdaUpdateWrapper<Orders> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Orders::getNumber, orders.getId())
                .set(Orders::getStatus, orders.getStatus());
        orderService.update(updateWrapper);
        return R.success("派送成功！");
    }

    @GetMapping("/userPage")
    public R<Page<OrdersDto>> userPage(Integer page, Integer pageSize) {
        Page<Orders> ordersPage = new Page<>(page, pageSize);
        orderService.page(ordersPage);
        Page<OrdersDto> ordersDtoPage = new Page<>();
        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");
        List<Orders> records = ordersPage.getRecords();
        List<OrdersDto> list = records.stream().map(record -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(record, ordersDto);
            String number = record.getNumber();
            LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderDetail::getOrderId, number);
            List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);
            ordersDto.setOrderDetails(orderDetailList);
            return ordersDto;
        }).collect(Collectors.toList());
        ordersDtoPage.setRecords(list);
        return R.success(ordersDtoPage);
    }
}
