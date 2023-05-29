package com.minqiliang.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minqiliang.entity.Employee;
import com.minqiliang.service.EmployeeService;
import com.minqiliang.mapper.EmployeeMapper;
import org.springframework.stereotype.Service;


@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee>
    implements EmployeeService{

}




