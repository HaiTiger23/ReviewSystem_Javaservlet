package com.example.api.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter xử lý CORS (Cross-Origin Resource Sharing) cho phép truy cập API từ các domain khác
 */
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
        
        // Cho phép truy cập từ bất kỳ nguồn nào
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        
        // Cho phép các header cụ thể
        httpResponse.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization");
        
        // Cho phép các phương thức HTTP
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        
        // Cho phép cookie
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        
        // Xử lý yêu cầu OPTIONS
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        // Không cần xử lý gì
    }
}
