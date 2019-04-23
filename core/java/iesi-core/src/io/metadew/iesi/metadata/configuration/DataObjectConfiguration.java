package io.metadew.iesi.metadata.configuration;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.apache.logging.log4j.Level;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.metadew.iesi.connection.tools.FileTools;
import io.metadew.iesi.connection.tools.FolderTools;
import io.metadew.iesi.framework.configuration.FrameworkObjectConfiguration;
import io.metadew.iesi.framework.execution.FrameworkExecution;
import io.metadew.iesi.metadata.definition.Component;
import io.metadew.iesi.metadata.definition.Connection;
import io.metadew.iesi.metadata.definition.DataObject;
import io.metadew.iesi.metadata.definition.Dataframe;
import io.metadew.iesi.metadata.definition.Environment;
import io.metadew.iesi.metadata.definition.Impersonation;
import io.metadew.iesi.metadata.definition.Ledger;
import io.metadew.iesi.metadata.definition.MetadataObject;
import io.metadew.iesi.metadata.definition.MetadataTable;
import io.metadew.iesi.metadata.definition.Repository;
import io.metadew.iesi.metadata.definition.Script;
import io.metadew.iesi.metadata.definition.Subroutine;
import io.metadew.iesi.metadata.operation.MetadataRepositoryOperation;
import io.metadew.iesi.script.operation.ScriptOperation;

public class DataObjectConfiguration {

	private FrameworkExecution frameworkExecution;
	private List<DataObject> dataObjects;
	private MetadataRepositoryConfiguration metadataRepositoryConfiguration;
	private MetadataRepositoryOperation metadataRepositoryOperation;

	// Constructors
	public DataObjectConfiguration(FrameworkExecution frameworkExecution) {
		this.setFrameworkExecution(frameworkExecution);
		if (this.getFrameworkExecution() != null)
			this.setMetadataRepositoryConfiguration(
					this.getFrameworkExecution().getMetadataControl().getGeneralRepositoryConfiguration());
	}

	public DataObjectConfiguration(FrameworkExecution frameworkExecution, List<DataObject> dataObjects) {
		this.setFrameworkExecution(frameworkExecution);
		if (this.getFrameworkExecution() != null)
			this.setMetadataRepositoryConfiguration(
					this.getFrameworkExecution().getMetadataControl().getGeneralRepositoryConfiguration());
		this.setDataObjects(dataObjects);
	}

	public DataObjectConfiguration(FrameworkExecution frameworkExecution,
			MetadataRepositoryConfiguration metadataRepositoryConfiguration, List<DataObject> dataObjects) {
		this.setFrameworkExecution(frameworkExecution);
		if (this.getFrameworkExecution() != null)
			this.setMetadataRepositoryConfiguration(metadataRepositoryConfiguration);
		this.setMetadataRepositoryOperation(new MetadataRepositoryOperation(this.getFrameworkExecution(),
				this.getMetadataRepositoryConfiguration()));
		this.setDataObjects(dataObjects);
	}

	// Methods
	public DataObject getDataObject(Object object) {
		String type = FrameworkObjectConfiguration.getFrameworkObjectType(object);
		DataObject dataObject = new DataObject(type, object);
		return dataObject;
	}

