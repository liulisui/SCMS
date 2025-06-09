package org.example.scms.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.example.scms.model.Administrator;
import org.example.scms.model.AuditLog;
import org.example.scms.service.AdministratorService;
import org.example.scms.service.AuditLogService;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 审计日志Servlet
 */
@WebServlet("/admin/audit/logs/*")
public class AuditLogServlet extends HttpServlet {    private static final long serialVersionUID = 1L;
    private AuditLogService auditLogService;
    private AdministratorService administratorService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        auditLogService = new AuditLogService();
        administratorService = new AdministratorService();
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 检查权限
        if (!checkAuditPermission(request, response)) {
            return;
        }

        String pathInfo = request.getPathInfo();
        
        if (pathInfo != null && pathInfo.length() > 1) {
            // 处理获取特定日志详情的请求 /admin/audit/logs/{id}
            handleLogDetail(request, response, pathInfo);
        } else {
            // 处理日志列表请求
            handleLogList(request, response);
        }
    }

    /**
     * 处理日志列表请求
     */
    private void handleLogList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
          // 检查是否为导出请求
        if ("true".equals(request.getParameter("export"))) {
            Administrator admin = (Administrator) request.getSession().getAttribute("admin");
            handleExport(request, response, admin);
            return;
        }

        // 获取搜索参数
        String action = request.getParameter("action");
        String resourceType = request.getParameter("resourceType");
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        String keyword = request.getParameter("keyword");
        
        // 分页参数
        int page = 1;
        try {
            String pageStr = request.getParameter("page");
            if (pageStr != null && !pageStr.isEmpty()) {
                page = Integer.parseInt(pageStr);
            }
        } catch (NumberFormatException e) {
            page = 1;
        }
        
        int pageSize = 20; // 每页显示20条记录
        
        // 解析日期
        LocalDate startDate = null;
        LocalDate endDate = null;
        
        try {
            if (startDateStr != null && !startDateStr.isEmpty()) {
                startDate = LocalDate.parse(startDateStr);
            } else {
                // 默认显示最近一周的数据
                startDate = LocalDate.now().minusDays(7);
            }
            
            if (endDateStr != null && !endDateStr.isEmpty()) {
                endDate = LocalDate.parse(endDateStr);
            } else {
                endDate = LocalDate.now();
            }
        } catch (DateTimeParseException e) {
            startDate = LocalDate.now().minusDays(7);
            endDate = LocalDate.now();
        }

        try {
            // 查询日志
            List<AuditLog> allLogs;
            if (action != null || resourceType != null || keyword != null) {
                // 使用高级搜索
                allLogs = auditLogService.searchAuditLogs(null, action, resourceType, 
                    startDate, endDate, keyword);
            } else {
                // 按日期范围查询
                allLogs = auditLogService.getAuditLogsByDateRange(startDate, endDate);
            }

            // 计算分页
            int totalCount = allLogs.size();
            int totalPages = (int) Math.ceil((double) totalCount / pageSize);
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, totalCount);
            
            List<AuditLog> paginatedLogs = totalCount > 0 ? 
                allLogs.subList(startIndex, endIndex) : new ArrayList<>();

            // 设置分页属性
            request.setAttribute("auditLogs", paginatedLogs);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalCount", totalCount);
            
            // 计算显示的页码范围
            int startPage = Math.max(1, page - 2);
            int endPage = Math.min(totalPages, page + 2);
            request.setAttribute("startPage", startPage);
            request.setAttribute("endPage", endPage);
            
            // 构建查询字符串用于分页链接
            StringBuilder queryString = new StringBuilder();
            if (action != null && !action.isEmpty()) {
                queryString.append("action=").append(action).append("&");
            }
            if (resourceType != null && !resourceType.isEmpty()) {
                queryString.append("resourceType=").append(resourceType).append("&");
            }
            if (startDateStr != null && !startDateStr.isEmpty()) {
                queryString.append("startDate=").append(startDateStr).append("&");
            }
            if (endDateStr != null && !endDateStr.isEmpty()) {
                queryString.append("endDate=").append(endDateStr).append("&");
            }
            if (keyword != null && !keyword.isEmpty()) {
                queryString.append("keyword=").append(keyword).append("&");
            }
            
            String queryStr = queryString.toString();
            if (queryStr.endsWith("&")) {
                queryStr = queryStr.substring(0, queryStr.length() - 1);
            }
            request.setAttribute("queryString", queryStr);

            // 转发到JSP页面
            request.getRequestDispatcher("/admin/audit/audit-logs.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取审计日志失败");
        }
    }

    /**
     * 处理日志详情请求
     */
    private void handleLogDetail(HttpServletRequest request, HttpServletResponse response,
            String pathInfo) throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // 从路径中提取日志ID
            String idStr = pathInfo.substring(1); // 去掉开头的 '/'
            Long logId = Long.parseLong(idStr);
            
            AuditLog log = auditLogService.getAuditLogById(logId);
            
            PrintWriter out = response.getWriter();
            if (log != null) {
                // 返回成功响应
                String jsonResponse = String.format(
                    "{\"success\": true, \"log\": %s}",
                    objectMapper.writeValueAsString(log)
                );
                out.print(jsonResponse);
            } else {
                // 返回失败响应
                out.print("{\"success\": false, \"message\": \"日志不存在\"}");
            }
            out.flush();
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的日志ID");
        } catch (Exception e) {
            e.printStackTrace();
            PrintWriter out = response.getWriter();
            out.print("{\"success\": false, \"message\": \"获取日志详情失败\"}");
            out.flush();
        }
    }

    /**
     * 处理统计信息请求
     */
    private void handleStatistics(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String period = request.getParameter("period");
            if (period == null) period = "week";
            
            LocalDate startDate;
            LocalDate endDate = LocalDate.now();
            
            switch (period) {
                case "today":
                    startDate = LocalDate.now();
                    break;
                case "week":
                    startDate = LocalDate.now().minusDays(7);
                    break;
                case "month":
                    startDate = LocalDate.now().minusMonths(1);
                    break;
                case "quarter":
                    startDate = LocalDate.now().minusMonths(3);
                    break;
                default:
                    startDate = LocalDate.now().minusDays(7);
            }
            
            // 获取统计数据
            List<AuditLog> logs = auditLogService.getAuditLogsByDateRange(startDate, endDate);
            
            // 计算各种统计指标
            long totalOperations = logs.size();
            long userOperations = logs.stream()
                .filter(log -> "VIEW".equals(log.getAction()) || "QUERY".equals(log.getAction()) || 
                              "CREATE".equals(log.getAction()))
                .count();
            long adminOperations = logs.stream()
                .filter(log -> "UPDATE".equals(log.getAction()) || "DELETE".equals(log.getAction()) ||
                              "APPROVE".equals(log.getAction()) || "REJECT".equals(log.getAction()))
                .count();
            long loginOperations = logs.stream()
                .filter(log -> "LOGIN".equals(log.getAction()) || "LOGOUT".equals(log.getAction()))
                .count();
            
            // 构建JSON响应
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{")
                .append("\"success\": true,")
                .append("\"statistics\": {")
                .append("\"totalOperations\": ").append(totalOperations).append(",")
                .append("\"userOperations\": ").append(userOperations).append(",")
                .append("\"adminOperations\": ").append(adminOperations).append(",")
                .append("\"loginOperations\": ").append(loginOperations).append(",")
                .append("\"period\": \"").append(period).append("\"")
                .append("}")
                .append("}");
            
            PrintWriter out = response.getWriter();
            out.print(jsonBuilder.toString());
            out.flush();
            
        } catch (Exception e) {
            e.printStackTrace();
            PrintWriter out = response.getWriter();
            out.print("{\"success\": false, \"message\": \"获取统计信息失败\"}");
            out.flush();
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

    /**
     * 处理日志列表显示
     */
    private void handleList(HttpServletRequest request, HttpServletResponse response, Administrator admin)
            throws ServletException, IOException {
        
        // 默认显示最近7天的日志
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);
        
        List<AuditLog> logs = auditLogService.getAuditLogsByDateRange(startDate, endDate);
        
        // 获取所有管理员列表，用于筛选
        List<Administrator> administrators = administratorService.getAllAdministrators();
        
        request.setAttribute("admin", admin);
        request.setAttribute("logs", logs);
        request.setAttribute("administrators", administrators);
        request.setAttribute("startDate", startDate);
        request.setAttribute("endDate", endDate);
        request.setAttribute("totalCount", logs.size());
        
        request.getRequestDispatcher("/admin/audit/logs.jsp").forward(request, response);
    }

    /**
     * 处理日志搜索
     */
    private void handleSearch(HttpServletRequest request, HttpServletResponse response, Administrator admin)
            throws ServletException, IOException {
        
        // 获取搜索参数
        String adminIdStr = request.getParameter("adminId");
        String actionType = request.getParameter("actionType");
        String resourceType = request.getParameter("resourceType");
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        String keyword = request.getParameter("keyword");
        String ipAddress = request.getParameter("ipAddress");
        
        Long adminId = null;
        if (adminIdStr != null && !adminIdStr.trim().isEmpty() && !"0".equals(adminIdStr)) {
            try {
                adminId = Long.parseLong(adminIdStr);
            } catch (NumberFormatException e) {
                // 忽略无效的管理员ID
            }
        }
        
        LocalDate startDate = LocalDate.now().minusDays(30); // 默认最近30天
        LocalDate endDate = LocalDate.now();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        try {
            if (startDateStr != null && !startDateStr.trim().isEmpty()) {
                startDate = LocalDate.parse(startDateStr, formatter);
            }
            if (endDateStr != null && !endDateStr.trim().isEmpty()) {
                endDate = LocalDate.parse(endDateStr, formatter);
            }
        } catch (DateTimeParseException e) {
            request.setAttribute("error", "日期格式错误，请使用 YYYY-MM-DD 格式");
            handleList(request, response, admin);
            return;
        }
        
        // 确保开始日期不晚于结束日期
        if (startDate.isAfter(endDate)) {
            request.setAttribute("error", "开始日期不能晚于结束日期");
            handleList(request, response, admin);
            return;
        }
        
        // 限制查询范围不超过6个月
        if (startDate.isBefore(LocalDate.now().minusMonths(6))) {
            startDate = LocalDate.now().minusMonths(6);
            request.setAttribute("warning", "查询范围已限制在最近6个月内");
        }
        
        List<AuditLog> logs;
          if (ipAddress != null && !ipAddress.trim().isEmpty()) {
            // 如果指定了IP地址，使用IP地址查询
            logs = auditLogService.getAuditLogsByIpAddress(ipAddress.trim());
            // 然后根据日期范围过滤
            final LocalDate finalStartDate = startDate;
            final LocalDate finalEndDate = endDate;
            logs = logs.stream()
                    .filter(log -> !log.getCreatedAt().toLocalDate().isBefore(finalStartDate) 
                            && !log.getCreatedAt().toLocalDate().isAfter(finalEndDate))
                    .collect(java.util.stream.Collectors.toList());
        } else {
            // 多条件搜索
            logs = auditLogService.searchAuditLogs(adminId, 
                    (actionType != null && !actionType.trim().isEmpty()) ? actionType : null,
                    (resourceType != null && !resourceType.trim().isEmpty()) ? resourceType : null,
                    startDate, endDate, keyword);
        }
        
        // 获取所有管理员列表，用于筛选
        List<Administrator> administrators = administratorService.getAllAdministrators();
        
        request.setAttribute("admin", admin);
        request.setAttribute("logs", logs);
        request.setAttribute("administrators", administrators);
        request.setAttribute("searchAdminId", adminId);
        request.setAttribute("searchActionType", actionType);
        request.setAttribute("searchResourceType", resourceType);
        request.setAttribute("startDate", startDate);
        request.setAttribute("endDate", endDate);
        request.setAttribute("searchKeyword", keyword);
        request.setAttribute("searchIpAddress", ipAddress);
        request.setAttribute("totalCount", logs.size());
        request.setAttribute("isSearchResult", true);
        
        request.getRequestDispatcher("/admin/audit/logs.jsp").forward(request, response);
    }

    /**
     * 处理日志详情查看
     */
    private void handleView(HttpServletRequest request, HttpServletResponse response, Administrator admin)
            throws ServletException, IOException {
        
        String logIdStr = request.getParameter("id");
        if (logIdStr == null || logIdStr.trim().isEmpty()) {
            request.setAttribute("error", "缺少日志ID参数");
            handleList(request, response, admin);
            return;
        }
        
        try {
            Long logId = Long.parseLong(logIdStr);
            AuditLog log = auditLogService.getAuditLogById(logId);
            
            if (log == null) {
                request.setAttribute("error", "找不到指定的日志记录");
                handleList(request, response, admin);
                return;
            }
            
            // 获取相关管理员信息
            Administrator logAdmin = null;
            if (log.getAdminId() != null) {
                logAdmin = administratorService.getAdministratorById(log.getAdminId());
            }
            
            request.setAttribute("admin", admin);
            request.setAttribute("auditLog", log);
            request.setAttribute("logAdmin", logAdmin);
            
            request.getRequestDispatcher("/admin/audit/log-view.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            request.setAttribute("error", "无效的日志ID");
            handleList(request, response, admin);
        }
    }

    /**
     * 处理日志导出
     */
    private void handleExport(HttpServletRequest request, HttpServletResponse response, Administrator admin)
            throws ServletException, IOException {
        
        // 获取导出参数
        String format = request.getParameter("format");
        if (format == null || format.trim().isEmpty()) {
            format = "csv";
        }
        
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        try {
            if (startDateStr != null && !startDateStr.trim().isEmpty()) {
                startDate = LocalDate.parse(startDateStr, formatter);
            }
            if (endDateStr != null && !endDateStr.trim().isEmpty()) {
                endDate = LocalDate.parse(endDateStr, formatter);
            }
        } catch (DateTimeParseException e) {
            request.setAttribute("error", "日期格式错误");
            handleList(request, response, admin);
            return;
        }
        
        List<AuditLog> logs = auditLogService.getAuditLogsByDateRange(startDate, endDate);
        
        if ("csv".equalsIgnoreCase(format)) {
            exportToCsv(response, logs, startDate, endDate);
        } else {
            request.setAttribute("error", "不支持的导出格式");
            handleList(request, response, admin);
        }
    }

    /**
     * 导出为CSV格式
     */
    private void exportToCsv(HttpServletResponse response, List<AuditLog> logs, 
                            LocalDate startDate, LocalDate endDate) throws IOException {
        
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String filename = String.format("audit_logs_%s_to_%s.csv", startDate, endDate);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        
        StringBuilder csv = new StringBuilder();
        
        // CSV头部
        csv.append("\uFEFF"); // BOM for UTF-8
        csv.append("ID,管理员ID,操作,资源类型,资源ID,详细信息,旧值,新值,IP地址,用户代理,创建时间\n");
        
        // CSV数据
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (AuditLog log : logs) {
            csv.append(log.getId()).append(",");
            csv.append(log.getAdminId() != null ? log.getAdminId() : "").append(",");
            csv.append(escapeCSV(log.getAction())).append(",");
            csv.append(escapeCSV(log.getResourceType())).append(",");
            csv.append(log.getResourceId() != null ? log.getResourceId() : "").append(",");
            csv.append(escapeCSV(log.getDetails())).append(",");
            csv.append(escapeCSV(log.getOldValue())).append(",");
            csv.append(escapeCSV(log.getNewValue())).append(",");
            csv.append(escapeCSV(log.getIpAddress())).append(",");
            csv.append(escapeCSV(log.getUserAgent())).append(",");
            csv.append(log.getCreatedAt() != null ? log.getCreatedAt().format(formatter) : "");
            csv.append("\n");
        }
        
        response.getWriter().write(csv.toString());
        response.getWriter().flush();
    }

    /**
     * CSV字段转义
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 检查权限
        if (!checkAuditPermission(request, response)) {
            return;
        }

        String action = request.getParameter("action");
        Administrator admin = (Administrator) request.getSession().getAttribute("admin");
        
        try {
            switch (action != null ? action : "") {
                case "search":
                    handleSearch(request, response, admin);
                    break;
                case "view":
                    handleView(request, response, admin);
                    break;
                case "export":
                    handleExport(request, response, admin);
                    break;
                default:
                    handleList(request, response, admin);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "操作失败: " + e.getMessage());
            request.getRequestDispatcher("/admin/audit/logs.jsp").forward(request, response);
        }
    }
}
