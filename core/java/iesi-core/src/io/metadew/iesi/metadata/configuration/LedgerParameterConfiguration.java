package io.metadew.iesi.metadata.configuration;

import io.metadew.iesi.connection.tools.SQLTools;
import io.metadew.iesi.framework.execution.FrameworkExecution;
import io.metadew.iesi.metadata.definition.LedgerParameter;

import javax.sql.rowset.CachedRowSet;
import java.io.PrintWriter;
import java.io.StringWriter;

public class LedgerParameterConfiguration {

    private LedgerParameter ledgerParameter;

    private FrameworkExecution frameworkExecution;

    // Constructors
    public LedgerParameterConfiguration(LedgerParameter ledgerParameter, FrameworkExecution frameworkExecution) {
        this.setLedgerParameter(ledgerParameter);
        this.setFrameworkExecution(frameworkExecution);
    }

    public LedgerParameterConfiguration(FrameworkExecution frameworkExecution) {
        this.setFrameworkExecution(frameworkExecution);
    }

    // Insert
    public String getInsertStatement(String ledgerName) {
        String sql = "";

        sql += "INSERT INTO " + this.getFrameworkExecution().getMetadataControl().getLedgerMetadataRepository()
                .getTableNameByLabel("LedgerParameters");
        sql += " (LEDGER_ID, LEDGER_PAR_NM, LEDGER_PAR_VAL) ";
        sql += "VALUES ";
        sql += "(";
        sql += "("
                + SQLTools.GetLookupIdStatement(
                this.getFrameworkExecution().getMetadataControl().getLedgerMetadataRepository()
                        .getTableNameByLabel("Ledgers"),
                "LEDGER_ID", "where LEDGER_NM = '" + ledgerName)
                + "')";
        sql += ",";
        sql += SQLTools.GetStringForSQL(this.getLedgerParameter().getName());
        sql += ",";
        sql += SQLTools.GetStringForSQL(this.getLedgerParameter().getValue());
        sql += ")";
        sql += ";";

        return sql;
    }

    public LedgerParameter getLedgerParameter(long ledgerId, String ledgerParameterName) {
        LedgerParameter ledgerParameter = new LedgerParameter();
        CachedRowSet crsLedgerParameter = null;
        String queryLedgerParameter = "select LEDGER_ID, LEDGER_PAR_NM, LEDGER_PAR_VAL from "
                + this.getFrameworkExecution().getMetadataControl().getLedgerMetadataRepository()
                .getTableNameByLabel("LedgerParameters")
                + " where LEDGER_ID = " + ledgerId + " and LEDGER_PAR_NM = '" + ledgerParameterName + "'";
        crsLedgerParameter = this.getFrameworkExecution().getMetadataControl().getDesignMetadataRepository()
                .executeQuery(queryLedgerParameter, "reader");
        try {
            while (crsLedgerParameter.next()) {
                ledgerParameter.setName(ledgerParameterName);
                ledgerParameter.setValue(crsLedgerParameter.getString("LEDGER_PAR_VAL"));
            }
            crsLedgerParameter.close();
        } catch (Exception e) {
            StringWriter StackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(StackTrace));
        }
        return ledgerParameter;
    }

    // Getters and Setters
    public LedgerParameter getLedgerParameter() {
        return ledgerParameter;
    }

    public void setLedgerParameter(LedgerParameter ledgerParameter) {
        this.ledgerParameter = ledgerParameter;
    }

    public FrameworkExecution getFrameworkExecution() {
        return frameworkExecution;
    }

    public void setFrameworkExecution(FrameworkExecution frameworkExecution) {
        this.frameworkExecution = frameworkExecution;
    }

}