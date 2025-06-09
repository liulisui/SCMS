<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.example.scms.model.Administrator" %>

<%
    Administrator admin = (Administrator) request.getAttribute("admin");
    String error = request.getParameter("error");
%>

<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>添加部门 - 校园通行码预约管理系统</title>
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
            max-width: 800px;
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
            margin-bottom: 2rem;
        }

        .page-title {
            font-size: 1.875rem;
            font-weight: 700;
            color: #1f2937;
            margin-bottom: 0.5rem;
        }

        .page-subtitle {
            color: #6b7280;
        }

        .alert {
            padding: 1rem;
            border-radius: 0.5rem;
            margin-bottom: 1.5rem;
            font-weight: 500;
        }

        .alert-error {
            background: #fee2e2;
            color: #991b1b;
            border: 1px solid #fca5a5;
        }

        .form-card {
            background: white;
            border-radius: 0.75rem;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
            padding: 2rem;
        }

        .form-group {
            margin-bottom: 1.5rem;
        }

        .form-label {
            display: block;
            font-weight: 600;
            color: #374151;
            margin-bottom: 0.5rem;
        }

        .required {
            color: #dc2626;
        }

        .form-input,
        .form-select,
        .form-textarea {
            width: 100%;
            padding: 0.75rem;
            border: 1px solid #d1d5db;
            border-radius: 0.5rem;
            font-size: 1rem;
            transition: border-color 0.2s, box-shadow 0.2s;
        }

        .form-input:focus,
        .form-select:focus,
        .form-textarea:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }

        .form-textarea {
            resize: vertical;
            min-height: 100px;
        }

        .form-help {
            font-size: 0.875rem;
            color: #6b7280;
            margin-top: 0.25rem;
        }

        .form-actions {
            display: flex;
            gap: 1rem;
            margin-top: 2rem;
            padding-top: 1.5rem;
            border-top: 1px solid #e5e7eb;
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
            justify-content: center;
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

        .form-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 1rem;
        }

        @media (max-width: 768px) {
            .container {
                padding: 1rem;
            }

            .form-card {
                padding: 1.5rem;
            }

            .form-row {
                grid-template-columns: 1fr;
            }

            .form-actions {
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
            <span>添加部门</span>
        </div>

        <div class="page-header">
            <h2 class="page-title">添加部门</h2>
            <p class="page-subtitle">创建新的部门信息</p>
        </div>

        <% if (error != null) { %>
            <div class="alert alert-error">
                ❌ <%= error %>
            </div>
        <% } %>

        <div class="form-card">
            <form method="post" action="${pageContext.request.contextPath}/admin/department">
                <input type="hidden" name="action" value="create">
                
                <div class="form-row">
                    <div class="form-group">
                        <label for="departmentName" class="form-label">
                            部门名称 <span class="required">*</span>
                        </label>
                        <input type="text" id="departmentName" name="departmentName" 
                               class="form-input" required maxlength="100"
                               placeholder="请输入部门名称">
                        <div class="form-help">例如：信息技术中心、学生事务处等</div>
                    </div>
                    
                    <div class="form-group">
                        <label for="departmentCode" class="form-label">
                            部门代码 <span class="required">*</span>
                        </label>
                        <input type="text" id="departmentCode" name="departmentCode" 
                               class="form-input" required maxlength="20"
                               placeholder="请输入部门代码" style="text-transform: uppercase;">
                        <div class="form-help">唯一标识，建议使用英文缩写，如：IT、STUDENT等</div>
                    </div>
                </div>

                <div class="form-group">
                    <label for="departmentType" class="form-label">
                        部门类型 <span class="required">*</span>
                    </label>                    <select id="departmentType" name="departmentType" class="form-select" required>
                        <option value="">请选择部门类型</option>
                        <option value="academic">教学部门</option>
                        <option value="administrative">行政部门</option>
                        <option value="direct">直属部门</option>
                    </select>
                    <div class="form-help">根据部门职能选择相应类型</div>
                </div>

                <div class="form-group">
                    <label for="description" class="form-label">部门描述</label>
                    <textarea id="description" name="description" class="form-textarea" 
                              maxlength="500" placeholder="请输入部门描述（可选）"></textarea>
                    <div class="form-help">详细描述部门的职能和作用</div>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn btn-primary">
                        ✅ 创建部门
                    </button>
                    <a href="${pageContext.request.contextPath}/admin/department" class="btn btn-secondary">
                        ❌ 取消
                    </a>
                </div>
            </form>
        </div>
    </div>

    <script>
        // 自动转换部门代码为大写
        document.getElementById('departmentCode').addEventListener('input', function(e) {
            e.target.value = e.target.value.toUpperCase();
        });

        // 表单验证
        document.querySelector('form').addEventListener('submit', function(e) {
            const departmentName = document.getElementById('departmentName').value.trim();
            const departmentCode = document.getElementById('departmentCode').value.trim();
            const departmentType = document.getElementById('departmentType').value;

            if (!departmentName || !departmentCode || !departmentType) {
                e.preventDefault();
                alert('请填写所有必填字段');
                return;
            }

            if (departmentCode.length < 2) {
                e.preventDefault();
                alert('部门代码至少需要2个字符');
                return;
            }

            if (!/^[A-Z0-9_]+$/.test(departmentCode)) {
                e.preventDefault();
                alert('部门代码只能包含大写字母、数字和下划线');
                return;            }
        });
        
        function confirmLogout() {
            return confirm('确认退出登录？这将清除您的会话信息。');
        }
    </script>
</body>
</html>
