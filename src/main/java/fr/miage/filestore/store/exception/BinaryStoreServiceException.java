package fr.miage.filestore.store.exception;

public class BinaryStoreServiceException extends Exception {

    public BinaryStoreServiceException(String message) {
        super(message);
    }

    public BinaryStoreServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
