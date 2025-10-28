package com.kev.ftpserver.controller.file;

import com.kev.ftpserver.model.FTPAccount;
import com.kev.ftpserver.service.FileService;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

@WebServlet("/upload")
@MultipartConfig
public class UploadServlet extends HttpServlet {

    @Inject
    private FileService fileService;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        HttpSession session = request.getSession(false);
        FTPAccount account = (session != null) ? (FTPAccount) session.getAttribute("account") : null;
        FTPClient ftpClient = (session != null) ? (FTPClient) session.getAttribute("ftpClient") : null;

        if (account == null || ftpClient == null || !ftpClient.isConnected()) {
            response.sendRedirect(request.getContextPath() + "/jsp/index.jsp");
            return;
        }

        String currentPath = request.getParameter("path");
        if (currentPath == null || currentPath.isBlank()) {
            currentPath = "/";
        }

        try {
            // 2. Gọi Service
            fileService.uploadFiles(ftpClient, currentPath, request.getParts());

        } catch (Exception e) {
            // 3. Xử lý lỗi
            session.setAttribute("file-error", "Upload thất bại: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/files?path=" + currentPath);
    }
}