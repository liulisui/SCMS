<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.example.scms.model.Reservation" %>
<%@ page import="org.example.scms.model.PublicReservation" %>
<%@ page import="org.example.scms.model.OfficialReservation" %>
<%@ page import="org.example.scms.util.DataEncryptionUtil" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
            <!DOCTYPE html>
            <html lang="zh-CN">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>校园通行码 - 校园管理系统</title>
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

                    .passcode-card {
                        background: white;
                        border-radius: 20px;
                        padding: 2rem;
                        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
                        border: 1px solid #e2e8f0;
                        text-align: center;
                        position: relative;
                        overflow: hidden;
                    }

                    .passcode-card::before {
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
                        display: inline-block;
                        padding: 0.5rem 1rem;
                        border-radius: 20px;
                        font-size: 0.9rem;
                        font-weight: 600;
                        margin-bottom: 1rem;
                    }

                    .status-valid {
                        background: #d1fae5;
                        color: #065f46;
                    }

                    .status-approved {
                        background: #d1fae5;
                        color: #065f46;
                    }                    .status-early {
                        background: #fef3c7;
                        color: #92400e;
                    }                    .status-pending {
                        background: #fef3c7;
                        color: #92400e;
                    }

                    .status-expired {
                        background: #fee2e2;
                        color: #991b1b;
                    }

                    .reservation-title {
                        font-size: 1.5rem;
                        color: #1a202c;
                        margin-bottom: 0.5rem;
                    }

                    .reservation-subtitle {
                        color: #64748b;
                        margin-bottom: 2rem;
                    }

                    .qr-section {
                        margin: 2rem 0;
                    }

                    .qr-code {
                        display: inline-block;
                        padding: 1rem;
                        background: white;
                        border: 2px solid #e5e7eb;
                        border-radius: 12px;
                        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
                    }                    .qr-placeholder {
                        width: 200px;
                        height: 200px;
                        background: #f8fafc;
                        border: 2px dashed #cbd5e1;
                        border-radius: 8px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        flex-direction: column;
                        color: #64748b;
                        margin: 0 auto;
                        text-align: center;
                        padding: 1rem;
                    }

                    .qr-placeholder.early {
                        background: #fefbf3;
                        border-color: #fbbf24;
                        color: #92400e;
                    }

                    .access-code {
                        font-size: 1.5rem;
                        font-weight: bold;
                        color: #667eea;
                        margin: 1rem 0;
                        padding: 1rem;
                        background: rgba(102, 126, 234, 0.1);
                        border-radius: 8px;
                        border: 2px solid rgba(102, 126, 234, 0.2);
                    }

                    .info-grid {
                        display: grid;
                        grid-template-columns: 1fr 1fr;
                        gap: 1rem;
                        margin: 2rem 0;
                        text-align: left;
                    }

                    .info-item {
                        padding: 1rem;
                        background: #f8fafc;
                        border-radius: 8px;
                        border: 1px solid #e2e8f0;
                    }

                    .info-label {
                        font-size: 0.875rem;
                        color: #64748b;
                        margin-bottom: 0.25rem;
                    }

                    .info-value {
                        font-weight: 600;
                        color: #1e293b;
                    }

                    .actions {
                        display: flex;
                        gap: 1rem;
                        justify-content: center;
                        margin-top: 2rem;
                    }

                    .action-btn {
                        padding: 0.75rem 1.5rem;
                        border-radius: 8px;
                        text-decoration: none;
                        font-weight: 600;
                        transition: all 0.3s ease;
                        border: none;
                        cursor: pointer;
                    }

                    .btn-primary {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                    }

                    .btn-primary:hover {
                        transform: translateY(-1px);
                        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
                    }

                    .btn-secondary {
                        background: white;
                        color: #667eea;
                        border: 2px solid #667eea;
                    }

                    .btn-secondary:hover {
                        background: #667eea;
                        color: white;
                    }

                    .notice {
                        background: #e0f2fe;
                        border: 1px solid #bae6fd;
                        color: #0369a1;
                        padding: 1rem;
                        border-radius: 8px;
                        margin-top: 2rem;
                        text-align: left;
                    }

                    .notice h4 {
                        margin-bottom: 0.5rem;
                    }

                    .notice ul {
                        margin-left: 1rem;
                    }

                    .notice li {
                        margin-bottom: 0.25rem;
                    }

                    .error-state {
                        text-align: center;
                        padding: 3rem;
                        color: #64748b;
                    }

                    .error-state .icon {
                        font-size: 4rem;
                        margin-bottom: 1rem;
                        opacity: 0.5;
                    }

                    .error-state h3 {
                        margin-bottom: 1rem;
                        color: #374151;
                    }

                    .error-state p {
                        margin-bottom: 2rem;
                    }

                    @media (max-width: 768px) {
                        .info-grid {
                            grid-template-columns: 1fr;
                        }

                        .actions {
                            flex-direction: column;
                        }

                        .container {
                            padding: 0 1rem;
                        }

                        .header-content {
                            padding: 0 1rem;
                        }

                        .passcode-card {
                            padding: 1.5rem;
                        }
                    }
                </style>
            </head>            <body>                <%
                    // 获取预约信息和通行码数据
                    Object reservationObj = request.getAttribute("reservation");
                    PublicReservation publicReservation = null;
                    OfficialReservation officialReservation = null;
                    
                    // 根据对象类型进行转换
                    if (reservationObj instanceof PublicReservation) {
                        publicReservation = (PublicReservation) reservationObj;
                    } else if (reservationObj instanceof OfficialReservation) {
                        officialReservation = (OfficialReservation) reservationObj;
                    }
                    
                    String qrCodeData = (String) request.getAttribute("qrCodeData");
                    String accessCode = (String) request.getAttribute("accessCode");
                    String status = (String) request.getAttribute("status");
                    String errorMessage = (String) request.getAttribute("error");
                    
                    // 参数
                    String reservationId = request.getParameter("id");
                    String reservationType = request.getParameter("type");                    
                    // 如果没有获取到参数，尝试从预约对象中获取
                    if (publicReservation != null) {
                        if (reservationId == null || reservationId.isEmpty()) {
                            reservationId = String.valueOf(publicReservation.getId());
                        }
                        if (reservationType == null || reservationType.isEmpty()) {
                            reservationType = "public";
                        }
                    } else if (officialReservation != null) {
                        if (reservationId == null || reservationId.isEmpty()) {
                            reservationId = String.valueOf(officialReservation.getId());
                        }
                        if (reservationType == null || reservationType.isEmpty()) {
                            reservationType = "official";
                        }
                    }
                %>

                    <header class="header">
                        <div class="header-content">
                            <h1>校园通行码</h1>
                            <a href="${pageContext.request.contextPath}/user/history" class="back-btn">返回查询</a>
                        </div>
                    </header>

                    <div class="container">
                        <div class="passcode-card">
                            <% if (errorMessage !=null) { %>
                                <div class="error-state">
                                    <div class="icon">❌</div>
                                    <h3>获取通行码失败</h3>
                                    <p>
                                        <%= errorMessage %>
                                    </p>
                                    <a href="${pageContext.request.contextPath}/user/history"
                                        class="action-btn btn-primary">
                                        返回查询页面
                                    </a>
                                </div>
                                <% } else if (publicReservation == null && officialReservation == null) { %>
                                    <div class="error-state">
                                        <div class="icon">🔍</div>
                                        <h3>未找到预约记录</h3>
                                        <p>请检查预约ID是否正确，或者该预约记录可能已被删除</p>
                                        <a href="${pageContext.request.contextPath}/user/history"
                                            class="action-btn btn-primary">
                                            返回查询页面
                                        </a>
                                    </div>
                                    <% } else { %>                                        <!-- 状态指示器 -->
                                        <% if ("valid".equals(status) || "approved" .equals(status)) { %>
                                            <div class="status-indicator status-valid">
                                                ✅ 通行码有效
                                            </div>
                                            <% } else if ("early".equals(status)) { %>
                                                <div class="status-indicator status-early">
                                                    ⏰ 还未生效
                                                </div>
                                                <% } else if ("expired".equals(status)) { %>
                                                    <div class="status-indicator status-expired">
                                                        ⏰ 通行码已过期
                                                    </div>
                                                    <% } else if ("pending".equals(status)) { %>
                                                        <div class="status-indicator status-pending">
                                                            ⏳ 等待审核通过
                                                        </div>
                                                        <% } else if ("rejected".equals(status)) { %>
                                                            <div class="status-indicator status-rejected">
                                                                ❌ 预约已被拒绝
                                                            </div>
                                                            <% } %>

                                                            <h1 class="reservation-title">校园通行码</h1>
                                                            <p class="reservation-subtitle">请在有效时间内使用此通行码进入校园</p>                                            <!-- 二维码区域 -->                                                            <div class="qr-section">                                                                <% if ("valid".equals(status) || "approved".equals(status)) { %>
                                                                    <!-- 有效的二维码 -->
                                                                    <div class="qr-code">
                                                                        <% if (qrCodeData != null && !qrCodeData.isEmpty()) { %>
                                                                            <img src="data:image/png;base64,<%= qrCodeData %>"
                                                                                alt="校园通行码二维码"
                                                                                style="width: 200px; height: 200px;">
                                                                        <% } else { %>
                                                                            <div style="width: 200px; height: 200px; display: flex; 
                                                                                    align-items: center; justify-content: center; 
                                                                                    border: 2px solid #e5e7eb; border-radius: 12px;">
                                                                                <span>二维码加载中...</span>
                                                                            </div>
                                                                        <% } %>
                                                                    </div>                                                                <% } else if ("early".equals(status)) { %>
                                                                    <!-- 未生效状态，显示提示信息而不是二维码 -->
                                                                    <div class="qr-placeholder early">
                                                                        <div style="font-size: 3rem; margin-bottom: 0.5rem;">
                                                                            ⏰
                                                                        </div>
                                                                        <div style="font-size: 1.2rem; font-weight: 600; color: #92400e; margin-bottom: 1rem;">
                                                                            预约还未生效
                                                                        </div>
                                                                        <div style="color: #64748b; line-height: 1.5; font-size: 0.9rem;">
                                                                            通行码将在访问日期当天生成<br>
                                                                            请在预约时间前30分钟再次查看
                                                                        </div>
                                                                    </div>
                                                                <% } else if (qrCodeData != null && ("expired".equals(status) || "rejected".equals(status))) { %>
                                                                    <!-- 无效的二维码，带禁止标志 -->
                                                                    <div class="qr-code" style="position: relative; display: inline-block;">
                                                                        <img src="data:image/png;base64,<%= qrCodeData %>"
                                                                            alt="校园通行码二维码"
                                                                            style="width: 200px; height: 200px; opacity: 0.3; filter: grayscale(100%);">
                                                                        <!-- 深灰色禁止标志覆盖层 -->
                                                                        <div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
                                                                                    width: 120px; height: 120px; border-radius: 60px; 
                                                                                    background: rgba(64, 64, 64, 0.9); 
                                                                                    display: flex; align-items: center; justify-content: center;
                                                                                    border: 4px solid #404040;">
                                                                            <!-- 白色斜杠禁止线 -->
                                                                            <div style="width: 80px; height: 6px; background: white; 
                                                                                        transform: rotate(45deg); border-radius: 3px;"></div>
                                                                        </div>
                                                                    </div>
                                                                    <% } else if (qrCodeData != null) { %>
                                                                    <!-- 其他状态的二维码（如等待中） -->
                                                                    <div class="qr-code">
                                                                        <img src="data:image/png;base64,<%= qrCodeData %>"
                                                                            alt="校园通行码二维码"
                                                                            style="width: 200px; height: 200px; opacity: 0.6;">
                                                                    </div>                                                                    <% } else { %>
                                                                    <!-- 无二维码数据时的占位符 -->
                                                                    <div class="qr-placeholder">
                                                                        <div style="font-size: 3rem; margin-bottom: 0.5rem;">
                                                                            <% if (("valid".equals(status) || "approved".equals(status))) { %>
                                                                                ✅
                                                                            <% } else if ("expired".equals(status) || "rejected".equals(status)) { %>
                                                                                🚫
                                                                            <% } else { %>
                                                                                📱
                                                                            <% } %>
                                                                        </div>
                                                                        <div>
                                                                            <% if (("valid".equals(status) || "approved".equals(status))) { %>
                                                                                通行码有效
                                                                            <% } else if ("early".equals(status)) { %>
                                                                                还未生效
                                                                            <% } else if ("pending".equals(status)) { %>
                                                                                通行码生成中
                                                                            <% } else if ("rejected".equals(status)) { %>
                                                                                预约被拒绝
                                                                            <% } else if ("expired".equals(status)) { %>
                                                                                通行码已过期
                                                                            <% } else { %>
                                                                                二维码不可用
                                                                            <% } %>
                                                                        </div>
                                                                    </div>
                                                                    <% } %>
                                                            </div><!-- 通行码 -->
                                                            <% if (accessCode !=null && "valid".equals(status)) { %>
                                                                <div class="access-code">
                                                                    通行码：<%= accessCode %>
                                                                </div>
                                                                <% } %>

                                                                    <!-- 预约信息 -->
                                                                    <div class="info-grid">
                                                                        <div class="info-item">
                                                                            <div class="info-label">预约类型</div>
                                                                            <div class="info-value">
                                                                                <% if
                                                                                    ("official".equals(reservationType))
                                                                                    { %>
                                                                                    💼 公务来访
                                                                                    <% } else { %>
                                                                                        👥 校园参观
                                                                                        <% } %>
                                                                            </div>
                                                                        </div>
                                                                        <div class="info-item">
                                                                            <div class="info-label">预约编号</div>
                                                                            <div class="info-value">
                                                                                <%= reservationId !=null ? reservationId
                                                                                    : "N/A" %>
                                                                            </div>
                                                                        </div>                                                                        <div class="info-item">
                                                                            <div class="info-label">申请人</div>
                                                                            <div class="info-value">                                                                                <%
                                                                                    String userName = null;
                                                                                    if (publicReservation != null) {
                                                                                        userName = publicReservation.getVisitorName();
                                                                                    } else if (officialReservation != null) {
                                                                                        userName = officialReservation.getVisitorName();
                                                                                    }
                                                                                    
                                                                                    String maskedName = "";
                                                                                    if (userName != null && userName.length() > 0) {
                                                                                        if (userName.length() == 2) {
                                                                                            maskedName = userName.charAt(0) + "*";
                                                                                        } else if (userName.length() > 2) {
                                                                                            StringBuilder sb = new StringBuilder();
                                                                                            sb.append(userName.charAt(0));
                                                                                            for (int i = 1; i < userName.length() - 1; i++) {
                                                                                                sb.append("*");
                                                                                            }
                                                                                            sb.append(userName.charAt(userName.length() - 1));
                                                                                            maskedName = sb.toString();
                                                                                        } else {
                                                                                            maskedName = userName;
                                                                                        }
                                                                                    } else {
                                                                                        maskedName = "访客";
                                                                                    }
                                                                                %>
                                                                                <%= maskedName %>
                                                                            </div>
                                                                        </div>                                                                        <div class="info-item">
                                                                            <div class="info-label">联系电话</div>
                                                                            <div class="info-value">
                                                                                <%
                                                                                    String phone = null;
                                                                                    try {
                                                                                        if (publicReservation != null) {
                                                                                            phone = DataEncryptionUtil.decrypt(publicReservation.getVisitorPhoneEncrypted());
                                                                                        } else if (officialReservation != null) {
                                                                                            phone = DataEncryptionUtil.decrypt(officialReservation.getVisitorPhoneEncrypted());
                                                                                        }
                                                                                    } catch (Exception e) {
                                                                                        // 解密失败
                                                                                    }
                                                                                    
                                                                                    String maskedPhone = "";
                                                                                    if (phone != null && phone.length() > 7) {
                                                                                        maskedPhone = phone.substring(0, 3) + "****" + phone.substring(7);
                                                                                    } else {
                                                                                        maskedPhone = "未提供";
                                                                                    }
                                                                                %>
                                                                                <%= maskedPhone %>
                                                                            </div>
                                                                        </div>                                                                        <div class="info-item">
                                                                            <div class="info-label">访问时间</div>
                                                                            <div class="info-value">
                                                                                <%
                                                                                    String visitTimeDisplay = "未指定";
                                                                                    if (publicReservation != null && publicReservation.getVisitDate() != null) {
                                                                                        visitTimeDisplay = publicReservation.getVisitDate().toString();
                                                                                    } else if (officialReservation != null && officialReservation.getVisitDate() != null) {
                                                                                        visitTimeDisplay = officialReservation.getVisitDate().toString();
                                                                                    }
                                                                                %>
                                                                                <%= visitTimeDisplay %>
                                                                            </div>
                                                                        </div>                                                                        <div class="info-item">
                                                                            <div class="info-label">目的地</div>
                                                                            <div class="info-value">
                                                                                <%
                                                                                    String destination = "未指定";
                                                                                    if (publicReservation != null && publicReservation.getVisitReason() != null) {
                                                                                        destination = publicReservation.getVisitReason();
                                                                                    } else if (officialReservation != null && officialReservation.getVisitReason() != null) {
                                                                                        destination = officialReservation.getVisitReason();
                                                                                    }
                                                                                %>
                                                                                <%= destination %>
                                                                            </div>
                                                                        </div>
                                                                    </div>                                                    <!-- 使用说明 -->
                                                                    <div class="notice">
                                                                        <% if ("early".equals(status)) { %>
                                                                            <h4>📋 预约信息：</h4>
                                                                            <ul>
                                                                                <li>您的预约已通过审核，但还未到生效时间</li>
                                                                                <li>通行码将在访问日期前30分钟自动生效</li>
                                                                                <li>请在预约时间前重新查看此页面</li>
                                                                                <li>如需修改预约信息，请联系相关部门</li>
                                                                            </ul>
                                                                        <% } else if ("valid".equals(status) || "approved".equals(status)) { %>
                                                                            <h4>🚀 使用说明：</h4>
                                                                            <ul>
                                                                                <li>请在访问时间段内使用此通行码</li>
                                                                                <li>进入校园时请出示此二维码</li>
                                                                                <li>请按照预约的目的地和访问时间安排行程</li>
                                                                                <li>如有变动请及时联系相关部门</li>
                                                                                <li>一人一码，请勿转让他人使用</li>
                                                                            </ul>
                                                                        <% } else if ("expired".equals(status)) { %>
                                                                            <h4>⚠️ 注意事项：</h4>
                                                                            <ul>
                                                                                <li>此通行码已过期，无法用于进入校园</li>
                                                                                <li>如需重新访问，请提交新的预约申请</li>
                                                                                <li>如有疑问，请联系相关部门</li>
                                                                            </ul>
                                                                        <% } else { %>
                                                                            <h4>📋 提示信息：</h4>
                                                                            <ul>
                                                                                <li>请等待预约审核通过</li>
                                                                                <li>通行码将在审核通过后生成</li>
                                                                                <li>如有疑问，请联系相关部门</li>
                                                                            </ul>
                                                                        <% } %>
                                                                    </div>
                                                                    <% } %>
                        </div>
                    </div>

                    <script>
                        // 下载二维码
                        function downloadQR() {
                            const qrImg = document.querySelector('.qr-code img');
                            if (qrImg) {
                                const link = document.createElement('a');
                                link.download = '校园通行码_<%= reservationId != null ? reservationId : "unknown" %>.png';
                                link.href = qrImg.src;
                                link.click();
                            }
                        }

                        // 打印通行码
                        function printPasscode() {
                            const printContent = document.querySelector('.passcode-card').innerHTML;
                            const printWindow = window.open('', '_blank');

                            const htmlContent =
                                '<html>' +
                                '<head>' +
                                '<title>校园通行码</title>' +
                                '<style>' +
                                'body { font-family: Arial, sans-serif; margin: 20px; }' +
                                '.passcode-card { max-width: 600px; margin: 0 auto; }' +
                                '.actions { display: none; }' +
                                '@media print { .actions { display: none !important; } }' +
                                '</style>' +
                                '</head>' +
                                '<body>' +
                                '<div class="passcode-card">' + printContent + '</div>' +
                                '<scr' + 'ipt>' +
                                'window.onload = function() { window.print(); window.close(); };' +
                                '</scr' + 'ipt>' +
                                '</body>' +
                                '</html>'; printWindow.document.write(htmlContent);
                            printWindow.document.close();
                        }

                        // 自动刷新状态（如果是待审核状态）
                        var reservationStatus = "<%= status %>";
                        if (reservationStatus === "pending") {
                            setTimeout(function () {
                                location.reload();
                            }, 30000); // 30秒后刷新
                        }
                    </script>
            </body>

            </html>