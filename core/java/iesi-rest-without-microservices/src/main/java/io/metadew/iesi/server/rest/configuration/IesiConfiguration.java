package io.metadew.iesi.server.rest.configuration;

import io.metadew.iesi.framework.configuration.metadata.MetadataConfiguration;
import io.metadew.iesi.framework.definition.FrameworkInitializationFile;
import io.metadew.iesi.framework.execution.FrameworkExecutionContext;
import io.metadew.iesi.framework.instance.FrameworkInstance;
import io.metadew.iesi.metadata.configuration.UserConfiguration;
import io.metadew.iesi.metadata.configuration.component.ComponentConfiguration;
import io.metadew.iesi.metadata.configuration.connection.ConnectionConfiguration;
import io.metadew.iesi.metadata.configuration.environment.EnvironmentConfiguration;
import io.metadew.iesi.metadata.configuration.execution.ExecutionRequestConfiguration;
import io.metadew.iesi.metadata.configuration.execution.script.ScriptExecutionConfiguration;
import io.metadew.iesi.metadata.configuration.impersonation.ImpersonationConfiguration;
import io.metadew.iesi.metadata.configuration.script.ScriptConfiguration;
import io.metadew.iesi.metadata.definition.Context;
import io.metadew.iesi.runtime.ExecutionRequestExecutorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;

import java.sql.SQLException;

@Configuration
public class IesiConfiguration {

    @Value("${iesi.home}")
    private String frameworkHome;

    @Bean
    @Order(0)
    public FrameworkInstance frameworkInstance(FrameworkInitializationFile frameworkInitializationFile, FrameworkExecutionContext frameworkExecutionContext) throws SQLException {
        io.metadew.iesi.framework.configuration.Configuration.getInstance();
        MetadataConfiguration.getInstance();
        return FrameworkInstance.getInstance();
    }

    @Bean
    FrameworkExecutionContext frameworkExecutionContext() {
        return new FrameworkExecutionContext(new Context("restserver", ""));
    }

    @Bean FrameworkInitializationFile frameworkInitializationFile() {
        return new FrameworkInitializationFile(System.getProperty("iesi.ini", "iesi-conf.ini"));
    }

    @Bean
    @DependsOn("frameworkInstance")
    ExecutionRequestExecutorService executorService() {
        return ExecutionRequestExecutorService.getInstance();
    }

    @Bean
    @DependsOn("frameworkInstance")
    public ConnectionConfiguration connectionConfiguration() {
        return ConnectionConfiguration.getInstance();
    }

    @Bean
    @DependsOn("frameworkInstance")
    public EnvironmentConfiguration environmentConfiguration() {
        return EnvironmentConfiguration.getInstance();
    }

    @Bean
    @DependsOn("frameworkInstance")
    public ImpersonationConfiguration impersonationConfiguration() {
        return ImpersonationConfiguration.getInstance();
    }

    @Bean
    @DependsOn("frameworkInstance")
    public ScriptConfiguration scriptConfiguration() {
        return ScriptConfiguration.getInstance();
    }
    @Bean

    @DependsOn("frameworkInstance")
    public ScriptExecutionConfiguration scriptExecutionConfiguration() {
        return ScriptExecutionConfiguration.getInstance();
    }

    @Bean
    @DependsOn("frameworkInstance")
    public UserConfiguration userConfiguration() {
        return new UserConfiguration();
    }

    @Bean
    @DependsOn("frameworkInstance")
    public ComponentConfiguration componentConfiguration() {
        return ComponentConfiguration.getInstance();
    }

    @Bean
    @DependsOn("frameworkInstance")
    public ExecutionRequestConfiguration executionRequestConfiguration() {
        return ExecutionRequestConfiguration.getInstance();
    }

}
