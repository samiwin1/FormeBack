package tn.esprit.formation_service.exception;

public class MaxAttemptsExceededException extends RuntimeException {

    public MaxAttemptsExceededException(String message) {
        super(message);
    }
}
