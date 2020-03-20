package io.metadew.iesi.metadata.configuration.type;

import io.metadew.iesi.framework.configuration.metadata.actiontypes.MetadataActionTypesConfiguration;
import io.metadew.iesi.metadata.definition.action.type.ActionType;

public class ActionTypeConfiguration {

    private String dataObjectType = "ActionType";

    public ActionTypeConfiguration() {
    }

    public ActionType getActionType(String actionTypeName) {
        return MetadataActionTypesConfiguration.getInstance().getActionType(actionTypeName)
                .orElseThrow(() -> new RuntimeException("action type " + actionTypeName + " not found"));
    }

    public String getDataObjectType() {
        return dataObjectType;
    }

    public void setDataObjectType(String dataObjectType) {
        this.dataObjectType = dataObjectType;
    }

}