//package io.metadew.iesi.connection.operation;
//
//import io.metadew.iesi.connection.database.DremioDatabase;
//import io.metadew.iesi.connection.database.connection.dremio.DremioDatabaseConnection;
//import io.metadew.iesi.framework.crypto.FrameworkCrypto;
//import io.metadew.iesi.framework.execution.FrameworkControl;
//import io.metadew.iesi.metadata.definition.connection.Connection;
//import lombok.extern.log4j.Log4j2;
//
//import java.text.MessageFormat;
//import java.util.Optional;
//
//@Log4j2
//public class DbDremioConnectionService {
//
//    private static DbDremioConnectionService INSTANCE;
//
//    private final static String connectionUrlKey = "connectionURL";
//    private final static String hostKey = "host";
//    private final static String portKey = "port";
//    private final static String connectionModeKey = "mode";
//    private final static String clusterNameKey = "cluster";
//    private final static String schemaKey = "schema";
//    private final static String userKey = "user";
//    private final static String passwordKey = "password";
//
//    public synchronized static DbDremioConnectionService getInstance() {
//        if (INSTANCE == null) {
//            INSTANCE = new DbDremioConnectionService();
//        }
//        return INSTANCE;
//    }
//
//    private DbDremioConnectionService() {
//    }
//
//    public DremioDatabase getDatabase(Connection connection) {
//        String userName = getMandatoryParameterWithKey(connection, userKey);
//        String userPassword = getMandatoryParameterWithKey(connection, passwordKey);
//        String schemaName = getMandatoryParameterWithKey(connection, schemaKey);
//        DremioDatabaseConnection dremioDatabaseConnection;
//        if (getOptionalParameterWithKey(connection, connectionUrlKey).isPresent()) {
//            dremioDatabaseConnection = new DremioDatabaseConnection(
//                    getOptionalParameterWithKey(connection, connectionUrlKey).get(),
//                    userName,
//                    userPassword,
//                    schemaName);
//            return new DremioDatabase(dremioDatabaseConnection, schemaName);
//        }
//
//        String hostName = getMandatoryParameterWithKey(connection, hostKey);
//        int port = Integer.parseInt(getMandatoryParameterWithKey(connection, portKey));
//        String connectionMode = getMandatoryParameterWithKey(connection, connectionModeKey);
//        String clusterName = getMandatoryParameterWithKey(connection, clusterNameKey);
//
//        dremioDatabaseConnection = new DremioDatabaseConnection(hostName,
//                port,
//                connectionMode,
//                clusterName,
//                schemaName,
//                userName,
//                userPassword);
//        return new DremioDatabase(dremioDatabaseConnection, schemaName);
//    }
//
//    private String getMandatoryParameterWithKey(Connection connection, String key) {
//        return connection.getParameters().stream()
//                .filter(connectionParameter -> connectionParameter.getName().equalsIgnoreCase(key))
//                .findFirst()
//                .map(connectionParameter -> FrameworkControl.getInstance().resolveConfiguration(connectionParameter.getValue()))
//                .map(connectionParameterValue -> FrameworkCrypto.getInstance().decryptIfNeeded(connectionParameterValue))
//                .orElseThrow(() -> new RuntimeException(MessageFormat.format("Connection {0} does not contain mandatory parameter ''{1}''", connection, key)));
//
//    }
//
//    private Optional<String> getOptionalParameterWithKey(Connection connection, String key) {
//        return connection.getParameters().stream()
//                .filter(connectionParameter -> connectionParameter.getName().equalsIgnoreCase(key))
//                .findFirst()
//                .map(connectionParameter -> FrameworkControl.getInstance().resolveConfiguration(connectionParameter.getValue()))
//                .map(connectionParameterValue -> FrameworkCrypto.getInstance().decryptIfNeeded(connectionParameterValue));
//    }
//}