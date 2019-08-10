package io.metadew.iesi.assembly.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.metadew.iesi.assembly.AssemblyContext;
import io.metadew.iesi.connection.tools.FileTools;
import io.metadew.iesi.connection.tools.FolderTools;
import io.metadew.iesi.metadata.definition.*;
import io.metadew.iesi.metadata.definition.action.ActionType;
import io.metadew.iesi.metadata.definition.connection.ConnectionType;
import io.metadew.iesi.metadata.definition.script.ScriptType;
import io.metadew.iesi.metadata.operation.DataObjectOperation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class AssemblyExecution {

	private AssemblyContext assemblyContext;

	private String repository;
	private String development;
	private String sandbox;
	private String instance;

	private String version;

	private String configuration;

	private boolean applyConfiguration;
	private boolean testAssembly;
	private boolean distribution;

	// Constructors
	public AssemblyExecution() {
		super();
	}

	public AssemblyExecution(String repository, String development, String sandbox, String instance, String version,
			String configuration, boolean applyConfiguration, boolean testAssembly, boolean distribution) {
		try {
			this.setRepository(repository);
			this.setDevelopment(development);
			this.setSandbox(sandbox);
			this.setInstance(instance);
			this.setVersion(version);
			this.setConfiguration(configuration);
			this.setApplyConfiguration(applyConfiguration);
			this.setTestAssembly(testAssembly);
			this.setDistribution(distribution);
			this.setAssemblyContext(new AssemblyContext(this.getRepository()));
		} catch (Exception e) {
			System.out.println("Invalid installation parameters");
			throw new RuntimeException(e);
		}
	}

	// Methods
	@SuppressWarnings({ "resource", "static-access", "rawtypes", "unchecked" })
	public void execute() {
		try {

			String repositoryHome = this.getRepository();
			String developmentHome = this.getDevelopment();
			String sandboxHome = this.getSandbox();
			String instanceHome = sandboxHome + File.separator + this.getInstance();
			String versionHome = instanceHome + File.separator + this.getVersion();
			String configurationHome = sandboxHome + File.separator + "conf" + File.separator + this.getInstance()
					+ File.separator + this.getConfiguration();

			File file = null;
			BufferedReader bufferedReader = null;
			String readLine = "";

			// Delete version directory
			FolderTools.deleteFolder(versionHome, true);

			// Recreate version directory
			FolderTools.createFolder(instanceHome);
			FolderTools.createFolder(versionHome);

			// Loop the file system configuration
			String fileSystemStructure = repositoryHome + File.separator + "core" + File.separator + "assembly"
					+ File.separator + "folder-assembly.conf";
			this.getAssemblyContext().getFileSystemOperation().createSolutionStructure(fileSystemStructure,
					versionHome);
			
			// Load Licenses
			String licensesSource = repositoryHome + File.separator + "licenses";
			String licensesTarget = versionHome + File.separator + "licenses";
			FolderTools.copyFromFolderToFolder(licensesSource, licensesTarget, true);

			String licensesReportSource = repositoryHome + File.separator + "core" + File.separator + "java"
					+ File.separator + "iesi-core" + File.separator + "target" + File.separator + "site";
			String licensesReportTarget = versionHome + File.separator + "licenses" + File.separator + "core";
			FolderTools.copyFromFolderToFolder(licensesReportSource, licensesReportTarget, true);

			// Load maven dependencies
			String mavenDependenciesSource = repositoryHome + File.separator + "core" + File.separator + "java"
					+ File.separator + "iesi-core" + File.separator + "target" + File.separator + "dependencies";
			String mavenDependenciesTarget = versionHome + File.separator + "lib";
			FolderTools.copyFromFolderToFolder(mavenDependenciesSource, mavenDependenciesTarget, true);

			//////////////////////////////////////////////////////////////////////////////////////////////////
			// Rest server
			//
			// Licenses
			licensesReportSource = repositoryHome + File.separator + "core" + File.separator + "java" + File.separator
					+ "iesi-rest-without-microservices" + File.separator + "target" + File.separator + "site";
			licensesReportTarget = versionHome + File.separator + "licenses" + File.separator + "rest";
			FolderTools.copyFromFolderToFolder(licensesReportSource, licensesReportTarget, true);

			// Dependencies
			mavenDependenciesSource = repositoryHome + File.separator + "core" + File.separator + "java"
					+ File.separator + "iesi-rest-without-microservices" + File.separator + "target" + File.separator + "dependencies";
			FolderTools.copyFromFolderToFolder(mavenDependenciesSource, mavenDependenciesTarget, true);

			//
			//////////////////////////////////////////////////////////////////////////////////////////////////

			//////////////////////////////////////////////////////////////////////////////////////////////////
			// Metadata Configuration
			//
			String metadataConfEditHome = repositoryHome + File.separator + "core" + File.separator + "metadata"
					+ File.separator + "conf";
			final File[] inputConfFolders = FolderTools.getFilesInFolder(metadataConfEditHome, "all", "");
			for (final File inputConfFolder : inputConfFolders) {
				if (inputConfFolder.isDirectory()) {
					final File[] inputConfs = FolderTools.getFilesInFolder(inputConfFolder.getAbsolutePath(), "regex",
							".+\\.yml");
					List<DataObject> confDataObjects = new ArrayList();
					for (final File inputConf : inputConfs) {
						// Read configuration
						DataObjectOperation inputDataObjectOperation = new DataObjectOperation(inputConf.getAbsolutePath());

						ObjectMapper inputObjectMapper = new ObjectMapper();
						for (DataObject dataObject : inputDataObjectOperation.getDataObjects()) {
							confDataObjects.add(dataObject);
						}
						String metadataConfFileName = inputConfFolder.getName() + "s.json";
						String metadataConfFilePath = metadataConfEditHome + File.separator + metadataConfFileName;
						
						// Write file to github repository
						inputObjectMapper.writerWithDefaultPrettyPrinter().writeValue(
								new File(metadataConfFilePath),
								confDataObjects);
						
						// Copy file to docs/_data for documentation update
						FileTools.copyFromFileToFile(metadataConfFilePath, repositoryHome + File.separator + "docs" + File.separator + "_data" + File.separator + metadataConfFileName);
					}
				}
			}
			//
			//////////////////////////////////////////////////////////////////////////////////////////////////

			//////////////////////////////////////////////////////////////////////////////////////////////////
			// Metadata Definition
			//
			String metadataDefEditHome = repositoryHome + File.separator + "core" + File.separator + "metadata"
					+ File.separator + "def";
			final File[] inputDefFolders = FolderTools.getFilesInFolder(metadataDefEditHome, "all", "");
			for (final File inputDefFolder : inputDefFolders) {
				if (inputDefFolder.isDirectory()) {
					// Tables
					final File[] inputDefTables = FolderTools.getFilesInFolder(inputDefFolder.getAbsolutePath() + File.separator + "Tables", "regex",
							".+\\.yml");
					List<DataObject> defTableDataObjects = new ArrayList();
					for (final File inputDef : inputDefTables) {
						// Read configuration
						DataObjectOperation inputDataObjectOperation = new DataObjectOperation(inputDef.getAbsolutePath());

						ObjectMapper inputObjectMapper = new ObjectMapper();
						for (DataObject dataObject : inputDataObjectOperation.getDataObjects()) {
							defTableDataObjects.add(dataObject);
						}
						String metadataDefFileName = inputDefFolder.getName() + "Tables.json";
						String metadataDefFilePath = metadataDefEditHome + File.separator + metadataDefFileName;
						
						// Write file to github repository
						inputObjectMapper.writerWithDefaultPrettyPrinter().writeValue(
								new File(metadataDefFilePath),
								defTableDataObjects);
						
						// Copy file to docs/_data for documentation update
						FileTools.copyFromFileToFile(metadataDefFilePath, repositoryHome + File.separator + "docs" + File.separator + "_data" + File.separator + "datamodel" + File.separator + metadataDefFileName);
					}

					// Objects
					final File[] inputDefObjects = FolderTools.getFilesInFolder(inputDefFolder.getAbsolutePath() + File.separator + "Objects", "regex",
							".+\\.yml");
					List<DataObject> defObjectDataObjects = new ArrayList();
					for (final File inputDef : inputDefObjects) {
						// Read configuration
						DataObjectOperation inputDataObjectOperation = new DataObjectOperation(inputDef.getAbsolutePath());

						ObjectMapper inputObjectMapper = new ObjectMapper();
						for (DataObject dataObject : inputDataObjectOperation.getDataObjects()) {
							defObjectDataObjects.add(dataObject);
						}
						String metadataDefFileName = inputDefFolder.getName() + "Objects.json";
						String metadataDefFilePath = metadataDefEditHome + File.separator + metadataDefFileName;
						
						// Write file to github repository
						inputObjectMapper.writerWithDefaultPrettyPrinter().writeValue(
								new File(metadataDefFilePath),
								defObjectDataObjects);
						
						// Copy file to docs/_data for documentation update
						FileTools.copyFromFileToFile(metadataDefFilePath, repositoryHome + File.separator + "docs" + File.separator + "_data" + File.separator + "datamodel" + File.separator + metadataDefFileName);
					}

				}
			}
			//
			//////////////////////////////////////////////////////////////////////////////////////////////////
			

			//////////////////////////////////////////////////////////////////////////////////////////////////
			// Framework Initialization
			//
			String metadataInitEditHome = repositoryHome + File.separator + "core" + File.separator + "sys"
					+ File.separator + "init";
			final File[] inputInitFolders = FolderTools.getFilesInFolder(metadataInitEditHome, "all", "");
			for (final File inputConfFolder : inputInitFolders) {
				if (inputConfFolder.isDirectory()) {
					final File[] inputConfs = FolderTools.getFilesInFolder(inputConfFolder.getAbsolutePath(), "regex",
							".+\\.yml");

					List<DataObject> confInitObjects = new ArrayList();
					for (final File inputConf : inputConfs) {
						// Read configuration
						DataObjectOperation inputInitObjectOperation = new DataObjectOperation(inputConf.getAbsolutePath());

						ObjectMapper inputObjectMapper = new ObjectMapper();
						for (DataObject dataObject : inputInitObjectOperation.getDataObjects()) {
							confInitObjects.add(dataObject);
						}
						String metadataInitFileName = inputConfFolder.getName() + ".json";
						String metadataInitFilePath = metadataInitEditHome + File.separator + metadataInitFileName;
						
						// Write file to github repository
						inputObjectMapper.writerWithDefaultPrettyPrinter().writeValue(
								new File(metadataInitFilePath),
								confInitObjects);
						
						// Copy file to docs/_data for documentation update
						FileTools.copyFromFileToFile(metadataInitFilePath, repositoryHome + File.separator + "docs" + File.separator + "_data" + File.separator + "framework" + File.separator + metadataInitFileName);
					}
				}
			}
			//
			//////////////////////////////////////////////////////////////////////////////////////////////////

			
			// Load assets into directory structure
			String fileSystemConfig = repositoryHome + File.separator + "core" + File.separator + "assembly"
					+ File.separator + "file-assembly.conf";
			file = new File(fileSystemConfig);
			bufferedReader = new BufferedReader(new FileReader(file));
			readLine = "";
			while ((readLine = bufferedReader.readLine()) != null) {
				String innerpart = readLine.trim();
				String[] parts = innerpart.split(";");
				// int assemblyCode = Integer.parseInt(parts[4]);

				// Take all items into account
				String sourcePath = parts[1].replace("#GIT_REPO#", repositoryHome);
				sourcePath = sourcePath.replace("#GIT_DEV#", developmentHome);
				String targetPath = versionHome + parts[2] + file.separator + parts[0];
				FileTools.copyFromFileToFile(sourcePath, targetPath);
			}

			// Load nodist assets into directory structure
			if (!this.isDistribution()) {
				String fileSystemConfigNoDist = developmentHome + File.separator + "core" + File.separator + "assembly"
						+ File.separator + "file-assembly-nodist.conf";
				file = new File(fileSystemConfigNoDist);
				bufferedReader = new BufferedReader(new FileReader(file));
				readLine = "";
				while ((readLine = bufferedReader.readLine()) != null) {
					String innerpart = readLine.trim();
					String[] parts = innerpart.split(";");
					// int assemblyCode = Integer.parseInt(parts[4]);

					// Take all items into account
					String sourcePath = parts[1].replace("#GIT_REPO#", repositoryHome);
					sourcePath = sourcePath.replace("#GIT_DEV#", developmentHome);
					String targetPath = versionHome + parts[2] + file.separator + parts[0];
					FileTools.copyFromFileToFile(sourcePath, targetPath);
				}
			}

			// Load configuration into directory structure
			if (this.isApplyConfiguration()) {
				FolderTools.copyFromFolderToFolder(configurationHome, versionHome, true);
			}

			// Load test assets
			if (this.isTestAssembly()) {
				String testConfigHome = repositoryHome + File.separator + "test" + File.separator + "conf";
				String versionMetadataInput = versionHome + File.separator + "metadata" + File.separator + "in"
						+ File.separator + "new";
				FolderTools.copyFromFolderToFolder(testConfigHome, versionMetadataInput, false);
			}

			// Init configuration
			// TODO optimize for configuration in yaml
			// Create Folders
			String metadataConfHome = versionHome + File.separator + "metadata" + File.separator + "conf";
			FolderTools.createFolder(metadataConfHome + File.separator + "ActionType");
			FolderTools.createFolder(metadataConfHome + File.separator + "ComponentType");
			FolderTools.createFolder(metadataConfHome + File.separator + "ConnectionType");
			FolderTools.createFolder(metadataConfHome + File.separator + "GenerationType");
			FolderTools.createFolder(metadataConfHome + File.separator + "GenerationRuleType");
			FolderTools.createFolder(metadataConfHome + File.separator + "GenerationOutputType");
			FolderTools.createFolder(metadataConfHome + File.separator + "GenerationControlType");
			FolderTools.createFolder(metadataConfHome + File.separator + "GenerationControlRuleType");
			FolderTools.createFolder(metadataConfHome + File.separator + "SubroutineType");
			FolderTools.createFolder(metadataConfHome + File.separator + "ScriptType");
			FolderTools.createFolder(metadataConfHome + File.separator + "DataframeType");
			FolderTools.createFolder(metadataConfHome + File.separator + "DataframeItemType");
			FolderTools.createFolder(metadataConfHome + File.separator + "LedgerType");
			FolderTools.createFolder(metadataConfHome + File.separator + "UserType");

			final File[] confs = FolderTools.getFilesInFolder(metadataConfHome, "regex", ".+\\.json");
			for (final File conf : confs) {
				// Read configuration
				DataObjectOperation dataObjectOperation = new DataObjectOperation(conf.getAbsolutePath());

				ObjectMapper objectMapper = new ObjectMapper();
				for (DataObject dataObject : dataObjectOperation.getDataObjects()) {
					DataObject dataObjectOutput = null;
					String output = "";
					String name = "";

					// Action Types
					if (dataObject.getType().equalsIgnoreCase("actiontype")) {
						ActionType actionType = objectMapper.convertValue(dataObject.getData(), ActionType.class);
						dataObjectOutput = new DataObject("ActionType", actionType);
						name = actionType.getName();
					}

					// Component Types
					if (dataObject.getType().equalsIgnoreCase("componenttype")) {
						ComponentType componentType = objectMapper.convertValue(dataObject.getData(),
								ComponentType.class);
						dataObjectOutput = new DataObject("ComponentType", componentType);
						name = componentType.getName();
					}

					// Connection Types
					if (dataObject.getType().equalsIgnoreCase("connectiontype")) {
						ConnectionType connectionType = objectMapper.convertValue(dataObject.getData(),
								ConnectionType.class);
						dataObjectOutput = new DataObject("ConnectionType", connectionType);
						name = connectionType.getName();
					}

					// Generation Types
					if (dataObject.getType().equalsIgnoreCase("generationtype")) {
						GenerationType generationType = objectMapper.convertValue(dataObject.getData(),
								GenerationType.class);
						dataObjectOutput = new DataObject("GenerationType", generationType);
						name = generationType.getName();
					}

					// Generation Rule Types
					if (dataObject.getType().equalsIgnoreCase("generationruletype")) {
						GenerationRuleType generationRuleType = objectMapper.convertValue(dataObject.getData(),
								GenerationRuleType.class);
						dataObjectOutput = new DataObject("GenerationRuleType", generationRuleType);
						name = generationRuleType.getName();
					}

					// Generation Output Types
					if (dataObject.getType().equalsIgnoreCase("generationoutputtype")) {
						GenerationOutputType generationOutputType = objectMapper.convertValue(dataObject.getData(),
								GenerationOutputType.class);
						dataObjectOutput = new DataObject("GenerationOutputType", generationOutputType);
						name = generationOutputType.getName();
					}

					// Generation Control Types
					if (dataObject.getType().equalsIgnoreCase("generationcontroltype")) {
						GenerationControlType generationControlType = objectMapper.convertValue(dataObject.getData(),
								GenerationControlType.class);
						dataObjectOutput = new DataObject("GenerationControlType", generationControlType);
						name = generationControlType.getName();
					}

					// Generation Control Rule Types
					if (dataObject.getType().equalsIgnoreCase("generationcontrolruletype")) {
						GenerationControlRuleType generationControlRuleType = objectMapper
								.convertValue(dataObject.getData(), GenerationControlRuleType.class);
						dataObjectOutput = new DataObject("GenerationControlRuleType", generationControlRuleType);
						name = generationControlRuleType.getName();
					}

					// Subroutine Types
					if (dataObject.getType().equalsIgnoreCase("subroutinetype")) {
						SubroutineType subroutineType = objectMapper.convertValue(dataObject.getData(),
								SubroutineType.class);
						dataObjectOutput = new DataObject("SubroutineType", subroutineType);
						name = subroutineType.getName();
					}

					// Script Types
					if (dataObject.getType().equalsIgnoreCase("scripttype")) {
						ScriptType scriptType = objectMapper.convertValue(dataObject.getData(), ScriptType.class);
						dataObjectOutput = new DataObject("ScriptType", scriptType);
						name = scriptType.getName();
					}

					// Ledger Types
					if (dataObject.getType().equalsIgnoreCase("ledgertype")) {
						LedgerType ledgerType = objectMapper.convertValue(dataObject.getData(), LedgerType.class);
						dataObjectOutput = new DataObject("LedgerType", ledgerType);
						name = ledgerType.getName();
					}

					// Dataframe Types
					if (dataObject.getType().equalsIgnoreCase("dataframetype")) {
						DataframeType dataframeType = objectMapper.convertValue(dataObject.getData(),
								DataframeType.class);
						dataObjectOutput = new DataObject("DataframeType", dataframeType);
						name = dataframeType.getName();
					}

					// DataframeItem Types
					if (dataObject.getType().equalsIgnoreCase("dataframeitemtype")) {
						DataframeItemType dataframeItemType = objectMapper.convertValue(dataObject.getData(),
								DataframeItemType.class);
						dataObjectOutput = new DataObject("DataframeItemType", dataframeItemType);
						name = dataframeItemType.getName();
					}

					// User Types
					if (dataObject.getType().equalsIgnoreCase("usertype")) {
						UserType userType = objectMapper.convertValue(dataObject.getData(), UserType.class);
						dataObjectOutput = new DataObject("UserType", userType);
						name = userType.getName();
					}

					// Write output
					output = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dataObjectOutput);
					FileTools.appendToFile(
							metadataConfHome + File.separator + dataObject.getType() + File.separator + name + ".json",
							"", output);

				}

			}
			// create folder
			/*
			 * String metadataDefHome = versionHome + File.separator + "metadata" +
			 * File.separator + "def"; String runExecHome = versionHome + File.separator +
			 * "run" + File.separator + "exec";
			 * this.getAssemblyContext().getConfigTools().addSetting(
			 * this.getAssemblyContext().getConfigTools().getSettingsConfig().getSettingPath
			 * ("metadata.repository.type"), "SQLITE"); SqliteDatabaseConnection
			 * executionServerRepositoryConnection = new SqliteDatabaseConnection(
			 * runExecHome + File.separator + "ExecutionServerRepository.db3");
			 * DataObjectOperation executionServerTablesOperation = new
			 * DataObjectOperation(null, metadataDefHome + File.separator +
			 * "ExecutionServerTables.json"); MetadataRepositoryConfigurationBack
			 * metadataRepositoryConfiguration = new MetadataRepositoryConfigurationBack(
			 * this.getAssemblyContext().getConfigTools(),
			 * executionServerRepositoryConnection);
			 * 
			 * ObjectMapper objectMapper = new ObjectMapper(); for (DataObject
			 * executionServerTable : executionServerTablesOperation.getDataObjects()) {
			 * String output = "";
			 * 
			 * // Metadata Tables if
			 * (executionServerTable.getType().equalsIgnoreCase("metadatatable")) {
			 * MetadataTable metadataTable =
			 * objectMapper.convertValue(executionServerTable.getData(),
			 * MetadataTable.class); MetadataTableConfiguration metadataTableConfiguration =
			 * new MetadataTableConfiguration( metadataTable,
			 * metadataRepositoryConfiguration); output =
			 * metadataTableConfiguration.getCreateStatement(); }
			 * 
			 * InputStream inputStream = FileTools .convertToInputStream(output,
			 * this.getAssemblyContext().getConfigTools());
			 * executionServerRepositoryConnection.executeScript(inputStream); }
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// Getters and Setters
	public AssemblyContext getAssemblyContext() {
		return assemblyContext;
	}

	public void setAssemblyContext(AssemblyContext assemblyContext) {
		this.assemblyContext = assemblyContext;
	}

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configuration) {
		this.configuration = configuration;
	}

	public boolean isTestAssembly() {
		return testAssembly;
	}

	public void setTestAssembly(boolean testAssembly) {
		this.testAssembly = testAssembly;
	}

	public String getSandbox() {
		return sandbox;
	}

	public void setSandbox(String sandbox) {
		this.sandbox = sandbox;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

	public String getDevelopment() {
		return development;
	}

	public void setDevelopment(String development) {
		this.development = development;
	}

	public boolean isDistribution() {
		return distribution;
	}

	public void setDistribution(boolean distribution) {
		this.distribution = distribution;
	}

	public boolean isApplyConfiguration() {
		return applyConfiguration;
	}

	public void setApplyConfiguration(boolean applyConfiguration) {
		this.applyConfiguration = applyConfiguration;
	}

}