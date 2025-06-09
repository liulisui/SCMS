package org.example.scms.servlet;

import java.io.IOException;

import org.example.scms.model.Administrator;
import org.example.scms.util.AuditLogUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 用户注销Servlet
 */
@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            // 记录登出日志 - 检查是管理员还是普通用户会话
            Administrator admin = (Administrator) session.getAttribute("admin");
            if (admin != null) {
                AuditLogUtil.logLogout(request, admin.getId());
            }

            session.invalidate();
        }

        // 重定向到首页
        response.sendRedirect(request.getContextPath() + "/");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
