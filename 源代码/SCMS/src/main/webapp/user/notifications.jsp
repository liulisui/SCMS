<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ page import="org.example.scms.model.Reservation" %>
        <%@ page import="java.util.List" %>
            <%@ page import="java.time.LocalDateTime" %>
                <%@ page import="java.time.format.DateTimeFormatter" %>

                    <% String errorMessage=(String) request.getAttribute("errorMessage"); String idCard=(String)
                        request.getAttribute("idCard"); List<Reservation> notifications = (List<Reservation>)
                            request.getAttribute("notifications");
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                            %>
                            <!DOCTYPE html>
                            <html lang="zh-CN">

                            <head>
                                <meta charset="UTF-8">
                                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                <title>消息通知 - 校园通行码预约管理系统</title>
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
                                        max-width: 1000px;
                                        margin: 2rem auto;
                                        padding: 0 2rem;
                                    }

                                    .card {
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

                                    .notification-item {
                                        background: white;
                                        border-radius: 12px;
                                        padding: 1.5rem;
                                        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
                                        border: 1px solid #e2e8f0;
                                        margin-bottom: 1rem;
                                        position: relative;
                                        overflow: hidden;
                                    }

                                    .notification-item::before {
                                        content: '';
                                        position: absolute;
                                        top: 0;
                                        left: 0;
                                        right: 0;
                                        height: 4px;
                                    }

                                    .notification-item.pending::before {
                                        background: #fbbf24;
                                    }

                                    .notification-item.approved::before {
                                        background: #10b981;
                                    }

                                    .notification-item.rejected::before {
                                        background: #ef4444;
                                    }

                                    .notification-header {
                                        display: flex;
                                        justify-content: space-between;
                                        align-items: center;
                                        margin-bottom: 1rem;
                                    }

                                    .notification-title {
                                        font-weight: 600;
                                        color: #1e293b;
                                        font-size: 1.1rem;
                                    }

                                    .notification-time {
                                        color: #64748b;
                                        font-size: 0.875rem;
                                    }

                                    .notification-content {
                                        color: #475569;
                                        line-height: 1.6;
                                        margin-bottom: 1rem;
                                    }

                                    .notification-actions {
                                        display: flex;
                                        gap: 0.5rem;
                                    }

                                    .btn {
                                        padding: 0.5rem 1rem;
                                        border-radius: 6px;
                                        text-decoration: none;
                                        font-weight: 500;
                                        display: inline-block;
                                        transition: all 0.3s ease;
                                        border: 1px solid;
                                        cursor: pointer;
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

                                    .empty-state {
                                        text-align: center;
                                        padding: 3rem;
                                        color: #64748b;
                                    }

                                    .empty-state .emoji {
                                        font-size: 4rem;
                                        margin-bottom: 1rem;
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

                                    @media (max-width: 768px) {
                                        .container {
                                            padding: 0 1rem;
                                        }

                                        .header-content {
                                            padding: 0 1rem;
                                        }

                                        .notification-header {
                                            flex-direction: column;
                                            align-items: flex-start;
                                            gap: 0.5rem;
                                        }

                                        .notification-actions {
                                            flex-direction: column;
                                        }
                                    }
                                </style>
                            </head>

                            <body>
                                <header class="header">
                                    <div class="header-content">
                                        <a href="${pageContext.request.contextPath}/user/dashboard" class="back-btn">←
                                            返回</a>
                                        <h1>消息通知</h1>
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
                                            <!-- 显示身份证信息和通知 -->
                                            <div class="card">
                                                <h2>📢 身份证：<%= idCard.substring(0, 6) + "****" + idCard.substring(14) %>
                                                        的相关通知</h2>

                                                <% if (notifications==null || notifications.isEmpty()) { %>
                                                    <div class="empty-state">
                                                        <div class="emoji">📭</div>
                                                        <h3>暂无相关通知</h3>
                                                        <p>该身份证号码暂无预约相关通知</p>
                                                        <a href="${pageContext.request.contextPath}/user/reservation"
                                                            style="display: inline-block; margin-top: 1rem; padding: 0.75rem 1.5rem; background: #667eea; color: white; text-decoration: none; border-radius: 8px;">
                                                            立即预约
                                                        </a>
                                                    </div>
                                                    <% } else { %>
                                                        <% for (Reservation reservation : notifications) { String
                                                            notificationType="" ; String notificationMessage="" ; if
                                                            ("pending".equals(reservation.getStatus())) {
                                                            notificationType="待审核" ;
                                                            notificationMessage="您的预约申请已提交，正在等待审核。预约编号：" +
                                                            reservation.getId(); } else if
                                                            ("approved".equals(reservation.getStatus())) {
                                                            notificationType="审核通过" ;
                                                            notificationMessage="恭喜！您的预约申请已通过审核，可以生成通行码了。预约编号：" +
                                                            reservation.getId(); } else if
                                                            ("rejected".equals(reservation.getStatus())) {
                                                            notificationType="审核未通过" ;
                                                            notificationMessage="很抱歉，您的预约申请未通过审核。预约编号：" +
                                                            reservation.getId(); if (reservation.getReviewReason()
                                                            !=null && !reservation.getReviewReason().trim().isEmpty()) {
                                                            notificationMessage +=" 原因：" +
                                                            reservation.getReviewReason(); } } %>
                                                            <div
                                                                class="notification-item <%= reservation.getStatus() %>">
                                                                <div class="notification-header">
                                                                    <div class="notification-title">
                                                                        <span
                                                                            class="status-badge status-<%= reservation.getStatus() %>">
                                                                            <%= notificationType %>
                                                                        </span>
                                                                        <%= "public"
                                                                            .equals(reservation.getReservationType())
                                                                            ? "社会公众预约" : "公务预约" %>
                                                                    </div>
                                                                    <div class="notification-time">
                                                                        <%= reservation.getCreatedAt().format(formatter)
                                                                            %>
                                                                    </div>
                                                                </div>

                                                                <div class="notification-content">
                                                                    <p>
                                                                        <%= notificationMessage %>
                                                                    </p>
                                                                    <p><strong>校区：</strong>
                                                                        <%= reservation.getCampus() %>
                                                                    </p>
                                                                    <p><strong>访问时间：</strong>
                                                                        <%= reservation.getVisitTime().format(formatter)
                                                                            %>
                                                                    </p>
                                                                    <% if (reservation.getPurpose() !=null &&
                                                                        !reservation.getPurpose().trim().isEmpty()) { %>
                                                                        <p><strong>访问目的：</strong>
                                                                            <%= reservation.getPurpose() %>
                                                                        </p>
                                                                        <% } %>
                                                                </div>                                                                <div class="notification-actions">
                                                                    <% if ("approved".equals(reservation.getStatus())) {
                                                                        %>
                                                                        <a href="${pageContext.request.contextPath}/user/passcode?type=<%= reservation.getReservationType() %>&id=<%= reservation.getId() %>"
                                                                            class="btn btn-primary">查看通行码</a>
                                                                        <% } %>
                                                                            <a href="${pageContext.request.contextPath}/user/history?idCard=<%= idCard %>"
                                                                                class="btn btn-secondary">查看详情</a>
                                                                </div>
                                                            </div>
                                                            <% } %>
                                                                <% } %>
                                            </div>
                                            <% } else { %>
                                                <!-- 显示查询界面 -->
                                                <div class="card">
                                                    <h2>🔍 查询预约通知</h2>
                                                    <p style="color: #64748b; margin-bottom: 1.5rem;">
                                                        请输入您的身份证号码来查询预约相关通知</p>

                                                    <form method="GET" style="max-width: 400px;">
                                                        <div style="margin-bottom: 1rem;">
                                                            <label for="idCard"
                                                                style="display: block; margin-bottom: 0.5rem; font-weight: 500;">身份证号码</label>
                                                            <input type="text" id="idCard" name="idCard"
                                                                placeholder="请输入18位身份证号码" pattern="[0-9X]{18}"
                                                                maxlength="18" required
                                                                style="width: 100%; padding: 0.75rem; border: 1px solid #d1d5db; border-radius: 8px; font-size: 1rem;">
                                                        </div>
                                                        <button type="submit"
                                                            style="width: 100%; padding: 0.75rem; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border: none; border-radius: 8px; font-size: 1rem; font-weight: 500; cursor: pointer;">
                                                            查询通知
                                                        </button>
                                                    </form>

                                                    <div
                                                        style="margin-top: 2rem; padding: 1rem; background: #f8fafc; border-radius: 8px; border: 1px solid #e2e8f0;">
                                                        <h4 style="color: #4a5568; margin-bottom: 0.5rem;">📋 通知内容包括：
                                                        </h4>
                                                        <ul style="color: #718096; padding-left: 1.5rem;">
                                                            <li>预约申请提交确认</li>
                                                            <li>预约审核结果通知</li>
                                                            <li>通行码生成提醒</li>
                                                            <li>访问时间临近提醒</li>
                                                        </ul>
                                                    </div>
                                                </div>
                                                <% } %>
                                </div>

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