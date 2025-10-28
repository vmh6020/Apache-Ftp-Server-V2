package com.kev.ftpserver.util;

// Import 2 thư viện mới
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {

    // Tạo 1 "cỗ máy" băm Bcrypt
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // HÀM BĂM MỚI
    public static String hashPassword(String plainPassword) {
        // Mã hóa Bcrypt (đã tự động "rắc muối" - salt)
        return encoder.encode(plainPassword);
    }

    // HÀM SO SÁNH MỚI
    public static boolean checkPassword(String plainPassword, String storedHashedPassword) {
        if (plainPassword == null || storedHashedPassword == null) {
            return false;
        }

        // Bcrypt tự động so sánh "muối" bên trong storedHashedPassword
        return encoder.matches(plainPassword, storedHashedPassword);
    }
}