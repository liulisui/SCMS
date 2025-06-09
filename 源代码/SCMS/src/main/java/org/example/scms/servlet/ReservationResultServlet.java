package org.example.scms.servlet;

import java.io.IOException;
import java.time.LocalDateTime;

import org.example.scms.model.Department;
import org.example.scms.model.OfficialReservation;
import org.example.scms.model.PublicReservation;
import org.example.scms.service.DepartmentService;
import org.example.scms.service.OfficialReservationService;
import org.example.scms.service.PublicReservationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 预约结果展示Servlet
 */
@WebServlet("/user/reservation-result")
public class ReservationResultServlet extends HttpServlet {    private final PublicReservationService publicReservationService = new PublicReservationService();
    private final OfficialReservationService officialReservationService = new OfficialReservationService();
    private final DepartmentService departmentService = new DepartmentService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String type = request.getParameter("type"); // public or official
        String idStr = request.getParameter("id");
        
        if (type == null || idStr == null) {
            response.sendRedirect(request.getContextPath() + "/user/dashboard");
            return;
        }

        try {
            Long reservationId = Long.valueOf(idStr);
              switch (type) {
                case "public":
                    // 处理社会公众预约
                    System.out.println("============ 社会公众预约结果处理开始 ============");
                    System.out.println("查询预约ID: " + reservationId);
                    
                    PublicReservation publicReservation = publicReservationService.getReservationById(reservationId);
                    if (publicReservation == null) {
                        System.out.println("社会公众预约记录不存在，ID: " + reservationId);
                        response.sendRedirect(request.getContextPath() + "/user/dashboard");
                        return;
                    }
                    
                    System.out.println("查询到预约记录:");
                    System.out.println("- 预约编号: " + publicReservation.getReservationNo());
                    System.out.println("- 状态: " + publicReservation.getStatus());
                    System.out.println("- 通行码: " + publicReservation.getPassCode());
                    System.out.println("- 访客姓名: " + publicReservation.getVisitorName());
                    System.out.println("- 访问日期: " + publicReservation.getVisitDate());
                    System.out.println("- 访问时间: " + publicReservation.getVisitTimeStart() + " - " + publicReservation.getVisitTimeEnd());
                    
                    processPublicReservationResult(request, publicReservation);
                    System.out.println("社会公众预约结果处理完成");
                    break;
                    
                case "official":
                    // 处理公务预约
                    OfficialReservation officialReservation = officialReservationService.getReservationById(reservationId);
                    if (officialReservation == null) {
                        response.sendRedirect(request.getContextPath() + "/user/dashboard");
                        return;
                    }
                    processOfficialReservationResult(request, officialReservation);
                    break;
                    
                default:
                    response.sendRedirect(request.getContextPath() + "/user/dashboard");
                    return;
            }

            request.getRequestDispatcher("/user/reservation-result.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/user/dashboard");
        }
    }    /**
     * 处理社会公众预约结果
     */
    private void processPublicReservationResult(HttpServletRequest request, PublicReservation reservation) {
        System.out.println("开始处理社会公众预约结果展示...");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime visitDateTime = LocalDateTime.of(reservation.getVisitDate(), reservation.getVisitTimeStart());
        LocalDateTime endDateTime = LocalDateTime.of(reservation.getVisitDate(), reservation.getVisitTimeEnd());

        System.out.println("时间比较:");
        System.out.println("- 当前时间: " + now);
        System.out.println("- 访问开始时间: " + visitDateTime);
        System.out.println("- 访问结束时间: " + endDateTime);
        System.out.println("- 提前30分钟时间: " + visitDateTime.minusMinutes(30));

        String status;
        String statusText;
        String qrCode = null;
        String passCode = null;

        System.out.println("当前预约状态: " + reservation.getStatus());

        switch (reservation.getStatus()) {
            case "pending":
                status = "pending";
                statusText = "等待审核";
                System.out.println("状态判断: 等待审核");
                break;
            case "approved":
                // 社会公众预约一般直接通过
                if (now.isBefore(visitDateTime.minusMinutes(30))) {
                    status = "early";
                    statusText = "预约成功，未到使用时间";
                    System.out.println("状态判断: 预约成功，未到使用时间");
                } else if (now.isAfter(visitDateTime.minusMinutes(30)) && now.isBefore(endDateTime)) {
                    status = "valid";
                    statusText = "通行码有效";
                    passCode = reservation.getPassCode();
                    System.out.println("状态判断: 通行码有效，通行码: " + passCode);
                    if (passCode != null) {
                        qrCode = reservation.getQrCodeData();
                        System.out.println("二维码数据: " + qrCode);
                    }
                } else {
                    status = "expired";
                    statusText = "通行码已过期";
                    System.out.println("状态判断: 通行码已过期");
                }
                break;
            case "rejected":
                status = "rejected";
                statusText = "审核未通过";
                System.out.println("状态判断: 审核未通过");
                break;
            default:
                status = "cancelled";
                statusText = "预约已取消";
                System.out.println("状态判断: 预约已取消");
                break;
        }

        System.out.println("最终状态: " + status + " - " + statusText);

        request.setAttribute("reservation", reservation);
        request.setAttribute("reservationType", "public");
        request.setAttribute("status", status);
        request.setAttribute("statusText", statusText);
        request.setAttribute("qrCode", qrCode);
        request.setAttribute("passCode", passCode);
        request.setAttribute("currentTime", now);
        
        System.out.println("预约结果属性设置完成");
    }/**
     * 处理公务预约结果
     */
    private void processOfficialReservationResult(HttpServletRequest request, OfficialReservation reservation) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime visitDateTime = LocalDateTime.of(reservation.getVisitDate(), reservation.getVisitTimeStart());
        LocalDateTime endDateTime = LocalDateTime.of(reservation.getVisitDate(), reservation.getVisitTimeEnd());

        String status;
        String statusText;
        String qrCode = null;
        String passCode = null;

        // 获取部门信息
        Department department = null;
        String departmentName = null;
        if (reservation.getHostDepartmentId() != null) {
            department = departmentService.getDepartmentById(reservation.getHostDepartmentId());
            if (department != null) {
                departmentName = department.getName();
            }
        }

        switch (reservation.getStatus()) {
            case "pending":
                status = "pending";
                statusText = "等待审核";
                break;
            case "approved":
                if (now.isBefore(visitDateTime.minusMinutes(30))) {
                    status = "early";
                    statusText = "审核通过，未到使用时间";
                } else if (now.isAfter(visitDateTime.minusMinutes(30)) && now.isBefore(endDateTime)) {
                    status = "valid";
                    statusText = "通行码有效";
                    passCode = reservation.getPassCode();
                    if (passCode != null) {
                        qrCode = reservation.getQrCodeData();
                    }
                } else {
                    status = "expired";
                    statusText = "通行码已过期";
                }
                break;
            case "rejected":
                status = "rejected";
                statusText = "审核未通过";
                break;
            default:
                status = "cancelled";
                statusText = "预约已取消";
                break;
        }

        request.setAttribute("reservation", reservation);
        request.setAttribute("reservationType", "official");
        request.setAttribute("status", status);
        request.setAttribute("statusText", statusText);
        request.setAttribute("qrCode", qrCode);
        request.setAttribute("passCode", passCode);
        request.setAttribute("currentTime", now);
        request.setAttribute("department", department);
        request.setAttribute("departmentName", departmentName);
    }
}
