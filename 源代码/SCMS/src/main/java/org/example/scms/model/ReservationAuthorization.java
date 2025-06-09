package org.example.scms.model;

import java.time.LocalDateTime;

/**
 * 部门预约授权实体类
 */
public class ReservationAuthorization {
    private Long id;
    private Long departmentId; // 部门ID
    private String reservationType; // 授权预约类型: public(社会公众预约)、official(公务预约)、both(两者都有)
    private Long grantedBy; // 授权人ID(管理员ID)
    private LocalDateTime grantedAt; // 授权时间
    private LocalDateTime revokedAt; // 撤销时间
    private String status; // 授权状态: active-有效, revoked-已撤销
    private String description; // 授权说明
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 构造函数
    public ReservationAuthorization() {
    }

    public ReservationAuthorization(Long departmentId, String reservationType, Long grantedBy, String description) {
        this.departmentId = departmentId;
        this.reservationType = reservationType;
        this.grantedBy = grantedBy;
        this.description = description;
        this.status = "active";
        this.grantedAt = LocalDateTime.now();
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

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getReservationType() {
        return reservationType;
    }

    public void setReservationType(String reservationType) {
        this.reservationType = reservationType;
    }

    public Long getGrantedBy() {
        return grantedBy;
    }

    public void setGrantedBy(Long grantedBy) {
        this.grantedBy = grantedBy;
    }

    public LocalDateTime getGrantedAt() {
        return grantedAt;
    }

    public void setGrantedAt(LocalDateTime grantedAt) {
        this.grantedAt = grantedAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    // 撤销授权
    public void revoke() {
        this.status = "revoked";
        this.revokedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "ReservationAuthorization{" +
                "id=" + id +
                ", departmentId=" + departmentId +
                ", reservationType='" + reservationType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}