package fr.miage.filestore.zip;

import com.google.common.net.HostAndPort;
import com.orbitz.consul.Consul;
import com.orbitz.consul.NotRegisteredException;
import com.orbitz.consul.model.agent.Registration;
import fr.miage.filestore.api.resource.FilesResource;
import fr.miage.filestore.files.FileService;
import fr.miage.filestore.files.entity.Node;
import fr.miage.filestore.files.exception.*;
import fr.miage.filestore.store.BinaryStore;
import fr.miage.filestore.store.exception.BinaryStoreServiceException;
import fr.miage.filestore.store.exception.BinaryStreamNotFoundException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Singleton
@Startup
public class ZipServiceBean implements ZipService {

    private static final Logger LOGGER = Logger.getLogger(ZipService.class.getName());

    @EJB
    private FileService files;

    @EJB
    private BinaryStore store;

    @Override
    public Node zip(String id) throws NodeNotFoundException {

        LOGGER.log(Level.INFO, "Zip initiate for " + id);

        Node node = files.get(id);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if (node.isFolder()) {

            try(ZipOutputStream zipOut = new ZipOutputStream(baos)) {

                zipRecursively("", node.getId(), zipOut);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {

            try(ZipOutputStream zipOut = new ZipOutputStream(baos)) {

                addZipEntry("", node, zipOut);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        ByteArrayInputStream inStream = new ByteArrayInputStream( baos.toByteArray() );

        LOGGER.log(Level.INFO, id + " --- zipped --> " + id);
        try {

            return files.add(node.getParent(), node.getName() + ".zip", inStream);

        } catch (NodeAlreadyExistsException e) {
            throw new RuntimeException(e);
        } catch (NodeTypeException e) {
            throw new RuntimeException(e);
        } catch (ContentException e) {
            throw new RuntimeException(e);
        } catch (FileServiceException e) {
            throw new RuntimeException(e);
        }
    }

    private void addZipEntry(String path, Node node, ZipOutputStream zos) throws IOException {
        ZipEntry zipEntry = new ZipEntry(path + node.getName());
        zos.putNextEntry(zipEntry);

        InputStream is;
        try {
            is = store.get(node.getContentId());
        } catch (BinaryStreamNotFoundException e) {
            throw new RuntimeException(e);
        } catch (BinaryStoreServiceException e) {
            throw new RuntimeException(e);
        }

        byte[] bytes = new byte[1024];
        int length;
        while((length = is.read(bytes)) >= 0) {

            System.out.println("---");
            zos.write(bytes, 0, length);

        }
    }

    private void zipRecursively(String path, String parentFolderId, ZipOutputStream zos) throws NodeNotFoundException, IOException {

        List<Node> nodes = files.list(parentFolderId);

        System.out.println(nodes.size());

        for (Node node : nodes) {

            if (node.isFolder()) {

                zipRecursively(path + node.getName() + "/", node.getId(), zos);

            } else {

                addZipEntry(path, node, zos);

            }
        }
    }

    @Override
    public List<Node> unzip(String id) throws ZipServiceException {

        LOGGER.log(Level.INFO, "UnZip initiate for " + id);

        // Todo: get content of id

        // Todo: unzip all of contents

        // Todo: upload all files

        // Todo: add files to stores

        return null;
    }
}
