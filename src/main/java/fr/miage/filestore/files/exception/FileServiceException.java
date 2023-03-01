package fr.miage.filestore.files.exception;

public class FileServiceException extends Exception {

    public FileServiceException() {
        super();
    }

    public FileServiceException(String message) {
        super(message);
    }

    public FileServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
