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
    <title>实时监控 - SCMS审计管理</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        .realtime-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border-radius: 15px;
            padding: 1.5rem;
            margin-bottom: 1rem;
        }
        
        .status-indicator {
            width: 12px;
            height: 12px;
            border-radius: 50%;
            display: inline-block;
            margin-right: 8px;
            animation: pulse 2s infinite;
        }
        
        .status-online { background-color: #28a745; }
        .status-warning { background-color: #ffc107; }
        .status-danger { background-color: #dc3545; }
        .status-offline { background-color: #6c757d; }
        
        @keyframes pulse {
            0% { opacity: 1; }
            50% { opacity: 0.5; }
            100% { opacity: 1; }
        }
        
        .metric-card {
            background: white;
            border-radius: 10px;
            padding: 1.5rem;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            transition: all 0.3s ease;
            border-left: 4px solid transparent;
        }
        
        .metric-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 20px rgba(0,0,0,0.15);
        }
        
        .metric-card.metric-success { border-left-color: #28a745; }
        .metric-card.metric-warning { border-left-color: #ffc107; }
        .metric-card.metric-danger { border-left-color: #dc3545; }
        .metric-card.metric-info { border-left-color: #17a2b8; }
        
        .activity-feed {
            max-height: 400px;
            overflow-y: auto;
            background: white;
            border-radius: 10px;
            padding: 1rem;
        }
        
        .activity-item {
            display: flex;
            align-items-center;
            padding: 0.75rem;
            border-bottom: 1px solid #e9ecef;
            transition: background-color 0.3s ease;
        }
        
        .activity-item:hover {
            background-color: #f8f9fa;
        }
        
        .activity-item:last-child {
            border-bottom: none;
        }
        
        .activity-icon {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 1rem;
            font-size: 1.2rem;
        }
        
        .activity-login { background-color: #d1ecf1; color: #0c5460; }
        .activity-error { background-color: #f8d7da; color: #721c24; }
        .activity-admin { background-color: #fff3cd; color: #856404; }
        .activity-security { background-color: #f1c0c7; color: #721c24; }
        
        .chart-container {
            position: relative;
            height: 250px;
            background: white;
            border-radius: 10px;
            padding: 1rem;
        }
        
        .monitoring-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 1rem;
            margin-bottom: 2rem;
        }
        
        .alert-banner {
            background: linear-gradient(45deg, #ff6b6b, #ee5a52);
            color: white;
            padding: 1rem;
            border-radius: 10px;
            margin-bottom: 1rem;
            display: none;
        }
        
        .connection-status {
            position: fixed;
            top: 80px;
            right: 20px;
            background: white;
            border-radius: 10px;
            padding: 1rem;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            z-index: 1000;
        }
        
        .system-health {
            background: white;
            border-radius: 10px;
            padding: 1.5rem;
        }
        
        .health-item {
            display: flex;
            justify-content: between;
            align-items: center;
            padding: 0.75rem 0;
            border-bottom: 1px solid #e9ecef;
        }
        
        .health-item:last-child {
            border-bottom: none;
        }
        
        .health-status {
            padding: 0.25rem 0.75rem;
            border-radius: 15px;
            font-size: 0.8rem;
            font-weight: bold;
        }
        
        .health-good { background: #d4edda; color: #155724; }
        .health-warning { background: #fff3cd; color: #856404; }
        .health-critical { background: #f8d7da; color: #721c24; }
    </style>
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
        <div class="container-fluid">
            <a class="navbar-brand" href="#">
                <i class="bi bi-activity"></i> SCMS 实时监控
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
                        <a class="nav-link" href="security.jsp">
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

    <!-- 连接状态指示器 -->
    <div class="connection-status" id="connectionStatus">
        <div class="d-flex align-items-center">
            <span class="status-indicator status-online" id="statusIndicator"></span>
            <span id="statusText">实时连接中</span>
        </div>
        <small class="text-muted">最后更新: <span id="lastUpdate">--</span></small>
    </div>

    <div class="container-fluid mt-4">
        <!-- 紧急警报横幅 -->
        <div class="alert-banner" id="alertBanner">
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    <i class="bi bi-exclamation-triangle fs-4"></i>
                    <span id="alertMessage"></span>
                </div>
                <button class="btn btn-outline-light btn-sm" onclick="dismissAlert()">
                    <i class="bi bi-x"></i>
                </button>
            </div>
        </div>

        <!-- 页面标题 -->
        <div class="row mb-4">
            <div class="col-12">
                <div class="realtime-card">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <h2 class="mb-1">
                                <i class="bi bi-activity"></i> 实时监控中心
                            </h2>
                            <p class="mb-0">系统运行状态实时监控与分析</p>
                        </div>
                        <div class="text-center">
                            <h4 id="activeUsers">--</h4>
                            <small>在线用户</small>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 实时指标 -->
        <div class="monitoring-grid">
            <div class="metric-card metric-success">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h3 id="systemLoad">--</h3>
                        <p class="text-muted mb-0">系统负载</p>
                    </div>
                    <i class="bi bi-cpu fs-1 text-success"></i>
                </div>
            </div>
            
            <div class="metric-card metric-info">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h3 id="memoryUsage">--</h3>
                        <p class="text-muted mb-0">内存使用率</p>
                    </div>
                    <i class="bi bi-memory fs-1 text-info"></i>
                </div>
            </div>
            
            <div class="metric-card metric-warning">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h3 id="requestsPerMinute">--</h3>
                        <p class="text-muted mb-0">每分钟请求数</p>
                    </div>
                    <i class="bi bi-graph-up fs-1 text-warning"></i>
                </div>
            </div>
            
            <div class="metric-card metric-danger">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h3 id="errorRate">--</h3>
                        <p class="text-muted mb-0">错误率</p>
                    </div>
                    <i class="bi bi-exclamation-triangle fs-1 text-danger"></i>
                </div>
            </div>
        </div>

        <div class="row">
            <!-- 实时活动图表 -->
            <div class="col-lg-8 mb-4">
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h5 class="mb-0">实时活动趋势</h5>
                        <div class="btn-group btn-group-sm" role="group">
                            <input type="radio" class="btn-check" name="timeWindow" id="time1m" value="1m" checked>
                            <label class="btn btn-outline-primary" for="time1m">1分钟</label>
                            
                            <input type="radio" class="btn-check" name="timeWindow" id="time5m" value="5m">
                            <label class="btn btn-outline-primary" for="time5m">5分钟</label>
                            
                            <input type="radio" class="btn-check" name="timeWindow" id="time15m" value="15m">
                            <label class="btn btn-outline-primary" for="time15m">15分钟</label>
                        </div>
                    </div>
                    <div class="card-body">
                        <div class="chart-container">
                            <canvas id="realtimeChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 系统健康状态 -->
            <div class="col-lg-4 mb-4">
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">系统健康状态</h5>
                    </div>
                    <div class="card-body">
                        <div class="system-health" id="systemHealth">
                            <div class="text-center text-muted py-3">
                                <i class="bi bi-hourglass-split"></i> 正在检测系统状态...
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <!-- 实时活动流 -->
            <div class="col-lg-6 mb-4">
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h5 class="mb-0">实时活动流</h5>
                        <div>
                            <button class="btn btn-sm btn-outline-secondary" onclick="pauseActivityFeed()" id="pauseBtn">
                                <i class="bi bi-pause"></i> 暂停
                            </button>
                            <button class="btn btn-sm btn-outline-primary" onclick="clearActivityFeed()">
                                <i class="bi bi-trash"></i> 清空
                            </button>
                        </div>
                    </div>
                    <div class="card-body p-0">
                        <div class="activity-feed" id="activityFeed">
                            <div class="text-center text-muted py-3">
                                <i class="bi bi-hourglass-split"></i> 等待活动数据...
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 安全监控 -->
            <div class="col-lg-6 mb-4">
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">安全监控</h5>
                    </div>
                    <div class="card-body">
                        <div class="chart-container">
                            <canvas id="securityChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 详细监控面板 -->
        <div class="row">
            <div class="col-12">
                <div class="card">
                    <div class="card-header">
                        <ul class="nav nav-tabs card-header-tabs" id="monitorTabs" role="tablist">
                            <li class="nav-item" role="presentation">
                                <button class="nav-link active" id="performance-tab" data-bs-toggle="tab" 
                                        data-bs-target="#performance" type="button" role="tab">
                                    <i class="bi bi-speedometer2"></i> 性能监控
                                </button>
                            </li>
                            <li class="nav-item" role="presentation">
                                <button class="nav-link" id="database-tab" data-bs-toggle="tab" 
                                        data-bs-target="#database" type="button" role="tab">
                                    <i class="bi bi-database"></i> 数据库监控
                                </button>
                            </li>
                            <li class="nav-item" role="presentation">
                                <button class="nav-link" id="network-tab" data-bs-toggle="tab" 
                                        data-bs-target="#network" type="button" role="tab">
                                    <i class="bi bi-wifi"></i> 网络监控
                                </button>
                            </li>
                            <li class="nav-item" role="presentation">
                                <button class="nav-link" id="logs-tab" data-bs-toggle="tab" 
                                        data-bs-target="#logs" type="button" role="tab">
                                    <i class="bi bi-file-text"></i> 日志监控
                                </button>
                            </li>
                        </ul>
                    </div>
                    <div class="card-body">
                        <div class="tab-content" id="monitorTabContent">
                            <!-- 性能监控 -->
                            <div class="tab-pane fade show active" id="performance" role="tabpanel">
                                <div class="row">
                                    <div class="col-md-6">
                                        <canvas id="cpuChart" style="height: 200px;"></canvas>
                                    </div>
                                    <div class="col-md-6">
                                        <canvas id="memoryChart" style="height: 200px;"></canvas>
                                    </div>
                                </div>
                            </div>
                            
                            <!-- 数据库监控 -->
                            <div class="tab-pane fade" id="database" role="tabpanel">
                                <div class="row">
                                    <div class="col-md-6">
                                        <canvas id="dbConnectionChart" style="height: 200px;"></canvas>
                                    </div>
                                    <div class="col-md-6">
                                        <canvas id="dbQueryChart" style="height: 200px;"></canvas>
                                    </div>
                                </div>
                            </div>
                            
                            <!-- 网络监控 -->
                            <div class="tab-pane fade" id="network" role="tabpanel">
                                <div class="row">
                                    <div class="col-md-6">
                                        <canvas id="networkInChart" style="height: 200px;"></canvas>
                                    </div>
                                    <div class="col-md-6">
                                        <canvas id="networkOutChart" style="height: 200px;"></canvas>
                                    </div>
                                </div>
                            </div>
                            
                            <!-- 日志监控 -->
                            <div class="tab-pane fade" id="logs" role="tabpanel">
                                <div class="table-responsive">
                                    <table class="table table-striped" id="realtimeLogsTable">
                                        <thead>
                                            <tr>
                                                <th>时间</th>
                                                <th>级别</th>
                                                <th>模块</th>
                                                <th>消息</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <tr>
                                                <td colspan="4" class="text-center text-muted">
                                                    正在加载日志数据...
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
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        let realtimeChart;
        let securityChart;
        let cpuChart, memoryChart, dbConnectionChart, dbQueryChart;
        let networkInChart, networkOutChart;
        
        let activityPaused = false;
        let maxActivityItems = 50;
        let websocket = null;
        let reconnectAttempts = 0;
        let maxReconnectAttempts = 5;

        // 页面加载时初始化
        document.addEventListener('DOMContentLoaded', function() {
            initCharts();
            connectWebSocket();
            
            // 时间窗口变化事件
            document.querySelectorAll('input[name="timeWindow"]').forEach(radio => {
                radio.addEventListener('change', function() {
                    updateChartTimeWindow(this.value);
                });
            });
        });

        // 初始化图表
        function initCharts() {
            // 实时活动趋势图
            const realtimeCtx = document.getElementById('realtimeChart').getContext('2d');
            realtimeChart = new Chart(realtimeCtx, {
                type: 'line',
                data: {
                    labels: [],
                    datasets: [{
                        label: '登录活动',
                        data: [],
                        borderColor: '#007bff',
                        backgroundColor: 'rgba(0, 123, 255, 0.1)',
                        tension: 0.4
                    }, {
                        label: '管理操作',
                        data: [],
                        borderColor: '#28a745',
                        backgroundColor: 'rgba(40, 167, 69, 0.1)',
                        tension: 0.4
                    }, {
                        label: '安全事件',
                        data: [],
                        borderColor: '#dc3545',
                        backgroundColor: 'rgba(220, 53, 69, 0.1)',
                        tension: 0.4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    animation: false,
                    scales: {
                        x: {
                            type: 'time',
                            time: {
                                unit: 'minute'
                            }
                        },
                        y: {
                            beginAtZero: true
                        }
                    },
                    plugins: {
                        legend: {
                            position: 'top'
                        }
                    }
                }
            });

            // 安全监控图表
            const securityCtx = document.getElementById('securityChart').getContext('2d');
            securityChart = new Chart(securityCtx, {
                type: 'doughnut',
                data: {
                    labels: ['正常', '警告', '危险'],
                    datasets: [{
                        data: [0, 0, 0],
                        backgroundColor: ['#28a745', '#ffc107', '#dc3545']
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom'
                        }
                    }
                }
            });

            initDetailCharts();
        }

        // 初始化详细监控图表
        function initDetailCharts() {
            const chartOptions = {
                responsive: true,
                maintainAspectRatio: false,
                animation: false,
                scales: {
                    y: { beginAtZero: true }
                }
            };

            // CPU图表
            cpuChart = new Chart(document.getElementById('cpuChart').getContext('2d'), {
                type: 'line',
                data: {
                    labels: [],
                    datasets: [{
                        label: 'CPU使用率 (%)',
                        data: [],
                        borderColor: '#007bff',
                        backgroundColor: 'rgba(0, 123, 255, 0.1)',
                        tension: 0.4
                    }]
                },
                options: chartOptions
            });

            // 内存图表
            memoryChart = new Chart(document.getElementById('memoryChart').getContext('2d'), {
                type: 'line',
                data: {
                    labels: [],
                    datasets: [{
                        label: '内存使用率 (%)',
                        data: [],
                        borderColor: '#28a745',
                        backgroundColor: 'rgba(40, 167, 69, 0.1)',
                        tension: 0.4
                    }]
                },
                options: chartOptions
            });

            // 数据库连接图表
            dbConnectionChart = new Chart(document.getElementById('dbConnectionChart').getContext('2d'), {
                type: 'line',
                data: {
                    labels: [],
                    datasets: [{
                        label: '活跃连接数',
                        data: [],
                        borderColor: '#ffc107',
                        backgroundColor: 'rgba(255, 193, 7, 0.1)',
                        tension: 0.4
                    }]
                },
                options: chartOptions
            });

            // 数据库查询图表
            dbQueryChart = new Chart(document.getElementById('dbQueryChart').getContext('2d'), {
                type: 'line',
                data: {
                    labels: [],
                    datasets: [{
                        label: '每秒查询数',
                        data: [],
                        borderColor: '#dc3545',
                        backgroundColor: 'rgba(220, 53, 69, 0.1)',
                        tension: 0.4
                    }]
                },
                options: chartOptions
            });

            // 网络流量图表
            networkInChart = new Chart(document.getElementById('networkInChart').getContext('2d'), {
                type: 'line',
                data: {
                    labels: [],
                    datasets: [{
                        label: '网络入流量 (KB/s)',
                        data: [],
                        borderColor: '#17a2b8',
                        backgroundColor: 'rgba(23, 162, 184, 0.1)',
                        tension: 0.4
                    }]
                },
                options: chartOptions
            });

            networkOutChart = new Chart(document.getElementById('networkOutChart').getContext('2d'), {
                type: 'line',
                data: {
                    labels: [],
                    datasets: [{
                        label: '网络出流量 (KB/s)',
                        data: [],
                        borderColor: '#6f42c1',
                        backgroundColor: 'rgba(111, 66, 193, 0.1)',
                        tension: 0.4
                    }]
                },
                options: chartOptions
            });
        }

        // 连接WebSocket
        function connectWebSocket() {
            try {
                const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
                const host = window.location.host;
                websocket = new WebSocket(`${protocol}//${host}/SCMS/realtime-monitor`);
                
                websocket.onopen = function() {
                    console.log('WebSocket连接已建立');
                    updateConnectionStatus('online', '实时连接中');
                    reconnectAttempts = 0;
                };
                
                websocket.onmessage = function(event) {
                    const data = JSON.parse(event.data);
                    handleRealtimeData(data);
                    updateLastUpdate();
                };
                
                websocket.onclose = function() {
                    console.log('WebSocket连接已关闭');
                    updateConnectionStatus('offline', '连接已断开');
                    attemptReconnect();
                };
                
                websocket.onerror = function(error) {
                    console.error('WebSocket错误:', error);
                    updateConnectionStatus('danger', '连接错误');
                };
            } catch (error) {
                console.error('WebSocket连接失败:', error);
                updateConnectionStatus('danger', '连接失败');
                // 降级到轮询模式
                startPolling();
            }
        }

        // 尝试重连
        function attemptReconnect() {
            if (reconnectAttempts < maxReconnectAttempts) {
                reconnectAttempts++;
                updateConnectionStatus('warning', `重连中... (${reconnectAttempts}/${maxReconnectAttempts})`);
                
                setTimeout(() => {
                    connectWebSocket();
                }, 5000 * reconnectAttempts); // 递增延迟
            } else {
                updateConnectionStatus('danger', '连接失败，使用轮询模式');
                startPolling();
            }
        }

        // 启动轮询模式
        function startPolling() {
            setInterval(async () => {
                try {
                    const response = await fetch('/SCMS/admin/audit/security/realtime/status');
                    const data = await response.json();
                    handleRealtimeData(data);
                    updateLastUpdate();
                    
                    if (!websocket || websocket.readyState === WebSocket.CLOSED) {
                        updateConnectionStatus('warning', '轮询模式');
                    }
                } catch (error) {
                    console.error('轮询获取数据失败:', error);
                }
            }, 5000); // 5秒轮询一次
        }

        // 处理实时数据
        function handleRealtimeData(data) {
            switch (data.type) {
                case 'metrics':
                    updateMetrics(data.data);
                    break;
                case 'activity':
                    addActivityItem(data.data);
                    break;
                case 'alert':
                    showAlert(data.data);
                    break;
                case 'system_health':
                    updateSystemHealth(data.data);
                    break;
                case 'chart_data':
                    updateChartData(data.data);
                    break;
                case 'logs':
                    updateRealtimeLogs(data.data);
                    break;
            }
        }

        // 更新指标
        function updateMetrics(metrics) {
            document.getElementById('activeUsers').textContent = metrics.activeUsers || '--';
            document.getElementById('systemLoad').textContent = metrics.systemLoad || '--';
            document.getElementById('memoryUsage').textContent = (metrics.memoryUsage || 0) + '%';
            document.getElementById('requestsPerMinute').textContent = metrics.requestsPerMinute || '--';
            document.getElementById('errorRate').textContent = (metrics.errorRate || 0) + '%';
        }

        // 添加活动项目
        function addActivityItem(activity) {
            if (activityPaused) return;
            
            const feed = document.getElementById('activityFeed');
            const item = document.createElement('div');
            item.className = 'activity-item';
            item.innerHTML = `
                <div class="activity-icon activity-${activity.type}">
                    <i class="bi bi-${getActivityIcon(activity.type)}"></i>
                </div>
                <div class="flex-grow-1">
                    <div class="d-flex justify-content-between">
                        <strong>${activity.title}</strong>
                        <small class="text-muted">${formatTime(activity.timestamp)}</small>
                    </div>
                    <p class="mb-0 text-muted small">${activity.description}</p>
                </div>
            `;
            
            // 添加到顶部
            if (feed.firstChild && feed.firstChild.classList && feed.firstChild.classList.contains('text-center')) {
                feed.innerHTML = '';
            }
            feed.insertBefore(item, feed.firstChild);
            
            // 限制显示数量
            while (feed.children.length > maxActivityItems) {
                feed.removeChild(feed.lastChild);
            }
        }

        // 显示警报
        function showAlert(alert) {
            const banner = document.getElementById('alertBanner');
            const message = document.getElementById('alertMessage');
            
            message.textContent = alert.message;
            banner.style.display = 'block';
            
            // 自动消失
            setTimeout(() => {
                banner.style.display = 'none';
            }, 10000);
        }

        // 更新系统健康状态
        function updateSystemHealth(health) {
            const container = document.getElementById('systemHealth');
            
            container.innerHTML = health.map(item => `
                <div class="health-item">
                    <div class="d-flex justify-content-between align-items-center w-100">
                        <span>${item.name}</span>
                        <span class="health-status health-${item.status}">${getHealthStatusText(item.status)}</span>
                    </div>
                    ${item.details ? `<small class="text-muted">${item.details}</small>` : ''}
                </div>
            `).join('');
        }

        // 更新图表数据
        function updateChartData(chartData) {
            if (chartData.realtime) {
                updateChart(realtimeChart, chartData.realtime);
            }
            if (chartData.security) {
                securityChart.data.datasets[0].data = chartData.security.data;
                securityChart.update('none');
            }
            if (chartData.performance) {
                updateChart(cpuChart, chartData.performance.cpu);
                updateChart(memoryChart, chartData.performance.memory);
            }
            if (chartData.database) {
                updateChart(dbConnectionChart, chartData.database.connections);
                updateChart(dbQueryChart, chartData.database.queries);
            }
            if (chartData.network) {
                updateChart(networkInChart, chartData.network.inbound);
                updateChart(networkOutChart, chartData.network.outbound);
            }
        }

        // 更新单个图表
        function updateChart(chart, data) {
            if (data.labels) {
                chart.data.labels = data.labels;
            }
            if (data.datasets) {
                data.datasets.forEach((dataset, index) => {
                    if (chart.data.datasets[index]) {
                        chart.data.datasets[index].data = dataset.data;
                    }
                });
            }
            chart.update('none');
        }

        // 更新实时日志
        function updateRealtimeLogs(logs) {
            const tbody = document.querySelector('#realtimeLogsTable tbody');
            
            if (!logs || logs.length === 0) return;
            
            tbody.innerHTML = logs.map(log => `
                <tr class="table-${getLogLevelClass(log.level)}">
                    <td>${formatTime(log.timestamp)}</td>
                    <td>
                        <span class="badge bg-${getLogLevelClass(log.level)}">${log.level}</span>
                    </td>
                    <td>${log.module}</td>
                    <td>${log.message}</td>
                </tr>
            `).join('');
        }

        // 更新连接状态
        function updateConnectionStatus(status, text) {
            const indicator = document.getElementById('statusIndicator');
            const statusText = document.getElementById('statusText');
            
            indicator.className = `status-indicator status-${status}`;
            statusText.textContent = text;
        }

        // 更新最后更新时间
        function updateLastUpdate() {
            document.getElementById('lastUpdate').textContent = new Date().toLocaleTimeString('zh-CN');
        }

        // 暂停/恢复活动流
        function pauseActivityFeed() {
            const btn = document.getElementById('pauseBtn');
            activityPaused = !activityPaused;
            
            if (activityPaused) {
                btn.innerHTML = '<i class="bi bi-play"></i> 恢复';
                btn.className = 'btn btn-sm btn-outline-success';
            } else {
                btn.innerHTML = '<i class="bi bi-pause"></i> 暂停';
                btn.className = 'btn btn-sm btn-outline-secondary';
            }
        }

        // 清空活动流
        function clearActivityFeed() {
            document.getElementById('activityFeed').innerHTML = `
                <div class="text-center text-muted py-3">
                    <i class="bi bi-info-circle"></i> 活动流已清空
                </div>
            `;
        }

        // 更新图表时间窗口
        function updateChartTimeWindow(window) {
            if (websocket && websocket.readyState === WebSocket.OPEN) {
                websocket.send(JSON.stringify({
                    type: 'change_time_window',
                    window: window
                }));
            }
        }

        // 关闭警报
        function dismissAlert() {
            document.getElementById('alertBanner').style.display = 'none';
        }

        // 工具函数
        function getActivityIcon(type) {
            switch (type) {
                case 'login': return 'box-arrow-in-right';
                case 'error': return 'exclamation-triangle';
                case 'admin': return 'gear';
                case 'security': return 'shield-exclamation';
                default: return 'circle';
            }
        }

        function getHealthStatusText(status) {
            switch (status) {
                case 'good': return '正常';
                case 'warning': return '警告';
                case 'critical': return '严重';
                default: return '未知';
            }
        }

        function getLogLevelClass(level) {
            switch (level.toLowerCase()) {
                case 'error': return 'danger';
                case 'warn': case 'warning': return 'warning';
                case 'info': return 'info';
                case 'debug': return 'secondary';
                default: return 'light';
            }
        }

        function formatTime(timestamp) {
            const date = new Date(timestamp);
            const now = new Date();
            const diff = now - date;
            
            if (diff < 60000) return '刚刚';
            if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
            
            return date.toLocaleTimeString('zh-CN');
        }

        // 页面卸载时关闭WebSocket
        window.addEventListener('beforeunload', function() {
            if (websocket) {
                websocket.close();
            }
        });
    </script>
</body>
</html>
