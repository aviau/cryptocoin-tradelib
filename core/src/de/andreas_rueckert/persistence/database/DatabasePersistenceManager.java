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

import de.andreas_rueckert.util.LogUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A manager to handle database persistence of the trade classes.
 */
public class DatabasePersistenceManager {

    // Inner classes


    // Static variables

    /**
     * The only instance of this class (singleton pattern).
     */
    private static DatabasePersistenceManager _instance = null;


    // Instance variables

    /**
     * The current database connection.
     */
    private DatabaseConnection _connection = null;

    /**
     * the default connection to some database.
     */
    private DatabaseConnection _defaultConnection = null;

    /**
     * A list of objects, that want to store info in the database.
     */
    List<DatabasePersistence> _persistentObjects = new ArrayList<DatabasePersistence>();

    /**
     * A cache for the tables of each persistent object in the current database.
     */
    Map<DatabasePersistence,List<String>> _tableCache = new HashMap<DatabasePersistence,List<String>>();


    // Constructors

    /**
     * A private contructor for singleton pattern.
     *
     * @param connection The database connection to use, or null, if the default connection should be used.
     */
    private DatabasePersistenceManager( DatabaseConnection connection) {

	if( connection != null) {  // If there was a connection passed,

	    _connection = connection;  // Store the connection in the instance.

	} else {

	    // Get the default database connection.
	    _connection = getDefaultDatabaseConnection();
	}
    }


    // Methods

    /**
     * Create the tables for all objects in the database.
     */
    public final void createTables() {

	// Loop over all the persistent objects.
	for( DatabasePersistence currentPersistentObject : _persistentObjects) {

	    createTables( currentPersistentObject);
	}
    }

    /**
     * Create the tables for a given persistent object.
     *
     * @param persistentObject The persistent object to create the tables for.
     */
    public final void createTables( DatabasePersistence persistentObject) {

	// Execute all the statements to create tables.
	executeStatements( persistentObject.getCreateTableStatements());
    }

    /**
     * Drop all the tables of the registered persistent objects.
     */
    public final void dropTables() {

	// Loop over all the persistent objects.
	for( DatabasePersistence currentPersistentObject : _persistentObjects) {

	    dropTables( currentPersistentObject);
	}
    }

    /**
     * Drop the tables of a given persistent object.
     *
     * @param persistentObject The persistent object to drop the tables for.
     */
    public final void dropTables( DatabasePersistence persistentObject) {

	// Execute all the statements to drop tables.
	executeStatements( persistentObject.getDropTableStatements());
    }

    /**
     * Execute a query on the database.
     *
     * @param query The SQL query as a string.
     *
     * @return a result set.
     */
    public ResultSet executeQuery( String query) throws SQLException {
	
	Statement statement = null;

	try {

	    // Create a statement.
	    statement = getDatabaseConnection().getConnection().createStatement();

	    // Execute the given query.
	    ResultSet resultSet = statement.executeQuery( query);

	    // And just return the result set.
	    return resultSet;

	} catch (SQLException sqle) {

	    LogUtils.getInstance().getLogger().error( "SQL query exception: " + sqle);

	} finally {
	    
	    if( statement != null) { // Try to close statement.
		statement.close(); 
	    }
	}

	return null;  // Execution failed.
    }

    /**
     * Execute a command.
     *
     * @param command The command.
     *
     * @return true, if the statement was successfully executed. False otherwise.
     */
    public boolean executeStatement( String command) {

	Connection connection = null;
	Statement statement = null;

	try {
	    
	    if( getDatabaseConnection() == null) {

		LogUtils.getInstance().getLogger().error( "Database connection is null in DatabasePersistenceImpl, so I cannot execute '" + command + "'");

	    } else {

		// Create a SQL for this connection via the JDBC connection.
		statement = getDatabaseConnection().getConnection().createStatement();
		
		// Execute the current SQL command against this statement.
		statement.executeUpdate( command);

	    }
	} catch( SQLException se){
		
	    // Errors for the JDBC execution.
	    LogUtils.getInstance().getLogger().error( "Error in JDBC execution: " + se);
	    
	    return false;

	} catch( Exception e){
		
	    // Errors for the JDBC connection.
	    LogUtils.getInstance().getLogger().error( "Error in JDBC connecting: " + e);
	    
	    return false;
	    
	} finally {
	    
	    try {
		if( statement != null) {  // Try to close the statement.
			
		    statement.close();
		    statement = null;
		}
	    } catch( SQLException se){
	    }

	    try{
		if( connection != null) {  // Try to close the connection.

		    connection.close();
		    connection = null;
		}
	    } catch( SQLException se){

		LogUtils.getInstance().getLogger().error( "Cannot close JDBC connection: " + se);
	    }
	}

	return true;  // Statement successfully executed.
    }

    /**
     * Execute a list of commands.
     *
     * @param statements The list of commands.
     *
     * @return true, if the statements were successfully executed. False otherwise.
     */
    public boolean executeStatements( List<String> statements) {

	Connection connection = null;
	Statement statement = null;

	// Loop over the statements.
	for( String currentStatement : statements) {

	    if( ! executeStatement( currentStatement)) {

		return false;
	    }
	}

	return true;  // All statements successfully executed.
    }

    /**
     * Return the database connection.
     *
     * @return The database connection.
     */
    public DatabaseConnection getDatabaseConnection() {

	return _connection;
    }

