package tn.esprit.forme.certificationservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import tn.esprit.forme.certificationservice.exception.BusinessException;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtPrincipal principal)) {
            throw new BusinessException("Authenticated user context is missing");
        }
        if (principal.getUserId() == null) {
            throw new BusinessException("JWT does not contain a numeric user id");
        }
        return principal.getUserId();
    }

    public static String currentSubject() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtPrincipal principal)) {
            return "unknown";
        }
        return principal.getSubject() == null ? "unknown" : principal.getSubject();
    }
}
