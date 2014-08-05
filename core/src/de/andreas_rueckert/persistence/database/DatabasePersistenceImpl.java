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
import java.util.ArrayList;
import java.util.List;


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
     * Store a reference to the persistence manager in the persistent object.
     *
     * @param dbPersistenceManager The database persistence manager.
     */
    public void setDatabasePersistenceManager( DatabasePersistenceManager dbPersistenceManager) {

	_databasePersistenceManager = dbPersistenceManager;
    }
}