package io.metadew.iesi.script.execution;

import io.metadew.iesi.framework.execution.IESIMessage;
import io.metadew.iesi.metadata.definition.action.Action;
import io.metadew.iesi.script.operation.ActionSelectOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;

public class RootStrategy implements RootingStrategy {

    private String environment;

    private static final Logger LOGGER = LogManager.getLogger();

    public RootStrategy(String environment) {
        this.environment = environment;
    }

    @Override
    public void prepareExecution(ScriptExecution scriptExecution) {
        scriptExecution.getExecutionControl().setEnvName(environment);
        scriptExecution.getExecutionControl().getExecutionRuntime().setRuntimeVariablesFromList(scriptExecution, scriptExecution.getFrameworkExecution().getMetadataControl()
                .getConnectivityMetadataRepository()
                .executeQuery("select env_par_nm, env_par_val from "
                        + scriptExecution.getFrameworkExecution().getMetadataControl().getConnectivityMetadataRepository().getTableNameByLabel("EnvironmentParameters")
                        + " where env_nm = '" + scriptExecution.getExecutionControl().getEnvName() + "' order by env_par_nm asc, env_par_val asc", "reader"));
    }

    @Override
    public boolean executionAllowed(ActionSelectOperation actionSelectOperation, Action action) {
        boolean actionAllowed = actionSelectOperation.getExecutionStatus(action);
        LOGGER.trace(new IESIMessage(MessageFormat.format("Execution of action ''{0}'' is {1}allowed", action.getName(), (actionAllowed ? "":"not "))));
        return actionSelectOperation.getExecutionStatus(action);
    }

    @Override
    public void endExecution(ScriptExecution scriptExecution) {
        scriptExecution.getExecutionControl().terminate();
        if (scriptExecution.isExitOnCompletion()) {
            scriptExecution.getExecutionControl().endExecution();
        }
    }

    @Override
    public void continueAction(ActionSelectOperation actionSelectOperation, Action action) {
        actionSelectOperation.setContinueStatus(action);
    }


}