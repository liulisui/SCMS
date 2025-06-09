package org.example.scms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.example.scms.model.Administrator;
import org.example.scms.util.DBUtil;

/**
 * 管理员数据访问对象
 */
public class AdministratorDAO {

    /**
     * 根据ID获取管理员
     */
    public Administrator getAdministratorById(Long id) {
        String sql = "SELECT * FROM administrators WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAdministrator(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据用户名获取管理员
     */
    public Administrator getAdministratorByUsername(String username) {
        String sql = "SELECT * FROM administrators WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAdministrator(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加管理员
     */    public Administrator addAdministrator(Administrator admin) {
        String sql = "INSERT INTO administrators (username, password, salt, real_name, phone_encrypted, phone_hash, " +
                "department_id, admin_type, failed_login_attempts, status, password_last_changed, created_at, updated_at) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();            stmt.setString(1, admin.getUsername());
            stmt.setString(2, admin.getPassword());
            stmt.setString(3, admin.getSalt());
            stmt.setString(4, admin.getFullName());
            stmt.setString(5, admin.getPhoneEncrypted());
            stmt.setString(6, admin.getPhoneHash());
            if (admin.getDepartmentId() != null) {
                stmt.setLong(7, admin.getDepartmentId());
            } else {
                stmt.setNull(7, java.sql.Types.BIGINT);
            }
            stmt.setString(8, admin.getAdminType());
            stmt.setInt(9, admin.getLoginFailCount());
            stmt.setString(10, admin.getStatus());
            stmt.setTimestamp(11, Timestamp.valueOf(admin.getLastPasswordChangeTime()));
            stmt.setTimestamp(12, Timestamp.valueOf(now));
            stmt.setTimestamp(13, Timestamp.valueOf(now));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("添加管理员失败，没有行被插入。");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    admin.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("添加管理员失败，未获取到ID。");
                }
            }

            return admin;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 更新管理员
     */    public boolean updateAdministrator(Administrator admin) {
        String sql = "UPDATE administrators SET username = ?, real_name = ?, phone_encrypted = ?, " +
                "phone_hash = ?, department_id = ?, admin_type = ?, status = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {            stmt.setString(1, admin.getUsername());
            stmt.setString(2, admin.getFullName());
            stmt.setString(3, admin.getPhoneEncrypted());
            stmt.setString(4, admin.getPhoneHash());
            if (admin.getDepartmentId() != null) {
                stmt.setLong(5, admin.getDepartmentId());
            } else {
                stmt.setNull(5, java.sql.Types.BIGINT);
            }
            stmt.setString(6, admin.getAdminType());
            stmt.setString(7, admin.getStatus());
            stmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(9, admin.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新管理员密码
     */    public boolean updateAdministratorPassword(Long adminId, String newPassword, String newSalt) {
        String sql = "UPDATE administrators SET password = ?, salt = ?, password_last_changed = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            LocalDateTime now = LocalDateTime.now();

            stmt.setString(1, newPassword);
            stmt.setString(2, newSalt);
            stmt.setTimestamp(3, Timestamp.valueOf(now));
            stmt.setTimestamp(4, Timestamp.valueOf(now));
            stmt.setLong(5, adminId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新管理员登录失败次数
     */    public boolean updateLoginFailCount(Long adminId, int loginFailCount) {
        String sql = "UPDATE administrators SET failed_login_attempts = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, loginFailCount);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(3, adminId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 锁定管理员账户
     */    public boolean lockAdministrator(Long adminId, LocalDateTime lockUntilTime) {
        String sql = "UPDATE administrators SET status = 'locked', locked_until = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(lockUntilTime));
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(3, adminId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 解锁管理员账户
     */    public boolean unlockAdministrator(Long adminId) {
        String sql = "UPDATE administrators SET status = 'active', locked_until = NULL, failed_login_attempts = 0, updated_at = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(2, adminId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新最后登录时间
     */
    public boolean updateLastLoginTime(Long adminId, LocalDateTime lastLoginTime) {
        String sql = "UPDATE administrators SET last_login_time = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(lastLoginTime));
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(3, adminId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除管理员
     */
    public boolean deleteAdministrator(Long id) {
        String sql = "DELETE FROM administrators WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取所有管理员
     */
    public List<Administrator> getAllAdministrators() {
        List<Administrator> administrators = new ArrayList<>();
        String sql = "SELECT a.*, d.department_name FROM administrators a LEFT JOIN departments d ON a.department_id = d.id ORDER BY a.id";

        try (Connection conn = DBUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                administrators.add(mapResultSetToAdministrator(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return administrators;
    }

    /**
     * 获取部门的所有管理员
     */
    public List<Administrator> getAdministratorsByDepartmentId(Long departmentId) {
        List<Administrator> administrators = new ArrayList<>();
        String sql = "SELECT a.*, d.department_name FROM administrators a LEFT JOIN departments d ON a.department_id = d.id WHERE a.department_id = ? ORDER BY a.id";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, departmentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    administrators.add(mapResultSetToAdministrator(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return administrators;
    }

    /**
     * 获取指定类型的所有管理员
     */
    public List<Administrator> getAdministratorsByType(String adminType) {
        List<Administrator> administrators = new ArrayList<>();
        String sql = "SELECT a.*, d.department_name FROM administrators a LEFT JOIN departments d ON a.department_id = d.id WHERE a.admin_type = ? ORDER BY a.id";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, adminType);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    administrators.add(mapResultSetToAdministrator(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return administrators;    }

    /**
     * 根据真实姓名获取管理员列表
     */
    public List<Administrator> getAdministratorsByRealName(String realName) {
        List<Administrator> administrators = new ArrayList<>();
        String sql = "SELECT a.*, d.department_name FROM administrators a LEFT JOIN departments d ON a.department_id = d.id WHERE a.real_name = ? ORDER BY a.id";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, realName);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    administrators.add(mapResultSetToAdministrator(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return administrators;
    }

    /**
     * 更新管理员密码 (Service层调用的方法名)
     */
    public boolean updatePassword(Long adminId, String newPassword, String newSalt) {
        return updateAdministratorPassword(adminId, newPassword, newSalt);
    }    /**
     * 锁定账户 (Service层调用的方法名)
     */
    public boolean lockAccount(Long adminId) {
        LocalDateTime lockUntilTime = LocalDateTime.now().plusMinutes(30); // 锁定30分钟
        return lockAdministrator(adminId, lockUntilTime);
    }

    /**
     * 解锁账户 (Service层调用的方法名)
     */
    public boolean unlockAccount(Long adminId) {
        return unlockAdministrator(adminId);
    }

    /**
     * 重置登录失败次数 (Service层调用的方法名)
     */
    public boolean resetLoginFailCount(Long adminId) {
        return updateLoginFailCount(adminId, 0);
    }

    /**
     * 根据部门获取管理员 (Service层调用的方法名)
     */
    public List<Administrator> getAdministratorsByDepartment(Long departmentId) {
        return getAdministratorsByDepartmentId(departmentId);
    }

    /**
     * 将ResultSet映射为Administrator对象
     */    private Administrator mapResultSetToAdministrator(ResultSet rs) throws SQLException {
        Administrator admin = new Administrator();
        admin.setId(rs.getLong("id"));
        admin.setUsername(rs.getString("username"));
        admin.setPassword(rs.getString("password"));
        admin.setSalt(rs.getString("salt"));
        admin.setFullName(rs.getString("real_name"));
        admin.setPhoneEncrypted(rs.getString("phone_encrypted"));
        admin.setPhoneHash(rs.getString("phone_hash"));
        admin.setDepartmentId(rs.getLong("department_id"));
        admin.setAdminType(rs.getString("admin_type"));

        Timestamp lastLoginTime = rs.getTimestamp("last_login_time");
        if (lastLoginTime != null) {
            admin.setLastLoginTime(lastLoginTime.toLocalDateTime());
        }

        Timestamp lastPasswordChangeTime = rs.getTimestamp("password_last_changed");
        if (lastPasswordChangeTime != null) {
            admin.setLastPasswordChangeTime(lastPasswordChangeTime.toLocalDateTime());
        }

        admin.setLoginFailCount(rs.getInt("failed_login_attempts"));

        Timestamp lockUntilTime = rs.getTimestamp("locked_until");
        if (lockUntilTime != null) {
            admin.setLockUntilTime(lockUntilTime.toLocalDateTime());
        }

        admin.setStatus(rs.getString("status"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            admin.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            admin.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return admin;
    }
}
