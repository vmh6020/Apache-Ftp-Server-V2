package com.kev.ftpserver.model;

public class FileItem {
    private final String name;
    private final long size;
    private final boolean isDirectory;
    private final String path; // Thêm path tạo link download

    public FileItem(String name, long size, boolean isDirectory, String path) {
        this.name = name;
        this.size = size;
        this.isDirectory = isDirectory;
        this.path = path;
    }

    public String getName() { return name; }
    public long getSize() { return size; }
    public boolean isDirectory() { return isDirectory; }
    public String getPath() { return path; }
}