	public String getDataObjectJSON(Object object) {
		DataObject dataObject = this.getDataObject(object);
		ObjectMapper mapper = new ObjectMapper();
		String json = "";
		try {
			json = mapper.writeValueAsString(dataObject);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return json;
	}

	public String getPrettyDataObjectJSON(Object object) {
		DataObject dataObject = this.getDataObject(object);
		ObjectMapper mapper = new ObjectMapper();
		String json = "";
		try {
			json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dataObject);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return json;
	}

	public boolean isJSONArray(String data) {
		if (data.trim().startsWith("[")) {
			return true;
		} else {
			return false;
		}
	}

	public DataObject getDataObject(String data) {
		DataObject dataObject = null;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			dataObject = objectMapper.readValue(data, new TypeReference<DataObject>() {
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataObject;
	}

	public List<DataObject> getDataArray(String data) {
		List<DataObject> dataObjectList = null;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			dataObjectList = objectMapper.readValue(data, new TypeReference<List<DataObject>>() {
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataObjectList;
	}

	public String getMetadataRepositoryDdl() {
		StringBuilder output = new StringBuilder();
		int counter = 0;
		for (DataObject dataObject : dataObjects) {
			if (counter != 0)
				output.append("\n");
			output.append(this.getMetadataRepositoryInsertStatement(dataObject));
		}
		return output.toString();
	}

	public void saveToMetadataRepository() {
		for (DataObject dataObject : dataObjects) {
			InputStream inputStream = FileTools.convertToInputStream(
					this.getMetadataRepositoryInsertStatement(dataObject),
					this.getFrameworkExecution().getFrameworkControl());
			this.getMetadataRepositoryConfiguration().executeScript(inputStream);
		}
	}

	private String appendOutput(String type, String input) {
		if (this.getMetadataRepositoryConfiguration().getMetadataObjectConfiguration().exists(type)) {
			return input;
		} else {
			return "";
		}
	}

	private String getMetadataRepositoryInsertStatement(DataObject dataObject) {
		ObjectMapper objectMapper = new ObjectMapper();
		String output = "";

		// Environment
		if (dataObject.getType().equalsIgnoreCase("environment")) {
			Environment environment = objectMapper.convertValue(dataObject.getData(), Environment.class);
			EnvironmentConfiguration environmentConfiguration = new EnvironmentConfiguration(environment,
					this.getFrameworkExecution());
			output = this.appendOutput("Environments", environmentConfiguration.getInsertStatement());
		}

		// Connections
		if (dataObject.getType().equalsIgnoreCase("connection")) {
			Connection connection = objectMapper.convertValue(dataObject.getData(), Connection.class);
			ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration(connection,
					this.getFrameworkExecution());
			output = this.appendOutput("Connections", connectionConfiguration.getInsertStatement());
		}

		// Impersonations
		if (dataObject.getType().equalsIgnoreCase("impersonation")) {
			Impersonation impersonation = objectMapper.convertValue(dataObject.getData(), Impersonation.class);
			ImpersonationConfiguration impersonationConfiguration = new ImpersonationConfiguration(impersonation,
					this.getFrameworkExecution());
			output = this.appendOutput("Impersonations", impersonationConfiguration.getInsertStatement());
		}

		// Repositories
		if (dataObject.getType().equalsIgnoreCase("repository")) {
			Repository repository = objectMapper.convertValue(dataObject.getData(), Repository.class);
			RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration(repository,
					this.getFrameworkExecution());
			output = this.appendOutput("Repositories", repositoryConfiguration.getInsertStatement());
		}

		// Subroutines
		if (dataObject.getType().equalsIgnoreCase("subroutine")) {
			Subroutine subroutine = objectMapper.convertValue(dataObject.getData(), Subroutine.class);
			SubroutineConfiguration subroutineConfiguration = new SubroutineConfiguration(subroutine,
					this.getFrameworkExecution());
			output = this.appendOutput("Subroutines", subroutineConfiguration.getInsertStatement());
		}

		// Script
		if (dataObject.getType().equalsIgnoreCase("script")) {
			Script script = objectMapper.convertValue(dataObject.getData(), Script.class);
			if (ScriptOperation.validateScriptQuality(script)) {
				ScriptConfiguration scriptConfiguration = new ScriptConfiguration(script, this.getFrameworkExecution());
				output = this.appendOutput("Scripts", scriptConfiguration.getInsertStatement());
			} else {
				this.getFrameworkExecution().getFrameworkLog().log("metadata.error.duplicate.names", Level.INFO);
				// TODO raise
			}
		}

		// Ledger
		if (dataObject.getType().equalsIgnoreCase("ledger")) {
			Ledger ledger = objectMapper.convertValue(dataObject.getData(), Ledger.class);
			LedgerConfiguration ledgerConfiguration = new LedgerConfiguration(ledger, this.getFrameworkExecution());
			output = this.appendOutput("Ledgers", ledgerConfiguration.getInsertStatement());
		}

		// Component
		if (dataObject.getType().equalsIgnoreCase("component")) {
			Component component = objectMapper.convertValue(dataObject.getData(), Component.class);
			ComponentConfiguration componentConfiguration = new ComponentConfiguration(component,
					this.getFrameworkExecution());
			output = this.appendOutput("Components", componentConfiguration.getInsertStatement());
		}

		// Dataframe
		if (dataObject.getType().equalsIgnoreCase("dataframe")) {
			Dataframe dataframe = objectMapper.convertValue(dataObject.getData(), Dataframe.class);
			DataframeConfiguration dataframeConfiguration = new DataframeConfiguration(dataframe,
					this.getFrameworkExecution(), this.getMetadataRepositoryOperation());
			output = this.appendOutput("Dataframes", dataframeConfiguration.getInsertStatement());
		}

		// Metadata Tables
		if (dataObject.getType().equalsIgnoreCase("metadatatable")) {
			MetadataTable metadataTable = objectMapper.convertValue(dataObject.getData(), MetadataTable.class);
			MetadataTableConfiguration metadataTableConfiguration = new MetadataTableConfiguration(metadataTable,
					this.getMetadataRepositoryConfiguration());

			// TODO
			output = metadataTableConfiguration.getCreateStatement();
		}

		return output;

	}

	@SuppressWarnings("unused")
	public void saveToMetadataFileStore() {
		ObjectMapper objectMapper = new ObjectMapper();
		String postSql = "";
		for (DataObject dataObject : dataObjects) {
			String output = "";

			// Environment
			if (dataObject.getType().equalsIgnoreCase("environment")) {
				Environment environment = objectMapper.convertValue(dataObject.getData(), Environment.class);

			}

			// Connections
			if (dataObject.getType().equalsIgnoreCase("connection")) {
				Connection connection = objectMapper.convertValue(dataObject.getData(), Connection.class);

			}

			// Impersonations
			if (dataObject.getType().equalsIgnoreCase("impersonation")) {

			}

			// Subroutines
			if (dataObject.getType().equalsIgnoreCase("subroutine")) {
				Subroutine subroutine = objectMapper.convertValue(dataObject.getData(), Subroutine.class);

			}

			// Scripts
			if (dataObject.getType().equalsIgnoreCase("script")) {
				Script script = objectMapper.convertValue(dataObject.getData(), Script.class);

			}

			// Component Types
			if (dataObject.getType().equalsIgnoreCase("component")) {
				Component component = objectMapper.convertValue(dataObject.getData(), Component.class);

			}

			// Metadata Tables
			if (dataObject.getType().equalsIgnoreCase("metadatatable")) {
				MetadataTable metadataTable = objectMapper.convertValue(dataObject.getData(), MetadataTable.class);

			}

			// Metadata Objects
			if (dataObject.getType().equalsIgnoreCase("metadataobject")) {
				MetadataObject metadataObject = objectMapper.convertValue(dataObject.getData(), MetadataObject.class);
				this.createFolder(this.getMetadataRepositoryConfiguration().getFileStoreConnection().getPath(),
						metadataObject.getName());
			}

			// Execute

		}

		if (!postSql.trim().equalsIgnoreCase("")) {
			InputStream inputStreamPostSql = FileTools.convertToInputStream(postSql,
					this.getFrameworkExecution().getFrameworkControl());
			this.getMetadataRepositoryConfiguration().executeScript(inputStreamPostSql);
		}

	}

	private void createFolder(String path, String folderName) {
		FolderTools.createFolder(path + File.separator + folderName);
	}

	// Getters and Setters
	public FrameworkExecution getFrameworkExecution() {
		return frameworkExecution;
	}

	public void setFrameworkExecution(FrameworkExecution frameworkExecution) {
		this.frameworkExecution = frameworkExecution;
	}

	public List<DataObject> getDataObjects() {
		return dataObjects;
	}

	public void setDataObjects(List<DataObject> dataObjects) {
		this.dataObjects = dataObjects;
	}

	public MetadataRepositoryConfiguration getMetadataRepositoryConfiguration() {
		return metadataRepositoryConfiguration;
	}

	public void setMetadataRepositoryConfiguration(MetadataRepositoryConfiguration metadataRepositoryConfiguration) {
		this.metadataRepositoryConfiguration = metadataRepositoryConfiguration;
	}

	public MetadataRepositoryOperation getMetadataRepositoryOperation() {
		return metadataRepositoryOperation;
	}

	public void setMetadataRepositoryOperation(MetadataRepositoryOperation metadataRepositoryOperation) {
		this.metadataRepositoryOperation = metadataRepositoryOperation;
	}

}