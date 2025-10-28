package com.kev.ftpserver.model;

import java.io.Serializable;

// lưu vào session
public class FTPAccount implements Serializable {
    private final String username;
    private final String password; // clear text, dùng trong session
    private final String server;
    private final int port;

    public FTPAccount(String username, String password, String server, int port) {
        this.username = username;
        this.password = password;
        this.server = server;
        this.port = port;
    }

    // Getters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getServer() { return server; }
    public int getPort() { return port; }
}