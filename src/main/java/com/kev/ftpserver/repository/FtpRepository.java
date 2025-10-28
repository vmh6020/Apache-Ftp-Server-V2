package com.kev.ftpserver.repository;

import com.kev.ftpserver.model.FileItem;
import com.kev.ftpserver.model.FileObject;
import com.kev.ftpserver.model.FolderObject;
import jakarta.enterprise.context.ApplicationScoped;
// === THÊM IMPORT LOGGER ===
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// ==========================
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp Thủ kho FTP (Thay thế FTPClientWrapper).
 * Đã được nâng cấp Logger (Sổ sách).
 */
@ApplicationScoped
public class FtpRepository {

    // === TẠO 1 "CUỐN SỔ" RIÊNG CHO CLASS NÀY ===
    private static final Logger log = LoggerFactory.getLogger(FtpRepository.class);

    // === HÀM KẾT NỐI VÀ NGẮT KẾT NỐI (DÙNG 1 LẦN) ===

    public FTPClient connect(String server, int port, String user, String pass) throws IOException {
        FTPClient ftp = new FTPClient();
        log.debug("Đang kết nối tới FTP server {}:{}", server, port);
        ftp.connect(server, port);
        if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
            log.error("Server từ chối kết nối. Reply code: {}", ftp.getReplyCode());
            throw new IOException("❌ Server từ chối kết nối");
        }
        if (!ftp.login(user, pass)) {
            log.warn("Đăng nhập FTP thất bại cho user '{}'", user);
            throw new IOException("❌ Sai user/pass FTP");
        }
        ftp.enterLocalPassiveMode();
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        log.info("Kết nối và Đăng nhập FTP thành công cho user '{}'", user);
        return ftp;
    }

    public void disconnect(FTPClient ftp) {
        if (ftp == null) return;
        try {
            if (ftp.isConnected()) {
                log.debug("Đang logout FTP...");
                ftp.logout();
                log.debug("Đang disconnect FTP...");
                ftp.disconnect();
                log.info("Đã ngắt kết nối FTP an toàn.");
            }
        } catch (IOException e) {
            // Lỗi này không nghiêm trọng, chỉ cần ghi log
            log.warn("Lỗi nhỏ khi ngắt kết nối FTP (có thể server đã tự đóng)", e);
        }
    }

    // === CÁC HÀM LÀM VIỆC (DÙNG FTPClient CÓ SẴN) ===

    public List<FileItem> listFiles(FTPClient ftp, String path) throws IOException {
        log.debug("Đang lấy danh sách file tại path: '{}'", path);
        List<FileItem> items = new ArrayList<>();
        for (FTPFile file : ftp.listFiles(path)) {
            String fullPath = path.equals("/") ? "/" + file.getName() : path + "/" + file.getName();

            if (file.isDirectory()) {
                items.add(new FolderObject(file.getName(), fullPath));
            } else {
                items.add(new FileObject(file.getName(), fullPath, file.getSize()));
            }
        }
        log.info("Lấy danh sách file thành công. Tìm thấy {} item.", items.size());
        return items;
    }

    public void renameFile(FTPClient ftp, String fromPath, String toPath) throws IOException {
        log.debug("Đang đổi tên từ '{}' sang '{}'", fromPath, toPath);
        if (!ftp.rename(fromPath, toPath)) {
            log.warn("Đổi tên thất bại. Reply: {}", ftp.getReplyString());
            throw new IOException("Không thể đổi tên: " + ftp.getReplyString());
        }
        log.info("Đổi tên thành công: '{}' -> '{}'", fromPath, toPath);
    }

    public void downloadFile(FTPClient ftp, String remoteFile, OutputStream output) throws IOException {
        log.debug("Đang tải file: '{}'", remoteFile);
        if (!ftp.retrieveFile(remoteFile, output)) {
            log.warn("Tải file thất bại: '{}'. Reply: {}", remoteFile, ftp.getReplyString());
            throw new IOException("❌ Không thể tải file: " + remoteFile);
        }
        log.info("Tải file thành công: '{}'", remoteFile);
    }

    public void uploadFile(FTPClient ftp, String remoteFile, InputStream input) throws IOException {
        log.debug("Đang upload file tới: '{}'", remoteFile);
        if (!ftp.storeFile(remoteFile, input)) {
            log.warn("Upload file thất bại: '{}'. Reply: {}", remoteFile, ftp.getReplyString());
            throw new IOException("❌ Upload thất bại: " + remoteFile);
        }
        log.info("Upload file thành công: '{}'", remoteFile);
    }

    public String downloadFileAsString(FTPClient ftp, String remoteFile) throws IOException {
        log.debug("Đang tải file (dạng text): '{}'", remoteFile);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (!ftp.retrieveFile(remoteFile, outputStream)) {
                log.warn("Tải file (dạng text) thất bại: '{}'. Reply: {}", remoteFile, ftp.getReplyString());
                throw new IOException("Không thể đọc file: " + remoteFile);
            }
            log.info("Tải file (dạng text) thành công: '{}'", remoteFile);
            return outputStream.toString(StandardCharsets.UTF_8);
        }
    }

    public void saveFileFromString(FTPClient ftp, String remoteFile, String content) throws IOException {
        log.debug("Đang lưu file (dạng text) tới: '{}'", remoteFile);
        try (InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            if (!ftp.storeFile(remoteFile, inputStream)) {
                log.warn("Lưu file (dạng text) thất bại: '{}'. Reply: {}", remoteFile, ftp.getReplyString());
                throw new IOException("❌ Lưu file thất bại: " + remoteFile);
            }
        }
        log.info("Lưu file (dạng text) thành công: '{}'", remoteFile);
    }

