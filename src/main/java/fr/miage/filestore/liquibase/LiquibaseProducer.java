package fr.miage.filestore.liquibase;

import liquibase.integration.cdi.CDILiquibaseConfig;
import liquibase.integration.cdi.annotations.LiquibaseType;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

import javax.annotation.Resource;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.logging.Logger;
@Dependent
public class LiquibaseProducer  {

    private static final Logger LOGGER = Logger.getLogger(LiquibaseProducer.class.getName());

    @Resource(lookup="java:jboss/datasources/FilestoreDS")
    private DataSource fsDS;

    @Produces
    @LiquibaseType
    public CDILiquibaseConfig createConfig()  {
        LOGGER.info("Building liquibase config");
        CDILiquibaseConfig config = new CDILiquibaseConfig();
        config.setChangeLog("liquibase/migration/db-changelog.xml");
        return config;
    }

    @Produces
    @LiquibaseType
    public DataSource createDataSource() throws SQLException {
        return fsDS;
    }

    @Produces
    @LiquibaseType
    public ResourceAccessor create()  {
        return  new ClassLoaderResourceAccessor(getClass().getClassLoader());
    }

}