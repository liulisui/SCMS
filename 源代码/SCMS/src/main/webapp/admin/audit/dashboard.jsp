<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
            <!DOCTYPE html>
            <html lang="zh-CN">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>审计管理 - 校园通行码预约管理系统</title>
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
                <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
                <style>
                    .dashboard-card {
                        border: none;
                        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
                        border-radius: 10px;
                    }

                    .stat-card {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                    }

                    .stat-card.success {
                        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
                    }

                    .stat-card.warning {
                        background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
                    }

                    .navbar-brand {
                        font-weight: bold;
                        color: #495057;
                    }

                    .action-btn {
                        padding: 15px 20px;
                        margin: 10px 0;
                        border-radius: 8px;
                        transition: all 0.3s ease;
                    }

                    .action-btn:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
                    }
                </style>
            </head>

            <body class="bg-light">
                <!-- 导航栏 -->                <nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm">
                    <div class="container-fluid">
                        <a class="navbar-brand" href="${pageContext.request.contextPath}/index.jsp">
                            <i class="fas fa-shield-alt text-primary me-2"></i>
                            审计管理系统
                        </a>
                        <div class="ms-auto">
                            <a href="${pageContext.request.contextPath}/index.jsp" class="btn btn-outline-danger">
                                <i class="fas fa-sign-out-alt me-2"></i>退出登录
                            </a>
                        </div>
                    </div>
                </nav>

                <div class="container-fluid py-4">
                    <!-- 页面标题 -->
                    <div class="text-center mb-5">
                        <h2 class="text-dark">
                            <i class="fas fa-clipboard-list me-2"></i>审计日志管理
                        </h2>
                        <p class="text-muted">查看和管理系统审计日志</p>
                    </div> <!-- 统计概览已被移除 -->                    <!-- 核心功能 -->
                    <div class="row justify-content-center">
                        <div class="col-md-8">
                            <div class="card dashboard-card">
                                <div class="card-header bg-primary text-white text-center">
                                    <h5 class="mb-0">
                                        <i class="fas fa-tools me-2"></i>核心功能
                                    </h5>
                                </div>                                <div class="card-body">
                                    <div class="row justify-content-center">
                                        <!-- 查看审计日志 -->
                                        <div class="col-md-6 text-center">
                                            <form action="${pageContext.request.contextPath}/admin/enhanced-audit" method="post">
                                                <input type="hidden" name="action" value="list">
                                                <input type="hidden" name="returnTo" value="audit_dashboard">
                                                <input type="hidden" name="adminType" value="audit">
                                                <button type="submit" class="btn btn-primary action-btn w-100">
                                                    <div class="mb-2">
                                                        <i class="fas fa-list fa-2x"></i>
                                                    </div>
                                                    <h6>查看审计日志</h6>
                                                    <small class="text-light">浏览所有系统日志</small>
                                                </button>
                                            </form>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- 最近日志预览 -->
                    <div class="row justify-content-center mt-5">
                        <div class="col-md-10">
                            <div class="card dashboard-card">
                                <div class="card-header bg-light">
                                    <div class="d-flex justify-content-between align-items-center">
                                        <h6 class="mb-0">
                                            <i class="fas fa-clock me-2"></i>最近活动
                                        </h6>
                                    </div>
                                </div>
                                <div class="card-body">
                                    <c:if test="${not empty recentLogs}">
                                        <div class="table-responsive">
                                            <table class="table table-hover">
                                                <thead class="table-light">
                                                    <tr>
                                                        <th>时间</th>
                                                        <th>操作</th>
                                                        <th>用户</th>
                                                        <th>IP地址</th>
                                                        <th>状态</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach var="log" items="${recentLogs}" varStatus="status">
                                                        <c:if test="${status.index < 5}">
                                                            <tr>
                                                                <td>
                                                                    <c:choose>
                                                                        <c:when test="${log.createdAt != null}">
                                                                            <fmt:parseDate value="${log.createdAt}"
                                                                                pattern="yyyy-MM-dd'T'HH:mm"
                                                                                var="parsedDate" type="both" />
                                                                            <fmt:formatDate value="${parsedDate}"
                                                                                pattern="MM-dd HH:mm" />
                                                                        </c:when>
                                                                        <c:otherwise>-</c:otherwise>
                                                                    </c:choose>
                                                                </td>
                                                                <td>${log.action}</td>
                                                                <td>${log.username}</td>
                                                                <td>
                                                                    <code>${log.ipAddress}</code>
                                                                </td>
                                                                <td>
                                                                    <span
                                                                        class="badge bg-${log.action.contains('FAILED') ? 'danger' : 
                                                                                log.action.contains('SUCCESS') ? 'success' : 'primary'}">
                                                                        ${log.action.contains('FAILED') ? '失败' :
                                                                        log.action.contains('SUCCESS') ? '成功' : '正常'}
                                                                    </span>
                                                                </td>
                                                            </tr>
                                                        </c:if>
                                                    </c:forEach>
                                                </tbody>
                                            </table>
                                        </div>
                                    </c:if>
                                    <c:if test="${empty recentLogs}">
                                        <div class="text-center text-muted py-4">
                                            <i class="fas fa-inbox fa-3x mb-3"></i>
                                            <p>暂无最近日志</p>
                                        </div>
                                    </c:if>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>



                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
            </body>

            </html>