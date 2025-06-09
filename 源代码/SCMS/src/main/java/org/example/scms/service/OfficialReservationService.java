package org.example.scms.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.example.scms.dao.AuditLogDAO;
import org.example.scms.dao.CampusDAO;
import org.example.scms.dao.DepartmentDAO;
import org.example.scms.dao.OfficialReservationDAO;
import org.example.scms.dao.ReservationAuthorizationDAO;
import org.example.scms.model.AuditLog;
import org.example.scms.model.Campus;
import org.example.scms.model.Department;
import org.example.scms.model.OfficialReservation;
import org.example.scms.util.DataEncryptionUtil;
import org.example.scms.util.NumberGeneratorUtil;
import org.example.scms.util.SM3HashUtil;

/**
 * 公务预约服务类
 * 处理公务预约相关的业务逻辑
 */
public class OfficialReservationService {
    private final OfficialReservationDAO officialReservationDAO;
    private final DepartmentDAO departmentDAO;
    private final CampusDAO campusDAO;
    private final ReservationAuthorizationDAO authorizationDAO;
    private final AuditLogDAO auditLogDAO;

    public OfficialReservationService() {
        this.officialReservationDAO = new OfficialReservationDAO();
        this.departmentDAO = new DepartmentDAO();
        this.campusDAO = new CampusDAO();
        this.authorizationDAO = new ReservationAuthorizationDAO();
        this.auditLogDAO = new AuditLogDAO();
    }    /**
     * 提交公务预约申请
     */    public OfficialReservation submitReservation(String visitorName, String visitorIdCard, String visitorPhone,
            String visitorOrganization, Long hostDepartmentId, String hostName,
            String hostPhone, Long campusId, LocalDate visitDate,
            LocalTime visitTimeStart, LocalTime visitTimeEnd,
            String visitReason, Integer accompanyingPersons, String vehicleNumber,
            String ipAddress, String userAgent) {try {
            System.out.println("开始处理公务预约提交请求...");
            System.out.println("接待部门ID: " + hostDepartmentId + ", 校区ID: " + campusId);
            System.out.println("预约日期: " + visitDate + ", 时间: " + visitTimeStart + " - " + visitTimeEnd);
            
            // 验证接待部门是否存在且有公务预约权限
            Department department = departmentDAO.getDepartmentById(hostDepartmentId);
            System.out.println("查询到的部门: " + (department != null ? department.getName() + " (状态: " + department.getStatus() + ")" : "null"));
            
            if (department == null || !"active".equals(department.getStatus())) {
                String errorMsg = "接待部门不存在或未激活: " + hostDepartmentId + 
                    (department != null ? " (状态: " + department.getStatus() + ")" : " (部门不存在)");
                System.out.println("验证失败: " + errorMsg);
                logAuditEvent(null, "SUBMIT_OFFICIAL_RESERVATION_FAILED", "OfficialReservation", null,
                        errorMsg, ipAddress, userAgent);
                return null;
            }            // 检查部门是否有公务预约权限
            boolean isAuthorized = authorizationDAO.isDepartmentAuthorized(hostDepartmentId, "official");
            System.out.println("部门公务预约权限检查结果: " + isAuthorized);
            
            if (!isAuthorized) {
                String errorMsg = "接待部门无公务预约权限: " + hostDepartmentId;
                System.out.println("验证失败: " + errorMsg);
                logAuditEvent(null, "SUBMIT_OFFICIAL_RESERVATION_FAILED", "OfficialReservation", null,
                        errorMsg, ipAddress, userAgent);
                return null;
            }            // 验证校区是否存在且激活
            Campus campus = campusDAO.getCampusById(campusId);
            System.out.println("查询到的校区: " + (campus != null ? campus.getCampusName() + " (状态: " + campus.getStatus() + ")" : "null"));
            
            if (campus == null || !"active".equals(campus.getStatus())) {
                String errorMsg = "校区不存在或未激活: " + campusId + 
                    (campus != null ? " (状态: " + campus.getStatus() + ")" : " (校区不存在)");
                System.out.println("验证失败: " + errorMsg);
                logAuditEvent(null, "SUBMIT_OFFICIAL_RESERVATION_FAILED", "OfficialReservation", null,
                        errorMsg, ipAddress, userAgent);
                return null;
            }            // 验证预约时间合法性
            LocalDate today = LocalDate.now();
            LocalDate maxDate = today.plusDays(60);
            System.out.println("时间验证 - 今天: " + today + ", 预约日期: " + visitDate + ", 最大允许日期: " + maxDate);
            System.out.println("开始时间: " + visitTimeStart + ", 结束时间: " + visitTimeEnd);
            
            if (visitDate.isBefore(today) ||
                    visitTimeStart.isAfter(visitTimeEnd) ||
                    visitDate.isAfter(maxDate)) { // 公务预约允许60天内
                String errorMsg = "预约时间不合法 - 预约日期: " + visitDate + 
                    " (今天: " + today + ", 最大允许: " + maxDate + "), 时间段: " + visitTimeStart + " - " + visitTimeEnd;
                System.out.println("验证失败: " + errorMsg);
                logAuditEvent(null, "SUBMIT_OFFICIAL_RESERVATION_FAILED", "OfficialReservation", null,
                        errorMsg, ipAddress, userAgent);
                return null;
            }            // 验证身份证号格式
            boolean idCardValid = isValidIdCard(visitorIdCard);
            System.out.println("身份证号验证: " + (idCardValid ? "通过" : "失败") + " (长度: " + (visitorIdCard != null ? visitorIdCard.length() : "null") + ")");
            
            if (!idCardValid) {
                String errorMsg = "身份证号格式不正确: " + (visitorIdCard != null ? visitorIdCard.substring(0, Math.min(6, visitorIdCard.length())) + "..." : "null");
                System.out.println("验证失败: " + errorMsg);
                logAuditEvent(null, "SUBMIT_OFFICIAL_RESERVATION_FAILED", "OfficialReservation", null,
                        errorMsg, ipAddress, userAgent);
                return null;
            }            // 验证手机号格式
            boolean phoneValid = isValidPhone(visitorPhone);
            System.out.println("访客手机号验证: " + (phoneValid ? "通过" : "失败") + " (" + visitorPhone + ")");
            
            if (!phoneValid) {
                String errorMsg = "手机号格式不正确: " + visitorPhone;
                System.out.println("验证失败: " + errorMsg);
                logAuditEvent(null, "SUBMIT_OFFICIAL_RESERVATION_FAILED", "OfficialReservation", null,
                        errorMsg, ipAddress, userAgent);
                return null;
            }

            // 验证接待人手机号格式
            boolean hostPhoneValid = isValidPhone(hostPhone);
            System.out.println("接待人手机号验证: " + (hostPhoneValid ? "通过" : "失败") + " (" + hostPhone + ")");
            
            if (!hostPhoneValid) {
                String errorMsg = "接待人手机号格式不正确: " + hostPhone;
                System.out.println("验证失败: " + errorMsg);
                logAuditEvent(null, "SUBMIT_OFFICIAL_RESERVATION_FAILED", "OfficialReservation", null,
                        errorMsg, ipAddress, userAgent);
                return null;
            }            // 检查是否有时间冲突的预约（同一部门同一时间段）
            System.out.println("检查时间冲突 - 部门ID: " + hostDepartmentId + ", 日期: " + visitDate);
            List<OfficialReservation> existingReservations = officialReservationDAO
                    .getReservationsByHostDepartment(hostDepartmentId, "approved");
            System.out.println("该部门已批准的预约数量: " + existingReservations.size());
            
            boolean hasTimeConflict = existingReservations.stream()
                    .anyMatch(r -> {
                        boolean dateMatch = r.getVisitDate().equals(visitDate);
                        boolean timeOverlap = isTimeOverlap(visitTimeStart, visitTimeEnd,
                                r.getVisitTimeStart(), r.getVisitTimeEnd());
                        if (dateMatch && timeOverlap) {
                            System.out.println("发现时间冲突 - 现有预约: " + r.getReservationNo() + 
                                " 时间: " + r.getVisitTimeStart() + "-" + r.getVisitTimeEnd());
                        }
                        return dateMatch && timeOverlap;
                    });

            if (hasTimeConflict) {
                String errorMsg = "该时间段已有其他公务预约";
                System.out.println("验证失败: " + errorMsg);
                logAuditEvent(null, "SUBMIT_OFFICIAL_RESERVATION_FAILED", "OfficialReservation", null,
                        errorMsg, ipAddress, userAgent);
                return null;
            }            // 加密敏感信息
            System.out.println("开始加密敏感信息...");
            String encryptedIdCard = DataEncryptionUtil.encrypt(visitorIdCard);
            String idCardHash = SM3HashUtil.hash(visitorIdCard);
            String encryptedPhone = DataEncryptionUtil.encrypt(visitorPhone);
            String phoneHash = SM3HashUtil.hash(visitorPhone);

            // 生成预约编号
            String reservationNo = NumberGeneratorUtil.generateOfficialReservationNumber();
            System.out.println("生成预约编号: " + reservationNo);            // 创建预约记录
            OfficialReservation reservation = new OfficialReservation(
                    reservationNo, visitorName, encryptedIdCard, idCardHash,
                    encryptedPhone, phoneHash, visitorOrganization, hostDepartmentId,
                    hostName, hostPhone, campusId, visitDate, visitTimeStart, visitTimeEnd,
                    visitReason, accompanyingPersons, vehicleNumber);

            System.out.println("开始保存预约记录到数据库...");
            OfficialReservation savedReservation = officialReservationDAO.addReservation(reservation);
            
            if (savedReservation != null) {
                System.out.println("预约保存成功，ID: " + savedReservation.getId());
                logAuditEvent(null, "SUBMIT_OFFICIAL_RESERVATION", "OfficialReservation", savedReservation.getId(),
                        "提交公务预约申请: " + reservationNo, ipAddress, userAgent);
                return savedReservation;
            } else {
                System.out.println("预约保存失败，数据库返回null");
            }        } catch (Exception e) {
            String errorMsg = "提交公务预约失败: " + e.getMessage();
            System.out.println("异常发生: " + errorMsg);
            e.printStackTrace(); // 打印完整的堆栈跟踪
            logAuditEvent(null, "SUBMIT_OFFICIAL_RESERVATION_FAILED", "OfficialReservation", null,
                    errorMsg, ipAddress, userAgent);
        }
        System.out.println("公务预约提交流程结束，返回null");
        return null;
    }

