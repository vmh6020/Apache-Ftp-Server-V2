package com.kev.ftpserver.servlet;

import com.kev.ftpserver.util.PasswordUtil; // Dùng util mới
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private DataSource dataSource;
    private String homeDirectory; // Biến lưu đường dẫn gốc

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config); // Phải gọi super.init(config) -> khơi tạo cho việc đoọc dữ liệu từ context.xml
        try {
            InitialContext initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:comp/env");
            dataSource = (DataSource) envContext.lookup("jdbc/myWebAppDB");

            // Đọc cấu hính homeDirectory từ context.xml
            ServletContext context = config.getServletContext();
            homeDirectory = context.getInitParameter("homeDirectory");
            if (homeDirectory == null || homeDirectory.isBlank()) {
                throw new ServletException("Thiếu cấu hình 'homeDirectory' trong web.xml");
            }

        } catch (Exception e) {
            throw new ServletException("Không thể khởi tạo DataSource hoặc homeDirectory", e);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        String userpassword = PasswordUtil.hashPassword(password);
        String homedirectory = Paths.get(homeDirectory, username).toString();

        boolean enabled = true;
        boolean writepermission = false; // để mặc định

        if (isUsernameTaken(username)) {
            request.setAttribute("register-error", "Tên người dùng '" + username + "' đã tồn tại!");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
            return;
        }

        String sql = "INSERT INTO ftp_users (username, userpassword, homedirectory, enabled, writepermission) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, userpassword);
            ps.setString(3, homedirectory);
            ps.setBoolean(4, enabled);
            ps.setBoolean(5, writepermission);
            ps.executeUpdate();

            // Tạo thư mục vật lý
            File homeDirFile = new File(homedirectory);
            if (!homeDirFile.exists()) {
                if (homeDirFile.mkdirs()) {
                    System.out.println("WEBAPP: Đã tạo thư mục home: " + homedirectory);
                } else {
                    // Nếu không tạo được, vẫn báo đăng ký thành công, nhưng sẽ có lỗi khi FTP login
                    System.err.println("WEBAPP: !!! LỖI: Không thể tạo thư mục home: " + homedirectory);
                }
            }

            HttpSession session = request.getSession();
            session.setAttribute("register-success", "Đăng kí thành công ✅! Bạn có thể login 🧑‍💻!");
            response.sendRedirect("index.jsp");
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("register-error", "Lỗi hệ thống, không thể đăng ký!");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }

    private boolean isUsernameTaken(String username) {
        String sql = "SELECT username FROM ftp_users WHERE username = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (var rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }
}