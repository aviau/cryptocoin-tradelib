/**
 * Java implementation for cryptocoin trading.
 *
 * Copyright (c) 2014 the authors:
 * 
 * @author Andreas Rueckert <mail@andreas-rueckert.de>
 *
 * Permission is hereby granted, free of charge, to any person obtaining 
 * a copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all 
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION 
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE 
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.andreas_rueckert.persistence.database;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.util.LogUtils;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Base class for objects, that want to use database persistence.
 */
public class DatabasePersistenceImpl implements DatabasePersistence {

    // Inner classes


    // Static variables


    // Instance variables

    /**
     * The current database connection.
     */
    private DatabaseConnection _connection = null;

    /**
     * A list with statements to create the tables and the indexes for this persistence.
     */
    protected List<String> _createTableStatements = null;

    /**
     * A reference to the database persistence manager.
     */
    private DatabasePersistenceManager _databasePersistenceManager = null;

    /**
     * the default connection to some database.
     */
    private DatabaseConnection _defaultConnection = null;

    /**
     * A list with statements to drop the tables and the indexes for this persistence.
     */
    protected List<String> _dropTableStatements = null;


    // Constructors

    /**
     * Create a new database persistence instance.
     */
    public DatabasePersistenceImpl() {

	// Query the database persistence manager, if this object is already registered
	// there, and register it, if it hasn't been done already.
	if( ! DatabasePersistenceManager.getInstance( null).isRegisteredPersistentObject( this)) {

	    DatabasePersistenceManager.getInstance( null).registerPersistentObject( this);  // Register the object.
	}
    }
    

    // Methods

    /**
     * Add a statement to create a table or an index for this persistence.
     *
     * @param createStatement The new statement to add.
     */
    protected void addCreateTableStatement( String createStatement) {

	// Add a new statement to the list of create statements.
	_createTableStatements.add( createStatement);
    }

    /**
     * Add a statement to drop a table or an index for this persistence.
     *
     * @param dropStatement The new statement to add.
     */
    protected void addDropTableStatement( String dropStatement) {

	// Add a new statement to the list of drop statements.
	_dropTableStatements.add( dropStatement);
    }
 
    /**
     * Get the SQL statements to create the tables for this persistent object.
     *
     * @return The list of SQL statements to create the tables for this persistent object.
     */
    public List<String> getCreateTableStatements() {

	throw new NotYetImplementedException( "Create table statements should be added in any subclass");
    }

    /**
     * Get the SQL statements to drop the tables for this persistent object.
     *
     * @return The list of SQL statements to drop the tables for this persistent object.
     */
    public List<String> getDropTableStatements() {

	throw new NotYetImplementedException( "Drop table statements should be added in any subclass");	
    }
    
    /**
     * Load all data (usually called when the program is started.
     */
    public void loadAll() {

	throw new NotYetImplementedException( "Any derived class should overwrite the loadAll method!");
    }

    /**
     * Save all the data (usually called before the program exits).
     */
    public void saveAll() {

	throw new NotYetImplementedException( "Any derived class should overwrite the saveAll method!");
    }

    /**
     * Search for a list of records in  a table. The conditions are passed in a column => condition pattern.
     *
     * @param tableName The name of the table.
     * @param searchConditions A list of conditions to limit the search results.
     *
     * @return A list of records as column => value maps.
     */
    public List< Map< String, Object>> search( String tableName, List< String> searchConditions) {

	// Create a string buffer for the search expression.
	StringBuffer searchExpression = new StringBuffer();

	// Create a SQL WHERE expression from the list.
	for( String currentCondition : searchConditions) {

	    // If there are conditions in the expression already, use AND to combine them.
	    if( searchExpression.length() > 0) {

		searchExpression.append( " AND ");
	    }

	    // Now append the current condition to the search expression.
	    searchExpression.append( "(" + currentCondition + ")");
	}

	// Create a SQL search command from the table name and the expression.
	String searchCommand = "SELECT * FROM " + tableName;

	// If there were search conditions given, add them to the SQL expression.
	if( searchExpression.length() > 0) {

	    searchCommand += " WHERE " + searchExpression.toString();
	}

	// Create a buffer for the result.
	List< Map< String, Object>> resultBuffer = new ArrayList< Map< String, Object>>();

	try {

	    // Execute the query via the persistence manager.
	    ResultSet resultSet = null;
	    if( ( resultSet = DatabasePersistenceManager.getInstance( null).executeQuery( searchCommand)) == null) {

		// Log the problem.
		LogUtils.getInstance().getLogger().error( "Cannot insert map into database.");

		return null;  // Search failed.
	    }

	    // Get info on the result set.
	    ResultSetMetaData metaData = resultSet.getMetaData();

	    // Convert the meta data to a list of column names.
	    List< String> columnNames = new ArrayList< String>();

	    // Loop over the columns to fetch the data.
	    for( int columnIndex = 0; columnIndex < metaData.getColumnCount(); ++columnIndex) {
		
		// Add the name of the current column to the list of column names.
		columnNames.add( metaData.getColumnName( columnIndex));
	    }
	
	    // Convert the result set to a list of maps.
	    while( resultSet.next()) {
		
		// Create a map for the current row.
		Map< String, Object> currentRow = new HashMap< String, Object>();

		// Loop over the columns to fetch the data.
		for( String currentColumnName : columnNames) {
		
		    currentRow.put( currentColumnName, resultSet.getObject( currentColumnName));
		}

		// Add the current map to the result buffer.
		resultBuffer.add( currentRow);
	    }
	    
	} catch( SQLException sqle) {  // Error while reading from the result set.

	    LogUtils.getInstance().getLogger().error( "SQL query exception while resding the result set from a query: " + sqle);
	    
	    return null;  // No data to return...
	}

	return resultBuffer;  // Return the list of maps.
    }

    /**
     * Store a reference to the persistence manager in the persistent object.
     *
     * @param dbPersistenceManager The database persistence manager.
     */
    public void setDatabasePersistenceManager( DatabasePersistenceManager dbPersistenceManager) {

	_databasePersistenceManager = dbPersistenceManager;
    }

    /**
     * Store an assoc array into the database.
     *
     * @param tableName The name of the database table.
     * @param valueMap The key=>value pairs to store in the database.
     */
    public void storeMap( String tableName, Map<String, String> valueMap) {

	// Create a buffer for the column names and the values.
	StringBuffer columnList = new StringBuffer();
	StringBuffer valueList = new StringBuffer();
	
	// Create a list of columns and a list of values from the map.
	for( Map.Entry<String, String> currentEntry : valueMap.entrySet()) {

	    // If there are entries in the list already, seperate them with a colon.
	    if( columnList.length() > 0) {

		columnList.append( ",");
		valueList.append( ",");  // Same for the values, since the 2 lists should have the same length.
	    }

	    // Now append the actual name and value to the 2 lists.
	    columnList.append( " " + currentEntry.getKey());
	    valueList.append( " " + currentEntry.getValue());
	}

	// Create a SQL insert statement from the data.
	String insertCommand = "INSERT INTO " 
	    + tableName
	    + " ( "
	    + columnList.toString()
	    + " ) VALUES ( " 
	    + valueList.toString()
	    + ")";
	
	System.out.println( "DEBUG: insert statement is: " + insertCommand);

	// Execute the statement via the persistence manager.
	if( ! DatabasePersistenceManager.getInstance( null).executeStatement( insertCommand)) {

	    // Log the problem.
	    LogUtils.getInstance().getLogger().error( "Cannot insert map into database.");
	}
    }
}