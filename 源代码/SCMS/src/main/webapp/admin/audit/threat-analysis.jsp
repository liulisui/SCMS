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
    <title>威胁分析 - SCMS审计管理</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        .threat-card {
            transition: all 0.3s ease;
            border-radius: 10px;
            border: 1px solid #e9ecef;
        }
        .threat-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        }
        
        .threat-level-high { border-left: 4px solid #dc3545; }
        .threat-level-medium { border-left: 4px solid #ffc107; }
        .threat-level-low { border-left: 4px solid #28a745; }
        
        .analysis-section {
            background: #f8f9fa;
            border-radius: 10px;
            padding: 2rem;
            margin-bottom: 2rem;
        }
        
        .metric-card {
            background: white;
            border-radius: 8px;
            padding: 1.5rem;
            text-align: center;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        
        .metric-value {
            font-size: 2rem;
            font-weight: bold;
            margin: 0.5rem 0;
        }
        
        .chart-container {
            position: relative;
            height: 400px;
            background: white;
            border-radius: 8px;
            padding: 1rem;
        }
        
        .threat-timeline {
            max-height: 400px;
            overflow-y: auto;
        }
        
        .timeline-item {
            border-left: 3px solid #dee2e6;
            padding-left: 1rem;
            margin-bottom: 1rem;
            position: relative;
        }
        
        .timeline-item::before {
            content: '';
            position: absolute;
            left: -6px;
            top: 0.5rem;
            width: 10px;
            height: 10px;
            border-radius: 50%;
            background: #6c757d;
        }
        
        .timeline-high::before { background: #dc3545; }
        .timeline-medium::before { background: #ffc107; }
        .timeline-low::before { background: #28a745; }
        
        .ip-analysis {
            background: white;
            border-radius: 8px;
            padding: 1rem;
        }
        
        .ip-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0.75rem;
            border-bottom: 1px solid #e9ecef;
        }
        
        .ip-item:last-child {
            border-bottom: none;
        }
        
        .risk-score {
            padding: 0.25rem 0.5rem;
            border-radius: 15px;
            font-size: 0.8rem;
            font-weight: bold;
        }
        
        .risk-high { background: #f8d7da; color: #721c24; }
        .risk-medium { background: #fff3cd; color: #856404; }
        .risk-low { background: #d1edff; color: #0c5460; }
        
        .filter-section {
            background: white;
            border-radius: 8px;
            padding: 1.5rem;
            margin-bottom: 2rem;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
    </style>
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
        <div class="container-fluid">
            <a class="navbar-brand" href="#">
                <i class="bi bi-search"></i> SCMS 威胁分析
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

    <div class="container-fluid mt-4">
        <!-- 页面标题 -->
        <div class="row mb-4">
            <div class="col-12">
                <div class="d-flex justify-content-between align-items-center">
                    <h2><i class="bi bi-search"></i> 威胁分析中心</h2>
                    <div>
                        <button class="btn btn-outline-primary" onclick="refreshAnalysis()">
                            <i class="bi bi-arrow-clockwise"></i> 刷新分析
                        </button>
                        <button class="btn btn-outline-success" onclick="exportAnalysis()">
                            <i class="bi bi-download"></i> 导出报告
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 威胁概览指标 -->
        <div class="row mb-4">
            <div class="col-lg-3 col-md-6 mb-3">
                <div class="metric-card">
                    <i class="bi bi-shield-exclamation text-danger fs-1"></i>
                    <div class="metric-value text-danger" id="totalThreats">--</div>
                    <div class="text-muted">总威胁数量</div>
                </div>
            </div>
            <div class="col-lg-3 col-md-6 mb-3">
                <div class="metric-card">
                    <i class="bi bi-geo-alt text-warning fs-1"></i>
                    <div class="metric-value text-warning" id="uniqueIPs">--</div>
                    <div class="text-muted">唯一威胁IP</div>
                </div>
            </div>
            <div class="col-lg-3 col-md-6 mb-3">
                <div class="metric-card">
                    <i class="bi bi-clock-history text-info fs-1"></i>
                    <div class="metric-value text-info" id="averageDetectionTime">--</div>
                    <div class="text-muted">平均检测时间(分钟)</div>
                </div>
            </div>
            <div class="col-lg-3 col-md-6 mb-3">
                <div class="metric-card">
                    <i class="bi bi-percent text-success fs-1"></i>
                    <div class="metric-value text-success" id="blockRate">--</div>
                    <div class="text-muted">拦截成功率</div>
                </div>
            </div>
        </div>

        <!-- 分析筛选器 -->
        <div class="filter-section">
            <form id="analysisForm">
                <div class="row">
                    <div class="col-md-3 mb-3">
                        <label class="form-label">分析时间范围</label>
                        <select class="form-select" name="timeRange">
                            <option value="24h" selected>过去24小时</option>
                            <option value="7d">过去7天</option>
                            <option value="30d">过去30天</option>
                            <option value="custom">自定义</option>
                        </select>
                    </div>
                    <div class="col-md-3 mb-3" id="customDateRange" style="display: none;">
                        <label class="form-label">自定义日期</label>
                        <div class="row">
                            <div class="col-6">
                                <input type="date" class="form-control form-control-sm" name="startDate">
                            </div>
                            <div class="col-6">
                                <input type="date" class="form-control form-control-sm" name="endDate">
                            </div>
                        </div>
                    </div>
                    <div class="col-md-3 mb-3">
                        <label class="form-label">威胁类型</label>
                        <select class="form-select" name="threatType">
                            <option value="">全部类型</option>
                            <option value="BRUTE_FORCE">暴力破解</option>
                            <option value="PRIVILEGE_ABUSE">权限滥用</option>
                            <option value="SUSPICIOUS_LOGIN">可疑登录</option>
                            <option value="DATA_BREACH">数据泄露</option>
                            <option value="AFTER_HOURS">非工作时间活动</option>
                        </select>
                    </div>
                    <div class="col-md-3 mb-3">
                        <label class="form-label">最小风险评分</label>
                        <input type="range" class="form-range" name="minRiskScore" 
                               min="0" max="100" value="0" 
                               oninput="document.getElementById('riskScoreValue').textContent = this.value">
                        <small class="text-muted">评分: <span id="riskScoreValue">0</span></small>
                    </div>
                </div>
                <div class="row">
                    <div class="col-12">
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-search"></i> 开始分析
                        </button>
                        <button type="button" class="btn btn-secondary" onclick="resetAnalysis()">
                            <i class="bi bi-arrow-clockwise"></i> 重置
                        </button>
                    </div>
                </div>
            </form>
        </div>

        <!-- 分析结果 -->
        <div class="row">
            <!-- 威胁趋势图 -->
            <div class="col-lg-8 mb-4">
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">威胁趋势分析</h5>
                    </div>
                    <div class="card-body">
                        <div class="chart-container">
                            <canvas id="threatTrendChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 威胁时间线 -->
            <div class="col-lg-4 mb-4">
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">威胁时间线</h5>
                    </div>
                    <div class="card-body">
                        <div class="threat-timeline" id="threatTimeline">
                            <div class="text-center text-muted py-3">
                                <i class="bi bi-hourglass-split"></i> 正在加载时间线...
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <!-- 威胁类型分布 -->
            <div class="col-lg-6 mb-4">
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

            <!-- 地理位置分布 -->
            <div class="col-lg-6 mb-4">
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">威胁地理分布</h5>
                    </div>
                    <div class="card-body">
                        <div class="chart-container">
                            <canvas id="geoDistributionChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 高风险IP分析 -->
        <div class="row">
            <div class="col-lg-6 mb-4">
                <div class="card">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h5 class="mb-0">高风险IP分析</h5>
                        <button class="btn btn-sm btn-outline-primary" onclick="blockSelectedIPs()">
                            <i class="bi bi-shield-x"></i> 批量拦截
                        </button>
                    </div>
                    <div class="card-body">
                        <div class="ip-analysis" id="highRiskIPs">
                            <div class="text-center text-muted py-3">
                                <i class="bi bi-hourglass-split"></i> 正在分析高风险IP...
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 攻击模式分析 -->
            <div class="col-lg-6 mb-4">
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">攻击模式分析</h5>
                    </div>
                    <div class="card-body">
                        <div id="attackPatterns">
                            <div class="text-center text-muted py-3">
                                <i class="bi bi-hourglass-split"></i> 正在分析攻击模式...
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- AI威胁预测 -->
        <div class="row">
            <div class="col-12 mb-4">
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0">
                            <i class="bi bi-cpu"></i> AI威胁预测分析
                        </h5>
                    </div>
                    <div class="card-body">
                        <div id="aiPrediction">
                            <div class="text-center text-muted py-3">
                                <i class="bi bi-hourglass-split"></i> AI正在分析威胁模式...
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        let threatTrendChart;
        let threatTypeChart;
        let geoDistributionChart;

        // 页面加载时初始化
        document.addEventListener('DOMContentLoaded', function() {
            initCharts();
            loadAnalysis();
            
            // 时间范围选择变化事件
            document.querySelector('select[name="timeRange"]').addEventListener('change', function() {
                const customRange = document.getElementById('customDateRange');
                if (this.value === 'custom') {
                    customRange.style.display = 'block';
                } else {
                    customRange.style.display = 'none';
                }
            });
            
            // 分析表单提交
            document.getElementById('analysisForm').addEventListener('submit', function(e) {
                e.preventDefault();
                loadAnalysis();
            });
        });

        // 初始化图表
        function initCharts() {
            // 威胁趋势图
            const trendCtx = document.getElementById('threatTrendChart').getContext('2d');
            threatTrendChart = new Chart(trendCtx, {
                type: 'line',
                data: {
                    labels: [],
                    datasets: [{
                        label: '高危威胁',
                        data: [],
                        borderColor: '#dc3545',
                        backgroundColor: 'rgba(220, 53, 69, 0.1)',
                        tension: 0.4
                    }, {
                        label: '中危威胁',
                        data: [],
                        borderColor: '#ffc107',
                        backgroundColor: 'rgba(255, 193, 7, 0.1)',
                        tension: 0.4
                    }, {
                        label: '低危威胁',
                        data: [],
                        borderColor: '#28a745',
                        backgroundColor: 'rgba(40, 167, 69, 0.1)',
                        tension: 0.4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
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
                            '#4bc0c0',
                            '#ff9f40'
                        ]
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

            // 地理分布图
            const geoCtx = document.getElementById('geoDistributionChart').getContext('2d');
            geoDistributionChart = new Chart(geoCtx, {
                type: 'bar',
                data: {
                    labels: [],
                    datasets: [{
                        label: '威胁数量',
                        data: [],
                        backgroundColor: 'rgba(54, 162, 235, 0.8)',
                        borderColor: 'rgba(54, 162, 235, 1)',
                        borderWidth: 1
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
        }

        // 加载分析数据
        async function loadAnalysis() {
            try {
                const formData = new FormData(document.getElementById('analysisForm'));
                const params = new URLSearchParams();
                
                for (let [key, value] of formData.entries()) {
                    if (value) params.append(key, value);
                }

                const response = await fetch(`/SCMS/admin/audit/security/threat-analysis?${params}`);
                const data = await response.json();
                
                updateMetrics(data.metrics);
                updateCharts(data);
                updateTimeline(data.timeline);
                updateHighRiskIPs(data.highRiskIPs);
                updateAttackPatterns(data.attackPatterns);
                updateAIPrediction(data.aiPrediction);
            } catch (error) {
                console.error('加载威胁分析失败:', error);
            }
        }

        // 更新指标
        function updateMetrics(metrics) {
            document.getElementById('totalThreats').textContent = metrics.totalThreats || 0;
            document.getElementById('uniqueIPs').textContent = metrics.uniqueIPs || 0;
            document.getElementById('averageDetectionTime').textContent = metrics.averageDetectionTime || '--';
            document.getElementById('blockRate').textContent = (metrics.blockRate || 0) + '%';
        }

        // 更新图表
        function updateCharts(data) {
            // 更新趋势图
            if (data.trendData) {
                threatTrendChart.data.labels = data.trendData.labels;
                threatTrendChart.data.datasets[0].data = data.trendData.high;
                threatTrendChart.data.datasets[1].data = data.trendData.medium;
                threatTrendChart.data.datasets[2].data = data.trendData.low;
                threatTrendChart.update();
            }

            // 更新威胁类型分布
            if (data.typeDistribution) {
                threatTypeChart.data.labels = data.typeDistribution.labels;
                threatTypeChart.data.datasets[0].data = data.typeDistribution.values;
                threatTypeChart.update();
            }

            // 更新地理分布
            if (data.geoDistribution) {
                geoDistributionChart.data.labels = data.geoDistribution.labels;
                geoDistributionChart.data.datasets[0].data = data.geoDistribution.values;
                geoDistributionChart.update();
            }
        }

        // 更新时间线
        function updateTimeline(timeline) {
            const container = document.getElementById('threatTimeline');
            
            if (!timeline || timeline.length === 0) {
                container.innerHTML = '<div class="text-center text-muted py-3">暂无威胁事件</div>';
                return;
            }

            container.innerHTML = timeline.map(item => `
                <div class="timeline-item timeline-${item.severity.toLowerCase()}">
                    <div class="d-flex justify-content-between align-items-start">
                        <div>
                            <strong>${item.type}</strong>
                            <p class="mb-1 text-muted small">${item.description}</p>
                            <small class="text-muted">
                                <i class="bi bi-geo-alt"></i> ${item.ipAddress}
                            </small>
                        </div>
                        <small class="text-muted">${formatTime(item.timestamp)}</small>
                    </div>
                </div>
            `).join('');
        }

        // 更新高风险IP
        function updateHighRiskIPs(ips) {
            const container = document.getElementById('highRiskIPs');
            
            if (!ips || ips.length === 0) {
                container.innerHTML = '<div class="text-center text-muted py-3">暂无高风险IP</div>';
                return;
            }

            container.innerHTML = ips.map(ip => `
                <div class="ip-item">
                    <div class="d-flex align-items-center">
                        <input type="checkbox" class="form-check-input me-2" value="${ip.address}">
                        <div>
                            <code>${ip.address}</code>
                            <br>
                            <small class="text-muted">
                                威胁次数: ${ip.threatCount} | 
                                地理位置: ${ip.location || '未知'}
                            </small>
                        </div>
                    </div>
                    <div>
                        <span class="risk-score risk-${getRiskLevel(ip.riskScore)}">
                            风险: ${ip.riskScore}
                        </span>
                    </div>
                </div>
            `).join('');
        }

        // 更新攻击模式
        function updateAttackPatterns(patterns) {
            const container = document.getElementById('attackPatterns');
            
            if (!patterns || patterns.length === 0) {
                container.innerHTML = '<div class="text-center text-muted py-3">暂无明显攻击模式</div>';
                return;
            }

            container.innerHTML = patterns.map(pattern => `
                <div class="threat-card threat-level-${pattern.severity.toLowerCase()} mb-3 p-3">
                    <h6>${pattern.name}</h6>
                    <p class="text-muted mb-2">${pattern.description}</p>
                    <div class="row">
                        <div class="col-6">
                            <small class="text-muted">检测次数: <strong>${pattern.occurrences}</strong></small>
                        </div>
                        <div class="col-6">
                            <small class="text-muted">置信度: <strong>${pattern.confidence}%</strong></small>
                        </div>
                    </div>
                    <div class="mt-2">
                        <span class="badge bg-${getSeverityColor(pattern.severity)}">${pattern.severity}</span>
                        ${pattern.automated ? '<span class="badge bg-info ms-1">自动化攻击</span>' : ''}
                    </div>
                </div>
            `).join('');
        }

        // 更新AI预测
        function updateAIPrediction(prediction) {
            const container = document.getElementById('aiPrediction');
            
            if (!prediction) {
                container.innerHTML = '<div class="text-center text-muted py-3">AI分析暂不可用</div>';
                return;
            }

            container.innerHTML = `
                <div class="row">
                    <div class="col-md-4 mb-3">
                        <div class="text-center">
                            <h4 class="text-${prediction.riskLevel === 'HIGH' ? 'danger' : prediction.riskLevel === 'MEDIUM' ? 'warning' : 'success'}">
                                ${prediction.overallRisk}%
                            </h4>
                            <p class="text-muted">整体风险评估</p>
                        </div>
                    </div>
                    <div class="col-md-4 mb-3">
                        <div class="text-center">
                            <h4 class="text-info">${prediction.nextAttackProbability}%</h4>
                            <p class="text-muted">24小时内攻击概率</p>
                        </div>
                    </div>
                    <div class="col-md-4 mb-3">
                        <div class="text-center">
                            <h4 class="text-warning">${prediction.vulnerabilityScore}</h4>
                            <p class="text-muted">系统脆弱性评分</p>
                        </div>
                    </div>
                </div>
                <div class="mt-3">
                    <h6>AI建议:</h6>
                    <ul class="list-unstyled">
                        ${prediction.recommendations.map(rec => `
                            <li class="mb-1">
                                <i class="bi bi-arrow-right text-primary"></i> ${rec}
                            </li>
                        `).join('')}
                    </ul>
                </div>
                <div class="mt-3">
                    <h6>预测趋势:</h6>
                    <p class="text-muted">${prediction.trendAnalysis}</p>
                </div>
            `;
        }

        // 拦截选中的IP
        function blockSelectedIPs() {
            const checkboxes = document.querySelectorAll('#highRiskIPs input[type="checkbox"]:checked');
            const ips = Array.from(checkboxes).map(cb => cb.value);
            
            if (ips.length === 0) {
                alert('请选择要拦截的IP地址');
                return;
            }
            
            if (confirm(`确定要拦截 ${ips.length} 个IP地址吗？`)) {
                blockIPs(ips);
            }
        }

        // 拦截IP地址
        async function blockIPs(ips) {
            try {
                const response = await fetch('/SCMS/admin/audit/security/block-ips', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ ips: ips })
                });
                
                if (response.ok) {
                    alert('IP地址拦截成功');
                    loadAnalysis(); // 重新加载分析
                } else {
                    alert('IP地址拦截失败');
                }
            } catch (error) {
                console.error('拦截IP失败:', error);
                alert('拦截IP失败');
            }
        }

        // 刷新分析
        function refreshAnalysis() {
            loadAnalysis();
        }

        // 导出分析报告
        function exportAnalysis() {
            const formData = new FormData(document.getElementById('analysisForm'));
            const params = new URLSearchParams();
            
            for (let [key, value] of formData.entries()) {
                if (value) params.append(key, value);
            }
            
            window.open(`/SCMS/admin/audit/security/threat-analysis/export?${params}`, '_blank');
        }

        // 重置分析
        function resetAnalysis() {
            document.getElementById('analysisForm').reset();
            document.getElementById('customDateRange').style.display = 'none';
            document.getElementById('riskScoreValue').textContent = '0';
            loadAnalysis();
        }

        // 工具函数
        function getRiskLevel(score) {
            if (score >= 80) return 'high';
            if (score >= 50) return 'medium';
            return 'low';
        }

        function getSeverityColor(severity) {
            switch (severity) {
                case 'HIGH': return 'danger';
                case 'MEDIUM': return 'warning';
                case 'LOW': return 'success';
                default: return 'secondary';
            }
        }

        function formatTime(timestamp) {
            const date = new Date(timestamp);
            const now = new Date();
            const diff = now - date;
            
            if (diff < 60000) return '刚刚';
            if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
            if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
            
            return date.toLocaleDateString('zh-CN');
        }
    </script>
</body>
</html>
