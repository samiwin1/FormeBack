package tn.esprit.events_service.util;

import tn.esprit.events_service.dto.ViewerRole;

public final class RequestHeaderUtil {

    private RequestHeaderUtil() {
    }

    public static ViewerRole parseViewerRole(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return ViewerRole.USER;
        }
        try {
            return ViewerRole.valueOf(headerValue.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ViewerRole.USER;
        }
    }
}
