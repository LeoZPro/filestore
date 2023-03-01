package fr.miage.filestore.files;

import fr.miage.filestore.auth.AuthenticationService;
import fr.miage.filestore.config.FileStoreConfig;
import fr.miage.filestore.files.entity.Node;
import fr.miage.filestore.files.exception.*;
import fr.miage.filestore.index.IndexStoreException;
import fr.miage.filestore.index.IndexStoreObject;
import fr.miage.filestore.index.IndexStoreResult;
import fr.miage.filestore.index.IndexStoreService;
import fr.miage.filestore.metrics.Metrics;
import fr.miage.filestore.metrics.MetricsServiceBean;
import fr.miage.filestore.notification.NotificationService;
import fr.miage.filestore.notification.NotificationServiceException;
import fr.miage.filestore.store.BinaryStore;
import fr.miage.filestore.store.exception.BinaryStoreServiceException;
import fr.miage.filestore.store.exception.BinaryStreamNotFoundException;
import org.apache.tika.Tika;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
@Startup
@Interceptors({MetricsServiceBean.class})
public class FileServiceBean implements FileService {

    private static final Logger LOGGER = Logger.getLogger(FileService.class.getName());

    @Inject
    private FileStoreConfig config;

    @EJB
    private AuthenticationService auth;

    @EJB
    private NotificationService notification;

    @EJB
    private IndexStoreService index;

    @EJB
    private BinaryStore store;

    @PersistenceContext(unitName="fsPU")
    private EntityManager em;

    public FileServiceBean() {
    }

    @PostConstruct
    private void init() {
        LOGGER.log(Level.INFO, "Initialising file service");
        LOGGER.log(Level.INFO, "Using home directory: " + config.home());
        boolean bootstrap = false;
        try {
            loadNode(Node.ROOT_ID);
        } catch (NodeNotFoundException e ) {
            bootstrap = true;
        }
        if ( bootstrap ) {
            synchronized (this) {
                LOGGER.log(Level.INFO, "Root node does not exists, applying bootstrap");
                Node node = new Node();
                node.setType(Node.Type.TREE);
                node.setName("");
                node.setId(Node.ROOT_ID);
                node.setMimetype(Node.TREE_MIMETYPE);
                em.persist(node);
                LOGGER.log(Level.INFO, "Bootstrap done, root node exists now.");
            }
        }
    }

    @Override
    public List<Node> list(String id) throws NodeNotFoundException {
        LOGGER.log(Level.FINE, "List children of node with id: " + id);
        Node node = loadNode(id);
        if ( node.isFolder() ) {
            TypedQuery<Node> query = em.createNamedQuery("Node.listChildren", Node.class).setParameter("parent", node.getId());
            List<Node> nodes = query.getResultList();
            LOGGER.log(Level.FINEST, "found " + nodes.size() + " children for node with id: " + node.getId());
            nodes.sort(new Node.NameComparatorAsc());
            return nodes;
        }
        return Collections.emptyList();
    }

    @Override
    public List<Node> path(String id) throws NodeNotFoundException {
        LOGGER.log(Level.FINE, "Get path for node with id: " + id);
        LOGGER.log(Level.FINE, "Get path of item: " + id);
        List<Node> nodes = new ArrayList<>();
        while (!id.equals(Node.ROOT_ID)) {
            Node node = loadNode(id);
            nodes.add(node);
            id = node.getParent();
        }
        Collections.reverse(nodes);
        LOGGER.log(Level.FINE, "path: " + nodes.stream().map(Node::getName).collect(Collectors.joining(" > ")));
        return nodes;
    }

    @Override
    public long size(String id) throws NodeNotFoundException {
        LOGGER.log(Level.FINE, "Get size for node with id: " + id);
        Node node = loadNode(id);
        return node.getSize();
    }

    @Override
    public Node get(String id) throws NodeNotFoundException {
        LOGGER.log(Level.FINE, "Get node with id: " + id);
        return loadNode(id);
    }

    @Override
    @Metrics(key = "download", type = Metrics.Type.INCREMENT)
    public InputStream getContent(String id) throws NodeNotFoundException {
        LOGGER.log(Level.FINE, "Get content for node with id: " + id);
        try {
            Node node = loadNode(id);
            if (node.getType().equals(Node.Type.BLOB)) {
                return store.get(node.getContentId());
            }
            return null;
        } catch (BinaryStoreServiceException | BinaryStreamNotFoundException e) {
            throw new NodeNotFoundException("Unable to find content for node with id: " + id, e);
        }
    }

    @Override
    public Node add(String id, String name) throws NodeNotFoundException, NodeAlreadyExistsException, NodeTypeException, FileServiceException {
        LOGGER.log(Level.FINE, "Add tree in node with id: " + id);
        Node parent = loadNode(id);
        if (parent.getType().equals(Node.Type.BLOB)) {
            throw new NodeTypeException("Unable to add children node to blob");
        }
        TypedQuery<Long> query = em.createNamedQuery("Node.countChildrenForName", Long.class).setParameter("parent", parent.getId()).setParameter("name", name);
        if (query.getSingleResult() > 0) {
            throw new NodeAlreadyExistsException("A node with name: " + name + " already exists as children of node with id: " + parent.getId());
        }
        try {
            parent.setSize(parent.getSize() + 1);
            Node node = new Node();
            node.setId(UUID.randomUUID().toString());
            node.setType(Node.Type.TREE);
            node.setParent(id);
            node.setName(name);
            node.setMimetype(Node.TREE_MIMETYPE);
            em.persist(node);
            parent.setModification(node.getModification());
            notification.throwEvent("folder.create", node.getId());
            notification.throwEvent("folder.update", parent.getId());
            return node;
        } catch (NotificationServiceException e) {
            throw new FileServiceException("Unable to add folder", e);
        }
    }

