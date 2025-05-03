package com.example.api.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.util.Date;

/**
 * Lu1edbp tiu1ec7n u00edch xu1eed lu00fd JWT (JSON Web Token)
 */
public class JwtUtil {
    private static final byte[] JWT_SECRET = Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded();
    private static final long JWT_EXPIRATION = 86400000; // 24 giu1edd
    
    /**
     * Tu1ea1o JWT token cho ngu01b0u1eddi du00f9ng
     * 
     * @param userId ID cu1ee7a ngu01b0u1eddi du00f9ng
     * @return JWT token
     */
    public static String generateToken(int userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);
        
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(JWT_SECRET))
                .compact();
    }
    
    /**
     * Xu00e1c thu1ef1c vu00e0 lu1ea5y thu00f4ng tin tu1eeb JWT token
     * 
     * @param token JWT token
     * @return Claims chu1ee9a thu00f4ng tin token
     */
    public static Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(JWT_SECRET))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * Lu1ea5y ID ngu01b0u1eddi du00f9ng tu1eeb JWT token
     * 
     * @param token JWT token
     * @return ID ngu01b0u1eddi du00f9ng hou1eb7c null nu1ebfu token khu00f4ng hu1ee3p lu1ec7
     */
    public static Integer getUserIdFromToken(String token) {
        try {
            Claims claims = validateToken(token);
            return Integer.parseInt(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Lu1ea5y JWT_SECRET u0111u1ec3 su1eed du1ee5ng trong cu00e1c lu1edbp khu00e1c
     * 
     * @return JWT_SECRET
     */
    public static byte[] getJwtSecret() {
        return JWT_SECRET;
    }
}
