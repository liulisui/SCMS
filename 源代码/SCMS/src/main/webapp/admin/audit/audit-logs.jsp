<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>审计日志 - 供应链管理系统</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
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
        .search-card {
            background: white;
            border-radius: 10px;
            padding: 20px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }
        .table-container {
            background: white;
            border-radius: 10px;
            padding: 20px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        .log-detail {
            max-width: 300px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        .action-badge {
            font-size: 0.8em;
        }
        .pagination-container {
            background: white;
            border-radius: 10px;
            padding: 15px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            margin-top: 20px;
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
                    <a class="nav-link active" href="/admin/audit/logs">
                        <i class="fas fa-list-alt me-2"></i>审计日志
                    </a>
                    <a class="nav-link" href="/admin/audit/reports">
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
                            <i class="fas fa-list-alt me-2"></i>审计日志
                        </h2>
                        <div class="text-muted">
                            共 ${totalCount} 条记录
                        </div>
                    </div>

                    <!-- 搜索筛选 -->
                    <div class="search-card">
                        <form method="GET" action="/admin/audit/logs" class="row g-3">
                            <div class="col-md-3">
                                <label for="action" class="form-label">操作类型</label>
                                <select class="form-select" id="action" name="action">
                                    <option value="">全部操作</option>
                                    <option value="LOGIN_SUCCESS" ${param.action == 'LOGIN_SUCCESS' ? 'selected' : ''}>成功登录</option>
                                    <option value="LOGIN_FAILED" ${param.action == 'LOGIN_FAILED' ? 'selected' : ''}>失败登录</option>
                                    <option value="LOGOUT" ${param.action == 'LOGOUT' ? 'selected' : ''}>退出登录</option>
                                    <option value="CREATE" ${param.action == 'CREATE' ? 'selected' : ''}>创建</option>
                                    <option value="UPDATE" ${param.action == 'UPDATE' ? 'selected' : ''}>更新</option>
                                    <option value="DELETE" ${param.action == 'DELETE' ? 'selected' : ''}>删除</option>
                                    <option value="VIEW" ${param.action == 'VIEW' ? 'selected' : ''}>查看</option>
                                </select>
                            </div>
                            <div class="col-md-3">
                                <label for="resourceType" class="form-label">资源类型</label>
                                <select class="form-select" id="resourceType" name="resourceType">
                                    <option value="">全部资源</option>
                                    <option value="ADMINISTRATOR" ${param.resourceType == 'ADMINISTRATOR' ? 'selected' : ''}>管理员</option>
                                    <option value="USER" ${param.resourceType == 'USER' ? 'selected' : ''}>用户</option>
                                    <option value="DEPARTMENT" ${param.resourceType == 'DEPARTMENT' ? 'selected' : ''}>部门</option>
                                    <option value="SYSTEM" ${param.resourceType == 'SYSTEM' ? 'selected' : ''}>系统</option>
                                </select>
                            </div>
                            <div class="col-md-2">
                                <label for="startDate" class="form-label">开始日期</label>
                                <input type="date" class="form-control" id="startDate" name="startDate" 
                                       value="${param.startDate}">
                            </div>
                            <div class="col-md-2">
                                <label for="endDate" class="form-label">结束日期</label>
                                <input type="date" class="form-control" id="endDate" name="endDate" 
                                       value="${param.endDate}">
                            </div>
                            <div class="col-md-2">
                                <label for="keyword" class="form-label">关键词</label>
                                <input type="text" class="form-control" id="keyword" name="keyword" 
                                       placeholder="IP/详情" value="${param.keyword}">
                            </div>
                            <div class="col-12">
                                <button type="submit" class="btn btn-primary">
                                    <i class="fas fa-search me-1"></i>搜索
                                </button>
                                <a href="/admin/audit/logs" class="btn btn-outline-secondary ms-2">
                                    <i class="fas fa-undo me-1"></i>重置
                                </a>
                                <button type="button" class="btn btn-outline-primary ms-2" onclick="exportLogs()">
                                    <i class="fas fa-download me-1"></i>导出
                                </button>
                            </div>
                        </form>
                    </div>

                    <!-- 日志表格 -->
                    <div class="table-container">
                        <c:if test="${not empty auditLogs}">
                            <div class="table-responsive">
                                <table class="table table-hover align-middle">
                                    <thead class="table-light">
                                        <tr>
                                            <th style="width: 140px;">时间</th>
                                            <th style="width: 120px;">操作</th>
                                            <th style="width: 100px;">资源类型</th>
                                            <th style="width: 120px;">IP地址</th>
                                            <th>详情</th>
                                            <th style="width: 80px;">操作</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="log" items="${auditLogs}">
                                            <tr>
                                                <td>
                                                    <small class="text-muted">
                                                        <fmt:formatDate value="${log.createdAt}" 
                                                                      pattern="yyyy-MM-dd"/>
                                                        <br>
                                                        <fmt:formatDate value="${log.createdAt}" 
                                                                      pattern="HH:mm:ss"/>
                                                    </small>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${log.action.contains('FAILED')}">
                                                            <span class="badge bg-danger action-badge">${log.action}</span>
                                                        </c:when>
                                                        <c:when test="${log.action.contains('SUCCESS')}">
                                                            <span class="badge bg-success action-badge">${log.action}</span>
                                                        </c:when>
                                                        <c:when test="${log.action == 'DELETE'}">
                                                            <span class="badge bg-warning action-badge">${log.action}</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge bg-primary action-badge">${log.action}</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <span class="badge bg-secondary">${log.resourceType}</span>
                                                </td>
                                                <td>
                                                    <code class="small">${log.ipAddress}</code>
                                                </td>
                                                <td>
                                                    <div class="log-detail" title="${log.details}">
                                                        ${log.details}
                                                    </div>
                                                </td>
                                                <td>
                                                    <button type="button" class="btn btn-sm btn-outline-info" 
                                                            onclick="showLogDetail(${log.id})">
                                                        <i class="fas fa-eye"></i>
                                                    </button>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:if>
                        <c:if test="${empty auditLogs}">
                            <div class="text-center text-muted py-5">
                                <i class="fas fa-inbox fa-3x mb-3"></i>
                                <h5>暂无审计日志</h5>
                                <p>请调整筛选条件后重新搜索</p>
                            </div>
                        </c:if>
                    </div>

                    <!-- 分页 -->
                    <c:if test="${totalPages > 1}">
                        <div class="pagination-container">
                            <nav aria-label="审计日志分页">
                                <ul class="pagination justify-content-center mb-0">
                                    <c:if test="${currentPage > 1}">
                                        <li class="page-item">
                                            <a class="page-link" href="?page=${currentPage - 1}&${queryString}">
                                                <i class="fas fa-chevron-left"></i> 上一页
                                            </a>
                                        </li>
                                    </c:if>

                                    <c:forEach begin="${startPage}" end="${endPage}" var="i">
                                        <li class="page-item ${currentPage == i ? 'active' : ''}">
                                            <a class="page-link" href="?page=${i}&${queryString}">${i}</a>
                                        </li>
                                    </c:forEach>

                                    <c:if test="${currentPage < totalPages}">
                                        <li class="page-item">
                                            <a class="page-link" href="?page=${currentPage + 1}&${queryString}">
                                                下一页 <i class="fas fa-chevron-right"></i>
                                            </a>
                                        </li>
                                    </c:if>
                                </ul>
                            </nav>
                            <div class="text-center mt-2">
                                <small class="text-muted">
                                    第 ${currentPage} 页，共 ${totalPages} 页，共 ${totalCount} 条记录
                                </small>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>

    <!-- 日志详情模态框 -->
    <div class="modal fade" id="logDetailModal" tabindex="-1" aria-labelledby="logDetailModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="logDetailModalLabel">
                        <i class="fas fa-info-circle me-2"></i>审计日志详情
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body" id="logDetailContent">
                    <!-- 详情内容将通过JavaScript动态加载 -->
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>
                </div>
            </div>
        </div>
    </div>

    <!-- JavaScript -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    
    <script>
        // 显示日志详情
        function showLogDetail(logId) {
            fetch('/admin/audit/logs/' + logId)
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        const log = data.log;
                        const content = `
                            <div class="row">
                                <div class="col-md-6">
                                    <h6 class="text-primary">基本信息</h6>
                                    <table class="table table-sm">
                                        <tr>
                                            <td class="fw-bold">日志ID:</td>
                                            <td>${log.id}</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-bold">时间:</td>
                                            <td>${new Date(log.createdAt).toLocaleString()}</td>
                                        </tr>
                                        <tr>
                                            <td class="fw-bold">操作:</td>
                                            <td><span class="badge bg-primary">${log.action}</span></td>
                                        </tr>
                                        <tr>
                                            <td class="fw-bold">资源类型:</td>
                                            <td><span class="badge bg-secondary">${log.resourceType}</span></td>
                                        </tr>
                                        <tr>
                                            <td class="fw-bold">资源ID:</td>
                                            <td>${log.resourceId || 'N/A'}</td>
                                        </tr>
                                    </table>
                                </div>
                                <div class="col-md-6">
                                    <h6 class="text-primary">网络信息</h6>
                                    <table class="table table-sm">
                                        <tr>
                                            <td class="fw-bold">IP地址:</td>
                                            <td><code>${log.ipAddress}</code></td>
                                        </tr>
                                        <tr>
                                            <td class="fw-bold">用户代理:</td>
                                            <td class="small">${log.userAgent || 'N/A'}</td>
                                        </tr>
                                    </table>
                                </div>
                            </div>
                            <div class="row mt-3">
                                <div class="col-12">
                                    <h6 class="text-primary">详细描述</h6>
                                    <div class="alert alert-light">
                                        ${log.details || '无详细描述'}
                                    </div>
                                </div>
                            </div>
                            ${log.oldValue || log.newValue ? `
                            <div class="row">
                                <div class="col-md-6">
                                    <h6 class="text-primary">修改前值</h6>
                                    <pre class="bg-light p-2 small">${log.oldValue || '无'}</pre>
                                </div>
                                <div class="col-md-6">
                                    <h6 class="text-primary">修改后值</h6>
                                    <pre class="bg-light p-2 small">${log.newValue || '无'}</pre>
                                </div>
                            </div>
                            ` : ''}
                        `;
                        document.getElementById('logDetailContent').innerHTML = content;
                        new bootstrap.Modal(document.getElementById('logDetailModal')).show();
                    } else {
                        alert('获取日志详情失败: ' + data.message);
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('获取日志详情时发生错误');
                });
        }

        // 导出日志
        function exportLogs() {
            const params = new URLSearchParams(window.location.search);
            params.set('export', 'true');
            window.open('/admin/audit/logs?' + params.toString(), '_blank');
        }

        // 设置默认日期
        document.addEventListener('DOMContentLoaded', function() {
            const endDate = document.getElementById('endDate');
            const startDate = document.getElementById('startDate');
            
            if (!endDate.value) {
                endDate.value = new Date().toISOString().split('T')[0];
            }
            
            if (!startDate.value) {
                const oneWeekAgo = new Date();
                oneWeekAgo.setDate(oneWeekAgo.getDate() - 7);
                startDate.value = oneWeekAgo.toISOString().split('T')[0];
            }
        });
    </script>
</body>
</html>
