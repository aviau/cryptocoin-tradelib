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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


/**
 * Class to connect to a HyperSQL database.
 *
 * @see <a href="http://www.hsqldb.org/doc/guide/">Hyper SQL guide</a>
 */
public class HyperSQLDatabaseConnection implements DatabaseConnection {

    // Inner classes 


    // Static variables


    // Instance variables

    /**
     * A list to connection to various databases.
     */
    Map<String,Connection> _dbConnectionCache = new HashMap<String, Connection>();

    /**
     * The default name of the database.
     */
    private String _defaultDatabaseName = "tradeappdb";

    /**
     * Flag to indicate an initialized driver.
     */
    private boolean driver_initialized = false;


    // Constructors


    // Methods

    /**
     * Connect to the default database via JDBC.
     *
     * @return The JDBC connection.
     */
    private Connection connect() {

	// Just use the default connect method with a null name.
	return connect( null);
    }

    /**
     * Get the JDBC connection.
     *
     * @see <a href="http://hsqldb.org/doc/guide/running-chapt.html">HyperSQL users guide</a>
     *
     * @param dbname The name of the database or null, if the default database should be used.
     *
     * @return The JDBC connection or null if no exception could be created.
     */
    private Connection connect( String dbname) {
	
	// Check, if the driver is already initialized.
	if( ! driver_initialized) {

	    initDriver();  // no => try to init the driver.
	}

	// Now check again, if the driver is now initialized.
	if( ! driver_initialized) {

	    return null;  // no => cannot create connection to the database.
	}

	// Get the name of the database.
	String db_name = ( dbname != null ? dbname : _defaultDatabaseName);

	try {

	    // Get the connection from the HyperSQL JDBC driver.
	    return DriverManager.getConnection("jdbc:hsqldb:file:" + System.getProperty("user.home") + "/.tradeapp/" + db_name, "SA", "");

	} catch( SQLException se) {

	    LogUtils.getInstance().getLogger().error( "failed to create HSQLDB JDBC connection: " + se);

	    return null;
	}
    }
    
    /**
     * Get a connection to the default database.
     *
     * @return a connection to the default database.
     */
    public Connection getConnection() {

	return getConnection( null);
    }

    /**
     * Get a connection to a database with a given name.
     *
     * @param databaseName The name of the database.
     *
     * @return A connection to the database or null, if we cannot connect.
     */
    public Connection getConnection( String databaseName) {

	if( databaseName == null) { // If no database name was given, use the default one.

	    databaseName = _defaultDatabaseName;
	}

	// Try to get a connection from the database cache.
	Connection connection = _dbConnectionCache.get( databaseName);

	if( connection == null) {  // If there is no connection in the cache.

	    // Try to connect.
	    connection = connect( databaseName);

	    if( connection != null) {

		// Put the connection into the cache.
		_dbConnectionCache.put( databaseName, connection);
	    }
	}
	
	return connection;  // Return the connection.
    }

    /**
     * Init the driver for the JDBC connection.
     *
     * @return true, if the initialization worked. False otherwise.
     */
    public boolean initDriver() {

	try {
	    Class.forName("org.hsqldb.jdbc.JDBCDriver" );

	    // Store the successful initialization in a instance variable.
	    driver_initialized = true;

	} catch(Exception e) {

	    LogUtils.getInstance().getLogger().error( "failed to load HSQLDB JDBC driver: " + e);

	    return false;  // Initialization failed!
	}

	return true;  // Initialization succeeded!
    }
}