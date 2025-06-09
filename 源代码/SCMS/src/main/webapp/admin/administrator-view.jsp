<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.example.scms.model.Administrator" %>
<%@ page import="org.example.scms.model.Department" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<%
    Administrator admin = (Administrator) request.getAttribute("admin");
    Administrator viewAdmin = (Administrator) request.getAttribute("targetAdmin");
    Department department = (Department) request.getAttribute("department");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
%>

<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>管理员详情 - 校园通行码预约管理系统</title>
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
            max-width: 900px;
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

        .details-container {
            background: white;
            border-radius: 0.75rem;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
            overflow: hidden;
        }

        .details-header {
            background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
            padding: 2rem;
            border-bottom: 1px solid #e5e7eb;
            text-align: center;
        }

        .avatar {
            width: 80px;
            height: 80px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 1rem;
            font-size: 2rem;
            color: white;
            font-weight: bold;
        }

        .user-name {
            font-size: 1.5rem;
            font-weight: 700;
            color: #1f2937;
            margin-bottom: 0.5rem;
        }

        .user-title {
            color: #6b7280;
            font-size: 1rem;
        }

        .details-content {
            padding: 2rem;
        }

        .info-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 2rem;
        }

        .info-section {
            background: #f9fafb;
            padding: 1.5rem;
            border-radius: 0.5rem;
            border: 1px solid #e5e7eb;
        }

        .section-title {
            font-size: 1.125rem;
            font-weight: 600;
            color: #374151;
            margin-bottom: 1rem;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }

        .info-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0.75rem 0;
            border-bottom: 1px solid #e5e7eb;
        }

        .info-item:last-child {
            border-bottom: none;
        }

        .info-label {
            font-weight: 500;
            color: #6b7280;
            flex-shrink: 0;
            min-width: 80px;
        }

        .info-value {
            font-weight: 600;
            color: #374151;
            text-align: right;
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
            justify-content: space-between;
            gap: 1rem;
            margin-top: 2rem;
        }

        .btn {
            padding: 0.75rem 1.5rem;
            border: none;
            border-radius: 0.5rem;
            font-size: 1rem;
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
        }

        .btn-secondary:hover {
            background: #e5e7eb;
        }

        .btn-warning {
            background: #fef3c7;
            color: #92400e;
        }

        .btn-warning:hover {
            background: #fde68a;
        }

        .empty-value {
            color: #9ca3af;
            font-style: italic;
        }

        @media (max-width: 768px) {
            .container {
                padding: 1rem;
            }

            .info-grid {
                grid-template-columns: 1fr;
            }

            .page-header {
                flex-direction: column;
                gap: 1rem;
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
            <a href="${pageContext.request.contextPath}/admin/administrator">管理员管理</a>
            <span> / </span>
            <span>查看详情</span>
        </div>

        <div class="page-header">
            <h2 class="page-title">管理员详情</h2>
        </div>

        <% if (viewAdmin != null) { %>
            <div class="details-container">
                <div class="details-header">
                    <div class="avatar">
                        <%= viewAdmin.getFullName() != null && viewAdmin.getFullName().length() > 0 ? 
                            viewAdmin.getFullName().substring(0, 1) : "?" %>
                    </div>
                    <div class="user-name"><%= viewAdmin.getFullName() %></div>
                    <div class="user-title">
                        <span class="type-badge type-<%= viewAdmin.getAdminType() %>">
                            <%= "super".equals(viewAdmin.getAdminType()) ? "超级管理员" :
                                "department".equals(viewAdmin.getAdminType()) ? "部门管理员" : "普通管理员" %>
                        </span>
                    </div>
                </div>

                <div class="details-content">
                    <div class="info-grid">
                        <div class="info-section">
                            <h3 class="section-title">👤 基本信息</h3>
                            <div class="info-item">
                                <span class="info-label">用户名</span>
                                <span class="info-value"><%= viewAdmin.getUsername() %></span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">姓名</span>
                                <span class="info-value"><%= viewAdmin.getFullName() %></span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">手机号</span>
                                <span class="info-value">
                                    <%= viewAdmin.getPhoneEncrypted() != null ? viewAdmin.getPhoneEncrypted() : 
                                        "<span class='empty-value'>未设置</span>" %>
                                </span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">状态</span>
                                <span class="info-value">
                                    <span class="status-badge status-<%= viewAdmin.getStatus() %>">
                                        <%= "active".equals(viewAdmin.getStatus()) ? "正常" : 
                                            "locked".equals(viewAdmin.getStatus()) ? "锁定" : "禁用" %>
                                    </span>
                                </span>
                            </div>
                        </div>

                        <div class="info-section">
                            <h3 class="section-title">🏢 部门信息</h3>
                            <div class="info-item">
                                <span class="info-label">所属部门</span>
                                <span class="info-value">
                                    <%= department != null ? department.getDepartmentName() : 
                                        "<span class='empty-value'>未分配</span>" %>
                                </span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">部门代码</span>
                                <span class="info-value">
                                    <%= department != null ? department.getDepartmentCode() : 
                                        "<span class='empty-value'>-</span>" %>
                                </span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">部门类型</span>
                                <span class="info-value">
                                    <% if (department != null) { %>
                                        <span class="type-badge type-<%= department.getDepartmentType() %>">
                                            <%= "academic".equals(department.getDepartmentType()) ? "教学" :
                                                "administrative".equals(department.getDepartmentType()) ? "行政" :
                                                "support".equals(department.getDepartmentType()) ? "支持" : "其他" %>
                                        </span>
                                    <% } else { %>
                                        <span class="empty-value">-</span>
                                    <% } %>
                                </span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">管理员类型</span>
                                <span class="info-value">
                                    <span class="type-badge type-<%= viewAdmin.getAdminType() %>">
                                        <%= "super".equals(viewAdmin.getAdminType()) ? "超级管理员" :
                                            "department".equals(viewAdmin.getAdminType()) ? "部门管理员" : "普通管理员" %>
                                    </span>
                                </span>
                            </div>
                        </div>

                        <div class="info-section">
                            <h3 class="section-title">🕐 时间信息</h3>
                            <div class="info-item">
                                <span class="info-label">创建时间</span>
                                <span class="info-value">
                                    <%= viewAdmin.getCreatedAt() != null ? viewAdmin.getCreatedAt().format(formatter) : 
                                        "<span class='empty-value'>未知</span>" %>
                                </span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">更新时间</span>
                                <span class="info-value">
                                    <%= viewAdmin.getUpdatedAt() != null ? viewAdmin.getUpdatedAt().format(formatter) : 
                                        "<span class='empty-value'>未更新</span>" %>
                                </span>
                            </div>                            <div class="info-item">
                                <span class="info-label">最后登录</span>
                                <span class="info-value">
                                    <%= viewAdmin.getLastLoginTime() != null ? viewAdmin.getLastLoginTime().format(formatter) : 
                                        "<span class='empty-value'>从未登录</span>" %>
                                </span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">最后登录IP</span>
                                <span class="info-value">
                                    <span class='empty-value'>暂不支持</span>
                                </span>
                            </div>
                        </div>

                        <div class="info-section">
                            <h3 class="section-title">🔒 安全信息</h3>                            <div class="info-item">
                                <span class="info-label">登录失败次数</span>
                                <span class="info-value"><%= viewAdmin.getLoginFailCount() != null ? viewAdmin.getLoginFailCount() : 0 %></span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">锁定时间</span>
                                <span class="info-value">
                                    <%= viewAdmin.getLockUntilTime() != null ? viewAdmin.getLockUntilTime().format(formatter) : 
                                        "<span class='empty-value'>未锁定</span>" %>
                                </span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">账户状态</span>
                                <span class="info-value">
                                    <% if ("locked".equals(viewAdmin.getStatus())) { %>
                                        <span style="color: #dc2626;">🔒 账户已锁定</span>
                                    <% } else if ("inactive".equals(viewAdmin.getStatus())) { %>
                                        <span style="color: #dc2626;">⏸️ 账户已停用</span>
                                    <% } else { %>
                                        <span style="color: #059669;">✅ 账户正常</span>
                                    <% } %>
                                </span>
                            </div>
                        </div>
                    </div>

                    <div class="actions">
                        <a href="${pageContext.request.contextPath}/admin/administrator" class="btn btn-secondary">
                            ← 返回列表
                        </a>
                        <div style="display: flex; gap: 1rem;">
                            <a href="${pageContext.request.contextPath}/admin/administrator?action=edit&id=<%= viewAdmin.getId() %>" 
                               class="btn btn-primary">
                                ✏️ 编辑管理员
                            </a>
                            <% if (!"active".equals(viewAdmin.getStatus())) { %>
                                <form method="post" style="display: inline-block;">
                                    <input type="hidden" name="action" value="activate">
                                    <input type="hidden" name="id" value="<%= viewAdmin.getId() %>">
                                    <button type="submit" class="btn btn-primary">
                                        ▶️ 激活账户
                                    </button>
                                </form>
                            <% } %>
                            <form method="post" style="display: inline-block;" 
                                  onsubmit="return confirm('确定要重置管理员【<%= viewAdmin.getFullName() %>】的密码吗？\n\n新密码将被设置为：123456')">
                                <input type="hidden" name="action" value="resetPassword">
                                <input type="hidden" name="id" value="<%= viewAdmin.getId() %>">
                                <button type="submit" class="btn btn-warning">
                                    🔑 重置密码
                                </button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        <% } else { %>
            <div class="details-container">
                <div class="details-content" style="text-align: center; padding: 4rem 2rem;">
                    <h3 style="color: #dc2626; margin-bottom: 1rem;">❌ 管理员不存在</h3>
                    <p style="color: #6b7280; margin-bottom: 2rem;">未找到指定的管理员信息，可能已被删除或不存在。</p>
                    <a href="${pageContext.request.contextPath}/admin/administrator" class="btn btn-secondary">
                        ← 返回列表
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
