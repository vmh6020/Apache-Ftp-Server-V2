package com.kev.ftpserver.servlet;

import com.kev.ftpserver.ftp.FTPClientWrapper;
import com.kev.ftpserver.model.FTPAccount;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;

@WebServlet("/upload")
@MultipartConfig
public class UploadServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        HttpSession session = request.getSession(false);
        FTPAccount account = (session != null) ? (FTPAccount) session.getAttribute("account") : null;

        if (account == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        // Lấy path hiện tại (để biết upload vào đâu)
        String currentPath = request.getParameter("path");
        if (currentPath == null || currentPath.isBlank()) {
            currentPath = "/";
        }

        Part filePart = request.getPart("file");
        String fileName = filePart.getSubmittedFileName();

        // Tạo đường dẫn file đầy đủ trên server
        String remoteFile = currentPath.equals("/") ? fileName : currentPath + "/" + fileName;

        try (InputStream input = filePart.getInputStream()) {
            FTPClientWrapper.uploadFile(account, remoteFile, input);
        } catch (IOException e) {
            // Xử lý lỗi đẹp hơn
            request.setAttribute("upload-error", "Upload thất bại: " + e.getMessage());
            // Gọi lại FileBrowserServlet để hiển thị lỗi
            request.getRequestDispatcher("/files").forward(request, response);
            return;
        }

        // Sau khi upload, quay lại đúng thư mục đó
        response.sendRedirect("files?path=" + currentPath);
    }
}