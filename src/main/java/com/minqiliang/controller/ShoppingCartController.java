package com.minqiliang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.minqiliang.common.R;
import com.minqiliang.entity.ShoppingCart;
import com.minqiliang.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private HttpServletRequest request;


    /**
     * 添加购物车
     *
     * @param cart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart cart) {
        // 获取用户id，指定当前是哪个用户的购物车数据
        Long userId = (Long) request.getSession().getAttribute("user");
        // 查询当前购物车数据是否存在，存在的话number+1就行了
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        queryWrapper.eq(cart.getDishId() != null, ShoppingCart::getDishId, cart.getDishId());
        queryWrapper.eq(cart.getSetmealId() != null, ShoppingCart::getSetmealId, cart.getSetmealId());
        ShoppingCart shoppingCart = shoppingCartService.getOne(queryWrapper);
        if (shoppingCart != null) {
            // 如果存在，修改number
            Integer number = shoppingCart.getNumber();
            shoppingCart.setNumber(number + 1);
            shoppingCartService.updateById(shoppingCart);
        } else {
            // 不存在的话，插入数据
            cart.setUserId(userId);
            cart.setNumber(1);
            cart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(cart);
            shoppingCart = cart;
        }
        return R.success(shoppingCart);
    }

    /**
     * 删除购物车
     *
     * @param cart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart cart) {
        // 获取用户id，指定当前是哪个用户的购物车数据
        Long userId = (Long) request.getSession().getAttribute("user");
        log.info("dishId:{}", cart.getDishId());
        // 查询当前购物车数据是否存在，存在的话number-1就行了
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        queryWrapper.eq(cart.getDishId() != null, ShoppingCart::getDishId, cart.getDishId());
        queryWrapper.eq(cart.getSetmealId() != null, ShoppingCart::getSetmealId, cart.getSetmealId());
        ShoppingCart shoppingCart = shoppingCartService.getOne(queryWrapper);
        if (shoppingCart != null) {
            // 如果存在，修改number
            Integer number = shoppingCart.getNumber();
            if (number > 0) {
                shoppingCart.setNumber(number - 1);
                shoppingCartService.updateById(shoppingCart);
            } else {
                shoppingCartService.remove(queryWrapper);
            }
        }
        return R.success(shoppingCart);
    }


    /**
     * 查看购物车
     *
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        Long userId = (Long) request.getSession().getAttribute("user");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> cartList = shoppingCartService.list(queryWrapper);
        return R.success(cartList);
    }

    @DeleteMapping("/clean")
    public R<String> cleanCart() {
        Long userId = (Long) request.getSession().getAttribute("user");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        shoppingCartService.remove(queryWrapper);
        return R.success("清空成功！");
    }
}
