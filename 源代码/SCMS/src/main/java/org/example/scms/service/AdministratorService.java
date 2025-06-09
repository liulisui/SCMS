package org.example.scms.service;

import java.time.LocalDateTime;
import java.util.List;

import org.example.scms.dao.AdministratorDAO;
import org.example.scms.dao.AuditLogDAO;
import org.example.scms.model.Administrator;
import org.example.scms.model.AuditLog;
import org.example.scms.util.DataEncryptionUtil;
import org.example.scms.util.SM3HashUtil;

/**
 * 管理员服务类
 * 处理管理员相关的业务逻辑
 */
public class AdministratorService {
    private AdministratorDAO administratorDAO;
    private AuditLogDAO auditLogDAO;

    public AdministratorService() {
        this.administratorDAO = new AdministratorDAO();
        this.auditLogDAO = new AuditLogDAO();
    }

    /**
     * 管理员登录验证
     */
    public Administrator login(String username, String password, String ipAddress, String userAgent) {
        Administrator admin = administratorDAO.getAdministratorByUsername(username);

        if (admin == null) {
            logAuditEvent(null, "LOGIN_FAILED", "Administrator", null,
                    "用户名不存在: " + username, ipAddress, userAgent);
            return null;
        }

        // 检查账户状态
        if ("locked".equals(admin.getStatus())) {
            // 检查锁定时间是否已过期
            if (admin.getLockUntilTime() != null && LocalDateTime.now().isAfter(admin.getLockUntilTime())) {
                // 锁定时间已过期，自动解锁账户
                administratorDAO.unlockAccount(admin.getId());
                logAuditEvent(admin.getId(), "ACCOUNT_AUTO_UNLOCKED", "Administrator", admin.getId(),
                        "锁定时间已过期，自动解锁账户", ipAddress, userAgent);
                // 重新获取管理员信息
                admin = administratorDAO.getAdministratorByUsername(username);
            } else {
                logAuditEvent(admin.getId(), "LOGIN_FAILED", "Administrator", admin.getId(),
                        "账户已锁定", ipAddress, userAgent);
                return null;
            }
        }

        if ("inactive".equals(admin.getStatus())) {
            logAuditEvent(admin.getId(), "LOGIN_FAILED", "Administrator", admin.getId(),
                    "账户已停用", ipAddress, userAgent);
            return null;
        }

        // 验证密码
        boolean passwordValid = SM3HashUtil.verifyPassword(password, admin.getSalt(), admin.getPassword());

        if (passwordValid) {
            // 登录成功，重置失败次数
            administratorDAO.resetLoginFailCount(admin.getId());
            administratorDAO.updateLastLoginTime(admin.getId(), LocalDateTime.now());

            logAuditEvent(admin.getId(), "LOGIN_SUCCESS", "Administrator", admin.getId(),
                    "登录成功", ipAddress, userAgent);

            return admin;
        } else {
            // 登录失败，增加失败次数
            int newFailCount = admin.getLoginFailCount() + 1;
            administratorDAO.updateLoginFailCount(admin.getId(), newFailCount);

            // 如果失败次数达到5次，锁定账户
            if (newFailCount >= 5) {
                administratorDAO.lockAccount(admin.getId());
                logAuditEvent(admin.getId(), "ACCOUNT_LOCKED", "Administrator", admin.getId(),
                        "连续登录失败5次，账户已锁定", ipAddress, userAgent);
            } else {
                logAuditEvent(admin.getId(), "LOGIN_FAILED", "Administrator", admin.getId(),
                        "密码错误，剩余尝试次数: " + (5 - newFailCount), ipAddress, userAgent);
            }

            return null;
        }
    }

