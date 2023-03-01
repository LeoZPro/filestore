package fr.miage.filestore.store;

import fr.miage.filestore.store.exception.BinaryStoreServiceException;
import fr.miage.filestore.store.exception.BinaryStreamNotFoundException;

import java.io.InputStream;

public interface BinaryStore {

    boolean exists(String key) throws BinaryStoreServiceException;

    String put(InputStream is) throws BinaryStoreServiceException;

    InputStream get(String key) throws BinaryStoreServiceException, BinaryStreamNotFoundException;

    long size(String key) throws BinaryStoreServiceException, BinaryStreamNotFoundException;

    String type(String key, String name) throws BinaryStoreServiceException, BinaryStreamNotFoundException;

    String extract(String key, String name, String type) throws BinaryStoreServiceException, BinaryStreamNotFoundException;

    void delete(String key);

}
