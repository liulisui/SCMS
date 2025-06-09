package org.example.scms.servlet;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.scms.model.Campus;
import org.example.scms.model.Department;
import org.example.scms.model.OfficialReservation;
import org.example.scms.model.PublicReservation;
import org.example.scms.service.CampusService;
import org.example.scms.service.DepartmentService;
import org.example.scms.service.OfficialReservationService;
import org.example.scms.service.PublicReservationService;
import org.example.scms.util.AuditLogUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 预约申请Servlet
 */
@WebServlet("/user/reservation")
public class ReservationServlet extends HttpServlet {

    private final PublicReservationService publicReservationService = new PublicReservationService();
    private final OfficialReservationService officialReservationService = new OfficialReservationService();
    private final CampusService campusService = new CampusService();
    private final DepartmentService departmentService = new DepartmentService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 获取校区列表
        List<Campus> campuses = campusService.getActiveCampuses();
        request.setAttribute("campuses", campuses);

        // 获取部门列表（用于公务预约）
        List<Department> departments = departmentService.getAllDepartments();
        request.setAttribute("departments", departments);

        request.getRequestDispatcher("/user/reservation.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String reservationType = request.getParameter("reservationType");
        String visitorName = request.getParameter("visitorName");
        String visitorIdCard = request.getParameter("visitorIdCard");
        String visitorPhone = request.getParameter("visitorPhone");
        String organization = request.getParameter("organization");
        String campusIdStr = request.getParameter("campusId");
        String visitDateStr = request.getParameter("visitDate");
        String visitTimeStartStr = request.getParameter("visitTimeStart");
        String visitTimeEndStr = request.getParameter("visitTimeEnd");
        String visitReason = request.getParameter("visitReason");
        String accompanyingPersonsStr = request.getParameter("accompanyingPersons");
        String vehicleNumber = request.getParameter("vehicleNumber");        // 公务预约特有字段
        String hostDepartmentIdStr = request.getParameter("hostDepartmentId");
        String contactPerson = request.getParameter("contactPerson");
        String contactPhone = request.getParameter("contactPhone");

        // 验证基本参数
        if (reservationType == null || reservationType.trim().isEmpty() ||
                visitorName == null || visitorName.trim().isEmpty() ||
                visitorIdCard == null || visitorIdCard.trim().isEmpty() ||
                visitorPhone == null || visitorPhone.trim().isEmpty() ||
                organization == null || organization.trim().isEmpty() ||
                campusIdStr == null || campusIdStr.trim().isEmpty() ||
                visitDateStr == null || visitDateStr.trim().isEmpty() ||
                visitTimeStartStr == null || visitTimeStartStr.trim().isEmpty() ||
                visitTimeEndStr == null || visitTimeEndStr.trim().isEmpty() ||
                visitReason == null || visitReason.trim().isEmpty()) {

            request.setAttribute("error", "请填写完整的基本信息");
            doGet(request, response);
            return;
        }        // 如果是公务预约，验证公务相关字段
        if ("official".equals(reservationType)) {
            if (hostDepartmentIdStr == null || hostDepartmentIdStr.trim().isEmpty() ||
                    contactPerson == null || contactPerson.trim().isEmpty() ||
                    contactPhone == null || contactPhone.trim().isEmpty()) {

                request.setAttribute("error", "公务预约请填写完整的公务信息");
                doGet(request, response);
                return;
            }
        }

        try {
            // 解析参数
            Long campusId = Long.parseLong(campusIdStr);
            LocalDate visitDate = LocalDate.parse(visitDateStr);
            LocalTime visitTimeStart = LocalTime.parse(visitTimeStartStr);
            LocalTime visitTimeEnd = LocalTime.parse(visitTimeEndStr);
            Integer accompanyingPersons = 0;
            if (accompanyingPersonsStr != null && !accompanyingPersonsStr.trim().isEmpty()) {
                accompanyingPersons = Integer.parseInt(accompanyingPersonsStr);
            }

            // 获取客户端IP地址和User-Agent
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");            // 根据预约类型创建预约
            if ("public".equals(reservationType)) {
                // 社会公众预约
                System.out.println("============ 社会公众预约处理开始 ============");
                System.out.println("接收到的参数:");
                System.out.println("- 预约类型: " + reservationType);
                System.out.println("- 访客姓名: " + visitorName);
                System.out.println("- 手机号: " + visitorPhone);
                System.out.println("- 组织机构: " + organization);
                System.out.println("- 校区ID: " + campusId);
                System.out.println("- 访问日期: " + visitDate);
                System.out.println("- 访问时间: " + visitTimeStart + " - " + visitTimeEnd);
                System.out.println("- 访问事由: " + visitReason);
                System.out.println("- 随行人数: " + accompanyingPersons);
                System.out.println("- 车牌号: " + vehicleNumber);
                System.out.println("- 客户端IP: " + ipAddress);
                
                PublicReservation reservation = publicReservationService.submitReservation(
                        visitorName, visitorIdCard, visitorPhone, organization, campusId,
                        visitDate, visitTimeStart, visitTimeEnd, visitReason,
                        accompanyingPersons, vehicleNumber, ipAddress, userAgent);                if (reservation != null) {
                    System.out.println("社会公众预约创建成功，ID: " + reservation.getId() + 
                        ", 编号: " + reservation.getReservationNo() + 
                        ", 状态: " + reservation.getStatus());
                    
                    // 记录创建公众预约的审计日志
                    Map<String, Object> reservationData = new HashMap<>();
                    reservationData.put("visitorName", visitorName);
                    reservationData.put("visitorIdCard", visitorIdCard);
                    reservationData.put("visitorPhone", visitorPhone);
                    reservationData.put("organization", organization);
                    reservationData.put("campusId", campusId);
                    reservationData.put("visitDate", visitDate.toString());
                    reservationData.put("visitTimeRange", visitTimeStart + " - " + visitTimeEnd);
                    reservationData.put("visitReason", visitReason);
                    reservationData.put("accompanyingPersons", accompanyingPersons);
                    reservationData.put("vehicleNumber", vehicleNumber);
                    reservationData.put("reservationNo", reservation.getReservationNo());
                    AuditLogUtil.logCreate(request, "public_reservations", reservation.getId(), reservationData);
                    
                    System.out.println("跳转到预约结果页面...");
                    // 跳转到预约结果页面
                    response.sendRedirect(request.getContextPath() +
                            "/user/reservation-result?type=public&id=" + reservation.getId());
                } else {
                    System.out.println("社会公众预约创建失败，返回null");
                    request.setAttribute("error", "预约申请失败，请检查输入信息或稍后重试");
                    doGet(request, response);
                }
                System.out.println("============ 社会公众预约处理结束 ============");
            } else if ("official".equals(reservationType)) {
                // 公务预约
                Long hostDepartmentId = Long.parseLong(hostDepartmentIdStr);                OfficialReservation reservation = officialReservationService.submitReservation(
                        visitorName, visitorIdCard, visitorPhone, organization, hostDepartmentId,
                        contactPerson, contactPhone, campusId, visitDate, visitTimeStart, visitTimeEnd,
                        visitReason, accompanyingPersons, vehicleNumber, ipAddress, userAgent);if (reservation != null) {
                    // 记录创建公务预约的审计日志
                    Map<String, Object> reservationData = new HashMap<>();
                    reservationData.put("visitorName", visitorName);
                    reservationData.put("visitorIdCard", visitorIdCard);
                    reservationData.put("visitorPhone", visitorPhone);
                    reservationData.put("organization", organization);
                    reservationData.put("hostDepartmentId", hostDepartmentId);
                    reservationData.put("contactPerson", contactPerson);
                    reservationData.put("contactPhone", contactPhone);
                    reservationData.put("campusId", campusId);
                    reservationData.put("visitDate", visitDate.toString());                    reservationData.put("visitTimeRange", visitTimeStart + " - " + visitTimeEnd);
                    reservationData.put("visitReason", visitReason);
                    reservationData.put("accompanyingPersons", accompanyingPersons);
                    reservationData.put("vehicleNumber", vehicleNumber);
                    reservationData.put("reservationNo", reservation.getReservationNo());
                    AuditLogUtil.logCreate(request, "official_reservations", reservation.getId(), reservationData);
                    
                    // 跳转到预约结果页面
                    response.sendRedirect(request.getContextPath() +
                            "/user/reservation-result?type=official&id=" + reservation.getId());
                } else {
                    request.setAttribute("error", "预约申请失败，请检查输入信息或稍后重试");
                    doGet(request, response);
                }
            } else {
                request.setAttribute("error", "无效的预约类型");
                doGet(request, response);
            }

        } catch (NumberFormatException | DateTimeParseException e) {
            request.setAttribute("error", "参数格式错误，请检查输入");
            doGet(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "系统错误，请稍后重试");
            doGet(request, response);
        }
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
