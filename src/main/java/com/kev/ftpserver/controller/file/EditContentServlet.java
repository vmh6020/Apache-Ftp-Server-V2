package com.kev.ftpserver.controller.file;

import com.kev.ftpserver.model.FTPAccount;
import com.kev.ftpserver.service.FileService;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

@WebServlet("/edit-content")
public class EditContentServlet extends HttpServlet {

    @Inject
    private FileService fileService;

    // HIỂN THỊ TRANG EDIT
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        FTPAccount account = (session != null) ? (FTPAccount) session.getAttribute("account") : null;
        FTPClient ftpClient = (session != null) ? (FTPClient) session.getAttribute("ftpClient") : null;

        if (account == null || ftpClient == null || !ftpClient.isConnected()) {
            response.sendRedirect(request.getContextPath() + "/jsp/index.jsp");
            return;
        }

        String path = request.getParameter("path");
        if (path == null || path.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu đường dẫn file.");
            return;
        }

        try {
            // 2. Gọi Service
            String fileContent = fileService.getFileContent(ftpClient, path);
            String parentPath = fileService.getParentPath(path);

            // 3. Gửi data cho View (JSP)
            request.setAttribute("fileContent", fileContent);
            request.setAttribute("filePath", path);
            request.setAttribute("parentPath", parentPath);

            request.getRequestDispatcher("/jsp/edit.jsp").forward(request, response);

        } catch (IOException e) {
            session.setAttribute("file-error", "Lỗi không đọc được file: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/files");
        }
    }

    // LƯU NỘI DUNG MỚI
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        FTPAccount account = (session != null) ? (FTPAccount) session.getAttribute("account") : null;
        FTPClient ftpClient = (session != null) ? (FTPClient) session.getAttribute("ftpClient") : null;

        if (account == null || ftpClient == null || !ftpClient.isConnected()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Phiên hết hạn.");
            return;
        }

        String newContent = request.getParameter("fileContent");
        String filePath = request.getParameter("filePath");
        String parentPath = request.getParameter("parentPath");

        if (filePath == null || newContent == null || parentPath == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu dữ liệu (path hoặc content).");
            return;
        }

        try {
            // 2. Gọi Service
            fileService.saveFileContent(ftpClient, filePath, newContent);

            // 3. Quay về
            response.sendRedirect(request.getContextPath() + "/files?path=" + parentPath);

        } catch (IOException e) {
            session.setAttribute("file-error", "Lỗi không lưu được file: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/files?path=" + parentPath);
        }
    }
}