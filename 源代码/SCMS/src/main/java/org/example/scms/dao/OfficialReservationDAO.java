package org.example.scms.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.scms.model.OfficialReservation;
import org.example.scms.util.DBUtil;

/**
 * 公务预约数据访问对象
 */
public class OfficialReservationDAO {

    /**
     * 根据ID获取公务预约信息
     */
    public OfficialReservation getReservationById(Long id) {
        String sql = "SELECT * FROM official_reservations WHERE id = ?";        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReservation(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("数据库操作错误: " + e.getMessage());
        }
        return null;
    }

    /**
     * 根据预约编号获取公务预约信息
     */
    public OfficialReservation getReservationByReservationNo(String reservationNo) {
        String sql = "SELECT * FROM official_reservations WHERE reservation_no = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reservationNo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReservation(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加公务预约信息
     */
    public OfficialReservation addReservation(OfficialReservation reservation) {
        String sql = "INSERT INTO official_reservations (reservation_no, visitor_name, visitor_id_card_encrypted, " +
                "visitor_id_card_hash, visitor_phone_encrypted, visitor_phone_hash, visitor_organization, " +
                "host_department_id, host_name, host_phone, campus_id, visit_date, visit_time_start, " +
                "visit_time_end, visit_reason, accompanying_persons, vehicle_number, status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            System.out.println("数据库连接成功，开始设置SQL参数...");
            LocalDateTime now = LocalDateTime.now();

            stmt.setString(1, reservation.getReservationNo());
            stmt.setString(2, reservation.getVisitorName());
            stmt.setString(3, reservation.getVisitorIdCardEncrypted());
            stmt.setString(4, reservation.getVisitorIdCardHash());
            stmt.setString(5, reservation.getVisitorPhoneEncrypted());
            stmt.setString(6, reservation.getVisitorPhoneHash());
            stmt.setString(7, reservation.getVisitorOrganization());
            stmt.setLong(8, reservation.getHostDepartmentId());
            stmt.setString(9, reservation.getHostName());
            stmt.setString(10, reservation.getHostPhone());
            stmt.setLong(11, reservation.getCampusId());
            stmt.setDate(12, Date.valueOf(reservation.getVisitDate()));
            stmt.setTime(13, Time.valueOf(reservation.getVisitTimeStart()));
            stmt.setTime(14, Time.valueOf(reservation.getVisitTimeEnd()));
            stmt.setString(15, reservation.getVisitReason());
            stmt.setInt(16, reservation.getAccompanyingPersons());
            stmt.setString(17, reservation.getVehicleNumber());
            stmt.setString(18, reservation.getStatus());
            stmt.setTimestamp(19, Timestamp.valueOf(now));
            stmt.setTimestamp(20, Timestamp.valueOf(now));

            System.out.println("SQL参数设置完成，执行插入操作...");
            System.out.println("预约编号: " + reservation.getReservationNo());
            System.out.println("访客姓名: " + reservation.getVisitorName());
            System.out.println("部门ID: " + reservation.getHostDepartmentId());
            System.out.println("校区ID: " + reservation.getCampusId());
            
            int affectedRows = stmt.executeUpdate();
            System.out.println("SQL执行完成，影响行数: " + affectedRows);

            if (affectedRows == 0) {
                throw new SQLException("添加公务预约失败，没有行被插入。");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long generatedId = generatedKeys.getLong(1);
                    reservation.setId(generatedId);
                    System.out.println("成功插入预约记录，生成的ID: " + generatedId);
                } else {
                    throw new SQLException("添加公务预约失败，未获取到ID。");
                }
            }

            return reservation;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 更新公务预约信息
     */
    public boolean updateReservation(OfficialReservation reservation) {
        String sql = "UPDATE official_reservations SET visitor_name = ?, visitor_organization = ?, " +
                "host_department_id = ?, host_name = ?, host_phone = ?, campus_id = ?, visit_date = ?, " +
                "visit_time_start = ?, visit_time_end = ?, visit_reason = ?, accompanying_persons = ?, " +
                "vehicle_number = ?, status = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reservation.getVisitorName());
            stmt.setString(2, reservation.getVisitorOrganization());
            stmt.setLong(3, reservation.getHostDepartmentId());
            stmt.setString(4, reservation.getHostName());
            stmt.setString(5, reservation.getHostPhone());
            stmt.setLong(6, reservation.getCampusId());
            stmt.setDate(7, Date.valueOf(reservation.getVisitDate()));
            stmt.setTime(8, Time.valueOf(reservation.getVisitTimeStart()));
            stmt.setTime(9, Time.valueOf(reservation.getVisitTimeEnd()));
            stmt.setString(10, reservation.getVisitReason());
            stmt.setInt(11, reservation.getAccompanyingPersons());
            stmt.setString(12, reservation.getVehicleNumber());
            stmt.setString(13, reservation.getStatus());
            stmt.setTimestamp(14, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(15, reservation.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 审批公务预约
     */    public boolean approveReservation(Long reservationId, Long adminId, String status,
            String comment, String passCode, String qrCodeData) {
        String sql = "UPDATE official_reservations SET status = ?, approval_comment = ?, " +
                "approved_by = ?, approved_at = ?, pass_code = ?, qr_code_data = ?, updated_at = ? " +
                "WHERE id = ?";

        System.out.println("=== DAO审批操作开始 ===");
        System.out.println("SQL: " + sql);
        System.out.println("参数: reservationId=" + reservationId + ", adminId=" + adminId + ", status=" + status);

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            System.out.println("数据库连接获取成功");
            
            LocalDateTime now = LocalDateTime.now();

            stmt.setString(1, status);
            stmt.setString(2, comment);
            stmt.setLong(3, adminId);
            stmt.setTimestamp(4, Timestamp.valueOf(now));
            stmt.setString(5, passCode);
            stmt.setString(6, qrCodeData);
            stmt.setTimestamp(7, Timestamp.valueOf(now));
            stmt.setLong(8, reservationId);

            System.out.println("执行SQL更新...");
            int affectedRows = stmt.executeUpdate();
            System.out.println("影响行数: " + affectedRows);
            
            // 强制提交事务
            if (!conn.getAutoCommit()) {
                conn.commit();
                System.out.println("手动提交事务");
            }
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("SQL异常: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("审批操作失败");
        return false;
    }

    /**
     * 取消公务预约
     */
    public boolean cancelReservation(Long reservationId) {
        String sql = "UPDATE official_reservations SET status = 'cancelled', updated_at = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(2, reservationId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 记录入校时间
     */
    public boolean recordCheckIn(Long reservationId, LocalDateTime checkInTime) {
        String sql = "UPDATE official_reservations SET check_in_time = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(checkInTime));
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(3, reservationId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 记录离校时间
     */
    public boolean recordCheckOut(Long reservationId, LocalDateTime checkOutTime) {
        String sql = "UPDATE official_reservations SET check_out_time = ?, status = 'completed', updated_at = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(checkOutTime));
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(3, reservationId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 根据通行证编码获取预约
     */
    public OfficialReservation getReservationByPassCode(String passCode) {
        String sql = "SELECT * FROM official_reservations WHERE pass_code = ? AND status = 'approved'";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, passCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReservation(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取待审批的公务预约列表
     */
    public List<OfficialReservation> getPendingReservations() {
        String sql = "SELECT * FROM official_reservations WHERE status = 'pending' ORDER BY created_at ASC";
        List<OfficialReservation> reservations = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reservations;
    }

    /**
     * 获取部门的待审批公务预约列表
     */
    public List<OfficialReservation> getPendingReservationsByDepartment(Long departmentId) {
        String sql = "SELECT * FROM official_reservations WHERE host_department_id = ? AND status = 'pending' " +
                "ORDER BY created_at ASC";
        List<OfficialReservation> reservations = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, departmentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reservations;
    }

    /**
     * 获取某个校区的公务预约列表
     */
    public List<OfficialReservation> getReservationsByCampus(Long campusId, String status) {
        String sql = "SELECT * FROM official_reservations WHERE campus_id = ?";
        if (status != null && !status.isEmpty()) {
            sql += " AND status = ?";
        }
        sql += " ORDER BY visit_date DESC, visit_time_start DESC";

        List<OfficialReservation> reservations = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, campusId);
            if (status != null && !status.isEmpty()) {
                stmt.setString(2, status);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reservations;
    }

    /**
     * 根据日期范围查询公务预约
     */
    public List<OfficialReservation> getReservationsByDateRange(LocalDate startDate, LocalDate endDate, String status) {
        String sql = "SELECT * FROM official_reservations WHERE visit_date BETWEEN ? AND ?";
        if (status != null && !status.isEmpty()) {
            sql += " AND status = ?";
        }
        sql += " ORDER BY visit_date ASC, visit_time_start ASC";

        List<OfficialReservation> reservations = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            if (status != null && !status.isEmpty()) {
                stmt.setString(3, status);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reservations;
    }

    /**
     * 根据接待部门查询公务预约
     */
    public List<OfficialReservation> getReservationsByHostDepartment(Long departmentId, String status) {
        String sql = "SELECT * FROM official_reservations WHERE host_department_id = ?";
        if (status != null && !status.isEmpty()) {
            sql += " AND status = ?";
        }
        sql += " ORDER BY visit_date DESC, visit_time_start DESC";

        List<OfficialReservation> reservations = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, departmentId);
            if (status != null && !status.isEmpty()) {
                stmt.setString(2, status);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reservations;
    }
    
    /**
     * 根据接待部门和日期范围查询公务预约（支持部门管理员权限控制）
     */
    public List<OfficialReservation> getReservationsByHostDepartmentAndDateRange(
            Long departmentId, LocalDate startDate, LocalDate endDate, String status) {
        StringBuilder sql = new StringBuilder("SELECT * FROM official_reservations WHERE host_department_id = ?");
        
        if (startDate != null && endDate != null) {
            sql.append(" AND visit_date BETWEEN ? AND ?");
        }
        
        if (status != null && !status.isEmpty()) {
            sql.append(" AND status = ?");
        }
        
        sql.append(" ORDER BY visit_date DESC, visit_time_start DESC");

        List<OfficialReservation> reservations = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            stmt.setLong(paramIndex++, departmentId);
            
            if (startDate != null && endDate != null) {
                stmt.setDate(paramIndex++, Date.valueOf(startDate));
                stmt.setDate(paramIndex++, Date.valueOf(endDate));
            }
            
            if (status != null && !status.isEmpty()) {
                stmt.setString(paramIndex++, status);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reservations;
    }

    /**
     * 根据身份证号哈希查询公务预约历史
     */
    public List<OfficialReservation> getReservationsByIdCardHash(String idCardHash) {
        String sql = "SELECT * FROM official_reservations WHERE visitor_id_card_hash = ? " +
                "ORDER BY created_at DESC";

        List<OfficialReservation> reservations = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idCardHash);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reservations;
    }

    /**
     * 统计某个时间段内各状态的公务预约数量
     */
    public Map<String, Integer> countReservationsByStatus(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT status, COUNT(*) as count FROM official_reservations " +
                "WHERE visit_date BETWEEN ? AND ? GROUP BY status";

        Map<String, Integer> counts = new HashMap<>();

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getString("status"), rs.getInt("count"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return counts;
    }

    /**
     * 统计每月公务预约数量
     */
    public Map<Integer, Integer> countReservationsByMonth(int year) {
        String sql = "SELECT MONTH(visit_date) as month, COUNT(*) as count FROM official_reservations " +
                "WHERE YEAR(visit_date) = ? GROUP BY MONTH(visit_date) ORDER BY MONTH(visit_date)";

        Map<Integer, Integer> counts = new HashMap<>();

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, year);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getInt("month"), rs.getInt("count"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return counts;
    }

    /**
     * 统计每个部门的公务预约数量
     */
    public Map<Long, Integer> countReservationsByDepartment(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT host_department_id, COUNT(*) as count FROM official_reservations " +
                "WHERE visit_date BETWEEN ? AND ? GROUP BY host_department_id";

        Map<Long, Integer> counts = new HashMap<>();

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getLong("host_department_id"), rs.getInt("count"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    /**
     * 统计每个校区的公务预约数量
     */
    public Map<Long, Integer> countReservationsByCampus(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT campus_id, COUNT(*) as count FROM official_reservations " +
                "WHERE visit_date BETWEEN ? AND ? GROUP BY campus_id";

        Map<Long, Integer> counts = new HashMap<>();

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    counts.put(rs.getLong("campus_id"), rs.getInt("count"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return counts;
    }

    /**
     * 将结果集映射为公务预约对象
     */
    private OfficialReservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        OfficialReservation reservation = new OfficialReservation();

        reservation.setId(rs.getLong("id"));
        reservation.setReservationNo(rs.getString("reservation_no"));
        reservation.setVisitorName(rs.getString("visitor_name"));
        reservation.setVisitorIdCardEncrypted(rs.getString("visitor_id_card_encrypted"));
        reservation.setVisitorIdCardHash(rs.getString("visitor_id_card_hash"));
        reservation.setVisitorPhoneEncrypted(rs.getString("visitor_phone_encrypted"));
        reservation.setVisitorPhoneHash(rs.getString("visitor_phone_hash"));
        reservation.setVisitorOrganization(rs.getString("visitor_organization"));
        reservation.setHostDepartmentId(rs.getLong("host_department_id"));
        reservation.setHostName(rs.getString("host_name"));
        reservation.setHostPhone(rs.getString("host_phone"));
        reservation.setCampusId(rs.getLong("campus_id"));

        Date visitDate = rs.getDate("visit_date");
        if (visitDate != null) {
            reservation.setVisitDate(visitDate.toLocalDate());
        }

        Time visitTimeStart = rs.getTime("visit_time_start");
        if (visitTimeStart != null) {
            reservation.setVisitTimeStart(visitTimeStart.toLocalTime());
        }

        Time visitTimeEnd = rs.getTime("visit_time_end");
        if (visitTimeEnd != null) {
            reservation.setVisitTimeEnd(visitTimeEnd.toLocalTime());
        }

        reservation.setVisitReason(rs.getString("visit_reason"));
        reservation.setAccompanyingPersons(rs.getInt("accompanying_persons"));
        reservation.setVehicleNumber(rs.getString("vehicle_number"));
        reservation.setStatus(rs.getString("status"));
        reservation.setApprovalComment(rs.getString("approval_comment"));

        Long approvedBy = rs.getLong("approved_by");
        if (!rs.wasNull()) {
            reservation.setApprovedBy(approvedBy);
        }

        Timestamp approvedAt = rs.getTimestamp("approved_at");
        if (approvedAt != null) {
            reservation.setApprovedAt(approvedAt.toLocalDateTime());
        }

        Timestamp checkInTime = rs.getTimestamp("check_in_time");
        if (checkInTime != null) {
            reservation.setCheckInTime(checkInTime.toLocalDateTime());
        }

        Timestamp checkOutTime = rs.getTimestamp("check_out_time");
        if (checkOutTime != null) {
            reservation.setCheckOutTime(checkOutTime.toLocalDateTime());
        }

        reservation.setPassCode(rs.getString("pass_code"));
        reservation.setQrCodeData(rs.getString("qr_code_data"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            reservation.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            reservation.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return reservation;
    }
}
