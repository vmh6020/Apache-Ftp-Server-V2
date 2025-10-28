package com.kev.ftpserver.model;

public class FileObject extends FileItem {
    private final long size;

    public FileObject(String name, String path, long size) {
        super(name, path); // Gọi cha để lưu name và path
        this.size = size;
    }

    @Override
    public String getIcon() {
        return "📄";
    }

    @Override
    public String getLink() {
        return "download?file=" + getPath();
    }

    @Override
    public String getSizeDisplay() {
        return String.valueOf(this.size);
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isEditable() {
        String nameLower = this.name.toLowerCase();

        // Duyệt qua "Danh sách ĐEN"
        for (String ext : BINARY_EXTENSIONS) {
            if (nameLower.endsWith(ext)) {
                return false; // Phát hiện file nhị phân! CẤM SỬA!
            }
        }
        return true;
    }
}