    /**
     * 获取预约详情
     */
    public OfficialReservation getReservationById(Long id) {
        return officialReservationDAO.getReservationById(id);
    }

    /**
     * 根据预约编号获取预约
     */
    public OfficialReservation getReservationByReservationNo(String reservationNo) {
        return officialReservationDAO.getReservationByReservationNo(reservationNo);
    }

    /**
     * 根据通行证编码获取预约
     */
    public OfficialReservation getReservationByPassCode(String passCode) {
        return officialReservationDAO.getReservationByPassCode(passCode);
    }

    /**
     * 审批公务预约
     */
    public boolean approveReservation(Long reservationId, Long adminId, String status,
            String comment, String ipAddress, String userAgent) {
        try {
            OfficialReservation reservation = officialReservationDAO.getReservationById(reservationId);
            if (reservation == null) {
                logAuditEvent(adminId, "APPROVE_OFFICIAL_RESERVATION_FAILED", "OfficialReservation", reservationId,
                        "预约记录不存在", ipAddress, userAgent);
                return false;
            }

            if (!"pending".equals(reservation.getStatus())) {
                logAuditEvent(adminId, "APPROVE_OFFICIAL_RESERVATION_FAILED", "OfficialReservation", reservationId,
                        "预约状态不是待审核", ipAddress, userAgent);
                return false;
            }

            String passCode = null;
            String qrCodeData = null;

            if ("approved".equals(status)) {
                // 生成通行证编码和二维码数据
                passCode = NumberGeneratorUtil.generatePassCode();
                qrCodeData = generateQRCodeData(reservation, passCode);
            }

            boolean success = officialReservationDAO.approveReservation(reservationId, adminId, status,
                    comment, passCode, qrCodeData);
            if (success) {
                logAuditEvent(adminId, "APPROVE_OFFICIAL_RESERVATION", "OfficialReservation", reservationId,
                        "审批公务预约: " + status + ", 意见: " + comment, ipAddress, userAgent);
                return true;
            }

        } catch (Exception e) {
            logAuditEvent(adminId, "APPROVE_OFFICIAL_RESERVATION_FAILED", "OfficialReservation", reservationId,
                    "审批失败: " + e.getMessage(), ipAddress, userAgent);
        }
        return false;
    }

