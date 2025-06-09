<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.example.scms.model.Administrator" %>
<%@ page import="org.example.scms.model.Department" %>
<%@ page import="org.example.scms.util.DataEncryptionUtil" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<%
    List<Administrator> administrators = (List<Administrator>) request.getAttribute("administrators");
    Administrator admin = (Administrator) request.getAttribute("admin");
    Map<Long, Department> departmentMap = (Map<Long, Department>) request.getAttribute("departmentMap");
    String pageTitle = (String) request.getAttribute("pageTitle");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    String error = request.getParameter("error");
    String success = request.getParameter("success");
    String errorFromRequest = (String) request.getAttribute("error");
    
    // 如果没有设置pageTitle，则使用默认值
    if (pageTitle == null) {
        pageTitle = "管理员管理";
    }
%>

<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= pageTitle %> - 校园通行码预约管理系统</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f8fafc;
            color: #333;
            min-height: 100vh;
        }

        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 1rem 0;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }

        .header-content {
            max-width: 1400px;
            margin: 0 auto;
            padding: 0 2rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .header h1 {
            font-size: 1.5rem;
            font-weight: 600;
        }

        .admin-info {
            display: flex;
            align-items: center;
            gap: 1rem;
        }

        .logout-btn {
            background: rgba(255, 255, 255, 0.2);
            color: white;
            padding: 0.5rem 1rem;
            border: none;
            border-radius: 0.375rem;
            text-decoration: none;
            font-size: 0.875rem;
            transition: background 0.2s;
        }

        .logout-btn:hover {
            background: rgba(255, 255, 255, 0.3);
        }

        .container {
            max-width: 1400px;
            margin: 0 auto;
            padding: 2rem;
        }

        .breadcrumb {
            background: white;
            padding: 1rem;
            border-radius: 0.5rem;
            margin-bottom: 1.5rem;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
        }

        .breadcrumb a {
            color: #667eea;
            text-decoration: none;
        }

        .breadcrumb a:hover {
            text-decoration: underline;
        }

        .page-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 2rem;
        }

        .page-title {
            font-size: 1.875rem;
            font-weight: 700;
            color: #1f2937;
        }

        .add-btn {
            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
            color: white;
            padding: 0.75rem 1.5rem;
            border: none;
            border-radius: 0.5rem;
            text-decoration: none;
            font-weight: 500;
            transition: transform 0.2s, box-shadow 0.2s;
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
        }

        .add-btn:hover {
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
        }

        .alert {
            padding: 1rem;
            border-radius: 0.5rem;
            margin-bottom: 1.5rem;
            font-weight: 500;
        }

        .alert-success {
            background: #d1fae5;
            color: #065f46;
            border: 1px solid #a7f3d0;
        }

        .alert-error {
            background: #fee2e2;
            color: #991b1b;
            border: 1px solid #fecaca;
        }

        .administrators-table {
            background: white;
            border-radius: 0.75rem;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
            overflow: hidden;
        }

        .table-header {
            background: #f9fafb;
            padding: 1.5rem;
            border-bottom: 1px solid #e5e7eb;
        }

        .table-header h3 {
            font-size: 1.25rem;
            font-weight: 600;
            color: #1f2937;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th, td {
            padding: 1rem;
            text-align: left;
            border-bottom: 1px solid #e5e7eb;
        }

        th {
            background: #f9fafb;
            font-weight: 600;
            color: #374151;
            font-size: 0.875rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }

        tr:hover {
            background: #f9fafb;
        }

        .status-badge {
            padding: 0.25rem 0.75rem;
            border-radius: 9999px;
            font-size: 0.75rem;
            font-weight: 500;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }

        .status-active {
            background: #d1fae5;
            color: #065f46;
        }

        .status-inactive {
            background: #fee2e2;
            color: #991b1b;
        }

        .status-locked {
            background: #fef3c7;
            color: #92400e;
        }

        .type-badge {
            padding: 0.25rem 0.75rem;
            border-radius: 0.375rem;
            font-size: 0.75rem;
            font-weight: 500;
            text-transform: uppercase;
        }

        .type-super {
            background: #ddd6fe;
            color: #5b21b6;
        }

        .type-department {
            background: #dbeafe;
            color: #1e40af;
        }

        .type-general {
            background: #f3f4f6;
            color: #374151;
        }

        .actions {
            display: flex;
            gap: 0.5rem;
            flex-wrap: wrap;
        }

        .btn {
            padding: 0.375rem 0.75rem;
            border: none;
            border-radius: 0.375rem;
            font-size: 0.75rem;
            font-weight: 500;
            text-decoration: none;
            cursor: pointer;
            transition: all 0.2s;
            display: inline-flex;
            align-items: center;
            gap: 0.25rem;
        }        .btn-edit {
            background: #fef3c7;
            color: #92400e;
        }

        .btn-edit:hover {
            background: #fde68a;
        }        .btn-danger {
            background: #fee2e2;
            color: #991b1b;
        }

        .btn-danger:hover {
            background: #fecaca;
        }

        .empty-state {
            text-align: center;
            padding: 4rem 2rem;
            color: #6b7280;
        }

        .empty-state svg {
            width: 4rem;
            height: 4rem;
            margin: 0 auto 1rem;
            opacity: 0.5;
        }

        .empty-state h3 {
            font-size: 1.125rem;
            font-weight: 600;
            color: #374151;
            margin-bottom: 0.5rem;
        }

        .table-overflow {
            overflow-x: auto;
        }

        @media (max-width: 1024px) {
            .container {
                padding: 1rem;
            }

            table {
                min-width: 1000px;
            }

            .actions {
                flex-direction: column;
            }
        }
    </style>
