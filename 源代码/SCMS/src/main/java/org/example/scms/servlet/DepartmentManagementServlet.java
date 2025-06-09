package org.example.scms.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.example.scms.model.Administrator;
import org.example.scms.model.Department;
import org.example.scms.model.ReservationAuthorization;
import org.example.scms.service.AdministratorService;
import org.example.scms.service.AuthorizationService;
import org.example.scms.service.DepartmentService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 部门管理Servlet
 * 处理部门的增删改查操作
 */
@WebServlet("/admin/department")
public class DepartmentManagementServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(DepartmentManagementServlet.class.getName());
    private final DepartmentService departmentService = new DepartmentService();
    private final AuthorizationService authorizationService = new AuthorizationService();
    private final AdministratorService administratorService = new AdministratorService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Administrator admin = (Administrator) session.getAttribute("admin");

        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        try {
            switch (action) {
                case "list":
                    handleList(request, response, admin);
                    break;
                case "add":
                    handleAddForm(request, response, admin);
                    break;
                case "edit":
                    handleEditForm(request, response, admin);
                    break;
                case "view":
                    handleView(request, response, admin);
                    break;
                case "permissions":
                    handlePermissions(request, response, admin);
                    break;
                default:
                    handleList(request, response, admin);                    break;
            }
        } catch (ServletException | IOException e) {
            logger.log(Level.SEVERE, "处理GET请求失败", e);
            request.setAttribute("error", "操作失败: " + e.getMessage());
            request.getRequestDispatcher("/admin/department-list.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Administrator admin = (Administrator) session.getAttribute("admin");

        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }

        String action = request.getParameter("action");
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        try {
            switch (action) {
                case "create":
                    handleCreate(request, response, admin, ipAddress, userAgent);
                    break;
                case "update":
                    handleUpdate(request, response, admin, ipAddress, userAgent);
                    break;
                case "delete":
                    handleDelete(request, response, admin, ipAddress, userAgent);
                    break;
                case "activate":
                    handleActivate(request, response, admin, ipAddress, userAgent);
                    break;
                case "deactivate":
                    handleDeactivate(request, response, admin, ipAddress, userAgent);
                    break;
                case "grant-permission":
                    handleGrantPermission(request, response, admin, ipAddress, userAgent);
                    break;
                case "revoke-permission":
                    handleRevokePermission(request, response, admin, ipAddress, userAgent);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/admin/department");                    break;
            }        } catch (ServletException | IOException e) {
            logger.log(Level.SEVERE, "处理POST请求失败", e);
            String errorMsg = URLEncoder.encode("操作失败: " + e.getMessage(), StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/department?error=" + errorMsg);
        }
    }    /**
     * 处理部门列表页面
     */
    private void handleList(HttpServletRequest request, HttpServletResponse response, Administrator admin)
            throws ServletException, IOException {
        
        // 权限检查：只有school_admin和system_admin可以查看部门列表
        String adminType = admin.getAdminType();
        if (!"school_admin".equals(adminType) && !"system_admin".equals(adminType)) {
            // 类似管理员管理的权限控制，设置错误信息并转发到页面
            request.setAttribute("error", "抱歉，您没有权限访问部门列表");
            request.setAttribute("admin", admin);
            request.getRequestDispatcher("/admin/department-list.jsp").forward(request, response);
            return;
        }
        
        List<Department> departments = departmentService.getAllDepartments();
        
        // 创建部门ID到部门管理员的映射
        java.util.Map<Long, Administrator> departmentAdminMap = new java.util.HashMap<>();
        if (departments != null) {
            for (Department dept : departments) {
                // 获取该部门的管理员列表
                List<Administrator> deptAdmins = administratorService.getAdministratorsByDepartment(dept.getId());
                // 通常每个部门只有一个管理员，取第一个
                if (deptAdmins != null && !deptAdmins.isEmpty()) {
                    departmentAdminMap.put(dept.getId(), deptAdmins.get(0));
                }
            }
        }
        
        request.setAttribute("departments", departments);
        request.setAttribute("departmentAdminMap", departmentAdminMap);
        request.setAttribute("admin", admin);
        request.getRequestDispatcher("/admin/department-list.jsp").forward(request, response);
    }    /**
     * 检查管理员是否有部门管理权限
     */
    private boolean hasPermission(Administrator admin) {
        String adminType = admin.getAdminType();
        return "school_admin".equals(adminType) || "system_admin".equals(adminType);
    }

    /**
     * 处理添加部门表单页面
     */
    private void handleAddForm(HttpServletRequest request, HttpServletResponse response, Administrator admin)
            throws ServletException, IOException {
        
        // 权限检查
        if (!hasPermission(admin)) {
            String errorMsg = URLEncoder.encode("您没有权限执行此操作", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=" + errorMsg);
            return;
        }
        
        request.setAttribute("admin", admin);
        request.getRequestDispatcher("/admin/department-add.jsp").forward(request, response);
    }    /**
     * 处理编辑部门表单页面
     */
    private void handleEditForm(HttpServletRequest request, HttpServletResponse response, Administrator admin)
            throws ServletException, IOException {
        
        // 权限检查
        if (!hasPermission(admin)) {
            String errorMsg = URLEncoder.encode("您没有权限执行此操作", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=" + errorMsg);
            return;
        }
        
        long departmentId = Long.parseLong(request.getParameter("id"));
        Department department = departmentService.getDepartmentById(departmentId);
        
        if (department == null) {
            String errorMsg = URLEncoder.encode("部门不存在", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/department?error=" + errorMsg);
            return;
        }

        request.setAttribute("department", department);
        request.setAttribute("admin", admin);
        request.getRequestDispatcher("/admin/department-edit.jsp").forward(request, response);
    }    /**
     * 处理查看部门详情页面
     */
    private void handleView(HttpServletRequest request, HttpServletResponse response, Administrator admin)
            throws ServletException, IOException {
        
        // 权限检查
        if (!hasPermission(admin)) {
            String errorMsg = URLEncoder.encode("您没有权限执行此操作", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=" + errorMsg);
            return;
        }
        
        long departmentId = Long.parseLong(request.getParameter("id"));
        Department department = departmentService.getDepartmentById(departmentId);
        
        if (department == null) {
            String errorMsg = URLEncoder.encode("部门不存在", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/department?error=" + errorMsg);
            return;
        }

        // 获取部门授权信息
        List<ReservationAuthorization> authorizations = authorizationService.getDepartmentAuthorizations(departmentId);

        request.setAttribute("department", department);
        request.setAttribute("authorizations", authorizations);
        request.setAttribute("admin", admin);
        request.getRequestDispatcher("/admin/department-view.jsp").forward(request, response);
    }    /**
     * 处理部门权限管理页面
     */
    private void handlePermissions(HttpServletRequest request, HttpServletResponse response, Administrator admin)
            throws ServletException, IOException {
        
        // 权限检查
        if (!hasPermission(admin)) {
            String errorMsg = URLEncoder.encode("您没有权限执行此操作", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=" + errorMsg);
            return;
        }
        
        long departmentId = Long.parseLong(request.getParameter("id"));
        Department department = departmentService.getDepartmentById(departmentId);
        
        if (department == null) {
            String errorMsg = URLEncoder.encode("部门不存在", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/department?error=" + errorMsg);
            return;
        }

        // 获取部门授权信息
        List<ReservationAuthorization> authorizations = authorizationService.getDepartmentAuthorizations(departmentId);

        request.setAttribute("department", department);
        request.setAttribute("authorizations", authorizations);
        request.setAttribute("admin", admin);
        request.getRequestDispatcher("/admin/department-permissions.jsp").forward(request, response);
    }    /**
     * 处理创建部门
     */
    private void handleCreate(HttpServletRequest request, HttpServletResponse response, Administrator admin,
            String ipAddress, String userAgent) throws ServletException, IOException {
        
        // 权限检查
        if (!hasPermission(admin)) {
            String errorMsg = URLEncoder.encode("您没有权限执行此操作", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=" + errorMsg);
            return;
        }
        
        String departmentName = request.getParameter("departmentName");
        String departmentCode = request.getParameter("departmentCode");
        String departmentType = request.getParameter("departmentType");
        String description = request.getParameter("description");

        // 参数验证
        if (departmentName == null || departmentName.trim().isEmpty() ||
            departmentCode == null || departmentCode.trim().isEmpty() ||
            departmentType == null || departmentType.trim().isEmpty()) {
            String errorMsg = URLEncoder.encode("参数不能为空", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/department?action=add&error=" + errorMsg);
            return;
        }

        Department department = new Department();
        department.setDepartmentName(departmentName.trim());
        department.setDepartmentCode(departmentCode.trim().toUpperCase());
        department.setDepartmentType(departmentType.trim());
        department.setDescription(description != null ? description.trim() : "");
        department.setStatus("active");

        boolean success = departmentService.addDepartment(department, admin.getId(), ipAddress, userAgent);
        
        if (success) {
            String successMsg = URLEncoder.encode("部门创建成功", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/department?success=" + successMsg);
        } else {
            String errorMsg = URLEncoder.encode("部门创建失败，请检查部门代码是否重复", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/department?action=add&error=" + errorMsg);
        }
    }    /**
     * 处理更新部门
     */    private void handleUpdate(HttpServletRequest request, HttpServletResponse response, Administrator admin,
            String ipAddress, String userAgent) throws ServletException, IOException {
        
        // 权限检查
        if (!hasPermission(admin)) {
            String errorMsg = URLEncoder.encode("您没有权限执行此操作", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=" + errorMsg);
            return;
        }
        
        long departmentId = Long.parseLong(request.getParameter("id"));
        String departmentName = request.getParameter("departmentName");
        String departmentCode = request.getParameter("departmentCode");
        String departmentType = request.getParameter("departmentType");
        String description = request.getParameter("description");

        // 参数验证
        if (departmentName == null || departmentName.trim().isEmpty() ||
            departmentCode == null || departmentCode.trim().isEmpty() ||
            departmentType == null || departmentType.trim().isEmpty()) {
            String errorMsg = URLEncoder.encode("参数不能为空", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/department?action=edit&id=" + departmentId + "&error=" + errorMsg);
            return;
        }

        Department department = departmentService.getDepartmentById(departmentId);
        if (department == null) {
            String errorMsg = URLEncoder.encode("部门不存在", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/department?error=" + errorMsg);
            return;
        }

        department.setDepartmentName(departmentName.trim());
        department.setDepartmentCode(departmentCode.trim().toUpperCase());
        department.setDepartmentType(departmentType.trim());
        department.setDescription(description != null ? description.trim() : "");
        department.setUpdatedAt(LocalDateTime.now());

        boolean success = departmentService.updateDepartment(department, admin.getId(), ipAddress, userAgent);
        
        if (success) {
            String successMsg = URLEncoder.encode("部门更新成功", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/department?success=" + successMsg);
        } else {
            String errorMsg = URLEncoder.encode("部门更新失败", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/department?action=edit&id=" + departmentId + "&error=" + errorMsg);
        }
    }    /**
     * 处理删除部门
     */    private void handleDelete(HttpServletRequest request, HttpServletResponse response, Administrator admin,
            String ipAddress, String userAgent) throws ServletException, IOException {
        
        // 权限检查
        if (!hasPermission(admin)) {
            String errorMsg = URLEncoder.encode("您没有权限执行此操作", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=" + errorMsg);
            return;
        }
        
        long departmentId = Long.parseLong(request.getParameter("id"));

        boolean success = departmentService.deleteDepartment(departmentId, admin.getId(), ipAddress, userAgent);
        
        if (success) {
            String successMsg = URLEncoder.encode("部门删除成功", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/department?success=" + successMsg);
        } else {
            String errorMsg = URLEncoder.encode("部门删除失败，可能存在关联的管理员或预约记录", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/department?error=" + errorMsg);
        }
    }    /**
     * 处理启用部门
     */    private void handleActivate(HttpServletRequest request, HttpServletResponse response, Administrator admin,
            String ipAddress, String userAgent) throws ServletException, IOException {
        
        // 权限检查
        if (!hasPermission(admin)) {
            String errorMsg = URLEncoder.encode("您没有权限执行此操作", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=" + errorMsg);
            return;
        }
        
        long departmentId = Long.parseLong(request.getParameter("id"));

        boolean success = departmentService.activateDepartment(departmentId, admin.getId(), ipAddress, userAgent);
        
        if (success) {
            String successMsg = URLEncoder.encode("部门启用成功", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/department?success=" + successMsg);
        } else {
            String errorMsg = URLEncoder.encode("部门启用失败", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/department?error=" + errorMsg);
        }
    }    /**
     * 处理禁用部门
     */
    private void handleDeactivate(HttpServletRequest request, HttpServletResponse response, Administrator admin,
            String ipAddress, String userAgent) throws ServletException, IOException {
        
        // 权限检查
        if (!hasPermission(admin)) {
            String errorMsg = URLEncoder.encode("您没有权限执行此操作", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=" + errorMsg);
            return;
        }
        
        long departmentId = Long.parseLong(request.getParameter("id"));

        boolean success = departmentService.deactivateDepartment(departmentId, admin.getId(), ipAddress, userAgent);
        
        if (success) {
            String successMsg = URLEncoder.encode("部门禁用成功", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/department?success=" + successMsg);
        } else {
            String errorMsg = URLEncoder.encode("部门禁用失败，可能存在未完成的预约", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/department?error=" + errorMsg);
        }
    }    /**
     * 处理授予权限
     */
    private void handleGrantPermission(HttpServletRequest request, HttpServletResponse response, Administrator admin,
            String ipAddress, String userAgent) throws ServletException, IOException {
        
        // 权限检查
        if (!hasPermission(admin)) {
            String errorMsg = URLEncoder.encode("您没有权限执行此操作", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=" + errorMsg);
            return;
        }
        
        long departmentId = Long.parseLong(request.getParameter("departmentId"));
        String reservationType = request.getParameter("reservationType");
        String description = request.getParameter("description");

        if (reservationType == null || reservationType.trim().isEmpty()) {
            String errorMsg = URLEncoder.encode("预约类型不能为空", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/department?action=permissions&id=" + departmentId + "&error=" + errorMsg);
            return;
        }

        boolean success = authorizationService.grantAuthorization(departmentId, reservationType.trim(), 
                admin.getId(), description != null ? description.trim() : "", ipAddress, userAgent);
        
        if (success) {
            String successMsg = URLEncoder.encode("权限授予成功", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/department?action=permissions&id=" + departmentId + "&success=" + successMsg);
        } else {
            String errorMsg = URLEncoder.encode("权限授予失败，可能已存在相同权限", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/department?action=permissions&id=" + departmentId + "&error=" + errorMsg);
        }
    }    /**
     * 处理撤销权限
     */
    private void handleRevokePermission(HttpServletRequest request, HttpServletResponse response, Administrator admin,
            String ipAddress, String userAgent) throws ServletException, IOException {
        
        // 权限检查
        if (!hasPermission(admin)) {
            String errorMsg = URLEncoder.encode("您没有权限执行此操作", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/dashboard?error=" + errorMsg);
            return;
        }
        
        long authorizationId = Long.parseLong(request.getParameter("authorizationId"));
        long departmentId = Long.parseLong(request.getParameter("departmentId"));

        boolean success = authorizationService.revokeAuthorization(authorizationId, admin.getId(), ipAddress, userAgent);
        
        if (success) {
            String successMsg = URLEncoder.encode("权限撤销成功", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/department?action=permissions&id=" + departmentId + "&success=" + successMsg);
        } else {
            String errorMsg = URLEncoder.encode("权限撤销失败", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/department?action=permissions&id=" + departmentId + "&error=" + errorMsg);
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
