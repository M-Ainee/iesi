package io.metadew.iesi.metadata.service.script;

import io.metadew.iesi.metadata.configuration.exception.MetadataAlreadyExistsException;
import io.metadew.iesi.metadata.configuration.script.trace.ScriptParameterTraceConfiguration;
import io.metadew.iesi.metadata.configuration.script.trace.ScriptTraceConfiguration;
import io.metadew.iesi.metadata.configuration.script.trace.ScriptVersionTraceConfiguration;
import io.metadew.iesi.metadata.definition.script.ScriptParameter;
import io.metadew.iesi.metadata.definition.script.trace.ScriptParameterTrace;
import io.metadew.iesi.metadata.definition.script.trace.ScriptTrace;
import io.metadew.iesi.metadata.definition.script.trace.ScriptVersionTrace;
import io.metadew.iesi.script.execution.ScriptExecution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ScriptTraceService {
    private static final Logger LOGGER = LogManager.getLogger();

    private static ScriptTraceService INSTANCE;

    public synchronized static ScriptTraceService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ScriptTraceService();
        }
        return INSTANCE;
    }

    private ScriptTraceService() {}

    public void trace(ScriptExecution scriptExecution) {
        try {
            String runId = scriptExecution.getExecutionControl().getRunId();
            Long processId = scriptExecution.getProcessId();
            ScriptTraceConfiguration.getInstance().insert(new ScriptTrace(runId, processId,
                    scriptExecution.getParentScriptExecution().map(ScriptExecution::getProcessId).orElse(0L),
                    scriptExecution.getScript()));
            ScriptVersionTraceConfiguration.getInstance().insert(new ScriptVersionTrace(runId, processId, scriptExecution.getScript().getVersion()));
            for (ScriptParameter scriptParameter : scriptExecution.getScript().getParameters()) {
                ScriptParameterTraceConfiguration.getInstance().insert(new ScriptParameterTrace(runId, processId, scriptParameter));
            }

        } catch (MetadataAlreadyExistsException e) {
            StringWriter StackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(StackTrace));

            LOGGER.warn("exception=" + e.getMessage());
            LOGGER.info("stacktrace" + StackTrace.toString());
        }
    }

}
