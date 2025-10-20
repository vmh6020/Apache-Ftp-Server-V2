<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<head>
    <title>Edit File</title>
</head>
<body>
<h2>Đang sửa file: <c:out value="${filePath}"/></h2>

<p><a href="files?path=<c:out value='${parentPath}'/>">&laquo; Quay lại (Không lưu)</a></p>

<form action="edit-content" method="POST">
    <%-- Các trường ẩn để servlet biết đang sửa file nào --%>
    <input type="hidden" name="filePath" value="<c:out value='${filePath}'/>">
    <input type="hidden" name="parentPath" value="<c:out value='${parentPath}'/>">

    <p><strong>Nội dung file:</strong></p>

    <%-- Textarea hiển thị nội dung file.
         Dùng <c:out> để hiển thị nội dung một cách an toàn (tránh lỗi HTML) --%>
    <textarea name="fileContent" style="width: 80%; height: 400px; font-family: monospace;">
<c:out value="${fileContent}"/>
</textarea>

    <br/><br/>
    <input type="submit" value="Lưu thay đổi">
</form>
</body>
</html>