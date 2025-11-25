package com.threadcity.jacketshopbackend.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.threadcity.jacketshopbackend.service.auth.JwtService;
import com.threadcity.jacketshopbackend.service.auth.UserDetailsServiceImpl;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        final String token = extractTokenFromRequset(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            final String username = jwtService.extractUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authentication set for user: {} with authorities: {}",
                            username, userDetails.getAuthorities());
                } else {
                    log.warn("Invalid token for user: {}", username);
                }
            }
        } catch (ExpiredJwtException e) {
            log.error("JWT token expired: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Token has expired");
            return;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Token format not supported");
            return;

        } catch (MalformedJwtException e) {
            log.error("Malformed JWT token: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid token format");
            return;

        } catch (UsernameNotFoundException e) {
            log.error("User not found: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "User not found");
            return;

        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Authentication error");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequset(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");

        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status); 
        response.setContentType("application/json;charset=UTF-8");

        String jsonResponse = String.format(
                "{\"status\":%d,\"error\":\"%s\",\"timestamp\":%d,\"path\":\"%s\"}",
                status,
                message,
                System.currentTimeMillis(),
                response.getHeader("X-Request-Path"));

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

}
