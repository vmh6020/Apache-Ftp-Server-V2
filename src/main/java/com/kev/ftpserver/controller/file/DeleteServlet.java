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

@WebServlet("/delete")
public class DeleteServlet extends HttpServlet {

    @Inject
    private FileService fileService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        FTPAccount account = (session != null) ? (FTPAccount) session.getAttribute("account") : null;
        FTPClient ftpClient = (session != null) ? (FTPClient) session.getAttribute("ftpClient") : null;

        if (account == null || ftpClient == null || !ftpClient.isConnected()) {
            response.sendRedirect(request.getContextPath() + "/jsp/index.jsp");
            return;
        }

        String pathToDelete = request.getParameter("path");
        String currentPath = request.getParameter("currentPath");

        if (pathToDelete == null || currentPath == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu thông tin file cần xóa.");
            return;
        }

        try {
            // 2. Gọi Service
            fileService.deleteItem(ftpClient, pathToDelete);
        } catch (IOException e) {
            // 3. Xử lý lỗi
            session.setAttribute("file-error", "Lỗi xóa: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/files?path=" + currentPath);
    }
}