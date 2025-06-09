<!-- filepath: c:\code\javaweb\SCMS\src\main\webapp\user\history.jsp -->
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.example.scms.servlet.HistoryServlet.ReservationHistoryItem" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="org.example.scms.service.DebugQueryService" %>
<!DOCTYPE html>
<html lang="zh-CN">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>查询预约记录 - 校园通行码预约管理系统</title>
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
        }

        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 1rem 0;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }

        .header-content {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 2rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .header h1 {
            font-size: 1.5rem;
        }

        .back-btn {
            background: rgba(255, 255, 255, 0.2);
            color: white;
            border: none;
            padding: 0.5rem 1rem;
            border-radius: 5px;
            text-decoration: none;
            cursor: pointer;
            transition: background 0.3s;
        }

        .back-btn:hover {
            background: rgba(255, 255, 255, 0.3);
        }

        .container {
            max-width: 1200px;
            margin: 2rem auto;
            padding: 0 2rem;
        }

        .search-card {
            background: white;
            border-radius: 12px;
            padding: 2rem;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
            border: 1px solid #e2e8f0;
            margin-bottom: 2rem;
        }

        .search-title {
            font-size: 1.25rem;
            color: #1a202c;
            margin-bottom: 1rem;
            text-align: center;
        }

        .search-subtitle {
            color: #64748b;
            text-align: center;
            margin-bottom: 2rem;
        }

        .search-form {
            display: grid;
            grid-template-columns: 2fr 1fr auto;
            gap: 1rem;
            align-items: end;
        }

        .form-group {
            display: flex;
            flex-direction: column;
        }

        .form-group label {
            margin-bottom: 0.5rem;
            font-weight: 500;
            color: #374151;
        }

        .required {
            color: #ef4444;
        }

        .form-group input,
        .form-group select {
            padding: 0.75rem;
            border: 2px solid #e5e7eb;
            border-radius: 8px;
            font-size: 1rem;
            transition: all 0.3s ease;
        }

        .form-group input:focus,
        .form-group select:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }

        .search-btn {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            padding: 0.75rem 1.5rem;
            border-radius: 8px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            height: fit-content;
        }

        .search-btn:hover {
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
        }

        .results-section {
            background: white;
            border-radius: 12px;
            padding: 1.5rem;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
            border: 1px solid #e2e8f0;
        }

        .results-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 1.5rem;
            padding-bottom: 1rem;
            border-bottom: 1px solid #e5e7eb;
        }

        .results-title {
            font-size: 1.25rem;
            color: #1a202c;
        }

        .results-count {
            color: #64748b;
            font-size: 0.9rem;
        }

        .reservation-grid {
            display: grid;
            gap: 1rem;
        }

        .reservation-card {
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            padding: 1.5rem;
            transition: all 0.3s ease;
            position: relative;
            overflow: hidden;
        }

        .reservation-card:hover {
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
            border-color: #667eea;
        }

        .reservation-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 4px;
        }

        .reservation-card.pending::before {
            background: #fbbf24;
        }

        .reservation-card.approved::before {
            background: #10b981;
        }

        .reservation-card.rejected::before {
            background: #ef4444;
        }

        .reservation-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 1rem;
        }

        .reservation-id {
            font-weight: 600;
            color: #667eea;
            font-size: 0.9rem;
        }

        .reservation-type {
            padding: 0.25rem 0.75rem;
            border-radius: 20px;
            font-size: 0.75rem;
            font-weight: 500;
        }

        .type-public {
            background: #e0f2fe;
            color: #0369a1;
        }

        .type-official {
            background: #fef3c7;
            color: #92400e;
        }

        .status-badge {
            padding: 0.25rem 0.75rem;
            border-radius: 20px;
            font-size: 0.875rem;
            font-weight: 500;
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

        .reservation-info {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 1rem;
        }

        .info-item {
            display: flex;
            flex-direction: column;
            gap: 0.25rem;
        }

        .info-label {
            color: #64748b;
            font-size: 0.875rem;
            font-weight: 500;
        }

        .info-value {
            color: #1e293b;
            font-weight: 500;
        }

        .reservation-actions {
            margin-top: 1rem;
            display: flex;
            gap: 0.5rem;
            justify-content: flex-end;
        }

        .action-btn {
            padding: 0.5rem 1rem;
            border-radius: 6px;
            text-decoration: none;
            font-size: 0.875rem;
            font-weight: 500;
            transition: all 0.3s ease;
            border: 1px solid transparent;
        }

        .btn-primary {
            background: #667eea;
            color: white;
        }

        .btn-primary:hover {
            background: #5a67d8;
        }        .btn-secondary {
            background: white;
            color: #667eea;
            border-color: #667eea;
        }

        .btn-secondary:hover {
            background: #667eea;
            color: white;
        }

        .btn-disabled {
            background: #f3f4f6;
            color: #9ca3af;
            border-color: #d1d5db;
            cursor: not-allowed;
        }

        .btn-disabled:hover {
            background: #f3f4f6;
            color: #9ca3af;
        }

        .empty-state {
            text-align: center;
            padding: 3rem;
            color: #64748b;
        }

        .empty-state .icon {
            font-size: 4rem;
            margin-bottom: 1rem;
            opacity: 0.5;
        }

        .empty-state h3 {
            margin-bottom: 1rem;
            color: #374151;
        }

        .empty-state p {
            margin-bottom: 2rem;
        }

        .new-reservation-btn {
            display: inline-block;
            padding: 0.75rem 1.5rem;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            text-decoration: none;
            border-radius: 8px;
            font-weight: 500;
            transition: all 0.3s ease;
        }

        .new-reservation-btn:hover {
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
        }

        .alert {
            padding: 1rem;
            border-radius: 8px;
            margin-bottom: 1rem;
            font-size: 0.9rem;
        }

        .alert-info {
            background: #e0f2fe;
            color: #0369a1;
            border: 1px solid #bae6fd;
        }

        .alert-error {
            background: #fee;
            color: #c33;
            border: 1px solid #fcc;
        }

        @media (max-width: 768px) {
            .search-form {
                grid-template-columns: 1fr;
                gap: 1rem;
            }

            .search-btn {
                justify-self: stretch;
            }

            .reservation-info {
                grid-template-columns: 1fr;
            }

            .reservation-header {
                flex-direction: column;
                align-items: flex-start;
                gap: 0.5rem;
            }

            .reservation-actions {
                justify-content: stretch;
                flex-direction: column;
            }

            .container {
                padding: 0 1rem;
            }

            .header-content {
                padding: 0 1rem;
            }
        }
    </style>
</head>

<body>
    <header class="header">
        <div class="header-content">
            <h1>查询预约记录</h1>
            <a href="${pageContext.request.contextPath}/index.jsp" class="back-btn">返回首页</a>
        </div>
    </header>

    <div class="container">
        <div class="search-card">
            <h2 class="search-title">预约记录查询</h2>
            <p class="search-subtitle">请输入您的身份证号查询预约记录</p>

            <% if (request.getAttribute("error") !=null) { %>
                <div class="alert alert-error">
                    <%= request.getAttribute("error") %>
                </div>
                <% } %>

                    <form method="get" action="${pageContext.request.contextPath}/user/history" class="search-form">
                        <div class="form-group">
                            <label for="idCard">身份证号 <span class="required">*</span></label>
                            <input type="text" id="idCard" name="idCard" maxlength="18" placeholder="请输入18位身份证号"
                                value="<%= request.getParameter("idCard") !=null ?
                                request.getParameter("idCard") : "" %>"
                            required>
                        </div>

                        <div class="form-group">
                            <label for="type">预约类型</label>
                            <select id="type" name="type">
                                <option value="">全部类型</option>
                                <option value="public" <%="public".equals(request.getParameter("type"))
                                    ? "selected" : "" %>>社会公众来访</option>
                                <option value="official" <%="official".equals(request.getParameter("type"))
                                    ? "selected" : "" %>>公务来访</option>
                            </select>
                        </div>

                        <button type="submit" class="search-btn">🔍 查询记录</button>
                    </form>
        </div>        <% List<ReservationHistoryItem> reservations = (List<ReservationHistoryItem>) request.getAttribute("reservations");
           String searchIdCard = (String) request.getAttribute("queryIdCard");
           if (searchIdCard == null) {
               searchIdCard = request.getParameter("idCard");
           }
             // 调试信息（仅用于开发）
           boolean showDebug = false;
           String debugInfo = "";
           if (showDebug && searchIdCard != null && !searchIdCard.trim().isEmpty()) {
               // 直接执行数据库查询以验证哈希和查询结果
               List<String> directResults = DebugQueryService.debugDirectQuery(searchIdCard);
               List<String> validationResults = DebugQueryService.validateHashAndQuery(searchIdCard);
               
               debugInfo += "<div style='background:#f0f0f0; border:1px solid #ccc; padding:10px; margin:10px 0; font-family:monospace; white-space:pre-wrap;'>";
               debugInfo += "<strong>基本调试信息</strong><br>";
               debugInfo += "- 查询ID卡: " + searchIdCard + "<br>";
               debugInfo += "- 查询结果属性: " + (reservations != null ? "存在" : "不存在") + "<br>";
               if (reservations != null) {
                   debugInfo += "- 查询结果记录数: " + reservations.size() + "<br><br>";
                   
                   if (reservations.isEmpty()) {
                       debugInfo += "- 无记录<br>";
                   } else {
                       for (int i = 0; i < Math.min(reservations.size(), 3); i++) {
                           ReservationHistoryItem item = reservations.get(i);
                           debugInfo += "- 记录 #" + (i+1) + ": 类型=" + item.getType() 
                                      + ", 姓名=" + item.getVisitorName()
                                      + ", 状态=" + item.getStatus() + "<br>";
                       }
                   }
                   
                   debugInfo += "<br><strong>直接SQL查询结果</strong><br>";
                   for (String result : directResults) {
                       debugInfo += result + "<br>";
                   }
                   
                   debugInfo += "<br><strong>哈希验证结果</strong><br>";
                   for (String result : validationResults) {
                       debugInfo += result + "<br>";
                   }
               }
               debugInfo += "</div>";
           }
        %>        <% if (searchIdCard != null && !searchIdCard.trim().isEmpty()) { %>
            <% if (showDebug) { %>
                <%= debugInfo %>
            <% } %>
            
            <div class="results-section">
                <div class="results-header">
                    <h2 class="results-title">查询结果</h2>
                    <span class="results-count">
                        共找到 <%= reservations != null ? reservations.size() : 0 %> 条记录
                    </span>
                </div><% if (reservations != null && !reservations.isEmpty()) { %>
                    <div class="reservation-grid">
                        <% for (ReservationHistoryItem item : reservations) { 
                            String statusClass = "pending";
                            String statusText = "待审核";
                            String typeClass = "type-public";
                            String typeText = "社会公众来访";
                            
                            if ("approved".equals(item.getStatus())) {
                                statusClass = "approved";
                                statusText = "已通过";
                            } else if ("rejected".equals(item.getStatus())) {
                                statusClass = "rejected";
                                statusText = "已拒绝";
                            }
                            
                            if ("official".equals(item.getType())) {
                                typeClass = "type-official";
                                typeText = "公务来访";
                            }
                        %>
                            <div class="reservation-card <%= statusClass %>">
                                <div class="reservation-header">
                                    <div class="reservation-id">预约编号：<%= item.getReservationNo() %></div>
                                    <div style="display: flex; gap: 0.5rem;">
                                        <span class="reservation-type <%= typeClass %>"><%= typeText %></span>
                                        <span class="status-badge status-<%= statusClass %>"><%= statusText %></span>
                                    </div>
                                </div>
                                
                                <div class="reservation-info">
                                    <div class="info-item">
                                        <span class="info-label">申请人</span>
                                        <span class="info-value"><%= item.getVisitorName() %></span>
                                    </div>
                                    <div class="info-item">
                                        <span class="info-label">访问日期</span>
                                        <span class="info-value"><%= item.getVisitDate() %></span>
                                    </div>
                                    <div class="info-item">
                                        <span class="info-label">访问时间</span>
                                        <span class="info-value"><%= item.getVisitTimeStart() %> - <%= item.getVisitTimeEnd() %></span>
                                    </div>
                                    <div class="info-item">
                                        <span class="info-label">申请时间</span>
                                        <span class="info-value"><%= item.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) %></span>
                                    </div>
                                </div>                                  <% if ("approved".equals(item.getStatus())) { 
                                    // 检查预约是否已生效（访问日期是否为今天或未来）
                                    java.time.LocalDate visitDate = item.getVisitDate();
                                    java.time.LocalDate today = java.time.LocalDate.now();
                                    boolean isEffective = !visitDate.isBefore(today);
                                    boolean isExpired = visitDate.isBefore(today);
                                %>                                <div class="reservation-actions">
                                    <% if (isEffective) { %>
                                        <a href="${pageContext.request.contextPath}/user/passcode?type=<%= item.getType() %>&id=<%= item.getId() %>" class="action-btn btn-primary">查看通行码</a>
                                    <% } else if (isExpired) { %>
                                        <a href="${pageContext.request.contextPath}/user/passcode?type=<%= item.getType() %>&id=<%= item.getId() %>" class="action-btn btn-secondary">查看通行码(已过期)</a>
                                    <% } else { %>
                                        <a href="${pageContext.request.contextPath}/user/passcode?type=<%= item.getType() %>&id=<%= item.getId() %>" class="action-btn btn-disabled">查看通行码(未生效)</a>
                                    <% } %>
                                </div>
                                <% } %>
                            </div>
                        <% } %>
                    </div>
                <% } else { %>
                    <div class="empty-state">
                        <div class="icon">📝</div>
                        <h3>未找到预约记录</h3>
                        <p>该身份证号暂无预约记录，您可以创建新的预约申请</p>
                        <a href="${pageContext.request.contextPath}/user/reservation.jsp" class="new-reservation-btn">
                            ➕ 新建预约
                        </a>
                    </div>
                <% } %>
            </div>
        <% } else { %>
            <div class="alert alert-info">
                <strong>📋 使用说明：</strong>
                <br>• 输入您的18位身份证号即可查询所有预约记录
                <br>• 支持查询社会公众来访和公务来访两种预约类型
                <br>• 无需注册账户，使用身份证号即可查询和管理预约
                <br>• 系统会保留您的历史预约记录，方便随时查询
            </div>
        <% } %>
    </div>

    <script>
        // 身份证号码格式验证
        document.getElementById('idCard').addEventListener('input', function () {
            const idCard = this.value;
            if (idCard.length === 18) {
                const pattern = /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/;
                if (!pattern.test(idCard)) {
                    this.setCustomValidity('请输入正确的18位身份证号码');
                } else {
                    this.setCustomValidity('');
                }
            }
        });

        // 自动格式化身份证号码输入
        document.getElementById('idCard').addEventListener('keypress', function (e) {
            // 只允许输入数字和X
            const char = String.fromCharCode(e.which);
            const isValid = /[0-9Xx]/.test(char);
            if (!isValid) {
                e.preventDefault();
            }
        });

        // 防止连续提交
        document.querySelector('.search-form').addEventListener('submit', function (e) {
            const submitBtn = document.querySelector('.search-btn');
            submitBtn.disabled = true;
            submitBtn.textContent = '查询中...';

            // 如果验证失败，重新启用按钮
            setTimeout(() => {
                if (!this.checkValidity()) {
                    submitBtn.disabled = false;
                    submitBtn.textContent = '🔍 查询记录';
                }
            }, 100);
        });
    </script>
</body>
</html>
