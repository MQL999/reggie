package com.minqiliang.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 自定义元数据对象处理器
 */
@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Autowired
    private HttpServletRequest request;

    @Override
    public void insertFill(MetaObject metaObject) {
        Long user = (Long) request.getSession().getAttribute("employee");
        if(user == null){
            user = (Long) request.getSession().getAttribute("user");
        }
        log.info("当前操作人：{}",user);
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("createUser",user);
        metaObject.setValue("updateUser",user);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Long user = (Long) request.getSession().getAttribute("employee");
        if(user == null){
            user = (Long) request.getSession().getAttribute("user");
        }
        log.info("当前操作人：{}",user);
        metaObject.setValue("updateTime",LocalDateTime.now());
        metaObject.setValue("updateUser",user);
    }
}
