package org.example.scms.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.example.scms.model.User;

import com.google.gson.Gson;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * 审计日志工具类
 * 用于记录系统操作日志
 */
public class AuditLogUtil {

    private static final Gson gson = new Gson();

    /**
     * 记录创建操作
     */
    public static void logCreate(HttpServletRequest request, String tableName, Long recordId, Object newValues) {
        logOperation(request, "CREATE", tableName, recordId, null, newValues);
    }

    /**
     * 记录更新操作
     */
    public static void logUpdate(HttpServletRequest request, String tableName, Long recordId, Object oldValues,
            Object newValues) {
        logOperation(request, "UPDATE", tableName, recordId, oldValues, newValues);
    }

    /**
     * 记录删除操作
     */
    public static void logDelete(HttpServletRequest request, String tableName, Long recordId, Object oldValues) {
        logOperation(request, "DELETE", tableName, recordId, oldValues, null);
    }

    /**
     * 记录审批操作
     */
    public static void logApprove(HttpServletRequest request, String tableName, Long recordId, Object newValues) {
        logOperation(request, "APPROVE", tableName, recordId, null, newValues);
    }

    /**
     * 记录拒绝操作
     */
    public static void logReject(HttpServletRequest request, String tableName, Long recordId, Object newValues) {
        logOperation(request, "REJECT", tableName, recordId, null, newValues);
    }

    /**
     * 记录查看操作
     */
    public static void logView(HttpServletRequest request, String tableName, Long recordId, Object viewData) {
        logOperation(request, "VIEW", tableName, recordId, null, viewData);
    }

    /**
     * 记录查询操作
     */
    public static void logQuery(HttpServletRequest request, String tableName, Object queryParams) {
        logOperation(request, "QUERY", tableName, null, null, queryParams);
    }

    /**
     * 记录登录操作
     */
    public static void logLogin(HttpServletRequest request, Long userId) {
        logOperation(request, "LOGIN", "users", userId, null, null);
    }

    /**
     * 记录登出操作
     */
    public static void logLogout(HttpServletRequest request, Long userId) {
        logOperation(request, "LOGOUT", "users", userId, null, null);
    }

    /**
     * 记录通用操作
     */
    public static void logOperation(HttpServletRequest request, String action, String tableName,
            Long recordId, Object oldValues, Object newValues) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "INSERT INTO audit_logs (user_id, action, table_name, record_id, old_values, new_values, ip_address, user_agent) "
                    +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);

            // 获取当前用户ID
            Long userId = getCurrentUserId(request);
            stmt.setObject(1, userId);
            stmt.setString(2, action);
            stmt.setString(3, tableName);
            stmt.setObject(4, recordId);

            // 将对象转换为JSON字符串
            stmt.setString(5, oldValues != null ? gson.toJson(oldValues) : null);
            stmt.setString(6, newValues != null ? gson.toJson(newValues) : null);

            // 获取客户端信息
            stmt.setString(7, getClientIpAddress(request));
            stmt.setString(8, request.getHeader("User-Agent"));

            stmt.executeUpdate();
        } catch (SQLException e) {
            // 记录日志失败不应该影响业务流程，只记录到系统日志
            System.err.println("记录审计日志失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户ID
     */
    private static Long getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                return user.getId();
            }
        }
        return null;
    }

    /**
     * 获取客户端IP地址
     */
    private static String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // 如果是多个IP地址，取第一个
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress;
    }

    /**
     * 创建审计日志数据对象
     */
    public static class AuditData {
        private Map<String, Object> data;

        public AuditData(Map<String, Object> data) {
            this.data = data;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }
    }
}
