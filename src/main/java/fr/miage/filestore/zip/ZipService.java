package fr.miage.filestore.zip;

import fr.miage.filestore.files.entity.Node;
import fr.miage.filestore.files.exception.NodeNotFoundException;

import java.util.List;

public interface ZipService {

    Node zip(String id) throws ZipServiceException, NodeNotFoundException;

    List<Node> unzip(String id) throws ZipServiceException;
}
