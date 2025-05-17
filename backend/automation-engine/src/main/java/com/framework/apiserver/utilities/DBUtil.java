package com.framework.apiserver.utilities;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.sql.*;

/**
 * DBUtil is a utility class for interacting with a database.
 * It provides methods to execute SQL queries and retrieve results in various formats.
 * Extends BaseClass to utilize logging functionality.
 *
 * @see BaseClass
 * @see Properties
 * @see Connection
 * @see ResultSet
 * @see Statement
 * @see DriverManager
 * @see SQLException
 */
public class DBUtil extends BaseClass {

    /**
     * Properties object to load SQL configuration from a properties file.
     */
    public Properties sqlProperties = new Properties();

    /**
     * FileInputStream to read the SQL properties file.
     */
    public FileInputStream sqlFile;

    // Static block to load the SQL properties file.
    {
        try {
            sqlFile = new FileInputStream(Constants.sqlFilePath);
            sqlProperties.load(sqlFile);
        } catch (Exception e) {
            failLog("Unable to load sql.properties file");
        }
    }

    /**
     * Connection object to establish a connection to the database.
     */
    Connection connection;

    /**
     * ResultSet object to store the results of executed queries.
     */
    ResultSet resultSet;

    // Static block to establish a database connection.
    {
        try {
            connection = DriverManager.getConnection(
                    sqlProperties.getProperty("url"),
                    sqlProperties.getProperty("user"),
                    sqlProperties.getProperty("password")
            );
        } catch (Exception e) {
            failLog("Unable to connect to database");
        }
    }

    /**
     * Executes a SQL query and retrieves the results as a list of rows,
     * where each row is a list of strings.
     *
     * @param query The SQL query to execute.
     * @return A list of rows, where each row is a list of strings.
     */
    public List<List<String>> executeQuery(String query) {
        List<List<String>> tableData = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                ArrayList<String> tableRow = new ArrayList<>();
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    tableRow.add(resultSet.getString(i));
                }
                tableData.add(tableRow);
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            failLog("Unable to execute query: " + query);
        } finally {
            closeConnection();
        }
        return tableData;
    }

    /**
     * Executes a SQL query and retrieves a single string value from the specified column.
     *
     * @param query The SQL query to execute.
     * @param columnName The name of the column to retrieve the value from.
     * @return The string value from the specified column, or null if no result is found.
     */
    public String executeQueryAndReturnString(String query, String columnName) {
        String result = null;
        try {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                result = resultSet.getString(columnName);
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            failLog("Unable to execute query: " + query);
        } finally {
            closeConnection();
        }
        return result;
    }

    /**
     * Executes a SQL query and retrieves a list of CLOB (Character Large Object) values
     * from the specified column.
     *
     * @param query The SQL query to execute.
     * @param columnName The name of the column to retrieve the CLOB values from.
     * @return A list of CLOB values from the specified column.
     */
    public List<Clob> executeQueryAndReturnClob(String query, String columnName) {
        List<Clob> clobList = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                clobList.add(resultSet.getClob(columnName));
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            failLog("Unable to execute query: " + query);
        } finally {
            closeConnection();
        }
        return clobList;
    }

    /**
     * Closes the database connection if it is open.
     */
    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            failLog("Unable to close database connection");
        }
    }
}