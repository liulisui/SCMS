package org.example.scms.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import org.example.scms.model.Administrator;
import org.example.scms.model.OfficialReservation;
import org.example.scms.service.OfficialReservationService;

import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 预约审批处理Servlet
 * 处理管理员对预约的审批操作
 */
@WebServlet("/admin/reservation-approval")
public class ReservationApprovalServlet extends HttpServlet {
      private OfficialReservationService officialService;
      @Override
    public void init() throws ServletException {
        this.officialService = new OfficialReservationService();
    }
      @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("=== ReservationApprovalServlet doPost 开始 ===");
        System.out.println("当前时间: " + new java.util.Date());
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        JsonObject jsonResponse = new JsonObject();
        
        try {
            // 验证管理员登录
            HttpSession session = request.getSession();
            System.out.println("会话ID: " + (session != null ? session.getId() : "null"));
            
            Administrator admin = (Administrator) session.getAttribute("admin");
            System.out.println("管理员登录状态检查:");
            System.out.println("  - 会话是否存在: " + (session != null));
            System.out.println("  - 管理员对象是否存在: " + (admin != null));
            if (admin != null) {
                System.out.println("  - 管理员ID: " + admin.getId());
                System.out.println("  - 管理员用户名: " + admin.getUsername());
                System.out.println("  - 管理员类型: " + admin.getAdminType());
                System.out.println("  - 管理员部门ID: " + admin.getDepartmentId());
            }
            
            if (admin == null) {
                System.out.println("管理员未登录，返回错误");
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "未登录或登录已过期");
                writeJsonResponse(response, jsonResponse);
                return;
            }            // 获取请求参数
            System.out.println("\n=== 请求参数获取阶段 ===");
            System.out.println("原始请求信息:");
            System.out.println("  - 请求方法: " + request.getMethod());
            System.out.println("  - 请求URL: " + request.getRequestURL());
            System.out.println("  - 查询字符串: " + request.getQueryString());
            System.out.println("  - Content-Type: " + request.getContentType());
            System.out.println("  - Content-Length: " + request.getContentLength());
            System.out.println("  - Character Encoding: " + request.getCharacterEncoding());
            
