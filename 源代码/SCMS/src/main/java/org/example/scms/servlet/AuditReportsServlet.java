package org.example.scms.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
 * 审计报告Servlet
 * 提供审计报告生成和查看功能
 */
@WebServlet("/admin/audit/reports/*")
public class AuditReportsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AuditLogService auditLogService;
    private AdministratorService administratorService;

    @Override
    public void init() throws ServletException {
        auditLogService = new AuditLogService();
        administratorService = new AdministratorService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 检查权限
        if (!checkAuditPermission(request, response)) {
            return;
        }

        String pathInfo = request.getPathInfo();
        String action = request.getParameter("action");
        
        if ("export".equals(action)) {
            handleExport(request, response);
        } else if (pathInfo != null && pathInfo.equals("/activity")) {
            handleActivityReport(request, response);
        } else if (pathInfo != null && pathInfo.equals("/security")) {
            handleSecurityReport(request, response);
        } else if (pathInfo != null && pathInfo.equals("/admin")) {
            handleAdminReport(request, response);
        } else {
            handleMainReports(request, response);
        }
    }

    /**
     * 处理主报告页面
     */
    private void handleMainReports(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Administrator admin = (Administrator) request.getSession().getAttribute("admin");
        
        try {
            // 获取日期范围参数
            String startDateStr = request.getParameter("startDate");
            String endDateStr = request.getParameter("endDate");
            String reportType = request.getParameter("reportType");
            
            LocalDate startDate = LocalDate.now().minusDays(30); // 默认最近30天
            LocalDate endDate = LocalDate.now();
            
            try {
                if (startDateStr != null && !startDateStr.isEmpty()) {
                    startDate = LocalDate.parse(startDateStr);
                }
                if (endDateStr != null && !endDateStr.isEmpty()) {
                    endDate = LocalDate.parse(endDateStr);
                }
            } catch (DateTimeParseException e) {
                request.setAttribute("warning", "日期格式错误，使用默认日期范围");
            }
            
            // 限制查询范围不超过90天
            if (startDate.isBefore(LocalDate.now().minusDays(90))) {
                startDate = LocalDate.now().minusDays(90);
                request.setAttribute("warning", "查询范围已限制在最近90天内");
            }
            
            Map<String, Object> reportData = new HashMap<>();
            
            if (reportType == null || reportType.isEmpty() || "overview".equals(reportType)) {
                reportData = generateOverviewReport(startDate, endDate);
            } else if ("activity".equals(reportType)) {
                reportData = generateActivityReport(startDate, endDate);
            } else if ("security".equals(reportType)) {
                reportData = generateSecurityReport(startDate, endDate);
            } else if ("admin".equals(reportType)) {
                reportData = generateAdminReport(startDate, endDate);
            }
            
            request.setAttribute("admin", admin);
            request.setAttribute("reportData", reportData);
            request.setAttribute("startDate", startDate);
            request.setAttribute("endDate", endDate);
            request.setAttribute("reportType", reportType != null ? reportType : "overview");
            
            request.getRequestDispatcher("/admin/audit/reports.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "生成报告失败: " + e.getMessage());
            request.getRequestDispatcher("/admin/audit/reports.jsp").forward(request, response);
        }
    }

    /**
     * 生成概览报告
     */
    private Map<String, Object> generateOverviewReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        
        // 获取指定时间范围内的所有日志
        List<AuditLog> logs = auditLogService.getAuditLogsByDateRange(startDate, endDate);
        
        // 基本统计
        report.put("totalLogs", logs.size());
        
        // 按操作类型统计
        Map<String, Long> actionStats = logs.stream()
            .collect(Collectors.groupingBy(AuditLog::getAction, Collectors.counting()));
        report.put("actionStats", actionStats);
        
        // 按资源类型统计
        Map<String, Long> resourceStats = logs.stream()
            .collect(Collectors.groupingBy(AuditLog::getResourceType, Collectors.counting()));
        report.put("resourceStats", resourceStats);
        
        // 按日期统计
        Map<LocalDate, Long> dailyStats = logs.stream()
            .collect(Collectors.groupingBy(
                log -> log.getCreatedAt().toLocalDate(), 
                Collectors.counting()
            ));
        report.put("dailyStats", dailyStats);
        
        // 登录统计
        long successfulLogins = logs.stream()
            .filter(log -> "LOGIN_SUCCESS".equals(log.getAction()))
            .count();
        long failedLogins = logs.stream()
            .filter(log -> "LOGIN_FAILED".equals(log.getAction()))
            .count();
        
        report.put("successfulLogins", successfulLogins);
        report.put("failedLogins", failedLogins);
        
        return report;
    }

    /**
     * 生成活动报告
     */
    private Map<String, Object> generateActivityReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        
        List<AuditLog> logs = auditLogService.getAuditLogsByDateRange(startDate, endDate);
        
        // 管理员活动统计
        Map<Long, Long> adminActivityStats = logs.stream()
            .filter(log -> log.getAdminId() != null)
            .collect(Collectors.groupingBy(AuditLog::getAdminId, Collectors.counting()));
        
        // 获取管理员详细信息
        Map<Long, Map<String, Object>> adminDetails = new HashMap<>();
        for (Long adminId : adminActivityStats.keySet()) {
            Administrator admin = administratorService.getAdministratorById(adminId);            if (admin != null) {
                Map<String, Object> details = new HashMap<>();
                details.put("fullName", admin.getFullName());
                details.put("username", admin.getUsername());
                details.put("role", admin.getAdminType());
                details.put("activityCount", adminActivityStats.get(adminId));
                adminDetails.put(adminId, details);
            }
        }
        
        // 高频操作统计
        Map<String, Long> highFrequencyActions = logs.stream()
            .collect(Collectors.groupingBy(AuditLog::getAction, Collectors.counting()))
            .entrySet().stream()
            .filter(entry -> entry.getValue() > 5) // 超过5次的操作
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));
        
        // 时间段活动分布（按小时）
        Map<Integer, Long> hourlyActivity = logs.stream()
            .collect(Collectors.groupingBy(
                log -> log.getCreatedAt().getHour(),
                Collectors.counting()
            ));
        
        report.put("adminActivityStats", adminActivityStats);
        report.put("adminDetails", adminDetails);
        report.put("highFrequencyActions", highFrequencyActions);
        report.put("hourlyActivity", hourlyActivity);
        
        return report;
    }

    /**
     * 生成安全报告
     */
    private Map<String, Object> generateSecurityReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        
        List<AuditLog> logs = auditLogService.getAuditLogsByDateRange(startDate, endDate);
        
        // 登录失败统计
        List<AuditLog> failedLogins = logs.stream()
            .filter(log -> "LOGIN_FAILED".equals(log.getAction()))
            .collect(Collectors.toList());
        
        // 按IP地址统计失败登录
        Map<String, Long> failedLoginsByIP = failedLogins.stream()
            .collect(Collectors.groupingBy(AuditLog::getIpAddress, Collectors.counting()));
        
        // 可疑IP（失败登录次数超过5次）
        Map<String, Long> suspiciousIPs = failedLoginsByIP.entrySet().stream()
            .filter(entry -> entry.getValue() > 5)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));
        
        // 权限相关操作
        List<AuditLog> privilegeOperations = logs.stream()
            .filter(log -> log.getAction().contains("CREATE") || 
                          log.getAction().contains("DELETE") ||
                          log.getAction().contains("UPDATE"))
            .filter(log -> "administrators".equals(log.getResourceType()) ||
                          "users".equals(log.getResourceType()))
            .collect(Collectors.toList());
        
        // 异常操作时间（非工作时间的操作）
        List<AuditLog> afterHoursActivity = logs.stream()
            .filter(log -> {
                int hour = log.getCreatedAt().getHour();
                return hour < 8 || hour > 18; // 非8-18点的操作
            })
            .collect(Collectors.toList());
        
        report.put("failedLogins", failedLogins);
        report.put("failedLoginsByIP", failedLoginsByIP);
        report.put("suspiciousIPs", suspiciousIPs);
        report.put("privilegeOperations", privilegeOperations);
        report.put("afterHoursActivity", afterHoursActivity);
        
        return report;
    }

    /**
     * 生成管理员报告
     */
    private Map<String, Object> generateAdminReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        
        List<AuditLog> logs = auditLogService.getAuditLogsByDateRange(startDate, endDate);
        
        // 管理员操作统计
        Map<Long, List<AuditLog>> adminLogs = logs.stream()
            .filter(log -> log.getAdminId() != null)
            .collect(Collectors.groupingBy(AuditLog::getAdminId));
        
        Map<Long, Map<String, Object>> adminReports = new HashMap<>();
        
        for (Map.Entry<Long, List<AuditLog>> entry : adminLogs.entrySet()) {
            Long adminId = entry.getKey();
            List<AuditLog> adminLogList = entry.getValue();
            
            Administrator admin = administratorService.getAdministratorById(adminId);
            if (admin != null) {
                Map<String, Object> adminReport = new HashMap<>();
                adminReport.put("admin", admin);
                adminReport.put("totalOperations", adminLogList.size());
                
                // 操作类型分布
                Map<String, Long> actionDistribution = adminLogList.stream()
                    .collect(Collectors.groupingBy(AuditLog::getAction, Collectors.counting()));
                adminReport.put("actionDistribution", actionDistribution);
                
                // 最近活动
                List<AuditLog> recentActivity = adminLogList.stream()
                    .sorted((log1, log2) -> log2.getCreatedAt().compareTo(log1.getCreatedAt()))
                    .limit(10)
                    .collect(Collectors.toList());
                adminReport.put("recentActivity", recentActivity);
                
                // 登录统计
                long loginAttempts = adminLogList.stream()
                    .filter(log -> log.getAction().contains("LOGIN"))
                    .count();
                long successfulLogins = adminLogList.stream()
                    .filter(log -> "LOGIN_SUCCESS".equals(log.getAction()))
                    .count();
                
                adminReport.put("loginAttempts", loginAttempts);
                adminReport.put("successfulLogins", successfulLogins);
                
                adminReports.put(adminId, adminReport);
            }
        }
        
        report.put("adminReports", adminReports);
        
        return report;
    }

    /**
     * 处理活动报告页面
     */
    private void handleActivityReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("reportType", "activity");
        handleMainReports(request, response);
    }

    /**
     * 处理安全报告页面
     */
    private void handleSecurityReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("reportType", "security");
        handleMainReports(request, response);
    }

    /**
     * 处理管理员报告页面
     */
    private void handleAdminReport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("reportType", "admin");
        handleMainReports(request, response);
    }

    /**
     * 处理报告导出
     */
    private void handleExport(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String reportType = request.getParameter("reportType");
        String format = request.getParameter("format");
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        
        if (!"csv".equalsIgnoreCase(format)) {
            request.setAttribute("error", "目前只支持CSV格式导出");
            handleMainReports(request, response);
            return;
        }
        
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();
        
        try {
            if (startDateStr != null && !startDateStr.isEmpty()) {
                startDate = LocalDate.parse(startDateStr);
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                endDate = LocalDate.parse(endDateStr);
            }
        } catch (DateTimeParseException e) {
            startDate = LocalDate.now().minusDays(30);
            endDate = LocalDate.now();
        }
        
        try {
            exportReportToCsv(response, reportType, startDate, endDate);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "导出失败");
        }
    }

    /**
     * 导出报告为CSV格式
     */
    private void exportReportToCsv(HttpServletResponse response, String reportType, 
                                  LocalDate startDate, LocalDate endDate) throws IOException {
        
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        
        String filename = String.format("audit_report_%s_%s_to_%s.csv", 
            reportType != null ? reportType : "overview", startDate, endDate);
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        
        PrintWriter writer = response.getWriter();
        
        // 写入BOM以支持中文
        writer.write('\uFEFF');
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        if ("activity".equals(reportType)) {
            exportActivityReportToCsv(writer, startDate, endDate, formatter);
        } else if ("security".equals(reportType)) {
            exportSecurityReportToCsv(writer, startDate, endDate, formatter);
        } else if ("admin".equals(reportType)) {
            exportAdminReportToCsv(writer, startDate, endDate, formatter);
        } else {
            exportOverviewReportToCsv(writer, startDate, endDate, formatter);
        }
        
        writer.flush();
    }

    private void exportOverviewReportToCsv(PrintWriter writer, LocalDate startDate, LocalDate endDate, 
                                         DateTimeFormatter formatter) {
        writer.println("审计日志概览报告");
        writer.println("生成时间," + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        writer.println("统计时间范围," + startDate + " 至 " + endDate);
        writer.println();
        
        Map<String, Object> report = generateOverviewReport(startDate, endDate);
        
        writer.println("基本统计");
        writer.println("总日志条数," + report.get("totalLogs"));
        writer.println("成功登录次数," + report.get("successfulLogins"));
        writer.println("失败登录次数," + report.get("failedLogins"));
        writer.println();
        
        writer.println("操作类型统计");
        writer.println("操作类型,次数");
        Map<String, Long> actionStats = (Map<String, Long>) report.get("actionStats");
        for (Map.Entry<String, Long> entry : actionStats.entrySet()) {
            writer.printf("\"%s\",%d%n", entry.getKey(), entry.getValue());
        }
        writer.println();
        
        writer.println("资源类型统计");
        writer.println("资源类型,次数");
        Map<String, Long> resourceStats = (Map<String, Long>) report.get("resourceStats");
        for (Map.Entry<String, Long> entry : resourceStats.entrySet()) {
            writer.printf("\"%s\",%d%n", entry.getKey(), entry.getValue());
        }
    }

    private void exportActivityReportToCsv(PrintWriter writer, LocalDate startDate, LocalDate endDate, 
                                         DateTimeFormatter formatter) {
        writer.println("活动报告");
        writer.println("生成时间," + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        writer.println("统计时间范围," + startDate + " 至 " + endDate);
        writer.println();
        
        Map<String, Object> report = generateActivityReport(startDate, endDate);
        
        writer.println("管理员活动统计");
        writer.println("管理员ID,姓名,用户名,角色,操作次数");
        
        Map<Long, Map<String, Object>> adminDetails = (Map<Long, Map<String, Object>>) report.get("adminDetails");
        for (Map.Entry<Long, Map<String, Object>> entry : adminDetails.entrySet()) {
            Map<String, Object> details = entry.getValue();
            writer.printf("%d,\"%s\",\"%s\",\"%s\",%d%n",
                entry.getKey(),
                details.get("fullName"),
                details.get("username"),
                details.get("role"),
                details.get("activityCount")
            );
        }
    }

    private void exportSecurityReportToCsv(PrintWriter writer, LocalDate startDate, LocalDate endDate, 
                                         DateTimeFormatter formatter) {
        writer.println("安全报告");
        writer.println("生成时间," + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        writer.println("统计时间范围," + startDate + " 至 " + endDate);
        writer.println();
        
        Map<String, Object> report = generateSecurityReport(startDate, endDate);
        
        writer.println("登录失败统计");
        writer.println("IP地址,失败次数");
        Map<String, Long> failedLoginsByIP = (Map<String, Long>) report.get("failedLoginsByIP");
        for (Map.Entry<String, Long> entry : failedLoginsByIP.entrySet()) {
            writer.printf("\"%s\",%d%n", entry.getKey(), entry.getValue());
        }
        writer.println();
        
        writer.println("可疑IP地址");
        writer.println("IP地址,失败次数");
        Map<String, Long> suspiciousIPs = (Map<String, Long>) report.get("suspiciousIPs");
        for (Map.Entry<String, Long> entry : suspiciousIPs.entrySet()) {
            writer.printf("\"%s\",%d%n", entry.getKey(), entry.getValue());
        }
    }

    private void exportAdminReportToCsv(PrintWriter writer, LocalDate startDate, LocalDate endDate, 
                                      DateTimeFormatter formatter) {
        writer.println("管理员报告");
        writer.println("生成时间," + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        writer.println("统计时间范围," + startDate + " 至 " + endDate);
        writer.println();
        
        Map<String, Object> report = generateAdminReport(startDate, endDate);
        
        writer.println("管理员操作统计");
        writer.println("管理员ID,姓名,总操作数,登录尝试次数,成功登录次数");
        
        Map<Long, Map<String, Object>> adminReports = (Map<Long, Map<String, Object>>) report.get("adminReports");
        for (Map.Entry<Long, Map<String, Object>> entry : adminReports.entrySet()) {
            Map<String, Object> adminReport = entry.getValue();
            Administrator admin = (Administrator) adminReport.get("admin");
            writer.printf("%d,\"%s\",%d,%d,%d%n",
                entry.getKey(),
                admin.getFullName(),
                adminReport.get("totalOperations"),
                adminReport.get("loginAttempts"),
                adminReport.get("successfulLogins")
            );
        }
    }

    /**
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
        }

        return true;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
