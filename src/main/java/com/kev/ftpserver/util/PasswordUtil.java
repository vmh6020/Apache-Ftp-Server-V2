package com.kev.ftpserver.util;

import org.apache.commons.codec.digest.DigestUtils;

public class PasswordUtil {

    // Mã hóa MD5
    public static String hashPassword(String plainPassword) {
        return DigestUtils.md5Hex(plainPassword);
    }

    // So sánh mật khẩu (dùng trong LoginServlet)
    public static boolean checkPassword(String plainPassword, String storedHashedPassword) {
        if (plainPassword == null || storedHashedPassword == null) {
            return false;
        }
        String inputHashedPassword = hashPassword(plainPassword);
        return storedHashedPassword.equalsIgnoreCase(inputHashedPassword);
    }
}