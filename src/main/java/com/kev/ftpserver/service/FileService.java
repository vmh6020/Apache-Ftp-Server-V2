package com.kev.ftpserver.service;

import com.kev.ftpserver.model.FileItem;
import com.kev.ftpserver.repository.FtpRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.Part;
import org.apache.commons.net.ftp.FTPClient;
// === THÊM IMPORT LOGGER ===
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// ==========================

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

/**
 * Lớp Bộ não: Chịu trách nhiệm cho logic nghiệp vụ liên quan đến File.
 * Đã được nâng cấp Logger (Sổ sách).
 */
@ApplicationScoped
public class FileService {

    // === TẠO 1 "CUỐN SỔ" RIÊNG CHO CLASS NÀY ===
    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    @Inject
    private FtpRepository ftpRepository;

    public List<FileItem> listFiles(FTPClient ftp, String path) throws IOException {
        log.info("FileService: Yêu cầu lấy danh sách file tại path: '{}'", path);
        return ftpRepository.listFiles(ftp, path);
    }

    public String getParentPath(String path) {
        if (path == null || path.equals("/")) {
            return "/";
        }
        Path nioPath = Paths.get(path);
        Path parentNioPath = nioPath.getParent();
        String parentPath = (parentNioPath != null) ? parentNioPath.toString().replace("\\", "/") : "/";
        if (parentPath.isEmpty()) {
            parentPath = "/";
        }
        log.debug("FileService: Đã tính toán parent path của '{}' là '{}'", path, parentPath);
        return parentPath;
    }

    public void downloadFile(FTPClient ftp, String remoteFile, OutputStream output) throws IOException {
        log.info("FileService: Yêu cầu tải file: '{}'", remoteFile);
        ftpRepository.downloadFile(ftp, remoteFile, output);
    }

    public void deleteItem(FTPClient ftp, String path) throws IOException {
        log.info("FileService: Yêu cầu xóa item: '{}'", path);
        ftpRepository.deleteItemRecursively(ftp, path);
    }

    public void renameItem(FTPClient ftp, String oldPath, String currentPath, String newName) throws IOException {
        String newPath = currentPath.equals("/") ? "/" + newName : currentPath + "/" + newName;
        log.info("FileService: Yêu cầu đổi tên: '{}' -> '{}'", oldPath, newPath);
        ftpRepository.renameFile(ftp, oldPath, newPath);
    }

    public String getFileContent(FTPClient ftp, String path) throws IOException {
        log.info("FileService: Yêu cầu đọc nội dung file: '{}'", path);
        return ftpRepository.downloadFileAsString(ftp, path);
    }

    public void saveFileContent(FTPClient ftp, String path, String content) throws IOException {
        log.info("FileService: Yêu cầu lưu nội dung file: '{}'", path);
        ftpRepository.saveFileFromString(ftp, path, content);
    }

