package com.kev.ftpserver.servlet;

import com.kev.ftpserver.ftp.FTPClientWrapper;
import com.kev.ftpserver.model.FTPAccount;
import com.kev.ftpserver.model.FileItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;

@WebServlet("/files") // URL mới để hiển thị file
public class FileBrowserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession(false);

        // Lấy FTPAccount từ session
        FTPAccount account = (session != null) ? (FTPAccount) session.getAttribute("account") : null;

        if (account != null) {
            try {
                // Lấy path hiện tại (ví dụ: ?path=/docs)
                String path = request.getParameter("path");
                if (path == null || path.isBlank()) {
                    path = "/"; // Path gốc
                }

                // Gọi Wrapper để lấy List<FileItem>
                List<FileItem> files = FTPClientWrapper.listFiles(
                        account.getServer(),
                        account.getPort(),
                        account.getUsername(),
                        account.getPassword(),
                        path
                );

                // Gửi danh sách file và path hiện tại sang JSP
                request.setAttribute("files", files);
                request.setAttribute("currentPath", path);

                if (!path.equals("/")) {
                    Path nioPath = Paths.get(path);
                    String parentPath = getString(nioPath);
                    request.setAttribute("parentPath", parentPath);
                }
                // Forward sang jsp/files.jsp
                request.getRequestDispatcher("/jsp/files.jsp").forward(request, response);

            } catch (IOException e) {
                // Nếu có lỗi (ví dụ: sai user/pass trong session), đá về trang login
                session.invalidate(); // Xóa session hỏng
                request.setAttribute("login-error", "Phiên hết hạn hoặc lỗi kết nối: " + e.getMessage());
                request.getRequestDispatcher("/jsp/index.jsp").forward(request, response);
            }
        } else {
            // Không có session, về trang login
            response.sendRedirect("jsp/index.jsp");
        }
    }

    private static String getString(Path nioPath) {
        Path parentNioPath = nioPath.getParent();

        // Nếu parentNioPath là null (nghĩa là đang ở thư mục /) thì mặc định là /
        // Nếu không thì chuyển nó về string và chuẩn hóa dấu \ thành /
        String parentPath = (parentNioPath != null) ? parentNioPath.toString().replace("\\", "/") : "/";

        // Trường hợp đặc biệt: parent của /foo là "" (empty), cần đổi thành /
        if (parentPath.isEmpty()) {
            parentPath = "/";
        }
        return parentPath;
    }
}