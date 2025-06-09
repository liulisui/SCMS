package org.example.scms.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.example.scms.dao.AuditLogDAO;
import org.example.scms.dao.CampusDAO;
import org.example.scms.dao.PublicReservationDAO;
import org.example.scms.model.AuditLog;
import org.example.scms.model.Campus;
import org.example.scms.model.PublicReservation;
import org.example.scms.util.DataEncryptionUtil;
import org.example.scms.util.NumberGeneratorUtil;
import org.example.scms.util.SM3HashUtil;

/**
 * 社会公众预约服务类
 * 处理社会公众预约相关的业务逻辑
 */
public class PublicReservationService {
    private final PublicReservationDAO publicReservationDAO;
    private final CampusDAO campusDAO;
    private final AuditLogDAO auditLogDAO;

    public PublicReservationService() {
        this.publicReservationDAO = new PublicReservationDAO();
        this.campusDAO = new CampusDAO();
        this.auditLogDAO = new AuditLogDAO();
    }    /**
     * 提交社会公众预约申请
     */
    public PublicReservation submitReservation(String visitorName, String visitorIdCard, String visitorPhone,
            String organization, Long campusId, LocalDate visitDate,
            LocalTime visitTimeStart, LocalTime visitTimeEnd, String visitReason,
            Integer accompanyingPersons, String vehicleNumber,
            String ipAddress, String userAgent) {
        try {
            System.out.println("开始处理社会公众预约提交请求...");
            System.out.println("校区ID: " + campusId);
            System.out.println("预约日期: " + visitDate + ", 时间: " + visitTimeStart + " - " + visitTimeEnd);
            System.out.println("访客信息: " + visitorName + ", 手机: " + visitorPhone + ", 组织: " + organization);
            
            // 验证校区是否存在且激活
            Campus campus = campusDAO.getCampusById(campusId);
            System.out.println("查询到的校区: " + (campus != null ? campus.getCampusName() + " (状态: " + campus.getStatus() + ")" : "null"));
            
            if (campus == null || !"active".equals(campus.getStatus())) {
                String errorMsg = "校区不存在或未激活: " + campusId + 
                    (campus != null ? " (状态: " + campus.getStatus() + ")" : " (校区不存在)");
                System.out.println("验证失败: " + errorMsg);
                logAuditEvent(null, "SUBMIT_RESERVATION_FAILED", "PublicReservation", null,
                        errorMsg, ipAddress, userAgent);
                return null;
            }

            // 验证预约时间合法性
            LocalDate today = LocalDate.now();
            LocalDate maxDate = today.plusDays(30); // 限制30天内预约
            System.out.println("时间验证 - 今天: " + today + ", 预约日期: " + visitDate + ", 最大允许日期: " + maxDate);
            System.out.println("开始时间: " + visitTimeStart + ", 结束时间: " + visitTimeEnd);
            
            if (visitDate.isBefore(today) ||
                    visitTimeStart.isAfter(visitTimeEnd) ||
                    visitDate.isAfter(maxDate)) {
                String errorMsg = "预约时间不合法 - 预约日期: " + visitDate + 
                    " (今天: " + today + ", 最大允许: " + maxDate + "), 时间段: " + visitTimeStart + " - " + visitTimeEnd;
                System.out.println("验证失败: " + errorMsg);
                logAuditEvent(null, "SUBMIT_RESERVATION_FAILED", "PublicReservation", null,
                        errorMsg, ipAddress, userAgent);
                return null;
            }

            // 验证身份证号格式
            boolean idCardValid = isValidIdCard(visitorIdCard);
            System.out.println("身份证号验证: " + (idCardValid ? "通过" : "失败") + " (长度: " + (visitorIdCard != null ? visitorIdCard.length() : "null") + ")");
            
            if (!idCardValid) {
                String errorMsg = "身份证号格式不正确: " + (visitorIdCard != null ? visitorIdCard.substring(0, Math.min(6, visitorIdCard.length())) + "..." : "null");
                System.out.println("验证失败: " + errorMsg);
                logAuditEvent(null, "SUBMIT_RESERVATION_FAILED", "PublicReservation", null,
                        errorMsg, ipAddress, userAgent);
                return null;
            }

            // 验证手机号格式
            boolean phoneValid = isValidPhone(visitorPhone);
            System.out.println("手机号验证: " + (phoneValid ? "通过" : "失败") + " (" + visitorPhone + ")");
            
            if (!phoneValid) {
                String errorMsg = "手机号格式不正确: " + visitorPhone;
                System.out.println("验证失败: " + errorMsg);
                logAuditEvent(null, "SUBMIT_RESERVATION_FAILED", "PublicReservation", null,
                        errorMsg, ipAddress, userAgent);
                return null;
            }

            // 检查是否有重复预约（同一人同一天）
            System.out.println("检查重复预约 - 日期: " + visitDate);
            String idCardHash = SM3HashUtil.hash(visitorIdCard);
            System.out.println("身份证哈希值已生成");
            
            List<PublicReservation> existingReservations = publicReservationDAO.getReservationsByIdCardHash(idCardHash);
            System.out.println("该身份证的现有预约数量: " + existingReservations.size());
            
            boolean hasConflict = existingReservations.stream()
                    .anyMatch(r -> {
                        boolean dateMatch = r.getVisitDate().equals(visitDate);
                        boolean statusValid = !"cancelled".equals(r.getStatus()) && !"rejected".equals(r.getStatus());
                        if (dateMatch && statusValid) {
                            System.out.println("发现重复预约 - 现有预约: " + r.getReservationNo() + 
                                " 状态: " + r.getStatus() + " 日期: " + r.getVisitDate());
                        }
                        return dateMatch && statusValid;
                    });

            if (hasConflict) {
                String errorMsg = "同一天已有预约记录";
                System.out.println("验证失败: " + errorMsg);
                logAuditEvent(null, "SUBMIT_RESERVATION_FAILED", "PublicReservation", null,
                        errorMsg, ipAddress, userAgent);
                return null;
            }

            // 加密敏感信息
            System.out.println("开始加密敏感信息...");
            String encryptedIdCard = DataEncryptionUtil.encrypt(visitorIdCard);
            String encryptedPhone = DataEncryptionUtil.encrypt(visitorPhone);
            String phoneHash = SM3HashUtil.hash(visitorPhone);
            System.out.println("敏感信息加密完成");

            // 生成预约编号
            String reservationNo = NumberGeneratorUtil.generateReservationNumber();
            System.out.println("生成预约编号: " + reservationNo);

            // 创建预约记录
            PublicReservation reservation = new PublicReservation(
                    reservationNo, visitorName, encryptedIdCard, idCardHash,
                    encryptedPhone, phoneHash, organization, campusId,
                    visitDate, visitTimeStart, visitTimeEnd, visitReason,
                    accompanyingPersons, vehicleNumber);

            System.out.println("开始保存预约记录到数据库...");
            PublicReservation savedReservation = publicReservationDAO.addReservation(reservation);
            
            if (savedReservation != null) {
                System.out.println("预约保存成功，ID: " + savedReservation.getId());
                logAuditEvent(null, "SUBMIT_RESERVATION", "PublicReservation", savedReservation.getId(),
                        "提交社会公众预约申请: " + reservationNo, ipAddress, userAgent);
                
                // 自动审批通过公众预约
                System.out.println("开始自动审批预约...");
                boolean autoApproved = autoApproveReservation(savedReservation.getId(), ipAddress, userAgent);
                System.out.println("自动审批结果: " + (autoApproved ? "成功" : "失败"));
                
                if (autoApproved) {
                    // 重新获取更新后的预约信息（包含通行码）
                    System.out.println("重新获取更新后的预约信息...");
                    savedReservation = publicReservationDAO.getReservationById(savedReservation.getId());
                    System.out.println("更新后的预约状态: " + (savedReservation != null ? savedReservation.getStatus() : "null"));
                }
                
                return savedReservation;
            } else {
                System.out.println("预约保存失败，数据库返回null");
            }

        } catch (Exception e) {
            String errorMsg = "提交社会公众预约失败: " + e.getMessage();
            System.out.println("异常发生: " + errorMsg);
            e.printStackTrace(); // 打印完整的堆栈跟踪
            logAuditEvent(null, "SUBMIT_RESERVATION_FAILED", "PublicReservation", null,
                    errorMsg, ipAddress, userAgent);
        }
        System.out.println("社会公众预约提交流程结束，返回null");
        return null;
    }

