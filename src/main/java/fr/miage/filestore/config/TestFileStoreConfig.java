package fr.miage.filestore.config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@Alternative
@ApplicationScoped
public class TestFileStoreConfig implements FileStoreConfig {

    private static final Logger LOGGER = Logger.getLogger(TestFileStoreConfig.class.getName());

    private Path home;
    private ConsulConfig consul;

    private InstanceConfig instance;

    public TestFileStoreConfig() {
    }

    @PostConstruct
    public void init() {
        LOGGER.log(Level.INFO, "Initializing config");
        home = Paths.get("/tmp/filestore/" + UUID.randomUUID().toString());
        LOGGER.log(Level.INFO, "Filestore home set to: " + home);
        consul = new LocalConsulConfig();
        instance = new LocalInstanceConfig();
    }

    @Override
    public Path home() {
        return home;
    }

    @Override
    public String owner() {
        return "testuser";
    }

    @Override
    public ConsulConfig consul() {
        return consul;
    }

    @Override
    public InstanceConfig instance() {
        return instance;
    }

    static class LocalConsulConfig implements ConsulConfig {

        @Override
        public boolean https() {
            return false;
        }

        @Override
        public String host() {
            return "localhost";
        }

        @Override
        public int port() {
            return 8500;
        }
    }

    static class LocalInstanceConfig implements InstanceConfig {

        @Override
        public boolean https() {
            return false;
        }

        @Override
        public String host() {
            return "localhost";
        }

        @Override
        public int port() {
            return 8180;
        }

        @Override
        public String ctx() {
            return "/fs";
        }
    }
}

