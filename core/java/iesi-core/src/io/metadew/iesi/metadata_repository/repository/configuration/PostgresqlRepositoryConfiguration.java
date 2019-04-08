package io.metadew.iesi.metadata_repository.repository.configuration;

import io.metadew.iesi.common.config.ConfigFile;
import io.metadew.iesi.framework.configuration.FrameworkSettingConfiguration;
import io.metadew.iesi.metadata_repository.repository.Repository;
import io.metadew.iesi.metadata_repository.repository.database.Database;
import io.metadew.iesi.metadata_repository.repository.database.PostgresqlDatabase;
import io.metadew.iesi.metadata_repository.repository.database.connection.PostgresqlDatabaseConnection;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PostgresqlRepositoryConfiguration extends RepositoryConfiguration {
    private final String jdbcConnectionStringFormat = "jdbc:postgresql://%s:%s/%s";

    private String jdbcConnectionString;
    private String host;
    private String port;
    private String name;
    private String schema;
    private String schemaUser;
    private String schemaUserPassword;
    private String writerUser;
    private String writerUserPassword;
    private String readerUser;
    private String readerUserPassword;


    public PostgresqlRepositoryConfiguration(ConfigFile configFile, FrameworkSettingConfiguration frameworkSettingConfiguration) {
        super(configFile, frameworkSettingConfiguration);
    }

    @Override
    void fromConfigFile(ConfigFile configFile, FrameworkSettingConfiguration frameworkSettingConfiguration) {
        // schema
        if (frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.schema").isPresent() &&
                configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.schema").get()).isPresent()) {
            schema = configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.schema").get()).get();
        }
        // set users and passwords
        if (frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.schema.user").isPresent() &&
                configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.schema.user").get()).isPresent()) {
            schemaUser = configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.schema.user").get()).get();
        }
        if (frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.schema.user.password").isPresent() &&
                configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.schema.user.password").get()).isPresent()) {
            schemaUserPassword = configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.schema.user.password").get()).get();
        }
        if (frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.schema.writer").isPresent() &&
                configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.schema.writer").get()).isPresent()) {
            writerUser = configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.schema.writer").get()).get();
        }
        if (frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.schema.writer.password").isPresent() &&
                configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.schema.writer.password").get()).isPresent()) {
            writerUserPassword = configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.schema.writer.password").get()).get();
        }
        if (frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.schema.reader").isPresent() &&
                configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.schema.reader").get()).isPresent()) {
            readerUser = configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.schema.reader").get()).get();
        }
        if (frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.schema.reader.password").isPresent() &&
                configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.schema.reader.password").get()).isPresent()) {
            readerUserPassword = configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.schema.reader.password").get()).get();
        }

        // get jdbc connection url
        if (frameworkSettingConfiguration.getSettingPath("metadata.repository.connection.string").isPresent() &&
                configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.connection.string").get()).isPresent()) {
            jdbcConnectionString = configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.connection.string").get()).get();
        } else if ((frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.host").isPresent() &&
                configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.host").get()).isPresent()) &&
                (frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.port").isPresent() &&
                        configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.port").get()).isPresent()) &&
                (frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.name").isPresent() &&
                        configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.name").get()).isPresent())) {
            host = configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.host").get()).get();
            port = configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.port").get()).get();
            name = configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.postgresql.name").get()).get();
            jdbcConnectionString = String.format(jdbcConnectionStringFormat, host, port, name);
        } else {
            throw new RuntimeException("Could not initialize Postgresql configuration. No connection string or host, port and name provided");
        }
    }

    @Override
    public Repository toRepository() {
        Map<String, Database> databases = new HashMap<>();

        getUser().ifPresent(owner -> {
            databases.put("owner", new PostgresqlDatabase(new PostgresqlDatabaseConnection(getJdbcConnectionString(), owner, getUserPassword().orElse("")), getSchema().orElse("")));
            databases.put("writer", new PostgresqlDatabase(new PostgresqlDatabaseConnection(getJdbcConnectionString(), owner, getUserPassword().orElse("")), getSchema().orElse("")));
            databases.put("reader", new PostgresqlDatabase(new PostgresqlDatabaseConnection(getJdbcConnectionString(), owner, getUserPassword().orElse("")), getSchema().orElse("")));
        });

        getWriter().ifPresent(writer -> {
            databases.put("writer", new PostgresqlDatabase(new PostgresqlDatabaseConnection(getJdbcConnectionString(), writer, getWriterPassword().orElse("")), getSchema().orElse("")));
            databases.put("reader", new PostgresqlDatabase(new PostgresqlDatabaseConnection(getJdbcConnectionString(), writer, getWriterPassword().orElse("")), getSchema().orElse("")));
        });

        getReader().ifPresent(reader -> databases.put("reader", new PostgresqlDatabase(new PostgresqlDatabaseConnection(getJdbcConnectionString(), reader, getReaderPassword().orElse("")), getSchema().orElse(""))));
        return new Repository(databases);
    }

    public String getJdbcConnectionString() {
        return jdbcConnectionString;
    }

    public Optional<String> getHost() {
        return Optional.ofNullable(host);
    }

    public Optional<String> getPort() {
        return Optional.ofNullable(port);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getSchema() {
        return Optional.ofNullable(schema);
    }

    public Optional<String> getUser() {
        return Optional.ofNullable(schemaUser);
    }

    public Optional<String> getUserPassword() {
        return Optional.ofNullable(schemaUserPassword);
    }

    public Optional<String> getWriter() {
        return Optional.ofNullable(writerUser);
    }

    public Optional<String> getWriterPassword() {
        return Optional.ofNullable(writerUserPassword);
    }

    public Optional<String> getReader() {
        return Optional.ofNullable(readerUser);
    }

    public Optional<String> getReaderPassword() {
        return Optional.ofNullable(readerUserPassword);
    }
}
