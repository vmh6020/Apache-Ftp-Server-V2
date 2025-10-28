package com.kev.ftpserver.controller.file; // Package mới

import com.kev.ftpserver.model.FTPAccount;
import com.kev.ftpserver.model.FileItem;
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
import java.util.List;

@WebServlet("/files")
public class FileBrowserServlet extends HttpServlet {

    @Inject
    private FileService fileService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        FTPAccount account = (session != null) ? (FTPAccount) session.getAttribute("account") : null;
        FTPClient ftpClient = (session != null) ? (FTPClient) session.getAttribute("ftpClient") : null;

        if (account == null || ftpClient == null || !ftpClient.isConnected()) {
            response.sendRedirect(request.getContextPath() + "/jsp/index.jsp");
            return;
        }

        try {
            // Xử lý flash message (lấy từ session, đặt vào request, xóa khỏi session)
            if (session.getAttribute("showLoginSuccess") != null) {
                request.setAttribute("welcomeMessage", "✅ Đăng nhập FTP thành công!");
                session.removeAttribute("showLoginSuccess");
            }
            if (session.getAttribute("file-error") != null) {
                request.setAttribute("file-error", session.getAttribute("file-error"));
                session.removeAttribute("file-error");
            }

            // 1. Lấy data từ Web
            String path = request.getParameter("path");
            if (path == null || path.isBlank()) {
                path = "/";
            }

            // 2. Gọi Service
            List<FileItem> files = fileService.listFiles(ftpClient, path);
            String parentPath = fileService.getParentPath(path);

            // 3. Gửi data cho View (JSP)
            request.setAttribute("files", files);
            request.setAttribute("currentPath", path);
            request.setAttribute("parentPath", parentPath);

            request.getRequestDispatcher("/jsp/files.jsp").forward(request, response);

        } catch (IOException e) {
            // Lỗi nghiêm trọng (mất kết nối FTP?)
            session.invalidate(); // Hủy session hỏng
            request.setAttribute("login-error", "Lỗi kết nối FTP: " + e.getMessage());
            request.getRequestDispatcher("/jsp/index.jsp").forward(request, response);
        }
    }
}