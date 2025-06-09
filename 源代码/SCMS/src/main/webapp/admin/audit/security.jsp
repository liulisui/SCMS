<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="org.example.scms.model.User" %>
<%
    User admin = (User) session.getAttribute("admin");
    if (admin == null || !"审计管理员".equals(admin.getRole())) {
        response.sendRedirect("/SCMS/admin/login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>安全监控 - SCMS审计管理</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        .security-card {
            transition: all 0.3s ease;
            border-left: 4px solid transparent;
        }
        .security-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
        }
        .security-high { border-left-color: #dc3545; }
        .security-medium { border-left-color: #ffc107; }
        .security-low { border-left-color: #28a745; }
        .security-info { border-left-color: #17a2b8; }
        
        .security-score {
            font-size: 2.5rem;
            font-weight: bold;
        }
        .score-excellent { color: #28a745; }
        .score-good { color: #ffc107; }
        .score-poor { color: #dc3545; }
        
        .threat-indicator {
            width: 12px;
            height: 12px;
            border-radius: 50%;
            display: inline-block;
            margin-right: 8px;
        }
        .threat-high { background-color: #dc3545; }
        .threat-medium { background-color: #ffc107; }
        .threat-low { background-color: #28a745; }
        
        .realtime-update {
            animation: pulse 2s infinite;
        }
        
        @keyframes pulse {
            0% { opacity: 1; }
            50% { opacity: 0.5; }
            100% { opacity: 1; }
        }
        
        .chart-container {
            position: relative;
            height: 300px;
        }
    </style>
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
        <div class="container-fluid">
            <a class="navbar-brand" href="#">
                <i class="bi bi-shield-check"></i> SCMS 安全监控
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav me-auto">
                    <li class="nav-item">
                        <a class="nav-link" href="dashboard.jsp">
                            <i class="bi bi-speedometer2"></i> 仪表板
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="audit-logs.jsp">
                            <i class="bi bi-list-ul"></i> 审计日志
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="reports.jsp">
                            <i class="bi bi-bar-chart"></i> 审计报告
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link active" href="security.jsp">
                            <i class="bi bi-shield-check"></i> 安全监控
                        </a>
                    </li>
                </ul>
                <ul class="navbar-nav">
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" id="navbarDropdown" role="button" data-bs-toggle="dropdown">
                            <i class="bi bi-person-circle"></i> <%= admin.getUsername() %>
                        </a>
                        <ul class="dropdown-menu">
                            <li><a class="dropdown-item" href="/SCMS/admin/logout">
                                <i class="bi bi-box-arrow-right"></i> 退出登录
                            </a></li>
                        </ul>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

    <div class="container-fluid mt-4">
        <!-- 安全概览 -->
        <div class="row mb-4">
            <div class="col-12">
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h2><i class="bi bi-shield-check"></i> 安全监控中心</h2>
                    <div>
                        <span class="badge bg-success realtime-update">实时监控中</span>
                        <button class="btn btn-outline-primary btn-sm" onclick="refreshData()">
                            <i class="bi bi-arrow-clockwise"></i> 刷新
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 安全评分和状态 -->
        <div class="row mb-4">
            <div class="col-lg-3 col-md-6 mb-3">
                <div class="card security-card">
                    <div class="card-body text-center">
                        <h5 class="card-title">安全评分</h5>
                        <div class="security-score" id="securityScore">--</div>
                        <small class="text-muted">综合安全指数</small>
                    </div>
                </div>
            </div>
            <div class="col-lg-3 col-md-6 mb-3">
                <div class="card security-card security-info">
                    <div class="card-body">
                        <h5 class="card-title">
                            <i class="bi bi-exclamation-triangle text-warning"></i> 安全事件
                        </h5>
                        <h3 id="securityEvents">--</h3>
                        <small class="text-muted">24小时内检测到</small>
                    </div>
                </div>
            </div>
            <div class="col-lg-3 col-md-6 mb-3">
                <div class="card security-card security-medium">
                    <div class="card-body">
                        <h5 class="card-title">
                            <i class="bi bi-geo-alt text-danger"></i> 威胁IP
                        </h5>
                        <h3 id="threatIPs">--</h3>
                        <small class="text-muted">需要关注的IP地址</small>
                    </div>
                </div>
            </div>
            <div class="col-lg-3 col-md-6 mb-3">
                <div class="card security-card security-high">
                    <div class="card-body">
                        <h5 class="card-title">
                            <i class="bi bi-shield-x text-danger"></i> 高危活动
                        </h5>
                        <h3 id="highRiskActivities">--</h3>
                        <small class="text-muted">需要立即处理</small>
                    </div>
                </div>
            </div>
        </div>

        <!-- 快速导航 -->
        <div class="row mb-4">
            <div class="col-12">
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">快速导航</h5>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-lg-3 col-md-6 mb-3">
                                <a href="security-alerts.jsp" class="btn btn-outline-danger w-100">
                                    <i class="bi bi-bell"></i> 安全警报
                                    <span class="badge bg-danger ms-2" id="alertCount">0</span>
                                </a>
                            </div>
                            <div class="col-lg-3 col-md-6 mb-3">
                                <a href="threat-analysis.jsp" class="btn btn-outline-warning w-100">
                                    <i class="bi bi-search"></i> 威胁分析
                                </a>
                            </div>
                            <div class="col-lg-3 col-md-6 mb-3">
                                <a href="realtime-monitor.jsp" class="btn btn-outline-info w-100">
                                    <i class="bi bi-activity"></i> 实时监控
                                </a>
                            </div>
                            <div class="col-lg-3 col-md-6 mb-3">
                                <button class="btn btn-outline-secondary w-100" onclick="exportSecurityReport()">
                                    <i class="bi bi-download"></i> 导出报告
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 安全趋势图表 -->
        <div class="row mb-4">
            <div class="col-lg-6 mb-3">
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">安全事件趋势</h5>
                    </div>
                    <div class="card-body">
                        <div class="chart-container">
                            <canvas id="securityTrendChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-lg-6 mb-3">
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">威胁类型分布</h5>
                    </div>
                    <div class="card-body">
                        <div class="chart-container">
                            <canvas id="threatTypeChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 最新安全事件 -->
        <div class="row">
            <div class="col-12">
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h5 class="mb-0">最新安全事件</h5>
                        <a href="security-alerts.jsp" class="btn btn-sm btn-outline-primary">查看全部</a>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-hover">
                                <thead>
                                    <tr>
                                        <th>威胁等级</th>
                                        <th>事件类型</th>
                                        <th>描述</th>
                                        <th>IP地址</th>
                                        <th>时间</th>
                                        <th>状态</th>
                                    </tr>
                                </thead>
                                <tbody id="securityEventsTable">
                                    <tr>
                                        <td colspan="6" class="text-center text-muted">
                                            <i class="bi bi-hourglass-split"></i> 正在加载安全事件...
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        let securityTrendChart;
        let threatTypeChart;

        // 页面加载时初始化
        document.addEventListener('DOMContentLoaded', function() {
            loadSecurityStatus();
            initCharts();
            
            // 设置自动刷新
            setInterval(loadSecurityStatus, 30000); // 30秒刷新一次
        });

        // 加载安全状态
        async function loadSecurityStatus() {
            try {
                const response = await fetch('/SCMS/admin/audit/security/status');
                const data = await response.json();
                
                updateSecurityScore(data.securityScore);
                updateSecurityMetrics(data);
                updateSecurityEvents(data.recentEvents);
                updateCharts(data);
            } catch (error) {
                console.error('加载安全状态失败:', error);
            }
        }

        // 更新安全评分
        function updateSecurityScore(score) {
            const scoreElement = document.getElementById('securityScore');
            scoreElement.textContent = score || '--';
            
            // 根据评分设置颜色
            scoreElement.className = 'security-score';
            if (score >= 80) {
                scoreElement.classList.add('score-excellent');
            } else if (score >= 60) {
                scoreElement.classList.add('score-good');
            } else {
                scoreElement.classList.add('score-poor');
            }
        }

        // 更新安全指标
        function updateSecurityMetrics(data) {
            document.getElementById('securityEvents').textContent = data.securityEventCount || 0;
            document.getElementById('threatIPs').textContent = data.threatIPCount || 0;
            document.getElementById('highRiskActivities').textContent = data.highRiskCount || 0;
            document.getElementById('alertCount').textContent = data.alertCount || 0;
        }

        // 更新安全事件表格
        function updateSecurityEvents(events) {
            const tbody = document.getElementById('securityEventsTable');
            
            if (!events || events.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="6" class="text-center text-muted">
                            <i class="bi bi-check-circle"></i> 暂无安全事件
                        </td>
                    </tr>
                `;
                return;
            }

            tbody.innerHTML = events.map(event => `
                <tr>
                    <td>
                        <span class="threat-indicator threat-${event.severity.toLowerCase()}"></span>
                        ${event.severity}
                    </td>
                    <td>${event.type}</td>
                    <td>${event.description}</td>
                    <td><code>${event.ipAddress}</code></td>
                    <td>${formatDate(event.timestamp)}</td>
                    <td>
                        <span class="badge bg-${event.status === 'RESOLVED' ? 'success' : 'warning'}">
                            ${event.status === 'RESOLVED' ? '已处理' : '待处理'}
                        </span>
                    </td>
                </tr>
            `).join('');
        }

        // 初始化图表
        function initCharts() {
            // 安全事件趋势图
            const trendCtx = document.getElementById('securityTrendChart').getContext('2d');
            securityTrendChart = new Chart(trendCtx, {
                type: 'line',
                data: {
                    labels: [],
                    datasets: [{
                        label: '安全事件数量',
                        data: [],
                        borderColor: 'rgb(255, 99, 132)',
                        backgroundColor: 'rgba(255, 99, 132, 0.2)',
                        tension: 0.1
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        y: {
                            beginAtZero: true
                        }
                    }
                }
            });

            // 威胁类型分布图
            const typeCtx = document.getElementById('threatTypeChart').getContext('2d');
            threatTypeChart = new Chart(typeCtx, {
                type: 'doughnut',
                data: {
                    labels: [],
                    datasets: [{
                        data: [],
                        backgroundColor: [
                            '#ff6384',
                            '#36a2eb',
                            '#cc65fe',
                            '#ffce56',
                            '#4bc0c0'
                        ]
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false
                }
            });
        }

        // 更新图表
        function updateCharts(data) {
            if (data.trendData) {
                securityTrendChart.data.labels = data.trendData.labels;
                securityTrendChart.data.datasets[0].data = data.trendData.values;
                securityTrendChart.update();
            }

            if (data.threatTypeData) {
                threatTypeChart.data.labels = data.threatTypeData.labels;
                threatTypeChart.data.datasets[0].data = data.threatTypeData.values;
                threatTypeChart.update();
            }
        }

        // 刷新数据
        function refreshData() {
            loadSecurityStatus();
        }

        // 导出安全报告
        function exportSecurityReport() {
            window.open('/SCMS/admin/audit/security/export', '_blank');
        }

        // 格式化日期
        function formatDate(timestamp) {
            const date = new Date(timestamp);
            return date.toLocaleString('zh-CN');
        }
    </script>
</body>
</html>
