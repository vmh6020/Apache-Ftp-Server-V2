package com.kev.ftpserver.controller.auth;

import com.kev.ftpserver.repository.FtpRepository; // Sửa 1
import jakarta.inject.Inject; // Sửa 2
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.net.ftp.FTPClient; // Sửa 3

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    @Inject // Tiêm "Thủ kho" FTP vào
    private FtpRepository ftpRepository;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);

        if (session != null) {

            // === PHẦN NÂNG CẤP HIỆU NĂNG ===
            // Lấy kết nối FTP "sống" ra khỏi session
            FTPClient ftpClient = (FTPClient) session.getAttribute("ftpClient");

            // Ra lệnh cho Thủ kho ngắt kết nối an toàn
            ftpRepository.disconnect(ftpClient);
            // =============================

            session.invalidate(); // Hủy session
        }

        response.sendRedirect(request.getContextPath() + "/");
    }
}