    /**
     * 取消公务预约
     */
    public boolean cancelReservation(Long reservationId, String ipAddress, String userAgent) {
        try {
            OfficialReservation reservation = officialReservationDAO.getReservationById(reservationId);
            if (reservation == null) {
                logAuditEvent(null, "CANCEL_OFFICIAL_RESERVATION_FAILED", "OfficialReservation", reservationId,
                        "预约记录不存在", ipAddress, userAgent);
                return false;
            }

            if (!"pending".equals(reservation.getStatus()) && !"approved".equals(reservation.getStatus())) {
                logAuditEvent(null, "CANCEL_OFFICIAL_RESERVATION_FAILED", "OfficialReservation", reservationId,
                        "只能取消待审核或已批准的预约", ipAddress, userAgent);
                return false;
            }

            // 检查是否已过预约时间
            LocalDateTime visitDateTime = LocalDateTime.of(reservation.getVisitDate(), reservation.getVisitTimeStart());
            if (visitDateTime.isBefore(LocalDateTime.now())) {
                logAuditEvent(null, "CANCEL_OFFICIAL_RESERVATION_FAILED", "OfficialReservation", reservationId,
                        "预约时间已过，无法取消", ipAddress, userAgent);
                return false;
            }

            boolean success = officialReservationDAO.cancelReservation(reservationId);
            if (success) {
                logAuditEvent(null, "CANCEL_OFFICIAL_RESERVATION", "OfficialReservation", reservationId,
                        "取消公务预约", ipAddress, userAgent);
                return true;
            }

        } catch (Exception e) {
            logAuditEvent(null, "CANCEL_OFFICIAL_RESERVATION_FAILED", "OfficialReservation", reservationId,
                    "取消公务预约失败: " + e.getMessage(), ipAddress, userAgent);
        }
        return false;
    }

