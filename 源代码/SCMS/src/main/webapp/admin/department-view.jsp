<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="org.example.scms.model.Department" %>
<%@ page import="org.example.scms.model.Administrator" %>
<%@ page import="org.example.scms.model.ReservationAuthorization" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<%
    Department department = (Department) request.getAttribute("department");
    Administrator admin = (Administrator) request.getAttribute("admin");
    List<ReservationAuthorization> authorizations = (List<ReservationAuthorization>) request.getAttribute("authorizations");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
%>

<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>部门详情 - 校园通行码预约管理系统</title>
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
            max-width: 1000px;
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

        .actions {
            display: flex;
            gap: 0.5rem;
        }

        .btn {
            padding: 0.75rem 1.5rem;
            border: none;
            border-radius: 0.5rem;
            font-size: 0.875rem;
            font-weight: 500;
            text-decoration: none;
            cursor: pointer;
            transition: all 0.2s;
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
        }

        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }

        .btn-primary:hover {
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
        }

        .btn-secondary {
            background: #f3f4f6;
            color: #374151;
            border: 1px solid #d1d5db;
        }

        .btn-secondary:hover {
            background: #e5e7eb;
        }

        .btn-warning {
            background: #fbbf24;
            color: #92400e;
        }

        .btn-warning:hover {
            background: #f59e0b;
        }

        .content-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 2rem;
        }

        .info-card,
        .permissions-card {
            background: white;
            border-radius: 0.75rem;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
            padding: 2rem;
        }

        .card-title {
            font-size: 1.25rem;
            font-weight: 600;
            color: #1f2937;
            margin-bottom: 1.5rem;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }

        .info-item {
            margin-bottom: 1.5rem;
        }

        .info-label {
            font-weight: 600;
            color: #6b7280;
            font-size: 0.875rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            margin-bottom: 0.5rem;
        }

        .info-value {
            font-size: 1rem;
            color: #1f2937;
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

        .type-badge {
            padding: 0.5rem 1rem;
            border-radius: 0.5rem;
            font-size: 0.875rem;
            font-weight: 500;
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

        .permissions-list {
            list-style: none;
        }

        .permission-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 1rem;
            border: 1px solid #e5e7eb;
            border-radius: 0.5rem;
            margin-bottom: 1rem;
        }

        .permission-info {
            flex: 1;
        }

        .permission-type {
            font-weight: 600;
            color: #1f2937;
            margin-bottom: 0.25rem;
        }

        .permission-meta {
            font-size: 0.875rem;
            color: #6b7280;
        }

        .permission-badge {
            padding: 0.25rem 0.75rem;
            border-radius: 0.375rem;
            font-size: 0.75rem;
            font-weight: 500;
            text-transform: uppercase;
        }

        .permission-public {
            background: #dbeafe;
            color: #1e40af;
        }

        .permission-official {
            background: #fef3c7;
            color: #92400e;
        }

        .permission-both {
            background: #e0e7ff;
            color: #5b21b6;
        }

        .empty-state {
            text-align: center;
            padding: 2rem;
            color: #6b7280;
        }

        .empty-state svg {
            width: 3rem;
            height: 3rem;
            margin: 0 auto 1rem;
            opacity: 0.5;
        }

        .description-box {
            background: #f9fafb;
            border: 1px solid #e5e7eb;
            border-radius: 0.5rem;
            padding: 1rem;
            font-style: italic;
            color: #6b7280;
            line-height: 1.5;
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

            .content-grid {
                grid-template-columns: 1fr;
                gap: 1.5rem;
            }

            .info-card,
            .permissions-card {
                padding: 1.5rem;
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
            <a href="${pageContext.request.contextPath}/admin/department">部门管理</a>
            <span> / </span>
            <span>部门详情</span>
        </div>

        <% if (department != null) { %>
            <div class="page-header">
                <h2 class="page-title"><%= department.getDepartmentName() %></h2>
                <div class="actions">
                    <a href="${pageContext.request.contextPath}/admin/department?action=edit&id=<%= department.getId() %>" 
                       class="btn btn-primary">
                        ✏️ 编辑
                    </a>
                    <a href="${pageContext.request.contextPath}/admin/department?action=permissions&id=<%= department.getId() %>" 
                       class="btn btn-warning">
                        🔐 权限管理
                    </a>
                    <a href="${pageContext.request.contextPath}/admin/department" class="btn btn-secondary">
                        ← 返回列表
                    </a>
                </div>
            </div>

            <div class="content-grid">
                <div class="info-card">
                    <h3 class="card-title">🏢 基本信息</h3>
                    
                    <div class="info-item">
                        <div class="info-label">部门名称</div>
                        <div class="info-value"><%= department.getDepartmentName() %></div>
                    </div>

                    <div class="info-item">
                        <div class="info-label">部门代码</div>
                        <div class="info-value"><code><%= department.getDepartmentCode() %></code></div>
                    </div>

                    <div class="info-item">
                        <div class="info-label">部门类型</div>
                        <div class="info-value">                            <span class="type-badge type-<%= department.getDepartmentType() %>">
                                <%= "academic".equals(department.getDepartmentType()) ? "教学部门" :
                                    "administrative".equals(department.getDepartmentType()) ? "行政部门" : "其他" %>
                            </span>
                        </div>
                    </div>

                    <div class="info-item">
                        <div class="info-label">状态</div>
                        <div class="info-value">
                            <span class="status-badge status-<%= department.getStatus() %>">
                                <%= "active".equals(department.getStatus()) ? "启用" : "禁用" %>
                            </span>
                        </div>
                    </div>

                    <div class="info-item">
                        <div class="info-label">创建时间</div>
                        <div class="info-value">
                            <%= department.getCreatedAt() != null ? department.getCreatedAt().format(formatter) : "未知" %>
                        </div>
                    </div>

                    <div class="info-item">
                        <div class="info-label">更新时间</div>
                        <div class="info-value">
                            <%= department.getUpdatedAt() != null ? department.getUpdatedAt().format(formatter) : "未更新" %>
                        </div>
                    </div>

                    <% if (department.getDescription() != null && !department.getDescription().trim().isEmpty()) { %>
                        <div class="info-item">
                            <div class="info-label">部门描述</div>
                            <div class="description-box">
                                <%= department.getDescription() %>
                            </div>
                        </div>
                    <% } %>
                </div>

                <div class="permissions-card">
                    <h3 class="card-title">🔐 预约权限</h3>
                    
                    <% if (authorizations != null && !authorizations.isEmpty()) { %>
                        <ul class="permissions-list">
                            <% for (ReservationAuthorization auth : authorizations) { %>
                                <li class="permission-item">
                                    <div class="permission-info">
                                        <div class="permission-type">
                                            <span class="permission-badge permission-<%= auth.getReservationType() %>">
                                                <%= "public".equals(auth.getReservationType()) ? "社会公众预约" :
                                                    "official".equals(auth.getReservationType()) ? "公务预约" :
                                                    "both".equals(auth.getReservationType()) ? "全部预约" : auth.getReservationType() %>
                                            </span>
                                        </div>
                                        <div class="permission-meta">
                                            状态：<%= "active".equals(auth.getStatus()) ? "有效" : "已撤销" %>
                                            | 授权时间：<%= auth.getGrantedAt() != null ? auth.getGrantedAt().format(formatter) : "未知" %>
                                        </div>
                                        <% if (auth.getDescription() != null && !auth.getDescription().trim().isEmpty()) { %>
                                            <div class="permission-meta">
                                                说明：<%= auth.getDescription() %>
                                            </div>
                                        <% } %>
                                    </div>
                                </li>
                            <% } %>
                        </ul>
                    <% } else { %>
                        <div class="empty-state">
                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                                      d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
                            </svg>
                            <h4>暂无权限</h4>
                            <p>该部门尚未被授予任何预约权限</p>
                        </div>
                    <% } %>
                </div>
            </div>
        <% } else { %>
            <div class="info-card">
                <div class="empty-state">
                    <h3>部门信息不存在</h3>
                    <p>请检查部门ID是否正确</p>
                    <a href="${pageContext.request.contextPath}/admin/department" class="btn btn-secondary">
                        返回部门列表
                    </a>
                </div>
            </div>
        <% } %>    </div>
    
    <script>
        function confirmLogout() {
            return confirm('确认退出登录？这将清除您的会话信息。');
        }
    </script>
</body>
</html>
