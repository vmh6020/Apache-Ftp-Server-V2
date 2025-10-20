package com.kev.ftpserver.servlet;

import com.kev.ftpserver.ftp.FTPClientWrapper;
import com.kev.ftpserver.model.FTPAccount;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.file.Paths; // Dùng Paths để ghép đường dẫn

@WebServlet("/rename")
public class RenameServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        FTPAccount account = (session != null) ? (FTPAccount) session.getAttribute("account") : null;

        if (account == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập");
            return;
        }

        String oldPath = request.getParameter("oldPath");
        String newName = request.getParameter("newName");
        String currentPath = request.getParameter("currentPath"); // Path của thư mục cha

        if (oldPath == null || newName == null || currentPath == null || newName.isBlank()) {
            session.setAttribute("file-error", "Tên mới không được để trống.");
            response.sendRedirect("files?path=" + currentPath);
            return;
        }

        // Tạo path mới.
        // Paths.get(currentPath, newName).toString() sẽ xử lý dấu /
        // replace("\\", "/") để đảm bảo chuẩn FTP (dùng /)
//        String newPath = Paths.get(currentPath, newName).toString().replace("\\", "/");
        String newPath = currentPath.equals("/") ? "/" + newName : currentPath + "/" + newName;

        try {
            FTPClientWrapper.renameFile(account, oldPath, newPath);
            // Thành công, quay lại thư mục cha (không báo gì cả)
            response.sendRedirect("files?path=" + currentPath);

        } catch (IOException e) {
            // Thất bại, quay lại và báo lỗi
            session.setAttribute("file-error", "Lỗi đổi tên: " + e.getMessage());
            response.sendRedirect("files?path=" + currentPath);
        }
    }
}