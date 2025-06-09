<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="org.example.scms.service.DebugQueryService" %>
<!DOCTYPE html>
<html>
<head>
    <title>直接数据库查询测试</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .result-box { background: #f5f5f5; border: 1px solid #ddd; padding: 15px; margin-top: 20px; white-space: pre-wrap; font-family: monospace; }
        .header { background: #4a6fdc; color: white; padding: 10px; margin-bottom: 20px; }
        input[type="text"] { padding: 8px; width: 300px; }
        button { padding: 8px 16px; background: #4a6fdc; color: white; border: none; cursor: pointer; }
    </style>
</head>
<body>
    <div class="header">
        <h1>ID卡哈希值验证工具</h1>
    </div>

    <form method="post">
        <input type="text" name="idCard" placeholder="请输入身份证号码" 
               value="<%=request.getParameter("idCard") != null ? request.getParameter("idCard") : ""%>" required>
        <button type="submit">验证</button>
    </form>

    <%
    String idCard = request.getParameter("idCard");
    if (idCard != null && !idCard.trim().isEmpty()) {
        List<String> directResults = DebugQueryService.debugDirectQuery(idCard);
        List<String> validationResults = DebugQueryService.validateHashAndQuery(idCard);
    %>
        <h2>查询结果</h2>
        <div class="result-box">
            <h3>直接SQL查询结果</h3>
            <% for (String result : directResults) { %>
                <%= result %><br>
            <% } %>
            
            <h3>哈希验证结果</h3>
            <% for (String result : validationResults) { %>
                <%= result %><br>
            <% } %>
        </div>
    <% } %>
</body>
</html>
