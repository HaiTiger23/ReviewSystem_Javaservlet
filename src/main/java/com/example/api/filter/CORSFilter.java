package com.example.api.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter xử lý CORS (Cross-Origin Resource Sharing) cho phép truy cập API từ các domain khác
 */
@WebFilter("/*")
public class CORSFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Không cần xử lý gì
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Lấy origin từ request header
        String origin = httpRequest.getHeader("Origin");
        
        // Kiểm tra origin có hợp lệ không
        boolean isValidOrigin = origin != null && 
                             (origin.startsWith("http://localhost:") || 
                              origin.startsWith("https://yourdomain.com") ||
                              origin.startsWith("http://localhost:3000"));
        
        if (isValidOrigin) {
            httpResponse.setHeader("Access-Control-Allow-Origin", origin);
        }
        
        // Cho phép các header cụ thể
        httpResponse.setHeader("Access-Control-Allow-Headers", 
            "Origin, X-Requested-With, Content-Type, Accept, Authorization, X-XSRF-TOKEN, X-Requested-With, X-Auth-Token");
        
        // Cho phép các phương thức HTTP
        httpResponse.setHeader("Access-Control-Allow-Methods", 
            "GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
        
        // Cho phép gửi cookie và xác thực
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        
        // Thời gian cache preflight (1 giờ)
        httpResponse.setHeader("Access-Control-Max-Age", "3600");
        
        // Expose các header tùy chỉnh
        httpResponse.setHeader("Access-Control-Expose-Headers", 
            "Content-Disposition, Authorization, X-Auth-Token");
        
        // Xử lý yêu cầu OPTIONS (preflight)
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        // Tiếp tục xử lý các request khác
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Không cần xử lý gì
    }
}
