package es.techbridge.techbridgehelprequest.domain.exceptions;

public class FailUploadResourceException extends RuntimeException {
    private static final String DESCRIPTION = "Resource is failed at uploading";

    public FailUploadResourceException(String detail) {
        super(DESCRIPTION + ". " + detail);
    }

}
