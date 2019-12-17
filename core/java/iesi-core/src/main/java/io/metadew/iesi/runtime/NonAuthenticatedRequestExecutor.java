package io.metadew.iesi.runtime;

import io.metadew.iesi.metadata.configuration.exception.MetadataDoesNotExistException;
import io.metadew.iesi.metadata.configuration.execution.ExecutionRequestConfiguration;
import io.metadew.iesi.metadata.definition.execution.ExecutionRequestStatus;
import io.metadew.iesi.metadata.definition.execution.NonAuthenticatedExecutionRequest;
import io.metadew.iesi.metadata.definition.execution.script.ScriptExecutionRequest;
import io.metadew.iesi.runtime.script.ScriptExecutionRequestListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NonAuthenticatedRequestExecutor implements RequestExecutor<NonAuthenticatedExecutionRequest> {

    private static final Logger LOGGER = LogManager.getLogger();

    private static NonAuthenticatedRequestExecutor INSTANCE;

    public synchronized static NonAuthenticatedRequestExecutor getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NonAuthenticatedRequestExecutor();
        }
        return INSTANCE;
    }

    private NonAuthenticatedRequestExecutor() {
    }

    @Override
    public Class<NonAuthenticatedExecutionRequest> appliesTo() {
        return NonAuthenticatedExecutionRequest.class;
    }

    @Override
    public void execute(NonAuthenticatedExecutionRequest executionRequest) {
        try {
            executionRequest.updateExecutionRequestStatus(ExecutionRequestStatus.ACCEPTED);
            ExecutionRequestConfiguration.getInstance().update(executionRequest);

            for (ScriptExecutionRequest scriptExecutionRequest : executionRequest.getScriptExecutionRequests()) {
                ScriptExecutionRequestListener.getInstance().execute(scriptExecutionRequest);
            }
            executionRequest.updateExecutionRequestStatus(ExecutionRequestStatus.COMPLETED);
            ExecutionRequestConfiguration.getInstance().update(executionRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
