package com.threadcity.jacketshopbackend.filter;

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

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        final String token = extractTokenFromRequest(request);
        final String path = request.getRequestURI();

        try {
            if (token == null) {
                chain.doFilter(request, response);
                return;
            }

            if (!jwtService.isSignatureValid(token) || jwtService.isTokenExpired(token)) {
                SecurityContextHolder.clearContext();
                throw new AuthenticationException("Invalid or expired token") {
                };
            }

            if (jwtService.isRefreshToken(token)) {
                if (isRefreshEndpoint(path)) {
                    chain.doFilter(request, response);
                } else {
                    SecurityContextHolder.clearContext();
                    throw new AuthenticationException("Refresh token is not allowed for this endpoint") {
                    };
                }
                return;
            }

            if (jwtService.isAccessToken(token)) {
                final String username = jwtService.extractUsername(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (!jwtService.isTokenValid(token, userDetails)) {
                        SecurityContextHolder.clearContext();
                        throw new AuthenticationException("Token subject mismatch or user disabled") {
                        };
                    }

                    var auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
                chain.doFilter(request, response);
                return;
            }

            SecurityContextHolder.clearContext();
            throw new AuthenticationException("Unknown token type") {
            };

        } catch (UsernameNotFoundException e) {
            log.warn("User not found: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            throw new AuthenticationException("User not found") {
            };
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Authentication processing error", e);
            SecurityContextHolder.clearContext();
            throw new AuthenticationException("Authentication error") {
            };
        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        final String auth = request.getHeader("Authorization");
        return (auth != null && auth.startsWith("Bearer ")) ? auth.substring(7) : null;
    }

    private boolean isRefreshEndpoint(String path) {
        return "/api/auth/refresh".equals(path);
    }
}
