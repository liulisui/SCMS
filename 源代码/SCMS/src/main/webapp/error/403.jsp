<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <!DOCTYPE html>
    <html lang="zh-CN">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>访问被拒绝 - 校园通行码预约管理系统</title>
        <style>
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }

            body {
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                display: flex;
                justify-content: center;
                align-items: center;
                min-height: 100vh;
                color: #333;
                padding: 2rem;
            }

            .error-container {
                background: white;
                border-radius: 20px;
                padding: 3rem 2rem;
                text-align: center;
                box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1);
                max-width: 500px;
                width: 100%;
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

            .error-icon {
                font-size: 5rem;
                margin-bottom: 1.5rem;
                color: #ef4444;
            }

            .error-code {
                font-size: 4rem;
                font-weight: bold;
                color: #dc2626;
                margin-bottom: 1rem;
            }

            .error-title {
                font-size: 1.5rem;
                color: #374151;
                margin-bottom: 1rem;
            }

            .error-message {
                color: #6b7280;
                margin-bottom: 2rem;
                line-height: 1.6;
            }

            .action-buttons {
                display: flex;
                gap: 1rem;
                justify-content: center;
                flex-wrap: wrap;
            }

            .btn {
                padding: 0.75rem 1.5rem;
                border: none;
                border-radius: 8px;
                text-decoration: none;
                font-weight: 500;
                transition: all 0.3s ease;
                cursor: pointer;
            }

            .btn-primary {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
            }

            .btn-primary:hover {
                transform: translateY(-2px);
                box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
            }

            .btn-secondary {
                background: #f3f4f6;
                color: #374151;
                border: 1px solid #d1d5db;
            }

            .btn-secondary:hover {
                background: #e5e7eb;
            }

            .error-details {
                background: #f8fafc;
                border-radius: 8px;
                padding: 1rem;
                margin: 1.5rem 0;
                font-size: 0.9rem;
                color: #64748b;
            }

            @media (max-width: 480px) {
                .error-container {
                    padding: 2rem 1.5rem;
                }

                .error-code {
                    font-size: 3rem;
                }

                .error-title {
                    font-size: 1.25rem;
                }

                .action-buttons {
                    flex-direction: column;
                }
            }
        </style>
    </head>

    <body>
        <div class="error-container">
            <div class="error-icon">🚫</div>
            <div class="error-code">403</div>
            <h1 class="error-title">访问被拒绝</h1>
            <p class="error-message">
                很抱歉，您没有权限访问此页面。这可能是因为：
            </p>

            <div class="error-details">
                <ul style="text-align: left; list-style: none; padding: 0;">
                    <li>• 您没有管理员权限</li>
                    <li>• 会话已过期，需要重新登录</li>
                    <li>• 页面需要特殊权限才能访问</li>
                    <li>• 系统正在维护中</li>
                </ul>
            </div>            <div class="action-buttons">
                <a href="<%= request.getContextPath() %>/index.jsp" class="btn btn-primary">
                    返回首页
                </a>
                <a href="<%= request.getContextPath() %>/admin/login.jsp" class="btn btn-secondary">
                    管理员登录
                </a>
                <button onclick="history.back()" class="btn btn-secondary">
                    返回上页
                </button>
            </div>
        </div>

        <script>
            // Auto redirect after 30 seconds
            setTimeout(function () {
                if (confirm('是否自动返回首页？')) {
                    window.location.href = '<%= request.getContextPath() %>/';
                }
            }, 30000);
        </script>
    </body>

    </html>
    max-width: 500px;
    width: 90%;
    animation: slideIn 0.6s ease-out;
    }

    @keyframes slideIn {
    from {
    opacity: 0;
    transform: translateY(-30px);
    }

    to {
    opacity: 1;
    transform: translateY(0);
    }
    }

    .error-code {
    font-size: 120px;
    font-weight: bold;
    color: #e74c3c;
    margin-bottom: 20px;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
    }

    .error-title {
    font-size: 32px;
    color: #2c3e50;
    margin-bottom: 20px;
    font-weight: 600;
    }

    .error-message {
    font-size: 18px;
    color: #7f8c8d;
    margin-bottom: 40px;
    line-height: 1.6;
    }

    .btn-group {
    display: flex;
    gap: 15px;
    justify-content: center;
    flex-wrap: wrap;
    }

    .btn {
    padding: 15px 30px;
    border: none;
    border-radius: 10px;
    font-size: 16px;
    font-weight: 600;
    text-decoration: none;
    cursor: pointer;
    transition: all 0.3s ease;
    display: inline-block;
    }

    .btn-primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    }

    .btn-primary:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
    }

    .btn-secondary {
    background: #ecf0f1;
    color: #2c3e50;
    }

    .btn-secondary:hover {
    background: #d5dbdb;
    transform: translateY(-2px);
    }

    .icon {
    font-size: 80px;
    color: #e74c3c;
    margin-bottom: 20px;
    }

    @media (max-width: 480px) {
    .error-container {
    padding: 40px 20px;
    }

    .error-code {
    font-size: 80px;
    }

    .error-title {
    font-size: 24px;
    }

    .btn-group {
    flex-direction: column;
    }
    }
    </style>
    </head>

    <body>
        <div class="error-container">
            <div class="icon">🚫</div>
            <div class="error-code">403</div>
            <h1 class="error-title">访问被拒绝</h1>
            <p class="error-message">
                抱歉，您没有权限访问此页面。<br>
                请联系管理员或使用正确的账户登录。
            </p>

            <div class="btn-group">
                <a href="${pageContext.request.contextPath}/login.jsp" class="btn btn-primary">
                    重新登录
                </a>
                <a href="javascript:history.back()" class="btn btn-secondary">
                    返回上页
                </a>
            </div>
        </div>
    </body>

    </html>