//    public void deleteFileOrDirectory(FTPClient ftp, String path) throws IOException {
//        log.debug("Đang xóa item: '{}'", path);
//        boolean deleted;
//        if (ftp.deleteFile(path)) {
//            deleted = true;
//            log.info("Đã xóa FILE: '{}'", path);
//        } else {
//            deleted = ftp.removeDirectory(path); // Chỉ xóa nếu thư mục rỗng
//            if (deleted) {
//                log.info("Đã xóa THƯ MỤC RỖNG: '{}'", path);
//            }
//        }
//        if (!deleted) {
//            log.warn("Xóa thất bại: '{}'. Có thể thư mục không rỗng. Reply: {}", path, ftp.getReplyString());
//            throw new IOException("Không thể xóa file hoặc thư mục (thư mục có thể không rỗng).");
//        }
//    }
    /**
     * Xóa file hoặc thư mục (kể cả thư mục không rỗng).
     * Tự động gọi hàm xóa đệ quy nếu là thư mục.
     */
    public void deleteItemRecursively(FTPClient ftp, String path) throws IOException {
        log.info("Yêu cầu xóa item (có thể đệ quy): '{}'", path);

        // Kiểm tra xem path là file hay thư mục
        // Note: listFiles(path) sẽ trả về null nếu path không tồn tại hoặc lỗi quyền
        //       Nó sẽ trả về mảng rỗng nếu là file
        //       Nó sẽ trả về mảng chứa nội dung nếu là thư mục
        FTPFile[] files = ftp.listFiles(path);

        // Trường hợp 1: Path không tồn tại hoặc lỗi quyền
        if (files == null) {
            // Thử xóa như một file xem sao (có thể là file nhưng listFiles bị lỗi)
            if (ftp.deleteFile(path)) {
                log.info("Đã xóa FILE (sau khi listFiles trả về null): '{}'", path);
                return;
            } else {
                log.warn("Không thể xác định loại hoặc xóa item: '{}'. Reply: {}", path, ftp.getReplyString());
                throw new IOException("Không thể xóa: '" + path + "'. Item không tồn tại hoặc lỗi quyền.");
            }
        }
        // Trường hợp 2: Path là một FILE (listFiles trả về mảng rỗng nhưng không phải null)
        // Hoặc đôi khi server trả về chính file đó trong mảng 1 phần tử? Kiểm tra kỹ hơn
        else if (files.length == 0 || (files.length == 1 && files[0].isFile() && files[0].getName().equals(Paths.get(path).getFileName().toString()))) {
            if (ftp.deleteFile(path)) {
                log.info("Đã xóa FILE: '{}'", path);
            } else {
                // Có thể là thư mục rỗng mà listFiles không liệt kê được? Thử xóa thư mục.
                if (ftp.removeDirectory(path)) {
                    log.info("Đã xóa THƯ MỤC RỖNG (sau khi thử xóa file thất bại): '{}'", path);
                } else {
                    log.warn("Xóa FILE '{}' thất bại. Reply: {}", path, ftp.getReplyString());
                    throw new IOException("Không thể xóa file: " + path);
                }
            }
        }
        // Trường hợp 3: Path là một THƯ MỤC (có nội dung hoặc rỗng)
        else {
            log.debug("Phát hiện '{}' là một thư mục. Bắt đầu xóa đệ quy...", path);
            deleteDirectoryRecursively(ftp, path);
            log.info("Đã xóa THƯ MỤC (và nội dung) thành công: '{}'", path);
        }
    }

    /**
     * Hàm trợ giúp: Xóa thư mục và toàn bộ nội dung bên trong nó (đệ quy).
     */
    private void deleteDirectoryRecursively(FTPClient ftp, String parentDir) throws IOException {
        log.debug("Đang xóa nội dung bên trong: '{}'", parentDir);
        FTPFile[] files = ftp.listFiles(parentDir);

        if (files != null && files.length > 0) {
            for (FTPFile file : files) {
                if (file == null) continue; // Bỏ qua nếu có lỗi

                String currentPath = parentDir.equals("/") ? "/" + file.getName() : parentDir + "/" + file.getName();

                if (file.isDirectory()) {
                    // Nếu là thư mục con -> gọi đệ quy để xóa nó trước
                    log.debug("Tìm thấy thư mục con '{}', gọi đệ quy...", currentPath);
                    deleteDirectoryRecursively(ftp, currentPath);
                } else {
                    // Nếu là file -> xóa file
                    log.debug("Đang xóa file con: '{}'", currentPath);
                    if (!ftp.deleteFile(currentPath)) {
                        log.warn("Xóa file con '{}' thất bại. Reply: {}", currentPath, ftp.getReplyString());
                        // Có thể ném lỗi ở đây để dừng lại, hoặc bỏ qua và tiếp tục
                        // throw new IOException("Không thể xóa file con: " + currentPath);
                    }
                }
            }
        } else if (files == null){
            log.warn("Không thể listFiles bên trong '{}' khi đang xóa đệ quy. Reply: {}", parentDir, ftp.getReplyString());
            // Có thể thư mục này không còn tồn tại hoặc lỗi quyền. Thử xóa luôn xem sao.
        }

        // Sau khi đã xóa hết nội dung bên trong (hoặc nếu thư mục rỗng)
        log.debug("Đang xóa thư mục cha (giờ đã rỗng): '{}'", parentDir);
        if (!ftp.removeDirectory(parentDir)) {
            log.error("Xóa thư mục cha '{}' thất bại sau khi đã xóa nội dung! Reply: {}", parentDir, ftp.getReplyString());
            throw new IOException("Lỗi nghiêm trọng: Không thể xóa thư mục cha " + parentDir);
        }
    }

    public void makeDirectory(FTPClient ftp, String dirPath) throws IOException {
        log.debug("Đang tạo thư mục: '{}'", dirPath);
        boolean success = ftp.makeDirectory(dirPath);
        if (!success) {
            // Bỏ qua lỗi 550 (File exists)
            if (ftp.getReplyCode() == 550) {
                log.debug("Thư mục '{}' đã tồn tại, bỏ qua tạo mới.", dirPath);
            } else {
                log.warn("Tạo thư mục '{}' thất bại. Reply: {}", dirPath, ftp.getReplyString());
                throw new IOException("Không thể tạo thư mục: " + dirPath + " (Lỗi: " + ftp.getReplyString() + ")");
            }
        } else {
            log.info("Đã tạo thư mục thành công: '{}'", dirPath);
        }
    }
}