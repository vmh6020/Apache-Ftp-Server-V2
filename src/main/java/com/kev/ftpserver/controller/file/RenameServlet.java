package com.kev.ftpserver.controller.file;

import com.kev.ftpserver.model.FTPAccount;
import com.kev.ftpserver.service.FileService;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

@WebServlet("/rename")
public class RenameServlet extends HttpServlet {

    @Inject
    private FileService fileService;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        FTPAccount account = (session != null) ? (FTPAccount) session.getAttribute("account") : null;
        FTPClient ftpClient = (session != null) ? (FTPClient) session.getAttribute("ftpClient") : null;

        if (account == null || ftpClient == null || !ftpClient.isConnected()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập");
            return;
        }

        String oldPath = request.getParameter("oldPath");
        String newName = request.getParameter("newName");
        String currentPath = request.getParameter("currentPath");

        if (oldPath == null || newName == null || currentPath == null || newName.isBlank()) {
            session.setAttribute("file-error", "Tên mới không được để trống.");
            response.sendRedirect(request.getContextPath() + "/files?path=" + currentPath);
            return;
        }

        try {
            // 2. Gọi Service
            fileService.renameItem(ftpClient, oldPath, currentPath, newName);

        } catch (IOException e) {
            // 3. Xử lý lỗi
            session.setAttribute("file-error", "Lỗi đổi tên: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/files?path=" + currentPath);
    }
}