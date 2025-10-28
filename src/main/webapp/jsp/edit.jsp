<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>Edit File</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <meta name="viewport" content="width=device-width, initial-scale=1">
</head>
<body class="bg-light">

<nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm">
    <div class="container">
        <a class="navbar-brand" href="#">FTP Manager</a>
        <span class="navbar-text">
            (Đăng nhập: <strong><c:out value="${sessionScope.account.username}"/></strong>)
        </span>
        <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline-light btn-sm ms-3">
            <i class="bi bi-box-arrow-right"></i> Đăng xuất
        </a>
    </div>
</nav>

<div class="container mt-4">
    <div class="card shadow-sm border-0">
        <div class="card-body p-4">

            <%-- Tiêu đề trang --%>
            <h2><i class="bi bi-pencil-square"></i> Đang sửa file: <c:out value="${filePath}"/></h2>

            <%-- Nút quay lại --%>
            <p class="mt-3">
                <a href="${pageContext.request.contextPath}/files?path=<c:out value='${parentPath}'/>" class="btn btn-outline-secondary">
                    <i class="bi bi-arrow-left"></i> Quay lại (Không lưu)
                </a>
            </p>

            <%-- Form để gửi nội dung đã sửa --%>
            <form action="${pageContext.request.contextPath}/edit-content" method="POST">

                <%-- === SỬA LỖI: Dùng EL trực tiếp cho value === --%>
                <%-- Trường ẩn 1: Lưu đường dẫn file đang sửa --%>
                <input type="hidden" name="filePath" value="${filePath}">

                <%-- Trường ẩn 2: Lưu đường dẫn thư mục cha để quay về --%>
                <input type="hidden" name="parentPath" value="${parentPath}">
                <%-- ============================================= --%>


                <%-- Ô nhập nội dung file --%>
                <div class="mb-3">
                    <label for="fileContent" class="form-label"><strong>Nội dung file:</strong></label>
                    <%-- === SỬA LỖI: Dùng EL trực tiếp, không dùng c:out bên trong textarea === --%>
                    <textarea name="fileContent" id="fileContent" class="form-control"
                              style="width: 100%; height: 50vh; font-family: monospace;"
                    >${fileContent}</textarea>
                    <%-- ======================================================================= --%>
                </div>

                <%-- Nút Lưu --%>
                <button type="submit" class="btn btn-primary">
                    <i class="bi bi-save"></i> Lưu thay đổi
                </button>
            </form>

        </div> <%-- Hết card-body --%>
    </div> <%-- Hết card --%>
</div> <%-- Hết container --%>

</body>
</html>