package fr.miage.filestore.config;

import org.apache.commons.text.CaseUtils;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Named;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@Default
@ApplicationScoped
public class LocalFileStoreConfig implements FileStoreConfig {

    private static final Logger LOGGER = Logger.getLogger(LocalFileStoreConfig.class.getName());

    private Path home;
    private String owner;
    private LocalInstanceConfig instance;
    private LocalConsulConfig consul;

    public LocalFileStoreConfig() {
    }

    @PostConstruct
    public void init() {
        LOGGER.log(Level.INFO, "Initializing config");
        home = Paths.get(getConfigValue("FILESTORE_HOME", System.getProperty("user.home").concat(File.separator).concat(".filestore")));
        LOGGER.log(Level.INFO, "home set to: " + home);
        owner = getConfigValue("FILESTORE_OWNER", "admin@admin.fr");
        LOGGER.log(Level.INFO, "owner set to: " + owner);
        consul = new LocalConsulConfig(Boolean.getBoolean(getConfigValue("FILESTORE_CONSUL_HTTPS", "false")),
                getConfigValue("FILESTORE_CONSUL_HOST", "localhost"),
                Integer.parseInt(getConfigValue("FILESTORE_CONSUL_PORT", "8500")));
        LOGGER.log(Level.INFO, "consul set to: " + consul);
        instance = new LocalInstanceConfig(Boolean.getBoolean(getConfigValue("FILESTORE_HTTPS", "false")),
                getConfigValue("FILESTORE_HOST", "localhost"),
                Integer.parseInt(getConfigValue("FILESTORE_PORT", "8180")),
                getConfigValue("FILESTORE_CTX", "/jayblanc-filestore-22.12.1-SNAPSHOT"));
        LOGGER.log(Level.INFO, "instance set to: " + instance);
    }

    @Override
    public InstanceConfig instance() {
        return instance;
    }

    @Override
    public Path home() {
        return home;
    }

    @Override
    public String owner() {
        return owner;
    }

    @Override
    public ConsulConfig consul() {
        return consul;
    }

    static class LocalConsulConfig implements ConsulConfig {

        private boolean https;
        private String host;
        private int port;

        public LocalConsulConfig(boolean https, String host, int port) {
            this.https = https;
            this.host = host;
            this.port = port;
        }

        @Override
        public boolean https() {
            return https;
        }

        @Override
        public String host() {
            return host;
        }

        @Override
        public int port() {
            return port;
        }

        @Override
        public String toString() {
            return "LocalConsulConfig{" +
                    "https=" + https +
                    ", host='" + host + '\'' +
                    ", port=" + port +
                    '}';
        }
    }

    static class LocalInstanceConfig implements InstanceConfig {

        private boolean https;
        private String host;
        private int port;

        private String ctx;

        public LocalInstanceConfig(boolean https, String host, int port, String ctx) {
            this.https = https;
            this.host = host;
            this.port = port;
            this.ctx = ctx;
        }

        @Override
        public boolean https() {
            return https;
        }

        @Override
        public String host() {
            return host;
        }

        @Override
        public int port() {
            return port;
        }

        @Override
        public String ctx() {
            return ctx;
        }

        @Override
        public String toString() {
            return "LocalInstanceConfig{" +
                    "https=" + https +
                    ", host='" + host + '\'' +
                    ", port=" + port +
                    ", ctx='" + ctx + '\'' +
                    '}';
        }
    }

    private String getConfigValue(String key, String defaultValue) {
        String camelCaseName = CaseUtils.toCamelCase(key, false, '_');
        LOGGER.log(Level.FINE, "camel case name for config key " + key + " -> " + camelCaseName);
        if ( System.getenv(key) != null ) {
            LOGGER.log(Level.FINE, "config value loaded from system environment: " + System.getenv(key));
            return System.getenv(key);
        } else if ( System.getProperty(camelCaseName) != null ) {
            LOGGER.log(Level.FINE, "config value loaded from system java property: " + System.getProperty(camelCaseName));
            return System.getProperty(camelCaseName);
        } else {
            LOGGER.log(Level.FINE, "config value default: " + defaultValue);
            return defaultValue;
        }
    }

}

