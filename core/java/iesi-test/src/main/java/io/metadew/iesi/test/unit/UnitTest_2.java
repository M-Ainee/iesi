package io.metadew.iesi.test.unit;

import io.metadew.iesi.metadata.configuration.exception.MetadataAlreadyExistsException;
import io.metadew.iesi.metadata.configuration.exception.MetadataDoesNotExistException;
import io.metadew.iesi.metadata.definition.execution.ExecutionRequestBuilderException;
import io.metadew.iesi.metadata.definition.execution.script.ScriptExecutionRequestBuilderException;
import org.apache.commons.cli.ParseException;

import java.sql.SQLException;

public class UnitTest_2 {
	
	public static void main(String[] args) throws Exception {
		String inputArgs[] = new String[6];
		inputArgs[0] = ("-script");
		inputArgs[1] = ("ut-2");
		inputArgs[2] = ("-env");
		inputArgs[3] = ("dev");
		inputArgs[4] = ("-impersonation");
		inputArgs[5] = ("ut-ds-1");
		io.metadew.iesi.launch.Launcher.main(inputArgs);
	}
}