<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="org.example.scms.model.AuditLog" %>
<%@ page import="org.example.scms.model.User" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>审计日志搜索结果 - SCMS管理系统</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/daterangepicker/daterangepicker.css" rel="stylesheet">
    <style>
        .sidebar {
            min-height: 100vh;
            background: linear-gradient(180deg, #2c3e50 0%, #34495e 100%);
        }
        .sidebar .nav-link {
            color: #ecf0f1;
            padding: 12px 20px;
            border-radius: 8px;
            margin: 4px 0;
            transition: all 0.3s ease;
        }
        .sidebar .nav-link:hover, .sidebar .nav-link.active {
            background-color: #3498db;
            color: white;
        }
        .content-area {
            background-color: #f8f9fa;
            min-height: 100vh;
        }
        .card {
            border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .badge-success { background-color: #28a745; }
        .badge-danger { background-color: #dc3545; }
        .badge-warning { background-color: #ffc107; color: #212529; }
        .badge-info { background-color: #17a2b8; }
        .table-hover tbody tr:hover {
            background-color: #f1f3f4;
        }
        .search-highlight {
            background-color: #fff3cd;
            padding: 2px 4px;
            border-radius: 3px;
        }
        .operation-icon {
            width: 20px;
            text-align: center;
        }
    </style>
</head>
<body>
    <div class="container-fluid">
        <div class="row">
            <!-- 侧边栏 -->
            <div class="col-md-2 p-0">
                <div class="sidebar">
                    <div class="p-3 text-center border-bottom">
                        <h5 class="text-white mb-0">
                            <i class="fas fa-shield-alt"></i> 审计管理
                        </h5>
                    </div>
                    <nav class="nav flex-column p-3">
                        <hr class="text-white-50">
                        <a class="nav-link" href="audit/dashboard.jsp">
                            <i class="fas fa-arrow-left me-2"></i> 返回管理后台
                        </a>
                    </nav>
                </div>
            </div>

            <!-- 主内容区 -->
            <div class="col-md-10 content-area">
                <div class="p-4">
                    <!-- 页面标题 -->
                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <div>
                            <h2><i class="fas fa-search me-2"></i>搜索结果</h2>
                            <p class="text-muted mb-0">审计日志搜索结果详情</p>
                        </div>
                        <div>
                            <button type="button" class="btn btn-success" onclick="exportResults()">
                                <i class="fas fa-download me-1"></i> 导出结果
                            </button>
                        </div>
                    </div>

                    <%
                        List<AuditLog> auditLogs = (List<AuditLog>) request.getAttribute("auditLogs");
                        Map<Integer, User> userMap = (Map<Integer, User>) request.getAttribute("userMap");
                        String searchCriteria = (String) request.getAttribute("searchCriteria");
                        int totalCount = (Integer) request.getAttribute("totalCount");
                    %>

                    <!-- 搜索条件摘要 -->
                    <div class="card mb-4">
                        <div class="card-body">
                            <h5 class="card-title">
                                <i class="fas fa-filter me-2"></i>搜索条件
                            </h5>
                            <div class="row">
                                <div class="col-md-8">
                                    <p class="mb-0"><%= searchCriteria != null ? searchCriteria : "无特定搜索条件" %></p>
                                </div>
                                <div class="col-md-4 text-end">
                                    <span class="badge bg-primary fs-6">
                                        找到 <%= totalCount %> 条记录
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 搜索结果 -->
                    <div class="card">
                        <div class="card-header bg-primary text-white">
                            <h5 class="mb-0">
                                <i class="fas fa-table me-2"></i>搜索结果 (<%= auditLogs != null ? auditLogs.size() : 0 %> 条)
                            </h5>
                        </div>
                        <div class="card-body p-0">
                            <% if (auditLogs != null && !auditLogs.isEmpty()) { %>
                            <div class="table-responsive">
                                <table class="table table-hover mb-0">
                                    <thead class="table-light">
                                        <tr>
                                            <th>时间</th>
                                            <th>用户</th>
                                            <th>操作</th>
                                            <th>表/模块</th>
                                            <th>IP地址</th>
                                            <th>状态</th>
                                            <th>操作</th>
                                        </tr>
                                    </thead>
                                    <tbody>                                        <% for (AuditLog log : auditLogs) { 
                                            User user = userMap.get(log.getAdminId());
                                        %>
                                        <tr>
                                            <td>
                                                <small class="text-muted">
                                                    <%= log.getCreatedAt() %>
                                                </small>
                                            </td>                                            <td>
                                                <div class="d-flex align-items-center">
                                                    <i class="fas fa-user-circle me-2 text-primary"></i>
                                                    <div>
                                                        <div class="fw-bold">
                                                            <%= user != null ? user.getFullName() : (log.getUsername() != null ? log.getUsername() : "未知用户") %>
                                                        </div>
                                                        <small class="text-muted">
                                                            <%= user != null ? user.getRealIdCard() : log.getAdminId() %>
                                                        </small>
                                                        <br>
                                                        <% if (user != null && user.getFullName() != null && !user.getFullName().equals(user.getUsername())) { %>
                                                            <span class="badge bg-info">管理员</span>
                                                        <% } else { %>
                                                            <span class="badge bg-secondary">普通用户</span>
                                                        <% } %>
                                                    </div>
                                                </div>
                                            </td>
                                            <td>                                                <span class="operation-icon">
                                                    <% if ("CREATE".equals(log.getAction())) { %>
                                                        <i class="fas fa-plus-circle text-success"></i>
                                                    <% } else if ("UPDATE".equals(log.getAction())) { %>
                                                        <i class="fas fa-edit text-warning"></i>
                                                    <% } else if ("DELETE".equals(log.getAction())) { %>
                                                        <i class="fas fa-trash text-danger"></i>
                                                    <% } else if ("VIEW".equals(log.getAction())) { %>
                                                        <i class="fas fa-eye text-info"></i>
                                                    <% } else if ("QUERY".equals(log.getAction())) { %>
                                                        <i class="fas fa-search text-primary"></i>
                                                    <% } else { %>
                                                        <i class="fas fa-cog text-secondary"></i>
                                                    <% } %>
                                                </span>
                                                <%= log.getAction() %>
                                            </td>                                            <td>
                                                <span class="badge bg-light text-dark">
                                                    <%= log.getResourceType() %>
                                                </span>
                                            </td>
                                            <td>
                                                <code><%= log.getIpAddress() %></code>
                                            </td>                                            <td>
                                                <span class="badge badge-success">已记录</span>
                                            </td><td>
                                                <button type="button" class="btn btn-sm btn-outline-primary" 
                                                        data-log-id="<%= log.getId() %>"
                                                        onclick="viewDetails(this.dataset.logId)">
                                                    <i class="fas fa-eye"></i> 详情
                                                </button>
                                            </td>
                                        </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </div>
                            <% } else { %>
                            <div class="text-center py-5">
                                <i class="fas fa-search text-muted" style="font-size: 3rem;"></i>
                                <h4 class="text-muted mt-3">未找到匹配的记录</h4>
                                <p class="text-muted">请尝试调整搜索条件</p>
                                <button type="button" class="btn btn-primary" onclick="history.back()">
                                    <i class="fas fa-arrow-left me-1"></i> 重新搜索
                                </button>
                            </div>
                            <% } %>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- 详情模态框 -->
    <div class="modal fade" id="detailModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">
                        <i class="fas fa-info-circle me-2"></i>审计日志详情
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body" id="modalBody">
                    <!-- 详情内容将通过AJAX加载 -->
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // 查看详情
        function viewDetails(logId) {
            fetch('/SCMS/admin/audit?action=getDetail&id=' + logId)
                .then(response => response.json())
                .then(data => {
                    document.getElementById('modalBody').innerHTML = formatLogDetail(data);
                    new bootstrap.Modal(document.getElementById('detailModal')).show();
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('加载详情失败');
                });
        }

        // 格式化日志详情
        function formatLogDetail(log) {
            return `
                <div class="row">
                    <div class="col-md-6">
                        <strong>操作时间:</strong><br>
                        <span class="text-muted">${log.createdAt}</span>
                    </div>
                    <div class="col-md-6">
                        <strong>操作类型:</strong><br>
                        <span class="badge bg-primary">${log.operationType}</span>
                    </div>
                </div>
                <hr>
                <div class="row">
                    <div class="col-md-6">
                        <strong>用户ID:</strong><br>
                        <span class="text-muted">${log.userId}</span>
                    </div>
                    <div class="col-md-6">
                        <strong>IP地址:</strong><br>
                        <code>${log.ipAddress}</code>
                    </div>
                </div>
                <hr>
                <div class="row">
                    <div class="col-md-6">
                        <strong>表名:</strong><br>
                        <span class="badge bg-light text-dark">${log.tableName}</span>
                    </div>
                    <div class="col-md-6">
                        <strong>记录ID:</strong><br>
                        <span class="text-muted">${log.recordId || '无'}</span>
                    </div>
                </div>
                <hr>
                <div>
                    <strong>操作详情:</strong><br>
                    <pre class="bg-light p-3 rounded">${log.operationDetail || '无详细信息'}</pre>
                </div>
            `;
        }

        // 导出搜索结果
        function exportResults() {
            const params = new URLSearchParams(window.location.search);
            params.set('export', 'true');
            window.location.href = '/SCMS/admin/audit?' + params.toString();
        }
    </script>
</body>
</html>
