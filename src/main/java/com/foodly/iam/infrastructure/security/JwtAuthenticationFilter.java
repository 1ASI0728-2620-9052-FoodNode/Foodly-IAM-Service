package com.foodly.iam.infrastructure.security;

import com.foodly.iam.domain.model.enums.Role;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenIssuer jwtTokenIssuer;

    public JwtAuthenticationFilter(JwtTokenIssuer jwtTokenIssuer) {
        this.jwtTokenIssuer = jwtTokenIssuer;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        boolean publicAuthEndpoint =
                HttpMethod.POST.matches(method)
                        && (path.equals("/iam/auth/register")
                        || path.equals("/iam/auth/login"));

        boolean healthEndpoint =
                HttpMethod.GET.matches(method)
                        && path.equals("/actuator/health");

        return publicAuthEndpoint || healthEndpoint;
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

        if (token.isEmpty()) {
            rejectRequest(response);
            return;
        }

        JwtTokenIssuer.AuthenticatedUser user;
        Role role;

        try {
            user = jwtTokenIssuer.validate(token);

            if (user.role() == null) {
                throw new IllegalArgumentException(
                        "JWT role is missing"
                );
            }

            role = Role.valueOf(user.role());

        } catch (JwtException | IllegalArgumentException exception) {
            rejectRequest(response);
            return;
        }

        var authority = new SimpleGrantedAuthority(
                "ROLE_" + role.name()
        );

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        user.userId().toString(),
                        null,
                        List.of(authority)
                );

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        try {
            SecurityContextHolder.setContext(context);
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void rejectRequest(
            HttpServletResponse response
    ) throws IOException {

        SecurityContextHolder.clearContext();

        response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Invalid or expired token"
        );
    }
}
