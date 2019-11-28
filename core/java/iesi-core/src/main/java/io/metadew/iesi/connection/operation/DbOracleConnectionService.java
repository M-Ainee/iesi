package io.metadew.iesi.connection.operation;

import io.metadew.iesi.connection.database.OracleDatabase;
import io.metadew.iesi.connection.database.TeradataDatabase;
import io.metadew.iesi.connection.database.connection.TeradataDatabaseConnection;
import io.metadew.iesi.framework.crypto.FrameworkCrypto;
import io.metadew.iesi.framework.execution.FrameworkControl;
import io.metadew.iesi.metadata.definition.connection.Connection;
import io.metadew.iesi.metadata.definition.connection.ConnectionParameter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DbOracleConnectionService {
    private static final Logger LOGGER = LogManager.getLogger();
    private static DbOracleConnectionService INSTANCE;

    public synchronized static DbOracleConnectionService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DbOracleConnectionService();
        }
        return INSTANCE;
    }

    private DbOracleConnectionService() {
    }

    public OracleDatabase getDatabase(Connection connection)  {
        return null;
    }

}
