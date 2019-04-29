package io.metadew.iesi.metadata_repository.repository.configuration;

import io.metadew.iesi.common.config.ConfigFile;
import io.metadew.iesi.framework.configuration.FrameworkSettingConfiguration;
import io.metadew.iesi.metadata_repository.repository.Repository;

public class ElasticSearchRepositoryConfiguration extends RepositoryConfiguration {

    private String url;


    public ElasticSearchRepositoryConfiguration(ConfigFile configFile, FrameworkSettingConfiguration frameworkSettingConfiguration) {
        super(configFile, frameworkSettingConfiguration);
    }

    @Override
    void fromConfigFile(ConfigFile configFile, FrameworkSettingConfiguration frameworkSettingConfiguration) {

        if (frameworkSettingConfiguration.getSettingPath("metadata.repository.elasticsearch.url").isPresent() &&
                configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.elasticsearch.url").get()).isPresent()) {

            url = configFile.getProperty(frameworkSettingConfiguration.getSettingPath("metadata.repository.elasticsearch.url").get()).get();
        } else {
            throw new RuntimeException("Could not initialize elasticsearch configuration");
        }
    }

    @Override
    public Repository toRepository() {
        return null;
    }

    public String getUrl() {
        return url;
    }
}