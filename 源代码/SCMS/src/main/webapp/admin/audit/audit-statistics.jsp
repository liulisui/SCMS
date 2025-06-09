<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>审计日志统计分析 - SCMS</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
        .sidebar {
            min-height: 100vh;
            background-color: #343a40;
        }
        .sidebar .nav-link {
            color: #adb5bd;
        }
        .sidebar .nav-link:hover,
        .sidebar .nav-link.active {
            color: #fff;
            background-color: #495057;
        }
        .stat-card {
            border-left: 4px solid #007bff;
            transition: transform 0.2s;
        }
        .stat-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
        }
        .chart-container {
            position: relative;
            height: 400px;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <div class="container-fluid">
        <div class="row">
            <!-- 侧边栏 -->
            <div class="col-md-2 sidebar">
                <div class="d-flex flex-column p-3">
                    <h5 class="text-white mb-3">
                        <i class="bi bi-shield-check"></i> 审计管理
                    </h5>
                    <ul class="nav nav-pills flex-column mb-auto">
                        <li class="nav-item">
                            <a href="${pageContext.request.contextPath}/admin/enhanced-audit?action=list" 
                               class="nav-link">
                                <i class="bi bi-list-ul"></i> 审计日志
                            </a>
                        </li>
                        <li class="nav-item">
                            <a href="${pageContext.request.contextPath}/admin/enhanced-audit?action=search" 
                               class="nav-link">
                                <i class="bi bi-search"></i> 高级搜索
                            </a>
                        </li>
                        <li class="nav-item">
                            <a href="${pageContext.request.contextPath}/admin/enhanced-audit?action=statistics" 
                               class="nav-link active">
                                <i class="bi bi-graph-up"></i> 统计分析
                            </a>
                        </li>
                        <li class="nav-item">
                            <a href="${pageContext.request.contextPath}/admin/enhanced-audit?action=user-activity" 
                               class="nav-link">
                                <i class="bi bi-person-lines-fill"></i> 用户活动
                            </a>
                        </li>
                        <li class="nav-item">
                            <a href="${pageContext.request.contextPath}/admin/enhanced-audit?action=security-alerts" 
                               class="nav-link">
                                <i class="bi bi-exclamation-triangle"></i> 安全告警
                            </a>
                        </li>
                        <li class="nav-item mt-3">
                            <a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-link">
                                <i class="bi bi-arrow-left"></i> 返回管理后台
                            </a>
                        </li>
                    </ul>
                </div>
            </div>

            <!-- 主内容区 -->
            <div class="col-md-10">
                <div class="p-4">
                    <h2><i class="bi bi-graph-up"></i> 审计日志统计分析</h2>
                    
                    <!-- 错误信息 -->
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <i class="bi bi-exclamation-triangle"></i> ${error}
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    </c:if>

                    <!-- 概览统计 -->
                    <div class="row mb-4">
                        <div class="col-md-6">
                            <div class="card stat-card">
                                <div class="card-body">
                                    <div class="d-flex justify-content-between">
                                        <div>
                                            <h6 class="card-subtitle text-muted">总日志数量</h6>
                                            <h2 class="card-title text-primary">${statistics.totalLogs}</h2>
                                            <small class="text-muted">系统启动以来的所有审计日志</small>
                                        </div>
                                        <div class="align-self-center">
                                            <i class="bi bi-journal-text text-primary" style="font-size: 3rem;"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="card stat-card">
                                <div class="card-body">
                                    <div class="d-flex justify-content-between">
                                        <div>
                                            <h6 class="card-subtitle text-muted">今日日志数量</h6>
                                            <h2 class="card-title text-success">${statistics.todayLogs}</h2>
                                            <small class="text-muted">今天产生的审计日志数量</small>
                                        </div>
                                        <div class="align-self-center">
                                            <i class="bi bi-calendar-day text-success" style="font-size: 3rem;"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 图表分析 -->
                    <div class="row">
                        <!-- 操作类型统计 -->
                        <div class="col-md-6">
                            <div class="card">
                                <div class="card-header">
                                    <h6 class="mb-0"><i class="bi bi-pie-chart"></i> 操作类型分布</h6>
                                </div>
                                <div class="card-body">
                                    <div class="chart-container">
                                        <canvas id="actionTypeChart"></canvas>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- 表操作统计 -->
                        <div class="col-md-6">
                            <div class="card">
                                <div class="card-header">
                                    <h6 class="mb-0"><i class="bi bi-bar-chart"></i> 数据表操作频率</h6>
                                </div>
                                <div class="card-body">
                                    <div class="chart-container">
                                        <canvas id="tableStatsChart"></canvas>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 最活跃用户 -->
                    <div class="row mt-4">
                        <div class="col-12">
                            <div class="card">
                                <div class="card-header">
                                    <h6 class="mb-0"><i class="bi bi-people"></i> 最活跃用户（Top 10）</h6>
                                </div>
                                <div class="card-body">
                                    <div class="table-responsive">
                                        <table class="table table-hover">
                                            <thead class="table-light">
                                                <tr>
                                                    <th>排名</th>
                                                    <th>用户名</th>
                                                    <th>真实姓名</th>
                                                    <th>操作次数</th>
                                                    <th>活跃度</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach items="${statistics.userStats}" var="userStat" varStatus="status">
                                                    <tr>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${status.index == 0}">
                                                                    <span class="badge bg-warning"><i class="bi bi-trophy"></i> ${status.index + 1}</span>
                                                                </c:when>
                                                                <c:when test="${status.index == 1}">
                                                                    <span class="badge bg-secondary"><i class="bi bi-award"></i> ${status.index + 1}</span>
                                                                </c:when>
                                                                <c:when test="${status.index == 2}">
                                                                    <span class="badge bg-warning"><i class="bi bi-award"></i> ${status.index + 1}</span>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="badge bg-light text-dark">${status.index + 1}</span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td>
                                                            <strong>${userStat.username}</strong>
                                                        </td>
                                                        <td>${userStat.realName != null ? userStat.realName : '-'}</td>
                                                        <td>
                                                            <span class="badge bg-primary">${userStat.count}</span>
                                                        </td>
                                                        <td>
                                                            <div class="progress" style="height: 20px;">
                                                                <c:set var="maxCount" value="${statistics.userStats[0].count}" />
                                                                <c:set var="percentage" value="${(userStat.count * 100) / maxCount}" />
                                                                <div class="progress-bar" role="progressbar" 
                                                                     style="width: ${percentage}%"
                                                                     aria-valuenow="${percentage}" aria-valuemin="0" aria-valuemax="100">
                                                                    <fmt:formatNumber value="${percentage}" maxFractionDigits="1"/>%
                                                                </div>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 详细统计表格 -->
                    <div class="row mt-4">
                        <div class="col-md-6">
                            <div class="card">
                                <div class="card-header">
                                    <h6 class="mb-0"><i class="bi bi-list-ol"></i> 操作类型详细统计</h6>
                                </div>
                                <div class="card-body">
                                    <div class="table-responsive">
                                        <table class="table table-sm">
                                            <thead>
                                                <tr>
                                                    <th>操作类型</th>
                                                    <th>次数</th>
                                                    <th>占比</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach items="${statistics.actionStats}" var="entry">
                                                    <tr>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${entry.key == 'LOGIN'}">
                                                                    <span class="badge bg-success">登录</span>
                                                                </c:when>
                                                                <c:when test="${entry.key == 'LOGOUT'}">
                                                                    <span class="badge bg-secondary">登出</span>
                                                                </c:when>
                                                                <c:when test="${entry.key == 'CREATE'}">
                                                                    <span class="badge bg-primary">创建</span>
                                                                </c:when>
                                                                <c:when test="${entry.key == 'UPDATE'}">
                                                                    <span class="badge bg-warning">更新</span>
                                                                </c:when>
                                                                <c:when test="${entry.key == 'DELETE'}">
                                                                    <span class="badge bg-danger">删除</span>
                                                                </c:when>
                                                                <c:when test="${entry.key == 'VIEW'}">
                                                                    <span class="badge bg-info">查看</span>
                                                                </c:when>
                                                                <c:when test="${entry.key == 'QUERY'}">
                                                                    <span class="badge bg-light text-dark">查询</span>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="badge bg-secondary">${entry.key}</span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td><strong>${entry.value}</strong></td>
                                                        <td>
                                                            <c:set var="percentage" value="${(entry.value * 100.0) / statistics.totalLogs}" />
                                                            <fmt:formatNumber value="${percentage}" maxFractionDigits="2"/>%
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="col-md-6">
                            <div class="card">
                                <div class="card-header">
                                    <h6 class="mb-0"><i class="bi bi-table"></i> 数据表操作统计</h6>
                                </div>
                                <div class="card-body">
                                    <div class="table-responsive">
                                        <table class="table table-sm">
                                            <thead>
                                                <tr>
                                                    <th>表名</th>
                                                    <th>操作次数</th>
                                                    <th>占比</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach items="${statistics.tableStats}" var="entry">
                                                    <tr>
                                                        <td><code>${entry.key}</code></td>
                                                        <td><strong>${entry.value}</strong></td>
                                                        <td>
                                                            <c:set var="percentage" value="${(entry.value * 100.0) / statistics.totalLogs}" />
                                                            <fmt:formatNumber value="${percentage}" maxFractionDigits="2"/>%
                                                        </td>
                                                    </tr>
                                                </c:forEach>
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
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // 操作类型饼图
        const actionTypeCtx = document.getElementById('actionTypeChart').getContext('2d');
        const actionTypeData = {
            labels: [
                <c:forEach items="${statistics.actionStats}" var="entry" varStatus="status">
                    '${entry.key}'<c:if test="${!status.last}">,</c:if>
                </c:forEach>
            ],
            datasets: [{
                data: [
                    <c:forEach items="${statistics.actionStats}" var="entry" varStatus="status">
                        ${entry.value}<c:if test="${!status.last}">,</c:if>
                    </c:forEach>
                ],
                backgroundColor: [
                    '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0',
                    '#9966FF', '#FF9F40', '#FF6384', '#C9CBCF',
                    '#4BC0C0', '#FF6384'
                ]
            }]
        };

        new Chart(actionTypeCtx, {
            type: 'pie',
            data: actionTypeData,
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

        // 表操作柱状图
        const tableStatsCtx = document.getElementById('tableStatsChart').getContext('2d');
        const tableStatsData = {
            labels: [
                <c:forEach items="${statistics.tableStats}" var="entry" varStatus="status">
                    '${entry.key}'<c:if test="${!status.last}">,</c:if>
                </c:forEach>
            ],
            datasets: [{
                label: '操作次数',
                data: [
                    <c:forEach items="${statistics.tableStats}" var="entry" varStatus="status">
                        ${entry.value}<c:if test="${!status.last}">,</c:if>
                    </c:forEach>
                ],
                backgroundColor: 'rgba(54, 162, 235, 0.8)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1
            }]
        };

        new Chart(tableStatsCtx, {
            type: 'bar',
            data: tableStatsData,
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
                        display: false
                    }
                }
            }
        });
    </script>
</body>
</html>
