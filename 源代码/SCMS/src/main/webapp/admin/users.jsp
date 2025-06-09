<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.example.scms.model.Reservation" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.time.LocalDateTime" %>

<%
    // Get data from request attributes
    List<Reservation> allReservations = (List<Reservation>) request.getAttribute("allReservations");
    String searchQuery = (String) request.getAttribute("searchQuery");
    String statusFilter = (String) request.getAttribute("statusFilter");
    String typeFilter = (String) request.getAttribute("typeFilter");
    
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    // Set defaults if null
    if (searchQuery == null) searchQuery = "";
    if (statusFilter == null) statusFilter = "";
    if (typeFilter == null) typeFilter = "";
%>

<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>访客管理 - 校园通行码预约管理系统</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f8fafc;
            color: #333;
            min-height: 100vh;
        }

        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 1rem 0;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }

        .header-content {
            max-width: 1400px;
            margin: 0 auto;
            padding: 0 2rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .header h1 {
            font-size: 1.5rem;
        }

        .nav-links {
            display: flex;
            gap: 1rem;
        }

        .nav-link {
            color: white;
            text-decoration: none;
            padding: 0.5rem 1rem;
            border-radius: 5px;
            transition: background 0.3s;
        }

        .nav-link:hover {
            background: rgba(255, 255, 255, 0.2);
        }

        .main-container {
            max-width: 1400px;
            margin: 0 auto;
            padding: 2rem;
        }

        .page-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 2rem;
        }

        .page-title {
            font-size: 1.5rem;
            color: #2d3748;
        }

        .filters-section {
            background: white;
            padding: 1.5rem;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            margin-bottom: 2rem;
        }

        .filters-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 1rem;
            align-items: end;
        }

        .filter-group {
            display: flex;
            flex-direction: column;
        }

        .filter-group label {
            color: #374151;
            font-weight: 500;
            margin-bottom: 0.5rem;
        }

        .filter-input,
        .filter-select {
            padding: 0.75rem;
            border: 2px solid #e5e7eb;
            border-radius: 8px;
            font-size: 0.9rem;
            transition: border-color 0.3s;
        }

        .filter-input:focus,
        .filter-select:focus {
            outline: none;
            border-color: #667eea;
        }

        .btn {
            padding: 0.75rem 1.5rem;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            font-size: 0.9rem;
            font-weight: 500;
            transition: all 0.3s;
            text-decoration: none;
            display: inline-block;
            text-align: center;
        }

        .btn-primary {
            background: #667eea;
            color: white;
        }

        .btn-primary:hover {
            background: #5a67d8;
        }

        .btn-secondary {
            background: #6b7280;
            color: white;
        }

        .btn-secondary:hover {
            background: #4b5563;
        }

        .btn-danger {
            background: #ef4444;
            color: white;
        }

        .btn-danger:hover {
            background: #dc2626;
        }

        .btn-success {
            background: #10b981;
            color: white;
        }

        .btn-success:hover {
            background: #059669;
        }

        .data-table {
            background: white;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            overflow: hidden;
        }

        .table-header {
            background: #f7fafc;
            padding: 1rem 1.5rem;
            border-bottom: 1px solid #e2e8f0;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .table-title {
            font-size: 1.1rem;
            font-weight: 600;
            color: #2d3748;
        }

        .table-stats {
            color: #718096;
            font-size: 0.9rem;
        }

        .table-container {
            overflow-x: auto;
        }

        .reservations-table {
            width: 100%;
            border-collapse: collapse;
        }

        .reservations-table th,
        .reservations-table td {
            padding: 1rem;
            text-align: left;
            border-bottom: 1px solid #e2e8f0;
        }

        .reservations-table th {
            background: #f8fafc;
            color: #374151;
            font-weight: 600;
            font-size: 0.85rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }

        .reservations-table tr:hover {
            background: #f7fafc;
        }

        .status-badge {
            padding: 0.25rem 0.75rem;
            border-radius: 20px;
            font-size: 0.75rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }

        .status-pending {
            background: #fef3c7;
            color: #92400e;
        }

        .status-approved {
            background: #d1fae5;
            color: #065f46;
        }

        .status-rejected {
            background: #fee2e2;
            color: #991b1b;
        }

        .status-expired {
            background: #f3f4f6;
            color: #6b7280;
        }

        .type-badge {
            padding: 0.25rem 0.75rem;
            border-radius: 15px;
            font-size: 0.75rem;
            font-weight: 500;
        }

        .type-official {
            background: #e0e7ff;
            color: #3730a3;
        }

        .type-public {
            background: #ecfdf5;
            color: #047857;
        }

        .action-buttons {
            display: flex;
            gap: 0.5rem;
        }

        .btn-sm {
            padding: 0.375rem 0.75rem;
            font-size: 0.8rem;
        }

        .empty-state {
            text-align: center;
            padding: 3rem;
            color: #718096;
        }

        .empty-state-icon {
            font-size: 3rem;
            margin-bottom: 1rem;
            opacity: 0.5;
        }

        .pagination {
            display: flex;
            justify-content: center;
            align-items: center;
            gap: 0.5rem;
            padding: 1.5rem;
            background: white;
            border-top: 1px solid #e2e8f0;
        }

        .pagination-btn {
            padding: 0.5rem 1rem;
            border: 1px solid #d1d5db;
            background: white;
            color: #374151;
            text-decoration: none;
            border-radius: 6px;
            transition: all 0.3s;
        }

        .pagination-btn:hover {
            background: #f9fafb;
            border-color: #9ca3af;
        }

        .pagination-btn.active {
            background: #667eea;
            color: white;
            border-color: #667eea;
        }

        .pagination-btn.disabled {
            opacity: 0.5;
            cursor: not-allowed;
        }

        @media (max-width: 768px) {
            .header-content {
                padding: 0 1rem;
                flex-direction: column;
                gap: 1rem;
            }
            
            .main-container {
                padding: 1rem;
            }
            
            .page-header {
                flex-direction: column;
                align-items: flex-start;
                gap: 1rem;
            }
            
            .filters-grid {
                grid-template-columns: 1fr;
            }
            
            .reservations-table {
                font-size: 0.8rem;
            }
            
            .reservations-table th,
            .reservations-table td {
                padding: 0.75rem 0.5rem;
            }
            
            .action-buttons {
                flex-direction: column;
            }
        }
    </style>
