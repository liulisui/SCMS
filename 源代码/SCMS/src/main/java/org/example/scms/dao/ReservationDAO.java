package org.example.scms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.example.scms.model.Reservation;
import org.example.scms.util.DBUtil;

/**
 * 预约数据访问对象
 */
public class ReservationDAO {

    /**
     * 添加预约
     */
    public boolean addReservation(Reservation reservation) {
        String sql = "INSERT INTO reservations (user_id, user_name, phone, id_card, reservation_type, purpose, destination, visit_time, duration, status, create_time, update_time, campus, organization, transport_mode, license_plate, companions, official_department, official_contact_person, official_reason, real_id_card, real_name, real_phone) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, reservation.getUserId());
            pstmt.setString(2, reservation.getUserName());
            pstmt.setString(3, reservation.getPhone());
            pstmt.setString(4, reservation.getIdCard());
            pstmt.setString(5, reservation.getReservationType());
            pstmt.setString(6, reservation.getPurpose());
            pstmt.setString(7, reservation.getDestination());
            pstmt.setTimestamp(8, Timestamp.valueOf(reservation.getVisitTime()));
            pstmt.setString(9, reservation.getDuration());
            pstmt.setString(10, reservation.getStatus());
            pstmt.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setTimestamp(12, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(13, reservation.getCampus());
            pstmt.setString(14, reservation.getOrganization());
            pstmt.setString(15, reservation.getTransportMode());
            pstmt.setString(16, reservation.getLicensePlate());
            pstmt.setString(17, reservation.getCompanions());
            pstmt.setString(18, reservation.getOfficialDepartment());
            pstmt.setString(19, reservation.getOfficialContactPerson());
            pstmt.setString(20, reservation.getOfficialReason());
            pstmt.setString(21, reservation.getRealIdCard());
            pstmt.setString(22, reservation.getRealName());
            pstmt.setString(23, reservation.getRealPhone());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据用户ID查询预约
     */
    public List<Reservation> findByUserId(Long userId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE user_id = ? ORDER BY create_time DESC";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                reservations.add(resultSetToReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }

    /**
     * 根据用户名查询预约
     */
    public List<Reservation> findByUserName(String userName) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE user_name = ? ORDER BY create_time DESC";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                reservations.add(resultSetToReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }

    /**
     * 根据ID查询预约
     */
    public Reservation findById(int id) {
        String sql = "SELECT * FROM reservations WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return resultSetToReservation(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取所有预约
     */
    public List<Reservation> findAll() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservations ORDER BY create_time DESC";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                reservations.add(resultSetToReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }

    /**
     * 根据状态查询预约
     */
    public List<Reservation> findByStatus(String status) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE status = ? ORDER BY create_time DESC";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                reservations.add(resultSetToReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }

    /**
     * 更新预约状态
     */
    public boolean updateStatus(int id, String status, String reviewReason, int reviewerId) {
        String sql = "UPDATE reservations SET status = ?, review_reason = ?, reviewer_id = ?, review_time = ?, update_time = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setString(2, reviewReason);
            pstmt.setInt(3, reviewerId);
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(6, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 统计预约数量
     */
    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM reservations WHERE status = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 根据姓名、身份证号和手机号查询历史预约记录
     */
    public List<Reservation> findByUserInfo(String realName, String realIdCard, String realPhone) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE real_name = ? AND real_id_card = ? AND real_phone = ? ORDER BY create_time DESC";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, realName);
            pstmt.setString(2, realIdCard);
            pstmt.setString(3, realPhone);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                reservations.add(resultSetToReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }

    /**
     * 根据真实身份证号查询预约记录
     */
    public List<Reservation> findByRealIdCard(String realIdCard) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE real_id_card = ? ORDER BY create_time DESC";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, realIdCard);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                reservations.add(resultSetToReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }

    /**
     * 将ResultSet转换为Reservation对象
     */
    private Reservation resultSetToReservation(ResultSet rs) throws SQLException {
        Reservation reservation = new Reservation();
        reservation.setId(rs.getInt("id"));
        reservation.setUserId(rs.getLong("user_id"));
        reservation.setUserName(rs.getString("user_name"));
        reservation.setPhone(rs.getString("phone"));
        reservation.setIdCard(rs.getString("id_card"));
        reservation.setReservationType(rs.getString("reservation_type"));
        reservation.setPurpose(rs.getString("purpose"));
        reservation.setDestination(rs.getString("destination"));
        reservation.setVisitTime(rs.getTimestamp("visit_time").toLocalDateTime());
        reservation.setDuration(rs.getString("duration"));
        reservation.setStatus(rs.getString("status"));
        reservation.setReviewReason(rs.getString("review_reason"));
        reservation.setReviewerId(rs.getInt("reviewer_id"));
        reservation.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
        reservation.setUpdateTime(rs.getTimestamp("update_time").toLocalDateTime());

        // 处理新增字段
        reservation.setCampus(rs.getString("campus"));
        reservation.setOrganization(rs.getString("organization"));
        reservation.setTransportMode(rs.getString("transport_mode"));
        reservation.setLicensePlate(rs.getString("license_plate"));
        reservation.setCompanions(rs.getString("companions"));
        reservation.setOfficialDepartment(rs.getString("official_department"));
        reservation.setOfficialContactPerson(rs.getString("official_contact_person"));
        reservation.setOfficialReason(rs.getString("official_reason"));
        reservation.setQrCodeData(rs.getString("qr_code_data"));
        reservation.setRealIdCard(rs.getString("real_id_card"));
        reservation.setRealName(rs.getString("real_name"));
        reservation.setRealPhone(rs.getString("real_phone"));

        Timestamp reviewTime = rs.getTimestamp("review_time");
        if (reviewTime != null) {
            reservation.setReviewTime(reviewTime.toLocalDateTime());
        }

        return reservation;
    }
}
