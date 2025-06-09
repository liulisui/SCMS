<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.example.scms.service.*" %>
<%@ page import="org.example.scms.model.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<%
    AdminQueryResult queryResult = (AdminQueryResult) request.getAttribute("queryResult");
    AdminQueryParams queryParams = (AdminQueryParams) request.getAttribute("queryParams");
    List<Campus> campuses = (List<Campus>) request.getAttribute("campuses");
    List<Department> departments = (List<Department>) request.getAttribute("departments");
    Map<String, Object> statistics = (Map<String, Object>) request.getAttribute("statistics");
    String errorMessage = (String) request.getAttribute("errorMessage");
    Administrator admin = (Administrator) request.getAttribute("admin");
    
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    // 获取管理员权限信息
    String adminType = admin != null ? admin.getAdminType() : "";
    boolean isDepartmentAdmin = "department_admin".equals(adminType);
    boolean canViewPublicReservations = !isDepartmentAdmin;
%>

<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>预约记录查询 - 校园通行码预约管理系统</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            background-color: #f5f7fa;
            line-height: 1.6;
        }
        
        .container {
            max-width: 1400px;
            margin: 0 auto;
            padding: 20px;
        }
        
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 2rem;
            border-radius: 10px;
            margin-bottom: 2rem;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }
        
        .header h1 {
            font-size: 2rem;
            margin-bottom: 0.5rem;
        }
        
        .header p {
            opacity: 0.9;
            font-size: 1.1rem;
        }
        
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1rem;
            margin-bottom: 2rem;
        }
        
        .stat-card {
            background: white;
            padding: 1.5rem;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            text-align: center;
        }
        
        .stat-number {
            font-size: 2rem;
            font-weight: bold;
            color: #667eea;
            margin-bottom: 0.5rem;
        }
        
        .stat-label {
            color: #666;
            font-size: 0.9rem;
        }
        
        .search-form {
            background: white;
            padding: 2rem;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            margin-bottom: 2rem;
        }
        
        .form-title {
            font-size: 1.2rem;
            font-weight: 600;
            margin-bottom: 1.5rem;
            color: #333;
            display: flex;
            align-items: center;
        }
        
        .form-title::before {
            content: '🔍';
            margin-right: 0.5rem;
        }
          .form-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 1rem;
            margin-bottom: 1.5rem;
        }
        
        .form-group {
            display: flex;
            flex-direction: column;
        }
        
        .form-group label {
            margin-bottom: 0.5rem;
            font-weight: 500;
            color: #333;
        }
        
        .form-group input,
        .form-group select {
            padding: 0.75rem;
            border: 2px solid #e1e5e9;
            border-radius: 6px;
            font-size: 1rem;
            transition: border-color 0.3s;
        }
        
        .form-group input:focus,
        .form-group select:focus {
            outline: none;
            border-color: #667eea;
        }
        
        .form-actions {
            display: flex;
            gap: 1rem;
            justify-content: flex-end;
        }
        
        .btn {
            padding: 0.75rem 1.5rem;
            border: none;
            border-radius: 6px;
            font-size: 1rem;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            transition: all 0.3s;
        }
        
        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        
        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 8px rgba(102, 126, 234, 0.3);
        }
        
        .btn-secondary {
            background: #6c757d;
            color: white;
        }
        
        .btn-secondary:hover {
            background: #545b62;
        }
        
        .results-section {
            background: white;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            overflow: hidden;
        }
        
        .results-header {
            background: #f8f9fa;
            padding: 1.5rem 2rem;
            border-bottom: 1px solid #e9ecef;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .results-title {
            font-size: 1.2rem;
            font-weight: 600;
            color: #333;
        }
        
        .results-count {
            color: #666;
            font-size: 0.9rem;
        }
        
        .table-container {
            overflow-x: auto;
        }
        
        .data-table {
            width: 100%;
            border-collapse: collapse;
        }
        
        .data-table th,
        .data-table td {
            padding: 1rem;
            text-align: left;
            border-bottom: 1px solid #e9ecef;
        }
        
        .data-table th {
            background: #f8f9fa;
            font-weight: 600;
            color: #333;
            position: sticky;
            top: 0;
            z-index: 10;
        }
        
        .data-table tbody tr:hover {
            background: #f8f9fa;
        }
        
        .type-badge {
            padding: 0.25rem 0.75rem;
            border-radius: 20px;
            font-size: 0.8rem;
            font-weight: 500;
            white-space: nowrap; /* 防止文本换行 */
            display: inline-block; /* 确保标签正确显示为块级元素 */
        }
        
        .type-public {
            background: #e3f2fd;
            color: #1976d2;
        }
        
        .type-official {
            background: #f3e5f5;
            color: #7b1fa2;
        }
        
        .status-badge {
            padding: 0.25rem 0.75rem;
            border-radius: 20px;
            font-size: 0.8rem;
            font-weight: 500;
            white-space: nowrap; /* 防止文本换行 */
            display: inline-block; /* 确保标签正确显示为块级元素 */
        }
        
        .status-pending {
            background: #fff3cd;
            color: #856404;
        }
        
        .status-approved {
            background: #d4edda;
            color: #155724;
        }
        
        .status-rejected {
            background: #f8d7da;
            color: #721c24;
        }
        
        .status-cancelled {
            background: #e2e3e5;
            color: #383d41;
        }
        
        .status-completed {
            background: #d1ecf1;
            color: #0c5460;
        }
        
        .pagination {
            padding: 1.5rem 2rem;
            background: #f8f9fa;
            border-top: 1px solid #e9ecef;
            display: flex;
            justify-content: between;
            align-items: center;
        }
        
        .pagination-info {
            color: #666;
            font-size: 0.9rem;
        }
        
        .pagination-links {
            display: flex;
            gap: 0.5rem;
        }
        
        .pagination-link {
            padding: 0.5rem 1rem;
            border: 1px solid #dee2e6;
            background: white;
            color: #007bff;
            text-decoration: none;
            border-radius: 4px;
            transition: all 0.3s;
        }
        
        .pagination-link:hover {
            background: #e9ecef;
        }
        
        .pagination-link.active {
            background: #007bff;
            color: white;
            border-color: #007bff;
        }
        
        .pagination-link.disabled {
            color: #6c757d;
            pointer-events: none;
            background: #e9ecef;
        }
        
        .error-message {
            background: #f8d7da;
            color: #721c24;
            padding: 1rem;
            border-radius: 6px;
            margin-bottom: 1rem;
            border: 1px solid #f5c6cb;
        }
        
        .no-data {
            text-align: center;
            padding: 3rem;
            color: #666;
        }
        
        .no-data i {
            font-size: 3rem;
            margin-bottom: 1rem;
            opacity: 0.5;
        }
        
        .back-link {
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            color: white;
            text-decoration: none;
            margin-bottom: 1rem;
            opacity: 0.9;
            transition: opacity 0.3s;
        }
        
        .back-link:hover {
            opacity: 1;
        }
          @media (max-width: 768px) {
            .container {
                padding: 10px;
            }
            
            .form-grid {
                grid-template-columns: 1fr;
            }
            
            .form-actions {
                justify-content: stretch;
            }
            
            .btn {
                flex: 1;
                justify-content: center;
            }
            
            .header {
                padding: 1.5rem;
            }
            
            .header h1 {
                font-size: 1.5rem;
            }
        }
        
        /* 操作按钮样式 */
        .action-buttons {
            display: flex;
            gap: 0.5rem;
            justify-content: center;
        }
        
        .btn-action {
            padding: 0.4rem 0.8rem;
            border: none;
            border-radius: 4px;
            font-size: 0.85rem;
            cursor: pointer;
            transition: all 0.3s;
        }
        
        .btn-approve {
            background: #28a745;
            color: white;
        }
        
        .btn-approve:hover {
            background: #218838;
        }
        
        .btn-reject {
            background: #dc3545;
            color: white;
        }
        
        .btn-reject:hover {
            background: #c82333;
        }
        
        .btn-view {
            background: #17a2b8;
            color: white;
        }
        
        .btn-view:hover {
            background: #138496;
        }
        
        /* 审批弹窗样式 */
        .modal {
            display: none;
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.5);
        }
        
        .modal-content {
            background-color: white;
            margin: 10% auto;
            padding: 2rem;
            border-radius: 10px;
            width: 80%;
            max-width: 500px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
        }
        
        .modal-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 1.5rem;
            padding-bottom: 1rem;
            border-bottom: 1px solid #eee;
        }
        
        .modal-title {
            font-size: 1.3rem;
            font-weight: 600;
            color: #333;
        }
        
        .close {
            color: #aaa;
            font-size: 1.5rem;
            font-weight: bold;
            cursor: pointer;
            line-height: 1;
        }
        
        .close:hover {
            color: #333;
        }
        
        .modal-body {
            margin-bottom: 1.5rem;
        }
        
        .info-item {
            margin-bottom: 1rem;
        }
        
        .info-label {
            font-weight: 600;
            color: #555;
            margin-bottom: 0.3rem;
            display: block;
        }
        
        .info-value {
            color: #333;
        }
        
        .form-group {
            margin-bottom: 1rem;
        }
        
        .form-group label {
            display: block;
            margin-bottom: 0.5rem;
            font-weight: 600;
            color: #555;
        }
        
        .form-group textarea {
            width: 100%;
            padding: 0.8rem;
            border: 1px solid #ddd;
            border-radius: 6px;
            font-size: 0.95rem;
            resize: vertical;
            min-height: 80px;
        }
        
        .modal-footer {
            display: flex;
            justify-content: flex-end;
            gap: 1rem;
            padding-top: 1rem;
            border-top: 1px solid #eee;
        }
        
        .btn-modal {
            padding: 0.8rem 1.5rem;
            border: none;
            border-radius: 6px;
            font-size: 1rem;
            cursor: pointer;
            transition: all 0.3s;
        }
        
        .btn-modal-primary {
            background: #667eea;
            color: white;
        }
        
        .btn-modal-primary:hover {
            background: #5a6fd8;
        }
        
        .btn-modal-secondary {
            background: #6c757d;
            color: white;
        }
        
        .btn-modal-secondary:hover {
            background: #545b62;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <a href="${pageContext.request.contextPath}/admin/dashboard" class="back-link">
                ← 返回仪表板
            </a>
            <h1>预约记录查询</h1>
            <p>统一查询和管理所有预约记录</p>
        </div>
          <% if (statistics != null && !statistics.containsKey("error")) { %>
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-number"><%= statistics.get("totalReservations") %></div>
                <div class="stat-label">总预约数</div>
            </div>
            <% if (canViewPublicReservations) { %>
            <div class="stat-card">
                <div class="stat-number"><%= statistics.get("totalPublic") %></div>
                <div class="stat-label">社会公众预约数</div>
            </div>
            <% } %>
            <div class="stat-card">
                <div class="stat-number"><%= statistics.get("totalOfficial") %></div>
                <div class="stat-label">公务预约数</div>
            </div>            <% 
                Map<String, Integer> publicStats = (Map<String, Integer>) statistics.get("publicStats");
                Map<String, Integer> officialStats = (Map<String, Integer>) statistics.get("officialStats");
                int pendingCount;
                if (isDepartmentAdmin) {
                    // 部门管理员只显示公务预约的待审核数
                    pendingCount = officialStats.getOrDefault("pending", 0);
                } else {
                    // 系统管理员和学校管理员显示全部待审核数
                    pendingCount = publicStats.getOrDefault("pending", 0) + officialStats.getOrDefault("pending", 0);
                }
            %>
            <div class="stat-card">
                <div class="stat-number"><%= pendingCount %></div>
                <div class="stat-label">待审核数</div>
            </div>
        </div>
        <% } %>
        
        <% if (errorMessage != null) { %>
        <div class="error-message">
            <%= errorMessage %>
        </div>
        <% } %>
        
        <form method="GET" action="${pageContext.request.contextPath}/admin/reservation-query" class="search-form">
            <div class="form-title">查询条件</div>
            
            <div class="form-grid">                <div class="form-group">
                    <label for="reservationType">预约类型</label>
                    <select id="reservationType" name="reservationType">
                        <% if (canViewPublicReservations) { %>
                        <option value="all" <%= "all".equals(queryParams != null ? queryParams.getReservationType() : "") ? "selected" : "" %>>全部类型</option>
                        <option value="public" <%= "public".equals(queryParams != null ? queryParams.getReservationType() : "") ? "selected" : "" %>>社会公众预约</option>
                        <% } %>
                        <option value="official" <%= "official".equals(queryParams != null ? queryParams.getReservationType() : "") ? "selected" : "" %>>公务预约</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="status">预约状态</label>
                    <select id="status" name="status">
                        <option value="" <%= (queryParams == null || queryParams.getStatus() == null || queryParams.getStatus().isEmpty()) ? "selected" : "" %>>全部状态</option>
                        <option value="pending" <%= "pending".equals(queryParams != null ? queryParams.getStatus() : "") ? "selected" : "" %>>待审核</option>
                        <option value="approved" <%= "approved".equals(queryParams != null ? queryParams.getStatus() : "") ? "selected" : "" %>>已通过</option>
                        <option value="rejected" <%= "rejected".equals(queryParams != null ? queryParams.getStatus() : "") ? "selected" : "" %>>已拒绝</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="campusId">校区</label>
                    <select id="campusId" name="campusId">
                        <option value="">全部校区</option>
                        <% if (campuses != null) {
                            for (Campus campus : campuses) { %>
                                <option value="<%= campus.getId() %>" 
                                    <%= campus.getId().equals(queryParams != null ? queryParams.getCampusId() : null) ? "selected" : "" %>>
                                    <%= campus.getCampusName() %>
                                </option>
                        <% }
                        } %>
                    </select>
                </div>
                  <div class="form-group">
                    <label for="departmentId">接待部门</label>
                    <select id="departmentId" name="departmentId">
                        <% if (!isDepartmentAdmin) { %>
                        <option value="">全部部门</option>
                        <% } %>
                        <% if (departments != null) {
                            for (Department department : departments) { %>
                                <option value="<%= department.getId() %>" 
                                    <%= department.getId().equals(queryParams != null ? queryParams.getDepartmentId() : null) ? "selected" : "" %>>
                                    <%= department.getDepartmentName() %>
                                </option>
                        <% }
                        } %>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="startDate">开始日期</label>
                    <input type="date" id="startDate" name="startDate" 
                           value="<%= queryParams != null && queryParams.getStartDate() != null ? queryParams.getStartDate().format(dateFormatter) : "" %>">
                </div>
                  <div class="form-group">
                    <label for="endDate">结束日期</label>
                    <input type="date" id="endDate" name="endDate" 
                           value="<%= queryParams != null && queryParams.getEndDate() != null ? queryParams.getEndDate().format(dateFormatter) : "" %>">
                </div>
                
                <div class="form-group">
                    <label for="visitorName">访客姓名</label>
                    <input type="text" id="visitorName" name="visitorName" placeholder="请输入访客姓名" 
                           value="<%= queryParams != null && queryParams.getVisitorName() != null ? queryParams.getVisitorName() : "" %>">
                </div>
                
                <div class="form-group">
                    <label for="visitorIdCard">身份证号</label>
                    <input type="text" id="visitorIdCard" name="visitorIdCard" placeholder="请输入身份证号" 
                           value="<%= queryParams != null && queryParams.getVisitorIdCard() != null ? queryParams.getVisitorIdCard() : "" %>">
                </div>
  
            </div>
            
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">
                    🔍 查询
                </button>
                <a href="${pageContext.request.contextPath}/admin/reservation-query" class="btn btn-secondary">
                    🔄 重置
                </a>
            </div>
        </form>
        
        <div class="results-section">
            <div class="results-header">
                <div class="results-title">查询结果</div>
                <div class="results-count">
                    <% if (queryResult != null && !queryResult.hasError()) { %>
                        共找到 <%= queryResult.getTotal() %> 条记录
                        (第 <%= queryResult.getPage() %> 页，共 <%= queryResult.getTotalPages() %> 页)
                    <% } %>
                </div>
            </div>
            
            <% if (queryResult != null && queryResult.hasError()) { %>
                <div class="no-data">
                    <div style="font-size: 3rem; margin-bottom: 1rem; opacity: 0.5;">⚠️</div>
                    <div><%= queryResult.getError() %></div>
                </div>
            <% } else if (queryResult == null || queryResult.getItems() == null || queryResult.getItems().isEmpty()) { %>
                <div class="no-data">
                    <div style="font-size: 3rem; margin-bottom: 1rem; opacity: 0.5;">📋</div>
                    <div>暂无符合条件的预约记录</div>
                </div>
            <% } else { %>
                <div class="table-container">
                    <table class="data-table">                        <thead>
                            <tr>
                                <th>预约编号</th>
                                <th>类型</th>
                                <th>访客姓名</th>
                                <th>所在单位</th>
                                <th>校区</th>
                                <th>接待部门</th>
                                <th>访问日期</th>
                                <th>访问时间</th>
                                <th>状态</th>
                                <th>申请时间</th>
                                <th>审批时间</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (ReservationQueryItem item : queryResult.getItems()) { %>
                            <tr>
                                <td><%= item.getReservationNo() %></td>
                                <td>
                                    <span class="type-badge type-<%= item.getReservationType() %>">
                                        <%= item.getReservationTypeText() %>
                                    </span>
                                </td>
                                <td><%= item.getVisitorName() %></td>
                                <td><%= item.getOrganization() != null ? item.getOrganization() : "-" %></td>
                                <td><%= item.getCampusName() %></td>
                                <td><%= item.isOfficialReservation() ? (item.getDepartmentName() != null ? item.getDepartmentName() : "-") : "-" %></td>
                                <td><%= item.getVisitDate() != null ? item.getVisitDate().format(dateFormatter) : "-" %></td>
                                <td><%= item.getFormattedVisitTime() %></td>
                                <td>
                                    <span class="status-badge status-<%= item.getStatus() %>">
                                        <%= item.getStatusText() %>
                                    </span>
                                </td>
                                <td><%= item.getCreatedAt() != null ? item.getCreatedAt().format(dateTimeFormatter) : "-" %></td>
                                <td><%= item.getApprovedAt() != null ? item.getApprovedAt().format(dateTimeFormatter) : "-" %></td>
                                <td>
                                    <div class="action-buttons">                                        <% if (item.isOfficialReservation() && "pending".equals(item.getStatus())) { %>
                                            <button class="btn-action btn-approve" 
                                                    onclick="showApprovalModal('<%= item.getId() %>', '<%= item.getReservationNo() %>', '<%= item.getVisitorName() %>', 'approve', '<%= item.getReservationType() %>')">
                                                通过
                                            </button>
                                            <button class="btn-action btn-reject" 
                                                    onclick="showApprovalModal('<%= item.getId() %>', '<%= item.getReservationNo() %>', '<%= item.getVisitorName() %>', 'reject', '<%= item.getReservationType() %>')">
                                                拒绝
                                            </button>
                                        <% } else { %>
                                            <button class="btn-action btn-view" 
                                                    onclick="viewDetails('<%= item.getId() %>', '<%= item.getReservationType() %>')">
                                                查看
                                            </button>
                                        <% } %>
                                    </div>
                                </td>
                            </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
                
                <% if (queryResult.getTotalPages() > 1) { %>
                <div class="pagination">
                    <div class="pagination-info">
                        显示第 <%= ((queryResult.getPage() - 1) * queryResult.getPageSize() + 1) %> - 
                        <%= Math.min(queryResult.getPage() * queryResult.getPageSize(), queryResult.getTotal()) %> 条，
                        共 <%= queryResult.getTotal() %> 条记录
                    </div>
                    
                    <div class="pagination-links">
                        <% if (queryResult.getPage() > 1) { %>
                            <a href="?page=<%= queryResult.getPage() - 1 %>&<%= request.getQueryString() != null ? request.getQueryString().replaceAll("page=\\d+", "") : "" %>" 
                               class="pagination-link">上一页</a>
                        <% } %>
                        
                        <% 
                            int startPage = Math.max(1, queryResult.getPage() - 2);
                            int endPage = Math.min(queryResult.getTotalPages(), queryResult.getPage() + 2);
                        %>
                        
                        <% for (int i = startPage; i <= endPage; i++) { %>
                            <% if (i == queryResult.getPage()) { %>
                                <span class="pagination-link active"><%= i %></span>
                            <% } else { %>
                                <a href="?page=<%= i %>&<%= request.getQueryString() != null ? request.getQueryString().replaceAll("page=\\d+", "") : "" %>" 
                                   class="pagination-link"><%= i %></a>
                            <% } %>
                        <% } %>
                        
                        <% if (queryResult.getPage() < queryResult.getTotalPages()) { %>
                            <a href="?page=<%= queryResult.getPage() + 1 %>&<%= request.getQueryString() != null ? request.getQueryString().replaceAll("page=\\d+", "") : "" %>" 
                               class="pagination-link">下一页</a>
                        <% } %>
                    </div>
                </div>
                <% } %>
            <% } %>
        </div>    </div>
    
    <!-- 审批弹窗 -->
    <div id="approvalModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title" id="modalTitle">审批预约</h2>
                <span class="close" onclick="closeApprovalModal()">&times;</span>
            </div>
            <div class="modal-body">
                <div class="info-item">
                    <span class="info-label">预约编号：</span>
                    <span class="info-value" id="modalReservationNo"></span>
                </div>
                <div class="info-item">
                    <span class="info-label">访客姓名：</span>
                    <span class="info-value" id="modalVisitorName"></span>
                </div>
                <div class="form-group">
                    <label for="approvalComment">审批意见：</label>
                    <textarea id="approvalComment" name="approvalComment" 
                              placeholder="请输入审批意见..."></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn-modal btn-modal-secondary" onclick="closeApprovalModal()">
                    取消
                </button>
                <button type="button" class="btn-modal btn-modal-primary" onclick="submitApproval()">
                    确认
                </button>
            </div>
        </div>
    </div>
    
    <!-- 详情查看弹窗 -->
    <div id="detailModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2 class="modal-title">预约详情</h2>
                <span class="close" onclick="closeDetailModal()">&times;</span>
            </div>
            <div class="modal-body" id="detailContent">
                <!-- 详情内容将通过JavaScript动态加载 -->
            </div>
            <div class="modal-footer">
                <button type="button" class="btn-modal btn-modal-secondary" onclick="closeDetailModal()">
                    关闭
                </button>
            </div>
        </div>
    </div>
    
    <script>        // 全局变量
        let currentReservationId = null;
        let currentApprovalAction = null;
        let currentReservationType = null;
        
        // 表单自动提交（当选择项改变时）
        document.querySelectorAll('select').forEach(select => {
            select.addEventListener('change', function() {
                // 可以选择是否自动提交表单
                // document.querySelector('form').submit();
            });
        });
        
        // 日期范围验证
        document.getElementById('startDate').addEventListener('change', function() {
            const endDate = document.getElementById('endDate');
            if (this.value && endDate.value && this.value > endDate.value) {
                alert('开始日期不能晚于结束日期');
                this.value = '';
            }
        });
        
        document.getElementById('endDate').addEventListener('change', function() {
            const startDate = document.getElementById('startDate');
            if (this.value && startDate.value && this.value < startDate.value) {
                alert('结束日期不能早于开始日期');
                this.value = '';
            }
        });          // 显示审批弹窗
        function showApprovalModal(reservationId, reservationNo, visitorName, action, reservationType) {
            console.log('=== 显示审批弹窗 ===');
            console.log('传入参数:');
            console.log('  - reservationId:', reservationId);
            console.log('  - reservationNo:', reservationNo);
            console.log('  - visitorName:', visitorName);
            console.log('  - action:', action);
            console.log('  - reservationType:', reservationType);
            
            // 检查参数有效性
            if (!reservationId || !action || !reservationType) {
                console.error('参数无效:');
                console.error('  - reservationId 为空:', !reservationId);
                console.error('  - action 为空:', !action);
                console.error('  - reservationType 为空:', !reservationType);
                alert('无法打开审批弹窗：参数不完整');
                return;
            }
            
            currentReservationId = reservationId;
            currentApprovalAction = action;
            currentReservationType = reservationType;
            
            console.log('设置全局变量:');
            console.log('  - currentReservationId:', currentReservationId);
            console.log('  - currentApprovalAction:', currentApprovalAction);
            console.log('  - currentReservationType:', currentReservationType);
            
            document.getElementById('modalReservationNo').textContent = reservationNo;
            document.getElementById('modalVisitorName').textContent = visitorName;
            document.getElementById('approvalComment').value = '';
            
            if (action === 'approve') {
                document.getElementById('modalTitle').textContent = '审批通过';
                document.getElementById('approvalComment').placeholder = '请输入通过理由（可选）...';
                console.log('弹窗模式: 审批通过');
            } else {
                document.getElementById('modalTitle').textContent = '审批拒绝';
                document.getElementById('approvalComment').placeholder = '请输入拒绝理由...';
                console.log('弹窗模式: 审批拒绝');
            }
            
            document.getElementById('approvalModal').style.display = 'block';
            console.log('弹窗已显示');
        }          // 关闭审批弹窗
        function closeApprovalModal() {
            console.log('=== 关闭审批弹窗 ===');
            console.log('清除全局变量前:');
            console.log('  - currentReservationId:', currentReservationId);
            console.log('  - currentApprovalAction:', currentApprovalAction);
            console.log('  - currentReservationType:', currentReservationType);
            
            document.getElementById('approvalModal').style.display = 'none';
            currentReservationId = null;
            currentApprovalAction = null;
            currentReservationType = null;
            
            console.log('清除全局变量后:');
            console.log('  - currentReservationId:', currentReservationId);
            console.log('  - currentApprovalAction:', currentApprovalAction);
            console.log('  - currentReservationType:', currentReservationType);
        }// 提交审批
        function submitApproval() {
            console.log('=== 前端审批提交开始 ===');
            console.log('当前时间:', new Date());
            
            // 参数检查
            console.log('参数检查:');
            console.log('  - currentReservationId:', currentReservationId);
            console.log('  - currentApprovalAction:', currentApprovalAction);
            console.log('  - currentReservationType:', currentReservationType);
            
            if (!currentReservationId || !currentApprovalAction || !currentReservationType) {
                console.error('参数验证失败:');
                console.error('  - currentReservationId 为空:', !currentReservationId);
                console.error('  - currentApprovalAction 为空:', !currentApprovalAction);
                console.error('  - currentReservationType 为空:', !currentReservationType);
                alert('参数错误');
                return;
            }
            
            const comment = document.getElementById('approvalComment').value.trim();
            console.log('审批意见:', comment);
            console.log('审批意见长度:', comment.length);
            
            if (currentApprovalAction === 'reject' && !comment) {
                console.warn('拒绝审批但未填写理由');
                alert('拒绝审批必须填写理由');
                return;
            }
              // 构建请求参数
            console.log('构建请求参数...');
            const params = new URLSearchParams();
            params.append('reservationId', currentReservationId);
            params.append('action', currentApprovalAction);
            params.append('comment', comment);
            params.append('reservationType', currentReservationType);
            
            console.log('URLSearchParams 内容:');
            for (let [key, value] of params) {
                console.log('  - ' + key + ': "' + value + '"');
            }
            
            const requestUrl = '${pageContext.request.contextPath}/admin/reservation-approval';
            console.log('请求URL:', requestUrl);
            console.log('请求体:', params.toString());
            
            console.log('发送请求...');
            fetch(requestUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: params
            })
            .then(response => {
                console.log('收到响应:');
                console.log('  - 状态码:', response.status);
                console.log('  - 状态文本:', response.statusText);
                console.log('  - Content-Type:', response.headers.get('Content-Type'));
                console.log('  - 响应OK:', response.ok);
                
                if (!response.ok) {
                    throw new Error('HTTP error! status: ' + response.status);
                }
                
                return response.json();
            })
            .then(data => {
                console.log('解析JSON响应成功:');
                console.log('  - 完整响应:', JSON.stringify(data, null, 2));
                console.log('  - success:', data.success);
                console.log('  - message:', data.message);
                
                if (data.success) {
                    console.log('审批成功，准备刷新页面');
                    alert('审批成功');
                    closeApprovalModal();
                    // 刷新页面
                    window.location.reload();
                } else {
                    console.error('审批失败:', data.message);
                    alert('审批失败: ' + (data.message || '未知错误'));
                }
            })
            .catch(error => {
                console.error('请求失败:');
                console.error('  - 错误类型:', error.name);
                console.error('  - 错误消息:', error.message);
                console.error('  - 完整错误:', error);
                alert('审批失败: 网络错误 - ' + error.message);
            })
            .finally(() => {
                console.log('=== 前端审批提交结束 ===');
            });
        }
        
        // 查看详情
        function viewDetails(reservationId, reservationType) {
            // 发送请求获取预约详情
            fetch('${pageContext.request.contextPath}/admin/reservation-detail?id=' + reservationId + '&type=' + reservationType)
            .then(response => response.text())
            .then(html => {
                document.getElementById('detailContent').innerHTML = html;
                document.getElementById('detailModal').style.display = 'block';
            })
            .catch(error => {
                console.error('Error:', error);
                alert('获取详情失败');
            });
        }
        
        // 关闭详情弹窗
        function closeDetailModal() {
            document.getElementById('detailModal').style.display = 'none';
        }
        
        // 点击弹窗外部关闭弹窗
        window.onclick = function(event) {
            const approvalModal = document.getElementById('approvalModal');
            const detailModal = document.getElementById('detailModal');
            
            if (event.target === approvalModal) {
                closeApprovalModal();
            }
            if (event.target === detailModal) {
                closeDetailModal();
            }
        }
    </script>
</body>
</html>
