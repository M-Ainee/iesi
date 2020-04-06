package io.metadew.iesi.data.generation.output.control.rule;

import io.metadew.iesi.connection.database.DatabaseHandlerImpl;
import io.metadew.iesi.data.generation.execution.GenerationControlRuleExecution;
import io.metadew.iesi.framework.execution.FrameworkExecution;
import io.metadew.iesi.script.execution.ExecutionControl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;

public class RecordCount {

    private GenerationControlRuleExecution generationControlRuleExecution;
    private FrameworkExecution frameworkExecution;
    private ExecutionControl executionControl;
    private String generationControlRuleTypeName = "record.count";
    private String output = "";
    private static final Logger LOGGER = LogManager.getLogger();

    // Parameters

    // Constructors
    public RecordCount(FrameworkExecution frameworkExecution, ExecutionControl executionControl,
                       GenerationControlRuleExecution generationControlRuleExecution) {
        this.setFrameworkExecution(frameworkExecution);
        this.setExecutionControl(executionControl);
        this.setGenerationControlRuleExecution(generationControlRuleExecution);
    }

    //
    public boolean execute() {
        LOGGER.info("generation.control.rule.type=" + this.getGenerationControlRuleTypeName());

        // Reset Parameters

        // Get Parameters

        // Run the generation Control Rule
        try {
            CachedRowSet crs = null;
            String query = "select count(*) as 'RECORD_COUNT' from "
                    + this.getGenerationControlRuleExecution().getGenerationExecution().getGenerationRuntime().getTableName();
            crs = DatabaseHandlerImpl.getInstance().executeQuery(this.getGenerationControlRuleExecution().getGenerationExecution().getGenerationRuntime().getTemporaryDatabaseConnection()
                    , query);

            String recordCount = "";
            while (crs.next()) {
                recordCount = crs.getString("RECORD_COUNT");
            }
            crs.close();

            this.setOutput(recordCount);
        } catch (SQLException e) {
            throw new RuntimeException("Issue generating control rule output: " + e, e);
        }

        return true;

    }

    // Getters and Setters
    public String getGenerationControlRuleTypeName() {
        return generationControlRuleTypeName;
    }

    public void setGenerationControlRuleTypeName(String generationControlRuleTypeName) {
        this.generationControlRuleTypeName = generationControlRuleTypeName;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public GenerationControlRuleExecution getGenerationControlRuleExecution() {
        return generationControlRuleExecution;
    }

    public void setGenerationControlRuleExecution(GenerationControlRuleExecution generationControlRuleExecution) {
        this.generationControlRuleExecution = generationControlRuleExecution;
    }

    public ExecutionControl getExecutionControl() {
        return executionControl;
    }

    public void setExecutionControl(ExecutionControl executionControl) {
        this.executionControl = executionControl;
    }

    public FrameworkExecution getFrameworkExecution() {
        return frameworkExecution;
    }

    public void setFrameworkExecution(FrameworkExecution frameworkExecution) {
        this.frameworkExecution = frameworkExecution;
    }

}