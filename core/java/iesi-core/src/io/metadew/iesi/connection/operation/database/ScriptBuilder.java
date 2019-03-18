package io.metadew.iesi.connection.operation.database;

import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sql.rowset.CachedRowSet;

import io.metadew.iesi.connection.DatabaseConnection;
import io.metadew.iesi.connection.tools.OutputTools;

public class ScriptBuilder
{

	public ScriptBuilder()
	{
	}

	// Insert Statements
	public String generateInsertStmts(DatabaseConnection databaseConnection, String schemaName, String tableName, String stmt,
				String output_loc)
	{
		return this.generateInsertStmts(databaseConnection, schemaName, tableName, stmt, "return", "", "");
	}

	public String generateInsertStmts(DatabaseConnection databaseConnection, String schemaName, String tableName,
				String sqlStatement, String outputLocation, String filePath, String fileName)
	{
		String textToWrite = "";

		if (outputLocation.equals("file"))
		{
			// Write general info
			textToWrite = "--automatically generated by ScriptBuilder";
			OutputTools.appendOutputFile(fileName, filePath, "", textToWrite);
			Date date = new Date();
			textToWrite = "--automatically generated on: " + new Timestamp(date.getTime());
			OutputTools.appendOutputFile(fileName, filePath, "", textToWrite);
			textToWrite = "--sql statement used: " + sqlStatement;
			OutputTools.appendOutputFile(fileName, filePath, "", textToWrite);
		}

		String insert_stmts = "";
		String insert_stmt_table = "";
		String insert_stmt_values = "";

		try
		{
			// Get data to buffer
			CachedRowSet crs = null;
			crs = databaseConnection.executeQuery(sqlStatement);

			// Get result set meta data
			ResultSetMetaData rsmd = crs.getMetaData();

			String col = "";
			if (rsmd.getColumnCount() == 0)
			{
				// No columns in result
				System.out.println("No columns in result");
				return "";
			}
			insert_stmt_table = "INSERT INTO ";
			if (schemaName.trim().equals(""))
			{
				insert_stmt_table += tableName;
			}
			else
			{
				insert_stmt_table += schemaName;
				insert_stmt_table += ".";
				insert_stmt_table += tableName;
			}

			// todo schema names
			insert_stmt_table = insert_stmt_table + " (";
			// Get Column Names
			int cols = rsmd.getColumnCount();

			for (int i = 1; i < cols + 1; i++)
			{
				col = rsmd.getColumnName(i);
				insert_stmt_table = insert_stmt_table + col;
				if (i != cols)
				{
					insert_stmt_table = insert_stmt_table + ",";
				}
			}

			insert_stmt_table = insert_stmt_table + ")";

			// Get Values
			StringBuffer stringBuffer = new StringBuffer();
			crs.beforeFirst();
			int type;
			while (crs.next())
			{
				if (!outputLocation.equals("file"))
				{
					stringBuffer.append(insert_stmt_table);
				}
				insert_stmt_values = " VALUES (";
				for (int i = 1; i < cols + 1; i++)
				{
					type = rsmd.getColumnType(i);
					if (type == Types.VARCHAR || type == Types.NVARCHAR || type == Types.CHAR || type == Types.NCHAR
								|| type == Types.LONGVARCHAR || type == Types.LONGNVARCHAR)
					{
						insert_stmt_values = insert_stmt_values + "'"
						// + ((crs.getObject(i) == null) ? "" : this.getSqlTools().GetStringForSQL((String) crs.getObject(i)))
						// Not included since it causes double quotes
									+ (crs.getObject(i) == null ? "" : (String)crs.getObject(i)) + "'";
					}
					else if (type == Types.DATE || type == Types.TIME || type == Types.TIMESTAMP)
					{
						// Types.TIME_WITH_TIMEZONE || type ==
						// Types.TIMESTAMP_WITH_TIMEZONE) {
						if (crs.getObject(i).equals("null") || crs.getObject(i) == null)
						{
							insert_stmt_values = insert_stmt_values + "null";
						}
						else
						{
							// insert_stmt_values = insert_stmt_values + "�DATE(" + crs.getObject(i) + ")";
							// DtDate date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(crs.getObject(i));
							String newstring = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(crs.getObject(i));
							String dateSyntax = "to_date('" + newstring + "','yyyymmdd hh24:mi:ss')";

							insert_stmt_values = insert_stmt_values + dateSyntax;
						}
					}
					else
					{
						insert_stmt_values = insert_stmt_values + crs.getObject(i);
					}

					if (i != cols)
					{
						insert_stmt_values = insert_stmt_values + ",";
					}
				}
				insert_stmt_values = insert_stmt_values + ")";
				insert_stmt_values = insert_stmt_values + ";";
				if (outputLocation.equals("file"))
				{
					textToWrite = insert_stmt_table + insert_stmt_values;
					OutputTools.appendOutputFile(fileName, filePath, "", textToWrite);
				}
				else
				{
					stringBuffer.append(insert_stmt_values);
					stringBuffer.append("\n");
				}
			}
			insert_stmts = stringBuffer.toString();

		}
		catch (Exception e)
		{
			System.out.println("Query Actions Failed");
			e.printStackTrace();
		}

		if (outputLocation.equals("file"))
		{
			return "";
		}
		else
		{
			return insert_stmts;
		}
	}
}
