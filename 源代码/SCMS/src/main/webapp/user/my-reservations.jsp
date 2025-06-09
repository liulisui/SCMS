<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.example.scms.model.Reservation" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<%
    String errorMessage = (String) request.getAttribute("errorMessage");
    String idCard = (String) request.getAttribute("idCard");
    List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
    String filterType = request.getParameter("type");
    if (filterType == null) filterType = "all";
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
%>

<!DOCTYPE html>
<html lang="zh-CN">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">    <title>我的预约 - 校园通行码预约管理系统</title>
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
        }        .header {
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
        }        .card {
            background: white;
            border-radius: 12px;
            padding: 1.5rem;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
            border: 1px solid #e2e8f0;
            margin-bottom: 2rem;
        }

        .card h2 {
            color: #1a202c;
            margin-bottom: 1rem;
            font-size: 1.25rem;
        }

        .reservations-grid {
            display: grid;
            gap: 1.5rem;
            grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
        }

        .reservation-card {
            background: white;
            border-radius: 12px;
            padding: 1.5rem;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
            border: 1px solid #e2e8f0;
            position: relative;
            overflow: hidden;
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
                                        font-size: 1.1rem;
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
                                        gap: 0.5rem;
                                        margin-bottom: 1rem;
                                    }

                                    .info-item {
                                        display: flex;
                                        justify-content: space-between;
                                        padding: 0.5rem 0;
                                        border-bottom: 1px solid #f1f5f9;
                                    }

                                    .info-item:last-child {
                                        border-bottom: none;
                                    }

                                    .info-label {
                                        color: #64748b;
                                        font-weight: 500;
                                    }

                                    .info-value {
                                        color: #1e293b;
                                        font-weight: 600;
                                    }

                                    .access-code-section {
                                        margin-top: 1rem;
                                        padding: 1rem;
                                        border-radius: 8px;
                                        border: 2px solid;
                                    }

                                    .access-code-valid {
                                        background: #d1fae5;
                                        border-color: #10b981;
                                    }

                                    .access-code-early {
                                        background: #fef3c7;
                                        border-color: #f59e0b;
                                    }

                                    .access-code-expired {
                                        background: #fee2e2;
                                        border-color: #ef4444;
                                    }

                                    .access-code-invalid {
                                        background: #f1f5f9;
                                        border-color: #9ca3af;
                                    }

                                    .access-code-status {
                                        display: flex;
                                        align-items: center;
                                        gap: 0.5rem;
                                        font-weight: 600;
                                        margin-bottom: 0.5rem;
                                    }

                                    .view-code-btn {
                                        width: 100%;
                                        padding: 0.75rem;
                                        background: #10b981;
                                        color: white;
                                        border: none;
                                        border-radius: 8px;
                                        text-decoration: none;
                                        display: inline-block;
                                        text-align: center;
                                        font-weight: 500;
                                        margin-top: 0.5rem;
                                        transition: all 0.3s ease;
                                    }

                                    .view-code-btn:hover {
                                        background: #059669;
                                        transform: translateY(-1px);
                                    }

                                    .view-code-btn:disabled {
                                        background: #9ca3af;
                                        cursor: not-allowed;
                                        transform: none;
                                    }

                                    .empty-state {
                                        text-align: center;
                                        padding: 3rem;
                                        color: #64748b;
                                    }

                                    .empty-state .emoji {
                                        font-size: 4rem;
                                        margin-bottom: 1rem;
                                    }

                                    .btn {
                                        padding: 0.5rem 1rem;
                                        border-radius: 6px;
                                        text-decoration: none;
                                        font-weight: 500;
                                        display: inline-block;
                                        transition: all 0.3s ease;
                                        border: 1px solid;
                                    }

                                    .btn-primary {
                                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                                        color: white;
                                        border-color: #667eea;
                                    }

                                    .btn-secondary {
                                        background: #f1f5f9;
                                        color: #64748b;
                                        border-color: #e2e8f0;
                                    }

                                    .btn:hover {
                                        transform: translateY(-1px);
                                        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
                                    }

                                    @media (max-width: 768px) {
                                        .reservations-grid {
                                            grid-template-columns: 1fr;
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
                                    <div class="header-content">                                        <a href="${pageContext.request.contextPath}/index.jsp" class="back-btn">←
                                            返回</a>
                                        <h1>我的预约</h1>
                                    </div>
                                </header>

                                <div class="container">
                                    <% if (errorMessage !=null) { %>
                                        <div class="card">
                                            <div class="empty-state">
                                                <div class="emoji">❌</div>
                                                <h3>查询失败</h3>
                                                <p>
                                                    <%= errorMessage %>
                                                </p>
                                                <a href="${pageContext.request.contextPath}/user/history"
                                                    style="display: inline-block; margin-top: 1rem; padding: 0.75rem 1.5rem; background: #667eea; color: white; text-decoration: none; border-radius: 8px;">
                                                    重新查询
                                                </a>
                                            </div>
                                        </div>
                                        <% } else if (idCard !=null) { %>
                                            <!-- 显示身份证信息和过滤选项 -->
                                            <div class="card">
                                                <h2>📋 身份证：<%= idCard.substring(0, 6) + "****" + idCard.substring(14) %>
                                                        的预约记录</h2>

                                                <!-- 过滤选项 -->
                                                <div style="margin-bottom: 1rem;">
                                                    <a href="?idCard=<%= idCard %>&type=all" class="<%= "
                                                        all".equals(filterType) ? "btn btn-primary"
                                                        : "btn btn-secondary" %>"
                                                        style="margin-right: 0.5rem;">全部</a>
                                                    <a href="?idCard=<%= idCard %>&type=public" class="<%= "
                                                        public".equals(filterType) ? "btn btn-primary"
                                                        : "btn btn-secondary" %>"
                                                        style="margin-right: 0.5rem;">社会公众</a>
                                                    <a href="?idCard=<%= idCard %>&type=official" class="<%= "
                                                        official".equals(filterType) ? "btn btn-primary"
                                                        : "btn btn-secondary" %>">公务预约</a>
                                                </div>

                                                <% if (reservations==null || reservations.isEmpty()) { %>
                                                    <div class="empty-state">
                                                        <div class="emoji">📝</div>
                                                        <h3>暂无预约记录</h3>
                                                        <p>该身份证号码还没有任何预约记录</p>
                                                        <a href="${pageContext.request.contextPath}/user/reservation"
                                                            style="display: inline-block; margin-top: 1rem; padding: 0.75rem 1.5rem; background: #667eea; color: white; text-decoration: none; border-radius: 8px;">
                                                            立即预约
                                                        </a>
                                                    </div>
                                                    <% } else { %>
                                                        <div class="reservations-grid">
                                                            <% for (Reservation reservation : reservations) { // 解析访问码状态
                                                                String accessCodeStatus="invalid" ; String
                                                                originalReviewReason="" ; if
                                                                (reservation.getReviewReason() !=null &&
                                                                reservation.getReviewReason().contains("|")) { String[]
                                                                parts=reservation.getReviewReason().split("\\|", 2);
                                                                accessCodeStatus=parts[0];
                                                                originalReviewReason=parts[1]; } else {
                                                                originalReviewReason=reservation.getReviewReason(); } %>
                                                                <div
                                                                    class="reservation-card <%= reservation.getStatus() %>">
                                                                    <div class="reservation-header">
                                                                        <span class="reservation-id">预约 #<%=
                                                                                reservation.getId() %></span>
                                                                        <span
                                                                            class="status-badge status-<%= reservation.getStatus() %>">
                                                                            <% if
                                                                                ("pending".equals(reservation.getStatus()))
                                                                                { %>
                                                                                待审核
                                                                                <% } else if
                                                                                    ("approved".equals(reservation.getStatus()))
                                                                                    { %>
                                                                                    已通过
                                                                                    <% } else if
                                                                                        ("rejected".equals(reservation.getStatus()))
                                                                                        { %>
                                                                                        已拒绝
                                                                                        <% } %>
                                                                        </span>
                                                                    </div>

                                                                    <div class="reservation-info">
                                                                        <div class="info-item">
                                                                            <span class="info-label">预约类型</span>
                                                                            <span class="info-value">
                                                                                <%= "public"
                                                                                    .equals(reservation.getReservationType())
                                                                                    ? "社会公众" : "公务预约" %>
                                                                            </span>
                                                                        </div>
                                                                        <div class="info-item">
                                                                            <span class="info-label">校区</span>
                                                                            <span class="info-value">
                                                                                <%= reservation.getCampus() %>
                                                                            </span>
                                                                        </div>
                                                                        <% if (reservation.getPurpose() !=null &&
                                                                            !reservation.getPurpose().trim().isEmpty())
                                                                            { %>
                                                                            <div class="info-item">
                                                                                <span class="info-label">来访目的</span>
                                                                                <span class="info-value">
                                                                                    <%= reservation.getPurpose() %>
                                                                                </span>
                                                                            </div>
                                                                            <% } %>
                                                                                <div class="info-item">
                                                                                    <span class="info-label">访问时间</span>
                                                                                    <span class="info-value">
                                                                                        <%= reservation.getVisitTime().format(formatter)
                                                                                            %>
                                                                                    </span>
                                                                                </div>
                                                                                <div class="info-item">
                                                                                    <span class="info-label">停留时长</span>
                                                                                    <span class="info-value">
                                                                                        <%= reservation.getDuration() %>
                                                                                    </span>
                                                                                </div>
                                                                                <% if
                                                                                    ("official".equals(reservation.getReservationType())
                                                                                    && reservation.getDepartment()
                                                                                    !=null) { %>
                                                                                    <div class="info-item">
                                                                                        <span
                                                                                            class="info-label">对接部门</span>
                                                                                        <span class="info-value">
                                                                                            <%= reservation.getDepartment()
                                                                                                %>
                                                                                        </span>
                                                                                    </div>
                                                                                    <% } %>
                                                                                        <% if (originalReviewReason
                                                                                            !=null &&
                                                                                            !originalReviewReason.trim().isEmpty())
                                                                                            { %>
                                                                                            <div class="info-item">
                                                                                                <span
                                                                                                    class="info-label">审核意见</span>
                                                                                                <span
                                                                                                    class="info-value">
                                                                                                    <%= originalReviewReason
                                                                                                        %>
                                                                                                </span>
                                                                                            </div>
                                                                                            <% } %>
                                                                    </div>

                                                                    <!-- 通行码状态显示 -->
                                                                    <% if ("approved".equals(reservation.getStatus())) {
                                                                        %>
                                                                        <div
                                                                            class="access-code-section access-code-<%= accessCodeStatus %>">
                                                                            <div class="access-code-status">
                                                                                <% if ("valid".equals(accessCodeStatus))
                                                                                    { %>
                                                                                    ✅ 通行码有效
                                                                                    <% } else if
                                                                                        ("early".equals(accessCodeStatus))
                                                                                        { %>
                                                                                        ⏰ 未到使用时间
                                                                                        <% } else if
                                                                                            ("expired".equals(accessCodeStatus))
                                                                                            { %>
                                                                                            ❌ 通行码已过期
                                                                                            <% } else { %>
                                                                                                ⚠️ 通行码无效
                                                                                                <% } %>
                                                                            </div>

                                                                            <% if ("valid".equals(accessCodeStatus)) {
                                                                                %>
                                                                                <p
                                                                                    style="font-size: 0.9rem; color: #065f46; margin-bottom: 0.5rem;">
                                                                                    当前时间在有效期内，可以使用通行码进入校园
                                                                                </p>
                                                                                <% } else if
                                                                                    ("early".equals(accessCodeStatus)) {
                                                                                    %>
                                                                                    <p
                                                                                        style="font-size: 0.9rem; color: #92400e; margin-bottom: 0.5rem;">
                                                                                        通行码将在预约时间前30分钟开始生效
                                                                                    </p>
                                                                                    <% } else if
                                                                                        ("expired".equals(accessCodeStatus))
                                                                                        { %>
                                                                                        <p
                                                                                            style="font-size: 0.9rem; color: #991b1b; margin-bottom: 0.5rem;">
                                                                                            通行码已超过有效期，无法继续使用
                                                                                        </p>                                                                                        <% } %>

                                                                                            <a href="${pageContext.request.contextPath}/user/passcode?type=<%= reservation.getReservationType() %>&id=<%= reservation.getId() %>"
                                                                                                class="view-code-btn">
                                                                                                查看通行码
                                                                                            </a>
                                                                        </div>
                                                                        <% } %>
                                                                </div>
                                                                <% } %>
                                                        </div>
                                                        <% } %>
                                            </div>
                                            <% } else { %>
                                                <!-- 显示查询界面 -->
                                                <div class="card">
                                                    <h2>🔍 查询我的预约记录</h2>
                                                    <p style="color: #64748b; margin-bottom: 1.5rem;">请输入您的身份证号码来查询预约记录
                                                    </p>

                                                    <form method="GET" style="max-width: 400px;">
                                                        <div style="margin-bottom: 1rem;">
                                                            <label for="idCard"
                                                                style="display: block; margin-bottom: 0.5rem; font-weight: 500;">身份证号码</label>
                                                            <input type="text" id="idCard" name="idCard"
                                                                placeholder="请输入18位身份证号码" pattern="[0-9X]{18}"
                                                                maxlength="18" required
                                                                style="width: 100%; padding: 0.75rem; border: 1px solid #d1d5db; border-radius: 8px; font-size: 1rem;">
                                                        </div>
                                                        <div style="margin-bottom: 1rem;">
                                                            <label for="type"
                                                                style="display: block; margin-bottom: 0.5rem; font-weight: 500;">预约类型</label>
                                                            <select id="type" name="type"
                                                                style="width: 100%; padding: 0.75rem; border: 1px solid #d1d5db; border-radius: 8px; font-size: 1rem;">
                                                                <option value="all">全部类型</option>
                                                                <option value="public">社会公众</option>
                                                                <option value="official">公务预约</option>
                                                            </select>
                                                        </div>
                                                        <button type="submit"
                                                            style="width: 100%; padding: 0.75rem; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border: none; border-radius: 8px; font-size: 1rem; font-weight: 500; cursor: pointer;">
                                                            查询预约记录
                                                        </button>
                                                    </form>
                                                </div>
                                                <% } %>                                </div>
    <script>
        // 身份证号码输入验证
        document.addEventListener('DOMContentLoaded', function () {
            const idCardInput = document.getElementById('idCard');
            if (idCardInput) {
                idCardInput.addEventListener('input', function (e) {
                    let value = e.target.value;
                    // 只允许数字和X
                    value = value.replace(/[^0-9X]/g, '');
                    e.target.value = value;
                });

                idCardInput.addEventListener('blur', function (e) {
                    const value = e.target.value;
                    if (value.length === 18) {
                        // 简单的身份证号码校验
                        if (!/^[0-9]{17}[0-9X]$/.test(value)) {
                            alert('请输入正确的身份证号码格式');
                            e.target.focus();
                        }
                    }
                });
            }
        });
    </script>
</body>
</html>