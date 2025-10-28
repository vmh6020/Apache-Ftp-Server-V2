package com.kev.ftpserver.service.dto;

// Đây là 1 class "ngu ngốc" (POJO/DTO) chỉ để chứa dữ liệu
// Nó giúp AuthService và UserRepository giao tiếp sạch sẽ hơn
public class RegisterRequest {
    private final String username;
    private final String homedirectory;

    public RegisterRequest(String username, String homedirectory) {
        this.username = username;
        this.homedirectory = homedirectory;
    }

    // Getters
    public String getUsername() {
        return username;
    }
    public String getHomedirectory() {
        return homedirectory;
    }
}