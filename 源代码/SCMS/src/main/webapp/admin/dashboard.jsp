<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.time.LocalDateTime" %>

<%
    // Get data from request attributes
    List<Map<String, Object>> pendingReservations = (List<Map<String, Object>>) request.getAttribute("pendingReservations");
    List<Map<String, Object>> todayReservations = (List<Map<String, Object>>) request.getAttribute("todayReservations");
    List<Map<String, Object>> recentActivity = (List<Map<String, Object>>) request.getAttribute("recentActivity");
    
    Integer totalReservations = (Integer) request.getAttribute("totalReservations");
    Integer todayCount = (Integer) request.getAttribute("todayCount");
    Integer pendingCount = (Integer) request.getAttribute("pendingCount");
    Integer approvedCount = (Integer) request.getAttribute("approvedCount");
    
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    // Set defaults if null
    if (totalReservations == null) totalReservations = 0;
    if (todayCount == null) todayCount = 0;
    if (pendingCount == null) pendingCount = 0;
    if (approvedCount == null) approvedCount = 0;
%>

<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>管理员仪表板 - 校园通行码预约管理系统</title>
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
            max-width: 1400px;
            margin: 0 auto;
            padding: 0 2rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .header h1 {
            font-size: 1.5rem;
        }

        .admin-info {
            display: flex;
            align-items: center;
            gap: 1rem;
        }

        .logout-btn {
            background: rgba(255, 255, 255, 0.2);
            color: white;
            border: none;
            padding: 0.5rem 1rem;
            border-radius: 5px;
            text-decoration: none;
            transition: background 0.3s;
        }

        .logout-btn:hover {
            background: rgba(255, 255, 255, 0.3);
        }

        .main-container {
            max-width: 1400px;
            margin: 0 auto;
            padding: 2rem;
        }

        .dashboard-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 2rem;
            margin-bottom: 2rem;
        }

        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1rem;
            margin-bottom: 2rem;
        }

        .stat-card {
            background: white;
            border-radius: 8px;
            padding: 1.5rem;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            text-align: center;
            transition: transform 0.3s, box-shadow 0.3s;
        }

        .stat-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
        }

        .stat-number {
            font-size: 2rem;
            font-weight: bold;
            color: #667eea;
            margin-bottom: 0.5rem;
        }        .stat-label {
            color: #666;
            font-size: 0.9rem;
        }

        .quick-actions-section {
            margin-bottom: 2rem;
        }

        .quick-actions-section .section-title {
            font-size: 1.5rem;
            font-weight: 600;
            color: #2d3748;
            margin-bottom: 1.5rem;
            text-align: center;
        }        .quick-actions-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 1.5rem;
            margin-bottom: 2rem;
        }

        .action-card {
            background: white;
            border-radius: 12px;
            padding: 2rem;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
            border: 1px solid #e2e8f0;
            text-decoration: none;
            color: inherit;
            transition: all 0.3s ease;
            text-align: center;
            cursor: pointer;
        }

        .action-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
            border-color: #667eea;
        }

        .action-icon {
            font-size: 2.5rem;
            margin-bottom: 1rem;
            display: block;
        }

        .action-title {
            font-size: 1.1rem;
            font-weight: 600;
            color: #2d3748;
            margin-bottom: 0.5rem;
        }

        .action-desc {
            font-size: 0.9rem;
            color: #718096;
            line-height: 1.4;
        }        .dashboard-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 2rem;
            margin-bottom: 2rem;
        }

        .section-card {
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            overflow: hidden;
        }

        .section-header {
            background: #f8fafc;
            padding: 1.5rem;
            border-bottom: 1px solid #e2e8f0;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .section-title {
            font-size: 1.2rem;
            font-weight: 600;
            color: #2d3748;
        }

        .refresh-btn {
            background: #667eea;
            color: white;
            border: none;
            padding: 0.5rem 1rem;
            border-radius: 5px;
            cursor: pointer;
            font-size: 0.9rem;
            transition: background 0.3s;
        }

        .refresh-btn:hover {
            background: #5a67d8;
        }

        .section-content {
            padding: 1.5rem;
        }

        .reservation-list {
            max-height: 400px;
            overflow-y: auto;
        }

        .reservation-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 1rem;
            border: 1px solid #e2e8f0;
            border-radius: 6px;
            margin-bottom: 0.5rem;
            background: #fafafa;
            transition: background 0.3s;
        }

        .reservation-item:hover {
            background: #f1f5f9;
        }

        .reservation-info {
            flex: 1;
        }

        .visitor-name {
            font-weight: 600;
            color: #2d3748;
            margin-bottom: 0.25rem;
        }

        .reservation-details {
            font-size: 0.9rem;
            color: #666;
        }

        .reservation-actions {
            display: flex;
            gap: 0.5rem;
        }

        .btn {
            padding: 0.5rem 1rem;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 0.9rem;
            transition: background 0.3s;
        }

        .btn-approve {
            background: #48bb78;
            color: white;
        }

        .btn-approve:hover {
            background: #38a169;
        }

        .btn-reject {
            background: #f56565;
            color: white;
        }

        .btn-reject:hover {
            background: #e53e3e;
        }

        .status-badge {
            padding: 0.25rem 0.75rem;
            border-radius: 12px;
            font-size: 0.8rem;
            font-weight: 500;
        }

        .status-pending {
            background: #fed7aa;
            color: #c2410c;
        }

        .status-approved {
            background: #bbf7d0;
            color: #059669;
        }

        .status-rejected {
            background: #fecaca;
            color: #dc2626;
        }

        .empty-state {
            text-align: center;
            padding: 2rem;
            color: #666;
        }        .auto-refresh-indicator {
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            font-size: 0.8rem;
            color: #ffffff;
            font-weight: 500;
            margin-left: 1rem;
        }.refresh-dot {
            width: 8px;
            height: 8px;
            background: #48bb78;
            border-radius: 50%;
            animation: pulse 2s infinite;
        }

        @keyframes pulse {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.5; }
        }        @media (max-width: 768px) {
            .header-content {
                flex-direction: column;
                gap: 1rem;
                text-align: center;
            }

            .main-container {
                padding: 1rem;
            }

            .dashboard-grid {
                grid-template-columns: 1fr;
            }

            .stats-grid {
                grid-template-columns: repeat(2, 1fr);
            }            .quick-actions-grid {
                grid-template-columns: 1fr;
                gap: 1rem;
            }

            .action-card {
                padding: 1.5rem;
            }

            .action-icon {
                font-size: 2rem;
            }

            .reservation-item {
                flex-direction: column;
                align-items: flex-start;
                gap: 1rem;
            }            .reservation-actions {
                width: 100%;
                justify-content: center;
            }
        }
    </style>
