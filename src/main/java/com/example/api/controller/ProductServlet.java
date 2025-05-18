package com.example.api.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servlet xử lý các yêu cầu API liên quan đến sản phẩm
 */
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 10)
public class ProductServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ProductController productController;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        super.init();
        productController = new ProductController();
        gson = new Gson();
    }

    @Override

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Thiết lập response type là JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Lấy đường dẫn URI
        String pathInfo = request.getPathInfo();

        // Kiểm tra nếu đường dẫn chứa "/reviews" thì chuyển cho ReviewServlet xử lý
        if (pathInfo != null && pathInfo.contains("/reviews")) {
            // Chuyển cho ReviewServlet xử lý
            request.getRequestDispatcher("/api/reviews" + pathInfo).forward(request, response);
            return;
        }

        // Xử lý endpoint lấy danh sách sản phẩm đã bookmark
        System.out.println("pathInfo: " + pathInfo);
        if (pathInfo != null && pathInfo.equals("/bookmark-user")) {
            Map<String, Object> result = productController.getBookmarkedProducts(request, response);
            sendJsonResponse(response, result);
            return;
        }

        // Xử lý các endpoint khác nhau
        Map<String, Object> result;

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Lấy danh sách sản phẩm
                result = productController.getProducts(request, response);
            } else {
                // Lấy chi tiết sản phẩm theo ID
                try {
                    int productId = Integer.parseInt(pathInfo.substring(1));
                    result = productController.getProductById(request, response, productId);
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    result = Map.of("error", "ID sản phẩm không hợp lệ");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result = Map.of("error", "Lỗi server: " + e.getMessage());
            e.printStackTrace();
        }

        // Trả về kết quả dưới dạng JSON
        sendJsonResponse(response, result);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Thiết lập response type là JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Lấy đường dẫn URI
        String pathInfo = request.getPathInfo();

        // Kiểm tra nếu đường dẫn chứa "/reviews" thì chuyển cho ReviewServlet xử lý
        if (pathInfo != null && pathInfo.contains("/reviews")) {
            // Chuyển cho ReviewServlet xử lý
            request.getRequestDispatcher("/api/reviews" + pathInfo).forward(request, response);
            return;
        }
        // Xử lý thêm bookmark

        String servletPath = request.getServletPath();
        System.out.println("[" + servletPath + "]");
        System.out.println("[" + pathInfo + "]");

        if (servletPath.equals("/api/products") && pathInfo != null && pathInfo.matches("^/bookmark/\\d+$")) {
            // Tách số từ pathInfo bằng regex
            Pattern pattern = Pattern.compile("^/bookmark/(\\d+)$");
            Matcher matcher = pattern.matcher(pathInfo);

            if (matcher.find()) {
                int productId = Integer.parseInt(matcher.group(1));
                System.out.println("productId: " + productId);
                Map<String, Object> result = productController.addBookmark(productId, request, response);
                try (PrintWriter out = response.getWriter()) {
                    out.print(gson.toJson(result));
                    out.flush();
                }
            }
        }

        // Kiểm tra Content-Type
        String contentType = request.getContentType();
        Map<String, Object> result;

        try {
            if (contentType != null && contentType.startsWith("application/json")) {
                // Xử lý yêu cầu JSON
                JsonObject jsonRequest = parseJsonRequest(request);
                result = productController.addProductJson(jsonRequest, request, response);
            } else {
                // Xử lý yêu cầu multipart/form-data
                result = productController.addProduct(request, response);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result = Map.of("error", "Lỗi server: " + e.getMessage());
            e.printStackTrace();
        }

        // Trả về kết quả dưới dạng JSON
        sendJsonResponse(response, result);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Thiết lập response type là JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Lấy đường dẫn URI
        String pathInfo = request.getPathInfo();

        // Kiểm tra nếu đường dẫn chứa "/reviews" thì chuyển cho ReviewServlet xử lý
        if (pathInfo != null && pathInfo.contains("/reviews")) {
            // Chuyển cho ReviewServlet xử lý
            request.getRequestDispatcher("/api/reviews" + pathInfo).forward(request, response);
            return;
        }

        // Xử lý yêu cầu cập nhật sản phẩm
        Map<String, Object> result;

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result = Map.of("error", "Thiếu ID sản phẩm");
            } else {
                try {
                    int productId = Integer.parseInt(pathInfo.substring(1));
                    result = productController.updateProduct(request, response, productId);
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    result = Map.of("error", "ID sản phẩm không hợp lệ");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result = Map.of("error", "Lỗi server: " + e.getMessage());
            e.printStackTrace();
        }

        // Trả về kết quả dưới dạng JSON
        sendJsonResponse(response, result);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Thiết lập response type là JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Lấy đường dẫn URI
        String pathInfo = request.getPathInfo();

        // Kiểm tra nếu đường dẫn chứa "/reviews" thì chuyển cho ReviewServlet xử lý
        if (pathInfo != null && pathInfo.contains("/reviews")) {
            // Chuyển cho ReviewServlet xử lý
            request.getRequestDispatcher("/api/reviews" + pathInfo).forward(request, response);
            return;
        }

        // Xử lý yêu cầu xóa sản phẩm
        Map<String, Object> result;

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result = Map.of("error", "Thiếu ID sản phẩm");
            } else {
                try {
                    int productId = Integer.parseInt(pathInfo.substring(1));
                    result = productController.deleteProduct(request, response, productId);
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    result = Map.of("error", "ID sản phẩm không hợp lệ");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result = Map.of("error", "Lỗi server: " + e.getMessage());
            e.printStackTrace();
        }

        // Trả về kết quả dưới dạng JSON
        sendJsonResponse(response, result);
    }

    /**
     * Phân tích dữ liệu JSON từ request
     * 
     * @param request Yêu cầu HTTP
     * @return Đối tượng JsonObject
     * @throws IOException nếu có lỗi khi đọc dữ liệu
     */
    private JsonObject parseJsonRequest(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;

        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        String requestBody = sb.toString();
        if (requestBody.isEmpty()) {
            return null;
        }

        return JsonParser.parseString(requestBody).getAsJsonObject();
    }

    private void sendJsonResponse(HttpServletResponse response, Map<String, Object> data) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(data));
            out.flush();
        }
    }
}
