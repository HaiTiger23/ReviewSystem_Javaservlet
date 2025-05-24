package com.example.api.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter("/*")
public class CorsResponseFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // Khởi tạo filter
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // Lấy origin từ request
        String origin = httpRequest.getHeader("Origin");
        
        // Kiểm tra origin có hợp lệ không
        if (origin != null && (origin.startsWith("http://localhost:") || 
                              origin.startsWith("https://yourdomain.com") ||
                              origin.startsWith("http://localhost:3000"))) {
            httpResponse.setHeader("Access-Control-Allow-Origin", origin);
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
            httpResponse.setHeader("Access-Control-Allow-Methods", 
                "GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
            httpResponse.setHeader("Access-Control-Allow-Headers", 
                "Origin, X-Requested-With, Content-Type, Accept, Authorization, X-XSRF-TOKEN, X-Requested-With, X-Auth-Token");
            httpResponse.setHeader("Access-Control-Expose-Headers", 
                "Content-Disposition, Authorization, X-Auth-Token");
            httpResponse.setHeader("Access-Control-Max-Age", "3600");
        }
        
        // Tiếp tục xử lý các request khác
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Hủy filter
    }
}
