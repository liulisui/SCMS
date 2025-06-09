package org.example.scms.servlet;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用户登录Servlet
 * 由于系统改为无账户预约模式，此Servlet主要用于重定向
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 检查是否为管理员登录请求
        String redirect = request.getParameter("redirect");
        if ("admin".equals(redirect)) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }

        // 普通用户直接重定向到用户仪表板（无需登录）
        response.sendRedirect(request.getContextPath() + "/user/dashboard");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // POST请求也重定向到用户仪表板
        response.sendRedirect(request.getContextPath() + "/user/dashboard");
    }
}
