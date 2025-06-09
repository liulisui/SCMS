package org.example.scms.model;

import java.time.LocalDateTime;

/**
 * 管理员实体类
 */
public class Administrator {
    private Long id;
    private String username; // 登录名
    private String password; // SM3加密存储的密码
    private String salt; // 密码盐值
    private String fullName; // 姓名
    private String phoneEncrypted; // SM4加密的联系电话
    private String phoneHash; // SM3哈希的联系电话
    private Long departmentId; // 所在部门ID
    private String adminType; // 管理员类型：SCHOOL(学校管理员)、DEPARTMENT(部门管理员)、AUDIT(审计管理员)、SYSTEM(系统管理员)
    private LocalDateTime lastLoginTime; // 最后登录时间
    private LocalDateTime lastPasswordChangeTime; // 最后修改密码时间
    private Integer loginFailCount; // 登录失败次数
    private LocalDateTime lockUntilTime; // 锁定结束时间
    private String status; // 状态：active-激活, inactive-未激活, locked-锁定
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 构造函数
    public Administrator() {
    }

    public Administrator(String username, String password, String salt, String fullName, String phoneEncrypted,
            String phoneHash, Long departmentId, String adminType) {
        this.username = username;
        this.password = password;
        this.salt = salt;
        this.fullName = fullName;
        this.phoneEncrypted = phoneEncrypted;
        this.phoneHash = phoneHash;
        this.departmentId = departmentId;
        this.adminType = adminType;
        this.loginFailCount = 0;
        this.status = "active";
        this.lastPasswordChangeTime = LocalDateTime.now();
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneEncrypted() {
        return phoneEncrypted;
    }

    public void setPhoneEncrypted(String phoneEncrypted) {
        this.phoneEncrypted = phoneEncrypted;
    }

    public String getPhoneHash() {
        return phoneHash;
    }

    public void setPhoneHash(String phoneHash) {
        this.phoneHash = phoneHash;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getAdminType() {
        return adminType;
    }

    public void setAdminType(String adminType) {
        this.adminType = adminType;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public LocalDateTime getLastPasswordChangeTime() {
        return lastPasswordChangeTime;
    }

    public void setLastPasswordChangeTime(LocalDateTime lastPasswordChangeTime) {
        this.lastPasswordChangeTime = lastPasswordChangeTime;
    }

    public Integer getLoginFailCount() {
        return loginFailCount;
    }

    public void setLoginFailCount(Integer loginFailCount) {
        this.loginFailCount = loginFailCount;
    }

    public LocalDateTime getLockUntilTime() {
        return lockUntilTime;
    }

    public void setLockUntilTime(LocalDateTime lockUntilTime) {
        this.lockUntilTime = lockUntilTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    // 检查密码是否需要更新 (超过90天)
    public boolean isPasswordExpired() {
        if (lastPasswordChangeTime == null) {
            return true;
        }
        return lastPasswordChangeTime.plusDays(90).isBefore(LocalDateTime.now());
    }

    // 检查账户是否被锁定
    public boolean isLocked() {
        if ("locked".equals(status) && lockUntilTime != null) {
            return lockUntilTime.isAfter(LocalDateTime.now());
        }
        return false;
    }

    // 增加登录失败次数
    public void incrementLoginFailCount() {
        this.loginFailCount = this.loginFailCount == null ? 1 : this.loginFailCount + 1;
        if (this.loginFailCount >= 5) {
            this.status = "locked";
            this.lockUntilTime = LocalDateTime.now().plusMinutes(30);
        }
    }

    // 重置登录失败次数
    public void resetLoginFailCount() {
        this.loginFailCount = 0;
        this.status = "active";
        this.lockUntilTime = null;
    }

    @Override
    public String toString() {
        return "Administrator{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", adminType='" + adminType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}