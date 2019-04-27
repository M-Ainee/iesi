package io.metadew.iesi.metadata_repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.metadew.iesi.framework.execution.FrameworkExecution;
import io.metadew.iesi.metadata.configuration.LedgerConfiguration;
import io.metadew.iesi.metadata.definition.DataObject;
import io.metadew.iesi.metadata.definition.Ledger;
import io.metadew.iesi.metadata_repository.repository.Repository;
import org.apache.logging.log4j.Level;

import java.text.MessageFormat;

public class TraceMetadataRepository extends MetadataRepository {

    public TraceMetadataRepository(String frameworkCode, String name, String scope, String instanceName, Repository repository, String repositoryObjectsPath, String repositoryTablesPath) {
        super(frameworkCode, name, scope, instanceName, repository, repositoryObjectsPath, repositoryTablesPath);
    }

    @Override
    public String getDefinitionFileName() {
        return "TraceTables.json";
    }

    @Override
    public String getObjectDefinitionFileName() {
        return "TraceObjects.json";
    }

    @Override
    public String getCategory() {
        return "trace";
    }


    @Override
    public String getCategoryPrefix() {
        return "TRC";
    }

    @Override
    public void save(DataObject dataObject, FrameworkExecution frameworkExecution) {
        ObjectMapper objectMapper = new ObjectMapper();
        if (dataObject.getType().equalsIgnoreCase("trace")) {
            // TODO
        } else {
            frameworkExecution.getFrameworkLog().log(MessageFormat.format("This repository is not responsible for loading saving {0}", dataObject.getType()), Level.DEBUG);
        }
    }
}
