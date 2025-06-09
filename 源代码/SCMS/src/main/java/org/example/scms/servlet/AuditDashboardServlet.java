package org.example.scms.servlet;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.scms.model.Administrator;
import org.example.scms.model.AuditLog;
import org.example.scms.service.AuditLogService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 审计管理员仪表板Servlet
 * 专门为审计管理员提供的仪表板，只显示日志查看功能
 */
@WebServlet("/admin/audit/dashboard")
public class AuditDashboardServlet extends HttpServlet {
    private final AuditLogService auditLogService = new AuditLogService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Administrator admin = (Administrator) session.getAttribute("admin");

        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }        // 验证是否为审计管理员 (只允许 audit_admin 类型的管理员访问)
        if (!"audit_admin".equals(admin.getAdminType())) {
            // 非审计管理员不能访问此页面，重定向到普通仪表板
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }try {
            // 获取基本统计数据
            LocalDate today = LocalDate.now();
            
            // 总日志数
            List<AuditLog> allLogs = auditLogService.getAllAuditLogs();
            int totalLogs = allLogs.size();
            
            // 今日成功登录数
            List<AuditLog> todayLogs = auditLogService.getAuditLogsByDateRange(today, today);
            long successfulLogins = todayLogs.stream()
                .filter(log -> "LOGIN_SUCCESS".equals(log.getAction()))
                .count();
            
            // 今日数据操作数
            long dataOperations = todayLogs.stream()
                .filter(log -> "CREATE".equals(log.getAction()) || 
                              "UPDATE".equals(log.getAction()) || 
                              "DELETE".equals(log.getAction()) ||
                              "VIEW".equals(log.getAction()) ||
                              "QUERY".equals(log.getAction()))
                .count();
            
            // 创建统计对象
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalLogs", totalLogs);
            statistics.put("successfulLogins", successfulLogins);
            statistics.put("dataOperations", dataOperations);
            
            // 获取最近5条日志用于预览
            List<AuditLog> recentLogs = auditLogService.getRecentAuditLogs(5);
            
            // 设置请求属性
            request.setAttribute("statistics", statistics);
            request.setAttribute("recentLogs", recentLogs);
            request.setAttribute("now", new java.util.Date());

            request.getRequestDispatcher("/admin/audit/dashboard.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "获取审计数据失败: " + e.getMessage());
            request.getRequestDispatcher("/admin/audit/dashboard.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