    /**
     * 创建新管理员
     */
    public Administrator createAdministrator(Administrator admin, Long operatorId, String ipAddress, String userAgent) {
        // 检查用户名是否已存在
        if (administratorDAO.getAdministratorByUsername(admin.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 生成盐值和哈希密码
        String salt = SM3HashUtil.generateSalt(16);
        String hashedPassword = SM3HashUtil.hashWithSalt(admin.getPassword(), salt);

        admin.setSalt(salt);
        admin.setPassword(hashedPassword);
        admin.setLastPasswordChangeTime(LocalDateTime.now());

        // 加密手机号
        if (admin.getPhoneEncrypted() != null) {
            String encryptedPhone = DataEncryptionUtil.encrypt(admin.getPhoneEncrypted());
            String phoneHash = SM3HashUtil.hash(admin.getPhoneEncrypted());
            admin.setPhoneEncrypted(encryptedPhone);
            admin.setPhoneHash(phoneHash);
        }

        Administrator createdAdmin = administratorDAO.addAdministrator(admin);

        if (createdAdmin != null) {
            logAuditEvent(operatorId, "CREATE", "Administrator", createdAdmin.getId(),
                    "创建管理员: " + admin.getUsername(), ipAddress, userAgent);
        }

        return createdAdmin;
    }

    /**
     * 更新管理员信息
     */
    public boolean updateAdministrator(Administrator admin, Long operatorId, String ipAddress, String userAgent) {
        Administrator oldAdmin = administratorDAO.getAdministratorById(admin.getId());
        if (oldAdmin == null) {
            return false;
        }

        // 如果电话号码有变化，重新加密
        if (admin.getPhoneEncrypted() != null &&
                !admin.getPhoneEncrypted().equals(oldAdmin.getPhoneEncrypted())) {
            String encryptedPhone = DataEncryptionUtil.encrypt(admin.getPhoneEncrypted());
            String phoneHash = SM3HashUtil.hash(admin.getPhoneEncrypted());
            admin.setPhoneEncrypted(encryptedPhone);
            admin.setPhoneHash(phoneHash);
        }

        boolean success = administratorDAO.updateAdministrator(admin);

        if (success) {
            logAuditEvent(operatorId, "UPDATE", "Administrator", admin.getId(),
                    "更新管理员信息: " + admin.getUsername(), ipAddress, userAgent);
        }

        return success;
    }

    /**
     * 修改密码
     */
    public boolean changePassword(Long adminId, String oldPassword, String newPassword,
            Long operatorId, String ipAddress, String userAgent) {
        Administrator admin = administratorDAO.getAdministratorById(adminId);
        if (admin == null) {
            return false;
        }

        // 验证旧密码
        if (!SM3HashUtil.verifyPassword(oldPassword, admin.getSalt(), admin.getPassword())) {
            logAuditEvent(operatorId, "PASSWORD_CHANGE_FAILED", "Administrator", adminId,
                    "旧密码验证失败", ipAddress, userAgent);
            return false;
        }

        // 生成新的盐值和哈希密码
        String newSalt = SM3HashUtil.generateSalt(16);
        String newHashedPassword = SM3HashUtil.hashWithSalt(newPassword, newSalt);

        boolean success = administratorDAO.updatePassword(adminId, newHashedPassword, newSalt);

        if (success) {
            logAuditEvent(operatorId, "PASSWORD_CHANGED", "Administrator", adminId,
                    "密码修改成功", ipAddress, userAgent);
        }

        return success;
    }

    /**
     * 重置密码（管理员操作）
     */
    public String resetPassword(Long adminId, Long operatorId, String ipAddress, String userAgent) {
        Administrator admin = administratorDAO.getAdministratorById(adminId);
        if (admin == null) {
            return null;
        }

        // 生成随机密码
        String newPassword = generateRandomPassword();
        String newSalt = SM3HashUtil.generateSalt(16);
        String newHashedPassword = SM3HashUtil.hashWithSalt(newPassword, newSalt);

        boolean success = administratorDAO.updatePassword(adminId, newHashedPassword, newSalt);

        if (success) {
            logAuditEvent(operatorId, "PASSWORD_RESET", "Administrator", adminId,
                    "管理员重置密码", ipAddress, userAgent);
            return newPassword;
        }

        return null;
    }

    /**
     * 锁定/解锁账户
     */
    public boolean toggleAccountLock(Long adminId, boolean lock, Long operatorId, String ipAddress, String userAgent) {
        boolean success;
        String action;
        String details;

        if (lock) {
            success = administratorDAO.lockAccount(adminId);
            action = "ACCOUNT_LOCKED";
            details = "管理员锁定账户";
        } else {
            success = administratorDAO.unlockAccount(adminId);
            action = "ACCOUNT_UNLOCKED";
            details = "管理员解锁账户";
        }

        if (success) {
            logAuditEvent(operatorId, action, "Administrator", adminId, details, ipAddress, userAgent);
        }

        return success;
    }

    /**
     * 获取所有管理员列表
     */
    public List<Administrator> getAllAdministrators() {
        return administratorDAO.getAllAdministrators();
    }

    /**
     * 根据部门获取管理员列表
     */
    public List<Administrator> getAdministratorsByDepartment(Long departmentId) {
        return administratorDAO.getAdministratorsByDepartment(departmentId);
    }

    /**
     * 根据管理员类型获取管理员列表
     */
    public List<Administrator> getAdministratorsByType(String adminType) {
        return administratorDAO.getAdministratorsByType(adminType);
    }

    /**
     * 根据真实姓名获取管理员列表
     */
    public List<Administrator> getAdministratorsByRealName(String realName) {
        return administratorDAO.getAdministratorsByRealName(realName);
    }

    /**
     * 根据ID获取管理员
     */
    public Administrator getAdministratorById(Long id) {
        return administratorDAO.getAdministratorById(id);
    }

    /**
     * 根据用户名获取管理员
     */
    public Administrator getAdministratorByUsername(String username) {
        return administratorDAO.getAdministratorByUsername(username);
    }

    /**
     * 删除管理员
     */
    public boolean deleteAdministrator(Long adminId, Long operatorId, String ipAddress, String userAgent) {
        Administrator admin = administratorDAO.getAdministratorById(adminId);
        if (admin == null) {
            return false;
        }

        boolean success = administratorDAO.deleteAdministrator(adminId);

        if (success) {
            logAuditEvent(operatorId, "DELETE", "Administrator", adminId,
                    "删除管理员: " + admin.getUsername(), ipAddress, userAgent);
        }

        return success;
    }

    /**
     * 激活管理员账户
     */
    public boolean activateAdministrator(Long adminId, Long operatorId, String ipAddress, String userAgent) {
        Administrator admin = administratorDAO.getAdministratorById(adminId);
        if (admin == null) {
            return false;
        }

        admin.setStatus("active");
        boolean success = administratorDAO.updateAdministrator(admin);

        if (success) {
            logAuditEvent(operatorId, "ACTIVATE", "Administrator", adminId,
                    "激活管理员账户: " + admin.getUsername(), ipAddress, userAgent);
        }

        return success;
    }

    /**
     * 停用管理员账户
     */
    public boolean deactivateAdministrator(Long adminId, Long operatorId, String ipAddress, String userAgent) {
        Administrator admin = administratorDAO.getAdministratorById(adminId);
        if (admin == null) {
            return false;
        }

        admin.setStatus("inactive");
        boolean success = administratorDAO.updateAdministrator(admin);

        if (success) {
            logAuditEvent(operatorId, "DEACTIVATE", "Administrator", adminId,
                    "停用管理员账户: " + admin.getUsername(), ipAddress, userAgent);
        }

        return success;
    }

    /**
     * 检查密码是否即将过期
     */
    public boolean isPasswordNearExpiry(Administrator admin) {
        if (admin.getLastPasswordChangeTime() == null) {
            return true; // 如果从未更改过密码，需要立即更改
        }

        LocalDateTime expiryDate = admin.getLastPasswordChangeTime().plusDays(90); // 90天密码有效期
        LocalDateTime warningDate = expiryDate.minusDays(7); // 提前7天警告

        return LocalDateTime.now().isAfter(warningDate);
    }

    /**
     * 检查密码是否已过期
     */
    public boolean isPasswordExpired(Administrator admin) {
        if (admin.getLastPasswordChangeTime() == null) {
            return true;
        }

        LocalDateTime expiryDate = admin.getLastPasswordChangeTime().plusDays(90);
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * 生成随机密码
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 12; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }

        return password.toString();
    }

    /**
     * 记录审计日志
     */
    private void logAuditEvent(Long adminId, String action, String resourceType, Long resourceId,
            String details, String ipAddress, String userAgent) {
        try {
            AuditLog log = new AuditLog();
            log.setAdminId(adminId);
            log.setAction(action);
            log.setResourceType(resourceType);
            log.setResourceId(resourceId);
            log.setDetails(details);
            log.setIpAddress(ipAddress);
            log.setUserAgent(userAgent);
            log.setCreatedAt(LocalDateTime.now());
            
            // 计算HMAC值确保日志完整性
            String logData = String.format("%s:%s:%s:%s:%s",
                    action, resourceType, resourceId, details, log.getCreatedAt());
            String hmacKey = "SCMS_AUDIT_LOG_HMAC_SECRET_KEY_2024";
            String hmacValue = SM3HashUtil.hmac(logData, hmacKey);
            log.setHmacValue(hmacValue);

            auditLogDAO.addAuditLog(log);
        } catch (Exception e) {
            // 审计日志记录失败不应影响主要业务逻辑
            e.printStackTrace();
        }
    }
}
