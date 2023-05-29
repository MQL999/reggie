package com.minqiliang.fliter;

import com.alibaba.fastjson.JSON;
import com.minqiliang.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 强制类型转换
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 获取请求的URI
        String uri = request.getRequestURI();
        log.info("请求的URI为：{}", uri);

        // 判断请求的url是否是登录，退出，静态资源
        boolean check = check(uri);
        if (check) {
            log.info("本次请求是登录，退出，静态资源，放行");
            // 放行
            filterChain.doFilter(request, response);
            return;
        }

        // 判断员工登录状态，如果登录了，放行
        Object employee = request.getSession().getAttribute("employee");
        if (employee != null) {
            log.info("本次请求已经登录，放行");
            // 放行
            filterChain.doFilter(request, response);
            return;
        }

        // 判断用户登录状态，如果登录了，放行
        Object user = request.getSession().getAttribute("user");
        if (user != null) {
            log.info("本次请求已经登录，放行");
            // 放行
            filterChain.doFilter(request, response);
            return;
        }

        // 没有登录，跳转到登录页面
        log.info("本次请求没有登录，跳转到登录页面");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     * 判断请求的url是否是登录，退出，静态资源
     * @param uri
     * @return
     */
    private boolean check(String uri) {
        String[] urls = {"/employee/login", "/employee/logout", "/backend/**", "/front/**","/user/sendMsg","/user/login"};
        for (String url : urls) {
            if (PATH_MATCHER.match(url, uri)) {
                return true;
            }
        }
        return false;
    }
}
