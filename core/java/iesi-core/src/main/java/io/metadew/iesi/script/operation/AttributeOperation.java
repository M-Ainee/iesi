package io.metadew.iesi.script.operation;

import io.metadew.iesi.metadata.execution.MetadataControl;
import io.metadew.iesi.script.execution.ActionExecution;
import io.metadew.iesi.script.execution.ExecutionControl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sql.rowset.CachedRowSet;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

/**
 * Operation to manage component attribute when a component
 * has been specific in the action
 *
 * @author peter.billen
 */
public class AttributeOperation {

    private Properties properties;
    private ExecutionControl executionControl;
    private ActionExecution actionExecution;
    private String type;
    private String name;
    private static final Logger LOGGER = LogManager.getLogger();

    // Constructors
    public AttributeOperation(ExecutionControl executionControl, ActionExecution actionExecution, String type, String name) {
        this.setExecutionControl(executionControl);
        this.setActionExecution(actionExecution);
        this.setType(type);
        this.setName(name);

        // initialize properties
        this.setProperties(new Properties());

        // Get component attributes
        String query = "";
        if (this.getType().equals("component")) {
            query = "select a.comp_id, a.comp_att_nm, a.comp_att_val from "
                    + MetadataControl.getInstance().getDesignMetadataRepository().getTableNameByLabel("ComponentAttributes")
                    + " a inner join "
                    + MetadataControl.getInstance().getDesignMetadataRepository().getTableNameByLabel("Components")
                    + " b on a.comp_id = b.comp_id where b.comp_nm = '" + this.getName() + "'";
        }

        // Set attribute values
        CachedRowSet crs = null;
        LOGGER.debug("component.name=" + name);
        crs = MetadataControl.getInstance().getDesignMetadataRepository().executeQuery(query, "reader");
        try {
            while (crs.next()) {
                String key = crs.getString("COMP_ATT_NM");
                String value = crs.getString("COMP_ATT_VAL");
                this.getProperties().put(key, value);
                LOGGER.debug("attribute.name=" + key);
                LOGGER.debug("attribute.name=" + value);
            }
            crs.close();
        } catch (Exception e) {
            StringWriter StackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(StackTrace));
        }
    }


    public String getProperty(String input) {
        String output = this.getProperties().getProperty(input);
        if (output == null) {
            throw new RuntimeException("Unknown value lookup requested: " + input);
        }
        return output;
    }

    // Getters and setters
    private Properties getProperties() {
        return properties;
    }

    private void setProperties(Properties properties) {
        this.properties = properties;
    }


    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type = type.trim().toLowerCase();
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public ExecutionControl getExecutionControl() {
        return executionControl;
    }


    public void setExecutionControl(ExecutionControl executionControl) {
        this.executionControl = executionControl;
    }


    public ActionExecution getActionExecution() {
        return actionExecution;
    }


    public void setActionExecution(ActionExecution actionExecution) {
        this.actionExecution = actionExecution;
    }

}