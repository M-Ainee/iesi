package io.metadew.iesi.metadata_repository.repository.database.connection;

/**
 * Connection object for Postgresql databases. This class extends the default database connection object.
 * 
 * @author peter.billen
 *
 */
public class PostgresqlDatabaseConnection extends DatabaseConnection {

	private static String type = "postgresql";

	public PostgresqlDatabaseConnection(String connectionURL, String userName, String userPassword) {
		super(type, connectionURL, userName, userPassword);
	}

	public PostgresqlDatabaseConnection(String hostName, int portNumber, String databaseName, String userName,
			String userPassword) {
		super(type, "jdbc:postgresql://" + hostName + ":" + portNumber + "/" + databaseName, userName, userPassword);
	}


	@Override
	public String getDriver() {
		return "org.postgresql.Driver";
	}
}
