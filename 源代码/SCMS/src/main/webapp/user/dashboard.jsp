<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <!DOCTYPE html>
    <html lang="zh-CN">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>用户仪表板 - 校园通行码预约管理系统</title>
        <s <div class="action-buttons">
            <a href="${pageContext.request.contextPath}/user/reservation.jsp?type=public" class="action-btn">
                👥 校园参观预约
            </a>
            <a href="${pageContext.request.contextPath}/user/reservation.jsp?type=official"
                class="action-btn secondary">
                💼 公务来访预约
            </a>
            <a href="${pageContext.request.contextPath}/user/history.jsp" class="action-btn success">
                📅 查询预约记录
            </a>
            </div> * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
            }

            body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f8fafc;
            color: #333;
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
            }

            .header-nav {
            display: flex;
            align-items: center;
            gap: 1rem;
            }

            .nav-btn {
            background: rgba(255, 255, 255, 0.2);
            color: white;
            border: none;
            padding: 0.5rem 1rem;
            border-radius: 5px;
            cursor: pointer;
            text-decoration: none;
            transition: background 0.3s;
            }

            .nav-btn:hover {
            background: rgba(255, 255, 255, 0.3);
            }

            .container {
            max-width: 1200px;
            margin: 2rem auto;
            padding: 0 2rem;
            }

            .welcome-section {
            background: white;
            border-radius: 12px;
            padding: 2rem;
            margin-bottom: 2rem;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
            border: 1px solid #e2e8f0;
            text-align: center;
            }

            .welcome-section h2 {
            color: #1a202c;
            font-size: 1.75rem;
            margin-bottom: 1rem;
            }

            .welcome-section p {
            color: #64748b;
            font-size: 1.1rem;
            margin-bottom: 2rem;
            }

            .dashboard-grid {
            display: grid;
            grid-template-columns: 1fr 2fr;
            gap: 2rem;
            margin-bottom: 2rem;
            }

            .card {
            background: white;
            border-radius: 12px;
            padding: 1.5rem;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
            border: 1px solid #e2e8f0;
            }

            .card h2 {
            color: #1a202c;
            margin-bottom: 1rem;
            font-size: 1.25rem;
            }

            .quick-actions {
            display: flex;
            flex-direction: column;
            gap: 1rem;
            }

            .action-btn {
            display: block;
            padding: 1rem;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            text-decoration: none;
            border-radius: 8px;
            text-align: center;
            font-weight: 500;
            transition: transform 0.2s, box-shadow 0.2s;
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

            .action-btn.success {
            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
            }

            .action-btn.success:hover {
            box-shadow: 0 8px 25px rgba(16, 185, 129, 0.3);
            }

            .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            gap: 1rem;
            }

            .stat-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 1.5rem;
            border-radius: 12px;
            text-align: center;
            }

            .stat-number {
            font-size: 2rem;
            font-weight: bold;
            margin-bottom: 0.5rem;
            }

            .stat-label {
            font-size: 0.9rem;
            opacity: 0.9;
            }

            .recent-section {
            margin-top: 2rem;
            }

            .recent-reservations {
            background: white;
            border-radius: 12px;
            padding: 1.5rem;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
            border: 1px solid #e2e8f0;
            }

            .reservation-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 1rem;
            border-bottom: 1px solid #f1f5f9;
            }

            .reservation-item:last-child {
            border-bottom: none;
            }

            .reservation-info h4 {
            color: #1a202c;
            margin-bottom: 0.25rem;
            }

            .reservation-info p {
            color: #64748b;
            font-size: 0.9rem;
            }

            .status-badge {
            padding: 0.25rem 0.75rem;
            border-radius: 20px;
            font-size: 0.875rem;
            font-weight: 500;
            }

            .status-pending {
            background: #fef3c7;
            color: #92400e;
            }

            .status-approved {
            background: #d1fae5;
            color: #065f46;
            }

            .status-rejected {
            background: #fee2e2;
            color: #991b1b;
            }

            .empty-state {
            text-align: center;
            padding: 3rem;
            color: #64748b;
            }

            .empty-state .icon {
            font-size: 3rem;
            margin-bottom: 1rem;
            }

            @media (max-width: 768px) {
            .dashboard-grid {
            grid-template-columns: 1fr;
            }

            .stats-grid {
            grid-template-columns: repeat(2, 1fr);
            }

            .container {
            padding: 0 1rem;
            }

            .header-content {
            padding: 0 1rem;
            flex-direction: column;
            gap: 1rem;
            text-align: center;
            }

            .header-nav {
            justify-content: center;
            }
            }
            </style>
    </head>

    <body>
        <% // 获取统计数据 Integer totalReservations=(Integer) request.getAttribute("totalReservations"); Integer
            publicReservations=(Integer) request.getAttribute("publicReservations"); Integer
            officialReservations=(Integer) request.getAttribute("officialReservations"); Integer
            pendingReservations=(Integer) request.getAttribute("pendingReservations"); Integer
            approvedReservations=(Integer) request.getAttribute("approvedReservations"); java.util.List
            recentReservations=(java.util.List) request.getAttribute("recentReservations"); // 默认值 if
            (totalReservations==null) totalReservations=0; if (publicReservations==null) publicReservations=0; if
            (officialReservations==null) officialReservations=0; if (pendingReservations==null) pendingReservations=0;
            if (approvedReservations==null) approvedReservations=0; %>

            <header class="header">
                <div class="header-content">
                    <h1>🎓 校园通行码系统</h1>
                    <div class="header-nav">
                        <a href="${pageContext.request.contextPath}/" class="nav-btn">🏠 首页</a>
                        <a href="${pageContext.request.contextPath}/user/history.jsp" class="nav-btn">📋 查询记录</a>
                    </div>
                </div>
            </header>

            <div class="container">
                <div class="welcome-section">
                    <h2>欢迎使用校园通行码预约系统</h2>
                    <p>无需注册，快速预约校园参观和公务来访通行码</p>
                </div>

                <div class="dashboard-grid">
                    <div class="card">
                        <h2>快速操作</h2>
                        <div class="quick-actions">
                            <a href="${pageContext.request.contextPath}/reservation?type=public" class="action-btn">
                                👥 校园参观预约
                            </a>
                            <a href="${pageContext.request.contextPath}/reservation?type=official"
                                class="action-btn secondary">
                                💼 公务来访预约
                            </a>
                            <a href="${pageContext.request.contextPath}/user/history" class="action-btn success">
                                📅 查询预约记录
                            </a>
                        </div>
                    </div>

                    <div class="card">
                        <h2>系统统计</h2>
                        <div class="stats-grid">
                            <div class="stat-card">
                                <div class="stat-number">
                                    <%= totalReservations %>
                                </div>
                                <div class="stat-label">总预约数</div>
                            </div>
                            <div class="stat-card"
                                style="background: linear-gradient(135deg, #10b981 0%, #059669 100%);">
                                <div class="stat-number">
                                    <%= publicReservations %>
                                </div>
                                <div class="stat-label">校园参观</div>
                            </div>
                            <div class="stat-card"
                                style="background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);">
                                <div class="stat-number">
                                    <%= officialReservations %>
                                </div>
                                <div class="stat-label">公务来访</div>
                            </div>
                            <div class="stat-card"
                                style="background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);">
                                <div class="stat-number">
                                    <%= pendingReservations %>
                                </div>
                                <div class="stat-label">待审核</div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="recent-section">
                    <div class="recent-reservations">
                        <h2>最近预约记录</h2>
                        <% if (recentReservations !=null && !recentReservations.isEmpty()) { %>
                            <% for (Object reservationObj : recentReservations) { // 这里需要根据实际的Reservation对象结构来处理 //
                                目前先显示占位符，实际开发中需要调整 %>
                                <div class="reservation-item">
                                    <div class="reservation-info">
                                        <h4>预约记录</h4>
                                        <p>预约时间：最近</p>
                                    </div>
                                    <span class="status-badge status-pending">待审核</span>
                                </div>
                                <% } %>
                                    <% } else { %>
                                        <div class="empty-state">
                                            <div class="icon">📝</div>
                                            <h3>暂无预约记录</h3>
                                            <p>开始您的第一次预约吧！</p>
                                        </div>
                                        <% } %>
                    </div>
                </div>
            </div>

            <script>
                // 定期刷新统计数据
                function refreshStats() {
                    fetch('${pageContext.request.contextPath}/user/dashboard', {
                        method: 'GET',
                        headers: {
                            'X-Requested-With': 'XMLHttpRequest'
                        }
                    })
                        .then(response => response.text())
                        .then(data => {
                            // 可以在这里处理返回的数据来更新统计信息
                            console.log('Stats refreshed');
                        })
                        .catch(error => {
                            console.error('Error refreshing stats:', error);
                        });
                }

                // 每30秒刷新一次统计数据
                setInterval(refreshStats, 30000);
            </script>
    </body>

    </html>
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    text-decoration: none;
    border-radius: 8px;
    text-align: center;
    font-weight: 500;
    transition: transform 0.2s, box-shadow 0.2s;
    }

    .action-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(102, 126, 234, 0.3);
    }

    .reservations-section {
    margin-top: 2rem;
    }

    .reservations-grid {
    display: grid;
    gap: 1rem;
    grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
    }

    .reservation-card {
    background: white;
    border-radius: 12px;
    padding: 1.5rem;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
    border: 1px solid #e2e8f0;
    position: relative;
    overflow: hidden;
    }

    .reservation-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    }

    .reservation-card.pending::before {
    background: #fbbf24;
    }

    .reservation-card.approved::before {
    background: #10b981;
    }

    .reservation-card.rejected::before {
    background: #ef4444;
    }

    .reservation-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
    }

    .reservation-id {
    font-weight: 600;
    color: #667eea;
    }

    .status-badge {
    padding: 0.25rem 0.75rem;
    border-radius: 20px;
    font-size: 0.875rem;
    font-weight: 500;
    }

    .status-pending {
    background: #fef3c7;
    color: #92400e;
    }

    .status-approved {
    background: #d1fae5;
    color: #065f46;
    }

    .status-rejected {
    background: #fee2e2;
    color: #991b1b;
    }

    .reservation-info {
    display: grid;
    gap: 0.5rem;
    margin-bottom: 1rem;
    }

    .info-item {
    display: flex;
    justify-content: space-between;
    padding: 0.5rem 0;
    border-bottom: 1px solid #f1f5f9;
    }

    .info-label {
    color: #64748b;
    font-weight: 500;
    }

    .info-value {
    color: #1e293b;
    }

    .passcode-btn {
    width: 100%;
    padding: 0.75rem;
    background: #10b981;
    color: white;
    border: none;
    border-radius: 8px;
    text-decoration: none;
    display: inline-block;
    text-align: center;
    font-weight: 500;
    transition: background 0.3s;
    }

    .passcode-btn:hover {
    background: #059669;
    }

    .passcode-btn:disabled {
    background: #9ca3af;
    cursor: not-allowed;
    }

    .empty-state {
    text-align: center;
    padding: 3rem;
    color: #64748b;
    }

    .empty-state img {
    width: 100px;
    height: 100px;
    margin-bottom: 1rem;
    opacity: 0.5;
    }

    @media (max-width: 768px) {
    .dashboard-grid {
    grid-template-columns: 1fr;
    }

    .reservations-grid {
    grid-template-columns: 1fr;
    }

    .container {
    padding: 0 1rem;
    }

    .header-content {
    padding: 0 1rem;
    flex-direction: column;
    gap: 1rem;
    text-align: center;
    }
    }
    </style>
    </head>

    <body>
        <% User user=(User) session.getAttribute("user"); List<Reservation> reservations = (List
            <Reservation>) request.getAttribute("reservations");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                %>

                <header class="header">
                    <div class="header-content">
                        <h1>校园通行码预约管理系统</h1>
                        <div class="user-info">
                            <span>欢迎，<%= user.getRealName() %></span>
                            <a href="${pageContext.request.contextPath}/logout" class="logout-btn">退出登录</a>
                        </div>
                    </div>
                </header>

                <div class="container">
                    <div class="dashboard-grid">
                        <div class="card">
                            <h2>快速操作</h2>
                            <div class="quick-actions"> <a href="${pageContext.request.contextPath}/user/reservation"
                                    class="action-btn">
                                    📋 新建预约申请
                                </a> <a href="${pageContext.request.contextPath}/user/history" class="action-btn"
                                    style="background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);">
                                    📅 我的预约
                                </a>
                                <a href="${pageContext.request.contextPath}/user/dashboard" class="action-btn"
                                    style="background: linear-gradient(135deg, #10b981 0%, #059669 100%);">
                                    🔄 刷新预约状态
                                </a>
                            </div>
                        </div>

                        <div class="card">
                            <h2>预约统计</h2>
                            <div class="reservation-info">
                                <div class="info-item">
                                    <span class="info-label">总预约数</span>
                                    <span class="info-value">
                                        <%= reservations !=null ? reservations.size() : 0 %>
                                    </span>
                                </div>
                                <div class="info-item">
                                    <span class="info-label">待审核</span>
                                    <span class="info-value">
                                        <%= reservations !=null ? reservations.stream().mapToInt(r ->
                                            "pending".equals(r.getStatus()) ? 1 : 0).sum() : 0 %>
                                    </span>
                                </div>
                                <div class="info-item">
                                    <span class="info-label">已通过</span>
                                    <span class="info-value">
                                        <%= reservations !=null ? reservations.stream().mapToInt(r ->
                                            "approved".equals(r.getStatus()) ? 1 : 0).sum() : 0 %>
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
    </body>

    </html>