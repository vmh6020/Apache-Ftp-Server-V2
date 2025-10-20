<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Register Page</title>
</head>
<body>
<form action="register" method="post"> <%-- Đổi action --%>
    <label for="username">Username: </label>
    <input type="text" id="username" name="username" required autofocus><br><br>
    <label for="password">Password: </label>
    <input type="password" id="password" name="password" required><br><br>
    <input type="submit" value="Register">
</form>

<% if (request.getAttribute("register-error") != null) { %>
<p style="color:red"><%= request.getAttribute("register-error") %></p>
<% } %>
</body>
</html>