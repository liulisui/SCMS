package org.example.scms.servlet;

import java.io.IOException;

import org.example.scms.model.Administrator;
import org.example.scms.service.AdministratorService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 管理员登录Servlet
 * 专门处理管理员登录请求，提供额外的安全检查
 */
@WebServlet("/admin/login")
public class AdminLoginServlet extends HttpServlet {

    private final AdministratorService administratorService = new AdministratorService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 显示管理员登录页面
        request.getRequestDispatcher("/admin/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String username = request.getParameter("adminUsername");
        String password = request.getParameter("adminPassword");

        // 验证参数
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "请输入管理员用户名和密码");
            request.getRequestDispatcher("/admin/login.jsp").forward(request, response);
            return;
        }

        try {
            // 使用AdministratorService进行登录验证
            String clientIP = getClientIP(request);
            String userAgent = request.getHeader("User-Agent");

            Administrator admin = administratorService.login(username.trim(), password, clientIP, userAgent);            if (admin != null) {
                // 登录成功，创建session
                HttpSession session = request.getSession();
                session.setAttribute("admin", admin); // 统一使用 "admin" 作为session key
                session.setAttribute("adminId", admin.getId());
                session.setAttribute("adminUsername", admin.getUsername());
                session.setAttribute("loginTime", System.currentTimeMillis());                // 检查管理员类型，只有 audit_admin 才能访问审计界面
                if ("audit_admin".equals(admin.getAdminType())) {
                    response.sendRedirect(request.getContextPath() + "/admin/audit/dashboard");
                } else {
                    // 其他管理员（包括系统管理员）重定向到普通管理员仪表板
                    response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                }
            }else {
                // 登录失败，获取用户信息以显示失败次数
                Administrator failedAdmin = administratorService.getAdministratorByUsername(username.trim());
                String errorMessage = "用户名或密码错误";                if (failedAdmin != null) {
                    Integer failCountObj = failedAdmin.getLoginFailCount();
                    int failCount = (failCountObj != null) ? failCountObj : 0;
                    
                    if ("locked".equals(failedAdmin.getStatus()) && failedAdmin.getLockUntilTime() != null) {
                        // 账户被锁定
                        errorMessage = "账户已被锁定30分钟，请稍后再试";
                        request.setAttribute("lockUntilTime", failedAdmin.getLockUntilTime());
                    } else if (failCount > 0) {
                        int remainingAttempts = 5 - failCount;
                        if (remainingAttempts > 0) {
                            errorMessage = String.format("用户名或密码错误，还可尝试 %d 次（连续失败5次将锁定30分钟）", remainingAttempts);
                        } else {
                            errorMessage = "账户已被锁定30分钟，请稍后再试";
                        }
                        request.setAttribute("failCount", failCount);
                        request.setAttribute("remainingAttempts", remainingAttempts);
                    }
                }
                
                request.setAttribute("error", errorMessage);
                request.getRequestDispatcher("/admin/login.jsp").forward(request, response);
            }

        } catch (Exception e) {
            // 记录系统错误
            System.err.println("管理员登录系统错误: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "系统错误，请稍后再试");
            request.getRequestDispatcher("/admin/login.jsp").forward(request, response);
        }
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
