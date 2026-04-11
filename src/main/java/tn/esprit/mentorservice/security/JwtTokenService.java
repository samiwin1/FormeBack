package tn.esprit.mentorservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Service
public class JwtTokenService {

    private final Key key;

    public JwtTokenService(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public MentorPrincipal parsePrincipal(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        if (claims.getExpiration().before(new Date())) {
            throw new IllegalArgumentException("Token expired");
        }

        Number uidNum = claims.get("uid", Number.class);
        if (uidNum == null) {
            throw new IllegalArgumentException("Missing uid claim");
        }
        long userId = uidNum.longValue();

        String email = claims.getSubject();
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Missing subject");
        }

        Collection<GrantedAuthority> authorities = extractRoles(claims);
        return new MentorPrincipal(userId, email, authorities);
    }

    @SuppressWarnings("unchecked")
    private static Collection<GrantedAuthority> extractRoles(Claims claims) {
        Object rolesObj = claims.get("roles");
        List<GrantedAuthority> out = new ArrayList<>();
        if (rolesObj instanceof List<?> list) {
            for (Object o : list) {
                if (o != null) {
                    String r = o.toString();
                    if (!r.startsWith("ROLE_")) {
                        r = "ROLE_" + r;
                    }
                    out.add(new SimpleGrantedAuthority(r));
                }
            }
        }
        if (out.isEmpty()) {
            out.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return out;
    }
}
