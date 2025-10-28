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
import java.nio.file.Paths;

@WebServlet("/download")
public class DownloadServlet extends HttpServlet {

    @Inject
    private FileService fileService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        HttpSession session = request.getSession(false);
        FTPAccount account = (session != null) ? (FTPAccount) session.getAttribute("account") : null;
        FTPClient ftpClient = (session != null) ? (FTPClient) session.getAttribute("ftpClient") : null;

        String remoteFile = request.getParameter("file");

        if (account == null || ftpClient == null || !ftpClient.isConnected()) {
            response.sendRedirect(request.getContextPath() + "/jsp/index.jsp");
            return;
        }

        if (remoteFile == null || remoteFile.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu tên file cần tải");
            return;
        }

        try {
            String fileName = Paths.get(remoteFile).getFileName().toString();
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            // 2. Gọi Service
            fileService.downloadFile(ftpClient, remoteFile, response.getOutputStream());

        } catch (IOException e) {
            // Lỗi này khó xử lý, vì header đã gửi đi
            // Cứ ném ra ServletException để Tomcat lo
            throw new ServletException("Không thể tải file từ FTP: " + e.getMessage(), e);
        }
    }
}