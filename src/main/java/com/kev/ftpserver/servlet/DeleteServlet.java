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

@WebServlet("/delete")
public class DeleteServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        FTPAccount account = (session != null) ? (FTPAccount) session.getAttribute("account") : null;

        if (account == null) {
            response.sendRedirect("jsp/index.jsp"); // Hết phiên, đá về login
            return;
        }

        // Lấy đường dẫn file cần xóa
        String pathToDelete = request.getParameter("path");
        // Lấy đường dẫn thư mục cha (để quay về)
        String currentPath = request.getParameter("currentPath");

        if (pathToDelete == null || currentPath == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu thông tin file cần xóa.");
            return;
        }

        try {
            // Gọi hàm xóa
            FTPClientWrapper.deleteFileOrDirectory(account, pathToDelete);
        } catch (IOException e) {
            // Nếu xóa lỗi (ví dụ: thư mục không rỗng), gửi lỗi về
            session.setAttribute("file-error", "Lỗi xóa: " + e.getMessage());
        }

        // Quay về trang 'files' tại thư mục cha
        response.sendRedirect("files?path=" + currentPath);
    }
}