<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>增强审计日志管理 - SCMS</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">
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
        .log-level-high { color: #dc3545; }
        .log-level-medium { color: #fd7e14; }
        .log-level-low { color: #28a745; }
        .card-stat {
            border-left: 4px solid #007bff;
        }
        .table-responsive {
            max-height: 600px;
            overflow-y: auto;
        }
        .log-details {
            max-width: 200px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            cursor: pointer;
        }
        .log-details:hover {
            white-space: normal;
            overflow: visible;
            background-color: #f8f9fa;
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
                        <li class="nav-item mt-3">
                            <a href="${pageContext.request.contextPath}/admin/audit/dashboard" class="nav-link">
                                <i class="bi bi-arrow-left"></i> 返回管理后台
                            </a>
                        </li>
                    </ul>
                </div>
            </div>

            <!-- 主内容区 -->
            <div class="col-md-10">
                <div class="p-4">
                    <!-- 头部操作栏 -->
                    <div class="d-flex justify-content-between align-items-center mb-4">
                        <h2><i class="bi bi-journal-text"></i> 审计日志管理</h2>
                        <div class="btn-group" role="group">
                            <a href="${pageContext.request.contextPath}/admin/enhanced-audit?action=export" 
                               class="btn btn-outline-primary">
                                <i class="bi bi-download"></i> 导出日志
                            </a>
                        </div>
                    </div>

                    <!-- 错误信息 -->
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <i class="bi bi-exclamation-triangle"></i> ${error}
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    </c:if>

                    <!-- 快速统计卡片 -->
                    <div class="row mb-4">
                        <div class="col-md-3">
                            <div class="card card-stat">
                                <div class="card-body">
                                    <div class="d-flex justify-content-between">
                                        <div>
                                            <h6 class="card-subtitle text-muted">总日志数</h6>
                                            <h4 class="card-title">${totalRecords}</h4>
                                        </div>
                                        <div class="align-self-center">
                                            <i class="bi bi-journal-text text-primary" style="font-size: 2rem;"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="card card-stat">
                                <div class="card-body">
                                    <div class="d-flex justify-content-between">
                                        <div>
                                            <h6 class="card-subtitle text-muted">当前页</h6>
                                            <h4 class="card-title">${currentPage} / ${totalPages}</h4>
                                        </div>
                                        <div class="align-self-center">
                                            <i class="bi bi-file-earmark-text text-info" style="font-size: 2rem;"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="card card-stat">
                                <div class="card-body">
                                    <div class="d-flex justify-content-between">
                                        <div>
                                            <h6 class="card-subtitle text-muted">每页最多显示记录数</h6>
                                            <h4 class="card-title">${pageSize}</h4>
                                        </div>
                                        <div class="align-self-center">
                                            <i class="bi bi-layers text-success" style="font-size: 2rem;"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="card card-stat">
                                <div class="card-body">
                                    <div class="d-flex justify-content-between">
                                        <div>
                                            <h6 class="card-subtitle text-muted">本页显示记录数</h6>
                                            <h4 class="card-title">${auditLogs.size()}</h4>
                                        </div>
                                        <div class="align-self-center">
                                            <i class="bi bi-eye text-warning" style="font-size: 2rem;"></i>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 快速搜索 -->
                    <div class="card mb-4">
                        <div class="card-header">
                            <h6 class="mb-0"><i class="bi bi-funnel"></i> 快速筛选</h6>
                        </div>
                        <div class="card-body">
                            <form method="get" action="${pageContext.request.contextPath}/admin/enhanced-audit">
                                <input type="hidden" name="action" value="search">
                                <div class="row g-3">
                                                                        <div class="col-md-3">
                                        <label class="form-label">操作类型</label>
                                        <select class="form-select" name="actionType">
                                            <option value="">全部</option>
                                            <option value="LOGIN">登录</option>
                                            <option value="LOGOUT">登出</option>
                                            <option value="CREATE">创建</option>
                                            <option value="UPDATE">更新</option>
                                            <option value="DELETE">删除</option>
                                            <option value="VIEW">查看</option>
                                            <option value="QUERY">查询</option>
                                            <option value="APPROVE">审批</option>
                                            <option value="REJECT">拒绝</option>
                                        </select>
                                    </div>
                                    <div class="col-md-2">
                                        <label class="form-label">开始日期</label>
                                        <input type="date" class="form-control" name="startDate">
                                    </div>
                                    <div class="col-md-2">
                                        <label class="form-label">结束日期</label>
                                        <input type="date" class="form-control" name="endDate">
                                    </div>
                                    <div class="col-md-2">
                                        <label class="form-label">&nbsp;</label>
                                        <button type="submit" class="btn btn-primary d-block">
                                            <i class="bi bi-search"></i> 搜索
                                        </button>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>

                    <!-- 审计日志列表 -->
                    <div class="card">
                        <div class="card-header">
                            <h6 class="mb-0"><i class="bi bi-list-ul"></i> 审计日志列表</h6>
                        </div>
                        <div class="card-body p-0">
                            <div class="table-responsive">
                                <table class="table table-hover mb-0">
                                    <thead class="table-dark">
                                        <tr>
                                            <th>ID</th>
                                            <th>时间</th>
                                            <th>用户</th>
                                            <th>操作</th>
                                            <th>表名</th>
                                            <th>记录ID</th>                                            <th>IP地址</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach items="${auditLogs}" var="log">
                                            <tr>
                                                <td>${log.id}</td>
                                                <td>
                                                    <fmt:formatDate value="${log.createdAt}" pattern="yyyy-MM-dd HH:mm:ss"/>
                                                </td>                                                <td>
                                                    <div>
                                                        <c:choose>
                                                            <c:when test="${log.userType == 'admin'}">
                                                                <strong>${log.username}</strong>
                                                                <br><small class="text-muted">${log.realName}</small>
                                                                <br><span class="badge bg-info">
                                                                    <c:choose>
                                                                        <c:when test="${log.adminType == 'system_admin'}">系统管理员</c:when>
                                                                        <c:when test="${log.adminType == 'school_admin'}">学校管理员</c:when>
                                                                        <c:when test="${log.adminType == 'department_admin'}">部门管理员</c:when>
                                                                        <c:when test="${log.adminType == 'audit_admin'}">审计管理员</c:when>
                                                                        <c:otherwise>管理员</c:otherwise>
                                                                    </c:choose>
                                                                </span>
                                                            </c:when>
                                                            <c:when test="${log.userType == 'user' && log.realName != null}">
                                                                <strong>${log.username}</strong>
                                                                <br><small class="text-muted">${log.realName}</small>
                                                                <br><span class="badge bg-secondary">
                                                                    <c:choose>
                                                                        <c:when test="${log.userRole == 'student'}">学生</c:when>
                                                                        <c:when test="${log.userRole == 'teacher'}">教师</c:when>
                                                                        <c:when test="${log.userRole == 'admin'}">用户管理员</c:when>
                                                                        <c:otherwise>普通用户</c:otherwise>
                                                                    </c:choose>
                                                                </span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <strong>${log.username != null ? log.username : '未知用户'}</strong>
                                                                <br><span class="badge bg-warning">未知用户</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </div>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${log.action == 'LOGIN'}">
                                                            <span class="badge bg-success">登录</span>
                                                        </c:when>
                                                        <c:when test="${log.action == 'LOGOUT'}">
                                                            <span class="badge bg-secondary">登出</span>
                                                        </c:when>
                                                        <c:when test="${log.action == 'CREATE'}">
                                                            <span class="badge bg-primary">创建</span>
                                                        </c:when>
                                                        <c:when test="${log.action == 'UPDATE'}">
                                                            <span class="badge bg-warning">更新</span>
                                                        </c:when>
                                                        <c:when test="${log.action == 'DELETE'}">
                                                            <span class="badge bg-danger">删除</span>
                                                        </c:when>
                                                        <c:when test="${log.action == 'VIEW'}">
                                                            <span class="badge bg-info">查看</span>
                                                        </c:when>
                                                        <c:when test="${log.action == 'QUERY'}">
                                                            <span class="badge bg-light text-dark">查询</span>
                                                        </c:when>
                                                        <c:when test="${log.action == 'APPROVE'}">
                                                            <span class="badge bg-success">审批</span>
                                                        </c:when>
                                                        <c:when test="${log.action == 'REJECT'}">
                                                            <span class="badge bg-danger">拒绝</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge bg-secondary">${log.action}</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <code>${log.tableName}</code>
                                                </td>
                                                <td>${log.recordId}</td>                                                <td>
                                                    <span class="text-monospace">${log.ipAddress}</span>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    <!-- 分页导航 -->
                    <c:if test="${totalPages > 1}">
                        <nav aria-label="审计日志分页" class="mt-4">
                            <ul class="pagination justify-content-center">
                                <c:if test="${currentPage > 1}">
                                    <li class="page-item">
                                        <a class="page-link" href="?action=list&page=${currentPage - 1}">
                                            <i class="bi bi-chevron-left"></i> 上一页
                                        </a>
                                    </li>
                                </c:if>
                                
                                <c:forEach begin="1" end="${totalPages}" var="pageNum">
                                    <c:if test="${pageNum <= 5 || pageNum > totalPages - 5 || (pageNum >= currentPage - 2 && pageNum <= currentPage + 2)}">
                                        <li class="page-item ${pageNum == currentPage ? 'active' : ''}">
                                            <a class="page-link" href="?action=list&page=${pageNum}">${pageNum}</a>
                                        </li>
                                    </c:if>
                                </c:forEach>
                                
                                <c:if test="${currentPage < totalPages}">
                                    <li class="page-item">
                                        <a class="page-link" href="?action=list&page=${currentPage + 1}">
                                            下一页 <i class="bi bi-chevron-right"></i>
                                        </a>
                                    </li>
                                </c:if>
                            </ul>
                        </nav>
                    </c:if>
                </div>
            </div>
        </div>
    </div>

    <!-- 日志详情模态框 -->
    <div class="modal fade" id="logDetailsModal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">审计日志详情</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body" id="logDetailsContent">
                    <!-- 详情内容将通过JavaScript加载 -->
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function refreshPage() {
            window.location.reload();
        }

        function showLogDetails(logId, action, tableName) {
            // 这里可以通过AJAX获取详细信息，暂时显示基本信息
            const content = `
                <div class="row">
                    <div class="col-md-6">
                        <h6>基本信息</h6>
                        <p><strong>日志ID:</strong> ${logId}</p>
                        <p><strong>操作类型:</strong> ${action}</p>
                        <p><strong>表名:</strong> ${tableName}</p>
                    </div>
                    <div class="col-md-6">
                        <h6>提示</h6>
                        <p class="text-muted">详细的新值和旧值信息可以通过导出功能获取完整数据。</p>
                    </div>
                </div>
            `;
            
            document.getElementById('logDetailsContent').innerHTML = content;
            new bootstrap.Modal(document.getElementById('logDetailsModal')).show();
        }

        // 自动设置今天的日期作为默认查询条件
        document.addEventListener('DOMContentLoaded', function() {
            const today = new Date().toISOString().split('T')[0];
            const endDateInput = document.querySelector('input[name="endDate"]');
            if (endDateInput && !endDateInput.value) {
                endDateInput.value = today;
            }
        });
    </script>
</body>
</html>
