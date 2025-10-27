package com.kev.ftpserver.ftp;

import com.kev.ftpserver.model.FTPAccount;
import com.kev.ftpserver.model.FileItem; // Import model mới
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FTPClientWrapper {
    private static FTPClient connect(String server, int port, String user, String pass) throws IOException {
        FTPClient ftp = new FTPClient();
        ftp.connect(server, port);
        if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
            throw new IOException("❌ Server từ chối kết nối");
        }
        if (!ftp.login(user, pass)) {
            throw new IOException("❌ Sai user/pass");
        }
        ftp.enterLocalPassiveMode();
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        return ftp;
    }

    // Khong List<String>, trả về List<FileItem>
    public static List<FileItem> listFiles(String server, int port, String user, String pass, String path) throws IOException {
        FTPClient ftp = connect(server, port, user, pass);
        List<FileItem> items = new ArrayList<>();
        try {
            // listFiles() nhận 1 path để biết đang ở thư mục nào
            for (FTPFile file : ftp.listFiles(path)) {
                //lấy cả thư mục
                String fullPath = path.equals("/") ? "/" + file.getName() : path + "/" + file.getName();

                FileItem item = new FileItem(
                        file.getName(),
                        file.getSize(),
                        file.isDirectory(),
                        fullPath // Truyền path để tạo link download
                );
                items.add(item);
            }
            ftp.logout();
        } finally {
            if (ftp.isConnected()) ftp.disconnect();
        }
        return items;
    }

    public static void renameFile(FTPAccount account, String fromPath, String toPath) throws IOException {
        FTPClient ftp = connect(account.getServer(), account.getPort(), account.getUsername(), account.getPassword());
        boolean success = false;
        try {
            success = ftp.rename(fromPath, toPath);
            if (!success) {
                throw new IOException("Không thể đổi tên: " + ftp.getReplyString());
            }
            ftp.logout();
        } finally {
            if (ftp.isConnected()) ftp.disconnect();
        }
    }

    public static void downloadFile(String server, int port, String user, String pass,
                                    String remoteFile, OutputStream output) throws IOException {
        FTPClient ftp = connect(server, port, user, pass);
        try {
            if (!ftp.retrieveFile(remoteFile, output)) {
                throw new IOException("❌ Không thể tải file: " + remoteFile);
            }
            ftp.logout();
        } finally {
            if (ftp.isConnected()) ftp.disconnect();
        }
    }

    public static void uploadFile(FTPAccount account, String remoteFile, InputStream input) throws IOException {
        FTPClient ftp = connect(account.getServer(), account.getPort(), account.getUsername(), account.getPassword());
        try {
            if (!ftp.storeFile(remoteFile, input)) {
                throw new IOException("❌ Upload thất bại: " + remoteFile);
            }
            ftp.logout();
        } finally {
            if (ftp.isConnected()) ftp.disconnect();
        }
    }
    public static String downloadFileAsString(FTPAccount account, String remoteFile) throws IOException {
        FTPClient ftp = connect(account.getServer(), account.getPort(), account.getUsername(), account.getPassword());

        // Dùng ByteArrayOutputStream để hứng dữ liệu từ file
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            boolean success = ftp.retrieveFile(remoteFile, outputStream);
            ftp.logout();

            if (!success) {
                throw new IOException("Không thể đọc file: " + remoteFile);
            }

            // Chuyển dữ liệu byte[] thành String (dùng UTF-8)
            return outputStream.toString(StandardCharsets.UTF_8);

        } finally {
            if (ftp.isConnected()) ftp.disconnect();
        }
    }
    public static void deleteFileOrDirectory(FTPAccount account, String path) throws IOException {
        FTPClient ftp = connect(account.getServer(), account.getPort(), account.getUsername(), account.getPassword());
        boolean deleted = false;
        try {
            // Thử xóa file trước
            if (ftp.deleteFile(path)) {
                deleted = true;
            } else {
                // Nếu không phải file, thử xóa thư mục (chỉ hoạt động nếu thư mục rỗng)
                deleted = ftp.removeDirectory(path);
            }
            ftp.logout();
        } finally {
            if (ftp.isConnected()) ftp.disconnect();
        }

        if (!deleted) {
            throw new IOException("Không thể xóa file hoặc thư mục (thư mục có thể không rỗng).");
        }
    }
}