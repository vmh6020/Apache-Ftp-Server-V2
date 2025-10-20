package com.kev.ftpserver.servlet;

import com.kev.ftpserver.ftp.FTPClientWrapper;
import com.kev.ftpserver.model.FTPAccount;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.file.Paths;

@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        // Lấy tên file từ query string (?file=/path/to/abc.txt)
        String remoteFile = request.getParameter("file");
        if (remoteFile == null || remoteFile.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu tên file cần tải");
            return;
        }

        // Lấy tên file (không bao gồm path) để gợi ý "Save As"
        String fileName = Paths.get(remoteFile).getFileName().toString();


        HttpSession session = request.getSession(false);
        FTPAccount account = (session != null) ? (FTPAccount) session.getAttribute("account") : null;

        if (account == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        // Giữ nguyên logic header
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        try {
            FTPClientWrapper.downloadFile(
                    account.getServer(),
                    account.getPort(),
                    account.getUsername(),
                    account.getPassword(),
                    remoteFile, // Truyền full path của file
                    response.getOutputStream()
            );
        } catch (IOException e) {
            throw new ServletException("Không thể tải file từ FTP: " + e.getMessage(), e);
        }
    }
}