            // 打印所有请求头
            System.out.println("请求头信息:");
            java.util.Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                System.out.println("  - " + headerName + ": " + request.getHeader(headerName));
            }
            
            // 打印所有参数
            System.out.println("所有参数映射:");
            java.util.Map<String, String[]> paramMap = request.getParameterMap();
            for (java.util.Map.Entry<String, String[]> entry : paramMap.entrySet()) {
                String key = entry.getKey();
                String[] values = entry.getValue();
                System.out.println("  - 参数名: '" + key + "', 值: " + java.util.Arrays.toString(values));
            }
            
            String reservationIdStr = request.getParameter("reservationId");
            String action = request.getParameter("action");
            String comment = request.getParameter("comment");
            String reservationType = request.getParameter("reservationType");
            
            System.out.println("\n获取到的具体参数值:");
            System.out.println("  - reservationIdStr: '" + reservationIdStr + "' (长度: " + (reservationIdStr != null ? reservationIdStr.length() : "null") + ")");
            System.out.println("  - action: '" + action + "' (长度: " + (action != null ? action.length() : "null") + ")");
            System.out.println("  - comment: '" + comment + "' (长度: " + (comment != null ? comment.length() : "null") + ")");
            System.out.println("  - reservationType: '" + reservationType + "' (长度: " + (reservationType != null ? reservationType.length() : "null") + ")");
            
            // 检查参数是否为空字符串
            System.out.println("参数空值检查:");
            System.out.println("  - reservationIdStr isEmpty: " + (reservationIdStr != null && reservationIdStr.isEmpty()));
            System.out.println("  - action isEmpty: " + (action != null && action.isEmpty()));
            System.out.println("  - reservationType isEmpty: " + (reservationType != null && reservationType.isEmpty()));
            
            // 参数验证
            System.out.println("\n=== 参数验证阶段 ===");
            boolean hasReservationId = reservationIdStr != null && !reservationIdStr.trim().isEmpty();
            boolean hasAction = action != null && !action.trim().isEmpty();
            boolean hasReservationType = reservationType != null && !reservationType.trim().isEmpty();
            
            System.out.println("参数验证结果:");
            System.out.println("  - reservationId 有效: " + hasReservationId);
            System.out.println("  - action 有效: " + hasAction);
            System.out.println("  - reservationType 有效: " + hasReservationType);
            
            if (!hasReservationId || !hasAction || !hasReservationType) {
                System.out.println("参数验证失败，缺少必要参数:");
                System.out.println("  - 缺少 reservationId: " + !hasReservationId);
                System.out.println("  - 缺少 action: " + !hasAction);
                System.out.println("  - 缺少 reservationType: " + !hasReservationType);
                
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "缺少必要参数");
                writeJsonResponse(response, jsonResponse);
                return;
            }            
            System.out.println("\n=== 预约ID解析阶段 ===");
            Long reservationId;
            try {
                System.out.println("尝试解析预约ID: '" + reservationIdStr + "'");
                reservationId = Long.parseLong(reservationIdStr.trim());
                System.out.println("预约ID解析成功: " + reservationId);
            } catch (NumberFormatException e) {
                System.out.println("预约ID解析失败: " + e.getMessage());
                System.out.println("原始字符串: '" + reservationIdStr + "'");
                System.out.println("去空格后: '" + reservationIdStr.trim() + "'");
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "预约ID格式错误");
                writeJsonResponse(response, jsonResponse);
                return;
            }
            
            // 确定审批状态
            System.out.println("\n=== 操作类型验证阶段 ===");
            System.out.println("操作类型: '" + action + "'");
            String status;
            if ("approve".equals(action.trim())) {
                status = "approved";
                System.out.println("操作类型确认: 审批通过");
            } else if ("reject".equals(action.trim())) {
                status = "rejected";
                System.out.println("操作类型确认: 审批拒绝");
            } else {
                System.out.println("无效的操作类型: '" + action + "'");
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "无效的操作类型");
                writeJsonResponse(response, jsonResponse);
                return;
            }
            
            // 获取客户端信息
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
              // 执行审批操作
            System.out.println("\n=== 审批操作执行阶段 ===");
            boolean success = false;
            String errorMessage = "";
            
            try {
                // 检查权限和执行审批
                System.out.println("开始权限检查...");
                System.out.println("权限检查参数:");
                System.out.println("  - 管理员ID: " + admin.getId());
                System.out.println("  - 管理员类型: " + admin.getAdminType());
                System.out.println("  - 管理员部门ID: " + admin.getDepartmentId());
                System.out.println("  - 预约ID: " + reservationId);
                System.out.println("  - 预约类型: " + reservationType);
                
                boolean hasPermission = hasApprovalPermission(admin, reservationId, reservationType);
                System.out.println("权限检查结果: " + hasPermission);
                
                if (hasPermission) {
                    System.out.println("权限检查通过，开始执行审批操作...");
                    System.out.println("审批参数:");
                    System.out.println("  - 预约ID: " + reservationId);
                    System.out.println("  - 管理员ID: " + admin.getId());
                    System.out.println("  - 审批状态: " + status);
                    System.out.println("  - 审批意见: '" + (comment != null ? comment : "") + "'");
                    System.out.println("  - 客户端IP: " + ipAddress);
                    System.out.println("  - User-Agent: " + userAgent);
                    
                    // 审批公务预约（默认处理公务预约，因为公众预约通常自动审批）
                    success = officialService.approveReservation(
                        reservationId, 
                        admin.getId(), 
                        status, 
                        comment != null ? comment : "", 
                        ipAddress, 
                        userAgent
                    );
                    
                    System.out.println("审批服务调用结果: " + success);
                    
                    if (!success) {
                        errorMessage = "审批操作失败，请检查预约状态";
                        System.out.println("审批失败原因: " + errorMessage);
                    } else {
                        System.out.println("审批操作成功完成");
                    }
                } else {
                    System.out.println("权限检查失败，拒绝审批操作");
                    errorMessage = "您没有权限审批此预约";
                }
            } catch (Exception e) {
                System.out.println("审批过程发生异常:");
                System.out.println("  - 异常类型: " + e.getClass().getName());
                System.out.println("  - 异常消息: " + e.getMessage());
                e.printStackTrace();
                errorMessage = "审批过程中发生错误: " + e.getMessage();
            }
              // 返回结果
            System.out.println("\n=== 返回结果阶段 ===");
            if (success) {
                System.out.println("最终结果: 审批成功");
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "审批成功");
            } else {
                System.out.println("最终结果: 审批失败");
                System.out.println("失败原因: " + errorMessage);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", errorMessage);
            }
            
            System.out.println("准备发送的JSON响应: " + jsonResponse.toString());
            
        } catch (Exception e) {
            System.out.println("\n=== 顶层异常处理 ===");
            System.out.println("发生顶层异常:");
            System.out.println("  - 异常类型: " + e.getClass().getName());
            System.out.println("  - 异常消息: " + e.getMessage());
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "系统错误: " + e.getMessage());
        }
        
        System.out.println("\n=== 发送响应 ===");
        System.out.println("最终JSON响应: " + jsonResponse.toString());
        writeJsonResponse(response, jsonResponse);
        System.out.println("=== ReservationApprovalServlet doPost 结束 ===\n");
    }    /**
     * 检查管理员是否有审批权限
     */    private boolean hasApprovalPermission(Administrator admin, Long reservationId, String reservationType) {
        System.out.println("\n=== 权限检查详细过程 ===");
        
        String adminType = admin.getAdminType();
        System.out.println("管理员权限检查:");
        System.out.println("  - 管理员ID: " + admin.getId());
        System.out.println("  - 管理员用户名: " + admin.getUsername());
        System.out.println("  - 管理员类型: '" + adminType + "'");
        System.out.println("  - 管理员部门ID: " + admin.getDepartmentId());
        System.out.println("  - 预约ID: " + reservationId);
        System.out.println("  - 预约类型: '" + reservationType + "'");
        
        // 系统管理员有所有权限
        if ("system_admin".equals(adminType)) {
            System.out.println("权限判断: 系统管理员，拥有所有权限 -> 允许");
            return true;
        }
        
        // 学校管理员可以审批所有预约
        if ("school_admin".equals(adminType)) {
            System.out.println("权限判断: 学校管理员，可以审批所有预约 -> 允许");
            return true;
        }
        
        // 部门管理员只能审批自己部门的公务预约
        if ("department_admin".equals(adminType)) {
            System.out.println("权限判断: 部门管理员，检查详细权限...");
            
            if (!"official".equals(reservationType)) {
                System.out.println("  - 预约类型检查: 不是公务预约 ('" + reservationType + "') -> 拒绝");
                return false;  // 部门管理员只能审批公务预约
            }
            System.out.println("  - 预约类型检查: 是公务预约 -> 通过");
            
            // 检查预约是否属于该管理员的部门
            try {
                System.out.println("  - 开始查询预约详情...");
                OfficialReservation reservation = officialService.getReservationById(reservationId);
                
                if (reservation == null) {
                    System.out.println("  - 预约查询结果: 预约不存在 -> 拒绝");
                    return false;  // 预约不存在
                }
                
                System.out.println("  - 预约查询成功:");
                System.out.println("    * 预约编号: " + reservation.getReservationNo());
                System.out.println("    * 访客姓名: " + reservation.getVisitorName());
                System.out.println("    * 接待部门ID: " + reservation.getHostDepartmentId());
                System.out.println("    * 管理员部门ID: " + admin.getDepartmentId());
                
                // 检查预约的接待部门是否与管理员所在部门一致
                boolean departmentMatch = reservation.getHostDepartmentId().equals(admin.getDepartmentId());
                System.out.println("  - 部门匹配检查: " + departmentMatch);
                
                if (departmentMatch) {
                    System.out.println("  - 最终判断: 部门匹配，允许审批 -> 允许");
                } else {
                    System.out.println("  - 最终判断: 部门不匹配，拒绝审批 -> 拒绝");
                }
                
                return departmentMatch;
            } catch (Exception e) {
                System.out.println("  - 预约查询异常:");
                System.out.println("    * 异常类型: " + e.getClass().getName());
                System.out.println("    * 异常消息: " + e.getMessage());
                e.printStackTrace();
                System.out.println("  - 因异常拒绝审批 -> 拒绝");
                return false;
            }
        }
        
        System.out.println("权限判断: 未知管理员类型 ('" + adminType + "') -> 拒绝");
        return false;
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * 写JSON响应
     */
    private void writeJsonResponse(HttpServletResponse response, JsonObject jsonObject) throws IOException {
        PrintWriter out = response.getWriter();
        out.print(jsonObject.toString());
        out.flush();
    }
}
