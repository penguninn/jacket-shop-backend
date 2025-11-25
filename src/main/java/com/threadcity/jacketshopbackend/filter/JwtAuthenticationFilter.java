package com.threadcity.jacketshopbackend.filter;

import com.threadcity.jacketshopbackend.exception.AuthenticationEntryPointImpl;
import com.threadcity.jacketshopbackend.service.auth.JwtService;
import com.threadcity.jacketshopbackend.service.auth.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationEntryPointImpl authenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        final String token = extractToken(request);

        if (token == null) {
            chain.doFilter(request, response);
            return;
        }

        try {
            if (!jwtService.isSignatureValid(token) || jwtService.isTokenExpired(token)) {
                SecurityContextHolder.clearContext();
                authenticationEntryPoint.commence(
                    request, response,
                    new AuthenticationException("Invalid or expired token") {}
                );
                return;
            }

            if (jwtService.isRefreshToken(token)) {
                if (isRefreshEndpoint(request.getRequestURI())) {
                    chain.doFilter(request, response);
                } else {
                    SecurityContextHolder.clearContext();
                    authenticationEntryPoint.commence(
                        request, response,
                        new AuthenticationException("Refresh token is not allowed for this endpoint") {}
                    );
                }
                return;
            }

            if (jwtService.isAccessToken(token)) {
                final String username = jwtService.extractUsername(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails user = userDetailsService.loadUserByUsername(username);
                    if (!jwtService.isTokenValid(token, user)) {
                        SecurityContextHolder.clearContext();
                        authenticationEntryPoint.commence(
                            request, response,
                            new AuthenticationException("Token subject mismatch or user disabled") {}
                        );
                        return;
                    }
                    var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
                chain.doFilter(request, response);
                return;
            }

            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(
                request, response,
                new AuthenticationException("Unknown token type") {}
            );

        } catch (UsernameNotFoundException e) {
            log.warn("User not found: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(
                request, response,
                new AuthenticationException("User not found") {}
            );
        } catch (Exception e) {
            log.error("Authentication processing error", e);
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(
                request, response,
                new AuthenticationException("Authentication error") {}
            );
        }
    }

    private String extractToken(HttpServletRequest request) {
        final String auth = request.getHeader("Authorization");
        return (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : null;
    }

    private boolean isRefreshEndpoint(String path) {
        return "/api/auth/refresh".equals(path);
    }
}

