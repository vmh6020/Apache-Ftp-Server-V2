<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<head>
    <title>FTP File Browser</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <style>
        /* CSS nhỏ để các nút hành động nhỏ hơn */
        .action-link {
            font-size: 0.9em;
            text-decoration: none;
            margin-right: 10px;
        }
    </style>
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

    <c:if test="${not empty requestScope.welcomeMessage}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle-fill"></i> <c:out value="${requestScope.welcomeMessage}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    <c:if test="${not empty requestScope['file-error']}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle-fill"></i> <c:out value="${requestScope['file-error']}"/>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <div class="card shadow-sm border-0 mb-3">
        <div class="card-body d-flex justify-content-between align-items-center">
            <nav style="--bs-breadcrumb-divider: '>';" aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item">
                        <c:if test="${currentPath != '/'}">
                            <a href="${pageContext.request.contextPath}/files?path=<c:out value='${parentPath}'/>"
                               class="text-decoration-none">&laquo; Lên thư mục cha</a>
                        </c:if>
                    </li>
                    <li class="breadcrumb-item active" aria-current="page">
                        <strong>Thư mục hiện tại:</strong> <c:out value="${currentPath}"/>
                    </li>
                </ol>
            </nav>
        </div>
    </div>

    <div class="card shadow-sm border-0">
        <div class="card-header bg-white">
            <h3 class="mb-0">📁 Danh sách file</h3>
        </div>
        <div class="card-body p-0">
            <table class="table table-hover table-striped mb-0">
                <thead class="table-light">
                <tr>
                    <th style="width: 5%;">Loại</th>
                    <th>Tên file</th>
                    <th style="width: 15%;">Kích thước (bytes)</th>
                    <th style="width: 25%;">Hành động</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="item" items="${files}">
                    <tr>
                        <td class="text-center">
                            <c:choose>
                                <c:when test="${item.isDirectory()}"><i class="bi bi-folder-fill text-primary"></i></c:when>
                                <c:otherwise><i class="bi bi-file-earmark-text"></i></c:otherwise>
                            </c:choose>
                        </td>

                        <td>
                            <c:choose>
                                <c:when test="${item.isDirectory()}">
                                    <a href="${pageContext.request.contextPath}/files?path=<c:out value='${item.path}'/>"
                                       class="text-decoration-none fw-bold"><c:out value="${item.name}"/></a>
                                </c:when>
                                <c:otherwise>
                                    <a href="${pageContext.request.contextPath}/download?file=<c:out value='${item.path}'/>"
                                       class="text-decoration-none"><c:out value="${item.name}"/></a>
                                </c:otherwise>
                            </c:choose>
                        </td>

                        <td class="text-end">
                            <c:if test="${!item.isDirectory()}">
                                <c:out value="${item.sizeDisplay}"/>
                            </c:if>
                        </td>

                        <td>
                            <a href="#" class="action-link"
                               onclick="renameFile('<c:out value='${item.path}'/>', '<c:out value='${item.name}'/>')">
                                <i class="bi bi-pencil-square"></i> (Đổi tên)
                            </a>

                            <c:if test="${!item.isDirectory()}">
                                <a href="${pageContext.request.contextPath}/edit-content?path=<c:out value='${item.path}'/>"
                                   class="action-link">
                                    <i class="bi bi-pencil"></i> (Sửa)
                                </a>
                            </c:if>

                            <a href="#" class="action-link text-danger"
                               onclick="deleteItem('<c:out value='${item.path}'/>', '<c:out value='${item.name}'/>')">
                                <i class="bi bi-trash"></i> (Xóa)
                            </a>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>

    <div class="card shadow-sm border-0 mt-4">
        <div class="card-body">
            <h3 class="mb-3"><i class="bi bi-upload"></i> Upload</h3>

            <form method="post" action="${pageContext.request.contextPath}/upload" enctype="multipart/form-data" class="mb-3">
                <label for="uploadFiles" class="form-label"><strong>Chọn File(s):</strong></label>
                <input type="hidden" name="path" value="<c:out value='${currentPath}'/>" />
                <div class="input-group">
                    <input type="file" class="form-control" name="files" id="uploadFiles" multiple />
                    <button type="submit" class="btn btn-primary">
                        <i class="bi bi-file-earmark-arrow-up"></i> Upload File(s)
                    </button>
                </div>
                <div class="form-text">Bạn có thể chọn một hoặc nhiều file để upload.</div>
            </form>

            <hr> <%-- Đường kẻ phân cách --%>

            <form method="post" action="${pageContext.request.contextPath}/upload" enctype="multipart/form-data" class="mt-3">
                <label for="uploadFolder" class="form-label"><strong>Chọn Folder:</strong></label>
                <input type="hidden" name="path" value="<c:out value='${currentPath}'/>" />
                <div class="input-group">
                    <input type="file" class="form-control" name="files" id="uploadFolder" webkitdirectory directory multiple />
                    <button type="submit" class="btn btn-secondary">
                        <i class="bi bi-folder-arrow-up"></i> Upload Folder
                    </button>
                </div>
                <div class="form-text">Chọn một thư mục để upload toàn bộ nội dung bên trong.</div>
            </form>

            <c:if test="${not empty requestScope['upload-error']}">
                <div class="alert alert-danger mt-3 mb-0" role="alert">
                    <c:out value="${requestScope['upload-error']}"/>
                </div>
                <% request.removeAttribute("upload-error"); %>
            </c:if>
        </div>
    </div>

</div> <form id="renameForm" action="${pageContext.request.contextPath}/rename" method="POST" style="display:none;">
    <input type="hidden" name="oldPath" id="renameOldPath">
    <input type="hidden" name="newName" id="renameNewName">
    <input type="hidden" name="currentPath" value="<c:out value='${currentPath}'/>">
</form>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

<script>
    function renameFile(oldPath, oldName) {
        const newName = prompt("Nhập tên mới cho:", oldName);
        if (newName && newName.trim() !== "" && newName !== oldName) {
            document.getElementById('renameOldPath').value = oldPath;
            document.getElementById('renameNewName').value = newName;
            // 'renameCurrentPath' đã được set cứng trong form rồi
            document.getElementById('renameForm').submit();
        } else if (newName === "") {
            alert("Tên mới không được để trống!");
        }
    }

    function deleteItem(path, name) {
        const currentPath = "<c:out value='${currentPath}'/>";
        if (confirm("Bạn có chắc chắn muốn xóa: " + name + "?\n(Lưu ý: Thư mục phải rỗng mới xóa được!)")) {

            window.location.href = "${pageContext.request.contextPath}/delete?path="
                + encodeURIComponent(path)
                + "&currentPath=" + encodeURIComponent(currentPath);
        }
    }
</script>

</body>
</html>