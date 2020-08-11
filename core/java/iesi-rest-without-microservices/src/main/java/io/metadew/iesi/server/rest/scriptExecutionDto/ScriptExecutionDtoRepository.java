package io.metadew.iesi.server.rest.scriptExecutionDto;

import io.metadew.iesi.common.configuration.ScriptRunStatus;
import io.metadew.iesi.common.configuration.metadata.repository.MetadataRepositoryConfiguration;
import io.metadew.iesi.common.configuration.metadata.tables.MetadataTablesConfiguration;
import io.metadew.iesi.connection.tools.SQLTools;
import io.metadew.iesi.metadata.definition.script.result.key.ScriptResultKey;
import io.metadew.iesi.server.rest.executionrequest.dto.ExecutionRequestLabelDto;
import io.metadew.iesi.server.rest.script.dto.label.ScriptLabelDto;
import io.metadew.iesi.server.rest.scriptExecutionDto.tools.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Repository
public class ScriptExecutionDtoRepository implements IScriptExecutionDtoRepository {

    MetadataRepositoryConfiguration metadataRepositoryConfiguration;

    @Autowired
    ScriptExecutionDtoRepository(MetadataRepositoryConfiguration metadataRepositoryConfiguration) {
        this.metadataRepositoryConfiguration = metadataRepositoryConfiguration;
    }

    @Override
    public List<ScriptExecutionDto> getAll() {
        return getExecutionDtoList(null, null);
    }

    @Override
    public List<ScriptExecutionDto> getByRunId(String runId) {
        return getExecutionDtoList(runId, null);
    }

    @Override
    public Optional<ScriptExecutionDto> getByRunIdAndProcessId(String runId, Long processId) {
        List<ScriptExecutionDto> scriptExecutionDtoStream = getExecutionDtoList(runId, processId);
        if (scriptExecutionDtoStream.size() > 1)
            log.warn("found multiple scriptExecution for runId " + runId + " and processId" + processId);
        return scriptExecutionDtoStream.stream().findFirst();
    }

