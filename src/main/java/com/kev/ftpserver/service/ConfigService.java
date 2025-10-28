package com.kev.ftpserver.service;

import jakarta.annotation.PostConstruct; // Import mới
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

@ApplicationScoped // Quản lý Bean này
public class ConfigService {
    private static final Logger log = LoggerFactory.getLogger(ConfigService.class);
    private final Properties properties = new Properties();

    // Hàm này được CDI gọi tự động SAU KHI class này được tạo
    @PostConstruct
    public void init() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                log.error("!!! LỖI NGHIÊM TRỌNG: Không tìm thấy file 'config.properties' trong resources!");
                return;
            }
            properties.load(input);
            log.info("Đã tải file config.properties thành công.");
        } catch (Exception e) {
            log.error("!!! LỖI NGHIÊM TRỌNG: Không thể đọc file 'config.properties'", e);
        }
    }

    // Các hàm "phát" cấu hình
    public String getDbJndiName() {
        return properties.getProperty("db.jndi.name", "jdbc/myWebAppDB"); // Cung cấp giá trị dự phòng
    }

    public String getFtpServer() {
        return properties.getProperty("ftp.server", "localhost");
    }

    public int getFtpPort() {
        return Integer.parseInt(properties.getProperty("ftp.port", "2221"));
    }

    public String getFtpHomeDirectory() {
        return properties.getProperty("ftp.home.directory");
    }
}