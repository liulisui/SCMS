package org.example.scms.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约查询结果项
 * 统一的预约信息展示类，用于管理员查询界面
 */
public class ReservationQueryItem {
    private Long id;
    private String reservationNo;
    private String reservationType; // public, official
    private String reservationTypeText;
    private String visitorName;
    private String visitorPhone;
    private String organization;
    private Long campusId;
    private String campusName;
    private Long departmentId; // 仅公务预约
    private String departmentName; // 仅公务预约
    private String hostName; // 仅公务预约
    private String hostPhone; // 仅公务预约
    private LocalDate visitDate;
    private LocalTime visitTimeStart;
    private LocalTime visitTimeEnd;
    private String visitReason;
    private Integer accompanyingPersons;
    private String status;
    private String statusText;
    private String approvalComment;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    
    public ReservationQueryItem() {}
    
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
    
    public String getReservationType() {
        return reservationType;
    }
    
    public void setReservationType(String reservationType) {
        this.reservationType = reservationType;
    }
    
    public String getReservationTypeText() {
        return reservationTypeText;
    }
    
    public void setReservationTypeText(String reservationTypeText) {
        this.reservationTypeText = reservationTypeText;
    }
    
    public String getVisitorName() {
        return visitorName;
    }
    
    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }
    
    public String getVisitorPhone() {
        return visitorPhone;
    }
    
    public void setVisitorPhone(String visitorPhone) {
        this.visitorPhone = visitorPhone;
    }
    
    public String getOrganization() {
        return organization;
    }
    
    public void setOrganization(String organization) {
        this.organization = organization;
    }
    
    public Long getCampusId() {
        return campusId;
    }
    
    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }
    
    public String getCampusName() {
        return campusName;
    }
    
    public void setCampusName(String campusName) {
        this.campusName = campusName;
    }
    
    public Long getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getStatusText() {
        return statusText;
    }
    
    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }
    
    public String getApprovalComment() {
        return approvalComment;
    }
    
    public void setApprovalComment(String approvalComment) {
        this.approvalComment = approvalComment;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
    
    /**
     * 判断是否为公务预约
     */
    public boolean isOfficialReservation() {
        return "official".equals(reservationType);
    }
    
    /**
     * 获取格式化的访问时间
     */
    public String getFormattedVisitTime() {
        if (visitTimeStart != null && visitTimeEnd != null) {
            return visitTimeStart.toString() + " - " + visitTimeEnd.toString();
        }
        return "";
    }
}
