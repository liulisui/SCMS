package org.example.scms.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 身份验证过滤器
 * 只对管理员路径进行登录验证，用户路径无需登录
 */
@WebFilter(urlPatterns = { "/admin/*" })
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化配置
    }    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestURI = httpRequest.getRequestURI();

        // 排除管理员登录页面，允许直接访问
        if (requestURI.endsWith("/admin/login") || 
            requestURI.endsWith("/admin/login.jsp")) {
            chain.doFilter(request, response);
            return;
        }        // 检查管理员是否已登录
        HttpSession session = httpRequest.getSession(false);
        boolean isAdminLoggedIn = (session != null && session.getAttribute("admin") != null);

        if (!isAdminLoggedIn) {
            // 未登录的管理员重定向到登录页面
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/admin/login");
            return;
        }

        // 管理员已登录，继续处理请求
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // 清理资源
    }
}
