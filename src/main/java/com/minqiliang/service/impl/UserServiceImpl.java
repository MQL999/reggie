package com.minqiliang.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minqiliang.entity.User;
import com.minqiliang.mapper.UserMapper;
import com.minqiliang.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
