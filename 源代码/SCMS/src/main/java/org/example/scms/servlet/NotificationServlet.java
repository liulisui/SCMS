package org.example.scms.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 通知管理Servlet - 无账户系统中已禁用
 * 保留基本功能用于前端兼容性
 */
@WebServlet({ "/notifications", "/notifications/*" })
public class NotificationServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        if (pathInfo != null && pathInfo.equals("/unread-count")) {
            // 返回空的未读通知数量（无账户系统）
            response.setContentType("application/json");
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("count", 0);
            result.put("message", "无账户系统中不支持通知功能");

            PrintWriter out = response.getWriter();
            out.print(gson.toJson(result));
            out.flush();
        } else {
            // 重定向到主页（无账户系统不支持通知页面）
            response.sendRedirect(request.getContextPath() + "/");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 无账户系统中所有通知操作都返回成功但无实际操作
        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "无账户系统中不支持通知功能");

        out.print(gson.toJson(result));
        out.flush();
    }
}
