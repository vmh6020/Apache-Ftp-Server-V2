    package com.kev.ftpserver.servlet;

    import com.kev.ftpserver.util.PasswordUtil;
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
        private String homeDirectory;

        @Override
        public void init(ServletConfig config) throws ServletException {
            super.init(config);
            try {
                InitialContext initContext = new InitialContext();
                Context envContext = (Context) initContext.lookup("java:comp/env");
                dataSource = (DataSource) envContext.lookup("jdbc/myWebAppDB");

                // Đọc cấu hình homeDirectory từ web.xml
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
            boolean writepermission = true;

            if (isUsernameTaken(username)) {
                request.setAttribute("register-error", "Tên người dùng '" + username + "' đã tồn tại!");
                request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
                return;
            }

            String sql = "INSERT INTO ftp_users (userid, userpassword, homedirectory, enableflag, writepermission, idletime, uploadrate, downloadrate, maxloginnumber, maxloginperip) VALUES (?, ?, ?, ?, ?, 0, 0, 0, 0, 0)";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username); // Dùng 'username' cho cột 'userid'
                ps.setString(2, userpassword);
                ps.setString(3, homedirectory);
                ps.setBoolean(4, enabled); // Dùng 'enabled' cho cột 'enableflag'
                ps.setBoolean(5, writepermission);
                ps.executeUpdate();

                // Tạo thư mục vật lý
                File homeDirFile = new File(homedirectory);
                if (!homeDirFile.exists()) {
                    if (homeDirFile.mkdirs()) {
                        System.out.println("WEBAPP: Đã tạo thư mục home: " + homedirectory);
                    } else {
                        System.err.println("WEBAPP: !!! LỖI: Không thể tạo thư mục home: " + homedirectory);
                    }
                }

                HttpSession session = request.getSession();
                session.setAttribute("register-success", "Đăng kí thành công ✅! Bạn có thể login 🧑‍💻!");
                response.sendRedirect(request.getContextPath() + "/jsp/index.jsp");
            } catch (SQLException e) {
                getServletContext().log("Lỗi SQL khi đăng ký user: " + username, e);
                request.setAttribute("register-error", "Lỗi hệ thống, không thể đăng ký!");
                request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
            }
        }

        private boolean isUsernameTaken(String username) {
            String sql = "SELECT userid FROM ftp_users WHERE userid = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    return rs.next();
                }
            } catch (SQLException e) {
                getServletContext().log("Lỗi SQL khi kiểm tra username: " + username, e);
                return true;
            }
        }
    }