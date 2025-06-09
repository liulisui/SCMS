package org.example.scms.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.scms.util.DBUtil;

/**
 * 通知服务类
 * 处理系统通知的创建、查询和管理
 */
public class NotificationService {

    /**
     * 创建通知
     */
    public boolean createNotification(Long userId, String title, String content, String type) {
        String sql = "INSERT INTO notifications (user_id, title, content, type) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setString(2, title);
            stmt.setString(3, content);
            stmt.setString(4, type);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取用户的所有通知
     */
    public List<Map<String, Object>> getUserNotifications(Long userId) {
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";
        List<Map<String, Object>> notifications = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> notification = new HashMap<>();
                notification.put("id", rs.getLong("id"));
                notification.put("title", rs.getString("title"));
                notification.put("content", rs.getString("content"));
                notification.put("type", rs.getString("type"));
                notification.put("isRead", rs.getBoolean("is_read"));
                notification.put("createdAt", rs.getTimestamp("created_at"));
                notifications.add(notification);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return notifications;
    }

    /**
     * 获取用户未读通知数量
     */
    public int getUnreadCount(Long userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * 标记通知为已读
     */
    public boolean markAsRead(Long notificationId, Long userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ? AND user_id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, notificationId);
            stmt.setLong(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 标记所有通知为已读
     */
    public boolean markAllAsRead(Long userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ? AND is_read = FALSE";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);

            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除通知
     */
    public boolean deleteNotification(Long notificationId, Long userId) {
        String sql = "DELETE FROM notifications WHERE id = ? AND user_id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, notificationId);
            stmt.setLong(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 发送预约审批通过通知
     */
    public void sendApprovalNotification(Long userId, String passCode) {
        createNotification(userId, "预约审批通过",
                "您的出入校预约申请已通过审批，通行证编号：" + passCode + "，请及时查看通行证信息。",
                "success");
    }

    /**
     * 发送预约审批拒绝通知
     */
    public void sendRejectionNotification(Long userId, String reason) {
        createNotification(userId, "预约审批未通过",
                "很抱歉，您的出入校预约申请未通过审批。原因：" + reason,
                "error");
    }

    /**
     * 发送预约提醒通知
     */
    public void sendReminderNotification(Long userId, String departureTime) {
        createNotification(userId, "预约提醒",
                "您有一个预约即将生效，预计离校时间：" + departureTime + "，请提前准备。",
                "info");
    }

    /**
     * 发送系统维护通知
     */
    public void sendMaintenanceNotification(Long userId) {
        createNotification(userId, "系统维护通知",
                "系统将于今晚23:00-01:00进行维护，期间可能影响使用，请提前安排。",
                "warning");
    }
}
