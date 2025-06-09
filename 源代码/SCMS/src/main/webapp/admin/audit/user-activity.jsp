<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>用户活动查询 - SCMS</title>
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
        .activity-timeline {
            position: relative;
            padding-left: 30px;
        }
        .activity-timeline::before {
            content: '';
            position: absolute;
            left: 15px;
            top: 0;
            bottom: 0;
            width: 2px;
            background: #dee2e6;
        }
        .activity-item {
            position: relative;
            margin-bottom: 20px;
        }
        .activity-item::before {
            content: '';
            position: absolute;
            left: -26px;
            top: 8px;
            width: 12px;
            height: 12px;
            border-radius: 50%;
            background: #007bff;
            border: 2px solid #fff;
            box-shadow: 0 0 0 2px #dee2e6;
        }
        .activity-item.activity-create::before { background: #28a745; }
        .activity-item.activity-update::before { background: #ffc107; }
        .activity-item.activity-delete::before { background: #dc3545; }
        .activity-item.activity-login::before { background: #17a2b8; }
        .activity-item.activity-logout::before { background: #6c757d; }
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
                               class="nav-link">
                                <i class="bi bi-graph-up"></i> 统计分析
                            </a>
                        </li>
                        <li class="nav-item">
                            <a href="${pageContext.request.contextPath}/admin/enhanced-audit?action=user-activity" 
                               class="nav-link active">
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
                    <h2><i class="bi bi-person-lines-fill"></i> 用户活动查询</h2>
                    
                    <!-- 错误信息 -->
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <i class="bi bi-exclamation-triangle"></i> ${error}
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    </c:if>

                    <!-- 查询表单 -->
                    <div class="card mb-4">
                        <div class="card-header">
                            <h6 class="mb-0"><i class="bi bi-search"></i> 用户活动查询</h6>
                        </div>
                        <div class="card-body">
                            <form method="get" action="${pageContext.request.contextPath}/admin/enhanced-audit">
                                <input type="hidden" name="action" value="user-activity">
                                <div class="row g-3">
                                    <div class="col-md-4">
                                        <label class="form-label">用户ID</label>
                                        <input type="number" class="form-control" name="userId" 
                                               value="${queryUserId}" placeholder="输入用户ID">
                                    </div>
                                    <div class="col-md-4">
                                        <label class="form-label">用户名</label>
                                        <input type="text" class="form-control" name="username" 
                                               value="${queryUsername}" placeholder="输入用户名">
                                    </div>
                                    <div class="col-md-4">
                                        <label class="form-label">&nbsp;</label>
                                        <button type="submit" class="btn btn-primary d-block">
                                            <i class="bi bi-search"></i> 查询活动
                                        </button>
                                    </div>
                                </div>
                                <div class="mt-3">
                                    <small class="text-muted">
                                        <i class="bi bi-info-circle"></i> 
                                        请输入用户ID或用户名中的任意一个进行查询，系统将显示最近100条活动记录。
                                    </small>
                                </div>
                            </form>
                        </div>
                    </div>

                    <!-- 用户活动结果 -->
                    <c:if test="${not empty userActivities}">
                        <div class="card">
                            <div class="card-header">
                                <div class="d-flex justify-content-between align-items-center">
                                    <h6 class="mb-0">
                                        <i class="bi bi-activity"></i> 用户活动记录
                                        <c:if test="${not empty queryUsername}">
                                            - ${queryUsername}
                                        </c:if>
                                        <c:if test="${not empty queryUserId}">
                                            - ID: ${queryUserId}
                                        </c:if>
                                    </h6>
                                    <span class="badge bg-info">${userActivities.size()} 条记录</span>
                                </div>
                            </div>
                            <div class="card-body">
                                <div class="activity-timeline">
                                    <c:forEach items="${userActivities}" var="activity">
                                        <div class="activity-item activity-${activity.action.toLowerCase()}">
                                            <div class="card">
                                                <div class="card-body p-3">
                                                    <div class="d-flex justify-content-between align-items-start">
                                                        <div>
                                                            <h6 class="mb-1">
                                                                <c:choose>
                                                                    <c:when test="${activity.action == 'LOGIN'}">
                                                                        <i class="bi bi-box-arrow-in-right text-info"></i> 用户登录
                                                                    </c:when>
                                                                    <c:when test="${activity.action == 'LOGOUT'}">
                                                                        <i class="bi bi-box-arrow-right text-secondary"></i> 用户登出
                                                                    </c:when>
                                                                    <c:when test="${activity.action == 'CREATE'}">
                                                                        <i class="bi bi-plus-circle text-success"></i> 创建操作
                                                                    </c:when>
                                                                    <c:when test="${activity.action == 'UPDATE'}">
                                                                        <i class="bi bi-pencil-square text-warning"></i> 更新操作
                                                                    </c:when>
                                                                    <c:when test="${activity.action == 'DELETE'}">
                                                                        <i class="bi bi-trash text-danger"></i> 删除操作
                                                                    </c:when>
                                                                    <c:when test="${activity.action == 'VIEW'}">
                                                                        <i class="bi bi-eye text-info"></i> 查看操作
                                                                    </c:when>
                                                                    <c:when test="${activity.action == 'QUERY'}">
                                                                        <i class="bi bi-search text-primary"></i> 查询操作
                                                                    </c:when>
                                                                    <c:when test="${activity.action == 'APPROVE'}">
                                                                        <i class="bi bi-check-circle text-success"></i> 审批通过
                                                                    </c:when>
                                                                    <c:when test="${activity.action == 'REJECT'}">
                                                                        <i class="bi bi-x-circle text-danger"></i> 审批拒绝
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <i class="bi bi-activity"></i> ${activity.action}
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </h6>
                                                            <p class="mb-1 text-muted">
                                                                <strong>表名:</strong> <code>${activity.tableName}</code>
                                                                <c:if test="${activity.recordId != null && activity.recordId > 0}">
                                                                    | <strong>记录ID:</strong> ${activity.recordId}
                                                                </c:if>
                                                            </p>
                                                            <p class="mb-0">
                                                                <small class="text-muted">
                                                                    <i class="bi bi-geo-alt"></i> IP: ${activity.ipAddress}
                                                                    <c:if test="${not empty activity.username}">
                                                                        | <i class="bi bi-person"></i> ${activity.username}
                                                                        <c:if test="${not empty activity.realName}">
                                                                            (${activity.realName})
                                                                        </c:if>
                                                                    </c:if>
                                                                </small>
                                                            </p>
                                                        </div>
                                                        <div class="text-end">
                                                            <small class="text-muted">
                                                                <fmt:formatDate value="${activity.createdAt}" pattern="MM-dd HH:mm:ss"/>
                                                            </small>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </div>
                        </div>

                        <!-- 活动统计摘要 -->
                        <div class="row mt-4">
                            <div class="col-md-12">
                                <div class="card">
                                    <div class="card-header">
                                        <h6 class="mb-0"><i class="bi bi-bar-chart"></i> 活动统计摘要</h6>
                                    </div>
                                    <div class="card-body">
                                        <div class="row">
                                            <c:set var="loginCount" value="0" />
                                            <c:set var="createCount" value="0" />
                                            <c:set var="updateCount" value="0" />
                                            <c:set var="deleteCount" value="0" />
                                            <c:set var="viewCount" value="0" />
                                            <c:set var="queryCount" value="0" />
                                            
                                            <c:forEach items="${userActivities}" var="activity">
                                                <c:choose>
                                                    <c:when test="${activity.action == 'LOGIN'}">
                                                        <c:set var="loginCount" value="${loginCount + 1}" />
                                                    </c:when>
                                                    <c:when test="${activity.action == 'CREATE'}">
                                                        <c:set var="createCount" value="${createCount + 1}" />
                                                    </c:when>
                                                    <c:when test="${activity.action == 'UPDATE'}">
                                                        <c:set var="updateCount" value="${updateCount + 1}" />
                                                    </c:when>
                                                    <c:when test="${activity.action == 'DELETE'}">
                                                        <c:set var="deleteCount" value="${deleteCount + 1}" />
                                                    </c:when>
                                                    <c:when test="${activity.action == 'VIEW'}">
                                                        <c:set var="viewCount" value="${viewCount + 1}" />
                                                    </c:when>
                                                    <c:when test="${activity.action == 'QUERY'}">
                                                        <c:set var="queryCount" value="${queryCount + 1}" />
                                                    </c:when>
                                                </c:choose>
                                            </c:forEach>

                                            <div class="col-md-2">
                                                <div class="text-center">
                                                    <div class="h4 text-info">${loginCount}</div>
                                                    <div class="text-muted">登录次数</div>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="text-center">
                                                    <div class="h4 text-success">${createCount}</div>
                                                    <div class="text-muted">创建操作</div>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="text-center">
                                                    <div class="h4 text-warning">${updateCount}</div>
                                                    <div class="text-muted">更新操作</div>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="text-center">
                                                    <div class="h4 text-danger">${deleteCount}</div>
                                                    <div class="text-muted">删除操作</div>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="text-center">
                                                    <div class="h4 text-primary">${viewCount}</div>
                                                    <div class="text-muted">查看操作</div>
                                                </div>
                                            </div>
                                            <div class="col-md-2">
                                                <div class="text-center">
                                                    <div class="h4 text-secondary">${queryCount}</div>
                                                    <div class="text-muted">查询操作</div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:if>

                    <!-- 无结果提示 -->
                    <c:if test="${empty userActivities && (not empty queryUserId || not empty queryUsername)}">
                        <div class="card">
                            <div class="card-body text-center py-5">
                                <i class="bi bi-search" style="font-size: 4rem; color: #dee2e6;"></i>
                                <h5 class="mt-3 text-muted">未找到用户活动记录</h5>
                                <p class="text-muted">
                                    请检查输入的用户ID或用户名是否正确，或者该用户可能没有相关操作记录。
                                </p>
                            </div>
                        </div>
                    </c:if>

                    <!-- 使用说明 -->
                    <c:if test="${empty queryUserId && empty queryUsername}">
                        <div class="card">
                            <div class="card-body">
                                <h5><i class="bi bi-info-circle"></i> 用户活动查询说明</h5>
                                <p>此功能允许管理员查询特定用户的操作活动记录，帮助监控用户行为和系统安全。</p>
                                
                                <h6>查询方式：</h6>
                                <ul>
                                    <li><strong>用户ID查询：</strong> 输入具体的用户ID数字进行精确查询</li>
                                    <li><strong>用户名查询：</strong> 输入用户名进行查询</li>
                                </ul>

                                <h6>活动类型说明：</h6>
                                <div class="row">
                                    <div class="col-md-6">
                                        <ul class="list-unstyled">
                                            <li><span class="badge bg-info">LOGIN</span> 用户登录系统</li>
                                            <li><span class="badge bg-secondary">LOGOUT</span> 用户登出系统</li>
                                            <li><span class="badge bg-success">CREATE</span> 创建新记录</li>
                                            <li><span class="badge bg-warning">UPDATE</span> 更新现有记录</li>
                                        </ul>
                                    </div>
                                    <div class="col-md-6">
                                        <ul class="list-unstyled">
                                            <li><span class="badge bg-danger">DELETE</span> 删除记录</li>
                                            <li><span class="badge bg-primary">VIEW</span> 查看记录详情</li>
                                            <li><span class="badge bg-light text-dark">QUERY</span> 执行查询操作</li>
                                            <li><span class="badge bg-success">APPROVE/REJECT</span> 审批操作</li>
                                        </ul>
                                    </div>
                                </div>

                                <div class="alert alert-info mt-3">
                                    <i class="bi bi-lightbulb"></i>
                                    <strong>提示：</strong> 系统默认显示最近100条活动记录，按时间倒序排列。如需查看更多历史记录，请使用导出功能。
                                </div>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
