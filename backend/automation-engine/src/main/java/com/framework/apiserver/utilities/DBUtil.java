package com.framework.apiserver.utilities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DBUtil provides database interaction methods using Spring JdbcTemplate.
 * It supports fetching results as lists of strings, CLOBs, or single column values.
 *
 * <p>Dependencies:</p>
 * <ul>
 *   <li>Spring JdbcTemplate for database operations</li>
 *   <li>BaseClass for logging errors</li>
 * </ul>
 *
 * @see JdbcTemplate
 * @see RowMapper
 * @see Clob
 * @see SQLException
 * @see BaseClass
 *
 * @author ashish-khandelwal01
 */
@Component
public class DBUtil {

    private final JdbcTemplate jdbcTemplate;
    private final BaseClass baseClass;

    /**
     * Constructs a DBUtil instance with the required dependencies.
     *
     * @param jdbcTemplate The Spring JdbcTemplate for database operations.
     * @param baseClass The BaseClass for logging errors.
     */
    @Autowired
    public DBUtil(@Qualifier("mysqlJdbcTemplate") JdbcTemplate jdbcTemplate, BaseClass baseClass) {
        this.jdbcTemplate = jdbcTemplate;
        this.baseClass = baseClass;
    }

    /**
     * Executes a SQL query and returns the result as a list of rows,
     * where each row is a list of strings.
     *
     * @param query The SQL query to execute.
     * @return A list of rows, where each row is a list of strings.
     */
    public List<List<String>> executeQuery(String query) {
        try {
            return jdbcTemplate.query(query, (ResultSet rs) -> {
                List<List<String>> result = new ArrayList<>();
                int columnCount = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    List<String> row = new ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.add(rs.getString(i));
                    }
                    result.add(row);
                }
                return result;
            });
        } catch (Exception e) {
            baseClass.failLog("Unable to execute query: " + query);
            return new ArrayList<>();
        }
    }

    /**
     * Executes a SQL query and returns the value of a specified column as a string.
     *
     * @param query The SQL query to execute.
     * @param columnName The name of the column to retrieve.
     * @return The value of the specified column as a string, or null if an error occurs.
     */
    public String executeQueryAndReturnString(String query, String columnName) {
        try {
            return jdbcTemplate.queryForObject(query, (rs, rowNum) -> rs.getString(columnName));
        } catch (Exception e) {
            baseClass.failLog("Unable to execute query: " + query);
            return null;
        }
    }

    /**
     * Executes a SQL query and returns the value of a specified column as a list of CLOBs.
     *
     * @param query The SQL query to execute.
     * @param columnName The name of the column to retrieve.
     * @return A list of CLOBs representing the values of the specified column.
     */
    public List<Clob> executeQueryAndReturnClob(String query, String columnName) {
        try {
            return jdbcTemplate.query(query, new RowMapper<Clob>() {
                @Override
                public Clob mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getClob(columnName);
                }
            });
        } catch (Exception e) {
            baseClass.failLog("Unable to execute query: " + query);
            return new ArrayList<>();
        }
    }
}