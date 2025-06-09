<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
    <!DOCTYPE html>
    <html lang="zh-CN">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>页面未找到 - 校园通行码预约管理系统</title>
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
                color: #f59e0b;
            }

            .error-code {
                font-size: 4rem;
                font-weight: bold;
                color: #d97706;
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

            .popular-links {
                text-align: left;
                margin: 1.5rem 0;
            }

            .popular-links h3 {
                color: #374151;
                margin-bottom: 0.5rem;
                font-size: 1rem;
            }

            .popular-links ul {
                list-style: none;
                padding: 0;
            }

            .popular-links li {
                margin-bottom: 0.5rem;
            }

            .popular-links a {
                color: #667eea;
                text-decoration: none;
                font-size: 0.9rem;
            }

            .popular-links a:hover {
                text-decoration: underline;
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
            <div class="error-icon">🔍</div>
            <div class="error-code">404</div>
            <h1 class="error-title">页面未找到</h1>
            <p class="error-message">
                抱歉，您访问的页面不存在或已被移动。请检查URL是否正确，或尝试以下操作：
            </p>

            <div class="error-details">
                <p><strong>请求URL:</strong>
                    <%= request.getRequestURL() %>
                </p>
            </div>

            <div class="popular-links">
                <h3>常用链接</h3>
                <ul>
                    <li><a href="<%= request.getContextPath() %>/">首页</a></li>
                    <li><a href="<%= request.getContextPath() %>/user/reservation.jsp">预约通行码</a></li>
                    <li><a href="<%= request.getContextPath() %>/user/passcode.jsp">查看通行码</a></li>
                    <li><a href="<%= request.getContextPath() %>/admin/login.jsp">管理员登录</a></li>
                </ul>
            </div>            <div class="action-buttons">
                <a href="<%= request.getContextPath() %>/index.jsp" class="btn btn-primary">
                    返回首页
                </a>
                <button onclick="history.back()" class="btn btn-secondary">
                    返回上页
                </button>
                <button onclick="location.reload()" class="btn btn-secondary">
                    重新加载
                </button>
            </div>
        </div>

        <script>
            // Track 404 errors (optional analytics)
            console.warn('404 Error - Page not found:', window.location.href);

            // Auto redirect after 60 seconds
            setTimeout(function () {
                if (confirm('页面长时间未找到，是否返回首页？')) {
                    window.location.href = '<%= request.getContextPath() %>/';
                }
            }, 60000);
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
    color: #f39c12;
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
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    }

    .btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
    }

    .icon {
    font-size: 80px;
    color: #f39c12;
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
    }
    </style>
    </head>

    <body>
        <div class="error-container">
            <div class="icon">🔍</div>
            <div class="error-code">404</div>
            <h1 class="error-title">页面未找到</h1>
            <p class="error-message">
                抱歉，您访问的页面不存在。<br>
                请检查URL是否正确，或返回首页。
            </p>            <a href="${pageContext.request.contextPath}/index.jsp" class="btn">
                返回首页
            </a>
        </div>
    </body>

    </html>