</head>
<body>
    <div class="header">
        <div class="header-content">
            <h1>访客管理</h1>
            <div class="nav-links">
                <a href="dashboard.jsp" class="nav-link">仪表板</a>
                <a href="${pageContext.request.contextPath}/index.jsp" class="nav-link" onclick="return confirmLogout()">退出登录</a>
            </div>
        </div>
    </div>

    <div class="main-container">
        <div class="page-header">
            <h2 class="page-title">访客预约管理</h2>
            <div class="action-buttons">
                <a href="../user/reservation.jsp" class="btn btn-primary">新建预约</a>
            </div>
        </div>

        <!-- Filters Section -->
        <div class="filters-section">
            <form method="get" action="users.jsp">
                <div class="filters-grid">
                    <div class="filter-group">
                        <label for="search">搜索访客</label>
                        <input type="text" 
                               id="search" 
                               name="search" 
                               class="filter-input" 
                               placeholder="姓名、身份证号、电话号码"
                               value="<%= searchQuery %>">
                    </div>
                    
                    <div class="filter-group">
                        <label for="status">状态筛选</label>
                        <select id="status" name="status" class="filter-select">
                            <option value="">全部状态</option>
                            <option value="PENDING" <%= "PENDING".equals(statusFilter) ? "selected" : "" %>>待审核</option>
                            <option value="APPROVED" <%= "APPROVED".equals(statusFilter) ? "selected" : "" %>>已通过</option>
                            <option value="REJECTED" <%= "REJECTED".equals(statusFilter) ? "selected" : "" %>>已拒绝</option>
                            <option value="EXPIRED" <%= "EXPIRED".equals(statusFilter) ? "selected" : "" %>>已过期</option>
                        </select>
                    </div>
                    
                    <div class="filter-group">
                        <label for="type">类型筛选</label>
                        <select id="type" name="type" class="filter-select">
                            <option value="">全部类型</option>
                            <option value="OFFICIAL" <%= "OFFICIAL".equals(typeFilter) ? "selected" : "" %>>公务预约</option>
                            <option value="PUBLIC" <%= "PUBLIC".equals(typeFilter) ? "selected" : "" %>>普通预约</option>
                        </select>
                    </div>
                    
                    <div class="filter-group">
                        <button type="submit" class="btn btn-primary">搜索</button>
                    </div>
                </div>
            </form>
        </div>

        <!-- Data Table -->
        <div class="data-table">
            <div class="table-header">
                <div class="table-title">访客预约列表</div>
                <div class="table-stats">
                    共 <%= allReservations != null ? allReservations.size() : 0 %> 条记录
                </div>
            </div>
            
            <div class="table-container">
                <% if (allReservations != null && !allReservations.isEmpty()) { %>
                    <table class="reservations-table">
                        <thead>
                            <tr>
                                <th>预约ID</th>
                                <th>访客姓名</th>
                                <th>身份证号</th>
                                <th>联系电话</th>
                                <th>预约类型</th>
                                <th>来访目的</th>
                                <th>目的地</th>
                                <th>预约时间</th>
                                <th>申请时间</th>
                                <th>状态</th>
                                <th>操作</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Reservation reservation : allReservations) { %>
                                <tr>
                                    <td>#<%= reservation.getId() %></td>
                                    <td><%= reservation.getName() %></td>
                                    <td><%= reservation.getIdCard().substring(0, 6) + "****" + reservation.getIdCard().substring(14) %></td>
                                    <td><%= reservation.getPhone() %></td>
                                    <td>
                                        <span class="type-badge type-<%= "OFFICIAL".equals(reservation.getReservationType()) ? "official" : "public" %>">
                                            <%= "OFFICIAL".equals(reservation.getReservationType()) ? "公务预约" : "普通预约" %>
                                        </span>
                                    </td>
                                    <td style="max-width: 150px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" 
                                        title="<%= reservation.getPurpose() %>">
                                        <%= reservation.getPurpose() %>
                                    </td>
                                    <td><%= reservation.getDestination() %></td>
                                    <td><%= reservation.getReservationDate().format(formatter) %></td>
                                    <td><%= reservation.getCreatedAt().format(formatter) %></td>
                                    <td>
                                        <span class="status-badge status-<%= reservation.getStatus().toLowerCase() %>">
                                            <%= "APPROVED".equals(reservation.getStatus()) ? "已通过" : 
                                                "PENDING".equals(reservation.getStatus()) ? "待审核" : 
                                                "REJECTED".equals(reservation.getStatus()) ? "已拒绝" : "已过期" %>
                                        </span>
                                    </td>
                                    <td>
                                        <div class="action-buttons">                                            <% if ("PENDING".equals(reservation.getStatus())) { %>
                                                <button class="btn btn-success btn-sm" 
                                                        onclick="processReservation('<%= reservation.getId() %>', 'APPROVED')">
                                                    通过
                                                </button>
                                                <button class="btn btn-danger btn-sm" 
                                                        onclick="processReservation('<%= reservation.getId() %>', 'REJECTED')">
                                                    拒绝
                                                </button>
                                            <% } else { %>
                                                <button class="btn btn-secondary btn-sm" 
                                                        onclick="viewDetails('<%= reservation.getId() %>')">
                                                    查看
                                                </button>
                                            <% } %>
                                            <button class="btn btn-danger btn-sm" 
                                                    onclick="deleteReservation('<%= reservation.getId() %>')">
                                                删除
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                <% } else { %>
                    <div class="empty-state">
                        <div class="empty-state-icon">📋</div>
                        <h3>暂无访客预约记录</h3>
                        <p>没有找到符合条件的预约记录</p>
                    </div>
                <% } %>
            </div>
        </div>
    </div>

    <script>
        function processReservation(reservationId, status) {
            const action = status === 'APPROVED' ? '通过' : '拒绝';
            if (confirm('确认要' + action + '这个预约吗？')) {
                fetch('/SCMS/admin/processReservation', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: 'reservationId=' + encodeURIComponent(reservationId) + 
                          '&status=' + encodeURIComponent(status)
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert('预约已' + action);
                        location.reload();
                    } else {
                        alert('操作失败: ' + (data.message || '未知错误'));
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('操作失败，请重试');
                });
            }
        }

        function deleteReservation(reservationId) {
            if (confirm('确认要删除这个预约记录吗？此操作不可撤销！')) {
                fetch('/SCMS/admin/deleteReservation', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: 'reservationId=' + encodeURIComponent(reservationId)
                })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        alert('预约记录已删除');
                        location.reload();
                    } else {
                        alert('删除失败: ' + (data.message || '未知错误'));
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('删除失败，请重试');
                });
            }
        }

        function viewDetails(reservationId) {
            window.open('/SCMS/admin/reservationDetails?id=' + reservationId, '_blank');
        }

        function exportData() {
            const params = new URLSearchParams(window.location.search);
            params.append('export', 'true');
            window.location.href = 'users.jsp?' + params.toString();
        }        // Auto-refresh every 2 minutes
        setInterval(function() {
            if (document.visibilityState === 'visible') {
                location.reload();
            }
        }, 120000);
        
        function confirmLogout() {
            return confirm('确认退出登录？这将清除您的会话信息。');
        }
    </script>
</body>
</html>
