package com.kev.ftpserver.servlet;

import com.kev.ftpserver.model.FTPAccount; // Dùng model mới
import com.kev.ftpserver.util.PasswordUtil; // Dùng util mới
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private DataSource dataSource;

    @Override
    public void init() throws ServletException {
        try {
            InitialContext initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:comp/env");
            dataSource = (DataSource) envContext.lookup("jdbc/myWebAppDB");
        } catch (Exception e) {
            throw new ServletException("Không thể khởi tạo DataSource", e);
        }
    }

    // Chỉ xử lý POST (đăng nhập)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (isValidUser(username, password)) {
            // Tạo object FTPAccount lưu vào session
            FTPAccount account = new FTPAccount(username, password, "localhost", 2221);
            HttpSession session = request.getSession();
            session.setAttribute("account", account); // Lưu 1 đối tượng, thay vì 2 string

            response.sendRedirect("files"); // FileBrowserServlet

        } else {
            request.setAttribute("login-error", "Sai tài khoản hoặc mật khẩu FTP!");
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }

    // doGet() chuyển sang FileBrowserServlet.java

    // Hàm isValidUser dùng PasswordUtil
    boolean isValidUser(String username, String password) {
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return false;
        }

        String storedHashedPassword = null;
        String sql = "SELECT userpassword FROM ftp_users WHERE userid = ? AND enableflag = true";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    storedHashedPassword = rs.getString("userpassword");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // So sánh mật khẩu dùng PasswordUtil
        return PasswordUtil.checkPassword(password, storedHashedPassword);
    }
}