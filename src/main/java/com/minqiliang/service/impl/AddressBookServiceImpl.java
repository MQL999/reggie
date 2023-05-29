package com.minqiliang.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minqiliang.entity.AddressBook;
import com.minqiliang.mapper.AddressBookMapper;
import com.minqiliang.service.AddressBookService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

}
