package com.kev.ftpserver.controller.auth;

import com.kev.ftpserver.service.AuthService;
import com.kev.ftpserver.service.ConfigService; // Import mới
import jakarta.inject.Inject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Inject
    private AuthService authService;

    @Inject // === TIÊM CONFIGSERVICE ===
    private ConfigService configService;

    // XÓA BỎ HÀM init() (Không cần nữa, ConfigService đã lo)

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // === LẤY CẤU HÌNH TỪ SERVICE ===
        String baseHomeDirectory = configService.getFtpHomeDirectory();
        if (baseHomeDirectory == null) {
            request.setAttribute("register-error", "Lỗi máy chủ: Chưa cấu hình 'ftp.home.directory' trong config.properties.");
            request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
            return;
        }

        // Ủy quyền cho "Bộ não" (Service)
        String errorMessage = authService.register(username, password, baseHomeDirectory);

        if (errorMessage == null) {
            // Đăng ký thành công!
            HttpSession session = request.getSession();
            session.setAttribute("register-success", "Đăng kí thành công ✅! Bạn có thể login 🧑‍💻!");
            response.sendRedirect(request.getContextPath() + "/jsp/index.jsp");

        } else {
            // Đăng ký thất bại
            request.setAttribute("register-error", errorMessage);
            request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
        }
    }
}