package org.example.scms.model;

import java.time.LocalDateTime;

/**
 * 管理员审计日志实体类
 */
public class AuditLog {
    private Long id;
    private Long adminId; // 管理员ID
    private String username; // 管理员用户名（用于显示）
    private String action; // 操作类型
    private String resourceType; // 资源类型
    private Long resourceId; // 资源ID
    private String details; // 操作详情
    private String oldValue; // 旧值(JSON格式)
    private String newValue; // 新值(JSON格式)
    private String ipAddress; // IP地址
    private String userAgent; // 用户代理
    private String hmacValue; // HMAC-SM3值
    private LocalDateTime createdAt; // 创建时间

    // 构造函数
    public AuditLog() {
    }

    public AuditLog(Long adminId, String action, String resourceType, Long resourceId,
            String details, String oldValue, String newValue, String ipAddress, String userAgent) {
        this.adminId = adminId;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.details = details;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = LocalDateTime.now();
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getHmacValue() {
        return hmacValue;
    }

    public void setHmacValue(String hmacValue) {
        this.hmacValue = hmacValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", adminId=" + adminId +
                ", action='" + action + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", resourceId=" + resourceId +
                ", createdAt=" + createdAt +
                '}';
    }
}