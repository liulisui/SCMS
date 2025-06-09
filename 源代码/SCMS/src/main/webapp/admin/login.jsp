<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <!DOCTYPE html>
    <html lang="zh-CN">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>管理员登录 - 校园通行码预约管理系统</title>
        <style>
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }

            body {
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                min-height: 100vh;
                display: flex;
                align-items: center;
                justify-content: center;
                padding: 2rem;
            }

            .login-container {
                background: white;
                border-radius: 16px;
                box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
                overflow: hidden;
                width: 100%;
                max-width: 400px;
                animation: slideUp 0.6s ease-out;
            }

            @keyframes slideUp {
                from {
                    opacity: 0;
                    transform: translateY(30px);
                }

                to {
                    opacity: 1;
                    transform: translateY(0);
                }
            }

            .login-header {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                padding: 2rem;
                text-align: center;
            }

            .login-header h1 {
                font-size: 1.5rem;
                margin-bottom: 0.5rem;
            }

            .login-header p {
                opacity: 0.9;
                font-size: 0.9rem;
            }

            .login-form {
                padding: 2rem;
            }

            .form-group {
                margin-bottom: 1.5rem;
            }

            .form-group label {
                display: block;
                color: #374151;
                font-weight: 500;
                margin-bottom: 0.5rem;
            }

            .form-input {
                width: 100%;
                padding: 0.75rem 1rem;
                border: 2px solid #e5e7eb;
                border-radius: 8px;
                font-size: 1rem;
                transition: all 0.3s ease;
            }

            .form-input:focus {
                outline: none;
                border-color: #667eea;
                box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
            }

            .login-btn {
                width: 100%;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                border: none;
                padding: 0.875rem;
                border-radius: 8px;
                font-size: 1rem;
                font-weight: 600;
                cursor: pointer;
                transition: all 0.3s ease;
                margin-bottom: 1rem;
            }

            .login-btn:hover {
                transform: translateY(-2px);
                box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
            }

            .login-btn:active {
                transform: translateY(0);
            }

            .back-link {
                display: block;
                text-align: center;
                color: #6b7280;
                text-decoration: none;
                font-size: 0.9rem;
                transition: color 0.3s ease;
            }

            .back-link:hover {
                color: #667eea;
            }

            .alert {
                padding: 1rem;
                border-radius: 8px;
                margin-bottom: 1rem;
                font-size: 0.9rem;
            }            .alert-error {
                background: #fee2e2;
                color: #b91c1c;
                border: 1px solid #fca5a5;
            }

            .alert-warning {
                background: #fef3c7;
                color: #d97706;
                border: 1px solid #fbbf24;
            }

            .alert-lockout {
                background: #fef2f2;
                color: #dc2626;
                border: 1px solid #f87171;
                font-weight: 600;
            }

            .alert-success {
                background: #d1fae5;
                color: #065f46;
                border: 1px solid #a7f3d0;
            }

            .system-info {
                background: #f8fafc;
                padding: 1rem;
                border-radius: 8px;
                margin-bottom: 1rem;
                font-size: 0.85rem;
                color: #64748b;
                text-align: center;
            }

            .admin-features {
                background: #f1f5f9;
                border-radius: 8px;
                padding: 1rem;
                margin-top: 1rem;
            }

            .admin-features h3 {
                color: #334155;
                font-size: 0.9rem;
                margin-bottom: 0.5rem;
            }

            .admin-features ul {
                list-style: none;
                padding: 0;
            }

            .admin-features li {
                color: #64748b;
                font-size: 0.8rem;
                margin-bottom: 0.25rem;
                padding-left: 1rem;
                position: relative;
            }

            .admin-features li::before {
                content: '✓';
                position: absolute;
                left: 0;
                color: #10b981;
                font-weight: bold;
            }

            @media (max-width: 480px) {
                body {
                    padding: 1rem;
                }

                .login-container {
                    max-width: 100%;
                }

                .login-header,
                .login-form {
                    padding: 1.5rem;
                }
            }
        </style>
    </head>

    <body>
        <div class="login-container">
            <div class="login-header">
                <h1>管理员登录</h1>
                <p>校园通行码预约管理系统</p>
            </div>            <div class="login-form">
                <% 
                String error = (String) request.getAttribute("error"); 
                String success = (String) request.getAttribute("success");
                Integer failCount = (Integer) request.getAttribute("failCount");
                Integer remainingAttempts = (Integer) request.getAttribute("remainingAttempts");
                java.time.LocalDateTime lockUntilTime = (java.time.LocalDateTime) request.getAttribute("lockUntilTime");
                %>

                <% if (error != null) { %>
                    <% if (lockUntilTime != null) { %>
                        <div class="alert alert-lockout">
                            🔒 <%= error %>
                            <br><small>锁定将在30分钟后自动解除</small>
                        </div>
                    <% } else if (failCount != null && failCount > 0) { %>
                        <div class="alert alert-warning">
                            ⚠️ <%= error %>
                            <% if (remainingAttempts != null && remainingAttempts > 0) { %>
                                <br><small>为了账户安全，请仔细检查用户名和密码</small>
                            <% } %>
                        </div>
                    <% } else { %>
                        <div class="alert alert-error">
                            <%= error %>
                        </div>
                    <% } %>
                <% } %>

                <% if (success != null) { %>
                    <div class="alert alert-success">
                        <%= success %>
                    </div>
                <% } %><div class="system-info">
                                        <strong>系统说明：</strong>请输入管理员用户名和密码登录系统
                                    </div>

                                    <form action="<%= request.getContextPath() %>/admin/login" method="post">
                                        <div class="form-group">
                                            <label for="adminUsername">管理员用户名</label>
                                            <input type="text" id="adminUsername" name="adminUsername" class="form-input"
                                                placeholder="请输入管理员用户名" required>
                                        </div>
                                        
                                        <div class="form-group">
                                            <label for="adminPassword">管理员密码</label>
                                            <input type="password" id="adminPassword" name="adminPassword" class="form-input"
                                                placeholder="请输入管理员密码" required>
                                        </div>

                                        <button type="submit" class="login-btn">
                                            登录管理系统
                                        </button>                                    </form>

                                    <a href="<%= request.getContextPath() %>/index.jsp" class="back-link">
                                        ← 返回首页
                                    </a>

                                    <div class="admin-features">
                                        <h3>管理功能</h3>
                                        <ul>
                                            <li>预约审核管理</li>
                                            <li>通行码生成和验证</li>
                                            <li>访客信息管理</li>
                                            <li>系统数据统计</li>
                                            <li>实时监控面板</li>
                                        </ul>
                                    </div>
            </div>
        </div>        <script>
            // Auto-focus on the admin username input
            document.getElementById('adminUsername').focus();

            // Form validation
            document.querySelector('form').addEventListener('submit', function (e) {
                const adminUsername = document.getElementById('adminUsername').value.trim();
                const adminPassword = document.getElementById('adminPassword').value.trim();

                if (!adminUsername) {
                    e.preventDefault();
                    alert('请输入管理员用户名');
                    return;
                }

                if (!adminPassword) {
                    e.preventDefault();
                    alert('请输入管理员密码');
                    return;
                }

                if (adminPassword.length < 6) {
                    e.preventDefault();
                    alert('管理员密码长度不能少于6位');
                    return;
                }
            });

            // Add enter key support for password field
            document.getElementById('adminPassword').addEventListener('keypress', function (e) {
                if (e.key === 'Enter') {
                    document.querySelector('form').submit();
                }
            });
        </script>
    </body>

    </html>