<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>审计报告 - 供应链管理系统</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        .navbar-brand {
            font-weight: bold;
            color: #495057;
        }
        .sidebar {
            background-color: #f8f9fa;
            min-height: calc(100vh - 76px);
            padding: 20px 0;
        }
        .sidebar .nav-link {
            color: #495057;
            padding: 10px 20px;
            border-left: 3px solid transparent;
        }
        .sidebar .nav-link:hover,
        .sidebar .nav-link.active {
            background-color: #e9ecef;
            color: #007bff;
            border-left-color: #007bff;
        }
        .report-card {
            background: white;
            border-radius: 10px;
            padding: 20px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }
        .chart-container {
            background: white;
            border-radius: 10px;
            padding: 20px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            margin-bottom: 20px;
            min-height: 300px;
        }
        .stat-card {
            text-align: center;
            padding: 20px;
        }
        .stat-number {
            font-size: 2rem;
            font-weight: bold;
            color: #007bff;
        }
        .report-nav {
            background: white;
            border-radius: 10px;
            padding: 15px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }
        .report-nav .nav-pills .nav-link {
            color: #6c757d;
            border-radius: 20px;
            margin-right: 10px;
        }
        .report-nav .nav-pills .nav-link.active {
            background-color: #007bff;
            color: white;
        }
        .table-container {
            background: white;
            border-radius: 10px;
            padding: 20px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }
        .export-btn {
            background: linear-gradient(45deg, #28a745, #20c997);
            border: none;
            border-radius: 20px;
            color: white;
            padding: 8px 20px;
        }
        .export-btn:hover {
            transform: translateY(-1px);
            box-shadow: 0 4px 8px rgba(0,0,0,0.2);
            color: white;
        }
    </style>
</head>
<body class="bg-light">
    <!-- 导航栏 -->
    <nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm">
        <div class="container-fluid">
            <a class="navbar-brand" href="/admin/audit/dashboard">
                <i class="fas fa-shield-alt text-primary me-2"></i>
                审计管理员仪表板
            </a>
            <div class="navbar-nav ms-auto">
                <div class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="#" id="navbarDropdown" role="button" 
                       data-bs-toggle="dropdown" aria-expanded="false">
                        <i class="fas fa-user-circle me-1"></i>
                        ${sessionScope.admin.fullName}
                    </a>
                    <ul class="dropdown-menu">
                        <li><a class="dropdown-item" href="/admin/logout">
                            <i class="fas fa-sign-out-alt me-2"></i>退出登录
                        </a></li>
                    </ul>
                </div>
            </div>
        </div>
    </nav>

    <div class="container-fluid">
        <div class="row">
            <!-- 侧边栏 -->
            <div class="col-md-2 sidebar">
                <nav class="nav flex-column">
                    <a class="nav-link" href="/admin/audit/dashboard">
                        <i class="fas fa-tachometer-alt me-2"></i>仪表板
                    </a>
                    <a class="nav-link" href="/admin/audit/logs">
                        <i class="fas fa-list-alt me-2"></i>审计日志
                    </a>
                    <a class="nav-link active" href="/admin/audit/reports">
                        <i class="fas fa-chart-bar me-2"></i>统计报告
                    </a>
                    <a class="nav-link" href="/admin/audit/security">
                        <i class="fas fa-shield-alt me-2"></i>安全监控
                    </a>
                </nav>
            </div>

            <!-- 主内容区 -->
            <div class="col-md-10">
                <div class="container-fluid py-4">
                    <!-- 页面标题 -->
                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <h2 class="text-dark">
                            <i class="fas fa-chart-bar me-2"></i>审计报告
                        </h2>
                        <div class="d-flex gap-2">
                            <button type="button" class="btn export-btn" onclick="exportReport()">
                                <i class="fas fa-download me-2"></i>导出报告
                            </button>
                        </div>
                    </div>

                    <!-- 报告类型导航 -->
                    <div class="report-nav">
                        <ul class="nav nav-pills">
                            <li class="nav-item">
                                <a class="nav-link ${reportType == null || reportType == 'overview' ? 'active' : ''}" 
                                   href="?reportType=overview&startDate=${startDate}&endDate=${endDate}">
                                    <i class="fas fa-chart-pie me-2"></i>概览报告
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link ${reportType == 'activity' ? 'active' : ''}" 
                                   href="?reportType=activity&startDate=${startDate}&endDate=${endDate}">
                                    <i class="fas fa-users me-2"></i>活动报告
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link ${reportType == 'security' ? 'active' : ''}" 
                                   href="?reportType=security&startDate=${startDate}&endDate=${endDate}">
                                    <i class="fas fa-shield-alt me-2"></i>安全报告
                                </a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link ${reportType == 'admin' ? 'active' : ''}" 
                                   href="?reportType=admin&startDate=${startDate}&endDate=${endDate}">
                                    <i class="fas fa-user-cog me-2"></i>管理员报告
                                </a>
                            </li>
                        </ul>
                    </div>

                    <!-- 时间范围选择 -->
                    <div class="report-card">
                        <h5 class="mb-3">
                            <i class="fas fa-calendar me-2"></i>报告时间范围
                        </h5>
                        <form method="GET" class="row g-3">
                            <input type="hidden" name="reportType" value="${reportType}">
                            <div class="col-md-3">
                                <label for="startDate" class="form-label">开始日期</label>
                                <input type="date" class="form-control" id="startDate" name="startDate" 
                                       value="${startDate}" required>
                            </div>
                            <div class="col-md-3">
                                <label for="endDate" class="form-label">结束日期</label>
                                <input type="date" class="form-control" id="endDate" name="endDate" 
                                       value="${endDate}" required>
                            </div>
                            <div class="col-md-3 d-flex align-items-end">
                                <button type="submit" class="btn btn-primary">
                                    <i class="fas fa-search me-2"></i>生成报告
                                </button>
                            </div>
                        </form>
                        <div class="mt-2 text-muted">
                            <small>
                                <i class="fas fa-info-circle me-1"></i>
                                当前报告时间范围：${startDate} 至 ${endDate}
                                <c:if test="${not empty warning}">
                                    <span class="text-warning ms-3">
                                        <i class="fas fa-exclamation-triangle me-1"></i>${warning}
                                    </span>
                                </c:if>
                            </small>
                        </div>
                    </div>

                    <!-- 概览报告 -->
                    <c:if test="${reportType == null || reportType == 'overview'}">
                        <div class="row">
                            <!-- 基本统计 -->
                            <div class="col-md-3 mb-4">
                                <div class="report-card stat-card">
                                    <div class="stat-number">${reportData.totalLogs}</div>
                                    <div class="text-muted">总日志条数</div>
                                </div>
                            </div>
                            <div class="col-md-3 mb-4">
                                <div class="report-card stat-card">
                                    <div class="stat-number text-success">${reportData.successfulLogins}</div>
                                    <div class="text-muted">成功登录</div>
                                </div>
                            </div>
                            <div class="col-md-3 mb-4">
                                <div class="report-card stat-card">
                                    <div class="stat-number text-danger">${reportData.failedLogins}</div>
                                    <div class="text-muted">失败登录</div>
                                </div>
                            </div>
                            <div class="col-md-3 mb-4">
                                <div class="report-card stat-card">
                                    <div class="stat-number text-info">
                                        <c:choose>
                                            <c:when test="${reportData.failedLogins > 0}">
                                                <fmt:formatNumber value="${reportData.successfulLogins / (reportData.successfulLogins + reportData.failedLogins) * 100}" 
                                                                  pattern="#.##"/>%
                                            </c:when>
                                            <c:otherwise>100%</c:otherwise>
                                        </c:choose>
                                    </div>
                                    <div class="text-muted">登录成功率</div>
                                </div>
                            </div>
                        </div>

                        <!-- 图表 -->
                        <div class="row">
                            <div class="col-md-6 mb-4">
                                <div class="chart-container">
                                    <h5 class="mb-3">操作类型分布</h5>
                                    <canvas id="actionChart"></canvas>
                                </div>
                            </div>
                            <div class="col-md-6 mb-4">
                                <div class="chart-container">
                                    <h5 class="mb-3">资源类型分布</h5>
                                    <canvas id="resourceChart"></canvas>
                                </div>
                            </div>
                        </div>

                        <!-- 每日活动趋势 -->
                        <div class="chart-container">
                            <h5 class="mb-3">每日活动趋势</h5>
                            <canvas id="dailyTrendChart"></canvas>
                        </div>
                    </c:if>

                    <!-- 活动报告 -->
                    <c:if test="${reportType == 'activity'}">
                        <div class="row">
                            <!-- 管理员活动统计 -->
                            <div class="col-md-6 mb-4">
                                <div class="chart-container">
                                    <h5 class="mb-3">管理员活动统计</h5>
                                    <canvas id="adminActivityChart"></canvas>
                                </div>
                            </div>
                            <!-- 时间段活动分布 -->
                            <div class="col-md-6 mb-4">
                                <div class="chart-container">
                                    <h5 class="mb-3">时间段活动分布</h5>
                                    <canvas id="hourlyActivityChart"></canvas>
                                </div>
                            </div>
                        </div>

                        <!-- 管理员详细活动 -->
                        <div class="table-container">
                            <h5 class="mb-3">管理员详细活动</h5>
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead class="table-light">
                                        <tr>
                                            <th>管理员</th>
                                            <th>用户名</th>
                                            <th>角色</th>
                                            <th>活动次数</th>
                                            <th>活跃度</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="entry" items="${reportData.adminDetails}">
                                            <tr>
                                                <td>${entry.value.fullName}</td>
                                                <td><code>${entry.value.username}</code></td>
                                                <td>
                                                    <span class="badge bg-secondary">${entry.value.role}</span>
                                                </td>
                                                <td>${entry.value.activityCount}</td>
                                                <td>
                                                    <div class="progress" style="height: 20px;">
                                                        <div class="progress-bar" role="progressbar" 
                                                             style="width: ${entry.value.activityCount * 100 / reportData.totalLogs}%">
                                                            <fmt:formatNumber value="${entry.value.activityCount * 100 / reportData.totalLogs}" 
                                                                              pattern="#.#"/>%
                                                        </div>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </c:if>

                    <!-- 安全报告 -->
                    <c:if test="${reportType == 'security'}">
                        <div class="row">
                            <!-- 登录失败统计 -->
                            <div class="col-md-4 mb-4">
                                <div class="report-card stat-card">
                                    <div class="stat-number text-danger">${reportData.failedLogins.size()}</div>
                                    <div class="text-muted">总失败登录次数</div>
                                </div>
                            </div>
                            <div class="col-md-4 mb-4">
                                <div class="report-card stat-card">
                                    <div class="stat-number text-warning">${reportData.suspiciousIPs.size()}</div>
                                    <div class="text-muted">可疑IP地址</div>
                                </div>
                            </div>
                            <div class="col-md-4 mb-4">
                                <div class="report-card stat-card">
                                    <div class="stat-number text-info">${reportData.privilegeOperations.size()}</div>
                                    <div class="text-muted">权限操作次数</div>
                                </div>
                            </div>
                        </div>

                        <!-- 可疑IP统计 -->
                        <c:if test="${not empty reportData.suspiciousIPs}">
                            <div class="table-container">
                                <h5 class="mb-3">
                                    <i class="fas fa-exclamation-triangle text-warning me-2"></i>可疑IP地址
                                </h5>
                                <div class="table-responsive">
                                    <table class="table table-hover">
                                        <thead class="table-light">
                                            <tr>
                                                <th>IP地址</th>
                                                <th>失败次数</th>
                                                <th>威胁等级</th>
                                                <th>状态</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="entry" items="${reportData.suspiciousIPs}">
                                                <tr>
                                                    <td><code>${entry.key}</code></td>
                                                    <td>
                                                        <span class="badge bg-danger">${entry.value}</span>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${entry.value > 20}">
                                                                <span class="badge bg-danger">高危</span>
                                                            </c:when>
                                                            <c:when test="${entry.value > 10}">
                                                                <span class="badge bg-warning">中危</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge bg-info">低危</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <span class="badge bg-secondary">监控中</span>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </c:if>

                        <!-- 非工作时间活动 -->
                        <c:if test="${not empty reportData.afterHoursActivity}">
                            <div class="table-container">
                                <h5 class="mb-3">
                                    <i class="fas fa-moon text-info me-2"></i>非工作时间活动
                                </h5>
                                <div class="table-responsive">
                                    <table class="table table-hover">
                                        <thead class="table-light">
                                            <tr>
                                                <th>时间</th>
                                                <th>操作</th>
                                                <th>资源类型</th>
                                                <th>IP地址</th>
                                                <th>详情</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="log" items="${reportData.afterHoursActivity}" varStatus="status">
                                                <c:if test="${status.index < 20}">
                                                    <tr>
                                                        <td>
                                                            <fmt:formatDate value="${log.createdAt}" 
                                                                          pattern="MM-dd HH:mm:ss"/>
                                                        </td>
                                                        <td>
                                                            <span class="badge bg-primary">${log.action}</span>
                                                        </td>
                                                        <td>
                                                            <span class="badge bg-secondary">${log.resourceType}</span>
                                                        </td>
                                                        <td><code>${log.ipAddress}</code></td>
                                                        <td class="text-truncate" style="max-width: 300px;">
                                                            ${log.details}
                                                        </td>
                                                    </tr>
                                                </c:if>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                                <c:if test="${reportData.afterHoursActivity.size() > 20}">
                                    <div class="text-center mt-3">
                                        <small class="text-muted">
                                            显示最近20条记录，共${reportData.afterHoursActivity.size()}条
                                        </small>
                                    </div>
                                </c:if>
                            </div>
                        </c:if>
                    </c:if>

                    <!-- 管理员报告 -->
                    <c:if test="${reportType == 'admin'}">
                        <c:if test="${not empty reportData.adminReports}">
                            <c:forEach var="entry" items="${reportData.adminReports}">
                                <div class="report-card">
                                    <div class="row">
                                        <div class="col-md-8">
                                            <h5 class="mb-3">
                                                <i class="fas fa-user-cog me-2"></i>
                                                ${entry.value.admin.fullName} (${entry.value.admin.username})
                                            </h5>
                                            <div class="row">
                                                <div class="col-md-3">
                                                    <div class="stat-card">
                                                        <div class="stat-number">${entry.value.totalOperations}</div>
                                                        <div class="text-muted">总操作数</div>
                                                    </div>
                                                </div>
                                                <div class="col-md-3">
                                                    <div class="stat-card">
                                                        <div class="stat-number text-success">${entry.value.successfulLogins}</div>
                                                        <div class="text-muted">成功登录</div>
                                                    </div>
                                                </div>
                                                <div class="col-md-3">
                                                    <div class="stat-card">
                                                        <div class="stat-number text-info">${entry.value.loginAttempts}</div>
                                                        <div class="text-muted">登录尝试</div>
                                                    </div>
                                                </div>
                                                <div class="col-md-3">
                                                    <div class="stat-card">
                                                        <div class="stat-number">
                                                            <span class="badge bg-secondary">${entry.value.admin.role}</span>
                                                        </div>
                                                        <div class="text-muted">角色</div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                        <div class="col-md-4">
                                            <h6 class="mb-3">操作类型分布</h6>
                                            <c:forEach var="actionEntry" items="${entry.value.actionDistribution}">
                                                <div class="d-flex justify-content-between align-items-center mb-2">
                                                    <span class="badge bg-primary">${actionEntry.key}</span>
                                                    <span>${actionEntry.value}次</span>
                                                </div>
                                            </c:forEach>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:if>
                    </c:if>

                    <!-- 错误提示 -->
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger" role="alert">
                            <i class="fas fa-exclamation-triangle me-2"></i>
                            ${error}
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>

    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    
    <script>
        // 图表配置
        Chart.defaults.font.family = "'Segoe UI', system-ui, sans-serif";
        Chart.defaults.font.size = 12;
        
        // 操作类型分布图表
        <c:if test="${reportType == null || reportType == 'overview'}">
            <c:if test="${not empty reportData.actionStats}">
                const actionCtx = document.getElementById('actionChart').getContext('2d');
                new Chart(actionCtx, {
                    type: 'doughnut',
                    data: {
                        labels: [<c:forEach var="entry" items="${reportData.actionStats}" varStatus="status">'${entry.key}'<c:if test="${!status.last}">,</c:if></c:forEach>],
                        datasets: [{
                            data: [<c:forEach var="entry" items="${reportData.actionStats}" varStatus="status">${entry.value}<c:if test="${!status.last}">,</c:if></c:forEach>],
                            backgroundColor: [
                                '#007bff', '#28a745', '#dc3545', '#ffc107', '#17a2b8', 
                                '#6f42c1', '#e83e8c', '#fd7e14', '#20c997', '#6c757d'
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
            </c:if>
            
            // 资源类型分布图表
            <c:if test="${not empty reportData.resourceStats}">
                const resourceCtx = document.getElementById('resourceChart').getContext('2d');
                new Chart(resourceCtx, {
                    type: 'pie',
                    data: {
                        labels: [<c:forEach var="entry" items="${reportData.resourceStats}" varStatus="status">'${entry.key}'<c:if test="${!status.last}">,</c:if></c:forEach>],
                        datasets: [{
                            data: [<c:forEach var="entry" items="${reportData.resourceStats}" varStatus="status">${entry.value}<c:if test="${!status.last}">,</c:if></c:forEach>],
                            backgroundColor: [
                                '#28a745', '#007bff', '#dc3545', '#ffc107', '#17a2b8',
                                '#6f42c1', '#e83e8c', '#fd7e14', '#20c997', '#6c757d'
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
            </c:if>
            
            // 每日活动趋势图表
            <c:if test="${not empty reportData.dailyStats}">
                const dailyCtx = document.getElementById('dailyTrendChart').getContext('2d');
                new Chart(dailyCtx, {
                    type: 'line',
                    data: {
                        labels: [<c:forEach var="entry" items="${reportData.dailyStats}" varStatus="status">'${entry.key}'<c:if test="${!status.last}">,</c:if></c:forEach>],
                        datasets: [{
                            label: '日志数量',
                            data: [<c:forEach var="entry" items="${reportData.dailyStats}" varStatus="status">${entry.value}<c:if test="${!status.last}">,</c:if></c:forEach>],
                            borderColor: '#007bff',
                            backgroundColor: 'rgba(0, 123, 255, 0.1)',
                            fill: true,
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
                        }
                    }
                });
            </c:if>
        </c:if>
        
        // 活动报告图表
        <c:if test="${reportType == 'activity'}">
            // 管理员活动统计
            <c:if test="${not empty reportData.adminActivityStats}">
                const adminActivityCtx = document.getElementById('adminActivityChart').getContext('2d');
                new Chart(adminActivityCtx, {
                    type: 'bar',
                    data: {
                        labels: [<c:forEach var="entry" items="${reportData.adminDetails}" varStatus="status">'${entry.value.fullName}'<c:if test="${!status.last}">,</c:if></c:forEach>],
                        datasets: [{
                            label: '活动次数',
                            data: [<c:forEach var="entry" items="${reportData.adminDetails}" varStatus="status">${entry.value.activityCount}<c:if test="${!status.last}">,</c:if></c:forEach>],
                            backgroundColor: '#007bff'
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
            </c:if>
            
            // 时间段活动分布
            <c:if test="${not empty reportData.hourlyActivity}">
                const hourlyCtx = document.getElementById('hourlyActivityChart').getContext('2d');
                new Chart(hourlyCtx, {
                    type: 'line',
                    data: {
                        labels: [<c:forEach var="entry" items="${reportData.hourlyActivity}" varStatus="status">'${entry.key}:00'<c:if test="${!status.last}">,</c:if></c:forEach>],
                        datasets: [{
                            label: '活动数量',
                            data: [<c:forEach var="entry" items="${reportData.hourlyActivity}" varStatus="status">${entry.value}<c:if test="${!status.last}">,</c:if></c:forEach>],
                            borderColor: '#28a745',
                            backgroundColor: 'rgba(40, 167, 69, 0.1)',
                            fill: true
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
            </c:if>
        </c:if>
        
        // 导出报告函数
        function exportReport() {
            const reportType = '${reportType}' || 'overview';
            const startDate = '${startDate}';
            const endDate = '${endDate}';
            
            const url = `/admin/audit/reports?action=export&reportType=${reportType}&format=csv&startDate=${startDate}&endDate=${endDate}`;
            window.open(url, '_blank');
        }
    </script>
</body>
</html>
