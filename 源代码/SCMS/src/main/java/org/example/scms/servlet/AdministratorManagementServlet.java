package org.example.scms.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.example.scms.model.Administrator;
import org.example.scms.model.Department;
import org.example.scms.service.AdministratorService;
import org.example.scms.service.DepartmentService;
import org.example.scms.util.SM3HashUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 管理员管理Servlet
 * 处理管理员的增删改查操作
 */
@WebServlet("/admin/administrator")
public class AdministratorManagementServlet extends HttpServlet {
    private final AdministratorService administratorService = new AdministratorService();
    private final DepartmentService departmentService = new DepartmentService();

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
                default:
                    handleList(request, response, admin);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "操作失败: " + e.getMessage());
            request.getRequestDispatcher("/admin/administrator-list.jsp").forward(request, response);
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
                case "resetPassword":
                    handleResetPassword(request, response, admin, ipAddress, userAgent);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/admin/administrator");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/administrator?error=" + e.getMessage());
        }
    }    /**
     * 处理管理员列表页面 - 根据当前管理员类型控制权限
     */
    private void handleList(HttpServletRequest request, HttpServletResponse response, Administrator admin)
            throws ServletException, IOException {
        
        String currentAdminType = admin.getAdminType();
        List<Administrator> administrators = new ArrayList<>();
        String pageTitle = "管理员管理";
        
        // 根据当前管理员的admin_type字段控制显示内容
        switch (currentAdminType) {
            case "department_admin":
                // 部门管理员：没有权限访问管理员列表
                request.setAttribute("error", "抱歉，您没有权限访问管理员列表");
                request.setAttribute("admin", admin);
                request.getRequestDispatcher("/admin/administrator-list.jsp").forward(request, response);
                return;
                
            case "school_admin":
                // 学校管理员：只能看到部门管理员列表
                administrators = administratorService.getAdministratorsByType("department_admin");
                pageTitle = "部门管理员管理";
                break;
                
            case "audit_admin":
                // 审计管理员：可以看到audit_admin和school_admin
                List<Administrator> auditAdmins = administratorService.getAdministratorsByType("audit_admin");
                List<Administrator> schoolAdmins = administratorService.getAdministratorsByType("school_admin");
                administrators.addAll(auditAdmins);
                administrators.addAll(schoolAdmins);
                pageTitle = "高级管理员管理";
                break;
                
            case "system_admin":
                // 系统管理员：可以看到所有类型的管理员
                administrators = administratorService.getAllAdministrators();
                pageTitle = "系统管理员管理";
                break;
                
            default:
                // 其他类型：没有权限访问
                request.setAttribute("error", "抱歉，您没有权限访问管理员列表");
                request.setAttribute("admin", admin);
                request.getRequestDispatcher("/admin/administrator-list.jsp").forward(request, response);
                return;
        }
        
        List<Department> departments = departmentService.getAllDepartments();
        // 创建部门ID到部门对象的映射，供JSP使用
        java.util.Map<Long, Department> departmentMap = new java.util.HashMap<>();
        if (departments != null) {
            for (Department dept : departments) {
                departmentMap.put(dept.getId(), dept);
            }
        }
        
        request.setAttribute("administrators", administrators);
        request.setAttribute("departments", departments);
        request.setAttribute("departmentMap", departmentMap);
        request.setAttribute("pageTitle", pageTitle);
        request.setAttribute("admin", admin);
        request.getRequestDispatcher("/admin/administrator-list.jsp").forward(request, response);
    }    /**
     * 处理添加管理员表单页面
     */
    private void handleAddForm(HttpServletRequest request, HttpServletResponse response, Administrator admin)
            throws ServletException, IOException {
        List<Department> departments = departmentService.getActiveDepartments();
        
        // 传递source参数以便表单知道来源页面
        String source = request.getParameter("source");
        request.setAttribute("source", source);
        
        request.setAttribute("departments", departments);
        request.setAttribute("admin", admin);
        request.getRequestDispatcher("/admin/administrator-add.jsp").forward(request, response);
    }    /**
     * 处理编辑管理员表单页面
     */    private void handleEditForm(HttpServletRequest request, HttpServletResponse response, Administrator admin)
            throws ServletException, IOException {
        long administratorId = Long.parseLong(request.getParameter("id"));
        Administrator targetAdmin = administratorService.getAdministratorById(administratorId);
        
        if (targetAdmin == null) {
            request.setAttribute("error", "管理员不存在");
            response.sendRedirect(request.getContextPath() + "/admin/administrator");
            return;
        }

        List<Department> departments = departmentService.getActiveDepartments();
        
        // 传递source参数以便表单知道来源页面
        String source = request.getParameter("source");
        request.setAttribute("source", source);
          request.setAttribute("editAdmin", targetAdmin);
        request.setAttribute("departments", departments);
        request.setAttribute("admin", admin);
        request.getRequestDispatcher("/admin/administrator-edit.jsp").forward(request, response);
    }

    /**
     * 处理查看管理员详情页面
     */    private void handleView(HttpServletRequest request, HttpServletResponse response, Administrator admin)
            throws ServletException, IOException {
        long administratorId = Long.parseLong(request.getParameter("id"));
        Administrator targetAdmin = administratorService.getAdministratorById(administratorId);
        
        if (targetAdmin == null) {
            request.setAttribute("error", "管理员不存在");
            response.sendRedirect(request.getContextPath() + "/admin/administrator");
            return;
        }

        // 获取管理员所属部门信息
        Department department = null;
        if (targetAdmin.getDepartmentId() != null) {
            department = departmentService.getDepartmentById(targetAdmin.getDepartmentId());
        }

        request.setAttribute("targetAdmin", targetAdmin);
        request.setAttribute("department", department);
        request.setAttribute("admin", admin);
        request.getRequestDispatcher("/admin/administrator-view.jsp").forward(request, response);
    }

    /**
     * 处理创建管理员
     */
    private void handleCreate(HttpServletRequest request, HttpServletResponse response, Administrator admin,
            String ipAddress, String userAgent) throws ServletException, IOException {
        String username = request.getParameter("username");
        String fullName = request.getParameter("fullName");
        String phoneEncrypted = request.getParameter("phoneEncrypted");
        String departmentIdStr = request.getParameter("departmentId");
        String adminType = request.getParameter("adminType");
        String password = request.getParameter("password");        // 参数验证
        if (username == null || username.trim().isEmpty() ||
            fullName == null || fullName.trim().isEmpty() ||
            departmentIdStr == null || departmentIdStr.trim().isEmpty() ||
            adminType == null || adminType.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            String errorMsg = URLEncoder.encode("参数不能为空", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/administrator?action=add&error=" + errorMsg);
            return;
        }// 检查用户名是否已存在
        Administrator existingAdmin = administratorService.getAdministratorByUsername(username.trim());
        if (existingAdmin != null) {
            String errorMsg = URLEncoder.encode("用户名已存在", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/administrator?action=add&error=" + errorMsg);
            return;
        }try {
            long departmentId = Long.parseLong(departmentIdStr);
            
            // 处理加密电话号码
            String phoneHash = null;
            if (phoneEncrypted != null && !phoneEncrypted.trim().isEmpty()) {
                phoneHash = SM3HashUtil.hash(phoneEncrypted.trim());
            }

            Administrator newAdmin = new Administrator();
            newAdmin.setUsername(username.trim());
            newAdmin.setPassword(password.trim()); // 传递原始密码，让Service层处理加密
            newAdmin.setFullName(fullName.trim());
            newAdmin.setPhoneEncrypted(phoneEncrypted != null ? phoneEncrypted.trim() : "");
            newAdmin.setPhoneHash(phoneHash);
            newAdmin.setDepartmentId(departmentId);
            newAdmin.setAdminType(adminType.trim());
            newAdmin.setStatus("active");
            newAdmin.setLoginFailCount(0);
            newAdmin.setLastPasswordChangeTime(LocalDateTime.now());            Administrator createdAdmin = administratorService.createAdministrator(newAdmin, admin.getId(), ipAddress, userAgent);
            
            // 获取返回URL参数，用于决定重定向到哪个页面
            String returnUrl = request.getParameter("returnUrl");
            String source = request.getParameter("source");
              if (createdAdmin != null) {
                // 根据来源决定重定向位置
                if ("department".equals(source) || (returnUrl != null && returnUrl.contains("department"))) {
                    String successMsg = URLEncoder.encode("部门管理员创建成功", StandardCharsets.UTF_8);
                    response.sendRedirect(request.getContextPath() + "/admin/department?success=" + successMsg);
                } else {
                    String successMsg = URLEncoder.encode("管理员创建成功", StandardCharsets.UTF_8);
                    response.sendRedirect(request.getContextPath() + "/admin/administrator?success=" + successMsg);
                }
            } else {
                // 错误时也要考虑返回正确的页面
                if ("department".equals(source) || (returnUrl != null && returnUrl.contains("department"))) {
                    String errorMsg = URLEncoder.encode("管理员创建失败", StandardCharsets.UTF_8);
                    response.sendRedirect(request.getContextPath() + "/admin/administrator?action=add&source=department&error=" + errorMsg);
                } else {
                    String errorMsg = URLEncoder.encode("管理员创建失败", StandardCharsets.UTF_8);
                    response.sendRedirect(request.getContextPath() + "/admin/administrator?action=add&error=" + errorMsg);
                }
            }        } catch (NumberFormatException e) {
            String errorMsg = URLEncoder.encode("部门ID格式错误", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/administrator?action=add&error=" + errorMsg);
        }
    }    /**
     * 处理更新管理员
     */    private void handleUpdate(HttpServletRequest request, HttpServletResponse response, Administrator admin,
            String ipAddress, String userAgent) throws ServletException, IOException {
        long administratorId = Long.parseLong(request.getParameter("id"));
        String username = request.getParameter("username");
        String fullName = request.getParameter("fullName");
        String phoneEncrypted = request.getParameter("phoneEncrypted");
        String departmentIdStr = request.getParameter("departmentId");
        String status = request.getParameter("status");        // 参数验证 - 移除departmentId的必填验证，因为页面中部门不是必填项
        if (username == null || username.trim().isEmpty() ||
            fullName == null || fullName.trim().isEmpty()) {
            String errorMsg = URLEncoder.encode("参数不能为空", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/administrator?action=edit&id=" + administratorId + "&error=" + errorMsg);
            return;
        }Administrator targetAdmin = administratorService.getAdministratorById(administratorId);
        if (targetAdmin == null) {
            String errorMsg = URLEncoder.encode("管理员不存在", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/administrator?error=" + errorMsg);
            return;
        }

        // 检查用户名是否被其他管理员占用
        Administrator existingAdmin = administratorService.getAdministratorByUsername(username.trim());
        if (existingAdmin != null && !existingAdmin.getId().equals(administratorId)) {
            String errorMsg = URLEncoder.encode("用户名已被占用", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/administrator?action=edit&id=" + administratorId + "&error=" + errorMsg);
            return;
        }        try {
            // 处理部门ID - 允许为空
            Long departmentId = null;
            if (departmentIdStr != null && !departmentIdStr.trim().isEmpty()) {
                departmentId = Long.parseLong(departmentIdStr);
            }
            
            // 处理加密电话号码
            String phoneHash = null;
            if (phoneEncrypted != null && !phoneEncrypted.trim().isEmpty()) {
                phoneHash = SM3HashUtil.hash(phoneEncrypted.trim());
            }

            targetAdmin.setUsername(username.trim());
            targetAdmin.setFullName(fullName.trim());
            targetAdmin.setPhoneEncrypted(phoneEncrypted != null ? phoneEncrypted.trim() : "");
            targetAdmin.setPhoneHash(phoneHash);
            targetAdmin.setDepartmentId(departmentId);
            // 不再更新adminType，保持现有值不变
            if (status != null && !status.trim().isEmpty()) {
                targetAdmin.setStatus(status.trim());
            }
            targetAdmin.setUpdatedAt(LocalDateTime.now());boolean success = administratorService.updateAdministrator(targetAdmin, admin.getId(), ipAddress, userAgent);
            
            // 获取返回URL参数，用于决定重定向到哪个页面
            String returnUrl = request.getParameter("returnUrl");
            String source = request.getParameter("source");
              if (success) {
                // 根据来源决定重定向位置
                if ("department".equals(source) || (returnUrl != null && returnUrl.contains("department"))) {
                    String successMsg = URLEncoder.encode("部门管理员更新成功", StandardCharsets.UTF_8);
                    response.sendRedirect(request.getContextPath() + "/admin/department?success=" + successMsg);
                } else {
                    String successMsg = URLEncoder.encode("管理员更新成功", StandardCharsets.UTF_8);
                    response.sendRedirect(request.getContextPath() + "/admin/administrator?success=" + successMsg);
                }
            } else {
                // 错误时也要考虑返回正确的页面
                if ("department".equals(source) || (returnUrl != null && returnUrl.contains("department"))) {
                    String errorMsg = URLEncoder.encode("管理员更新失败", StandardCharsets.UTF_8);
                    response.sendRedirect(request.getContextPath() + "/admin/administrator?action=edit&id=" + administratorId + "&source=department&error=" + errorMsg);
                } else {
                    String errorMsg = URLEncoder.encode("管理员更新失败", StandardCharsets.UTF_8);
                    response.sendRedirect(request.getContextPath() + "/admin/administrator?action=edit&id=" + administratorId + "&error=" + errorMsg);
                }
            }        } catch (NumberFormatException e) {
            String errorMsg = URLEncoder.encode("部门ID格式错误", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/administrator?action=edit&id=" + administratorId + "&error=" + errorMsg);
        }
    }    /**
     * 处理删除管理员
     */    private void handleDelete(HttpServletRequest request, HttpServletResponse response, Administrator admin,
            String ipAddress, String userAgent) throws ServletException, IOException {
        long administratorId = Long.parseLong(request.getParameter("id"));

        // 防止删除自己
        if (administratorId == admin.getId()) {
            String errorMsg = URLEncoder.encode("不能删除自己的账户", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/administrator?error=" + errorMsg);
            return;
        }

        boolean success = administratorService.deleteAdministrator(administratorId, admin.getId(), ipAddress, userAgent);
        
        if (success) {
            String successMsg = URLEncoder.encode("管理员删除成功", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/administrator?success=" + successMsg);
        } else {
            String errorMsg = URLEncoder.encode("管理员删除失败", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/administrator?error=" + errorMsg);
        }
    }    /**
     * 处理启用管理员
     */    private void handleActivate(HttpServletRequest request, HttpServletResponse response, Administrator admin,
            String ipAddress, String userAgent) throws ServletException, IOException {
        long administratorId = Long.parseLong(request.getParameter("id"));

        boolean success = administratorService.activateAdministrator(administratorId, admin.getId(), ipAddress, userAgent);
        
        if (success) {
            String successMsg = URLEncoder.encode("管理员启用成功", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/administrator?success=" + successMsg);
        } else {
            String errorMsg = URLEncoder.encode("管理员启用失败", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/administrator?error=" + errorMsg);
        }
    }    /**
     * 处理禁用管理员
     */    private void handleDeactivate(HttpServletRequest request, HttpServletResponse response, Administrator admin,
            String ipAddress, String userAgent) throws ServletException, IOException {
        long administratorId = Long.parseLong(request.getParameter("id"));

        // 防止禁用自己
        if (administratorId == admin.getId()) {
            String errorMsg = URLEncoder.encode("不能禁用自己的账户", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/administrator?error=" + errorMsg);
            return;
        }

        boolean success = administratorService.deactivateAdministrator(administratorId, admin.getId(), ipAddress, userAgent);
        
        if (success) {
            String successMsg = URLEncoder.encode("管理员禁用成功", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/administrator?success=" + successMsg);
        } else {
            String errorMsg = URLEncoder.encode("管理员禁用失败", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/administrator?error=" + errorMsg);
        }
    }    /**
     * 处理重置密码
     */    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response, Administrator admin,
            String ipAddress, String userAgent) throws ServletException, IOException {
        long administratorId = Long.parseLong(request.getParameter("id"));

        String newPassword = administratorService.resetPassword(administratorId, admin.getId(), ipAddress, userAgent);
        
        if (newPassword != null) {
            String successMsg = URLEncoder.encode("密码重置成功，新密码为：" + newPassword, StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/administrator?success=" + successMsg);
        } else {
            String errorMsg = URLEncoder.encode("密码重置失败", StandardCharsets.UTF_8);
            response.sendRedirect(request.getContextPath() + "/admin/administrator?error=" + errorMsg);
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
