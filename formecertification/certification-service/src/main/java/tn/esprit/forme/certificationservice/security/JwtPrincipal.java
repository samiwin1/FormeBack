package tn.esprit.forme.certificationservice.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor

public class JwtPrincipal {
    private final Long userId;
    private final String subject;
    private final Set<String> roles;
}
