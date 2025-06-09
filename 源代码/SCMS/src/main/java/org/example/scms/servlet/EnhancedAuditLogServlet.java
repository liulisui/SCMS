package org.example.scms.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.example.scms.model.AuditLog;
import org.example.scms.model.User;
import org.example.scms.util.AuditLogUtil;
import org.example.scms.util.DBUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 增强的审计日志管理Servlet
 * 为审计管理员提供完善的日志查询、统计和管理功能
 */
@WebServlet("/admin/enhanced-audit")
public class EnhancedAuditLogServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(EnhancedAuditLogServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 检查管理员权限
        if (!isAdminUser(request)) {
            response.sendRedirect(request.getContextPath() + "/admin/login.jsp");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        switch (action) {
            case "list":
                handleListAuditLogs(request, response);
                break;
            case "search":
                handleSearchAuditLogs(request, response);
                break;
            case "export":
                handleExportAuditLogs(request, response);
                break;
            case "statistics":
                handleStatistics(request, response);
                break;
            case "user-activity":
                handleUserActivity(request, response);
                break;
            case "security-alerts":
                handleSecurityAlerts(request, response);
                break;
            default:
                handleListAuditLogs(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    /**
     * 处理审计日志列表查询
     */
    private void handleListAuditLogs(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 记录管理员查看审计日志的操作
        Map<String, Object> viewData = new HashMap<>();
        viewData.put("action", "view_audit_logs");
        AuditLogUtil.logView(request, "audit_logs", null, viewData);

        // 分页参数
        int page = 1;
        int pageSize = 20;
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                page = Integer.parseInt(pageParam);
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        List<Map<String, Object>> auditLogs = new ArrayList<>();
        int totalRecords = 0;

        try (Connection conn = DBUtil.getConnection()) {
            // 获取总记录数
            String countSql = "SELECT COUNT(*) FROM audit_logs";
            PreparedStatement countStmt = conn.prepareStatement(countSql);
            ResultSet countRs = countStmt.executeQuery();
            if (countRs.next()) {
                totalRecords = countRs.getInt(1);
            }            // 获取分页数据 - 只查询administrators表来判断用户类型
            String sql = "SELECT al.*, " +
                        "a.username as admin_username, a.real_name as admin_full_name, a.admin_type " +
                        "FROM audit_logs al " +
                        "LEFT JOIN administrators a ON al.user_id = a.id " +
                        "ORDER BY al.created_at DESC " +
                        "LIMIT ? OFFSET ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, pageSize);
            stmt.setInt(2, (page - 1) * pageSize);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> log = new HashMap<>();
                log.put("id", rs.getLong("id"));
                log.put("userId", rs.getLong("user_id"));                // 根据administrators表判断用户类型
                String username = rs.getString("admin_username");
                String realName = rs.getString("admin_full_name");
                String userType = "user"; // 默认为普通用户
                
                if (username != null && realName != null) {
                    // 在administrators表中找到，这是管理员
                    userType = "admin";
                    log.put("adminType", rs.getString("admin_type"));
                } else {
                    // 在administrators表中未找到，这是普通用户
                    username = "user_" + rs.getLong("user_id"); // 生成默认用户名
                    realName = "普通用户"; // 默认显示名称
                }
                
                log.put("username", username);
                log.put("realName", realName);
                log.put("userType", userType);
                log.put("action", rs.getString("action"));
                log.put("tableName", rs.getString("table_name"));
                log.put("recordId", rs.getLong("record_id"));
                log.put("oldValues", rs.getString("old_values"));
                log.put("newValues", rs.getString("new_values"));
                log.put("ipAddress", rs.getString("ip_address"));
                log.put("userAgent", rs.getString("user_agent"));
                log.put("createdAt", rs.getTimestamp("created_at"));
                auditLogs.add(log);
            }} catch (SQLException e) {
            logger.log(Level.SEVERE, "查询审计日志失败", e);
            request.setAttribute("error", "查询审计日志失败：" + e.getMessage());
        }

        // 计算分页信息
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        
        request.setAttribute("auditLogs", auditLogs);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalRecords", totalRecords);
        request.setAttribute("pageSize", pageSize);
        
        request.getRequestDispatcher("/admin/audit/enhanced-audit-logs.jsp").forward(request, response);
    }

    /**
     * 处理审计日志搜索
     */
    private void handleSearchAuditLogs(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String action = request.getParameter("actionType");
        String tableName = request.getParameter("tableName");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String ipAddress = request.getParameter("ipAddress");

        // 记录搜索操作
        Map<String, Object> searchData = new HashMap<>();
        searchData.put("username", username);
        searchData.put("action", action);
        searchData.put("tableName", tableName);
        searchData.put("startDate", startDate);
        searchData.put("endDate", endDate);
        searchData.put("ipAddress", ipAddress);
        AuditLogUtil.logQuery(request, "audit_logs_search", searchData);        List<AuditLog> searchResults = new ArrayList<>();
        Map<Long, User> userMap = new HashMap<>();
          try (Connection conn = DBUtil.getConnection()) {            StringBuilder sqlBuilder = new StringBuilder();            sqlBuilder.append("SELECT al.*, ")
                     .append("a.username as admin_username, a.real_name as admin_full_name, a.admin_type ")
                     .append("FROM audit_logs al ")
                     .append("LEFT JOIN administrators a ON al.user_id = a.id ")
                     .append("WHERE 1=1 ");

            List<Object> params = new ArrayList<>();

            if (username != null && !username.trim().isEmpty()) {
                sqlBuilder.append("AND a.username LIKE ? ");
                params.add("%" + username.trim() + "%");
            }

            if (action != null && !action.trim().isEmpty()) {
                sqlBuilder.append("AND al.action = ? ");
                params.add(action.trim());
            }

            if (tableName != null && !tableName.trim().isEmpty()) {
                sqlBuilder.append("AND al.table_name = ? ");
                params.add(tableName.trim());
            }

            if (startDate != null && !startDate.trim().isEmpty()) {
                sqlBuilder.append("AND al.created_at >= ? ");
                params.add(startDate.trim() + " 00:00:00");
            }

            if (endDate != null && !endDate.trim().isEmpty()) {
                sqlBuilder.append("AND al.created_at <= ? ");
                params.add(endDate.trim() + " 23:59:59");
            }

            if (ipAddress != null && !ipAddress.trim().isEmpty()) {
                sqlBuilder.append("AND al.ip_address LIKE ? ");
                params.add("%" + ipAddress.trim() + "%");
            }

            sqlBuilder.append("ORDER BY al.created_at DESC LIMIT 100");

            PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString());
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                AuditLog log = new AuditLog();
                log.setId(rs.getLong("id"));
                log.setAdminId(rs.getLong("user_id"));
                  // 根据administrators表判断用户类型
                String logUsername = rs.getString("admin_username");
                String logRealName = rs.getString("admin_full_name");
                
                if (logUsername != null && logRealName != null) {
                    // 在administrators表中找到，这是管理员
                    log.setUsername(logUsername);
                } else {
                    // 在administrators表中未找到，这是普通用户
                    logUsername = "user_" + rs.getLong("user_id");
                    logRealName = "普通用户";
                    log.setUsername(logUsername);
                }
                
                log.setAction(rs.getString("action"));
                log.setResourceType(rs.getString("table_name"));
                log.setResourceId(rs.getLong("record_id"));
                log.setOldValue(rs.getString("old_values"));
                log.setNewValue(rs.getString("new_values"));
                log.setIpAddress(rs.getString("ip_address"));
                log.setUserAgent(rs.getString("user_agent"));
                
                // 设置创建时间
                if (rs.getTimestamp("created_at") != null) {
                    log.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
                
                searchResults.add(log);
                
                // 创建用户映射
                if (rs.getLong("user_id") != 0 && logUsername != null) {
                    User user = new User();
                    user.setId(rs.getLong("user_id"));
                    user.setUsername(logUsername);
                    user.setFullName(logRealName);
                    userMap.put(rs.getLong("user_id"), user);
                }
            }        } catch (SQLException e) {
            logger.log(Level.SEVERE, "搜索审计日志失败", e);
            request.setAttribute("error", "搜索审计日志失败：" + e.getMessage());
        }        request.setAttribute("auditLogs", searchResults);
        request.setAttribute("userMap", userMap);
        request.setAttribute("totalCount", searchResults.size());
        request.setAttribute("searchCriteria", buildSearchCriteria(searchData));
        request.getRequestDispatcher("/admin/audit/audit-search-results.jsp").forward(request, response);
    }

    /**
     * 处理审计日志导出
     */
    private void handleExportAuditLogs(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 记录导出操作
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("action", "export_audit_logs");
        exportData.put("format", "csv");
        AuditLogUtil.logView(request, "audit_logs_export", null, exportData);

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"audit_logs_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv\"");

        try (PrintWriter writer = response.getWriter();
             Connection conn = DBUtil.getConnection()) {
            
            // CSV头部
            writer.println("ID,用户ID,用户名,真实姓名,操作类型,表名,记录ID,旧值,新值,IP地址,用户代理,创建时间");            String sql = "SELECT al.*, a.username, a.real_name " +
                        "FROM audit_logs al " +
                        "LEFT JOIN administrators a ON al.user_id = a.id " +
                        "ORDER BY al.created_at DESC";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
              while (rs.next()) {                String username = rs.getString("username");
                String fullName = rs.getString("real_name");
                
                // 如果不是管理员，使用默认值
                if (username == null) {
                    username = "user_" + rs.getLong("user_id");
                }
                if (fullName == null) {
                    fullName = "普通用户";
                }
                
                writer.printf("%d,%d,%s,%s,%s,%s,%d,\"%s\",\"%s\",%s,\"%s\",%s%n",
                    rs.getLong("id"),
                    rs.getLong("user_id"),
                    username,
                    fullName,
                    rs.getString("action"),
                    rs.getString("table_name"),
                    rs.getLong("record_id"),
                    rs.getString("old_values") != null ? rs.getString("old_values").replace("\"", "\"\"") : "",
                    rs.getString("new_values") != null ? rs.getString("new_values").replace("\"", "\"\"") : "",
                    rs.getString("ip_address"),
                    rs.getString("user_agent") != null ? rs.getString("user_agent").replace("\"", "\"\"") : "",
                    rs.getTimestamp("created_at")
                );
            }} catch (SQLException e) {
            logger.log(Level.SEVERE, "导出审计日志失败", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "导出失败：" + e.getMessage());
        }
    }

    /**
     * 处理统计信息
     */
    private void handleStatistics(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Map<String, Object> statistics = new HashMap<>();
        
        try (Connection conn = DBUtil.getConnection()) {
            // 总日志数量
            String totalCountSql = "SELECT COUNT(*) FROM audit_logs";
            PreparedStatement totalStmt = conn.prepareStatement(totalCountSql);
            ResultSet totalRs = totalStmt.executeQuery();
            if (totalRs.next()) {
                statistics.put("totalLogs", totalRs.getInt(1));
            }

            // 今日日志数量
            String todayCountSql = "SELECT COUNT(*) FROM audit_logs WHERE DATE(created_at) = CURDATE()";
            PreparedStatement todayStmt = conn.prepareStatement(todayCountSql);
            ResultSet todayRs = todayStmt.executeQuery();
            if (todayRs.next()) {
                statistics.put("todayLogs", todayRs.getInt(1));
            }

            // 按操作类型统计
            String actionStatsSql = "SELECT action, COUNT(*) as count FROM audit_logs GROUP BY action ORDER BY count DESC";
            PreparedStatement actionStmt = conn.prepareStatement(actionStatsSql);
            ResultSet actionRs = actionStmt.executeQuery();
            Map<String, Integer> actionStats = new HashMap<>();
            while (actionRs.next()) {
                actionStats.put(actionRs.getString("action"), actionRs.getInt("count"));
            }
            statistics.put("actionStats", actionStats);

            // 按表名统计
            String tableStatsSql = "SELECT table_name, COUNT(*) as count FROM audit_logs GROUP BY table_name ORDER BY count DESC LIMIT 10";
            PreparedStatement tableStmt = conn.prepareStatement(tableStatsSql);
            ResultSet tableRs = tableStmt.executeQuery();
            Map<String, Integer> tableStats = new HashMap<>();
            while (tableRs.next()) {
                tableStats.put(tableRs.getString("table_name"), tableRs.getInt("count"));            }
            statistics.put("tableStats", tableStats);

            // 最活跃用户
            String userStatsSql = "SELECT a.username, a.real_name, COUNT(*) as count " +
                                 "FROM audit_logs al " +
                                 "LEFT JOIN administrators a ON al.user_id = a.id " +
                                 "WHERE al.user_id IS NOT NULL " +
                                 "GROUP BY al.user_id, a.username, a.real_name " +
                                 "ORDER BY count DESC LIMIT 10";
            PreparedStatement userStmt = conn.prepareStatement(userStatsSql);
            ResultSet userRs = userStmt.executeQuery();
            List<Map<String, Object>> userStats = new ArrayList<>();
            while (userRs.next()) {
                Map<String, Object> userStat = new HashMap<>();
                String username = userRs.getString("username");
                String fullName = userRs.getString("real_name");
                
                // 如果不是管理员，使用默认值
                if (username == null) {
                    username = "普通用户";
                    fullName = "普通用户";
                }
                
                userStat.put("username", username);
                userStat.put("realName", fullName);
                userStat.put("count", userRs.getInt("count"));
                userStats.add(userStat);
            }

            statistics.put("userStats", userStats);

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "获取统计信息失败", e);
            request.setAttribute("error", "获取统计信息失败：" + e.getMessage());
        }

        request.setAttribute("statistics", statistics);
        request.getRequestDispatcher("/admin/audit/audit-statistics.jsp").forward(request, response);
    }

    /**
     * 处理用户活动查询
     */
    private void handleUserActivity(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String userId = request.getParameter("userId");
        String username = request.getParameter("username");
        
        if ((userId == null || userId.trim().isEmpty()) && 
            (username == null || username.trim().isEmpty())) {
            request.setAttribute("error", "请指定用户ID或用户名");
            request.getRequestDispatcher("/admin/audit/user-activity.jsp").forward(request, response);
            return;
        }

        List<Map<String, Object>> userActivities = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection()) {
            String sql;
            PreparedStatement stmt;            if (userId != null && !userId.trim().isEmpty()) {
                sql = "SELECT al.*, a.username, a.real_name " +
                     "FROM audit_logs al " +
                     "LEFT JOIN administrators a ON al.user_id = a.id " +
                     "WHERE al.user_id = ? " +
                     "ORDER BY al.created_at DESC LIMIT 100";
                stmt = conn.prepareStatement(sql);
                stmt.setLong(1, Long.parseLong(userId));
            } else {
                sql = "SELECT al.*, a.username, a.real_name " +
                     "FROM audit_logs al " +
                     "LEFT JOIN administrators a ON al.user_id = a.id " +
                     "WHERE a.username = ? " +
                     "ORDER BY al.created_at DESC LIMIT 100";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("id", rs.getLong("id"));
                activity.put("action", rs.getString("action"));
                activity.put("tableName", rs.getString("table_name"));
                activity.put("recordId", rs.getLong("record_id"));
                activity.put("ipAddress", rs.getString("ip_address"));                activity.put("createdAt", rs.getTimestamp("created_at"));
                  String activityUsername = rs.getString("username");
                String activityFullName = rs.getString("real_name");
                
                // 如果不是管理员，使用默认值
                if (activityUsername == null) {
                    activityUsername = "user_" + rs.getLong("user_id");
                    activityFullName = "普通用户";
                }
                
                activity.put("username", activityUsername);
                activity.put("realName", activityFullName);
                userActivities.add(activity);}
        } catch (SQLException | NumberFormatException e) {
            logger.log(Level.SEVERE, "查询用户活动失败", e);
            request.setAttribute("error", "查询用户活动失败：" + e.getMessage());
        }

        request.setAttribute("userActivities", userActivities);
        request.setAttribute("queryUserId", userId);
        request.setAttribute("queryUsername", username);
        request.getRequestDispatcher("/admin/audit/user-activity.jsp").forward(request, response);
    }

    /**
     * 处理安全告警
     */
    private void handleSecurityAlerts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        List<Map<String, Object>> securityAlerts = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection()) {            // 检查异常登录（同一用户短时间内从不同IP登录）
            String suspiciousLoginSql = "SELECT a.username, al1.ip_address as ip1, al2.ip_address as ip2, " +
                                       "al1.created_at as time1, al2.created_at as time2 " +
                                       "FROM audit_logs al1 " +
                                       "JOIN audit_logs al2 ON al1.user_id = al2.user_id " +
                                       "LEFT JOIN administrators a ON al1.user_id = a.id " +
                                       "WHERE al1.action = 'LOGIN' AND al2.action = 'LOGIN' " +
                                       "AND al1.ip_address != al2.ip_address " +
                                       "AND ABS(TIMESTAMPDIFF(MINUTE, al1.created_at, al2.created_at)) <= 30 " +
                                       "AND al1.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                                       "ORDER BY al1.created_at DESC LIMIT 50";
            
            PreparedStatement stmt = conn.prepareStatement(suspiciousLoginSql);
            ResultSet rs = stmt.executeQuery();
              while (rs.next()) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("type", "异常登录");
                String username = rs.getString("username");
                if (username == null) {
                    username = "普通用户";
                }
                alert.put("description", String.format("用户 %s 在短时间内从不同IP登录: %s -> %s", 
                    username, rs.getString("ip1"), rs.getString("ip2")));
                alert.put("severity", "高");
                alert.put("time", rs.getTimestamp("time2"));
                securityAlerts.add(alert);
            }

            // 检查频繁失败操作
            String frequentFailuresSql = "SELECT a.username, al.action, al.ip_address, COUNT(*) as failure_count, " +
                                        "MAX(al.created_at) as last_failure " +
                                        "FROM audit_logs al " +
                                        "LEFT JOIN administrators a ON al.user_id = a.id " +                                        "WHERE al.action IN ('LOGIN_FAILED', 'ACCESS_DENIED') " +
                                        "AND al.created_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR) " +
                                        "GROUP BY a.username, al.action, al.ip_address " +
                                        "HAVING failure_count >= 5 " +
                                        "ORDER BY failure_count DESC, last_failure DESC";
            
            PreparedStatement failureStmt = conn.prepareStatement(frequentFailuresSql);
            ResultSet failureRs = failureStmt.executeQuery();
            
            while (failureRs.next()) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("type", "频繁失败");
                String failureUsername = failureRs.getString("username");
                if (failureUsername == null) {
                    failureUsername = "普通用户";
                }
                alert.put("description", String.format("IP %s 的用户 %s 在1小时内 %s 失败 %d 次", 
                    failureRs.getString("ip_address"),
                    failureUsername,
                    failureRs.getString("action"),
                    failureRs.getInt("failure_count")));
                alert.put("severity", "中");
                alert.put("time", failureRs.getTimestamp("last_failure"));
                securityAlerts.add(alert);            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "获取安全告警失败", e);
            request.setAttribute("error", "获取安全告警失败：" + e.getMessage());
        }

        request.setAttribute("securityAlerts", securityAlerts);
        request.getRequestDispatcher("/admin/audit/security-alerts.jsp").forward(request, response);
    }    /**
     * 检查是否为管理员用户或审计管理员
     */
    private boolean isAdminUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            // 检查是否为普通管理员
            User user = (User) session.getAttribute("user");
            if (user != null && "admin".equals(user.getRole())) {
                return true;
            }
            
            // 检查是否为审计管理员
            Object admin = session.getAttribute("admin");
            if (admin != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * 构建搜索条件描述
     */
    private String buildSearchCriteria(Map<String, Object> searchData) {
        StringBuilder criteria = new StringBuilder();
        
        if (searchData.get("username") != null && !searchData.get("username").toString().trim().isEmpty()) {
            criteria.append("用户名: ").append(searchData.get("username")).append("; ");
        }
        if (searchData.get("action") != null && !searchData.get("action").toString().trim().isEmpty()) {
            criteria.append("操作类型: ").append(searchData.get("action")).append("; ");
        }
        if (searchData.get("tableName") != null && !searchData.get("tableName").toString().trim().isEmpty()) {
            criteria.append("表名: ").append(searchData.get("tableName")).append("; ");
        }
        if (searchData.get("startDate") != null && !searchData.get("startDate").toString().trim().isEmpty()) {
            criteria.append("开始日期: ").append(searchData.get("startDate")).append("; ");
        }
        if (searchData.get("endDate") != null && !searchData.get("endDate").toString().trim().isEmpty()) {
            criteria.append("结束日期: ").append(searchData.get("endDate")).append("; ");
        }
        if (searchData.get("ipAddress") != null && !searchData.get("ipAddress").toString().trim().isEmpty()) {
            criteria.append("IP地址: ").append(searchData.get("ipAddress")).append("; ");
        }
        
        return criteria.length() > 0 ? criteria.toString() : "无特定搜索条件";
    }
}
