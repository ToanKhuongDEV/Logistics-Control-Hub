package com.logistics.hub.feature.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtils {

    @Value("${spring.jwt.secret}")
    private String secretKey;

    @Value("${spring.jwt.refresh-token.secret}")
    private String refreshSecretKey;

    @Value("${spring.jwt.expiration}")
    private long jwtExpiration;

    @Value("${spring.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    // ======================== Access Token ========================

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, getSignInKey());
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration, getSignInKey(), SignatureAlgorithm.HS256);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token, getSignInKey());
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ======================== Refresh Token ========================

    public String extractUsernameFromRefreshToken(String token) {
        return extractClaim(token, Claims::getSubject, getRefreshSignInKey());
    }

    public String extractJtiFromRefreshToken(String token) {
        return extractClaim(token, Claims::getId, getRefreshSignInKey());
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration, getRefreshSignInKey(),
                SignatureAlgorithm.HS512);
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsernameFromRefreshToken(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token, getRefreshSignInKey());
    }

    private Key getRefreshSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(refreshSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Trả về thời điểm hết hạn của refresh token (now + refreshExpiration).
     * Dùng để lưu expiresAt trong DB.
     */
    public Date getRefreshTokenExpirationDate() {
        return new Date(System.currentTimeMillis() + refreshExpiration);
    }

    /**
     * Trả về thời gian sống của refresh token tính bằng giây.
     * Dùng để set maxAge cho cookie.
     */
    public long getRefreshExpirationSeconds() {
        return refreshExpiration / 1000;
    }

    // ======================== Common ========================

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration, Key key,
            SignatureAlgorithm algorithm) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setId(UUID.randomUUID().toString())
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, algorithm)
                .compact();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver, Key key) {
        final Claims claims = extractAllClaims(token, key);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token, Key key) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token, Key key) {
        return extractExpiration(token, key).before(new Date());
    }

    private Date extractExpiration(String token, Key key) {
        return extractClaim(token, Claims::getExpiration, key);
    }
}
