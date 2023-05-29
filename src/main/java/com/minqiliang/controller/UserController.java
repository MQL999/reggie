package com.minqiliang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minqiliang.common.R;
import com.minqiliang.entity.User;
import com.minqiliang.service.UserService;
import com.minqiliang.utils.SMSUtils;
import com.minqiliang.utils.UserNameUtils;
import com.minqiliang.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/sendMsg")
    private R<String> sendMsg(@RequestBody User user, HttpSession session) {
        // 获取手机号
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
            // 生成四位验证码
            String code = ValidateCodeUtils.generateValidateCode(6).toString();

            // 发送短信
            log.info("验证码为：{}", code);
            // SMSUtils.sendMessage("瑞吉外卖","",phone,code);

            // 保存验证码到session
            //session.setAttribute(phone,code);

            // 保存验证码到redis,并且设置有效期5分钟
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);

            return R.success("发送成功！");
        }
        return R.error("手机号不能为空！");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        String phone = (String) map.get("phone");
        String code = (String) map.get("code");
        // 从sesson获取验证码
        //String validateCode = (String) session.getAttribute(phone);

        // 从redis获取验证码
        String validateCode = (String) redisTemplate.opsForValue().get(phone);

        if (Objects.equals(code, validateCode)) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User u = userService.getOne(queryWrapper);
            if (u == null) {
                User user = new User();
                user.setName(UserNameUtils.getStringRandom(10));
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
                session.setAttribute("user", user.getId());
                // 删除redis缓存的验证码
                redisTemplate.delete(phone);
                return R.success(user);
            }
            Integer status = u.getStatus();
            if (status != 0) {
                session.setAttribute("user", u.getId());
                // 删除redis缓存的验证码
                redisTemplate.delete(phone);
                return R.success(u);
            }
            return R.error("账号异常,请联系商家！");
        }
        return R.error("验证码错误！");
    }

    @PostMapping("/loginout")
    public R<String> loginout(HttpServletRequest request) {
        request.getSession().removeAttribute("user");
        return R.success("退出成功！");
    }
}
