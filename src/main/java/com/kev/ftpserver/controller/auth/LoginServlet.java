package com.kev.ftpserver.controller.auth;

import com.kev.ftpserver.model.FTPAccount;
import com.kev.ftpserver.service.AuthService;
import com.kev.ftpserver.service.dto.LoginResult; // Sửa
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.net.ftp.FTPClient; // Sửa

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Inject
    private AuthService authService;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // === THAY ĐỔI LOGIC ===
        // Ủy quyền cho "Bộ não" (Service)
        LoginResult loginResult = authService.login(username, password);

        if (loginResult != null) {
            // Đăng nhập thành công
            HttpSession session = request.getSession();

            // Lưu cả 2 vào Session
            session.setAttribute("account", loginResult.getAccount());
            session.setAttribute("ftpClient", loginResult.getFtpClient()); // <-- LƯU KẾT NỐI

            session.setAttribute("showLoginSuccess", true);
            response.sendRedirect(request.getContextPath() + "/files");

        } else {
            // Đăng nhập thất bại
            request.setAttribute("login-error", "Sai tài khoản hoặc mật khẩu FTP!");
            request.getRequestDispatcher("/jsp/index.jsp").forward(request, response);
        }
    }
}