package org.example.scms.servlet;

import java.io.IOException;
import java.util.List;

import org.example.scms.dao.UserDAO;
import org.example.scms.model.Administrator;
import org.example.scms.model.User;
import org.example.scms.util.AuditLogUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 用户管理Servlet
 */
@WebServlet("/admin/users")
public class UserManagementServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // 检查管理员权限
        HttpSession session = request.getSession(false);
        Administrator admin = null;
        if (session != null) {
            admin = (Administrator) session.getAttribute("admin");
        }

        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }

        // 获取用户列表
        List<User> users = userDAO.findAllUsers();

        // 设置属性
        request.setAttribute("users", users);

        // 转发到用户管理页面
        request.getRequestDispatcher("/admin/users.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // 检查管理员权限
        HttpSession session = request.getSession(false);
        Administrator admin = null;
        if (session != null) {
            admin = (Administrator) session.getAttribute("admin");
        }

        if (admin == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "未授权访问");
            return;
        }
        String action = request.getParameter("action");

        switch (action) {
            case "add":
                addUser(request, response);
                break;
            case "edit":
                editUser(request, response);
                break;
            case "delete":
                deleteUser(request, response);
                break;
            case "toggleStatus":
                toggleUserStatus(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的操作");
                break;
        }
    }

    /**
     * 添加用户
     */
    private void addUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String realName = request.getParameter("realName");
            String phone = request.getParameter("phone");
            String studentId = request.getParameter("studentId");
            String userType = request.getParameter("userType");

            // 验证参数
            if (username == null || username.trim().isEmpty() ||
                    password == null || password.trim().isEmpty() ||
                    realName == null || realName.trim().isEmpty()) {

                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "必填字段不能为空");
                return;
            }

            // 检查用户名是否已存在
            if (userDAO.findByUsername(username) != null) {
                response.sendError(HttpServletResponse.SC_CONFLICT, "用户名已存在");
                return;
            } // 创建用户对象
            User user = new User(username, password, "", realName, phone, studentId, "", userType);
            user.setStatus("active"); // 默认启用

            // 保存用户
            boolean success = userDAO.addUser(user);
            if (success) {
                // 记录审计日志
                AuditLogUtil.logCreate(request, "users", user.getId(), user);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"success\": true, \"message\": \"用户添加成功\"}");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "用户添加失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "系统错误");
        }
    }

    /**
     * 编辑用户
     */
    private void editUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            String userIdStr = request.getParameter("id");
            if (userIdStr == null || userIdStr.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "用户ID不能为空");
                return;
            }

            Long userId = Long.valueOf(userIdStr);
            User oldUser = userDAO.findById(userId);

            if (oldUser == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "用户不存在");
                return;
            }

            // 获取更新参数
            String realName = request.getParameter("realName");
            String studentId = request.getParameter("studentId");
            String phone = request.getParameter("phone");
            String userType = request.getParameter("userType");
            String password = request.getParameter("password");

            // 更新用户信息
            if (realName != null && !realName.trim().isEmpty()) {
                oldUser.setFullName(realName);
            }
            if (studentId != null && !studentId.trim().isEmpty()) {
                oldUser.setStudentId(studentId);
            }
            if (phone != null && !phone.trim().isEmpty()) {
                oldUser.setPhone(phone);
            }
            if (userType != null && !userType.trim().isEmpty()) {
                oldUser.setRole(userType);
            }

            // 如果提供了新密码，则更新密码
            if (password != null && !password.trim().isEmpty()) {
                // TODO: 使用PasswordUtil重新加密密码
                oldUser.setPassword(password);
            }

            // 保存更新
            boolean success = userDAO.updateUser(oldUser);

            if (success) {
                // 记录审计日志
                AuditLogUtil.logUpdate(request, "users", userId, null, oldUser);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"success\": true, \"message\": \"用户更新成功\"}");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "用户更新失败");
            }

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的用户ID");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "系统错误");
        }
    }

    /**
     * 删除用户
     */
    private void deleteUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            String userIdStr = request.getParameter("id");
            if (userIdStr == null || userIdStr.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "用户ID不能为空");
                return;
            }

            Long userId = Long.valueOf(userIdStr);
            User user = userDAO.findById(userId);

            if (user == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "用户不存在");
                return;
            }

            // 删除用户
            boolean success = userDAO.deleteUser(userId);

            if (success) {
                // 记录审计日志
                AuditLogUtil.logDelete(request, "users", userId, user);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"success\": true, \"message\": \"用户删除成功\"}");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "用户删除失败");
            }

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的用户ID");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "系统错误");
        }
    }

    /**
     * 切换用户状态（启用/禁用）
     */
    private void toggleUserStatus(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            String userIdStr = request.getParameter("id");
            if (userIdStr == null || userIdStr.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "用户ID不能为空");
                return;
            }

            Long userId = Long.valueOf(userIdStr);
            User user = userDAO.findById(userId);

            if (user == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "用户不存在");
                return;
            }

            // 切换状态
            boolean success = userDAO.toggleUserStatus(userId);

            if (success) {
                // 记录审计日志
                String action = "TOGGLE_STATUS";
                AuditLogUtil.logOperation(request, action, "users", userId, null, user);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"success\": true, \"message\": \"用户状态已更新\"}");
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "状态更新失败");
            }

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的用户ID");
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "系统错误");
        }
    }
}
