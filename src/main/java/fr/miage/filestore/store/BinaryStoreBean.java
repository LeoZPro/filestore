package fr.miage.filestore.store;

import fr.miage.filestore.config.FileStoreConfig;
import fr.miage.filestore.store.exception.BinaryStoreServiceException;
import fr.miage.filestore.store.exception.BinaryStreamNotFoundException;
import fr.miage.filestore.store.hash.HashedFilterInputStream;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class BinaryStoreBean implements BinaryStore {

    private static final Logger LOGGER = Logger.getLogger(BinaryStore.class.getName());
    public static final String BINARY_DATA_HOME = "binary";

    @Inject
    FileStoreConfig config;

    private Path base;
    private Tika tika;

    public BinaryStoreBean() {
    }

    @PostConstruct
    public void init() {
        this.base = Paths.get(config.home().toString(), BINARY_DATA_HOME);
        LOGGER.log(Level.FINEST, "Initializing store with base folder: " + base);
        try {
            Files.createDirectories(base);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "unable to initialize binary store", e);
        }
        this.tika = new Tika();
    }

    @Override
    public boolean exists(String key) throws BinaryStoreServiceException {
        Path file = Paths.get(base.toString(), key);
        return Files.exists(file);
    }

    @Override
    public String put(InputStream is) throws BinaryStoreServiceException {
        String tmpkey = UUID.randomUUID().toString();
        Path tmpfile = Paths.get(base.toString(), tmpkey);
        try (HashedFilterInputStream his = HashedFilterInputStream.SHA256(is)) {
            Files.copy(his, tmpfile, StandardCopyOption.REPLACE_EXISTING);
            String key = his.getHash();
            Path file = Paths.get(base.toString(), key);
            if ( !Files.exists(file) ) {
                Files.move(tmpfile, file);
            } else {
                Files.delete(tmpfile);
            }
            return key;
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new BinaryStoreServiceException("unexpected error during stream copy", e);
        }
    }

    @Override
    public InputStream get(String key) throws BinaryStoreServiceException, BinaryStreamNotFoundException {
        Path file = Paths.get(base.toString(), key);
        if ( !Files.exists(file) ) {
            throw new BinaryStreamNotFoundException("file not found in storage");
        }
        try {
            return Files.newInputStream(file, StandardOpenOption.READ);
        } catch (IOException e) {
            throw new BinaryStoreServiceException("unexpected error while opening stream", e);
        }
    }

    @Override
    public long size(String key) throws BinaryStoreServiceException, BinaryStreamNotFoundException {
        Path file = Paths.get(base.toString(), key);
        if ( !Files.exists(file) ) {
            throw new BinaryStreamNotFoundException("file not found in storage");
        }
        try {
            return Files.size(file);
        } catch (IOException e) {
            throw new BinaryStoreServiceException("unexpected error while getting stream size", e);
        }
    }


    @Override
    public String type(String key, String name) throws BinaryStoreServiceException, BinaryStreamNotFoundException {
        LOGGER.log(Level.FINE, "Extract type for key: " + key);
        Path file = Paths.get(base.toString(), key);
        if ( !Files.exists(file) ) {
            throw new BinaryStreamNotFoundException("file not found in storage");
        }
        String mimetype = MediaType.APPLICATION_OCTET_STREAM;
        try (InputStream stream = Files.newInputStream(file)) {
            mimetype = tika.detect(stream, name);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to detect mimetype: " + e.getMessage(), e);
        }
        return mimetype;
    }

    @Override
    public String extract(String key, String name, String type) throws BinaryStoreServiceException, BinaryStreamNotFoundException {
        LOGGER.log(Level.FINE, "Extract text for key: " + key);
        Path file = Paths.get(base.toString(), key);
        if ( !Files.exists(file) ) {
            throw new BinaryStreamNotFoundException("file not found in storage");
        }
        try (InputStream stream = Files.newInputStream(file)) {
            BodyContentHandler handler = new BodyContentHandler();
            AutoDetectParser parser = new AutoDetectParser();
            Metadata metadata = new Metadata();
            metadata.set(Metadata.CONTENT_TYPE, type);
            parser.parse(stream, handler, metadata);
            return handler.toString();
        } catch (IOException | SAXException | TikaException e) {
            throw new BinaryStoreServiceException("unexpected error while opening stream", e);
        }
    }

    @Override
    public void delete(String key) {
        //TODO Mark the file for deletion and include in periodic check of
    }

}
