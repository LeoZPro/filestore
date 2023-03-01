package fr.miage.filestore.store;

import fr.miage.filestore.store.exception.BinaryStoreServiceException;
import fr.miage.filestore.store.exception.BinaryStreamNotFoundException;
import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

//@RunWith(Arquillian.class)
public class BinaryStoreTest {

    private static final Logger LOGGER = Logger.getLogger(BinaryStoreTest.class.getName());

    //@EJB
    private BinaryStore store;

    //@Deployment
    public static Archive createDeployment() throws Exception {
        WebArchive archive = ShrinkWrap.create(WebArchive.class);
        archive.addPackage("fr.miage.filestore.store");
        archive.addPackage("fr.miage.filestore.store.exception");
        archive.addPackage("fr.miage.filestore.store.hash");
        archive.addPackage("fr.miage.filestore.config");
        archive.addAsResource("test-beans.xml", "META-INF/beans.xml");
        File[] file = Maven.resolver().loadPomFromFile("pom.xml")
                .importCompileAndRuntimeDependencies()
                .resolve("commons-io:commons-io:2.11.0")
                .withTransitivity().asFile();
        archive.addAsLibraries(file);
        return archive;
    }

    //@Test
    public void simpleCreateFileTest() throws BinaryStoreServiceException, BinaryStreamNotFoundException, IOException {
        LOGGER.log(Level.INFO, "Starting Simple Create File Test");
        String content = "This is a test";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
        String KEY = store.put(inputStream);
        LOGGER.log(Level.INFO, "File stored with key: " + KEY);
        assertNotNull(KEY);
        assertTrue(store.exists(KEY));
        InputStream inputStream1 = store.get(KEY);
        assertEquals(14, store.size(KEY));
        String retrieved = new String(IOUtils.toByteArray(inputStream1));
        assertEquals(content, retrieved);

        inputStream = new ByteArrayInputStream(content.getBytes());
        String KEY2 = store.put(inputStream);
        assertEquals(KEY, KEY2);
    }

}
