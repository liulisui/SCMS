package org.example.scms.model;

import java.time.LocalDateTime;

/**
 * 预约实体类
 */
public class Reservation {
    private int id;
    private Long userId;
    private String userName;
    private String phone;
    private String idCard;
    private String reservationType; // public-社会公众, official-公务
    private String purpose; // 来访目的
    private String destination; // 目的地
    private LocalDateTime visitTime; // 来访时间
    private String duration; // 停留时长

    // 新增手机端功能字段
    private String campus; // 预约校区
    private String organization; // 所在单位
    private String transportMode; // 交通方式
    private String licensePlate; // 车牌号
    private String companions; // 随行人员信息(JSON格式)
    private String officialDepartment; // 公务访问部门
    private String officialContactPerson; // 公务访问接待人
    private String officialReason; // 公务来访事由
    private String qrCodeData; // 二维码数据
    private String realIdCard; // 真实身份证号
    private String realName; // 真实姓名
    private String realPhone; // 真实手机号
    private String status; // pending-待审核, approved-已通过, rejected-已拒绝, expired-已过期
    private String reviewReason; // 审核意见
    private int reviewerId; // 审核人ID
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime reviewTime;

    public Reservation() {
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getReservationType() {
        return reservationType;
    }

    public void setReservationType(String reservationType) {
        this.reservationType = reservationType;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDateTime getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(LocalDateTime visitTime) {
        this.visitTime = visitTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReviewReason() {
        return reviewReason;
    }

    public void setReviewReason(String reviewReason) {
        this.reviewReason = reviewReason;
    }

    public int getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(int reviewerId) {
        this.reviewerId = reviewerId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public LocalDateTime getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(LocalDateTime reviewTime) {
        this.reviewTime = reviewTime;
    }

    // 新增字段的getter和setter方法
    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getTransportMode() {
        return transportMode;
    }

    public void setTransportMode(String transportMode) {
        this.transportMode = transportMode;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getCompanions() {
        return companions;
    }

    public void setCompanions(String companions) {
        this.companions = companions;
    }

    public String getOfficialDepartment() {
        return officialDepartment;
    }

    public void setOfficialDepartment(String officialDepartment) {
        this.officialDepartment = officialDepartment;
    }

    public String getOfficialContactPerson() {
        return officialContactPerson;
    }

    public void setOfficialContactPerson(String officialContactPerson) {
        this.officialContactPerson = officialContactPerson;
    }

    public String getOfficialReason() {
        return officialReason;
    }

    public void setOfficialReason(String officialReason) {
        this.officialReason = officialReason;
    }

    public String getQrCodeData() {
        return qrCodeData;
    }

    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }

    public String getRealIdCard() {
        return realIdCard;
    }

    public void setRealIdCard(String realIdCard) {
        this.realIdCard = realIdCard;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getRealPhone() {
        return realPhone;
    }

    public void setRealPhone(String realPhone) {
        this.realPhone = realPhone;
    }

    // 别名方法，为兼容JSP调用
    public String getContactPhone() {
        return realPhone; // 返回真实手机号
    }

    public String getDepartment() {
        return officialDepartment; // 返回公务访问部门
    }

    // 添加JSP兼容性方法
    public String getName() {
        return realName != null ? realName : userName;
    }

    public java.time.LocalDate getReservationDate() {
        return visitTime != null ? visitTime.toLocalDate() : null;
    }

    public LocalDateTime getCreatedAt() {
        return createTime;
    }
}
