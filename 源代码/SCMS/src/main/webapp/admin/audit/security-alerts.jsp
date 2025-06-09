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
    <title>安全警报 - SCMS审计管理</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        .alert-card {
            transition: all 0.3s ease;
            border-left: 4px solid transparent;
        }
        .alert-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
        }
        .alert-high { border-left-color: #dc3545; }
        .alert-medium { border-left-color: #ffc107; }
        .alert-low { border-left-color: #28a745; }
        
        .severity-badge {
            font-size: 0.8rem;
            padding: 0.3rem 0.6rem;
        }
        
        .alert-status {
            cursor: pointer;
        }
        
        .search-section {
            background-color: #f8f9fa;
            border-radius: 10px;
            padding: 1.5rem;
            margin-bottom: 2rem;
        }
        
        .alert-item {
            background: white;
            border-radius: 8px;
            padding: 1.5rem;
            margin-bottom: 1rem;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            transition: all 0.3s ease;
        }
        
        .alert-item:hover {
            box-shadow: 0 4px 8px rgba(0,0,0,0.15);
        }
        
        .alert-header {
            display: flex;
            justify-content: between;
            align-items: center;
            margin-bottom: 1rem;
        }
        
        .alert-actions {
            opacity: 0;
            transition: opacity 0.3s ease;
        }
        
        .alert-item:hover .alert-actions {
            opacity: 1;
        }
        
        .bulk-actions {
            background: white;
            border-radius: 8px;
            padding: 1rem;
            margin-bottom: 1rem;
            border: 1px solid #dee2e6;
        }
    </style>
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
        <div class="container-fluid">
            <a class="navbar-brand" href="#">
                <i class="bi bi-bell"></i> SCMS 安全警报
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
                    <h2><i class="bi bi-bell"></i> 安全警报管理</h2>
                    <div>
                        <button class="btn btn-outline-primary" onclick="refreshAlerts()">
                            <i class="bi bi-arrow-clockwise"></i> 刷新
                        </button>
                        <button class="btn btn-outline-success" onclick="exportAlerts()">
                            <i class="bi bi-download"></i> 导出
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 警报统计 -->
        <div class="row mb-4">
            <div class="col-lg-3 col-md-6 mb-3">
                <div class="card alert-card alert-high">
                    <div class="card-body text-center">
                        <h5 class="card-title text-danger">高危警报</h5>
                        <h3 id="highAlerts">--</h3>
                        <small class="text-muted">需要立即处理</small>
                    </div>
                </div>
            </div>
            <div class="col-lg-3 col-md-6 mb-3">
                <div class="card alert-card alert-medium">
                    <div class="card-body text-center">
                        <h5 class="card-title text-warning">中危警报</h5>
                        <h3 id="mediumAlerts">--</h3>
                        <small class="text-muted">需要关注</small>
                    </div>
                </div>
            </div>
            <div class="col-lg-3 col-md-6 mb-3">
                <div class="card alert-card alert-low">
                    <div class="card-body text-center">
                        <h5 class="card-title text-success">低危警报</h5>
                        <h3 id="lowAlerts">--</h3>
                        <small class="text-muted">信息提醒</small>
                    </div>
                </div>
            </div>
            <div class="col-lg-3 col-md-6 mb-3">
                <div class="card alert-card">
                    <div class="card-body text-center">
                        <h5 class="card-title">已处理</h5>
                        <h3 id="resolvedAlerts">--</h3>
                        <small class="text-muted">本月处理数量</small>
                    </div>
                </div>
            </div>
        </div>

        <!-- 搜索和筛选 -->
        <div class="search-section">
            <form id="searchForm">
                <div class="row">
                    <div class="col-md-3 mb-3">
                        <label class="form-label">威胁等级</label>
                        <select class="form-select" name="severity">
                            <option value="">全部</option>
                            <option value="HIGH">高危</option>
                            <option value="MEDIUM">中危</option>
                            <option value="LOW">低危</option>
                        </select>
                    </div>
                    <div class="col-md-3 mb-3">
                        <label class="form-label">警报状态</label>
                        <select class="form-select" name="status">
                            <option value="">全部</option>
                            <option value="OPEN">待处理</option>
                            <option value="INVESTIGATING">调查中</option>
                            <option value="RESOLVED">已处理</option>
                            <option value="FALSE_POSITIVE">误报</option>
                        </select>
                    </div>
                    <div class="col-md-3 mb-3">
                        <label class="form-label">威胁类型</label>
                        <select class="form-select" name="type">
                            <option value="">全部</option>
                            <option value="BRUTE_FORCE">暴力破解</option>
                            <option value="PRIVILEGE_ABUSE">权限滥用</option>
                            <option value="SUSPICIOUS_LOGIN">可疑登录</option>
                            <option value="DATA_BREACH">数据泄露</option>
                            <option value="AFTER_HOURS">非工作时间活动</option>
                        </select>
                    </div>
                    <div class="col-md-3 mb-3">
                        <label class="form-label">IP地址</label>
                        <input type="text" class="form-control" name="ipAddress" placeholder="输入IP地址">
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-6 mb-3">
                        <label class="form-label">时间范围</label>
                        <div class="row">
                            <div class="col-6">
                                <input type="date" class="form-control" name="startDate">
                            </div>
                            <div class="col-6">
                                <input type="date" class="form-control" name="endDate">
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6 mb-3">
                        <label class="form-label">关键词</label>
                        <input type="text" class="form-control" name="keyword" placeholder="搜索警报描述...">
                    </div>
                </div>
                <div class="row">
                    <div class="col-12">
                        <button type="submit" class="btn btn-primary">
                            <i class="bi bi-search"></i> 搜索
                        </button>
                        <button type="button" class="btn btn-secondary" onclick="resetSearch()">
                            <i class="bi bi-arrow-clockwise"></i> 重置
                        </button>
                    </div>
                </div>
            </form>
        </div>

        <!-- 批量操作 -->
        <div class="bulk-actions" id="bulkActions" style="display: none;">
            <div class="d-flex justify-content-between align-items-center">
                <span>已选择 <strong id="selectedCount">0</strong> 个警报</span>
                <div>
                    <button class="btn btn-sm btn-success" onclick="bulkMarkResolved()">
                        <i class="bi bi-check-circle"></i> 标记为已处理
                    </button>
                    <button class="btn btn-sm btn-warning" onclick="bulkMarkInvestigating()">
                        <i class="bi bi-search"></i> 标记为调查中
                    </button>
                    <button class="btn btn-sm btn-secondary" onclick="bulkMarkFalsePositive()">
                        <i class="bi bi-x-circle"></i> 标记为误报
                    </button>
                    <button class="btn btn-sm btn-outline-secondary" onclick="clearSelection()">
                        取消选择
                    </button>
                </div>
            </div>
        </div>

        <!-- 警报列表 -->
        <div id="alertsList">
            <div class="text-center py-5">
                <i class="bi bi-hourglass-split fs-1 text-muted"></i>
                <p class="text-muted">正在加载安全警报...</p>
            </div>
        </div>

        <!-- 分页 -->
        <div class="d-flex justify-content-center mt-4">
            <nav>
                <ul class="pagination" id="pagination">
                </ul>
            </nav>
        </div>
    </div>

    <!-- 警报详情模态框 -->
    <div class="modal fade" id="alertDetailModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">警报详情</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body" id="alertDetailContent">
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>
                    <button type="button" class="btn btn-primary" onclick="updateAlertStatus()">更新状态</button>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        let currentPage = 1;
        let selectedAlerts = new Set();
        let currentAlert = null;

        // 页面加载时初始化
        document.addEventListener('DOMContentLoaded', function() {
            loadAlertStats();
            loadAlerts();
            
            // 搜索表单提交
            document.getElementById('searchForm').addEventListener('submit', function(e) {
                e.preventDefault();
                currentPage = 1;
                loadAlerts();
            });
        });

        // 加载警报统计
        async function loadAlertStats() {
            try {
                const response = await fetch('/SCMS/admin/audit/security/alerts/stats');
                const data = await response.json();
                
                document.getElementById('highAlerts').textContent = data.highCount || 0;
                document.getElementById('mediumAlerts').textContent = data.mediumCount || 0;
                document.getElementById('lowAlerts').textContent = data.lowCount || 0;
                document.getElementById('resolvedAlerts').textContent = data.resolvedCount || 0;
            } catch (error) {
                console.error('加载警报统计失败:', error);
            }
        }

        // 加载警报列表
        async function loadAlerts() {
            try {
                const formData = new FormData(document.getElementById('searchForm'));
                const params = new URLSearchParams();
                
                // 添加搜索参数
                for (let [key, value] of formData.entries()) {
                    if (value) params.append(key, value);
                }
                params.append('page', currentPage);
                params.append('size', 10);

                const response = await fetch(`/SCMS/admin/audit/security/alerts?${params}`);
                const data = await response.json();
                
                renderAlerts(data.alerts);
                renderPagination(data.totalPages, data.currentPage);
            } catch (error) {
                console.error('加载警报列表失败:', error);
                document.getElementById('alertsList').innerHTML = `
                    <div class="text-center py-5">
                        <i class="bi bi-exclamation-triangle fs-1 text-danger"></i>
                        <p class="text-danger">加载警报失败，请稍后重试</p>
                    </div>
                `;
            }
        }

        // 渲染警报列表
        function renderAlerts(alerts) {
            const container = document.getElementById('alertsList');
            
            if (!alerts || alerts.length === 0) {
                container.innerHTML = `
                    <div class="text-center py-5">
                        <i class="bi bi-check-circle fs-1 text-success"></i>
                        <p class="text-muted">暂无安全警报</p>
                    </div>
                `;
                return;
            }

            container.innerHTML = alerts.map(alert => `
                <div class="alert-item" data-alert-id="${alert.id}">
                    <div class="alert-header">
                        <div class="d-flex align-items-center">
                            <input type="checkbox" class="form-check-input me-3" 
                                   onchange="toggleSelection('${alert.id}')" 
                                   ${selectedAlerts.has(alert.id) ? 'checked' : ''}>
                            <span class="severity-badge badge bg-${getSeverityColor(alert.severity)}">
                                ${getSeverityText(alert.severity)}
                            </span>
                            <h6 class="ms-3 mb-0">${alert.type}</h6>
                        </div>
                        <div class="alert-actions">
                            <button class="btn btn-sm btn-outline-info" onclick="viewAlert('${alert.id}')">
                                <i class="bi bi-eye"></i>
                            </button>
                            <div class="btn-group" role="group">
                                <button class="btn btn-sm btn-outline-secondary dropdown-toggle" 
                                        data-bs-toggle="dropdown">
                                    状态
                                </button>
                                <ul class="dropdown-menu">
                                    <li><a class="dropdown-item" onclick="updateStatus('${alert.id}', 'INVESTIGATING')">
                                        调查中</a></li>
                                    <li><a class="dropdown-item" onclick="updateStatus('${alert.id}', 'RESOLVED')">
                                        已处理</a></li>
                                    <li><a class="dropdown-item" onclick="updateStatus('${alert.id}', 'FALSE_POSITIVE')">
                                        误报</a></li>
                                </ul>
                            </div>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col-md-8">
                            <p class="mb-2">${alert.description}</p>
                            <small class="text-muted">
                                <i class="bi bi-clock"></i> ${formatDate(alert.timestamp)} | 
                                <i class="bi bi-geo-alt"></i> ${alert.ipAddress}
                            </small>
                        </div>
                        <div class="col-md-4 text-end">
                            <span class="badge bg-${getStatusColor(alert.status)}">
                                ${getStatusText(alert.status)}
                            </span>
                            ${alert.riskScore ? `<br><small class="text-muted">风险评分: ${alert.riskScore}</small>` : ''}
                        </div>
                    </div>
                </div>
            `).join('');
        }

        // 渲染分页
        function renderPagination(totalPages, current) {
            const pagination = document.getElementById('pagination');
            
            if (totalPages <= 1) {
                pagination.innerHTML = '';
                return;
            }

            let html = '';
            
            // 上一页
            html += `
                <li class="page-item ${current <= 1 ? 'disabled' : ''}">
                    <a class="page-link" href="#" onclick="changePage(${current - 1})">上一页</a>
                </li>
            `;
            
            // 页码
            for (let i = 1; i <= totalPages; i++) {
                if (i === current || i === 1 || i === totalPages || 
                    (i >= current - 2 && i <= current + 2)) {
                    html += `
                        <li class="page-item ${i === current ? 'active' : ''}">
                            <a class="page-link" href="#" onclick="changePage(${i})">${i}</a>
                        </li>
                    `;
                } else if (i === current - 3 || i === current + 3) {
                    html += '<li class="page-item disabled"><span class="page-link">...</span></li>';
                }
            }
            
            // 下一页
            html += `
                <li class="page-item ${current >= totalPages ? 'disabled' : ''}">
                    <a class="page-link" href="#" onclick="changePage(${current + 1})">下一页</a>
                </li>
            `;
            
            pagination.innerHTML = html;
        }

        // 切换页面
        function changePage(page) {
            currentPage = page;
            loadAlerts();
        }

        // 切换选择
        function toggleSelection(alertId) {
            if (selectedAlerts.has(alertId)) {
                selectedAlerts.delete(alertId);
            } else {
                selectedAlerts.add(alertId);
            }
            updateBulkActions();
        }

        // 更新批量操作显示
        function updateBulkActions() {
            const bulkActions = document.getElementById('bulkActions');
            const selectedCount = document.getElementById('selectedCount');
            
            if (selectedAlerts.size > 0) {
                bulkActions.style.display = 'block';
                selectedCount.textContent = selectedAlerts.size;
            } else {
                bulkActions.style.display = 'none';
            }
        }

        // 清除选择
        function clearSelection() {
            selectedAlerts.clear();
            document.querySelectorAll('.form-check-input').forEach(cb => cb.checked = false);
            updateBulkActions();
        }

        // 查看警报详情
        async function viewAlert(alertId) {
            try {
                const response = await fetch(`/SCMS/admin/audit/security/alerts/${alertId}`);
                const alert = await response.json();
                
                currentAlert = alert;
                
                document.getElementById('alertDetailContent').innerHTML = `
                    <div class="row">
                        <div class="col-md-6">
                            <h6>基本信息</h6>
                            <table class="table table-sm">
                                <tr><td>警报ID:</td><td>${alert.id}</td></tr>
                                <tr><td>威胁等级:</td><td>
                                    <span class="badge bg-${getSeverityColor(alert.severity)}">
                                        ${getSeverityText(alert.severity)}
                                    </span>
                                </td></tr>
                                <tr><td>威胁类型:</td><td>${alert.type}</td></tr>
                                <tr><td>状态:</td><td>
                                    <span class="badge bg-${getStatusColor(alert.status)}">
                                        ${getStatusText(alert.status)}
                                    </span>
                                </td></tr>
                                <tr><td>风险评分:</td><td>${alert.riskScore || 'N/A'}</td></tr>
                            </table>
                        </div>
                        <div class="col-md-6">
                            <h6>网络信息</h6>
                            <table class="table table-sm">
                                <tr><td>IP地址:</td><td><code>${alert.ipAddress}</code></td></tr>
                                <tr><td>用户代理:</td><td>${alert.userAgent || 'N/A'}</td></tr>
                                <tr><td>地理位置:</td><td>${alert.location || 'N/A'}</td></tr>
                                <tr><td>检测时间:</td><td>${formatDate(alert.timestamp)}</td></tr>
                            </table>
                        </div>
                    </div>
                    <div class="row mt-3">
                        <div class="col-12">
                            <h6>详细描述</h6>
                            <p>${alert.description}</p>
                        </div>
                    </div>
                    ${alert.evidence ? `
                        <div class="row mt-3">
                            <div class="col-12">
                                <h6>证据信息</h6>
                                <pre class="bg-light p-3 rounded">${JSON.stringify(alert.evidence, null, 2)}</pre>
                            </div>
                        </div>
                    ` : ''}
                    <div class="row mt-3">
                        <div class="col-12">
                            <h6>更新状态</h6>
                            <select class="form-select" id="newStatus">
                                <option value="OPEN" ${alert.status === 'OPEN' ? 'selected' : ''}>待处理</option>
                                <option value="INVESTIGATING" ${alert.status === 'INVESTIGATING' ? 'selected' : ''}>调查中</option>
                                <option value="RESOLVED" ${alert.status === 'RESOLVED' ? 'selected' : ''}>已处理</option>
                                <option value="FALSE_POSITIVE" ${alert.status === 'FALSE_POSITIVE' ? 'selected' : ''}>误报</option>
                            </select>
                            <div class="mt-2">
                                <textarea class="form-control" id="statusComment" 
                                          placeholder="添加处理备注..."></textarea>
                            </div>
                        </div>
                    </div>
                `;
                
                new bootstrap.Modal(document.getElementById('alertDetailModal')).show();
            } catch (error) {
                console.error('加载警报详情失败:', error);
                alert('加载警报详情失败');
            }
        }

        // 更新警报状态
        async function updateAlertStatus() {
            if (!currentAlert) return;
            
            const newStatus = document.getElementById('newStatus').value;
            const comment = document.getElementById('statusComment').value;
            
            try {
                const response = await fetch(`/SCMS/admin/audit/security/alerts/${currentAlert.id}/status`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        status: newStatus,
                        comment: comment
                    })
                });
                
                if (response.ok) {
                    bootstrap.Modal.getInstance(document.getElementById('alertDetailModal')).hide();
                    loadAlerts();
                    loadAlertStats();
                } else {
                    alert('更新状态失败');
                }
            } catch (error) {
                console.error('更新状态失败:', error);
                alert('更新状态失败');
            }
        }

        // 单个更新状态
        async function updateStatus(alertId, status) {
            try {
                const response = await fetch(`/SCMS/admin/audit/security/alerts/${alertId}/status`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ status: status })
                });
                
                if (response.ok) {
                    loadAlerts();
                    loadAlertStats();
                } else {
                    alert('更新状态失败');
                }
            } catch (error) {
                console.error('更新状态失败:', error);
                alert('更新状态失败');
            }
        }

        // 批量标记为已处理
        function bulkMarkResolved() {
            bulkUpdateStatus('RESOLVED');
        }

        // 批量标记为调查中
        function bulkMarkInvestigating() {
            bulkUpdateStatus('INVESTIGATING');
        }

        // 批量标记为误报
        function bulkMarkFalsePositive() {
            bulkUpdateStatus('FALSE_POSITIVE');
        }

        // 批量更新状态
        async function bulkUpdateStatus(status) {
            if (selectedAlerts.size === 0) return;
            
            try {
                const response = await fetch('/SCMS/admin/audit/security/alerts/bulk-update', {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        alertIds: Array.from(selectedAlerts),
                        status: status
                    })
                });
                
                if (response.ok) {
                    clearSelection();
                    loadAlerts();
                    loadAlertStats();
                } else {
                    alert('批量更新失败');
                }
            } catch (error) {
                console.error('批量更新失败:', error);
                alert('批量更新失败');
            }
        }

        // 刷新警报
        function refreshAlerts() {
            loadAlertStats();
            loadAlerts();
        }

        // 导出警报
        function exportAlerts() {
            const formData = new FormData(document.getElementById('searchForm'));
            const params = new URLSearchParams();
            
            for (let [key, value] of formData.entries()) {
                if (value) params.append(key, value);
            }
            
            window.open(`/SCMS/admin/audit/security/alerts/export?${params}`, '_blank');
        }

        // 重置搜索
        function resetSearch() {
            document.getElementById('searchForm').reset();
            currentPage = 1;
            loadAlerts();
        }

        // 工具函数
        function getSeverityColor(severity) {
            switch (severity) {
                case 'HIGH': return 'danger';
                case 'MEDIUM': return 'warning';
                case 'LOW': return 'success';
                default: return 'secondary';
            }
        }

        function getSeverityText(severity) {
            switch (severity) {
                case 'HIGH': return '高危';
                case 'MEDIUM': return '中危';
                case 'LOW': return '低危';
                default: return '未知';
            }
        }

        function getStatusColor(status) {
            switch (status) {
                case 'OPEN': return 'danger';
                case 'INVESTIGATING': return 'warning';
                case 'RESOLVED': return 'success';
                case 'FALSE_POSITIVE': return 'secondary';
                default: return 'secondary';
            }
        }

        function getStatusText(status) {
            switch (status) {
                case 'OPEN': return '待处理';
                case 'INVESTIGATING': return '调查中';
                case 'RESOLVED': return '已处理';
                case 'FALSE_POSITIVE': return '误报';
                default: return '未知';
            }
        }

        function formatDate(timestamp) {
            const date = new Date(timestamp);
            return date.toLocaleString('zh-CN');
        }
    </script>
</body>
</html>