    /**
     * 获取预约详情
     */
    public PublicReservation getReservationById(Long id) {
        return publicReservationDAO.getReservationById(id);
    }

    /**
     * 根据预约编号获取预约
     */
    public PublicReservation getReservationByReservationNo(String reservationNo) {
        return publicReservationDAO.getReservationByReservationNo(reservationNo);
    }

    /**
     * 根据通行证编码获取预约
     */
    public PublicReservation getReservationByPassCode(String passCode) {
        return publicReservationDAO.getReservationByPassCode(passCode);
    }

    /**
     * 审批预约
     */
    public boolean approveReservation(Long reservationId, Long adminId, String status,
            String comment, String ipAddress, String userAgent) {
        try {
            PublicReservation reservation = publicReservationDAO.getReservationById(reservationId);
            if (reservation == null) {
                logAuditEvent(adminId, "APPROVE_RESERVATION_FAILED", "PublicReservation", reservationId,
                        "预约记录不存在", ipAddress, userAgent);
                return false;
            }

            if (!"pending".equals(reservation.getStatus())) {
                logAuditEvent(adminId, "APPROVE_RESERVATION_FAILED", "PublicReservation", reservationId,
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

            boolean success = publicReservationDAO.approveReservation(reservationId, adminId, status,
                    comment, passCode, qrCodeData);
            if (success) {
                logAuditEvent(adminId, "APPROVE_RESERVATION", "PublicReservation", reservationId,
                        "审批社会公众预约: " + status + ", 意见: " + comment, ipAddress, userAgent);
                return true;
            }

        } catch (Exception e) {
            logAuditEvent(adminId, "APPROVE_RESERVATION_FAILED", "PublicReservation", reservationId,
                    "审批失败: " + e.getMessage(), ipAddress, userAgent);
        }
        return false;
    }

    /**
     * 取消预约
     */
    public boolean cancelReservation(Long reservationId, String ipAddress, String userAgent) {
        try {
            PublicReservation reservation = publicReservationDAO.getReservationById(reservationId);
            if (reservation == null) {
                logAuditEvent(null, "CANCEL_RESERVATION_FAILED", "PublicReservation", reservationId,
                        "预约记录不存在", ipAddress, userAgent);
                return false;
            }

            if (!"pending".equals(reservation.getStatus()) && !"approved".equals(reservation.getStatus())) {
                logAuditEvent(null, "CANCEL_RESERVATION_FAILED", "PublicReservation", reservationId,
                        "只能取消待审核或已批准的预约", ipAddress, userAgent);
                return false;
            }

            // 检查是否已过预约时间
            LocalDateTime visitDateTime = LocalDateTime.of(reservation.getVisitDate(), reservation.getVisitTimeStart());
            if (visitDateTime.isBefore(LocalDateTime.now())) {
                logAuditEvent(null, "CANCEL_RESERVATION_FAILED", "PublicReservation", reservationId,
                        "预约时间已过，无法取消", ipAddress, userAgent);
                return false;
            }

            boolean success = publicReservationDAO.cancelReservation(reservationId);
            if (success) {
                logAuditEvent(null, "CANCEL_RESERVATION", "PublicReservation", reservationId,
                        "取消社会公众预约", ipAddress, userAgent);
                return true;
            }

        } catch (Exception e) {
            logAuditEvent(null, "CANCEL_RESERVATION_FAILED", "PublicReservation", reservationId,
                    "取消预约失败: " + e.getMessage(), ipAddress, userAgent);
        }
        return false;
    }

    /**
     * 记录入校时间
     */
    public boolean recordCheckIn(Long reservationId, Long operatorId, String ipAddress, String userAgent) {
        try {
            PublicReservation reservation = publicReservationDAO.getReservationById(reservationId);
            if (reservation == null || !"approved".equals(reservation.getStatus())) {
                logAuditEvent(operatorId, "RECORD_CHECKIN_FAILED", "PublicReservation", reservationId,
                        "预约不存在或状态不正确", ipAddress, userAgent);
                return false;
            }

            LocalDateTime now = LocalDateTime.now();
            boolean success = publicReservationDAO.recordCheckIn(reservationId, now);
            if (success) {
                logAuditEvent(operatorId, "RECORD_CHECKIN", "PublicReservation", reservationId,
                        "记录入校时间", ipAddress, userAgent);
                return true;
            }

        } catch (Exception e) {
            logAuditEvent(operatorId, "RECORD_CHECKIN_FAILED", "PublicReservation", reservationId,
                    "记录入校时间失败: " + e.getMessage(), ipAddress, userAgent);
        }
        return false;
    }

    /**
     * 记录离校时间
     */
    public boolean recordCheckOut(Long reservationId, Long operatorId, String ipAddress, String userAgent) {
        try {
            PublicReservation reservation = publicReservationDAO.getReservationById(reservationId);
            if (reservation == null || reservation.getCheckInTime() == null) {
                logAuditEvent(operatorId, "RECORD_CHECKOUT_FAILED", "PublicReservation", reservationId,
                        "预约不存在或未入校", ipAddress, userAgent);
                return false;
            }

            LocalDateTime now = LocalDateTime.now();
            boolean success = publicReservationDAO.recordCheckOut(reservationId, now);
            if (success) {
                logAuditEvent(operatorId, "RECORD_CHECKOUT", "PublicReservation", reservationId,
                        "记录离校时间", ipAddress, userAgent);
                return true;
            }

        } catch (Exception e) {
            logAuditEvent(operatorId, "RECORD_CHECKOUT_FAILED", "PublicReservation", reservationId,
                    "记录离校时间失败: " + e.getMessage(), ipAddress, userAgent);
        }
        return false;
    }

    /**
     * 获取待审批的预约列表
     */
    public List<PublicReservation> getPendingReservations() {
        return publicReservationDAO.getPendingReservations();
    }

    /**
     * 获取某个校区的预约列表
     */
    public List<PublicReservation> getReservationsByCampus(Long campusId, String status) {
        return publicReservationDAO.getReservationsByCampus(campusId, status);
    }

    /**
     * 根据日期范围查询预约
     */
    public List<PublicReservation> getReservationsByDateRange(LocalDate startDate, LocalDate endDate, String status) {
        return publicReservationDAO.getReservationsByDateRange(startDate, endDate, status);
    }

    /**
     * 根据身份证号查询预约历史（需要先哈希）
     */
    public List<PublicReservation> getReservationsByIdCard(String idCard) {
        String idCardHash = SM3HashUtil.hash(idCard);
        return publicReservationDAO.getReservationsByIdCardHash(idCardHash);
    }

    /**
     * 统计各状态预约数量
     */
    public Map<String, Integer> countReservationsByStatus(LocalDate startDate, LocalDate endDate) {
        return publicReservationDAO.countReservationsByStatus(startDate, endDate);
    }

    /**
     * 统计每月预约数量
     */
    public Map<Integer, Integer> countReservationsByMonth(int year) {
        return publicReservationDAO.countReservationsByMonth(year);
    }

    /**
     * 统计每个校区的预约数量
     */
    public Map<Long, Integer> countReservationsByCampus(LocalDate startDate, LocalDate endDate) {
        return publicReservationDAO.countReservationsByCampus(startDate, endDate);
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
     * 生成二维码数据
     */
    private String generateQRCodeData(PublicReservation reservation, String passCode) {
        return String.format("SCMS_PUBLIC_%s_%s_%s_%s",
                reservation.getReservationNo(),
                passCode,
                reservation.getVisitDate(),
                reservation.getCampusId());
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
    }    /**
     * 自动审批公众预约
     * 公众预约无需人工审批，系统自动通过并生成通行码
     */
    public boolean autoApproveReservation(Long reservationId, String ipAddress, String userAgent) {
        try {
            System.out.println("开始自动审批预约，预约ID: " + reservationId);
            
            PublicReservation reservation = publicReservationDAO.getReservationById(reservationId);
            if (reservation == null) {
                String errorMsg = "预约记录不存在";
                System.out.println("自动审批失败: " + errorMsg);
                logAuditEvent(null, "AUTO_APPROVE_FAILED", "PublicReservation", reservationId,
                        errorMsg, ipAddress, userAgent);
                return false;
            }

            System.out.println("预约记录查询成功 - 编号: " + reservation.getReservationNo() + 
                ", 状态: " + reservation.getStatus());

            if (!"pending".equals(reservation.getStatus())) {
                String errorMsg = "预约状态不是待审核，当前状态: " + reservation.getStatus();
                System.out.println("自动审批失败: " + errorMsg);
                logAuditEvent(null, "AUTO_APPROVE_FAILED", "PublicReservation", reservationId,
                        errorMsg, ipAddress, userAgent);
                return false;
            }

            // 生成通行证编码和二维码数据
            System.out.println("生成通行证编码和二维码数据...");
            String passCode = NumberGeneratorUtil.generatePassCode();
            String qrCodeData = generateQRCodeData(reservation, passCode);
            System.out.println("通行证编码: " + passCode);
            System.out.println("二维码数据: " + qrCodeData);

            // 系统自动审批通过，无需管理员ID
            System.out.println("更新预约状态为已批准...");
            boolean success = publicReservationDAO.approveReservation(reservationId, null, "approved",
                    "系统自动审批通过", passCode, qrCodeData);
            
            if (success) {
                System.out.println("自动审批成功完成");
                logAuditEvent(null, "AUTO_APPROVE_RESERVATION", "PublicReservation", reservationId,
                        "系统自动审批通过公众预约，通行码: " + passCode, ipAddress, userAgent);
                return true;
            } else {
                System.out.println("数据库更新失败，自动审批未成功");
            }

        } catch (Exception e) {
            String errorMsg = "自动审批失败: " + e.getMessage();
            System.out.println("自动审批异常: " + errorMsg);
            e.printStackTrace(); // 打印完整的堆栈跟踪
            logAuditEvent(null, "AUTO_APPROVE_FAILED", "PublicReservation", reservationId,
                    errorMsg, ipAddress, userAgent);
        }
        System.out.println("自动审批流程结束，返回false");
        return false;
    }
}
