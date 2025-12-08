package com.threadcity.jacketshopbackend.service.auth;

import com.threadcity.jacketshopbackend.common.Enums.TokenType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-ttl-ms}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-ttl-ms}")
    private long refreshTokenExpiration;

    @Value("${jwt.issuer}")
    private String issuer;

    // Extract claims
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (JwtException e) {
            log.error("Failed to extract username from token: {}", e.getMessage());
            return null;
        }
    }

    public String extractType(String token) {
        try {
            return extractClaim(token, claims -> claims.get("type", String.class));
        } catch (JwtException e) {
            log.error("Failed to extract type from token: {}", e.getMessage());
            return null;
        }
    }

    public String extractJti(String token) {
        try {
            return extractClaim(token, claims -> claims.get("jti", String.class));
        } catch (JwtException e) {
            log.error("Failed to extract JTI from token: {}", e.getMessage());
            return null;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .requireIssuer(issuer)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            throw new JwtException("Invalid token signature", e);
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new JwtException("Malformed token", e);
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            throw new JwtException("Token expired", e);
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
            throw new JwtException("Unsupported token", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
            throw new JwtException("Token claims empty", e);
        }
    }

    // Check tokens is valid
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);

            return username != null
                    && username.equals(userDetails.getUsername())
                    && !isTokenExpired(token)
                    && isSignatureValid(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractClaim(token, Claims::getExpiration);
            return expiration.before(new Date());
        } catch (JwtException e) {
            log.error("Failed to check token expiration: {}", e.getMessage());
            return true;
        }
    }

    public boolean isSignatureValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid signature: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // Check type of token
    public boolean isAccessToken(String token) {
        return TokenType.ACCESS.name().equals(extractType(token));
    }

    public boolean isRefreshToken(String token) {
        return TokenType.REFRESH.name().equals(extractType(token));
    }

    // Gennerate tokens
    public String generateAccessToken(UserDetails userDetails, String jti) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put("roles", roles);
        claims.put("type", TokenType.ACCESS.name());
        claims.put("jti", jti);
        return buildToken(claims, userDetails, accessTokenExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails, String jti) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", TokenType.REFRESH.name());
        claims.put("jti", jti);
        return buildToken(claims, userDetails, refreshTokenExpiration);
    }

    private String buildToken(Map<String, Object> claims, UserDetails userDetails, long expiration) {
        long currentTimeMillis = System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(currentTimeMillis + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
