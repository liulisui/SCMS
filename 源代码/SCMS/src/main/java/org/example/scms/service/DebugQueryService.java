package org.example.scms.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.example.scms.util.DBUtil;
import org.example.scms.util.SM3HashUtil;

/**
 * 调试用服务类 - 专门用于直接测试查询
 */
public class DebugQueryService {

    /**
     * 直接使用原始SQL查询身份证哈希相关的预约记录
     */
    public static List<String> debugDirectQuery(String idCard) {
        List<String> results = new ArrayList<>();
        
        try {
            // 1. 计算哈希
            String idCardHash = SM3HashUtil.hash(idCard);
            results.add("[哈希] " + idCard + " -> " + idCardHash);
            
            // 2. 查询public_reservations表
            String publicSql = "SELECT * FROM public_reservations WHERE visitor_id_card_hash = ?";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(publicSql)) {
                
                stmt.setString(1, idCardHash);
                try (ResultSet rs = stmt.executeQuery()) {
                    int count = 0;
                    while (rs.next()) {
                        count++;
                        results.add("[公众预约] ID=" + rs.getLong("id") + 
                                   ", 姓名=" + rs.getString("visitor_name") +
                                   ", 状态=" + rs.getString("status"));
                    }
                    results.add("[公众预约] 共找到 " + count + " 条记录");
                }
            }
            
            // 3. 查询official_reservations表
            String officialSql = "SELECT * FROM official_reservations WHERE visitor_id_card_hash = ?";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(officialSql)) {
                
                stmt.setString(1, idCardHash);
                try (ResultSet rs = stmt.executeQuery()) {
                    int count = 0;
                    while (rs.next()) {
                        count++;
                        results.add("[公务预约] ID=" + rs.getLong("id") + 
                                   ", 姓名=" + rs.getString("visitor_name") +
                                   ", 状态=" + rs.getString("status"));
                    }
                    results.add("[公务预约] 共找到 " + count + " 条记录");
                }
            }
            
        } catch (SQLException e) {
            results.add("[错误] SQL异常: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            results.add("[错误] 其他异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        return results;
    }
    
    /**
     * 验证哈希值路径并与数据库进行比较
     */
    public static List<String> validateHashAndQuery(String idCard) {
        List<String> results = new ArrayList<>();
        
        try {
            // 1. 生成哈希并获取结果
            String idCardHash = SM3HashUtil.hash(idCard);
            results.add("身份证号: " + idCard);
            results.add("计算哈希: " + idCardHash);
            
            // 2. 检查数据库中所有不同的哈希值（取样）
            String sampleSql = "SELECT DISTINCT visitor_id_card_hash FROM public_reservations LIMIT 10";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sampleSql);
                 ResultSet rs = stmt.executeQuery()) {
                
                results.add("\n数据库中的哈希值样本:");
                int i = 0;
                while (rs.next() && i < 5) {
                    String dbHash = rs.getString("visitor_id_card_hash");
                    results.add("数据库哈希 #" + (++i) + ": " + dbHash);
                    
                    // 测试是否能用这个哈希值找到记录
                    results.add("  匹配我们的查询?: " + (dbHash.equals(idCardHash) ? "是" : "否"));
                }
            }
            
            // 3. 检查输入完全相同的事务记录
            String countExactSql = "SELECT COUNT(*) FROM public_reservations WHERE visitor_id_card_hash = ?";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(countExactSql)) {
                
                stmt.setString(1, idCardHash);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        results.add("\n使用精确哈希查询:");
                        results.add("哈希值: " + idCardHash);
                        results.add("匹配记录数: " + count);
                    }
                }
            }
            
            // 4. 直接执行SQL以查找任何包含类似哈希的记录
            String likeSql = "SELECT id, visitor_name, visitor_id_card_hash FROM public_reservations WHERE visitor_id_card_hash LIKE ? LIMIT 5";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(likeSql)) {
                
                stmt.setString(1, "%" + idCardHash.substring(0, 10) + "%");
                try (ResultSet rs = stmt.executeQuery()) {
                    results.add("\n模糊匹配查询结果:");
                    int count = 0;
                    while (rs.next()) {
                        count++;
                        long id = rs.getLong("id");
                        String name = rs.getString("visitor_name");
                        String hash = rs.getString("visitor_id_card_hash");
                        results.add("记录 #" + count + ": ID=" + id + ", 姓名=" + name + ", 哈希=" + hash);
                    }
                    
                    if (count == 0) {
                        results.add("未找到部分匹配记录");
                    }
                }
            }
            
        } catch (Exception e) {
            results.add("验证过程出错: " + e.getMessage());
            e.printStackTrace();
        }
        
        return results;
    }
}
