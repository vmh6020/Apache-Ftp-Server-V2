package com.kev.ftpserver.servlet;

import com.kev.ftpserver.ftp.FTPClientWrapper;
import com.kev.ftpserver.model.FTPAccount;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.ByteArrayInputStream; // Thêm import
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets; // Thêm import
import java.nio.file.Paths;

@WebServlet("/edit-content")
public class EditContentServlet extends HttpServlet {

    // HIỂN THỊ TRANG EDIT: Tải nội dung file và chuyển cho edit.jsp
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        FTPAccount account = (session != null) ? (FTPAccount) session.getAttribute("account") : null;

        if (account == null) {
            response.sendRedirect("index.jsp"); // Chưa login, về index
            return;
        }

        String path = request.getParameter("path");
        if (path == null || path.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu đường dẫn file.");
            return;
        }

        try {
            // 1. Gọi hàm mới để download file về dạng String
            String fileContent = FTPClientWrapper.downloadFileAsString(account, path);

            // 2. Lấy thư mục cha (để biết đường quay về)
            String parentPath = Paths.get(path).getParent().toString().replace("\\", "/");
            if (parentPath.isEmpty()) parentPath = "/"; // Xử lý trường hợp file ở gốc

            // 3. Đặt thuộc tính cho jsp
            request.setAttribute("fileContent", fileContent);
            request.setAttribute("filePath", path);
            request.setAttribute("parentPath", parentPath);

            // 4. Chuyển sang trang edit.jsp
            request.getRequestDispatcher("/jsp/edit.jsp").forward(request, response);

        } catch (IOException e) {
            session.setAttribute("file-error", "Lỗi không đọc được file: " + e.getMessage());
            response.sendRedirect("files"); // Quay về trang list file
        }
    }

    // LƯU NỘI DUNG MỚI: Nhận text và upload (ghi đè)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        FTPAccount account = (session != null) ? (FTPAccount) session.getAttribute("account") : null;

        if (account == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Phiên hết hạn.");
            return;
        }

        // Lấy nội dung text mới từ <textarea>
        String newContent = request.getParameter("fileContent");
        String filePath = request.getParameter("filePath");
        String parentPath = request.getParameter("parentPath");

        if (filePath == null || newContent == null || parentPath == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu dữ liệu (path hoặc content).");
            return;
        }

        try {
            // 1. Chuyển String nội dung mới thành InputStream
            InputStream inputStream = new ByteArrayInputStream(newContent.getBytes(StandardCharsets.UTF_8));

            // 2. Ghi đè file cũ
            FTPClientWrapper.uploadFile(account, filePath, inputStream);

            // 3. Quay về trang danh sách file (tại thư mục cha)
            response.sendRedirect("files?path=" + parentPath);

        } catch (IOException e) {
            session.setAttribute("file-error", "Lỗi không lưu được file: " + e.getMessage());
            response.sendRedirect("files?path=" + parentPath); // Quay về và báo lỗi
        }
    }
}