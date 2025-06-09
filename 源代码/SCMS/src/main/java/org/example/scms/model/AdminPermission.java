package org.example.scms.model;

import java.time.LocalDateTime;

/**
 * 管理员权限关联实体类
 */
public class AdminPermission {
    private Long id;
    private Long adminId; // 管理员ID
    private Long permissionId; // 权限ID
    private Long grantedBy; // 授权人ID
    private LocalDateTime grantedAt; // 授权时间
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 构造函数
    public AdminPermission() {
    }

    public AdminPermission(Long adminId, Long permissionId, Long grantedBy) {
        this.adminId = adminId;
        this.permissionId = permissionId;
        this.grantedBy = grantedBy;
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

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
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

    @Override
    public String toString() {
        return "AdminPermission{" +
                "id=" + id +
                ", adminId=" + adminId +
                ", permissionId=" + permissionId +
                ", grantedAt=" + grantedAt +
                '}';
    }
}
