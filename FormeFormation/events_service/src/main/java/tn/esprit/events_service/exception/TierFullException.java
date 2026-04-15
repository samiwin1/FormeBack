package tn.esprit.events_service.exception;

/**
 * Raised when a partner tier has reached its maximum capacity.
 */
public class TierFullException extends BusinessException {

    public TierFullException(String message) {
        super(message);
    }
}
