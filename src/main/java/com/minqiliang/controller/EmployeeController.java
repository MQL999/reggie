package com.minqiliang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minqiliang.entity.Employee;
import com.minqiliang.common.R;
import com.minqiliang.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping(path = "/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping(path = "/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        // 将页面传递过来的密码进行加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        // 根据用户名查询用户信息
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        // 判断用户是否存在
        if (emp == null) {
            return R.error("用户名不存在！");
        }

        // 判断密码是否正确
        if (!emp.getPassword().equals(password)) {
            return R.error("密码错误！");
        }

        // 判断状态是否正常
        if (emp.getStatus() == 0) {
            return R.error("账号已被禁用，请联系管理员！");
        }

        // 将用户信息存储到session中
        request.getSession().setAttribute("employee", emp.getId());
        // 返回成功信息
        return R.success(emp);
    }

    /**
     * 退出登录
     */
    @PostMapping(path = "/logout")
    public R<String> logout(HttpServletRequest request) {
        // 将session中的用户信息清除
        request.getSession().removeAttribute("employee");
        // 返回退出成功信息
        return R.success("注销成功！");
    }

    /**
     * 新增员工
     * @param request
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增员工：{}", employee);
        // 设置一个默认密码，并且加密
        String password = "123456";
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        employee.setPassword(password);
        // 保存用户信息
        boolean result = employeeService.save(employee);
        // 判断是否保存成功
        if (result) {
            return R.success("保存成功！");
        }
        return R.error("保存失败！");
    }

    /**
     * 分页条件查询数据
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize, String name) {
        // 构造分页构造器
        Page p = new Page(page, pageSize);
        // 构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        // 添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        // 添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        // 执行查询
        employeeService.page(p, queryWrapper);
        return R.success(p);
    }

    /**
     * 根据id修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        // 修改数据
        employeeService.updateById(employee);
        return R.success("修改成功");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        Employee employee = employeeService.getById(id);
        if (employee != null){
            return R.success(employee);
        }
        return R.error("没有查询到员工信息");
    }
}
