<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<body>
<h2>FTP Login</h2>
<form method="post" action="${pageContext.request.contextPath}/login">
    <label for="username"> Username: </label>
    <input type="text" id="username" name="username" autocomplete="kevin" autofocus required/><br/>
    <br/>
    <label for="password"> Password: </label>
    <input type="password" id="password" name="password" autocomplete="123" required/><br/><br/>
    <input type="submit" value="Login" /><br/><br/>
    <a href="${pageContext.request.contextPath}/jsp/register.jsp">Register new account</a>
</form>

<% if (request.getAttribute("login-error") != null) { %>
<p style="color:red"><%= request.getAttribute("login-error") %></p>
<% session.removeAttribute("login-error"); %>
<% } %>
<% if (session.getAttribute("register-success") != null) { %>
<p style="color:yellowgreen"><%= session.getAttribute("register-success") %></p>
<% session.removeAttribute("register-success"); %>
<% } %>
</body>
</html>