    /**
     * Logic Upload phức tạp (từ UploadServlet)
     */
    /**
     * Logic Upload phức tạp (từ UploadServlet)
     * Đã sửa lỗi không tạo thư mục gốc khi upload folder.
     */
    public void uploadFiles(FTPClient ftp, String currentPath, Collection<Part> parts) throws IOException, SecurityException {
        log.info("FileService: Yêu cầu upload (có thể là thư mục) vào path: '{}'", currentPath);
        int fileCount = 0;
        int skippedCount = 0;
        // Dùng Set để tránh gọi makeDirectory lặp lại cho cùng 1 thư mục
        java.util.Set<String> createdDirs = new java.util.HashSet<>();

        for (Part filePart : parts) {
            String submittedFileName = filePart.getSubmittedFileName();

            if (submittedFileName == null || submittedFileName.isBlank()) {
                continue;
            }
            log.debug("FileService: Đang xử lý file upload: '{}'", submittedFileName);

            // Chuẩn hóa và kiểm tra Path Traversal (giữ nguyên)
            Path submittedPath;
            try {
                // Quan trọng: Chuẩn hóa dấu \ thành / TRƯỚỚC KHI tạo Path
                submittedPath = Paths.get(submittedFileName.replace("\\", "/"));
            } catch (InvalidPathException e) {
                log.warn("FileService: Tên file upload không hợp lệ, bỏ qua: '{}'", submittedFileName, e);
                skippedCount++;
                continue;
            }
            String safeFileNameOnly = submittedPath.getFileName().toString(); // Chỉ lấy tên file cuối: "a.txt"
            if (!submittedPath.normalize().equals(submittedPath) || submittedFileName.contains("..")) {
                log.error("!!! TẤN CÔNG BẢO MẬT !!! Phát hiện Path Traversal: '{}'", submittedFileName);
                throw new SecurityException("Phát hiện tấn công Path Traversal: " + submittedFileName);
            }

            // === PHẦN SỬA LỖI LOGIC ===

            // 1. Xác định đường dẫn tương đối (ví dụ: "MyFolder/Sub")
            String relativePathStr = "";
            Path parentInSubmitted = submittedPath.getParent();
            if (parentInSubmitted != null) {
                relativePathStr = parentInSubmitted.toString().replace("\\", "/"); // "MyFolder/Sub"
            }

            // 2. Tạo đường dẫn thư mục ĐẦY ĐỦ trên FTP server
            // Ví dụ: currentPath = "/docs", relativePathStr = "MyFolder/Sub"
            // => finalDirPath = "/docs/MyFolder/Sub"
            String finalDirPath = currentPath;
            if (!relativePathStr.isEmpty()) {
                finalDirPath = currentPath.equals("/") ? "/" + relativePathStr : currentPath + "/" + relativePathStr;
            }


            // 3. Tạo thư mục ĐỆ QUY (nếu chưa tạo)
            // Chúng ta sẽ tạo từ gốc đi vào để đảm bảo thư mục cha tồn tại
            if (!relativePathStr.isEmpty() && !createdDirs.contains(finalDirPath)) {
                log.debug("FileService: Đang tạo (các) thư mục (nếu chưa có): '{}'", finalDirPath);
                String[] dirsToCreate = relativePathStr.split("/");
                String currentBuildPath = currentPath;
                for (String dir : dirsToCreate) {
                    if (dir.isEmpty()) continue;
                    currentBuildPath = currentBuildPath.equals("/") ? "/" + dir : currentBuildPath + "/" + dir;
                    // Chỉ gọi makeDirectory nếu chưa tạo thư mục này trong lần lặp này
                    if (!createdDirs.contains(currentBuildPath)) {
                        try {
                            ftpRepository.makeDirectory(ftp, currentBuildPath);
                            createdDirs.add(currentBuildPath); // Đánh dấu đã tạo
                        } catch (IOException e) {
                            // Ghi log lỗi tạo thư mục nhưng vẫn tiếp tục thử upload file
                            log.error("FileService: Lỗi khi tạo thư mục '{}'. File '{}' có thể không được upload đúng vị trí.", currentBuildPath, safeFileNameOnly, e);
                            // Không ném lỗi ra ngoài vội, thử upload xem sao
                        }
                    }
                }
                createdDirs.add(finalDirPath); // Đánh dấu thư mục cuối cùng đã được xử lý
            }

            // 4. Tạo đường dẫn file cuối cùng để upload
            String remoteFileFullPath = finalDirPath.equals("/") ? "/" + safeFileNameOnly : finalDirPath + "/" + safeFileNameOnly;
            log.debug("FileService: Đang upload file: '{}'", remoteFileFullPath);

            // === KẾT THÚC SỬA LỖI LOGIC ===

            try (InputStream input = filePart.getInputStream()) {
                ftpRepository.uploadFile(ftp, remoteFileFullPath, input);
                fileCount++;
            } catch (IOException uploadEx) {
                log.error("FileService: Lỗi khi upload file '{}'", remoteFileFullPath, uploadEx);
                skippedCount++;
            }
        } // Hết vòng lặp for parts

        if(skippedCount > 0) {
            log.warn("FileService: Upload hoàn tất nhưng đã bỏ qua {} file do lỗi hoặc tên không hợp lệ.", skippedCount);
        } else {
            log.info("FileService: Upload hoàn tất. Đã tải lên {} file.", fileCount);
        }
    }
}