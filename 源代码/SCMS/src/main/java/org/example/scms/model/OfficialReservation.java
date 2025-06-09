package org.example.scms.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 公务预约实体类
 */
public class OfficialReservation {
    private Long id;
    private String reservationNo; // 预约编号
    private String visitorName; // 预约人姓名
    private String visitorIdCardEncrypted; // 加密的身份证号(SM4)
    private String visitorIdCardHash; // 身份证号哈希值(SM3)
    private String visitorPhoneEncrypted; // 加密的手机号(SM4)
    private String visitorPhoneHash; // 手机号哈希值(SM3)
    private String visitorOrganization; // 访客所在单位
    private Long hostDepartmentId; // 接待部门ID
    private String hostName; // 接待人姓名
    private String hostPhone; // 接待人电话
    private Long campusId; // 预约校区ID
    private LocalDate visitDate; // 预约日期
    private LocalTime visitTimeStart; // 预约开始时间
    private LocalTime visitTimeEnd; // 预约结束时间
    private String visitReason; // 来访事由
    private Integer accompanyingPersons; // 随行人数
    private String vehicleNumber; // 车牌号
    private String status; // 预约状态: pending-待审核, approved-已批准, rejected-已拒绝, cancelled-已取消, completed-已完成
    private String approvalComment; // 审批意见
    private Long approvedBy; // 审批人ID
    private LocalDateTime approvedAt; // 审批时间
    private LocalDateTime checkInTime; // 实际入校时间
    private LocalDateTime checkOutTime; // 实际离校时间
    private String passCode; // 通行证编码
    private String qrCodeData; // QR码数据
    private LocalDateTime createdAt; // 申请时间
    private LocalDateTime updatedAt; // 更新时间

    // 构造函数
    public OfficialReservation() {
    }

    public OfficialReservation(String reservationNo, String visitorName, String visitorIdCardEncrypted,
            String visitorIdCardHash,
            String visitorPhoneEncrypted, String visitorPhoneHash, String visitorOrganization,
            Long hostDepartmentId, String hostName, String hostPhone, Long campusId,
            LocalDate visitDate, LocalTime visitTimeStart, LocalTime visitTimeEnd,
            String visitReason, Integer accompanyingPersons, String vehicleNumber) {
        this.reservationNo = reservationNo;
        this.visitorName = visitorName;
        this.visitorIdCardEncrypted = visitorIdCardEncrypted;
        this.visitorIdCardHash = visitorIdCardHash;
        this.visitorPhoneEncrypted = visitorPhoneEncrypted;
        this.visitorPhoneHash = visitorPhoneHash;
        this.visitorOrganization = visitorOrganization;
        this.hostDepartmentId = hostDepartmentId;
        this.hostName = hostName;
        this.hostPhone = hostPhone;
        this.campusId = campusId;
        this.visitDate = visitDate;
        this.visitTimeStart = visitTimeStart;
        this.visitTimeEnd = visitTimeEnd;
        this.visitReason = visitReason;
        this.accompanyingPersons = accompanyingPersons;
        this.vehicleNumber = vehicleNumber;
        this.status = "pending";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReservationNo() {
        return reservationNo;
    }

    public void setReservationNo(String reservationNo) {
        this.reservationNo = reservationNo;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

    public String getVisitorIdCardEncrypted() {
        return visitorIdCardEncrypted;
    }

    public void setVisitorIdCardEncrypted(String visitorIdCardEncrypted) {
        this.visitorIdCardEncrypted = visitorIdCardEncrypted;
    }

    public String getVisitorIdCardHash() {
        return visitorIdCardHash;
    }

    public void setVisitorIdCardHash(String visitorIdCardHash) {
        this.visitorIdCardHash = visitorIdCardHash;
    }

    public String getVisitorPhoneEncrypted() {
        return visitorPhoneEncrypted;
    }

    public void setVisitorPhoneEncrypted(String visitorPhoneEncrypted) {
        this.visitorPhoneEncrypted = visitorPhoneEncrypted;
    }

    public String getVisitorPhoneHash() {
        return visitorPhoneHash;
    }

    public void setVisitorPhoneHash(String visitorPhoneHash) {
        this.visitorPhoneHash = visitorPhoneHash;
    }

    public String getVisitorOrganization() {
        return visitorOrganization;
    }

    public void setVisitorOrganization(String visitorOrganization) {
        this.visitorOrganization = visitorOrganization;
    }

    public Long getHostDepartmentId() {
        return hostDepartmentId;
    }

    public void setHostDepartmentId(Long hostDepartmentId) {
        this.hostDepartmentId = hostDepartmentId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostPhone() {
        return hostPhone;
    }

    public void setHostPhone(String hostPhone) {
        this.hostPhone = hostPhone;
    }

    public Long getCampusId() {
        return campusId;
    }

    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }

    public LocalTime getVisitTimeStart() {
        return visitTimeStart;
    }

    public void setVisitTimeStart(LocalTime visitTimeStart) {
        this.visitTimeStart = visitTimeStart;
    }

    public LocalTime getVisitTimeEnd() {
        return visitTimeEnd;
    }

    public void setVisitTimeEnd(LocalTime visitTimeEnd) {
        this.visitTimeEnd = visitTimeEnd;
    }

    public String getVisitReason() {
        return visitReason;
    }

    public void setVisitReason(String visitReason) {
        this.visitReason = visitReason;
    }

    public Integer getAccompanyingPersons() {
        return accompanyingPersons;
    }

    public void setAccompanyingPersons(Integer accompanyingPersons) {
        this.accompanyingPersons = accompanyingPersons;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApprovalComment() {
        return approvalComment;
    }

    public void setApprovalComment(String approvalComment) {
        this.approvalComment = approvalComment;
    }

    public Long getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Long approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public String getPassCode() {
        return passCode;
    }

    public void setPassCode(String passCode) {
        this.passCode = passCode;
    }

    public String getQrCodeData() {
        return qrCodeData;
    }

    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // 获取预约月份(用于统计)
    public int getApplicationMonth() {
        return createdAt.getMonthValue();
    }

    // 获取预约月份(用于统计)
    public int getVisitMonth() {
        return visitDate.getMonthValue();
    }

    @Override
    public String toString() {
        return "OfficialReservation{" +
                "id=" + id +
                ", reservationNo='" + reservationNo + '\'' +
                ", visitorName='" + visitorName + '\'' +
                ", hostDepartmentId=" + hostDepartmentId +
                ", visitDate=" + visitDate +
                ", status='" + status + '\'' +
                '}';
    }
}
