<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.example.scms.model.Administrator" %>
<%@ page import="org.example.scms.model.Department" %>
<%@ page import="java.util.List" %>

<%
    Administrator admin = (Administrator) request.getAttribute("admin");
    List<Department> departments = (List<Department>) request.getAttribute("departments");
    String error = request.getParameter("error");
    String source = (String) request.getAttribute("source");
    if (source == null) {
        source = request.getParameter("source");
    }
%>

<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>添加管理员 - 校园通行码预约管理系统</title>
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
            border: 1px solid #fecaca;
        }

        .form-container {
            background: white;
            padding: 2rem;
            border-radius: 0.75rem;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
        }

        .form-group {
            margin-bottom: 1.5rem;
        }

        .form-group:last-child {
            margin-bottom: 0;
        }

        label {
            display: block;
            font-weight: 600;
            color: #374151;
            margin-bottom: 0.5rem;
        }

        .required {
            color: #ef4444;
        }

        input[type="text"],
        input[type="password"],
        input[type="tel"],
        select {
            width: 100%;
            padding: 0.75rem 1rem;
            border: 1px solid #d1d5db;
            border-radius: 0.5rem;
            font-size: 1rem;
            transition: border-color 0.2s, box-shadow 0.2s;
        }

        input[type="text"]:focus,
        input[type="password"]:focus,
        input[type="tel"]:focus,
        select:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }

        .form-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 1rem;
        }

        .password-hint {
            font-size: 0.875rem;
            color: #6b7280;
            margin-top: 0.25rem;
        }

        .form-actions {
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

        @media (max-width: 768px) {
            .container {
                padding: 1rem;
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
            <a href="${pageContext.request.contextPath}/admin/administrator">管理员管理</a>
            <span> / </span>
            <span>添加管理员</span>
        </div>

        <div class="page-header">
            <h2 class="page-title">添加管理员</h2>
        </div>

        <% if (error != null) { %>
            <div class="alert alert-error">
                ❌ <%= error %>
            </div>
        <% } %>        <div class="form-container">
            <form method="post" action="${pageContext.request.contextPath}/admin/administrator">
                <input type="hidden" name="action" value="create">
                <% if (source != null) { %>
                    <input type="hidden" name="source" value="<%= source %>">
                <% } %>
                
                <div class="form-row">
                    <div class="form-group">
                        <label for="username">用户名 <span class="required">*</span></label>
                        <input type="text" id="username" name="username" required 
                               pattern="[a-zA-Z0-9_]{3,20}" 
                               title="用户名只能包含字母、数字和下划线，长度3-20位"
                               value="<%= request.getParameter("username") != null ? request.getParameter("username") : "" %>">
                    </div>
                    
                    <div class="form-group">
                        <label for="fullName">姓名 <span class="required">*</span></label>
                        <input type="text" id="fullName" name="fullName" required 
                               maxlength="50"
                               value="<%= request.getParameter("fullName") != null ? request.getParameter("fullName") : "" %>">
                    </div>
                </div>

                <div class="form-row">                    <div class="form-group">
                        <label for="password">密码 <span class="required">*</span></label>
                        <input type="password" id="password" name="password" required 
                               minlength="8" maxlength="30"
                               pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$"
                               title="密码必须包含至少8位字符，包括大写字母、小写字母、数字和特殊字符">
                        <div class="password-hint">密码长度至少8位，必须包含大写字母、小写字母、数字和特殊字符（@$!%*?&）</div>
                    </div>
                    
                    <div class="form-group">
                        <label for="confirmPassword">确认密码 <span class="required">*</span></label>
                        <input type="password" id="confirmPassword" name="confirmPassword" required 
                               minlength="8" maxlength="30"
                               pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$"
                               title="密码必须包含至少8位字符，包括大写字母、小写字母、数字和特殊字符">
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="phoneEncrypted">手机号</label>
                        <input type="tel" id="phoneEncrypted" name="phoneEncrypted" 
                               pattern="1[3-9]\d{9}" 
                               title="请输入有效的11位手机号码"
                               value="<%= request.getParameter("phoneEncrypted") != null ? request.getParameter("phoneEncrypted") : "" %>">
                    </div>
                    
                    <div class="form-group">
                        <label for="departmentId">所属部门 <span class="required">*</span></label>
                        <select id="departmentId" name="departmentId" required>
                            <option value="">请选择部门</option>
                            <% if (departments != null) {
                                String selectedDeptId = request.getParameter("departmentId");
                                for (Department dept : departments) { 
                                    if ("active".equals(dept.getStatus())) { %>
                                        <option value="<%= dept.getId() %>" 
                                                <%= dept.getId().toString().equals(selectedDeptId) ? "selected" : "" %>>
                                            <%= dept.getDepartmentName() %>
                                        </option>
                                    <% }
                                }
                            } %>
                        </select>
                    </div>                </div>                <!-- 隐藏字段：默认设置为部门管理员 -->
                <input type="hidden" name="adminType" value="department_admin"><div class="form-actions">
                    <% if ("department".equals(source)) { %>
                        <a href="${pageContext.request.contextPath}/admin/department" class="btn btn-secondary">
                            ← 返回部门列表
                        </a>
                    <% } else { %>
                        <a href="${pageContext.request.contextPath}/admin/administrator" class="btn btn-secondary">
                            ← 返回列表
                        </a>
                    <% } %>
                    <button type="submit" class="btn btn-primary">
                        ✓ 创建管理员
                    </button>
                </div>
            </form>
        </div>
    </div>    <script>
        // 密码强度验证函数
        function validatePasswordStrength(password) {
            const minLength = 8;
            const hasUpperCase = /[A-Z]/.test(password);
            const hasLowerCase = /[a-z]/.test(password);
            const hasNumbers = /\d/.test(password);
            const hasSpecialChar = /[@$!%*?&]/.test(password);
            
            return password.length >= minLength && hasUpperCase && hasLowerCase && hasNumbers && hasSpecialChar;
        }

        // 密码确认验证
        document.getElementById('confirmPassword').addEventListener('input', function() {
            const password = document.getElementById('password').value;
            const confirmPassword = this.value;
            
            if (password !== confirmPassword) {
                this.setCustomValidity('两次输入的密码不一致');
            } else {
                this.setCustomValidity('');
            }
        });

        // 密码输入时的实时验证
        document.getElementById('password').addEventListener('input', function() {
            const password = this.value;
            const confirmPassword = document.getElementById('confirmPassword');
            
            if (!validatePasswordStrength(password)) {
                this.setCustomValidity('密码必须包含至少8位字符，包括大写字母、小写字母、数字和特殊字符（@$!%*?&）');
            } else {
                this.setCustomValidity('');
            }
            
            // 如果确认密码已填写，检查是否一致
            if (confirmPassword.value && password !== confirmPassword.value) {
                confirmPassword.setCustomValidity('两次输入的密码不一致');
            } else {
                confirmPassword.setCustomValidity('');
            }
        });

        // 表单提交前验证
        document.querySelector('form').addEventListener('submit', function(e) {
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            
            if (password !== confirmPassword) {
                e.preventDefault();
                alert('两次输入的密码不一致，请重新输入！');
                return false;
            }
            
            if (!validatePasswordStrength(password)) {
                e.preventDefault();
                alert('密码必须包含至少8位字符，包括大写字母、小写字母、数字和特殊字符（@$!%*?&）！');
                return false;
            }        });
        
        function confirmLogout() {
            return confirm('确认退出登录？这将清除您的会话信息。');
        }
    </script>
</body>
</html>
