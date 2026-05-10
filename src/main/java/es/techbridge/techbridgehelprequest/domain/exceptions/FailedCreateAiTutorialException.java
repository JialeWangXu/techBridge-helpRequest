package es.techbridge.techbridgehelprequest.domain.exceptions;

public class FailedCreateAiTutorialException extends RuntimeException {
    private static final String DESCRIPTION = "Error at creating Ai tutorial. ";
    public FailedCreateAiTutorialException(String message) {
        super(DESCRIPTION+message);
    }
}
