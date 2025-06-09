package org.example.scms.servlet;

import java.io.IOException;

import org.example.scms.model.Administrator;
import org.example.scms.service.OfficialReservationService;
import org.example.scms.service.PublicReservationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 预约审核Servlet
 */
@WebServlet("/admin/review")
public class ReviewServlet extends HttpServlet {

    private final PublicReservationService publicReservationService = new PublicReservationService();
    private final OfficialReservationService officialReservationService = new OfficialReservationService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");        HttpSession session = request.getSession();
        Administrator admin = (Administrator) session.getAttribute("admin");

        if (admin == null || !"active".equals(admin.getStatus())) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }

        String reservationIdStr = request.getParameter("reservationId");
        String reservationType = request.getParameter("reservationType"); // public or official
        String action = request.getParameter("action");
        String reviewReason = request.getParameter("reviewReason");

        if (reservationIdStr == null || action == null || reservationType == null) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        try {
            Long reservationId = Long.parseLong(reservationIdStr);
            String status = "approve".equals(action) ? "approved" : "rejected";

            if (reviewReason == null || reviewReason.trim().isEmpty()) {
                reviewReason = "approve".equals(action) ? "审核通过" : "审核未通过";
            }

            // 获取客户端IP地址和User-Agent
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            boolean success = false;
            if ("public".equals(reservationType)) {
                success = publicReservationService.approveReservation(reservationId, admin.getId(),
                        status, reviewReason, ipAddress, userAgent);
            } else if ("official".equals(reservationType)) {
                success = officialReservationService.approveReservation(reservationId, admin.getId(),
                        status, reviewReason, ipAddress, userAgent);
            }

            if (success) {
                request.getSession().setAttribute("message", "审核操作成功");
            } else {
                request.getSession().setAttribute("error", "审核操作失败");
            }

        } catch (NumberFormatException e) {
            request.getSession().setAttribute("error", "参数错误");
        }

        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
