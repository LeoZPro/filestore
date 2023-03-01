package fr.miage.filestore.config;

import java.nio.file.Path;

public interface FileStoreConfig {

    String owner();

    Path home();

    InstanceConfig instance();

    ConsulConfig consul();

    interface ConsulConfig {

        boolean https();

        String host();

        int port();

    }

    interface InstanceConfig {

        boolean https();

        String host();

        int port();

        String ctx();

    }
}
