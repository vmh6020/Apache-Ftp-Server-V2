<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="com.kev.ftpserver.model.FileItem" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
    <title>FTP File Browser</title>
</head>
<body>
<h2>✅ Đăng nhập FTP thành công!</h2>
<h3>Thư mục hiện tại: <c:out value="${currentPath}"/></h3>
<p><a href="logout">(Đăng xuất)</a></p> <%-- HOÀN THIỆN: Thêm link Logout --%>

<%-- Hiển thị lỗi (nếu có) từ session --%>
<% if (session.getAttribute("file-error") != null) { %>
<p style="color:red; font-weight: bold;">
    LỖI: <%= session.getAttribute("file-error") %>
</p>
<% session.removeAttribute("file-error"); %>
<% } %>

<h3>📁 Danh sách file:</h3>
<table border="1" style="width: 70%; border-collapse: collapse;" cellpadding="5">
    <tr style="background-color: #f0f0f0;">
        <th>Loại</th>
        <th>Tên file</th>
        <th>Kích thước (bytes)</th>
        <th>Hành động</th>
    </tr>

    <c:forEach var="item" items="${files}">
        <c:set var="fileNameLower" value="${fn:toLowerCase(item.name)}" />
        <tr>
                <%-- CỘT 1: LOẠI (ICON) --%>
            <td style="text-align: center;">
                <c:if test="${item.isDirectory()}"><span>📁</span></c:if>
                <c:if test="${!item.isDirectory()}"><span>📄</span></c:if>
            </td>

                <%-- CỘT 2: TÊN FILE (LINK DUYỆT HOẶC DOWNLOAD) --%>
            <td>
                <c:if test="${item.isDirectory()}">
                    <a href="files?path=<c:out value='${item.path}'/>"><c:out value="${item.name}"/></a>
                </c:if>
                <c:if test="${!item.isDirectory()}">
                    <a href="download?file=<c:out value='${item.path}'/>"><c:out value="${item.name}"/></a>
                </c:if>

                    <%-- SỬA LỖI: Đã xóa link hỏng (thẻ <a> trống) ở đây --%>

            </td>

                <%-- CỘT 3: KÍCH THƯỚC --%>
            <td style="text-align: right;">
                <c:if test="${!item.isDirectory()}">
                    <c:out value="${item.size}"/>
                </c:if>
            </td>

                <%-- CỘT 4: HÀNH ĐỘNG (ĐỔI TÊN / SỬA NỘI DUNG) --%>
            <td>
                    <%-- 1. Link "Đổi tên" (Luôn hiển thị) --%>
                <a href="#"
                   style="font-size: 0.9em; margin-right: 10px;"
                   onclick="renameFile(
                           '<c:out value='${item.path}'/>',
                           '<c:out value='${item.name}'/>',
                           '<c:out value='${currentPath}'/>'
                           )">
                    (Đổi tên)
                </a>

                    <%-- 2. Link "Sửa nội dung" (Chỉ hiển thị cho file text) --%>
                <c:if test="${!item.isDirectory() &&
                             (fn:endsWith(fileNameLower, '.txt') ||
                              fn:endsWith(fileNameLower, '.md') ||
                              fn:endsWith(fileNameLower, '.log') ||
                              fn:endsWith(fileNameLower, '.java') ||
                              fn:endsWith(fileNameLower, '.xml') ||
                              fn:endsWith(fileNameLower, '.properties'))
                            }">

                    <a href="edit-content?path=<c:out value='${item.path}'/>"
                       style="font-size: 0.9em;">
                        (Sửa nội dung)
                    </a>
                </c:if>
            </td>
        </tr>
    </c:forEach>
</table>

<%-- --- PHẦN UPLOAD FILE --- --%>
<h3>⬆️ Upload file vào thư mục này</h3>
<form method="post" action="upload" enctype="multipart/form-data">
    <input type="hidden" name="path" value="<c:out value='${currentPath}'/>" />
    <input type="file" name="file" />
    <input type="submit" value="Upload" />
</form>

<%-- Hiển thị lỗi nếu upload thất bại (từ request) --%>
<% if (request.getAttribute("upload-error") != null) { %>
<p style="color:red"><%= request.getAttribute("upload-error") %></p>
<% } %>

<%-- --- FORM ẨN VÀ JAVASCRIPT CHO CHỨC NĂNG "ĐỔI TÊN" --- --%>
<form id="renameForm" action="rename" method="POST" style="display:none;">
    <input type="hidden" name="oldPath" id="renameOldPath">
    <input type="hidden" name="newName" id="renameNewName">
    <input type="hidden" name="currentPath" id="renameCurrentPath">
</form>

<script>
    function renameFile(oldPath, oldName, currentPath) {
        const newName = prompt("Nhập tên mới cho:", oldName);
        if (newName && newName.trim() !== "" && newName !== oldName) {
            document.getElementById('renameOldPath').value = oldPath;
            document.getElementById('renameNewName').value = newName;
            document.getElementById('renameCurrentPath').value = currentPath;
            document.getElementById('renameForm').submit();
        } else if (newName === "") {
            alert("Tên mới không được để trống!");
        }
    }
</script>

</body>
</html>