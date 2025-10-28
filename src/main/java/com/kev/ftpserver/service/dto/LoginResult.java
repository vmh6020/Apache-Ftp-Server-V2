package com.kev.ftpserver.service.dto;

import com.kev.ftpserver.model.FTPAccount;
import org.apache.commons.net.ftp.FTPClient;

// DTO này chứa "gói hàng" trả về khi login thành công
public class LoginResult {
    private final FTPAccount account;
    private final FTPClient ftpClient; // Kết nối FTP đang "sống"

    public LoginResult(FTPAccount account, FTPClient ftpClient) {
        this.account = account;
        this.ftpClient = ftpClient;
    }

    public FTPAccount getAccount() { return account; }
    public FTPClient getFtpClient() { return ftpClient; }
}