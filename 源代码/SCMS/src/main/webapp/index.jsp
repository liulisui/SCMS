<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <!DOCTYPE html>
    <html lang="zh-CN">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>校园通行码预约管理系统</title>
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
            }

            .main-container {
                background: rgba(255, 255, 255, 0.95);
                backdrop-filter: blur(10px);
                border-radius: 20px;
                box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
                padding: 3rem;
                width: 100%;
                max-width: 600px;
                text-align: center;
            }

            .main-container::before {
                content: '';
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                height: 5px;
                background: linear-gradient(90deg, #667eea, #764ba2, #667eea);
                background-size: 200% 100%;
                animation: shimmer 2s infinite;
            }

            @keyframes shimmer {
                0% {
                    background-position: -200% 0;
                }

                100% {
                    background-position: 200% 0;
                }
            }

            .header {
                margin-bottom: 2rem;
            }

            .header h1 {
                color: #333;
                font-size: 2.5rem;
                margin-bottom: 1rem;
            }

            .header p {
                color: #666;
                font-size: 1.1rem;
                line-height: 1.6;
            }

            .actions {
                display: grid;
                gap: 1rem;
                margin-bottom: 2rem;
            }

            .action-btn {
                display: block;
                padding: 1rem 2rem;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                text-decoration: none;
                border-radius: 12px;
                font-size: 1.1rem;
                font-weight: 600;
                transition: all 0.3s ease;
                border: none;
                cursor: pointer;
            }

            .action-btn:hover {
                transform: translateY(-2px);
                box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
            }

            .action-btn.secondary {
                background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
            }

            .action-btn.secondary:hover {
                box-shadow: 0 8px 25px rgba(245, 158, 11, 0.3);
            }

            .admin-link {
                margin-top: 2rem;
                padding-top: 2rem;
                border-top: 1px solid #e2e8f0;
            }

            .admin-btn {
                display: inline-block;
                padding: 0.75rem 1.5rem;
                background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
                color: white;
                text-decoration: none;
                border-radius: 8px;
                font-weight: 600;
                transition: all 0.3s ease;
            }

            .admin-btn:hover {
                transform: translateY(-1px);
                box-shadow: 0 4px 15px rgba(231, 76, 60, 0.3);
            }

            .features {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
                gap: 1rem;
                margin-top: 2rem;
            }

            .feature {
                padding: 1rem;
                background: rgba(102, 126, 234, 0.1);
                border-radius: 8px;
                text-align: center;
            }

            .feature-icon {
                font-size: 2rem;
                margin-bottom: 0.5rem;
            }

            .feature h3 {
                color: #333;
                font-size: 1rem;
                margin-bottom: 0.5rem;
            }

            .feature p {
                color: #666;
                font-size: 0.9rem;
            }

            @media (max-width: 480px) {
                .main-container {
                    margin: 1rem;
                    padding: 2rem;
                }

                .header h1 {
                    font-size: 2rem;
                }

                .features {
                    grid-template-columns: 1fr;
                }
            }
        </style>
    </head>

    <body>
        <div class="main-container">
            <div class="header">
                <h1>🎓 校园通行码系统</h1>
                <p>欢迎使用校园通行码预约管理系统<br>无需注册，即可预约校园参观和公务来访</p>
            </div>

            <div class="actions">
                <a href="${pageContext.request.contextPath}/user/reservation.jsp" class="action-btn">
                    📋 预约校园通行码
                </a>
                <a href="${pageContext.request.contextPath}/user/history.jsp" class="action-btn secondary">
                    📅 查询预约记录
                </a>
            </div>

            <div class="features">
                <div class="feature">
                    <div class="feature-icon">👥</div>
                    <h3>社会公众来访预约</h3>
                    <p>无需登录，直接预约校园参观通行码</p>
                </div>
                <div class="feature">
                    <div class="feature-icon">📅</div>
                    <h3>查询预约</h3>
                    <p>使用姓名、身份证号和手机号查询预约状态</p>
                </div>
                <div class="feature">
                    <div class="feature-icon">📱</div>
                    <h3>二维码通行</h3>
                    <p>获取专属通行二维码，快速进出校园</p>
                </div>
            </div>

            <div class="admin-link">
                <a href="${pageContext.request.contextPath}/admin/login" class="admin-btn">
                    🔒 管理员登录
                </a>
            </div>
        </div>
    </body>

    </html>