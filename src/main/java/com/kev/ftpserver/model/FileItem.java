package com.kev.ftpserver.model;

import java.util.Set;

// Biến nó thành abstract class
public abstract class FileItem {
    // Các thuộc tính chung mà File và Folder đều có
    protected final String name;
    protected final String path;

    // Danh sách các đuôi file có thể sửa (chỉ dùng cho FileObject)
    protected static final Set<String> BINARY_EXTENSIONS = Set.of(
            // Hình ảnh
            ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".ico", ".webp",
            // Âm thanh / Video
            ".mp3", ".mp4", ".wav", ".ogg", ".avi", ".mkv", ".mov",
            // Nén
            ".zip", ".rar", ".7z", ".gz", ".tar",
            // Thực thi / compiled
            ".exe", ".dll", ".jar", ".war", ".class", ".o",
            // Font
            ".ttf", ".woff", ".woff2",
            // Tài liệu phức tạp
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx"
    );

    public FileItem(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() { return name; }
    public String getPath() { return path; }
    public abstract String getIcon();
    public abstract String getLink();
    public abstract String getSizeDisplay();
    public abstract boolean isDirectory();
    public abstract boolean isEditable();
}