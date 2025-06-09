package org.example.scms.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.scms.model.PublicReservation;
import org.example.scms.util.DBUtil;

/**
 * 社会公众预约数据访问对象
 */
public class PublicReservationDAO {

    /**
     * 根据ID获取社会公众预约信息
     */
    public PublicReservation getReservationById(Long id) {
        String sql = "SELECT * FROM public_reservations WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
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
     * 根据预约编号获取社会公众预约信息
     */
    public PublicReservation getReservationByReservationNo(String reservationNo) {
        String sql = "SELECT * FROM public_reservations WHERE reservation_no = ?";
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
     * 添加社会公众预约信息
     */
    public PublicReservation addReservation(PublicReservation reservation) {
        String sql = "INSERT INTO public_reservations (reservation_no, visitor_name, visitor_id_card_encrypted, " +
                "visitor_id_card_hash, visitor_phone_encrypted, visitor_phone_hash, organization, campus_id, " +
                "visit_date, visit_time_start, visit_time_end, visit_reason, accompanying_persons, vehicle_number, " +
                "status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();

            stmt.setString(1, reservation.getReservationNo());
            stmt.setString(2, reservation.getVisitorName());
            stmt.setString(3, reservation.getVisitorIdCardEncrypted());
            stmt.setString(4, reservation.getVisitorIdCardHash());
            stmt.setString(5, reservation.getVisitorPhoneEncrypted());
            stmt.setString(6, reservation.getVisitorPhoneHash());
            stmt.setString(7, reservation.getOrganization());
            stmt.setLong(8, reservation.getCampusId());
            stmt.setDate(9, Date.valueOf(reservation.getVisitDate()));
            stmt.setTime(10, Time.valueOf(reservation.getVisitTimeStart()));
            stmt.setTime(11, Time.valueOf(reservation.getVisitTimeEnd()));
            stmt.setString(12, reservation.getVisitReason());
            stmt.setInt(13, reservation.getAccompanyingPersons());
            stmt.setString(14, reservation.getVehicleNumber());
            stmt.setString(15, reservation.getStatus());
            stmt.setTimestamp(16, Timestamp.valueOf(now));
            stmt.setTimestamp(17, Timestamp.valueOf(now));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("添加预约失败，没有行被插入。");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reservation.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("添加预约失败，未获取到ID。");
                }
            }

            return reservation;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 更新社会公众预约信息
     */
    public boolean updateReservation(PublicReservation reservation) {
        String sql = "UPDATE public_reservations SET visitor_name = ?, organization = ?, campus_id = ?, " +
                "visit_date = ?, visit_time_start = ?, visit_time_end = ?, visit_reason = ?, " +
                "accompanying_persons = ?, vehicle_number = ?, status = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reservation.getVisitorName());
            stmt.setString(2, reservation.getOrganization());
            stmt.setLong(3, reservation.getCampusId());
            stmt.setDate(4, Date.valueOf(reservation.getVisitDate()));
            stmt.setTime(5, Time.valueOf(reservation.getVisitTimeStart()));
            stmt.setTime(6, Time.valueOf(reservation.getVisitTimeEnd()));
            stmt.setString(7, reservation.getVisitReason());
            stmt.setInt(8, reservation.getAccompanyingPersons());
            stmt.setString(9, reservation.getVehicleNumber());
            stmt.setString(10, reservation.getStatus());
            stmt.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(12, reservation.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 审批预约
     */
    public boolean approveReservation(Long reservationId, Long adminId, String status,
            String comment, String passCode, String qrCodeData) {
        String sql = "UPDATE public_reservations SET status = ?, approval_comment = ?, " +
                "approved_by = ?, approved_at = ?, pass_code = ?, qr_code_data = ?, updated_at = ? " +
                "WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            LocalDateTime now = LocalDateTime.now();            stmt.setString(1, status);
            stmt.setString(2, comment);
            // 处理可能为null的adminId（自动审批时）
            if (adminId != null) {
                stmt.setLong(3, adminId);
            } else {
                stmt.setNull(3, Types.BIGINT);
            }
            stmt.setTimestamp(4, Timestamp.valueOf(now));
            stmt.setString(5, passCode);
            stmt.setString(6, qrCodeData);
            stmt.setTimestamp(7, Timestamp.valueOf(now));
            stmt.setLong(8, reservationId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 取消预约
     */
    public boolean cancelReservation(Long reservationId) {
        String sql = "UPDATE public_reservations SET status = 'cancelled', updated_at = ? WHERE id = ?";

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
        String sql = "UPDATE public_reservations SET check_in_time = ?, updated_at = ? WHERE id = ?";

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
        String sql = "UPDATE public_reservations SET check_out_time = ?, status = 'completed', updated_at = ? WHERE id = ?";

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
    public PublicReservation getReservationByPassCode(String passCode) {
        String sql = "SELECT * FROM public_reservations WHERE pass_code = ? AND status = 'approved'";
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
     * 获取待审批的预约列表
     */
    public List<PublicReservation> getPendingReservations() {
        String sql = "SELECT * FROM public_reservations WHERE status = 'pending' ORDER BY created_at ASC";
        List<PublicReservation> reservations = new ArrayList<>();

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
     * 获取某个校区的预约列表
     */
    public List<PublicReservation> getReservationsByCampus(Long campusId, String status) {
        String sql = "SELECT * FROM public_reservations WHERE campus_id = ?";
        if (status != null && !status.isEmpty()) {
            sql += " AND status = ?";
        }
        sql += " ORDER BY visit_date DESC, visit_time_start DESC";

        List<PublicReservation> reservations = new ArrayList<>();

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
     * 根据日期范围查询预约
     */
    public List<PublicReservation> getReservationsByDateRange(LocalDate startDate, LocalDate endDate, String status) {
        String sql = "SELECT * FROM public_reservations WHERE visit_date BETWEEN ? AND ?";
        if (status != null && !status.isEmpty()) {
            sql += " AND status = ?";
        }
        sql += " ORDER BY visit_date ASC, visit_time_start ASC";

        List<PublicReservation> reservations = new ArrayList<>();

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
     * 根据身份证号哈希查询预约历史
     */
    public List<PublicReservation> getReservationsByIdCardHash(String idCardHash) {
        String sql = "SELECT * FROM public_reservations WHERE visitor_id_card_hash = ? " +
                "ORDER BY created_at DESC";

        List<PublicReservation> reservations = new ArrayList<>();

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
     * 统计某个时间段内各状态的预约数量
     */
    public Map<String, Integer> countReservationsByStatus(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT status, COUNT(*) as count FROM public_reservations " +
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
     * 统计每月预约数量
     */
    public Map<Integer, Integer> countReservationsByMonth(int year) {
        String sql = "SELECT MONTH(visit_date) as month, COUNT(*) as count FROM public_reservations " +
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
     * 统计每个校区的预约数量
     */
    public Map<Long, Integer> countReservationsByCampus(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT campus_id, COUNT(*) as count FROM public_reservations " +
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
     * 将结果集映射为预约对象
     */
    private PublicReservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        PublicReservation reservation = new PublicReservation();

        reservation.setId(rs.getLong("id"));
        reservation.setReservationNo(rs.getString("reservation_no"));
        reservation.setVisitorName(rs.getString("visitor_name"));
        reservation.setVisitorIdCardEncrypted(rs.getString("visitor_id_card_encrypted"));
        reservation.setVisitorIdCardHash(rs.getString("visitor_id_card_hash"));
        reservation.setVisitorPhoneEncrypted(rs.getString("visitor_phone_encrypted"));
        reservation.setVisitorPhoneHash(rs.getString("visitor_phone_hash"));
        reservation.setOrganization(rs.getString("organization"));
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
