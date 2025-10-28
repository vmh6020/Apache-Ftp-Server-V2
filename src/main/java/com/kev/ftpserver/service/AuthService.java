package com.kev.ftpserver.service;

// Import các file mới
import com.kev.ftpserver.model.FTPAccount;
import com.kev.ftpserver.repository.FtpRepository;
import com.kev.ftpserver.repository.UserRepository;
import com.kev.ftpserver.service.dto.LoginResult; // <-- Import "gói hàng"
import com.kev.ftpserver.service.dto.RegisterRequest;
import com.kev.ftpserver.util.PasswordUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.commons.net.ftp.FTPClient; // <-- Import FTPClient
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;

@ApplicationScoped
public class AuthService {
    // === TẠO RA 1 LOGGER RIÊNG CHO CLASS NÀY ===
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Inject
    private UserRepository userRepository;

    @Inject
    private FtpRepository ftpRepository; // Tiêm Thủ kho FTP

    @Inject // === TIÊM THÊM CONFIGSERVICE ===
    private ConfigService configService;
    /**
     * Logic đăng nhập
     * === HÀM ĐÃ ĐƯỢC NÂNG CẤP ===
     * @return Trả về LoginResult (chứa Account và FTPClient) nếu thành công, null nếu thất bại
     */
    public LoginResult login(String username, String password) { // <-- KIỂU TRẢ VỀ LÀ LoginResult
        if (username == null || password == null) {
            return null;
        }

        try {
            // 1. Lấy hash từ DB
            String storedHash = userRepository.findHashedPasswordByUsername(username);

            // 2. So sánh mật khẩu
            if (PasswordUtil.checkPassword(password, storedHash)) {
                log.info("User '{}' đăng nhập thành công (mật khẩu khớp)", username);

                // 3. Mật khẩu đúng! KẾT NỐI FTP NGAY BÂY GIỜ
                String server = configService.getFtpServer();
                int port = configService.getFtpPort();

                FTPClient liveFtpClient = ftpRepository.connect(server, port, username, password);
                log.info("Đã tạo kết nối FTP cho user '{}'", username);
                // 4. Tạo Account
                FTPAccount account = new FTPAccount(username, password, server, port);

                // 5. Trả về "Gói hàng" chứa cả 2
                return new LoginResult(account, liveFtpClient); // <-- Trả về gói hàng
            }
        } catch (SQLException e) { // Bắt cả lỗi FTP
            // Nếu lỗi (sai pass FTP, sập CSDL...) thì cũng là login thất bại
            log.error("Lỗi nghiêm trọng khi login cho user: {}", username, e);
            return null;
        } catch (IOException e) {
            log.error("Lỗi FTP khi user '{}' đăng nhập (sai pass FTP?)", username, e);
            return null;
        }

        return null; // Sai mật khẩu hoặc không có user
    }

    /**
     * Logic đăng ký (Giữ nguyên như cũ)
     */
    public String register(String username, String password, String baseHomeDirectory) {
        try {
            // 1. Kiểm tra username tồn tại
            if (userRepository.isUsernameTaken(username)) {
                log.warn("User '{}' đăng ký thất bại (tên đã tồn tại)", username);
                return "Tên người dùng '" + username + "' đã tồn tại!";
            }

            // 2. Băm mật khẩu
            String hashedPassword = PasswordUtil.hashPassword(password);

            // 3. Tính toán đường dẫn
            String homedirectory = Paths.get(baseHomeDirectory, username).toString();

            // 4. Gói dữ liệu
            RegisterRequest dto = new RegisterRequest(username, homedirectory);

            // 5. Lưu vào DB
            userRepository.saveNewUser(dto, hashedPassword);

            // 6. Tạo thư mục vật lý
            createHomeDirectory(homedirectory);
            log.info("User '{}' đăng ký thành công!", username);
            // 7. Thành công
            return null;

        } catch (SQLException e) {
            log.error("Lỗi SQL khi register user: {}", username, e);
            return "Lỗi hệ thống CSDL, không thể đăng ký!";
        } catch (IOException e) {
            log.error("Lỗi IO khi tạo thư mục cho user: {}", username, e);
            return "Lỗi hệ thống (File), không thể tạo thư mục!";
        }
    }

    // Logic tạo thư mục (Giữ nguyên như cũ)
    private void createHomeDirectory(String homedirectory) throws IOException {
        File homeDirFile = new File(homedirectory);
        if (!homeDirFile.exists()) {
            if (homeDirFile.mkdirs()) {
                log.info("AuthService: Đã tạo thư mục home: {}", homedirectory);
            } else {
                // Ném lỗi để hàm register() bắt được
                throw new IOException("Không thể tạo thư mục home: " + homedirectory);
            }
        }
    }
}