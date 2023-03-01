package fr.miage.filestore.index;

import java.util.List;

public interface IndexStoreService {

    void index(IndexStoreObject object) throws IndexStoreException;

    void remove(String identifier) throws IndexStoreException;

    List<IndexStoreResult> search(String scope, String query) throws IndexStoreException;

}
