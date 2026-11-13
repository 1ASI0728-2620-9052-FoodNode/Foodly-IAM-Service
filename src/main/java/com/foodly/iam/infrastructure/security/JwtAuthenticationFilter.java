package com.foodly.iam.infrastructure.security;

import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtTokenIssuer jwtTokenIssuer;

    public JwtAuthenticationFilter(
            JwtTokenIssuer jwtTokenIssuer
    ) {
        this.jwtTokenIssuer = jwtTokenIssuer;
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {
        String path = request.getServletPath();

        return path.equals("/iam/auth/register")
                || path.equals("/iam/auth/login")
                || path.equals("/actuator/health");
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization =
                request.getHeader("Authorization");

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7).trim();

        JwtTokenIssuer.AuthenticatedUser user;

        try {
            user = jwtTokenIssuer.validate(token);
        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid or expired token"
            );
            return;
        }

        var authority = new SimpleGrantedAuthority(
                "ROLE_" + user.role()
        );

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        user.userId().toString(),
                        null,
                        List.of(authority)
                );

        try {
            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
