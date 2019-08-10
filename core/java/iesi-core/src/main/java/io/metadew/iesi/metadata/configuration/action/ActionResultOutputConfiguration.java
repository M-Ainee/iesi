package io.metadew.iesi.metadata.configuration.action;

import io.metadew.iesi.framework.instance.FrameworkInstance;
import io.metadew.iesi.metadata.definition.action.ActionResultOutput;
import io.metadew.iesi.metadata.execution.MetadataControl;

import javax.sql.rowset.CachedRowSet;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class ActionResultOutputConfiguration {

    private String runId;
    private ActionResultOutput actionResultOutput;

    // Constructors
    public ActionResultOutputConfiguration() {
    }

    // Methods
	public List<ActionResultOutput> getActionResultOutputs(String runId, long processId) {
        List<ActionResultOutput> actionResultOutputs = new ArrayList<>();
        String queryActionResultOutputs = "select RUN_ID, PRC_ID, ACTION_ID, OUT_NM, OUT_VAL from " + MetadataControl.getInstance().getResultMetadataRepository().getTableNameByLabel("ActionResultOutputs")
                + " where RUN_ID = '" + runId + "' and PRC_ID = " + processId + " order by LOAD_TMS asc";
        CachedRowSet crsActionResultOutputs = MetadataControl.getInstance().getResultMetadataRepository().executeQuery(queryActionResultOutputs, "reader");
        try {
            while (crsActionResultOutputs.next()) {
            	ActionResultOutput actionResultOutput= new ActionResultOutput();
            	actionResultOutput.setName(crsActionResultOutputs.getString("OUT_NM"));
            	actionResultOutput.setValue(crsActionResultOutputs.getString("OUT_VAL"));
                actionResultOutputs.add(actionResultOutput);
            }
            crsActionResultOutputs.close();
        } catch (Exception e) {
            StringWriter StackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(StackTrace));
        }
        
        return actionResultOutputs;
    }
    

    // Getters and Setters
    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

	public ActionResultOutput getActionResultOutput() {
		return actionResultOutput;
	}

	public void setActionResultOutput(ActionResultOutput actionResultOutput) {
		this.actionResultOutput = actionResultOutput;
	}

}