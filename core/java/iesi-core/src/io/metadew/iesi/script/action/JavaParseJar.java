package io.metadew.iesi.script.action;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import io.metadew.iesi.connection.java.operation.JarOperation;
import io.metadew.iesi.connection.tools.HostConnectionTools;
import io.metadew.iesi.framework.execution.FrameworkExecution;
import io.metadew.iesi.metadata.definition.ActionParameter;
import io.metadew.iesi.script.execution.ActionExecution;
import io.metadew.iesi.script.execution.ExecutionControl;
import io.metadew.iesi.script.execution.ScriptExecution;
import io.metadew.iesi.script.operation.ActionParameterOperation;

/**
 * Action type to parse java archive files.
 * 
 * @author peter.billen
 *
 */
public class JavaParseJar {

	private ActionExecution actionExecution;
	private FrameworkExecution frameworkExecution;
	private ExecutionControl executionControl;

	// Parameters
	private ActionParameterOperation filePath;
	private ActionParameterOperation fileName;
	private ActionParameterOperation connectionName;
	private HashMap<String, ActionParameterOperation> actionParameterOperationMap;

	// Constructors
	public JavaParseJar() {

	}

	public JavaParseJar(FrameworkExecution frameworkExecution, ExecutionControl executionControl,
			ScriptExecution scriptExecution, ActionExecution actionExecution) {
		this.init(frameworkExecution, executionControl, scriptExecution, actionExecution);
	}

	public void init(FrameworkExecution frameworkExecution, ExecutionControl executionControl,
			ScriptExecution scriptExecution, ActionExecution actionExecution) {
		this.setFrameworkExecution(frameworkExecution);
		this.setExecutionControl(executionControl);
		this.setActionExecution(actionExecution);
		this.setActionParameterOperationMap(new HashMap<String, ActionParameterOperation>());
	}

	public void prepare() {
		// Reset Parameters
		this.setFilePath(new ActionParameterOperation(this.getFrameworkExecution(), this.getExecutionControl(),
				this.getActionExecution(), this.getActionExecution().getAction().getType(), "path"));
		this.setFileName(new ActionParameterOperation(this.getFrameworkExecution(), this.getExecutionControl(),
				this.getActionExecution(), this.getActionExecution().getAction().getType(), "file"));
		this.setConnectionName(new ActionParameterOperation(this.getFrameworkExecution(), this.getExecutionControl(),
				this.getActionExecution(), this.getActionExecution().getAction().getType(), "connection"));

		// Get Parameters
		for (ActionParameter actionParameter : this.getActionExecution().getAction().getParameters()) {
			if (actionParameter.getName().equalsIgnoreCase("path")) {
				this.getFilePath().setInputValue(actionParameter.getValue());
			} else if (actionParameter.getName().equalsIgnoreCase("file")) {
				this.getFileName().setInputValue(actionParameter.getValue());
			} else if (actionParameter.getName().equalsIgnoreCase("connection")) {
				this.getConnectionName().setInputValue(actionParameter.getValue());
			}
		}

		// Create parameter list
		this.getActionParameterOperationMap().put("path", this.getFilePath());
		this.getActionParameterOperationMap().put("file", this.getFileName());
		this.getActionParameterOperationMap().put("connection", this.getConnectionName());
	}

	// Methods
	public boolean execute() {
		try {
			boolean isOnLocalhost = HostConnectionTools.isOnLocalhost(this.getFrameworkExecution(),
					this.getConnectionName().getValue(), this.getExecutionControl().getEnvName());

			if (isOnLocalhost) {
				JarOperation jarOperation = new JarOperation();
				jarOperation.getJavaArchiveDefinition("target/iesi-core.jar");
			} else {
			
			}

			return true;
		} catch (Exception e) {
			StringWriter StackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(StackTrace));

			this.getActionExecution().getActionControl().increaseErrorCount();

			this.getActionExecution().getActionControl().logOutput("exception", e.getMessage());
			this.getActionExecution().getActionControl().logOutput("stacktrace", StackTrace.toString());

			return false;
		}

	}

	@SuppressWarnings("unused")
	private void setScope(String input) {
		this.getActionExecution().getActionControl().logOutput("java.parse", input);
	}

	@SuppressWarnings("unused")
	private void setError(String input) {
		this.getActionExecution().getActionControl().logOutput("java.parse.error", input);
		this.getActionExecution().getActionControl().increaseErrorCount();
	}

	@SuppressWarnings("unused")
	private void setSuccess() {
		this.getActionExecution().getActionControl().logOutput("java.parse.success", "confirmed");
		this.getActionExecution().getActionControl().increaseSuccessCount();
	}

	// Getters and Setters
	public FrameworkExecution getFrameworkExecution() {
		return frameworkExecution;
	}

	public void setFrameworkExecution(FrameworkExecution frameworkExecution) {
		this.frameworkExecution = frameworkExecution;
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

	public ActionParameterOperation getConnectionName() {
		return connectionName;
	}

	public void setConnectionName(ActionParameterOperation connectionName) {
		this.connectionName = connectionName;
	}

	public HashMap<String, ActionParameterOperation> getActionParameterOperationMap() {
		return actionParameterOperationMap;
	}

	public void setActionParameterOperationMap(HashMap<String, ActionParameterOperation> actionParameterOperationMap) {
		this.actionParameterOperationMap = actionParameterOperationMap;
	}

	public ActionParameterOperation getFilePath() {
		return filePath;
	}

	public void setFilePath(ActionParameterOperation filePath) {
		this.filePath = filePath;
	}

	public ActionParameterOperation getFileName() {
		return fileName;
	}

	public void setFileName(ActionParameterOperation fileName) {
		this.fileName = fileName;
	}

}