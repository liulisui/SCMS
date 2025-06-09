<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ page import="org.example.scms.model.Reservation" %>
    <%@ page import="org.example.scms.model.PublicReservation" %>
    <%@ page import="org.example.scms.model.OfficialReservation" %>
    <%@ page import="org.example.scms.util.DataEncryptionUtil" %>
        <%@ page import="java.time.LocalDateTime" %>
            <%@ page import="java.time.format.DateTimeFormatter" %>

                <% 
                    Object reservationObj = request.getAttribute("reservation");
                    String reservationType = (String) request.getAttribute("reservationType");
                    
                    // 根据预约类型获取预约信息
                    String reservationNo = "";
                    String visitorName = "";
                    String contactPhone = "";
                    String department = "";
                    String visitPurpose = "";
                    java.time.LocalDate visitDate = null;
                    java.time.LocalTime visitTimeStart = null;
                    java.time.LocalTime visitTimeEnd = null;
                      if ("public".equals(reservationType) && reservationObj instanceof PublicReservation) {
                        PublicReservation reservation = (PublicReservation) reservationObj;
                        reservationNo = reservation.getReservationNo();
                        visitorName = reservation.getVisitorName();
                        // 解密手机号
                        try {
                            contactPhone = DataEncryptionUtil.decrypt(reservation.getVisitorPhoneEncrypted());
                        } catch (Exception e) {
                            contactPhone = "***";
                        }
                        department = reservation.getOrganization();
                        visitPurpose = reservation.getVisitReason();
                        visitDate = reservation.getVisitDate();
                        visitTimeStart = reservation.getVisitTimeStart();
                        visitTimeEnd = reservation.getVisitTimeEnd();
                    } else if ("official".equals(reservationType) && reservationObj instanceof OfficialReservation) {
                        OfficialReservation reservation = (OfficialReservation) reservationObj;
                        reservationNo = reservation.getReservationNo();                        visitorName = reservation.getVisitorName();
                        // 解密手机号
                        try {
                            contactPhone = DataEncryptionUtil.decrypt(reservation.getVisitorPhoneEncrypted());
                        } catch (Exception e) {
                            contactPhone = "***";
                        }
                        
                        // 获取对接部门名称
                        String departmentName = (String) request.getAttribute("departmentName");
                        department = departmentName != null ? departmentName : "未知部门";
                        
                        visitPurpose = reservation.getVisitReason();
                        visitDate = reservation.getVisitDate();
                        visitTimeStart = reservation.getVisitTimeStart();
                        visitTimeEnd = reservation.getVisitTimeEnd();
                    }
                    
                    String qrCode = (String) request.getAttribute("qrCode"); 
                    String status = (String) request.getAttribute("status"); 
                    String statusText = (String) request.getAttribute("statusText"); 
                    String validTime = (String) request.getAttribute("validTime"); 
                    String errorMessage = (String) request.getAttribute("errorMessage"); 
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"); 
                %>

                    <!DOCTYPE html>
                    <html lang="zh-CN">

                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>预约结果 - 校园通行码预约管理系统</title>
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
                                display: flex;
                                flex-direction: column;
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

                            .back-btn {
                                background: rgba(255, 255, 255, 0.2);
                                color: white;
                                border: none;
                                padding: 0.5rem 1rem;
                                border-radius: 5px;
                                text-decoration: none;
                                transition: background 0.3s;
                            }

                            .back-btn:hover {
                                background: rgba(255, 255, 255, 0.3);
                            }

                            .container {
                                max-width: 600px;
                                margin: 2rem auto;
                                padding: 0 2rem;
                                flex: 1;
                                display: flex;
                                flex-direction: column;
                                justify-content: center;
                            }

                            .result-card {
                                background: white;
                                border-radius: 20px;
                                padding: 2rem;
                                box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
                                border: 1px solid #e2e8f0;
                                text-align: center;
                                position: relative;
                                overflow: hidden;
                            }

                            .result-card::before {
                                content: '';
                                position: absolute;
                                top: 0;
                                left: 0;
                                right: 0;
                                height: 5px;
                                background: linear-gradient(90deg, #667eea, #764ba2, #667eea);
                                background-size: 200% 100%;
                                animation: shimmer 2s infinite;
                            }

                            @keyframes shimmer {
                                0% {
                                    background-position: -200% 0;
                                }

                                100% {
                                    background-position: 200% 0;
                                }
                            }

                            .status-indicator {
                                display: block;
                                padding: 0.75rem 2rem;
                                border-radius: 25px;
                                font-weight: 600;
                                font-size: 1.2rem;
                                margin: 0 auto 1.5rem auto;
                                position: relative;
                                width: fit-content;
                            }

                            .status-pending {
                                background: #fef3c7;
                                color: #92400e;
                                border: 2px solid #f59e0b;
                            }

                            .status-valid {
                                background: #d1fae5;
                                color: #065f46;
                                border: 2px solid #10b981;
                            }

                            .status-early {
                                background: #e0f2fe;
                                color: #0c4a6e;
                                border: 2px solid #0ea5e9;
                            }

                            .status-expired {
                                background: #fee2e2;
                                color: #991b1b;
                                border: 2px solid #ef4444;
                            }

                            .status-rejected {
                                background: #f1f5f9;
                                color: #64748b;
                                border: 2px solid #9ca3af;
                            }

                            .qr-container {
                                margin: 2rem auto;
                                padding: 1.5rem;
                                background: #fff;
                                border-radius: 15px;
                                box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
                                display: block;
                                border: 2px solid #e2e8f0;
                                width: fit-content;
                            }

                            .qr-code {
                                display: block;
                                max-width: 200px;
                                height: auto;
                                margin: 0 auto;
                                border-radius: 10px;
                            }

                            .qr-placeholder {
                                width: 200px;
                                height: 200px;
                                background: #f1f5f9;
                                border-radius: 10px;
                                display: flex;
                                align-items: center;
                                justify-content: center;
                                color: #64748b;
                                font-size: 1rem;
                                text-align: center;
                                margin: 0 auto;
                            }

                            .reservation-details {
                                background: #f8fafc;
                                border-radius: 12px;
                                padding: 1.5rem;
                                margin: 1.5rem 0;
                                text-align: left;
                            }

                            .detail-grid {
                                display: grid;
                                gap: 1rem;
                                grid-template-columns: 1fr;
                            }

                            .detail-item {
                                display: flex;
                                justify-content: space-between;
                                align-items: center;
                                padding: 0.75rem 0;
                                border-bottom: 1px solid #e2e8f0;
                            }

                            .detail-item:last-child {
                                border-bottom: none;
                            }

                            .detail-label {
                                color: #64748b;
                                font-weight: 500;
                            }

                            .detail-value {
                                color: #1e293b;
                                font-weight: 600;
                            }

                            .usage-tips {
                                background: #eff6ff;
                                border: 1px solid #bfdbfe;
                                border-radius: 8px;
                                padding: 1rem;
                                margin-top: 1.5rem;
                                text-align: left;
                            }

                            .usage-tips h4 {
                                color: #1e40af;
                                margin-bottom: 0.5rem;
                            }

                            .usage-tips ul {
                                color: #1e40af;
                                padding-left: 1.5rem;
                            }

                            .usage-tips li {
                                margin-bottom: 0.25rem;
                            }

                            .action-buttons {
                                margin-top: 2rem;
                                display: flex;
                                gap: 1rem;
                                justify-content: center;
                            }

                            .btn {
                                padding: 0.75rem 1.5rem;
                                border-radius: 8px;
                                font-weight: 500;
                                cursor: pointer;
                                text-decoration: none;
                                transition: all 0.3s ease;
                                border: none;
                            }

                            .btn-primary {
                                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                                color: white;
                            }

                            .btn-secondary {
                                background: #f1f5f9;
                                color: #64748b;
                                border: 1px solid #e2e8f0;
                            }

                            .btn:hover {
                                transform: translateY(-2px);
                                box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
                            }

                            @media (max-width: 768px) {
                                .container {
                                    padding: 0 1rem;
                                }

                                .result-card {
                                    padding: 1.5rem;
                                }

                                .header-content {
                                    padding: 0 1rem;
                                }

                                .action-buttons {
                                    flex-direction: column;
                                }
                            }
                        </style>
                    </head>

                    <body>                        <header class="header">
                            <div class="header-content">
                                <a href="${pageContext.request.contextPath}/index.jsp" class="back-btn">← 返回</a>
                                <h1>预约结果</h1>
                            </div>
                        </header>

                        <div class="container">
                            <% if (errorMessage !=null) { %>
                                <div class="result-card">
                                    <div class="status-indicator status-rejected">
                                        预约查询失败
                                    </div>
                                    <div class="qr-container">
                                        <div class="qr-placeholder">
                                            <%= errorMessage %>
                                        </div>
                                    </div>
                                    <div class="action-buttons">
                                        <a href="${pageContext.request.contextPath}/user/reservation"
                                            class="btn btn-primary">重新预约</a>
                                        <a href="${pageContext.request.contextPath}/user/history"
                                            class="btn btn-secondary">查询历史</a>
                                    </div>
                                </div>
                                <% } else if (reservationObj != null) { %>
                                    <div class="result-card">
                                        <div class="status-indicator status-<%= status %>">
                                            <%= statusText %>
                                        </div>

                                        <% if (qrCode !=null) { %>
                                            <div class="qr-container">
                                                <img src="<%= qrCode %>" alt="校园通行码" class="qr-code">
                                            </div>
                                            <% } else { %>
                                                <div class="qr-container">
                                                    <div class="qr-placeholder">
                                                        <% if ("pending".equals(status)) { %>
                                                            等待审核通过后<br>生成通行码
                                                            <% } else if ("rejected".equals(status)) { %>
                                                                审核未通过<br>无法生成通行码
                                                                <% } else { %>
                                                                    通行码暂不可用
                                                                    <% } %>
                                                    </div>
                                                </div>
                                                <% } %>

                                                    <div class="reservation-details">
                                                        <div class="detail-grid">                                                            <div class="detail-item">
                                                                <span class="detail-label">预约编号</span>
                                                                <span class="detail-value">
                                                                    <%= reservationNo %>
                                                                </span>
                                                            </div>
                                                            <div class="detail-item">
                                                                <span class="detail-label">访问类型</span>
                                                                <span class="detail-value">
                                                                    <%= "public".equals(reservationType) ? "社会公众" : "公务预约" %>
                                                                </span>
                                                            </div>
                                                            <div class="detail-item">
                                                                <span class="detail-label">访问时间</span>
                                                                <span class="detail-value">
                                                                    <%= visitDate.toString() + " " + visitTimeStart.toString() + " - " + visitTimeEnd.toString() %>
                                                                </span>
                                                            </div>                                                            <% if (visitPurpose != null && !visitPurpose.trim().isEmpty()) { %>                                                            <div class="detail-item">
                                                                <span class="detail-label">访问目的</span>
                                                                <span class="detail-value">
                                                                    <%= visitPurpose %>
                                                                </span>
                                                            </div>
                                                                <% } %>                                                                    <div class="detail-item">
                                                                        <span class="detail-label">联系电话</span>
                                                                        <span class="detail-value">
                                                                            <%= contactPhone %>
                                                                        </span>
                                                                    </div>
                                                                    <% if ("official".equals(reservationType) && department != null && !department.trim().isEmpty()) { %>
                                                                        <div class="detail-item">
                                                                            <span class="detail-label">对接部门</span>
                                                                            <span class="detail-value">
                                                                                <%= department %>
                                                                            </span>
                                                                        </div>
                                                                    <% } %>
                                                        </div>
                                                    </div>

                                                    <% if ("valid".equals(status)) { %>
                                                        <div class="usage-tips">
                                                            <h4>使用说明</h4>
                                                            <ul>
                                                                <li>请在访问时出示此通行码</li>
                                                                <li>通行码包含您的身份验证信息</li>
                                                                <li>请勿截图分享给他人</li>
                                                                <li>如遇验证问题，请联系门卫</li>
                                                            </ul>
                                                        </div>
                                                        <% } else if ("early".equals(status)) { %>
                                                            <div class="usage-tips">
                                                                <h4>温馨提示</h4>
                                                                <ul>
                                                                    <li>通行码将在访问时间前30分钟生效</li>
                                                                    <li>请准时到达指定地点</li>
                                                                    <li>如需修改访问时间，请重新预约</li>
                                                                </ul>
                                                            </div>
                                                            <% } else if ("pending".equals(status)) { %>
                                                                <div class="usage-tips">
                                                                    <h4>审核说明</h4>
                                                                    <ul>
                                                                        <li>您的预约申请正在审核中</li>
                                                                        <li>一般情况下1-2个工作日内完成审核</li>
                                                                        <li>审核结果将通过系统通知您</li>
                                                                        <li>可在"我的预约"中查看审核进度</li>
                                                                    </ul>
                                                                </div>
                                                                <% } %>
                                                                    <div class="action-buttons">
                                                                        <a href="${pageContext.request.contextPath}/user/history"
                                                                            class="btn btn-primary">查询我的预约</a>
                                                                        <a href="${pageContext.request.contextPath}/user/reservation"
                                                                            class="btn btn-secondary">再次预约</a>
                                                                    </div>
                                    </div>
                                    <% } else { %>
                                        <div class="result-card">
                                            <div class="status-indicator status-rejected">
                                                预约信息不存在
                                            </div>
                                            <div class="qr-container">
                                                <div class="qr-placeholder">
                                                    未找到相关预约信息<br>请检查预约编号或重新预约
                                                </div>
                                            </div>
                                            <div class="action-buttons">
                                                <a href="${pageContext.request.contextPath}/user/reservation"
                                                    class="btn btn-primary">立即预约</a>
                                                <a href="${pageContext.request.contextPath}/user/history"
                                                    class="btn btn-secondary">查询历史</a>
                                            </div>
                                        </div>
                                        <% } %>
                        </div>
                    </body>

                    </html>