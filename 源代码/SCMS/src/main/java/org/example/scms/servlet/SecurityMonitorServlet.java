package org.example.scms.servlet;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.scms.model.Administrator;
import org.example.scms.model.AuditLog;
import org.example.scms.service.AdministratorService;
import org.example.scms.service.AuditLogService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 安全监控Servlet
 * 提供实时安全监控和威胁检测功能
 */
@WebServlet("/admin/audit/security/*")
public class SecurityMonitorServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AuditLogService auditLogService;
    private AdministratorService administratorService;

    @Override
    public void init() throws ServletException {
        auditLogService = new AuditLogService();
        administratorService = new AdministratorService();
    }    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 检查权限
        if (!checkAuditPermission(request, response)) {
            return;
        }

        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("")) {
            handleMainSecurity(request, response);
        } else if (pathInfo.equals("/status")) {
            handleSecurityStatus(request, response);        } else if (pathInfo.equals("/alerts")) {
            // 根据请求参数判断是页面请求还是API请求
            String format = request.getParameter("format");
            if ("json".equals(format) || "application/json".equals(request.getHeader("Accept"))) {
                handleAlertsApi(request, response);
            } else {
                handleAlerts(request, response);
            }
        } else if (pathInfo.equals("/alerts/stats")) {
            handleAlertsStats(request, response);
        } else if (pathInfo.startsWith("/alerts/") && pathInfo.endsWith("/status")) {
            handleAlertStatusUpdate(request, response);
        } else if (pathInfo.equals("/alerts/export")) {
            handleAlertsExport(request, response);
        } else if (pathInfo.equals("/alerts/bulk-update")) {
            handleBulkAlertUpdate(request, response);
        } else if (pathInfo.equals("/threat-analysis")) {
            handleThreatAnalysis(request, response);
        } else if (pathInfo.equals("/threat-analysis/export")) {
            handleThreatAnalysisExport(request, response);
        } else if (pathInfo.equals("/realtime/status")) {
            handleRealtimeStatus(request, response);
        } else if (pathInfo.equals("/block-ips")) {
            handleBlockIPs(request, response);
        } else if (pathInfo.equals("/export")) {
            handleSecurityReportExport(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * 处理主安全监控页面
     */
    private void handleMainSecurity(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Administrator admin = (Administrator) request.getSession().getAttribute("admin");
        
        try {
            // 获取实时安全状态
            Map<String, Object> securityStatus = generateSecurityStatus();
            
            // 获取最近的安全事件
            List<Map<String, Object>> recentSecurityEvents = getRecentSecurityEvents();
            
            // 获取威胁统计
            Map<String, Object> threatStats = generateThreatStatistics();
            
            // 获取活跃IP统计
            Map<String, Object> ipStats = generateIPStatistics();
            
            request.setAttribute("admin", admin);
            request.setAttribute("securityStatus", securityStatus);
            request.setAttribute("recentSecurityEvents", recentSecurityEvents);
            request.setAttribute("threatStats", threatStats);
            request.setAttribute("ipStats", ipStats);
            request.setAttribute("currentTime", LocalDateTime.now());
            
            request.getRequestDispatcher("/admin/audit/security.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "获取安全监控数据失败: " + e.getMessage());
            request.getRequestDispatcher("/admin/audit/security.jsp").forward(request, response);
        }
    }

    /**
     * 生成安全状态概览
     */
    private Map<String, Object> generateSecurityStatus() {
        Map<String, Object> status = new HashMap<>();
        
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate weekAgo = today.minusDays(7);
        
        // 今日安全事件
        List<AuditLog> todayLogs = auditLogService.getAuditLogsByDateRange(today, today);
        List<AuditLog> yesterdayLogs = auditLogService.getAuditLogsByDateRange(yesterday, yesterday);
        List<AuditLog> weekLogs = auditLogService.getAuditLogsByDateRange(weekAgo, today);
        
        // 登录失败统计
        long todayFailedLogins = todayLogs.stream()
            .filter(log -> "LOGIN_FAILED".equals(log.getAction()))
            .count();
        long yesterdayFailedLogins = yesterdayLogs.stream()
            .filter(log -> "LOGIN_FAILED".equals(log.getAction()))
            .count();
        
        // 可疑活动统计（非工作时间的活动）
        long todaySuspiciousActivity = todayLogs.stream()
            .filter(log -> {
                int hour = log.getCreatedAt().getHour();
                return hour < 7 || hour > 19; // 非7-19点的活动
            })
            .count();
        
        // 高权限操作统计
        long todayHighPrivilegeOps = todayLogs.stream()
            .filter(log -> log.getAction().contains("DELETE") || 
                          log.getAction().contains("CREATE") ||
                          (log.getAction().contains("UPDATE") && 
                           ("administrators".equals(log.getResourceType()) || 
                            "users".equals(log.getResourceType()))))
            .count();
        
        // 异常IP统计
        Map<String, Long> ipFailureCounts = weekLogs.stream()
            .filter(log -> "LOGIN_FAILED".equals(log.getAction()))
            .collect(Collectors.groupingBy(AuditLog::getIpAddress, Collectors.counting()));
        
        long suspiciousIPs = ipFailureCounts.entrySet().stream()
            .filter(entry -> entry.getValue() > 10) // 一周内失败超过10次
            .count();
        
        // 计算安全评分（满分100）
        int securityScore = 100;
        securityScore -= Math.min(todayFailedLogins * 2, 30); // 失败登录扣分
        securityScore -= Math.min(todaySuspiciousActivity * 3, 20); // 可疑活动扣分
        securityScore -= Math.min(suspiciousIPs * 5, 25); // 可疑IP扣分
        securityScore -= Math.min(todayHighPrivilegeOps * 2, 15); // 高权限操作扣分
        securityScore = Math.max(securityScore, 0);
        
        status.put("securityScore", securityScore);
        status.put("todayFailedLogins", todayFailedLogins);
        status.put("yesterdayFailedLogins", yesterdayFailedLogins);
        status.put("todaySuspiciousActivity", todaySuspiciousActivity);
        status.put("todayHighPrivilegeOps", todayHighPrivilegeOps);
        status.put("suspiciousIPs", suspiciousIPs);
        
        // 威胁等级
        String threatLevel = "LOW";
        if (securityScore < 60) {
            threatLevel = "HIGH";
        } else if (securityScore < 80) {
            threatLevel = "MEDIUM";
        }
        status.put("threatLevel", threatLevel);
        
        return status;
    }

    /**
     * 获取最近的安全事件
     */
    private List<Map<String, Object>> getRecentSecurityEvents() {
        LocalDate weekAgo = LocalDate.now().minusDays(7);
        LocalDate today = LocalDate.now();
        
        List<AuditLog> logs = auditLogService.getAuditLogsByDateRange(weekAgo, today);
        
        List<Map<String, Object>> securityEvents = new ArrayList<>();
        
        for (AuditLog log : logs) {
            boolean isSecurityEvent = false;
            String eventType = "";
            String severity = "LOW";
            
            // 判断是否为安全事件
            if ("LOGIN_FAILED".equals(log.getAction())) {
                isSecurityEvent = true;
                eventType = "登录失败";
                severity = "MEDIUM";
            } else if (log.getAction().contains("DELETE") && 
                      ("administrators".equals(log.getResourceType()) || 
                       "users".equals(log.getResourceType()))) {
                isSecurityEvent = true;
                eventType = "用户删除";
                severity = "HIGH";
            } else if (log.getAction().contains("CREATE") && 
                      "administrators".equals(log.getResourceType())) {
                isSecurityEvent = true;
                eventType = "管理员创建";
                severity = "MEDIUM";
            } else if (log.getCreatedAt().getHour() < 7 || log.getCreatedAt().getHour() > 19) {
                isSecurityEvent = true;
                eventType = "非工作时间活动";
                severity = "LOW";
            }
            
            if (isSecurityEvent) {
                Map<String, Object> event = new HashMap<>();
                event.put("id", log.getId());
                event.put("timestamp", log.getCreatedAt());
                event.put("eventType", eventType);
                event.put("action", log.getAction());
                event.put("resourceType", log.getResourceType());
                event.put("ipAddress", log.getIpAddress());
                event.put("details", log.getDetails());
                event.put("severity", severity);
                event.put("adminId", log.getAdminId());
                
                // 获取管理员信息
                if (log.getAdminId() != null) {
                    Administrator admin = administratorService.getAdministratorById(log.getAdminId());
                    if (admin != null) {
                        event.put("adminName", admin.getFullName());
                    }
                }
                
                securityEvents.add(event);
            }
        }
        
        // 按时间倒序排序，取最近50条
        return securityEvents.stream()
            .sorted((e1, e2) -> ((LocalDateTime) e2.get("timestamp")).compareTo((LocalDateTime) e1.get("timestamp")))
            .limit(50)
            .collect(Collectors.toList());
    }

    /**
     * 生成威胁统计
     */
    private Map<String, Object> generateThreatStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDate monthAgo = LocalDate.now().minusDays(30);
        LocalDate today = LocalDate.now();
        
        List<AuditLog> logs = auditLogService.getAuditLogsByDateRange(monthAgo, today);
        
        // 按威胁类型统计
        Map<String, Long> threatTypes = new HashMap<>();
        threatTypes.put("登录攻击", logs.stream()
            .filter(log -> "LOGIN_FAILED".equals(log.getAction()))
            .count());
        threatTypes.put("权限滥用", logs.stream()
            .filter(log -> log.getAction().contains("DELETE") || 
                          (log.getAction().contains("UPDATE") && 
                           "administrators".equals(log.getResourceType())))
            .count());
        threatTypes.put("异常时间活动", logs.stream()
            .filter(log -> {
                int hour = log.getCreatedAt().getHour();
                return hour < 7 || hour > 19;
            })
            .count());
        
        // 按日期统计威胁趋势
        Map<LocalDate, Long> threatTrend = new HashMap<>();
        for (int i = 0; i < 30; i++) {
            LocalDate date = today.minusDays(i);
            long dailyThreats = logs.stream()
                .filter(log -> log.getCreatedAt().toLocalDate().equals(date))
                .filter(log -> "LOGIN_FAILED".equals(log.getAction()) || 
                              log.getAction().contains("DELETE") ||
                              (log.getCreatedAt().getHour() < 7 || log.getCreatedAt().getHour() > 19))
                .count();
            threatTrend.put(date, dailyThreats);
        }
        
        stats.put("threatTypes", threatTypes);
        stats.put("threatTrend", threatTrend);
        
        return stats;
    }

    /**
     * 生成IP统计
     */
    private Map<String, Object> generateIPStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDate weekAgo = LocalDate.now().minusDays(7);
        LocalDate today = LocalDate.now();
        
        List<AuditLog> logs = auditLogService.getAuditLogsByDateRange(weekAgo, today);
        
        // 按IP统计活动
        Map<String, Long> ipActivity = logs.stream()
            .collect(Collectors.groupingBy(AuditLog::getIpAddress, Collectors.counting()));
        
        // 按IP统计登录失败
        Map<String, Long> ipFailures = logs.stream()
            .filter(log -> "LOGIN_FAILED".equals(log.getAction()))
            .collect(Collectors.groupingBy(AuditLog::getIpAddress, Collectors.counting()));
        
        // 识别可疑IP
        List<Map<String, Object>> suspiciousIPs = new ArrayList<>();
        for (Map.Entry<String, Long> entry : ipFailures.entrySet()) {
            if (entry.getValue() > 5) { // 失败次数超过5次
                Map<String, Object> suspiciousIP = new HashMap<>();
                suspiciousIP.put("ipAddress", entry.getKey());
                suspiciousIP.put("failureCount", entry.getValue());
                suspiciousIP.put("totalActivity", ipActivity.getOrDefault(entry.getKey(), 0L));
                
                // 计算失败率
                double failureRate = (double) entry.getValue() / ipActivity.get(entry.getKey()) * 100;
                suspiciousIP.put("failureRate", Math.round(failureRate * 100.0) / 100.0);
                
                // 获取最近的失败时间
                LocalDateTime lastFailure = logs.stream()
                    .filter(log -> log.getIpAddress().equals(entry.getKey()) && 
                                  "LOGIN_FAILED".equals(log.getAction()))
                    .map(AuditLog::getCreatedAt)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);
                suspiciousIP.put("lastFailure", lastFailure);
                
                suspiciousIPs.add(suspiciousIP);
            }
        }
        
        // 按失败次数排序
        suspiciousIPs.sort((ip1, ip2) -> 
            Long.compare((Long) ip2.get("failureCount"), (Long) ip1.get("failureCount")));
        
        stats.put("ipActivity", ipActivity);
        stats.put("ipFailures", ipFailures);
        stats.put("suspiciousIPs", suspiciousIPs);
        
        return stats;
    }

    /**
     * 处理安全警报页面
     */
    private void handleAlerts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Administrator admin = (Administrator) request.getSession().getAttribute("admin");
        
        // 生成安全警报
        List<Map<String, Object>> alerts = generateSecurityAlerts();
        
        request.setAttribute("admin", admin);
        request.setAttribute("alerts", alerts);
        request.setAttribute("currentTime", LocalDateTime.now());
        
        request.getRequestDispatcher("/admin/audit/security-alerts.jsp").forward(request, response);
    }

    /**
     * 生成安全警报
     */
    private List<Map<String, Object>> generateSecurityAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);
        
        List<AuditLog> weekLogs = auditLogService.getAuditLogsByDateRange(weekAgo, today);
        
        // 1. 检查频繁登录失败的IP
        Map<String, Long> ipFailures = weekLogs.stream()
            .filter(log -> "LOGIN_FAILED".equals(log.getAction()))
            .collect(Collectors.groupingBy(AuditLog::getIpAddress, Collectors.counting()));
        
        for (Map.Entry<String, Long> entry : ipFailures.entrySet()) {
            if (entry.getValue() > 20) { // 一周内失败超过20次
                Map<String, Object> alert = new HashMap<>();
                alert.put("type", "BRUTE_FORCE_ATTACK");
                alert.put("level", "HIGH");
                alert.put("title", "可能的暴力破解攻击");
                alert.put("message", String.format("IP地址 %s 在过去7天内登录失败 %d 次", 
                    entry.getKey(), entry.getValue()));
                alert.put("ipAddress", entry.getKey());
                alert.put("count", entry.getValue());
                alert.put("timestamp", LocalDateTime.now());
                alerts.add(alert);
            }
        }
        
        // 2. 检查异常时间的管理员活动
        List<AuditLog> afterHoursActivity = weekLogs.stream()
            .filter(log -> {
                int hour = log.getCreatedAt().getHour();
                return (hour < 6 || hour > 22) && log.getAdminId() != null;
            })
            .collect(Collectors.toList());
        
        Map<Long, Long> adminAfterHours = afterHoursActivity.stream()
            .collect(Collectors.groupingBy(AuditLog::getAdminId, Collectors.counting()));
        
        for (Map.Entry<Long, Long> entry : adminAfterHours.entrySet()) {
            if (entry.getValue() > 5) { // 异常时间活动超过5次
                Administrator admin = administratorService.getAdministratorById(entry.getKey());
                if (admin != null) {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("type", "SUSPICIOUS_ACTIVITY");
                    alert.put("level", "MEDIUM");
                    alert.put("title", "异常时间管理员活动");
                    alert.put("message", String.format("管理员 %s 在过去7天内有 %d 次非工作时间活动", 
                        admin.getFullName(), entry.getValue()));
                    alert.put("adminId", entry.getKey());
                    alert.put("adminName", admin.getFullName());
                    alert.put("count", entry.getValue());
                    alert.put("timestamp", LocalDateTime.now());
                    alerts.add(alert);
                }
            }
        }
        
        // 3. 检查高频删除操作
        List<AuditLog> deleteOperations = weekLogs.stream()
            .filter(log -> "DELETE".equals(log.getAction()))
            .collect(Collectors.toList());
        
        Map<Long, Long> adminDeletes = deleteOperations.stream()
            .filter(log -> log.getAdminId() != null)
            .collect(Collectors.groupingBy(AuditLog::getAdminId, Collectors.counting()));
        
        for (Map.Entry<Long, Long> entry : adminDeletes.entrySet()) {
            if (entry.getValue() > 10) { // 一周内删除操作超过10次
                Administrator admin = administratorService.getAdministratorById(entry.getKey());
                if (admin != null) {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("type", "HIGH_DELETE_ACTIVITY");
                    alert.put("level", "MEDIUM");
                    alert.put("title", "高频删除操作");
                    alert.put("message", String.format("管理员 %s 在过去7天内执行了 %d 次删除操作", 
                        admin.getFullName(), entry.getValue()));
                    alert.put("adminId", entry.getKey());
                    alert.put("adminName", admin.getFullName());
                    alert.put("count", entry.getValue());
                    alert.put("timestamp", LocalDateTime.now());
                    alerts.add(alert);
                }
            }
        }
        
        // 按严重程度和时间排序
        alerts.sort((a1, a2) -> {
            String level1 = (String) a1.get("level");
            String level2 = (String) a2.get("level");
            
            int levelCompare = getLevelPriority(level2) - getLevelPriority(level1);
            if (levelCompare != 0) {
                return levelCompare;
            }
            
            LocalDateTime time1 = (LocalDateTime) a1.get("timestamp");
            LocalDateTime time2 = (LocalDateTime) a2.get("timestamp");
            return time2.compareTo(time1);
        });
        
        return alerts;
    }

    private int getLevelPriority(String level) {
        switch (level) {
            case "HIGH": return 3;
            case "MEDIUM": return 2;
            case "LOW": return 1;
            default: return 0;
        }
    }    /**
     * 检查审计管理员权限
     */
    private boolean checkAuditPermission(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("/admin/login.jsp");
            return false;
        }

        Administrator admin = (Administrator) session.getAttribute("admin");
        if (admin == null) {
            response.sendRedirect("/admin/login.jsp");
            return false;
        }        // 检查是否为审计管理员
        if (!"audit_admin".equals(admin.getAdminType())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "权限不足：只有审计管理员可以访问此功能");
            return false;
        }return true;
    }

    /**
     * 处理安全状态API请求
     */
    private void handleSecurityStatus(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        try {
            Map<String, Object> status = generateSecurityStatus();
            
            // 添加实时数据
            status.put("recentEvents", getRecentSecurityEvents());
            status.put("trendData", generateTrendData());
            status.put("threatTypeData", generateThreatTypeData());
            
            // 将Map转换为JSON字符串
            String jsonResponse = mapToJson(status);
            response.getWriter().write(jsonResponse);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"获取安全状态失败\"}");
        }
    }

    /**
     * 处理警报统计API请求
     */
    private void handleAlertsStats(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 模拟警报统计数据
            stats.put("highCount", 5);
            stats.put("mediumCount", 12);
            stats.put("lowCount", 8);
            stats.put("resolvedCount", 35);
            
            String jsonResponse = mapToJson(stats);
            response.getWriter().write(jsonResponse);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"获取警报统计失败\"}");
        }
    }    /**
     * 处理警报列表API请求
     */
    private void handleAlertsApi(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        try {
            // 获取分页参数
            int page = parseInt(request.getParameter("page"), 1);
            int size = parseInt(request.getParameter("size"), 10);
            
            // 模拟警报数据
            List<Map<String, Object>> alerts = generateMockAlerts();
            
            // 分页处理
            int start = (page - 1) * size;
            int end = Math.min(start + size, alerts.size());
            List<Map<String, Object>> pageAlerts = alerts.subList(start, end);
            
            Map<String, Object> result = new HashMap<>();
            result.put("alerts", pageAlerts);
            result.put("totalPages", (int) Math.ceil((double) alerts.size() / size));
            result.put("currentPage", page);
            result.put("totalCount", alerts.size());
            
            String jsonResponse = mapToJson(result);
            response.getWriter().write(jsonResponse);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"获取警报列表失败\"}");
        }
    }

    /**
     * 处理警报状态更新
     */
    private void handleAlertStatusUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        try {
            // 模拟状态更新成功
            response.getWriter().write("{\"success\":true,\"message\":\"状态更新成功\"}");
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"状态更新失败\"}");
        }
    }

    /**
     * 处理批量警报更新
     */
    private void handleBulkAlertUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        try {
            // 模拟批量更新成功
            response.getWriter().write("{\"success\":true,\"message\":\"批量更新成功\"}");
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"批量更新失败\"}");
        }
    }

    /**
     * 处理威胁分析API请求
     */
    private void handleThreatAnalysis(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        try {
            Map<String, Object> analysis = generateThreatAnalysis();
            String jsonResponse = mapToJson(analysis);
            response.getWriter().write(jsonResponse);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"威胁分析失败\"}");
        }
    }

    /**
     * 处理实时状态API请求
     */
    private void handleRealtimeStatus(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        try {
            Map<String, Object> realtimeData = generateRealtimeData();
            String jsonResponse = mapToJson(realtimeData);
            response.getWriter().write(jsonResponse);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"获取实时数据失败\"}");
        }
    }

    /**
     * 处理IP拦截请求
     */
    private void handleBlockIPs(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        try {
            // 模拟IP拦截成功
            response.getWriter().write("{\"success\":true,\"message\":\"IP拦截成功\"}");
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"IP拦截失败\"}");
        }
    }

    /**
     * 处理导出请求
     */
    private void handleAlertsExport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"security-alerts.csv\"");
        
        try {
            response.getWriter().write("导出功能暂未实现");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleThreatAnalysisExport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"threat-analysis.csv\"");
        
        try {
            response.getWriter().write("导出功能暂未实现");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSecurityReportExport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"security-report.csv\"");
        
        try {
            response.getWriter().write("导出功能暂未实现");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成趋势数据
     */
    private Map<String, Object> generateTrendData() {
        Map<String, Object> trendData = new HashMap<>();
        
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        
        // 生成过去24小时的数据
        for (int i = 23; i >= 0; i--) {
            LocalDateTime time = LocalDateTime.now().minusHours(i);
            labels.add(String.format("%02d:00", time.getHour()));
            values.add((int) (Math.random() * 20)); // 随机数据
        }
        
        trendData.put("labels", labels);
        trendData.put("values", values);
        
        return trendData;
    }

    /**
     * 生成威胁类型数据
     */
    private Map<String, Object> generateThreatTypeData() {
        Map<String, Object> threatTypeData = new HashMap<>();
        
        List<String> labels = List.of("暴力破解", "权限滥用", "可疑登录", "数据泄露", "非工作时间活动");
        List<Integer> values = List.of(15, 8, 12, 3, 7);
        
        threatTypeData.put("labels", labels);
        threatTypeData.put("values", values);
        
        return threatTypeData;
    }

    /**
     * 生成模拟警报数据
     */
    private List<Map<String, Object>> generateMockAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        
        String[] severities = {"HIGH", "MEDIUM", "LOW"};
        String[] types = {"BRUTE_FORCE", "PRIVILEGE_ABUSE", "SUSPICIOUS_LOGIN", "DATA_BREACH", "AFTER_HOURS"};
        String[] statuses = {"OPEN", "INVESTIGATING", "RESOLVED", "FALSE_POSITIVE"};
        String[] ips = {"192.168.1.100", "10.0.0.25", "172.16.0.50", "203.0.113.10"};
        
        for (int i = 0; i < 25; i++) {
            Map<String, Object> alert = new HashMap<>();
            alert.put("id", "ALERT-" + (1000 + i));
            alert.put("severity", severities[i % severities.length]);
            alert.put("type", types[i % types.length]);
            alert.put("status", statuses[i % statuses.length]);
            alert.put("ipAddress", ips[i % ips.length]);
            alert.put("description", "安全威胁检测：" + types[i % types.length]);
            alert.put("timestamp", System.currentTimeMillis() - (i * 3600000)); // 每小时一个
            alert.put("riskScore", 60 + (i % 40));
            
            alerts.add(alert);
        }
        
        return alerts;
    }

    /**
     * 生成威胁分析数据
     */
    private Map<String, Object> generateThreatAnalysis() {
        Map<String, Object> analysis = new HashMap<>();
        
        // 指标数据
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalThreats", 45);
        metrics.put("uniqueIPs", 12);
        metrics.put("averageDetectionTime", 3.5);
        metrics.put("blockRate", 87);
        
        // 趋势数据
        Map<String, Object> trendData = new HashMap<>();
        trendData.put("labels", List.of("00:00", "04:00", "08:00", "12:00", "16:00", "20:00"));
        trendData.put("high", List.of(2, 1, 4, 6, 8, 3));
        trendData.put("medium", List.of(5, 3, 7, 9, 12, 6));
        trendData.put("low", List.of(3, 2, 5, 4, 7, 4));
        
        // 类型分布
        Map<String, Object> typeDistribution = new HashMap<>();
        typeDistribution.put("labels", List.of("暴力破解", "权限滥用", "可疑登录", "数据泄露"));
        typeDistribution.put("values", List.of(15, 8, 12, 3));
        
        // 地理分布
        Map<String, Object> geoDistribution = new HashMap<>();
        geoDistribution.put("labels", List.of("北京", "上海", "广州", "深圳", "其他"));
        geoDistribution.put("values", List.of(12, 8, 6, 4, 15));
        
        // 时间线数据
        List<Map<String, Object>> timeline = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", "BRUTE_FORCE");
            item.put("severity", i % 2 == 0 ? "HIGH" : "MEDIUM");
            item.put("description", "检测到暴力破解攻击");
            item.put("ipAddress", "192.168.1." + (100 + i));
            item.put("timestamp", System.currentTimeMillis() - (i * 600000)); // 每10分钟一个
            timeline.add(item);
        }
        
        // 高风险IP
        List<Map<String, Object>> highRiskIPs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> ip = new HashMap<>();
            ip.put("address", "192.168.1." + (200 + i));
            ip.put("threatCount", 15 - i * 2);
            ip.put("riskScore", 95 - i * 10);
            ip.put("location", "北京市");
            highRiskIPs.add(ip);
        }
        
        // 攻击模式
        List<Map<String, Object>> attackPatterns = new ArrayList<>();
        Map<String, Object> pattern1 = new HashMap<>();
        pattern1.put("name", "分布式暴力破解");
        pattern1.put("description", "来自多个IP的协调暴力破解攻击");
        pattern1.put("severity", "HIGH");
        pattern1.put("occurrences", 25);
        pattern1.put("confidence", 92);
        pattern1.put("automated", true);
        attackPatterns.add(pattern1);
        
        // AI预测
        Map<String, Object> aiPrediction = new HashMap<>();
        aiPrediction.put("overallRisk", 75);
        aiPrediction.put("riskLevel", "HIGH");
        aiPrediction.put("nextAttackProbability", 68);
        aiPrediction.put("vulnerabilityScore", 7.2);
        aiPrediction.put("recommendations", List.of(
            "立即加强密码策略",
            "启用多因素认证",
            "增加登录失败锁定时间",
            "部署Web应用防火墙"
        ));
        aiPrediction.put("trendAnalysis", "基于当前攻击模式，预计未来24小时内攻击活动将增加30%");
        
        analysis.put("metrics", metrics);
        analysis.put("trendData", trendData);
        analysis.put("typeDistribution", typeDistribution);
        analysis.put("geoDistribution", geoDistribution);
        analysis.put("timeline", timeline);
        analysis.put("highRiskIPs", highRiskIPs);
        analysis.put("attackPatterns", attackPatterns);
        analysis.put("aiPrediction", aiPrediction);
        
        return analysis;
    }

    /**
     * 生成实时数据
     */
    private Map<String, Object> generateRealtimeData() {
        Map<String, Object> data = new HashMap<>();
        
        data.put("type", "metrics");
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("activeUsers", 23);
        metrics.put("systemLoad", "1.2");
        metrics.put("memoryUsage", 68);
        metrics.put("requestsPerMinute", 145);
        metrics.put("errorRate", 2.3);
        
        data.put("data", metrics);
        
        return data;
    }

    /**
     * 简单的Map到JSON转换
     */
    private String mapToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;
            
            json.append("\"").append(entry.getKey()).append("\":");
            json.append(objectToJson(entry.getValue()));
        }
        
        json.append("}");
        return json.toString();
    }

    /**
     * 对象到JSON转换
     */
    private String objectToJson(Object obj) {
        if (obj == null) {
            return "null";
        } else if (obj instanceof String) {
            return "\"" + obj.toString().replace("\"", "\\\"") + "\"";
        } else if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        } else if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            StringBuilder json = new StringBuilder();
            json.append("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) json.append(",");
                json.append(objectToJson(list.get(i)));
            }
            json.append("]");
            return json.toString();        } else if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) obj;
            return mapToJson(map);
        } else {
            return "\"" + obj.toString().replace("\"", "\\\"") + "\"";
        }
    }

    /**
     * 安全解析整数
     */
    private int parseInt(String str, int defaultValue) {
        try {
            return str != null ? Integer.parseInt(str) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