</head>
<body>
    <header class="header">
        <div class="header-content">
            <h1>校园通行码预约管理系统</h1>
            <div class="admin-info">
                <span>欢迎，<%= admin.getFullName() %></span>
                <a href="${pageContext.request.contextPath}/index.jsp" class="logout-btn" onclick="return confirmLogout()">退出登录</a>
            </div>
        </div>
    </header>    <div class="container">
        <div class="breadcrumb">
            <a href="${pageContext.request.contextPath}/admin/dashboard">仪表板</a>
            <span> / </span>
            <span><%= pageTitle %></span>
        </div>

        <div class="page-header">
            <h2 class="page-title"><%= pageTitle %></h2>            <% if (!"department_admin".equals(admin.getAdminType()) && errorFromRequest == null) { %>
                <a href="${pageContext.request.contextPath}/admin/administrator?action=add" class="add-btn">
                    ➕ 添加管理员
                </a>
            <% } %>
        </div>        <% if (success != null) { %>
            <div class="alert alert-success">
                ✅ <%= success %>
            </div>
        <% } %>

        <% if (error != null) { %>
            <div class="alert alert-error">
                ❌ <%= error %>
            </div>
        <% } %>
        
        <% if (errorFromRequest != null) { %>
            <div class="alert alert-error">
                ❌ <%= errorFromRequest %>
            </div>
        <% } %>        <% if (errorFromRequest == null) { %>
            <div class="administrators-table">
                <div class="table-header">
                    <h3>管理员列表</h3>
                </div>
                
                <% if (administrators != null && !administrators.isEmpty()) { %>
                <div class="table-overflow">
                    <table>
                        <thead>
                            <tr>
                                <th>用户名</th>
                                <th>姓名</th>
                                <th>手机号</th>
                                <th>所属部门</th>
                                <th>管理员类型</th>
                                <th>状态</th>
                                <th>创建时间</th>
                                <th>最后登录</th>
                                <th>操作</th>
                            </tr>
                        </thead>                        <tbody>
                            <% for (Administrator adm : administrators) { %>
                                <tr>
                                    <td>
                                        <strong><%= adm.getUsername() %></strong>
                                    </td>
                                    <td style="white-space: nowrap;">
                                        <%= adm.getFullName() %>
                                    </td>
                                    <td>
                                        <%
                                            String phoneDisplay = "未设置";
                                            if (adm.getPhoneEncrypted() != null && !adm.getPhoneEncrypted().trim().isEmpty()) {
                                                try {
                                                    phoneDisplay = DataEncryptionUtil.decrypt(adm.getPhoneEncrypted());
                                                } catch (Exception e) {
                                                    phoneDisplay = "解密失败";
                                                }
                                            }
                                        %>
                                        <%= phoneDisplay %>
                                    </td>
                                    <td>
                                        <% 
                                            Department dept = departmentMap != null ? departmentMap.get(adm.getDepartmentId()) : null;
                                            String deptName = dept != null ? dept.getDepartmentName() : "未分配";
                                        %>
                                        <%= deptName %>
                                    </td>
                                    <td>
                                        <span class="type-badge type-<%= adm.getAdminType() %>">
                                            <%= "super".equals(adm.getAdminType()) ? "超级管理员" :
                                                "department_admin".equals(adm.getAdminType()) ? "部门管理员" : 
                                                "school_admin".equals(adm.getAdminType()) ? "学校管理员" :
                                                "audit_admin".equals(adm.getAdminType()) ? "审计管理员" :
                                                "system_admin".equals(adm.getAdminType()) ? "系统管理员" : "普通管理员" %>
                                        </span>
                                    </td>
                                    <td>
                                        <span class="status-badge status-<%= adm.getStatus() %>">
                                            <%= "active".equals(adm.getStatus()) ? "正常" : 
                                                "locked".equals(adm.getStatus()) ? "锁定" : "禁用" %>
                                        </span>
                                    </td>                                    <td>
                                        <%= adm.getCreatedAt() != null ? adm.getCreatedAt().format(formatter) : "未知" %>
                                    </td>
                                    <td>
                                        <%= adm.getLastLoginTime() != null ? adm.getLastLoginTime().format(formatter) : "从未登录" %>
                                    </td>
                                    <td>
                                        <div class="actions">                                            <a href="${pageContext.request.contextPath}/admin/administrator?action=edit&id=<%= adm.getId() %>" 
                                               class="btn btn-edit" title="编辑管理员">
                                                ✏️ 编辑
                                            </a>
                                            
                                            <% if (!adm.getId().equals(admin.getId())) { %>
                                                <form method="post" style="display: inline-block;" 
                                                      onsubmit="return confirm('确定要删除管理员【<%= adm.getFullName() %>】吗？\n\n⚠️ 此操作不可恢复！')">
                                                    <input type="hidden" name="action" value="delete">
                                                    <input type="hidden" name="id" value="<%= adm.getId() %>">
                                                    <button type="submit" class="btn btn-danger" title="删除管理员">
                                                        🗑️ 删除
                                                    </button>
                                                </form>
                                            <% } %>
                                        </div>
                                    </td>
                                </tr>                            <% } %>
                        </tbody>
                    </table>
                </div>                <% } else { %>
                    <div class="empty-state">
                        <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                                  d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.5 2.5 0 11-5 0 2.5 2.5 0 015 0z"></path>
                        </svg>
                        <h3>暂无管理员数据</h3>                        <% if (!"department_admin".equals(admin.getAdminType()) && errorFromRequest == null) { %>
                            <p>点击上方"添加管理员"按钮创建第一个管理员账户</p>
                        <% } %>
                    </div>
                <% } %>
            </div>
        <% } %>
    </div>
    
    <script>
        function confirmLogout() {
            return confirm('确认退出登录？这将清除您的会话信息。');
        }
    </script>
</body>
</html>
