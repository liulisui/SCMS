<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.example.scms.model.Department" %>
<%@ page import="org.example.scms.model.Administrator" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<%
    List<Department> departments = (List<Department>) request.getAttribute("departments");
    Administrator admin = (Administrator) request.getAttribute("admin");
    Map<Long, Administrator> departmentAdminMap = (Map<Long, Administrator>) request.getAttribute("departmentAdminMap");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");    String error = request.getParameter("error");
    String success = request.getParameter("success");
    String errorFromRequest = (String) request.getAttribute("error");
%>

<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>部门管理 - 校园通行码预约管理系统</title>
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
            max-width: 1200px;
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

        .admin-info span {
            font-size: 0.9rem;
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
            max-width: 1200px;
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
            border: 1px solid #fca5a5;
        }

        .departments-table {
            background: white;
            border-radius: 0.75rem;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
            overflow: hidden;
        }

        .table-header {
            background: #f8fafc;
            padding: 1.5rem;
            border-bottom: 1px solid #e5e7eb;
        }

        .table-header h3 {
            font-size: 1.125rem;
            font-weight: 600;
            color: #374151;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        thead th {
            background: #f9fafb;
            padding: 1rem;
            text-align: left;
            font-weight: 600;
            color: #374151;
            border-bottom: 1px solid #e5e7eb;
        }

        tbody td {
            padding: 1rem;
            border-bottom: 1px solid #f3f4f6;
            vertical-align: middle;
        }

        tbody tr:hover {
            background: #f9fafb;
        }        .status-badge {
            padding: 0.25rem 0.75rem;
            border-radius: 9999px;
            font-size: 0.75rem;
            font-weight: 500;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            white-space: nowrap;
        }

        .status-active {
            background: #d1fae5;
            color: #065f46;
        }

        .status-inactive {
            background: #fee2e2;
            color: #991b1b;
        }

        .type-badge {
            padding: 0.25rem 0.75rem;
            border-radius: 0.375rem;
            font-size: 0.75rem;
            font-weight: 500;
            text-transform: uppercase;
            white-space: nowrap;
        }

        .type-academic {
            background: #dbeafe;
            color: #1e40af;
        }

        .type-administrative {
            background: #fef3c7;
            color: #92400e;
        }

        .type-support {
            background: #e0e7ff;
            color: #5b21b6;
        }

        .type-other {
            background: #f3f4f6;
            color: #374151;
        }

        .department-name {
            white-space: nowrap;
        }

        .actions {
            display: flex;
            gap: 0.5rem;
        }

        .btn {
            padding: 0.5rem 1rem;
            border: none;
            border-radius: 0.375rem;
            font-size: 0.875rem;
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
        }

        .btn-danger {
            background: #fee2e2;
            color: #991b1b;
        }

        .btn-danger:hover {
            background: #fca5a5;
        }

        .empty-state {
            text-align: center;
            padding: 3rem;
            color: #6b7280;
        }

        .empty-state svg {
            width: 4rem;
            height: 4rem;
            margin: 0 auto 1rem;
            opacity: 0.5;
        }

        @media (max-width: 768px) {
            .container {
                padding: 1rem;
            }

            .page-header {
                flex-direction: column;
                gap: 1rem;
                align-items: stretch;
            }

            .departments-table {
                overflow-x: auto;
            }

            table {
                min-width: 800px;
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
    </header>

    <div class="container">
        <div class="breadcrumb">
            <a href="${pageContext.request.contextPath}/admin/dashboard">仪表板</a>
            <span> / </span>
            <span>部门管理</span>
        </div>        <div class="page-header">
            <h2 class="page-title">部门管理</h2>
            <% if (errorFromRequest == null && !"department_admin".equals(admin.getAdminType())) { %>
                <a href="${pageContext.request.contextPath}/admin/department?action=add" class="add-btn">
                    ➕ 添加部门
                </a>
            <% } %>
        </div>

        <% if (success != null) { %>
            <div class="alert alert-success">
                ✅ <%= success %>
            </div>
        <% } %>        <% if (error != null) { %>
            <div class="alert alert-error">
                ❌ <%= error %>
            </div>
        <% } %>        <% if (errorFromRequest != null) { %>
            <div class="alert alert-error">
                ❌ <%= errorFromRequest %>
            </div>
        <% } %>

        <% if (errorFromRequest == null && !"department_admin".equals(admin.getAdminType())) { %>
            <div class="departments-table">
            <div class="table-header">
                <h3>部门列表</h3>
            </div>
            
            <% if (departments != null && !departments.isEmpty()) { %>
                <table>
                    <thead>
                        <tr>
                            <th>部门名称</th>
                            <th>部门代码</th>
                            <th>部门类型</th>
                            <th>状态</th>
                            <th>创建时间</th>
                            <th>描述</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>                        <% for (Department dept : departments) { %>
                            <tr>
                                <td>
                                    <strong class="department-name"><%= dept.getDepartmentName() %></strong>
                                </td>
                                <td>
                                    <code><%= dept.getDepartmentCode() %></code>
                                </td>
                                <td>                                    <span class="type-badge type-<%= dept.getDepartmentType() %>">
                                        <%= "academic".equals(dept.getDepartmentType()) ? "教学" :
                                            "administrative".equals(dept.getDepartmentType()) ? "行政" : "其他" %>
                                    </span>
                                </td>
                                <td>
                                    <span class="status-badge status-<%= dept.getStatus() %>">
                                        <%= "active".equals(dept.getStatus()) ? "启用" : "禁用" %>
                                    </span>
                                </td>
                                <td>
                                    <%= dept.getCreatedAt() != null ? dept.getCreatedAt().format(formatter) : "未知" %>
                                </td>
                                <td>
                                    <span title="<%= dept.getDescription() != null ? dept.getDescription() : "无描述" %>">
                                        <%= dept.getDescription() != null && dept.getDescription().length() > 20 ? 
                                            dept.getDescription().substring(0, 20) + "..." : 
                                            (dept.getDescription() != null ? dept.getDescription() : "无描述") %>
                                    </span>
                                </td>
                                <td>
                                    <div class="actions">
                                        <a href="${pageContext.request.contextPath}/admin/department?action=edit&id=<%= dept.getId() %>" 
                                           class="btn btn-edit" title="编辑部门">
                                            ✏️ 编辑
                                        </a>
                                        
                                        <form method="post" style="display: inline-block;" 
                                              onsubmit="return confirm('确定要删除部门【<%= dept.getDepartmentName() %>】吗？\n\n⚠️ 此操作不可恢复，请确认该部门没有关联的管理员或预约记录。')">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="<%= dept.getId() %>">
                                            <button type="submit" class="btn btn-danger" title="删除部门">
                                                🗑️ 删除
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            <% } else { %>
                <div class="empty-state">
                    <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                              d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path>
                    </svg>
                    <h3>暂无部门数据</h3>
                    <p>点击上方"添加部门"按钮创建第一个部门</p>
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