    /**
     * Get the existing tables from the database.
     *
     * @return The names of the existing tables as a list of strings.
     *
     * @see <a href="http://stackoverflow.com/questions/7070449/how-can-i-see-table-structure-in-hsqldb">How can I see the table structure in HyperSQL</a>
     */
    public List<String> getDatabaseTables() {

	// Create a buffer for the result.
	List<String> resultBuffer = new ArrayList<String>();
	
	try {

	    // Query the tables from the information schema.
	    ResultSet resultSet = executeQuery( "Select TABLE_NAME From INFORMATION_SCHEMA.SYSTEM_TABLES Where TABLE_SCHEM = 'PUBLIC' ORDER BY UPPER(TABLE_NAME)");

	    while( resultSet.next()) {
		
		// Get the name of the current name as a string.
		String currentTableName = resultSet.getString( "TABLE_NAME");
		
		// System.out.println( "DEBUG: found table '" + currentTableName + "' in current database.");

		// Add the name to the result buffer.
		resultBuffer.add( currentTableName);
	    }
	} catch( SQLException sqle) {

	    LogUtils.getInstance().getLogger().error( "SQL error while fetching the tables from the SQL persistence: " + sqle);

	    return null;  // Just return nothing instead of half of the table names.
	}

	// Return the result.
	return resultBuffer;
    }
	    
    /**
     * Get the default database connection.
     *
     * @return The default database connection or null, if no such default connection exists.
     */
    public DatabaseConnection getDefaultDatabaseConnection() {

	if( _defaultConnection == null) {  // If there is no default connection yet,

	    _defaultConnection = new HyperSQLDatabaseConnection();  // create one for HyperSQL.
	}

	return _defaultConnection;  // Return the default connection.
    }

    /**
     * Get the only instance of this class (singleton pattern).
     *
     * @param connection An optional database connection, or null if the default database should be used.
     *
     * @return The only instance of this class (singleton pattern).
     */
    public static DatabasePersistenceManager getInstance( DatabaseConnection connection) {

	if( _instance == null) {  // If there is no instance yet

	    _instance = new DatabasePersistenceManager( connection);  // create a new one.
	}

	return _instance;  // Return the only instance of this class.
    }

    /**
     * Get the table names from the current database.
     *
     * @return A list of table names from the current database.
     */
    public List<String> getTableNames( DatabasePersistence persistentObject) {

	// Try to get a result from the cache first.
	List<String> result = _tableCache.get(persistentObject);

	if( result == null) {  // If the cache for the table names is empty.

	    // Parse the create table statements to get the table names.
	    result = parseTableNames( persistentObject);

	    // Put the result in the cache of table names.
	    _tableCache.put( persistentObject, result);
	}

	return result;  // Return the result.
    }

    /**
     * Loop over the SQL statements of the given object and try to find the table names in them.
     *
     * @param persistentObject The objects to check for the required tables.
     *
     * @return A list with table names.
     */
    private List<String> parseTableNames( DatabasePersistence persistentObject) {
	
	// Create a buffer for the result.
	List<String> resultBuffer = new ArrayList<String>();

	// Loop over the statements, that create the tables for the persistentObject.
	for( String currentSQLStatement : persistentObject.getCreateTableStatements()) {

	    // Remove leading spaces just in case...
	    currentSQLStatement = currentSQLStatement.trim();

	    // If this a statement to create a table (and not an index, or so).
	    if( currentSQLStatement.toUpperCase().startsWith( "CREATE TABLE ")) {

		// Now keep the original case of the SQL statement.
		String tableName = currentSQLStatement.substring( 13);

		// Now check for the end of the table name, which should be before the '(' starting
		// the table structure.
		int structureStart = tableName.indexOf( "(");

		if( structureStart != -1) {  // Start of the table structure found, so we should have a name...

		    tableName = tableName.substring( 0, structureStart).trim();  // Remove trailing spaces after the name.
		}

		// Check, if the name is not empty (should not happen, if it's a valid SQL statement).
		if( tableName.length() > 0) {

		    resultBuffer.add( tableName);  // Add the name to the found list of names.
		}
	    }
	}

	// Return the result buffer.
	return resultBuffer;
    }

    /**
     * Check, if a given object is already registered as a persistent object.
     *
     * @param persistentObject The object to use the SQL database.
     *
     * @return true, if the object is already registered in the manager as a persistent object. False otherwise.
     */
    public final boolean isRegisteredPersistentObject( DatabasePersistence persistentObject) {

	return _persistentObjects.contains( persistentObject);
    }

    /**
     * Add an object to the list of persistent objects.
     *
     * @param persistentObject The persistent object to add.
     */
    public final void registerPersistentObject( DatabasePersistence persistentObject) {

	// Add the object to the list of persistent objects.
	_persistentObjects.add( persistentObject);

	// Set a reference to the manager in the registered object.
	persistentObject.setDatabasePersistenceManager( this);

	// Now check, if the tables for this object exist and create them, if not.
	List<String> objectTables = getTableNames( persistentObject);

	// Now check, if all the tables already exist.
	List<String> tablesInDatabase = getDatabaseTables();

	boolean recreateTables = false;  // Default is to not create the tables.

	// Loop over the tables of the persistent object.
	for( String currentTableName : objectTables) {
	    
	    // It seems at least HyperSQL converts the table names to upper case?
	    if( ! tablesInDatabase.contains( currentTableName.toUpperCase())) {

		recreateTables = true;  // Set the flag to recreate the tables.

		System.out.println( "DEBUG: table '" + currentTableName + "' not found in current database.");
		
		break;  // And exit the loop.
	    }
	}

	if( recreateTables) {  // (Re-)create the tables for the registered object.

	    // Some of the tables might still exist, so remove them all.
	    dropTables( persistentObject);

	    createTables( persistentObject);
	}
    }
}
