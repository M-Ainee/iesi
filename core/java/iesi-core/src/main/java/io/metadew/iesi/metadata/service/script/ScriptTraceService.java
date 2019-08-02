package io.metadew.iesi.metadata.service.script;

import io.metadew.iesi.connection.tools.SQLTools;
import io.metadew.iesi.metadata.configuration.action.ActionParameterTraceConfiguration;
import io.metadew.iesi.metadata.configuration.action.ActionTraceConfiguration;
import io.metadew.iesi.metadata.configuration.exception.MetadataAlreadyExistsException;
import io.metadew.iesi.metadata.configuration.script.ScriptParameterTraceConfiguration;
import io.metadew.iesi.metadata.configuration.script.ScriptTraceConfiguration;
import io.metadew.iesi.metadata.configuration.script.ScriptVersionTraceConfiguration;
import io.metadew.iesi.metadata.definition.script.ScriptParameter;
import io.metadew.iesi.metadata.definition.script.ScriptParameterTrace;
import io.metadew.iesi.metadata.definition.script.ScriptTrace;
import io.metadew.iesi.metadata.definition.script.ScriptVersionTrace;
import io.metadew.iesi.metadata.execution.MetadataControl;
import io.metadew.iesi.script.execution.ActionExecution;
import io.metadew.iesi.script.execution.ScriptExecution;
import io.metadew.iesi.script.operation.ActionParameterOperation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScriptTraceService {

    private ScriptTraceConfiguration scriptTraceConfiguration;
    private ScriptVersionTraceConfiguration scriptVersionTraceConfiguration;
    private ScriptParameterTraceConfiguration scriptParameterTraceConfiguration;


    public ScriptTraceService(MetadataControl metadataControl) {
        this.scriptTraceConfiguration = new ScriptTraceConfiguration(metadataControl);
        this.scriptVersionTraceConfiguration = new ScriptVersionTraceConfiguration(metadataControl);
        this.scriptParameterTraceConfiguration = new ScriptParameterTraceConfiguration(metadataControl);
    }

    public void trace(ScriptExecution scriptExecution) {
        try {
            String runId = scriptExecution.getExecutionControl().getRunId();
            Long processId = scriptExecution.getProcessId();
            scriptTraceConfiguration.insert(new ScriptTrace(runId, processId,
                    scriptExecution.getParentScriptExecution().map(ScriptExecution::getProcessId).orElse(0L),
                    scriptExecution.getScript()));
            scriptVersionTraceConfiguration.insert(new ScriptVersionTrace(runId, processId, scriptExecution.getScript().getVersion()));
            for (ScriptParameter scriptParameter : scriptExecution.getScript().getParameters()) {
                scriptParameterTraceConfiguration.insert(new ScriptParameterTrace(runId, processId, scriptParameter));
            }

        } catch (MetadataAlreadyExistsException | SQLException e) {
            e.printStackTrace();
        }
    }

}