    /**
     * 记录入校时间
     */
    public boolean recordCheckIn(Long reservationId, Long operatorId, String ipAddress, String userAgent) {
        try {
            OfficialReservation reservation = officialReservationDAO.getReservationById(reservationId);
            if (reservation == null || !"approved".equals(reservation.getStatus())) {
                logAuditEvent(operatorId, "RECORD_OFFICIAL_CHECKIN_FAILED", "OfficialReservation", reservationId,
                        "预约不存在或状态不正确", ipAddress, userAgent);
                return false;
            }

            LocalDateTime now = LocalDateTime.now();
            boolean success = officialReservationDAO.recordCheckIn(reservationId, now);
            if (success) {
                logAuditEvent(operatorId, "RECORD_OFFICIAL_CHECKIN", "OfficialReservation", reservationId,
                        "记录公务访问入校时间", ipAddress, userAgent);
                return true;
            }

        } catch (Exception e) {
            logAuditEvent(operatorId, "RECORD_OFFICIAL_CHECKIN_FAILED", "OfficialReservation", reservationId,
                    "记录入校时间失败: " + e.getMessage(), ipAddress, userAgent);
        }
        return false;
    }

    /**
     * 记录离校时间
     */
    public boolean recordCheckOut(Long reservationId, Long operatorId, String ipAddress, String userAgent) {
        try {
            OfficialReservation reservation = officialReservationDAO.getReservationById(reservationId);
            if (reservation == null || reservation.getCheckInTime() == null) {
                logAuditEvent(operatorId, "RECORD_OFFICIAL_CHECKOUT_FAILED", "OfficialReservation", reservationId,
                        "预约不存在或未入校", ipAddress, userAgent);
                return false;
            }

            LocalDateTime now = LocalDateTime.now();
            boolean success = officialReservationDAO.recordCheckOut(reservationId, now);
            if (success) {
                logAuditEvent(operatorId, "RECORD_OFFICIAL_CHECKOUT", "OfficialReservation", reservationId,
                        "记录公务访问离校时间", ipAddress, userAgent);
                return true;
            }

        } catch (Exception e) {
            logAuditEvent(operatorId, "RECORD_OFFICIAL_CHECKOUT_FAILED", "OfficialReservation", reservationId,
                    "记录离校时间失败: " + e.getMessage(), ipAddress, userAgent);
        }
        return false;
    }

    /**
     * 获取待审批的公务预约列表
     */
    public List<OfficialReservation> getPendingReservations() {
        return officialReservationDAO.getPendingReservations();
    }

