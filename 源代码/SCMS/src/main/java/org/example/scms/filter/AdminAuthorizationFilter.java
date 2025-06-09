package org.example.scms.filter;

import java.io.IOException;

import org.example.scms.model.Administrator;

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
 * 管理员权限过滤器
 * 确保只有管理员才能访问管理员功能
 */
@WebFilter(urlPatterns = { "/admin/*", "/review" })
public class AdminAuthorizationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化配置
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 获取请求URI
        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();

        // 排除管理员登录页面，允许公开访问
        if (requestURI.endsWith("/admin/login") || requestURI.endsWith("/admin/login.jsp")) {
            chain.doFilter(request, response);
            return;
        }        HttpSession session = httpRequest.getSession(false); // 检查用户是否为管理员
        boolean isAdmin = false;
        if (session != null) {
            Administrator admin = (Administrator) session.getAttribute("admin");
            if (admin != null && "active".equals(admin.getStatus())) {
                isAdmin = true;
            }
        }

        if (!isAdmin) {
            // 非管理员用户，返回403错误或重定向到无权限页面
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/error/403.jsp");
            return;
        }

        // 管理员用户，继续处理请求
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // 清理资源
    }
}
