package com.kev.ftpserver.repository;

import com.kev.ftpserver.service.ConfigService;
import com.kev.ftpserver.service.dto.RegisterRequest; // Chúng ta sẽ tạo DTO này ở bước sau
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Lớp Thủ kho: Chỉ chịu trách nhiệm giao tiếp với Database.
 * Không chứa bất kỳ logic nghiệp vụ nào (như hash password).
 */
@ApplicationScoped // Báo cho CDI biết "đây là một Bean, hãy quản lý nó"
public class UserRepository {

    // Tạo 1 "Cuốn sổ log" riêng cho class này
    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

    private DataSource dataSource;
    /**
     * Constructor rỗng bắt buộc cho CDI Proxying.
     * Không làm gì cả, việc khởi tạo DataSource sẽ do hàm @Inject lo.
     */
    public UserRepository() {
        log.debug("UserRepository: Constructor rỗng được gọi (CDI Proxy).");
    }
    // ==========================================

    /**
     * Constructor chính, được CDI gọi để "tiêm" ConfigService.
     * Sẽ thực hiện việc khởi tạo DataSource.
     */
    @Inject // Yêu cầu CDI tiêm ConfigService vào hàm này
    public UserRepository(ConfigService configService) {
        try {
            String jndiName = configService.getDbJndiName(); // Lấy tên JNDI từ config
            InitialContext initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:comp/env");
            this.dataSource = (DataSource) envContext.lookup(jndiName);
            log.info("Khởi tạo UserRepository thành công, đã kết nối JNDI [{}]", jndiName);
        } catch (Exception e) {
            log.error("!!! LỖI NGHIÊM TRỌNG: Không thể tìm thấy DataSource (kiểm tra config.properties và context.xml)", e);
            throw new RuntimeException("Không thể khởi tạo DataSource cho UserRepository", e);
        }
    }

    /**
     * Tìm mật khẩu đã băm của user (lấy từ LoginServlet)
     */
    public String findHashedPasswordByUsername(String username) throws SQLException {
        String sql = "SELECT userpassword FROM ftp_users WHERE userid = ? AND enableflag = true";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("userpassword");
                }
            }
        }
        return null; // Không tìm thấy user
    }

    /**
     * Kiểm tra username tồn tại (lấy từ RegisterServlet)
     */
    public boolean isUsernameTaken(String username) throws SQLException {
        String sql = "SELECT userid FROM ftp_users WHERE userid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (var rs = ps.executeQuery()) {
                return rs.next(); // true nếu đã có
            }
        }
    }

    /**
     * Lưu user mới vào DB (lấy từ RegisterServlet)
     */
    public void saveNewUser(RegisterRequest req, String hashedPassword) throws SQLException {
        String sql = "INSERT INTO ftp_users (userid, userpassword, homedirectory, enableflag, writepermission, idletime, uploadrate, downloadrate, maxloginnumber, maxloginperip) VALUES (?, ?, ?, ?, ?, 0, 0, 0, 0, 0)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, req.getUsername());
            ps.setString(2, hashedPassword);
            ps.setString(3, req.getHomedirectory());
            ps.setBoolean(4, true); // enableflag
            ps.setBoolean(5, true); // writepermission

            ps.executeUpdate();
            log.info("Đã tạo user mới '{}' trong CSDL", req.getUsername());
        }
    }
}