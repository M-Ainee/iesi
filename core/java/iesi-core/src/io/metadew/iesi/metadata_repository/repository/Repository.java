package io.metadew.iesi.metadata_repository.repository;

import io.metadew.iesi.connection.database.sql.SqlScriptResult;
import io.metadew.iesi.framework.execution.FrameworkLog;
import io.metadew.iesi.metadata.definition.MetadataTable;
import io.metadew.iesi.metadata_repository.repository.database.Database;

import javax.sql.rowset.CachedRowSet;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class Repository {

    private Map<String, Database> databases;

    public Repository(Map<String, Database> databases) {
        this.databases = databases;
    }

    public String getAllTablesQuery(String pattern) {
        return databases.get("reader").getAllTablesQuery(pattern);
    }

    public CachedRowSet executeQuery(String query, String logonType) {
        CachedRowSet crs;
        try {
            crs = this.databases.get(logonType).executeQuery(query);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return crs;
    }

    public void executeUpdate(String query) {
        this.databases.get("writer").executeUpdate(query);
    }

    public void executeScript(String fileName, String logonType) {
        SqlScriptResult dcSQLScriptResult = this.databases.get(logonType).executeScript(fileName);

        if (dcSQLScriptResult.getReturnCode() != 0) {
            throw new RuntimeException("Error executing SQL script");
        }
    }

    public void executeScript(InputStream inputStream, String logonType) {
        SqlScriptResult dcSQLScriptResult = this.databases.get(logonType).executeScript(inputStream);

        if (dcSQLScriptResult.getReturnCode() != 0) {
            throw new RuntimeException("Error executing SQL script");
        }
    }

    public void dropAllTables(String pattern, FrameworkLog frameworkLog) {
        this.databases.get("owner").dropAllTables(pattern, frameworkLog);
    }

    public void dropTable(String tableName, FrameworkLog frameworkLog) {
        this.databases.get("owner").dropTable(tableName, frameworkLog);
    }

    public void cleanTable(String tableName, FrameworkLog frameworkLog) {
        this.databases.get("writer").cleanTable(tableName, frameworkLog);
    }

    public void createTable(MetadataTable table, String tableNamePrefix) {
        this.databases.get("owner").createTable(table, tableNamePrefix);
    }

    public List<String> getAllTables(String pattern) {
        return this.databases.get("reader").getAllTables(pattern);
    }

    public void cleanAllTables(String pattern, FrameworkLog frameworkLog) {
        this.databases.get("writer").cleanAllTables(pattern, frameworkLog);
    }

    public Map<String, Database> getDatabases() {
        return databases;
    }
}
