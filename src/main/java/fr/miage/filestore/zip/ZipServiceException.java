package fr.miage.filestore.zip;

public class ZipServiceException extends Exception {
    public ZipServiceException(String message) {
        super(message);
    }

    public ZipServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
