<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="com.kev.ftpserver.model.FileItem" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> <%-- Import JSTL --%>
<%-- Thêm thư viện JSTL functions (để check đuôi file) --%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head>
    <title>FTP File Browser</title>
</head>
<body>
<h2>✅ Đăng nhập FTP thành công!</h2>
<h3>Thư mục hiện tại: <c:out value="${currentPath}"/></h3>

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

    <%-- Dùng JSTL để lặp qua List<FileItem> --%>
    <c:forEach var="item" items="${files}">
        <%-- Biến tạm thời để check đuôi file (viết thường) --%>
        <c:set var="fileNameLower" value="${fn:toLowerCase(item.name)}" />

        <tr>
            <td>
                    <%--CỘT 1: Hiển thị icon 📁 hoặc 📄 --%>
                <c:if test="${item.isDirectory()}">
                    <span>📁</span>
                </c:if>
                <c:if test="${!item.isDirectory()}">
                    <span>📄</span>
                </c:if>
            </td>
                <%-- CỘT 2: TÊN FILE (LINK DUYỆT HOẶC DOWNLOAD) --%>
            <td>
                    <%--thư mục -> tạo link để duyệt vào --%>
                <c:if test="${item.isDirectory()}">
                    <a href="files?path=<c:out value='${item.path}'/>"><c:out value="${item.name}"/></a>
                </c:if>
                    <%-- file ->  link download --%>
                <c:if test="${!item.isDirectory()}">
                    <a href="download?file=<c:out value='${item.path}'/>"><c:out value="${item.name}"/></a>
                </c:if>
                <%-- Link edit--%>
                <a href="#"
                   style="font-size: 0.8em; margin-left: 10px;"
                   onclick="editFile(
                           '<c:out value='${item.path}'/>',
                           '<c:out value='${item.name}'/>',
                           '<c:out value='${currentPath}'/>'
                           )">
                </a>
            </td>
                <%-- CỘT 3: KÍCH THƯỚC --%>
            <td>
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

<h3>⬆️ Upload file vào thư mục này</h3>
<form method="post" action="upload" enctype="multipart/form-data">
    <%-- Gửi kèm path hiện tại để UploadServlet biết upload vào đâu --%>
    <input type="hidden" name="path" value="<c:out value='${currentPath}'/>" />
    <input type="file" name="file" />
    <input type="submit" value="Upload" />
</form>

<%-- Hiển thị lỗi nếu upload thất bại --%>
<% if (request.getAttribute("upload-error") != null) { %>
<p style="color:red"><%= request.getAttribute("upload-error") %></p>
<% } %>
<%-- --- FORM ẨN VÀ JAVASCRIPT CHO CHỨC NĂNG "ĐỔI TÊN" --- --%>
<%-- Form này bị ẩn, nó chờ JavaScript điền thông tin và submit --%>
<form id="renameForm" action="rename" method="POST" style="display:none;">
    <input type="hidden" name="oldPath" id="renameOldPath">
    <input type="hidden" name="newName" id="renameNewName">
    <input type="hidden" name="currentPath" id="renameCurrentPath">
</form>

<script>
    // Đổi tên hàm cho rõ nghĩa
    function renameFile(oldPath, oldName, currentPath) {
        // Hiện hộp thoại simple
        const newName = prompt("Nhập tên mới cho:", oldName);

        // Nếu user nhấn OK và tên mới có nội dung (khác tên cũ)
        if (newName && newName.trim() !== "" && newName !== oldName) {
            // Điền thông tin vào form ẩn "renameForm"
            document.getElementById('renameOldPath').value = oldPath;
            document.getElementById('renameNewName').value = newName;
            document.getElementById('renameCurrentPath').value = currentPath;

            // Submit form ẩn
            document.getElementById('renameForm').submit();
        } else if (newName === "") {
            alert("Tên mới không được để trống!");
        }
        // Nếu user nhấn Cancel, không làm gì cả
    }
</script>

</body>
</html>