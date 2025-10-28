package com.kev.ftpserver.model;

public class FolderObject extends FileItem {

    public FolderObject(String name, String path) {
        super(name, path); // Chỉ cần name và path
    }
    @Override
    public String getIcon() {
        return "📁"; // Icon thư mục
    }
    @Override
    public String getLink() {
        return "files?path=" + getPath();
    }        // Hành động chính của Folder là DUYỆT (browse)

    @Override
    public String getSizeDisplay() {
        return "";
    }        // Thư mục không có kích thước, trả về rỗng
    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public boolean isEditable() {
        return false;
    }
}