</head>
<body>
    <!-- Header -->
    <header class="header">
        <div class="header-content">
            <h1>管理员仪表板</h1>            <div class="admin-info">
                <span>管理员已登录</span>
                <span class="auto-refresh-indicator">
                    <span class="refresh-dot"></span>
                    自动刷新
                </span>
                <a href="${pageContext.request.contextPath}/index.jsp" class="logout-btn" onclick="return confirmLogout()">退出登录</a>
            </div>
        </div>
    </header>

    <!-- Main Content -->
    <main class="main-container">        <!-- Statistics Cards -->
                <!-- Quick Actions Navigation -->
        <div class="quick-actions-section">
            <h2 class="section-title">管理功能</h2>
            <div class="quick-actions-grid">                <a href="${pageContext.request.contextPath}/admin/reservation-query" class="action-card">
                    <div class="action-icon">🔍</div>
                    <div class="action-title">预约管理</div>
                    <div class="action-desc">综合查询和管理预约记录</div>
                </a>
                <a href="${pageContext.request.contextPath}/admin/department" class="action-card">
                    <div class="action-icon">🏢</div>
                    <div class="action-title">部门管理</div>
                    <div class="action-desc">管理系统部门信息</div>
                </a>
                <a href="${pageContext.request.contextPath}/admin/administrator" class="action-card">
                    <div class="action-icon">👨‍💼</div>
                    <div class="action-title">管理员管理</div>
                    <div class="action-desc">管理部门管理员账户</div>
                </a>

            </div>
        </div>

        <!-- Dashboard Grid -->
            <!-- Pending Reservations -->
            

            

            
        </div>
    </main>

    <script>
        // Auto refresh every 30 seconds
        setInterval(function() {
            location.reload();
        }, 30000);

        // Refresh specific section
        function refreshSection(section) {
            // Show loading state
            const refreshBtn = event.target;
            const originalText = refreshBtn.textContent;
            refreshBtn.textContent = '刷新中...';
            refreshBtn.disabled = true;

            // Simulate refresh (in real implementation, this would be an AJAX call)
            setTimeout(function() {
                location.reload();
            }, 1000);
        }        // Approve reservation
        function approveReservation(reservationId) {
            if (confirm('确认通过此预约？')) {
                // 显示加载状态
                const button = document.querySelector(`[data-reservation-id="${reservationId}"].btn-approve`);
                const originalText = button.textContent;
                button.textContent = '处理中...';
                button.disabled = true;
                
                // 使用 AJAX 发送审批请求
                fetch('${pageContext.request.contextPath}/admin/reservation-approval', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: new URLSearchParams({
                        'reservationId': reservationId,
                        'action': 'approve',
                        'reservationType': 'official'
                    })
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert('预约审批成功！');
                        location.reload(); // 刷新页面显示最新状态
                    } else {
                        alert('审批失败: ' + data.message);
                        button.textContent = originalText;
                        button.disabled = false;
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('网络错误，请稍后重试');
                    button.textContent = originalText;
                    button.disabled = false;
                });
            }
        }        // Reject reservation
        function rejectReservation(reservationId) {
            if (confirm('确认拒绝此预约？')) {
                // 显示加载状态
                const button = document.querySelector(`[data-reservation-id="${reservationId}"].btn-reject`);
                const originalText = button.textContent;
                button.textContent = '处理中...';
                button.disabled = true;
                
                // 使用 AJAX 发送审批请求
                fetch('${pageContext.request.contextPath}/admin/reservation-approval', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: new URLSearchParams({
                        'reservationId': reservationId,
                        'action': 'reject',
                        'reservationType': 'official'
                    })
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert('预约已拒绝！');
                        location.reload(); // 刷新页面显示最新状态
                    } else {
                        alert('操作失败: ' + data.message);
                        button.textContent = originalText;
                        button.disabled = false;
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('网络错误，请稍后重试');
                    button.textContent = originalText;
                    button.disabled = false;
                });
            }
        }// Logout confirmation
        function confirmLogout() {
            return confirm('确认退出登录？这将清除您的会话信息。');
        }

        // Keyboard shortcuts
        document.addEventListener('keydown', function(e) {
            if (e.ctrlKey && e.key === 'r') {
                e.preventDefault();
                location.reload();
            }
        });

        // Show loading states for better UX
        document.addEventListener('DOMContentLoaded', function() {
            // Add subtle animations to cards
            const cards = document.querySelectorAll('.stat-card, .section-card');
            cards.forEach((card, index) => {
                card.style.animationDelay = (index * 0.1) + 's';
                card.style.animation = 'slideIn 0.5s ease-out forwards';
            });
        });

        // CSS Animation for loading
        const style = document.createElement('style');
        style.textContent = `
            @keyframes slideIn {
                from {
                    opacity: 0;
                    transform: translateY(20px);
                }
                to {
                    opacity: 1;
                    transform: translateY(0);
                }
            }
        `;
        document.head.appendChild(style);
    </script>
</body>
</html>