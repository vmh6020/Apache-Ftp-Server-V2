<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>FTP Manager - Đăng ký</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <style>
        body { background-color: #f8f9fa; }
    </style>
</head>
<body>

<div class="container">
    <div class="row justify-content-center">
        <div class="col-md-5 col-lg-4" style="margin-top: 10vh;">
            <div class="card shadow-sm border-0">
                <div class="card-body p-4">
                    <h2 class="card-title text-center mb-4">Register Account</h2>

                    <form method="post" action="${pageContext.request.contextPath}/register">
                        <div class="mb-3">
                            <label for="username" class="form-label">Username:</label>
                            <input type="text" class="form-control" id="username" name="username" autofocus required>
                        </div>
                        <div class="mb-3">
                            <label for="password" class="form-label">Password:</label>
                            <input type="password" class="form-control" id="password" name="password" required>
                        </div>
                        <div class="d-grid mt-4">
                            <input type="submit" value="Register" class="btn btn-primary">
                        </div>
                        <div class="text-center mt-3">
                            <a href="${pageContext.request.contextPath}/jsp/index.jsp">Back to Login</a>
                        </div>
                    </form>

                    <c:if test="${not empty requestScope['register-error']}">
                        <div class="alert alert-danger mt-3 mb-0" role="alert">
                            <c:out value="${requestScope['register-error']}"/>
                        </div>
                        <% request.removeAttribute("register-error"); %>
                    </c:if>

                </div>
            </div>
        </div>
    </div>
</div>

</body>
</html>