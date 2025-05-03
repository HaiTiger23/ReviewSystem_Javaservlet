package com.example.api.controller;

import com.example.api.model.Category;
import com.example.api.util.JwtUtil;
import com.example.api.util.SlugUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet xu1eed lu00fd cu00e1c request API cho danh mu1ee5c
 */
public class CategoryServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CategoryController categoryController;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        categoryController = new CategoryController();
        gson = new Gson();
    }
    
    /**
     * Xu1eed lu00fd request GET u0111u1ec3 lu1ea5y danh mu1ee5c
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Thiu1ebft lu1eadp response type lu00e0 JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        Map<String, Object> result;
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // Lu1ea5y tu1ea5t cu1ea3 danh mu1ee5c
            result = categoryController.getAllCategories(request, response);
        } else {
            // Lu1ea5y danh mu1ee5c theo ID
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length > 1) {
                try {
                    int categoryId = Integer.parseInt(pathParts[1]);
                    result = categoryController.getCategoryById(categoryId, request, response);
                } catch (NumberFormatException e) {
                    result = new HashMap<>();
                    result.put("error", "ID danh mu1ee5c khu00f4ng hu1ee3p lu1ec7");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                result = new HashMap<>();
                result.put("error", "URL khu00f4ng hu1ee3p lu1ec7");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        
        // Tru1ea3 vu1ec1 ku1ebft quu1ea3 du01b0u1edbi du1ea1ng JSON
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(result));
            out.flush();
        }
    }
    
    /**
     * Xu1eed lu00fd request POST u0111u1ec3 thu00eam danh mu1ee5c mu1edbi
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Thiu1ebft lu1eadp response type lu00e0 JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Parse JSON tu1eeb request body
        JsonObject jsonRequest = parseJsonRequest(request);
        Map<String, Object> result;
        
        if (jsonRequest != null) {
            result = categoryController.addCategory(jsonRequest, request, response);
        } else {
            result = new HashMap<>();
            result.put("error", "Du1eef liu1ec7u khu00f4ng hu1ee3p lu1ec7");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        
        // Tru1ea3 vu1ec1 ku1ebft quu1ea3 du01b0u1edbi du1ea1ng JSON
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(result));
            out.flush();
        }
    }
    
    /**
     * Xu1eed lu00fd request PUT u0111u1ec3 cu1eadp nhu1eadt danh mu1ee5c
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Thiu1ebft lu1eadp response type lu00e0 JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        Map<String, Object> result = new HashMap<>();
        
        if (pathInfo != null && !pathInfo.equals("/")) {
            // Lu1ea5y ID danh mu1ee5c tu1eeb URL
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length > 1) {
                try {
                    int categoryId = Integer.parseInt(pathParts[1]);
                    
                    // Parse JSON tu1eeb request body
                    JsonObject jsonRequest = parseJsonRequest(request);
                    
                    if (jsonRequest != null) {
                        result = categoryController.updateCategory(categoryId, jsonRequest, request, response);
                    } else {
                        result.put("error", "Du1eef liu1ec7u khu00f4ng hu1ee3p lu1ec7");
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    }
                } catch (NumberFormatException e) {
                    result.put("error", "ID danh mu1ee5c khu00f4ng hu1ee3p lu1ec7");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                result.put("error", "URL khu00f4ng hu1ee3p lu1ec7");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            result.put("error", "Cu1ea7n chu1ec9 u0111u1ecbnh ID danh mu1ee5c");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        
        // Tru1ea3 vu1ec1 ku1ebft quu1ea3 du01b0u1edbi du1ea1ng JSON
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(result));
            out.flush();
        }
    }
    
    /**
     * Xu1eed lu00fd request DELETE u0111u1ec3 xu00f3a danh mu1ee5c
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Thiu1ebft lu1eadp response type lu00e0 JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        Map<String, Object> result = new HashMap<>();
        
        if (pathInfo != null && !pathInfo.equals("/")) {
            // Lu1ea5y ID danh mu1ee5c tu1eeb URL
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length > 1) {
                try {
                    int categoryId = Integer.parseInt(pathParts[1]);
                    result = categoryController.deleteCategory(categoryId, request, response);
                } catch (NumberFormatException e) {
                    result.put("error", "ID danh mu1ee5c khu00f4ng hu1ee3p lu1ec7");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                result.put("error", "URL khu00f4ng hu1ee3p lu1ec7");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            result.put("error", "Cu1ea7n chu1ec9 u0111u1ecbnh ID danh mu1ee5c");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        
        // Tru1ea3 vu1ec1 ku1ebft quu1ea3 du01b0u1edbi du1ea1ng JSON
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(result));
            out.flush();
        }
    }
    
    /**
     * Parse JSON tu1eeb request body
     */
    private JsonObject parseJsonRequest(HttpServletRequest request) throws IOException {
        StringBuilder buffer = new StringBuilder();
        String line;
        
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        }
        
        String data = buffer.toString();
        if (data.isEmpty()) {
            return null;
        }
        
        try {
            return JsonParser.parseString(data).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }
}