    /**
     * 获取部门的待审批公务预约列表
     */
    public List<OfficialReservation> getPendingReservationsByDepartment(Long departmentId) {
        return officialReservationDAO.getPendingReservationsByDepartment(departmentId);
    }

    /**
     * 获取某个校区的公务预约列表
     */
    public List<OfficialReservation> getReservationsByCampus(Long campusId, String status) {
        return officialReservationDAO.getReservationsByCampus(campusId, status);
    }

    /**
     * 根据接待部门查询公务预约
     */
    public List<OfficialReservation> getReservationsByHostDepartment(Long departmentId, String status) {
        return officialReservationDAO.getReservationsByHostDepartment(departmentId, status);
    }

    /**
     * 根据接待部门和日期范围查询公务预约（支持部门管理员权限控制）
     */
    public List<OfficialReservation> getReservationsByHostDepartmentAndDateRange(
            Long departmentId, LocalDate startDate, LocalDate endDate, String status) {
        return officialReservationDAO.getReservationsByHostDepartmentAndDateRange(
            departmentId, startDate, endDate, status);
    }

    /**
     * 根据日期范围查询公务预约
     */
    public List<OfficialReservation> getReservationsByDateRange(LocalDate startDate, LocalDate endDate, String status) {
        return officialReservationDAO.getReservationsByDateRange(startDate, endDate, status);
    }

    /**
     * 根据身份证号查询公务预约历史（需要先哈希）
     */
    public List<OfficialReservation> getReservationsByIdCard(String idCard) {
        String idCardHash = SM3HashUtil.hash(idCard);
        return officialReservationDAO.getReservationsByIdCardHash(idCardHash);
    }

    /**
     * 统计各状态公务预约数量
     */
    public Map<String, Integer> countReservationsByStatus(LocalDate startDate, LocalDate endDate) {
        return officialReservationDAO.countReservationsByStatus(startDate, endDate);
    }

    /**
     * 统计每月公务预约数量
     */
    public Map<Integer, Integer> countReservationsByMonth(int year) {
        return officialReservationDAO.countReservationsByMonth(year);
    }

    /**
     * 统计每个校区的公务预约数量
     */
    public Map<Long, Integer> countReservationsByCampus(LocalDate startDate, LocalDate endDate) {
        return officialReservationDAO.countReservationsByCampus(startDate, endDate);
    }

    /**
     * 统计每个部门的公务预约数量
     */
    public Map<Long, Integer> countReservationsByDepartment(LocalDate startDate, LocalDate endDate) {
        return officialReservationDAO.countReservationsByDepartment(startDate, endDate);
    }

    /**
     * 获取有公务预约权限的部门列表
     */
    public List<Long> getAuthorizedDepartmentIds() {
        return authorizationDAO.getAuthorizedDepartmentIds("official");
    }

    /**
     * 验证身份证号格式
     */
    private boolean isValidIdCard(String idCard) {
        if (idCard == null || idCard.trim().isEmpty()) {
            return false;
        }
        // 18位身份证号码正则表达式
        String regex = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";
        return idCard.matches(regex);
    }

    /**
     * 验证手机号格式
     */
    private boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        // 11位手机号码正则表达式
        String regex = "^1[3-9]\\d{9}$";
        return phone.matches(regex);
    }

    /**
     * 检查时间段是否重叠
     */
    private boolean isTimeOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    /**
     * 生成公务预约二维码数据
     */
    private String generateQRCodeData(OfficialReservation reservation, String passCode) {
        return String.format("SCMS_OFFICIAL_%s_%s_%s_%s_%s",
                reservation.getReservationNo(),
                passCode,
                reservation.getVisitDate(),
                reservation.getCampusId(),
                reservation.getHostDepartmentId());
    }

    /**
     * 记录审计日志
     */
    private void logAuditEvent(Long adminId, String operation, String resourceType, Long resourceId,
            String details, String ipAddress, String userAgent) {
        try {
            AuditLog auditLog = new AuditLog(adminId, operation, resourceType, resourceId,
                    details, null, null, ipAddress, userAgent);
            auditLogDAO.addAuditLog(auditLog);
        } catch (Exception e) {
            // 记录审计日志失败，可以记录到文件日志或其他方式
            System.err.println("Failed to log audit event: " + e.getMessage());
        }
    }
}
