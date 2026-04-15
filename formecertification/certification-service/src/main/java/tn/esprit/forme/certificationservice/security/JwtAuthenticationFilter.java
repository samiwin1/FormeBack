package tn.esprit.forme.certificationservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SecretKey secretKey;

    public JwtAuthenticationFilter(@Value("${app.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
            Long uid = extractUserId(claims);
            String subject = claims.getSubject();
            Set<String> roles = extractRoles(claims);

            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            JwtPrincipal principal = new JwtPrincipal(uid, subject, roles);
            var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (ExpiredJwtException | SecurityException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private Long extractUserId(Claims claims) {
        Object uidClaim = claims.get("uid");
        if (uidClaim instanceof Number number) {
            return number.longValue();
        }
        if (uidClaim instanceof String uidStr) {
            try {
                return Long.parseLong(uidStr);
            } catch (NumberFormatException ignored) {
            }
        }

        String subject = claims.getSubject();
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractRoles(Claims claims) {
        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof Collection<?> collection) {
            Set<String> roles = new HashSet<>();
            for (Object role : collection) {
                String normalized = normalizeRole(String.valueOf(role));
                if (!normalized.isBlank()) {
                    roles.add(normalized);
                }
            }
            return roles;
        }

        if (rolesClaim instanceof String rolesString && !rolesString.isBlank()) {
            String[] split = rolesString.split(",");
            Set<String> roles = new HashSet<>();
            for (String role : split) {
                String normalized = normalizeRole(role);
                if (!normalized.isBlank()) {
                    roles.add(normalized);
                }
            }
            return roles;
        }

        Object roleClaim = claims.get("role");
        if (roleClaim != null && !String.valueOf(roleClaim).isBlank()) {
            String normalized = normalizeRole(String.valueOf(roleClaim));
            if (!normalized.isBlank()) {
                return Set.of(normalized);
            }
        }

        return Set.of();
    }

    private String normalizeRole(String rawRole) {
        String role = rawRole == null ? "" : rawRole.trim();
        if (role.isEmpty()) {
            return role;
        }
        if (role.startsWith("ROLE_")) {
            return role;
        }
        return "ROLE_" + role;
    }
}