    /**
     * This method take care of running the appropriate query depending of the provided parameter
     * and call subMethod to create the ScriptExecutionDto as POJOs
     *
     * @param runId     - runId of the ScriptExecution, can be null
     * @param processId - processId of the ScriptExecution, can be null
     * @return Stream of ScriptExecutionDto that can contain zero, one or several ScriptExecutionDto
     */
    private List<ScriptExecutionDto> getExecutionDtoList(String runId, Long processId) {
        try {
            Map<ScriptResultKey, ScriptExecutionDtoBuildHelper> scriptExecutionDtoBuildHelpers = new HashMap<>();
            String SQLQuery = getSQLQuery(runId, processId);
            CachedRowSet cachedRowSet = metadataRepositoryConfiguration.getResultMetadataRepository()
                    .executeQuery(SQLQuery, "reader");

            while (cachedRowSet.next()) {
                mapRow(cachedRowSet, scriptExecutionDtoBuildHelpers);
            }

            return scriptExecutionDtoBuildHelpers.values().stream()
                    .map(ScriptExecutionDtoBuildHelper::toScriptExecutionDto)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * mapRow treats the result of the query and populate the provided Map
     *
     * @param cachedRowSet                   - cachedRowSet containing the result of the SQLquery
     * @param scriptExecutionDtoBuildHelpers - Map designed to contain the POJOs made out of the SQLQuery
     * @throws SQLException - Throws SQLException due to the param cachedRowSet
     */
    private void mapRow(CachedRowSet cachedRowSet, Map<ScriptResultKey, ScriptExecutionDtoBuildHelper> scriptExecutionDtoBuildHelpers) throws SQLException {

        String runId = cachedRowSet.getString("RUN_ID");
        Long scriptPrcId = cachedRowSet.getLong("SCRIPT_PRC_ID");

        ScriptResultKey scriptResultKey = new ScriptResultKey(runId, scriptPrcId);

        ScriptExecutionDtoBuildHelper scriptExecutionDtoBuildHelper = scriptExecutionDtoBuildHelpers.get(scriptResultKey);
        if (scriptExecutionDtoBuildHelper == null) {
            scriptExecutionDtoBuildHelper = mapScriptExecutionDtoBuildHelper(cachedRowSet);
            scriptExecutionDtoBuildHelpers.put(scriptResultKey, scriptExecutionDtoBuildHelper);
        }

        // infoType is an int that gives information about the current row data
        int infoType = cachedRowSet.getInt("INFO_TYPE");

        if (infoType == 0) {
            // ExecutionInputparams of the script
            String scriptExecInputParameterName = cachedRowSet.getString("SCRIPT_EXEC_REQ_PAR_NAME");
            // infotype 0 could not contain parameter name as it also initialize the script
            if (scriptExecInputParameterName != null &&
                    scriptExecutionDtoBuildHelper.getInputParameters().get(scriptExecInputParameterName) == null) {
                scriptExecutionDtoBuildHelper.getInputParameters()
                        .put(scriptExecInputParameterName, new ExecInputParameterDto(scriptExecInputParameterName,
                                cachedRowSet.getString("SCRIPT_EXEC_REQ_PAR_VALUE")));
            }
        } else if (infoType == 1) {
            // Infotype 1: rows are present only if containing DesignLabels
            String designLabelId = cachedRowSet.getString("SCRIPT_LBL_ID");
            if (scriptExecutionDtoBuildHelper.getDesignLabels().get(designLabelId) == null) {
                scriptExecutionDtoBuildHelper.getDesignLabels()
                        .put(designLabelId, new ScriptLabelDto(cachedRowSet.getString("SCRIPT_LBL_NM"),
                                cachedRowSet.getString("SCRIPT_LBL_VAL")));
            }
        } else if (infoType == 2) {
            // Infotype 2: rows are present only if containing Outputs of the script
            String outputName = cachedRowSet.getString("SCRIPT_OUTPUT_NM");
            if (scriptExecutionDtoBuildHelper.getOutput().get(outputName) == null) {
                scriptExecutionDtoBuildHelper.getOutput()
                        .put(outputName, new OutputDto(outputName, cachedRowSet.getString("SCRIPT_OUTPUT_VAL")));
            }
        } else if (infoType == 3) {
            // Infotype 3: rows are present only if containing Execution Labels
            String executionLabelName = cachedRowSet.getString("SCRIPT_EXE_LBL_NM");
            if (scriptExecutionDtoBuildHelper.getExecutionLabels().get(executionLabelName) == null) {
                scriptExecutionDtoBuildHelper.getExecutionLabels()
                        .put(executionLabelName, new ExecutionRequestLabelDto(executionLabelName,
                                cachedRowSet.getString("SCRIPT_EXE_LBL_VAL")));
            }
        } else {
            // else infotype 4 and 5 -> Action
            // Actions - PRK RunID + PrcID + ActionID : RunID of action is the same than the RunID of the script
            String actionId = cachedRowSet.getString("ACTION_ID");
            Long actionPrcId = cachedRowSet.getLong("ACTION_PRC_ID");
            ActionExecutionKey actionExecutionKey = new ActionExecutionKey(actionId, actionPrcId);

            ActionExecutionDtoBuildHelper actionExecutionDtoBuildHelper = scriptExecutionDtoBuildHelper.getActions().get(actionExecutionKey);
            if (actionExecutionDtoBuildHelper == null) {
                actionExecutionDtoBuildHelper = mapActionExecutionDtoBuildHelper(cachedRowSet);
                scriptExecutionDtoBuildHelper.getActions().put(actionExecutionKey, actionExecutionDtoBuildHelper);
            }

            if (infoType == 4) {
                // Infotype 4: always present if the script contains action and could contain action parameter
                // script + script action + action parameters
                String actionParameterName = cachedRowSet.getString("ACTION_PAR_NM");
                if (actionParameterName != null && actionExecutionDtoBuildHelper.getInputParameters().get(actionParameterName) == null) {
                    actionExecutionDtoBuildHelper.getInputParameters()
                            .put(actionParameterName, new ActionInputParametersDto(actionParameterName,
                                    cachedRowSet.getString("ACTION_PAR_VAL_RAW"),
                                    cachedRowSet.getString("ACTION_PAR_VAL_RESOLVED")));
                }
            } else if (infoType == 5) {
                // Infotype 5: could not be present if the action doesn't contain any action
                // script + script action + action output
                String actionOutput = cachedRowSet.getString("ACTION_OUTPUT_NM");
                if (actionExecutionDtoBuildHelper.getOutput().get(actionOutput) == null) {
                    actionExecutionDtoBuildHelper.getOutput()
                            .put(actionOutput, new OutputDto(actionOutput,
                                    cachedRowSet.getString("ACTION_OUTPUT_VAL")));
                }
            }
        }
    }

    /**
     * This methods create and return an ScriptExecutionDtoBuildHelper:
     * inputParameters, designLabels, executionLabels, actions and output are created empty
     *
     * @param cachedRowSet - item containing the fields required to create the object
     * @return ScriptExecutionDtoBuildHelper - Object similar to ScriptExecutionDto but containing Hashmap instead of List
     * @throws SQLException - Throws SQLException due to the param cachedRowSet
     */
    private ScriptExecutionDtoBuildHelper mapScriptExecutionDtoBuildHelper(CachedRowSet cachedRowSet) throws SQLException {
        return ScriptExecutionDtoBuildHelper.builder()
                .runId(cachedRowSet.getString("RUN_ID"))
                .processId(cachedRowSet.getLong("SCRIPT_PRC_ID"))
                .parentProcessId(cachedRowSet.getLong("SCRIPT_PARENT_PRC_ID"))
                .scriptId(cachedRowSet.getString("SCRIPT_ID"))
                .scriptName(cachedRowSet.getString("SCRIPT_NM"))
                .scriptVersion(cachedRowSet.getLong("SCRIPT_VRS_NB"))
                .environment(cachedRowSet.getString("ENV_NM"))
                .status(ScriptRunStatus.valueOf(cachedRowSet.getString("SCRIPT_ST_NM")))
                .startTimestamp(SQLTools.getLocalDatetimeFromSql(cachedRowSet.getString("SCRIPT_STRT_TMS")))
                .endTimestamp(SQLTools.getLocalDatetimeFromSql(cachedRowSet.getString("SCRIPT_END_TMS")))
                .inputParameters(new HashMap<>())
                .designLabels(new HashMap<>())
                .executionLabels(new HashMap<>())
                .actions(new HashMap<>())
                .output(new HashMap<>())
                .build();
    }

    /**
     * This methods create and return an ActionExecutionDtoBuildHelper: only inputParameters and output aren't completed
     *
     * @param cachedRowSet - item containing the fields required to create the object
     * @return mapActionExecutionDtoBuildHelper: object similar to ActionExecutionDto but containing map instead of list
     * @throws SQLException - Throws SQLException due to the param cachedRowSet
     */
    private ActionExecutionDtoBuildHelper mapActionExecutionDtoBuildHelper(CachedRowSet cachedRowSet) throws SQLException {
        return ActionExecutionDtoBuildHelper.builder()
                .runId(cachedRowSet.getString("RUN_ID")) // the runId of the action is the same than the runId of the script
                .processId(cachedRowSet.getLong("ACTION_PRC_ID"))
                .type(cachedRowSet.getString("ACTION_TYP_NM"))
                .name(cachedRowSet.getString("ACTION_NM"))
                .description(cachedRowSet.getString("ACTION_DSC"))
                .condition(cachedRowSet.getString("ACTION_CONDITION_VAL"))
                .errorStop(cachedRowSet.getString("ACTION_STOP_ERR_FL").equalsIgnoreCase("y") ||
                        cachedRowSet.getString("ACTION_STOP_ERR_FL").equalsIgnoreCase("yes"))
                .errorExpected(cachedRowSet.getString("ACTION_EXP_ERR_FL").equalsIgnoreCase("y") ||
                        cachedRowSet.getString("ACTION_EXP_ERR_FL").equalsIgnoreCase("yes"))
                .status(ScriptRunStatus.valueOf(cachedRowSet.getString("ACTION_ST_NM")))
                .startTimestamp(SQLTools.getLocalDatetimeFromSql(cachedRowSet.getString("ACTION_STRT_TMS")))
                .endTimestamp(SQLTools.getLocalDatetimeFromSql(cachedRowSet.getString("ACTION_END_TMS")))
                .inputParameters(new HashMap<>())
                .output(new HashMap<>())
                .build();
    }


    /**
     * This method computes the SQL Statement with or without filter depending of the given parameters.
     * The method uses Union all query to make an efficient query on the DataBase
     *
     * @param runId     - runId of the Script
     * @param processId - processId of the Script
     * @return Return a String containing the SQL statement
     */
    private String getSQLQuery(String runId, Long processId) {
        return "SELECT 0 INFO_TYPE, results.RUN_ID RUN_ID, results.PRC_ID SCRIPT_PRC_ID, " +
                "results.PARENT_PRC_ID SCRIPT_PARENT_PRC_ID, results.SCRIPT_ID SCRIPT_ID, results.SCRIPT_NM SCRIPT_NM, " +
                "results.SCRIPT_VRS_NB SCRIPT_VRS_NB, results.ENV_NM ENV_NM, results.ST_NM SCRIPT_ST_NM, " +
                "results.STRT_TMS SCRIPT_STRT_TMS, results.END_TMS SCRIPT_END_TMS, " +
                "script_exec_req_par.NAME SCRIPT_EXEC_REQ_PAR_NAME, script_exec_req_par.VALUE SCRIPT_EXEC_REQ_PAR_VALUE, " +
                "null SCRIPT_LBL_ID, null SCRIPT_LBL_NM, " +
                "null SCRIPT_LBL_VAL, null SCRIPT_OUTPUT_NM, null SCRIPT_OUTPUT_VAL, null SCRIPT_EXE_LBL_NM, " +
                "null SCRIPT_EXE_LBL_VAL, null ACTION_PRC_ID, null ACTION_ID, null ACTION_TYP_NM, null ACTION_NM, " +
                "null ACTION_DSC, null ACTION_CONDITION_VAL, null ACTION_STOP_ERR_FL, null ACTION_EXP_ERR_FL, " +
                "null ACTION_ST_NM, null ACTION_STRT_TMS, null ACTION_END_TMS, null ACTION_PAR_NM, null ACTION_PAR_VAL_RAW, " +
                "null ACTION_PAR_VAL_RESOLVED, null ACTION_OUTPUT_NM, null ACTION_OUTPUT_VAL " +

                "FROM " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ScriptResults").getName() + " results " +

                "LEFT OUTER JOIN " +
                MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ScriptExecutions").getName() + " script_exec " +
                "on results.RUN_ID = script_exec.RUN_ID AND results.PRC_ID = -1 " +
                "LEFT OUTER JOIN " +
                MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ScriptExecutionRequestParameters").getName() + " script_exec_req_par " +
                "on script_exec_req_par.SCRIPT_EXEC_REQ_ID = script_exec.SCRPT_REQUEST_ID " +

                getWhereClause(runId, processId).orElse("") +
                "UNION ALL " +
                "SELECT 1 INFO_TYPE, results.RUN_ID RUN_ID, results.PRC_ID SCRIPT_PRC_ID, " +
                "results.PARENT_PRC_ID SCRIPT_PARENT_PRC_ID, results.SCRIPT_ID SCRIPT_ID, results.SCRIPT_NM SCRIPT_NM, " +
                "results.SCRIPT_VRS_NB SCRIPT_VRS_NB, results.ENV_NM ENV_NM, results.ST_NM SCRIPT_ST_NM, " +
                "results.STRT_TMS SCRIPT_STRT_TMS, results.END_TMS SCRIPT_END_TMS, " +
                "null SCRIPT_EXEC_REQ_PAR_NAME, null SCRIPT_EXEC_REQ_PAR_VALUE, " +
                "trc_des_script_lbl.SCRIPT_LBL_ID SCRIPT_LBL_ID, " +
                "trc_des_script_lbl.NAME SCRIPT_LBL_NM, trc_des_script_lbl.VALUE SCRIPT_LBL_VAL, null SCRIPT_OUTPUT_NM, " +
                "null SCRIPT_OUTPUT_VAL, null SCRIPT_EXE_LBL_NM, null SCRIPT_EXE_LBL_VAL, null ACTION_PRC_ID, " +
                "null ACTION_ID, null ACTION_TYP_NM, null ACTION_NM, null ACTION_DSC, null ACTION_CONDITION_VAL, " +
                "null ACTION_STOP_ERR_FL, null ACTION_EXP_ERR_FL, null ACTION_ST_NM, null ACTION_STRT_TMS, " +
                "null ACTION_END_TMS, null ACTION_PAR_NM, null ACTION_PAR_VAL_RAW, null ACTION_PAR_VAL_RESOLVED, " +
                "null ACTION_OUTPUT_NM, null ACTION_OUTPUT_VAL " +

                "FROM " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ScriptResults").getName() + " results " +

                "INNER JOIN " +
                MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ScriptLabelDesignTraces").getName() + " trc_des_script_lbl " +
                "on results.RUN_ID = trc_des_script_lbl.RUN_ID AND results.PRC_ID = trc_des_script_lbl.PRC_ID " +
                getWhereClause(runId, processId).orElse("") +

                "UNION ALL " +
                "SELECT 2 INFO_TYPE, results.RUN_ID RUN_ID, results.PRC_ID SCRIPT_PRC_ID, " +
                "results.PARENT_PRC_ID SCRIPT_PARENT_PRC_ID, results.SCRIPT_ID SCRIPT_ID, results.SCRIPT_NM SCRIPT_NM, " +
                "results.SCRIPT_VRS_NB SCRIPT_VRS_NB, results.ENV_NM ENV_NM, results.ST_NM SCRIPT_ST_NM, " +
                "results.STRT_TMS SCRIPT_STRT_TMS, results.END_TMS SCRIPT_END_TMS, " +
                "null SCRIPT_EXEC_REQ_PAR_NAME, null SCRIPT_EXEC_REQ_PAR_VALUE, " +
                "null SCRIPT_LBL_ID, null SCRIPT_LBL_NM, " +
                "null SCRIPT_LBL_VAL, script_output.OUT_NM SCRIPT_OUTPUT_NM, script_output.OUT_VAL SCRIPT_OUTPUT_VAL, " +
                "null SCRIPT_EXE_LBL_NM, null SCRIPT_EXE_LBL_VAL, null ACTION_PRC_ID, null ACTION_ID, " +
                "null ACTION_TYP_NM, null ACTION_NM, null ACTION_DSC, null ACTION_CONDITION_VAL, null ACTION_STOP_ERR_FL, " +
                "null ACTION_EXP_ERR_FL, null ACTION_ST_NM, null ACTION_STRT_TMS, null ACTION_END_TMS, null ACTION_PAR_NM, " +
                "null ACTION_PAR_VAL_RAW, null ACTION_PAR_VAL_RESOLVED, null ACTION_OUTPUT_NM, null ACTION_OUTPUT_VAL " +

                "FROM " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ScriptResults").getName() + " results " +

                "INNER JOIN " +
                MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ScriptResultOutputs").getName() + " script_output " +
                "on results.RUN_ID = script_output.RUN_ID AND results.PRC_ID = script_output.PRC_ID " +
                getWhereClause(runId, processId).orElse("") +

                "UNION ALL " +
                "SELECT 3 INFO_TYPE, results.RUN_ID RUN_ID, results.PRC_ID SCRIPT_PRC_ID, " +
                "results.PARENT_PRC_ID SCRIPT_PARENT_PRC_ID, results.SCRIPT_ID SCRIPT_ID, results.SCRIPT_NM SCRIPT_NM, " +
                "results.SCRIPT_VRS_NB SCRIPT_VRS_NB, results.ENV_NM ENV_NM, results.ST_NM SCRIPT_ST_NM, " +
                "results.STRT_TMS SCRIPT_STRT_TMS, results.END_TMS SCRIPT_END_TMS, " +
                "null SCRIPT_EXEC_REQ_PAR_NAME, null SCRIPT_EXEC_REQ_PAR_VALUE, " +
                "null SCRIPT_LBL_ID, null SCRIPT_LBL_NM, " +
                "null SCRIPT_LBL_VAL, null SCRIPT_OUTPUT_NM, null SCRIPT_OUTPUT_VAL, " +
                "script_exec_lbl.NAME SCRIPT_EXE_LBL_NM, script_exec_lbl.VALUE SCRIPT_EXE_LBL_VAL, null ACTION_PRC_ID, " +
                "null ACTION_ID, null ACTION_TYP_NM, null ACTION_NM, null ACTION_DSC, null ACTION_CONDITION_VAL, " +
                "null ACTION_STOP_ERR_FL, null ACTION_EXP_ERR_FL, null ACTION_ST_NM, null ACTION_STRT_TMS, " +
                "null ACTION_END_TMS, null ACTION_PAR_NM, null ACTION_PAR_VAL_RAW, null ACTION_PAR_VAL_RESOLVED, " +
                "null ACTION_OUTPUT_NM, null ACTION_OUTPUT_VAL " +

                "FROM " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ScriptResults").getName() + " results " +

                "INNER JOIN " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ScriptExecutions").getName() + " script_exec " +
                "on results.RUN_ID = script_exec.RUN_ID " +
                "INNER JOIN " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ScriptExecutionRequests").getName() + " IESER " +
                "on script_exec.SCRPT_REQUEST_ID = IESER.SCRPT_REQUEST_ID " +
                "INNER JOIN " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ExecutionRequests").getName() + " IER " +
                "on IESER.ID = IER.REQUEST_ID " +
                "INNER JOIN " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ExecutionRequestLabels").getName() + " script_exec_lbl " +
                "on IER.REQUEST_ID = script_exec_lbl.REQUEST_ID " +
                getWhereClause(runId, processId).orElse("") +

                "UNION ALL " +
                "SELECT 4 INFO_TYPE, results.RUN_ID RUN_ID, results.PRC_ID SCRIPT_PRC_ID, " +
                "results.PARENT_PRC_ID SCRIPT_PARENT_PRC_ID, results.SCRIPT_ID SCRIPT_ID, results.SCRIPT_NM SCRIPT_NM, " +
                "results.SCRIPT_VRS_NB SCRIPT_VRS_NB, results.ENV_NM ENV_NM, results.ST_NM SCRIPT_ST_NM, " +
                "results.STRT_TMS SCRIPT_STRT_TMS, results.END_TMS SCRIPT_END_TMS, " +
                "null SCRIPT_EXEC_REQ_PAR_NAME, null SCRIPT_EXEC_REQ_PAR_VALUE, " +
                "null SCRIPT_LBL_ID, null SCRIPT_LBL_NM, " +
                "null SCRIPT_LBL_VAL, null SCRIPT_OUTPUT_NM, null SCRIPT_OUTPUT_VAL, null SCRIPT_EXE_LBL_NM, " +
                "null SCRIPT_EXE_LBL_VAL, action_trc.PRC_ID ACTION_PRC_ID, action_trc.ACTION_ID ACTION_ID, " +
                "action_trc.ACTION_TYP_NM ACTION_TYP_NM, action_trc.ACTION_NM ACTION_NM, " +
                "action_trc.ACTION_DSC ACTION_DSC, action_trc.CONDITION_VAL ACTION_CONDITION_VAL, " +
                "action_trc.STOP_ERR_FL ACTION_STOP_ERR_FL, action_trc.EXP_ERR_FL ACTION_EXP_ERR_FL, " +
                "action_res.ST_NM ACTION_ST_NM, action_res.STRT_TMS ACTION_STRT_TMS, action_res.END_TMS ACTION_END_TMS, " +
                "action_des_trc_par.ACTION_PAR_NM ACTION_PAR_NM, action_des_trc_par.ACTION_PAR_VAL ACTION_PAR_VAL_RAW, " +
                "action_trc_par.ACTION_PAR_VAL ACTION_PAR_VAL_RESOLVED, null ACTION_OUTPUT_NM, null ACTION_OUTPUT_VAL " +

                "FROM " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ScriptResults").getName() + " results " +

                "INNER JOIN " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ActionDesignTraces").getName() + " action_trc " +
                "on results.RUN_ID = action_trc.RUN_ID " +
                "INNER JOIN " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ActionResults").getName() + " action_res " +
                "on results.RUN_ID = action_res.RUN_ID AND action_trc.PRC_ID = action_res.PRC_ID AND results.PRC_ID = action_res.SCRIPT_PRC_ID " +
                "LEFT OUTER JOIN " +
                MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ActionParameterDesignTraces").getName() + " action_des_trc_par " +
                "on action_trc.RUN_ID = action_des_trc_par.RUN_ID AND action_trc.PRC_ID = action_des_trc_par.PRC_ID " +
                "LEFT OUTER JOIN " +
                MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ActionParameterTraces").getName() + " action_trc_par " +
                "on action_trc.RUN_ID = action_trc_par.RUN_ID AND action_trc.PRC_ID = action_trc_par.PRC_ID " +
                "AND action_trc_par.ACTION_PAR_NM = action_des_trc_par.ACTION_PAR_NM " +
                getWhereClause(runId, processId).orElse("") +

                "UNION ALL " +
                "SELECT 5 INFO_TYPE, results.RUN_ID RUN_ID, results.PRC_ID SCRIPT_PRC_ID, " +
                "results.PARENT_PRC_ID SCRIPT_PARENT_PRC_ID, results.SCRIPT_ID SCRIPT_ID, results.SCRIPT_NM SCRIPT_NM, " +
                "results.SCRIPT_VRS_NB SCRIPT_VRS_NB, results.ENV_NM ENV_NM, results.ST_NM SCRIPT_ST_NM, " +
                "results.STRT_TMS SCRIPT_STRT_TMS, results.END_TMS SCRIPT_END_TMS, " +
                "null SCRIPT_EXEC_REQ_PAR_NAME, null SCRIPT_EXEC_REQ_PAR_VALUE, " +
                "null SCRIPT_LBL_ID, null SCRIPT_LBL_NM, " +
                "null SCRIPT_LBL_VAL, null SCRIPT_OUTPUT_NM, null SCRIPT_OUTPUT_VAL, null SCRIPT_EXE_LBL_NM, " +
                "null SCRIPT_EXE_LBL_VAL, action_trc.PRC_ID ACTION_PRC_ID, action_trc.ACTION_ID ACTION_ID, " +
                "action_trc.ACTION_TYP_NM ACTION_TYP_NM, action_trc.ACTION_NM ACTION_NM, action_trc.ACTION_DSC ACTION_DSC, " +
                "action_trc.CONDITION_VAL ACTION_CONDITION_VAL, action_trc.STOP_ERR_FL ACTION_STOP_ERR_FL, " +
                "action_trc.EXP_ERR_FL ACTION_EXP_ERR_FL, action_res.ST_NM ACTION_ST_NM, " +
                "action_res.STRT_TMS ACTION_STRT_TMS, action_res.END_TMS ACTION_END_TMS, null ACTION_PAR_NM, " +
                "null ACTION_PAR_VAL_RAW, null ACTION_PAR_VAL_RESOLVED, action_res_output.OUT_NM ACTION_OUTPUT_NM, " +
                "action_res_output.OUT_VAL ACTION_OUTPUT_VAL " +

                "FROM " + MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ScriptResults").getName() + " results " +

                "INNER JOIN " +
                MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ActionDesignTraces").getName() + " action_trc " +
                "on results.RUN_ID = action_trc.RUN_ID " +
                "INNER JOIN " +
                MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ActionResults").getName() + " action_res " +
                "on results.RUN_ID = action_res.RUN_ID AND action_trc.PRC_ID = action_res.PRC_ID AND results.PRC_ID = action_res.SCRIPT_PRC_ID " +
                "INNER JOIN " +
                MetadataTablesConfiguration.getInstance().getMetadataTableNameByLabel("ActionResultOutputs").getName() + " action_res_output " +
                "on action_res.RUN_ID = action_res_output.RUN_ID AND action_res.PRC_ID = action_res_output.PRC_ID " +
                getWhereClause(runId, processId).orElse("") +

                ";";
    }

    /**
     * getWhereClause return a String containing or not the Where SQL statement depending if the parameters are null or not
     *
     * @param runId     - If null, doesn't provide a where clause regarding runID
     * @param processId - If null, doesn't provide a where clause regarding processID
     * @return - Return an Optional of String containing the where clause or nothing if null parameters were passed
     */
    private Optional<String> getWhereClause(String runId, Long processId) {
        List<String> conditions = new ArrayList<>();
        if (runId != null) conditions.add(" results.RUN_ID = " + SQLTools.GetStringForSQL(runId));
        if (processId != null) conditions.add(" results.prc_id = " + SQLTools.GetStringForSQL(processId));
        if (conditions.isEmpty()) return Optional.empty();
        return Optional.of(" where " + String.join(" and ", conditions) + " ");
    }
}