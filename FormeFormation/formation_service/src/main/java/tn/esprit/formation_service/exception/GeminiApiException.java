package tn.esprit.formation_service.exception;

/**
 * Thrown when the Gemini API call fails (e.g. 503 high demand, invalid key).
 * The message is forwarded to the client for display.
 */
public class GeminiApiException extends RuntimeException {

    public GeminiApiException(String message) {
        super(message);
    }

    public GeminiApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
