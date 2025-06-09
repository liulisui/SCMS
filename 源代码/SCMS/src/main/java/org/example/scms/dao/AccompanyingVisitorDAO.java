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

import org.example.scms.model.AccompanyingVisitor;
import org.example.scms.util.DBUtil;

/**
 * 随行人员数据访问对象
 */
public class AccompanyingVisitorDAO {

    /**
     * 根据ID获取随行人员
     */
    public AccompanyingVisitor getVisitorById(Long id) {
        String sql = "SELECT * FROM accompanying_visitors WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVisitor(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加随行人员
     */
    public AccompanyingVisitor addVisitor(AccompanyingVisitor visitor) {
        String sql = "INSERT INTO accompanying_visitors (reservation_type, reservation_id, name, id_card_encrypted, " +
                "id_card_hash, phone_encrypted, phone_hash, organization, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();

            stmt.setString(1, visitor.getReservationType());
            stmt.setLong(2, visitor.getReservationId());
            stmt.setString(3, visitor.getName());
            stmt.setString(4, visitor.getIdCardEncrypted());
            stmt.setString(5, visitor.getIdCardHash());
            stmt.setString(6, visitor.getPhoneEncrypted());
            stmt.setString(7, visitor.getPhoneHash());
            stmt.setString(8, visitor.getOrganization());
            stmt.setTimestamp(9, Timestamp.valueOf(now));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("添加随行人员失败，没有行被插入。");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    visitor.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("添加随行人员失败，未获取到ID。");
                }
            }

            return visitor;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 批量添加随行人员
     */
    public boolean addVisitors(List<AccompanyingVisitor> visitors) {
        String sql = "INSERT INTO accompanying_visitors (reservation_type, reservation_id, name, id_card_encrypted, " +
                "id_card_hash, phone_encrypted, phone_hash, organization, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            LocalDateTime now = LocalDateTime.now();

            for (AccompanyingVisitor visitor : visitors) {
                stmt.setString(1, visitor.getReservationType());
                stmt.setLong(2, visitor.getReservationId());
                stmt.setString(3, visitor.getName());
                stmt.setString(4, visitor.getIdCardEncrypted());
                stmt.setString(5, visitor.getIdCardHash());
                stmt.setString(6, visitor.getPhoneEncrypted());
                stmt.setString(7, visitor.getPhoneHash());
                stmt.setString(8, visitor.getOrganization());
                stmt.setTimestamp(9, Timestamp.valueOf(now));

                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);

            // 检查是否所有添加都成功
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新随行人员信息
     */
    public boolean updateVisitor(AccompanyingVisitor visitor) {
        String sql = "UPDATE accompanying_visitors SET name = ?, organization = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, visitor.getName());
            stmt.setString(2, visitor.getOrganization());
            stmt.setLong(3, visitor.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除随行人员
     */
    public boolean deleteVisitor(Long visitorId) {
        String sql = "DELETE FROM accompanying_visitors WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, visitorId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除预约的所有随行人员
     */
    public boolean deleteVisitorsByReservation(String reservationType, Long reservationId) {
        String sql = "DELETE FROM accompanying_visitors WHERE reservation_type = ? AND reservation_id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reservationType);
            stmt.setLong(2, reservationId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows >= 0; // 可能没有随行人员，所以>=0都视为成功
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取预约的所有随行人员
     */
    public List<AccompanyingVisitor> getVisitorsByReservation(String reservationType, Long reservationId) {
        String sql = "SELECT * FROM accompanying_visitors WHERE reservation_type = ? AND reservation_id = ?";
        List<AccompanyingVisitor> visitors = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reservationType);
            stmt.setLong(2, reservationId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    visitors.add(mapResultSetToVisitor(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return visitors;
    }

    /**
     * 根据身份证号哈希查询访问记录
     */
    public List<AccompanyingVisitor> getVisitorsByIdCardHash(String idCardHash) {
        String sql = "SELECT * FROM accompanying_visitors WHERE id_card_hash = ? " +
                "ORDER BY created_at DESC";

        List<AccompanyingVisitor> visitors = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idCardHash);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    visitors.add(mapResultSetToVisitor(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return visitors;
    }

    /**
     * 将结果集映射为随行人员对象
     */
    private AccompanyingVisitor mapResultSetToVisitor(ResultSet rs) throws SQLException {
        AccompanyingVisitor visitor = new AccompanyingVisitor();

        visitor.setId(rs.getLong("id"));
        visitor.setReservationType(rs.getString("reservation_type"));
        visitor.setReservationId(rs.getLong("reservation_id"));
        visitor.setName(rs.getString("name"));
        visitor.setIdCardEncrypted(rs.getString("id_card_encrypted"));
        visitor.setIdCardHash(rs.getString("id_card_hash"));
        visitor.setPhoneEncrypted(rs.getString("phone_encrypted"));
        visitor.setPhoneHash(rs.getString("phone_hash"));
        visitor.setOrganization(rs.getString("organization"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            visitor.setCreatedAt(createdAt.toLocalDateTime());
        }

        return visitor;
    }
}