    @Override
    @Metrics(key = "upload", type = Metrics.Type.INCREMENT)
    public Node add(String id, String name, InputStream content) throws NodeNotFoundException, NodeAlreadyExistsException, NodeTypeException, ContentException, FileServiceException {
        LOGGER.log(Level.FINE, "Add blob in node with id: " + id);
        try {
            Node parent = loadNode(id);
            if (parent.getType().equals(Node.Type.BLOB)) {
                throw new NodeTypeException("Unable to add node in blob");
            }
            TypedQuery<Long> query = em.createNamedQuery("Node.countChildrenForName", Long.class).setParameter("parent", parent.getId()).setParameter("name", name);
            if ( query.getSingleResult() > 0 ) {
                throw new NodeAlreadyExistsException("A node with name: " + name + " already exists as children of node with id: " + parent.getId());
            }
            String key = store.put(content);
            String mimetype = store.type(key, name);
            parent.setSize(parent.getSize() + 1);
            Node node = new Node();
            node.setType(Node.Type.BLOB);
            node.setName(name);
            node.setParent(id);
            node.setId(UUID.randomUUID().toString());
            node.setMimetype(mimetype);
            node.setContentId(key);
            node.setSize(store.size(key));
            em.persist(node);
            parent.setModification(node.getModification());
            notification.throwEvent("file.create", node.getId());
            notification.throwEvent("folder.update", parent.getId());
            return node;
        } catch (BinaryStoreServiceException | BinaryStreamNotFoundException e) {
            throw new ContentException("Error in content", e);
        } catch (NotificationServiceException e) {
            throw new FileServiceException("Unable to add file", e);
        }
    }

    @Override
    public void remove(String id, String name) throws NodeNotFoundException, NodeNotEmptyException, FileServiceException {
        LOGGER.log(Level.FINE, "Remove item with name: " + name + " in parent with id: " + id);
        Node parent = loadNode(id);
        try {
            Optional<Node> node = em.createNamedQuery("Node.findChildrenForName", Node.class).setParameter("parent", parent.getId()).setParameter("name", name).getResultStream().findFirst();
            if (!node.isPresent()) {
                throw new NodeNotFoundException("A node with name: " + name + " was not found as children of node with id: " + parent.getId());
            }
            if (node.get().getType().equals(Node.Type.TREE)) {
                Long folderSize = em.createNamedQuery("Node.countChildren", Long.class).setParameter("parent", node.get().getId()).getSingleResult();
                if ( folderSize > 0 ) {
                    throw new NodeNotEmptyException("node is a tree and is not empty, unable to remove, purge before");
                }
            }
            String eventType = "folder.remove";
            if (node.get().getType().equals(Node.Type.BLOB)) {
                store.delete(node.get().getContentId());
                eventType = "file.remove";
            }
            parent.setSize(parent.getSize() - 1);
            parent.setModification(System.currentTimeMillis());
            em.remove(node.get());
            notification.throwEvent(eventType, node.get().getId());
            notification.throwEvent("folder.update", parent.getId());
        } catch (NotificationServiceException e ) {
            throw new FileServiceException("Unable to delete node", e);
        }
    }

    @Override
    @Metrics(key = "search", type = Metrics.Type.INCREMENT)
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Node> search(String query) throws FileServiceException {
        LOGGER.log(Level.FINE, "Searching items for query: " + query);
        try {
            String scope = (auth.getConnectedProfile().isOwner())? IndexStoreObject.Scope.PRIVATE.name(): IndexStoreObject.Scope.PUBLIC.name();
            List<IndexStoreResult> results = index.search(IndexStoreObject.Scope.PRIVATE.name(), query);
            return results.stream().map(res -> {
                Node node = em.find(Node.class, res.getIdentifier());
                if (node != null) {
                    node.setSearchResultScore(res.getScore());
                    node.setSearchResultExplanation(res.getExplain());
                }
                return node;
            }).filter(res -> res != null).collect(Collectors.toList());
        } catch (IndexStoreException e ) {
            throw new FileServiceException("Error while searching files", e);
        }
    }

    //INTERNAL OPERATIONS

    private Node loadNode(String id) throws NodeNotFoundException {
        if ( id == null || id.isEmpty() ) {
            id = Node.ROOT_ID;
        }
        Node node = em.find(Node.class, id);
        if (node == null) {
            throw new NodeNotFoundException("unable to find a item with id: " + id);
        }
        return node;
    }

    private Node loadNodeWithLock(String id) throws NodeNotFoundException {
        if ( id == null || id.isEmpty() ) {
            id = Node.ROOT_ID;
        }
        Node node = em.find(Node.class, id, LockModeType.WRITE);
        if (node == null) {
            throw new NodeNotFoundException("unable to find a item with id: " + id);
        }
        return node;
    }

}
