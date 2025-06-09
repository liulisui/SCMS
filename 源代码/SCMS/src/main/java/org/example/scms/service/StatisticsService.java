package org.example.scms.service;

import org.example.scms.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计报表服务类
 * 提供各种数据统计和报表功能
 */
public class StatisticsService {

    /**
     * 获取总体统计数据
     */
    public Map<String, Object> getOverallStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try (Connection conn = DBUtil.getConnection()) {
            // 总用户数
            stats.put("totalUsers", getTotalUsers(conn));

            // 总预约数
            stats.put("totalReservations", getTotalReservations(conn));

            // 待审批预约数
            stats.put("pendingReservations", getPendingReservations(conn));

            // 今日新增预约数
            stats.put("todayReservations", getTodayReservations(conn));

            // 本月预约数
            stats.put("monthlyReservations", getMonthlyReservations(conn));

            // 预约状态分布
            stats.put("statusDistribution", getStatusDistribution(conn));

            // 用户角色分布
            stats.put("roleDistribution", getRoleDistribution(conn));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * 获取每日预约统计（最近30天）
     */
    public List<Map<String, Object>> getDailyReservationStats() {
        String sql = "SELECT DATE(created_at) as date, COUNT(*) as count " +
                "FROM reservations " +
                "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
                "GROUP BY DATE(created_at) " +
                "ORDER BY date";

        List<Map<String, Object>> stats = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("date", rs.getDate("date"));
                stat.put("count", rs.getInt("count"));
                stats.add(stat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * 获取月度预约统计
     */
    public List<Map<String, Object>> getMonthlyReservationStats() {
        String sql = "SELECT YEAR(created_at) as year, MONTH(created_at) as month, COUNT(*) as count " +
                "FROM reservations " +
                "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH) " +
                "GROUP BY YEAR(created_at), MONTH(created_at) " +
                "ORDER BY year, month";

        List<Map<String, Object>> stats = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("year", rs.getInt("year"));
                stat.put("month", rs.getInt("month"));
                stat.put("count", rs.getInt("count"));
                stats.add(stat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * 获取用户预约排行榜
     */
    public List<Map<String, Object>> getUserReservationRanking(int limit) {
        String sql = "SELECT u.username, u.full_name, COUNT(r.id) as reservation_count " +
                "FROM users u " +
                "LEFT JOIN reservations r ON u.id = r.user_id " +
                "WHERE u.role != 'admin' " +
                "GROUP BY u.id, u.username, u.full_name " +
                "ORDER BY reservation_count DESC " +
                "LIMIT ?";

        List<Map<String, Object>> rankings = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            int rank = 1;
            while (rs.next()) {
                Map<String, Object> ranking = new HashMap<>();
                ranking.put("rank", rank++);
                ranking.put("username", rs.getString("username"));
                ranking.put("fullName", rs.getString("full_name"));
                ranking.put("reservationCount", rs.getInt("reservation_count"));
                rankings.add(ranking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rankings;
    }

    /**
     * 获取热门目的地统计
     */
    public List<Map<String, Object>> getPopularDestinations(int limit) {
        String sql = "SELECT destination, COUNT(*) as count " +
                "FROM reservations " +
                "WHERE destination IS NOT NULL AND destination != '' " +
                "GROUP BY destination " +
                "ORDER BY count DESC " +
                "LIMIT ?";

        List<Map<String, Object>> destinations = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> destination = new HashMap<>();
                destination.put("destination", rs.getString("destination"));
                destination.put("count", rs.getInt("count"));
                destinations.add(destination);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return destinations;
    }

    // 私有辅助方法

    private int getTotalUsers(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role != 'admin'";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int getTotalReservations(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int getPendingReservations(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations WHERE status = 'pending'";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int getTodayReservations(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations WHERE DATE(created_at) = CURDATE()";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int getMonthlyReservations(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations WHERE YEAR(created_at) = YEAR(CURDATE()) AND MONTH(created_at) = MONTH(CURDATE())";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private List<Map<String, Object>> getStatusDistribution(Connection conn) throws SQLException {
        String sql = "SELECT status, COUNT(*) as count FROM reservations GROUP BY status";
        List<Map<String, Object>> distribution = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("status", rs.getString("status"));
                item.put("count", rs.getInt("count"));
                distribution.add(item);
            }
        }

        return distribution;
    }

    private List<Map<String, Object>> getRoleDistribution(Connection conn) throws SQLException {
        String sql = "SELECT role, COUNT(*) as count FROM users GROUP BY role";
        List<Map<String, Object>> distribution = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("role", rs.getString("role"));
                item.put("count", rs.getInt("count"));
                distribution.add(item);
            }
        }

        return distribution;
    }
}
