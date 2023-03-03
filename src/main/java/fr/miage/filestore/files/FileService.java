package fr.miage.filestore.files;

import fr.miage.filestore.files.entity.Node;
import fr.miage.filestore.files.exception.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public interface FileService {

    List<Node> list(String id) throws NodeNotFoundException;

    List<Node> path(String id) throws NodeNotFoundException;

    long size(String id) throws NodeNotFoundException;

    Node get(String id) throws NodeNotFoundException;

    InputStream getContent(String id) throws NodeNotFoundException;

    Node add(String id, String name) throws NodeNotFoundException, NodeAlreadyExistsException, NodeTypeException, FileServiceException;

    Node add(String id, String name, InputStream content) throws NodeNotFoundException, NodeAlreadyExistsException, NodeTypeException, ContentException, FileServiceException;

    void remove(String id, String name) throws NodeNotFoundException, NodeNotEmptyException, FileServiceException;

    List<Node> search(String query) throws FileServiceException;

    void updateFileStatistique(String id) throws NodeNotFoundException;

    void updateFolderStatistique(String id) throws NodeNotFoundException;

    Long countAllNode() throws NodeNotFoundException;

    Long countAllDownload() throws NodeNotFoundException;
    Long countAllFiles() throws NodeNotFoundException;
    Long countAllFolders() throws NodeNotFoundException;

    HashMap<String, Long> countAllMimetype() throws NodeNotFoundException;
}
