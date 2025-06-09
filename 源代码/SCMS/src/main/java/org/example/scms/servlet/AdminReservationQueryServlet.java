package org.example.scms.servlet;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.example.scms.model.Administrator;
import org.example.scms.model.Campus;
import org.example.scms.model.Department;
import org.example.scms.service.AdminQueryParams;
import org.example.scms.service.AdminQueryResult;
import org.example.scms.service.AdminReservationQueryService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 管理员预约查询Servlet
 * 提供统一的预约记录查询和管理功能
 */
@WebServlet("/admin/reservation-query")
public class AdminReservationQueryServlet extends HttpServlet {
    
    private AdminReservationQueryService queryService;
    
    @Override
    public void init() throws ServletException {
        this.queryService = new AdminReservationQueryService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 验证管理员登录
        HttpSession session = request.getSession();
        Administrator admin = (Administrator) session.getAttribute("admin");
        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }
          try {
            // 解析查询参数
            AdminQueryParams params = parseQueryParams(request, admin);
            
            // 执行查询
            AdminQueryResult result = queryService.queryReservations(params, admin);
            
            // 获取基础数据（根据管理员权限过滤）
            List<Campus> campuses = queryService.getAllCampuses();
            List<Department> departments = getDepartmentsByAdminPermission(admin);            // 获取统计信息（根据管理员权限）
            Map<String, Object> statistics = null;
            if (params.getStartDate() != null && params.getEndDate() != null) {
                statistics = queryService.getStatistics(params.getStartDate(), params.getEndDate(), admin);
            } else {
                // 默认统计最近30天
                LocalDate endDate = LocalDate.now();
                LocalDate startDate = endDate.minusDays(30);
                statistics = queryService.getStatistics(startDate, endDate, admin);
            }
            
            // 设置请求属性
            request.setAttribute("queryResult", result);
            request.setAttribute("queryParams", params);
            request.setAttribute("campuses", campuses);
            request.setAttribute("departments", departments);
            request.setAttribute("statistics", statistics);
            request.setAttribute("admin", admin);  // 添加管理员信息供JSP使用
            
            // 转发到JSP页面
            request.getRequestDispatcher("/admin/reservation-query.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "查询失败: " + e.getMessage());
            request.getRequestDispatcher("/admin/reservation-query.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // POST请求重定向到GET请求，避免重复提交
        doGet(request, response);
    }
      /**
     * 解析查询参数
     */    private AdminQueryParams parseQueryParams(HttpServletRequest request, Administrator admin) {
        AdminQueryParams params = new AdminQueryParams();
        
        String adminType = admin.getAdminType();
        Long adminDepartmentId = admin.getDepartmentId();
        
        // 预约类型（根据管理员权限限制）
        String reservationType = request.getParameter("reservationType");
        if ("department_admin".equals(adminType)) {
            // 部门管理员只能查询公务预约
            params.setReservationType("official");
        } else if (reservationType != null && !reservationType.trim().isEmpty()) {
            params.setReservationType(reservationType.trim());
        } else {
            params.setReservationType("all");
        }
        
        // 状态筛选
        String status = request.getParameter("status");
        if (status != null && !status.trim().isEmpty()) {
            params.setStatus(status.trim());
        }
        
        // 校区筛选
        String campusIdStr = request.getParameter("campusId");
        if (campusIdStr != null && !campusIdStr.trim().isEmpty()) {
            try {
                params.setCampusId(Long.parseLong(campusIdStr.trim()));
            } catch (NumberFormatException e) {
                // 忽略无效的校区ID
            }
        }
          // 部门筛选（根据管理员权限限制）
        String departmentIdStr = request.getParameter("departmentId");
        if ("department_admin".equals(adminType) && adminDepartmentId != null) {
            // 部门管理员只能查询自己部门的数据
            params.setDepartmentId(adminDepartmentId);
        } else if (departmentIdStr != null && !departmentIdStr.trim().isEmpty()) {
            try {
                params.setDepartmentId(Long.parseLong(departmentIdStr.trim()));
            } catch (NumberFormatException e) {
                // 忽略无效的部门ID
            }
        }
        
        // 日期范围
        String startDateStr = request.getParameter("startDate");
        String endDateStr = request.getParameter("endDate");
        
        if (startDateStr != null && !startDateStr.trim().isEmpty()) {
            try {
                params.setStartDate(LocalDate.parse(startDateStr.trim()));
            } catch (DateTimeParseException e) {
                // 忽略无效的开始日期
            }
        }
        
        if (endDateStr != null && !endDateStr.trim().isEmpty()) {
            try {
                params.setEndDate(LocalDate.parse(endDateStr.trim()));
            } catch (DateTimeParseException e) {
                // 忽略无效的结束日期
            }
        }
        
        // 如果没有设置日期范围，默认查询最近30天
        if (params.getStartDate() == null || params.getEndDate() == null) {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30);
            params.setStartDate(startDate);
            params.setEndDate(endDate);
        }
        
        // 访客姓名
        String visitorName = request.getParameter("visitorName");
        if (visitorName != null && !visitorName.trim().isEmpty()) {
            params.setVisitorName(visitorName.trim());
        }
        
        // 访客身份证号
        String visitorIdCard = request.getParameter("visitorIdCard");
        if (visitorIdCard != null && !visitorIdCard.trim().isEmpty()) {
            params.setVisitorIdCard(visitorIdCard.trim());
        }
        
        // 分页参数
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.trim().isEmpty()) {
            try {
                params.setPage(Integer.parseInt(pageStr.trim()));
            } catch (NumberFormatException e) {
                params.setPage(1);
            }
        }
        
        String pageSizeStr = request.getParameter("pageSize");
        if (pageSizeStr != null && !pageSizeStr.trim().isEmpty()) {
            try {
                params.setPageSize(Integer.parseInt(pageSizeStr.trim()));
            } catch (NumberFormatException e) {
                params.setPageSize(20);
            }
        }        
        return params;
    }
    
    /**
     * 根据管理员权限获取部门列表
     */
    private List<Department> getDepartmentsByAdminPermission(Administrator admin) {
        String adminType = admin.getAdminType();
        
        if ("system_admin".equals(adminType) || "school_admin".equals(adminType)) {
            // 系统管理员和学校管理员可以看到所有部门
            return queryService.getAllDepartments();
        } else if ("department_admin".equals(adminType) && admin.getDepartmentId() != null) {
            // 部门管理员只能看到自己的部门
            List<Department> departments = new ArrayList<>();
            Department dept = queryService.getDepartmentById(admin.getDepartmentId());
            if (dept != null) {
                departments.add(dept);
            }
            return departments;
        }
        
        return new ArrayList<>();
    }
}
