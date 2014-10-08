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

package de.andreas_rueckert.trade.site;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.persistence.database.DatabasePersistenceImpl;
import de.andreas_rueckert.persistence.database.DatabasePersistenceManager;
import de.andreas_rueckert.util.LogUtils;
import de.andreas_rueckert.util.ModuleLoader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * This class implements persistence for user accounts by using
 * a SQL database.
 */
public class TradeSiteUserAccountPersistenceSQLImpl extends DatabasePersistenceImpl implements TradeSiteUserAccountPersistence {

    // Inner classes


    // Static variables

    /**
     * The only instance of this class (singleton pattern).
     */
    private static TradeSiteUserAccountPersistenceSQLImpl _instance = null;


    // Instance variables

    /**
     * The list of currently loaded user accounts.
     */
    List<TradeSiteUserAccount> _accountList = new ArrayList<TradeSiteUserAccount>();


    // Constructors

    /**
     * Private constructor for singleton pattern.
     */
    private TradeSiteUserAccountPersistenceSQLImpl() {

	super();  // Register this object as SQL persistent.
    }
    

    // Methods

    /**
     * Add a new account.
     *
     * @param newAccount The new account to be added.
     */
    public void addAccount( TradeSiteUserAccount newAccount) {

	// Search the maximum ID of the current accounts.
	// Not very efficient, but the number of accounts shouldn't be that high.
	// Since some account's might have been deleted, the ID's might not be consecutive.
	int maxId = -1;
	for( TradeSiteUserAccount currentAccount : _accountList) {

	    if( currentAccount.getId() > maxId) {

		maxId = currentAccount.getId();
	    }
	}

	// Set a new ID for this account.
	newAccount.setId( maxId + 1);

       	// Add the new accout to the account list.
	_accountList.add( newAccount);
    }

    /**
     * Delete a user account.
     *
     * @param account The account to be deleted.
     */
    public void delete( TradeSiteUserAccount account) {

	// Remove this account from the list of accounts.
	_accountList.remove( account);
    }

    /**
     * Get all accounts for a given trade site.
     *
     * @param tradeSite The trade site, we need accounts for.
     *
     * @return A list of accounts for this trade site.
     */
    public List<TradeSiteUserAccount> getAccountsForTradeSite( TradeSite tradeSite) {

	// Create a buffer for the result.
	List<TradeSiteUserAccount> resultBuffer = new ArrayList<TradeSiteUserAccount>();

	// Loop over all the accounts.
	for( TradeSiteUserAccount currentAccount : getAllAccounts()) {

	    // If this account is for the target site.
	    if( currentAccount.getTradeSite().equals( tradeSite)) {

		resultBuffer.add( currentAccount);  // Add it to the result.
	    }
	}

	return resultBuffer;  // Return the list of accounts.
    }

    /**
     * Get all accounts, that are currently stored.
     *
     * @return all accounts, that are currently stored.
     */
    public final List<TradeSiteUserAccount> getAllAccounts() {

	return _accountList;
    }

    /**
     * Get the list of SQL statements, that generate the tables for this persistent object.
     * It's important to generate this list in a way, so that the constructor of the super class
     * can use it!
     *
     * @return The list of SQL statements, that create the tables for this persistent object.
     */
    public List<String> getCreateTableStatements() {

	// Check, if the list of statements was already generated.
	if( _createTableStatements == null) { // no?

	    // Create a new list.
	    _createTableStatements = new ArrayList<String>();

	    // Add the statements to create the tables.

	    // Statement to create the main table.
	    addCreateTableStatement( "CREATE TABLE tradeSiteUserAccounts("
				     + "id INTEGER PRIMARY KEY"
				     + ", name VARCHAR(24)" 
				     + ", tradeSite VARCHAR(24)"
				     + ", userId VARCHAR(24)"
				     + ", email VARCHAR(32)"
				     + ", password VARCHAR(32)"
				     + ", apiKey VARCHAR(64)"
				     + ", secret VARCHAR(64)"
				     + ", activated BOOLEAN"
				     + ", created TIMESTAMP NOT NULL)"
				     );
	    // Add indexes to table
	    addCreateTableStatement( "CREATE UNIQUE INDEX public.name ON public.tradeSiteUserAccounts ( name)");
	    addCreateTableStatement( "CREATE INDEX public.tradeSite ON public.tradeSiteUserAccounts ( tradeSite)");
	    addCreateTableStatement( "CREATE INDEX public.activated ON public.tradeSiteUserAccounts ( activated)");
	    addCreateTableStatement( "CREATE INDEX public.created ON public.tradeSiteUserAccounts ( created)");
	}

	// Return the list of statements.
	return _createTableStatements;
    }

    /**
     * Get a list of SQL statements to drop the tables for this persistent object.
     *
     * @return A list of SQL statements to drop the tables for this persistent object.
     */
    public List<String> getDropTableStatements() {

	// Check, if the list of drop table statements was already created.
	if( _dropTableStatements == null) {  // No?

	    // Create a new list.
	    _dropTableStatements = new ArrayList<String>();

	    // Drop the tables in reverse order. Indexes are removed automatically.
	    addDropTableStatement( "DROP TABLE tradeSiteUserAccounts IF EXISTS");
	}

	return _dropTableStatements;  // Return the list of statements.
    }

    /**
     * Get the only instance of this class (singleton pattern).
     *
     * @return The only instance of this class (singleton pattern).
     */
    public static TradeSiteUserAccountPersistenceSQLImpl getInstance() {

	if( _instance == null) {  // If there is no instance yet,

	    _instance = new TradeSiteUserAccountPersistenceSQLImpl();  // create one.
	}
	
	return _instance;  // Return the only instance.
    }

    /**
     * Load all the accounts.
     */
    public void loadAll() {

	// Read the accounts from the database.
	_accountList = readAccountsFromDatabase();
    }

    /**
     * Load all trade site accounts from the SQL database.
     *
     * @return A list with the user accounts.
     */
    private List<TradeSiteUserAccount> readAccountsFromDatabase() {

	// Statement to read all the user accounts from the database.
	String selectStatement = "SELECT * FROM tradeSiteUserAccounts";

	try {
	    ResultSet resultSet = DatabasePersistenceManager.getInstance( null).executeQuery( selectStatement);

	    // Create a buffer for the result.
	    List<TradeSiteUserAccount> resultBuffer = new ArrayList<TradeSiteUserAccount>();

	    while( resultSet.next()) {  // While there are returned records.

		// Create a new account.
		TradeSiteUserAccount newAccount = new TradeSiteUserAccount( ModuleLoader.getInstance().getRegisteredTradeSite( resultSet.getString( "tradeSite")));

		// Set the name of the account.
		newAccount.setAccountName( resultSet.getString( "name"));

		// Set the other parameters.
		newAccount.setUserId( resultSet.getString( "userId"));
		newAccount.setEmail( resultSet.getString( "email"));
		newAccount.setPassword( resultSet.getString( "password"));
		newAccount.setAPIkey( resultSet.getString( "apiKey"));
		newAccount.setSecret( resultSet.getString( "secret"));
		newAccount.setActivated( resultSet.getBoolean( "activated"));
		newAccount.setCreated( new Date( resultSet.getTimestamp( "created").getTime()));

		// Add the new account to the result buffer.
		resultBuffer.add( newAccount);
	    }

	    // Return the result buffer.
	    return resultBuffer;

	} catch( SQLException sqle) {

	    LogUtils.getInstance().getLogger().error( "TradeSiteUserAccountPersistenceSQLImpl: querying the user accounts failed: " + sqle);
	}

	return null;  // Default return value.
    }

    /**
     * Save all accounts to the database.
     */
    public void saveAll() {

	// To sync the accounts with the database, start with reading the accounts.
	List<TradeSiteUserAccount> currentlySavedAccounts = readAccountsFromDatabase();

	// A list with accounts to update in the database.
	List<TradeSiteUserAccount> accountsToUpdate = new ArrayList<TradeSiteUserAccount>();

	// A list with accounts to add to the database.
	List<TradeSiteUserAccount> accountsToAdd = new ArrayList<TradeSiteUserAccount>();

	// Now loop over the accounts in memory and check, if they are also in the database.
	nextMemAccount:
	for( TradeSiteUserAccount currentAccount : _accountList) {

	    // Check, if an account with the same name is in the database.
	    for( TradeSiteUserAccount currentDatabaseAccount : currentlySavedAccounts) {

		if( currentDatabaseAccount.getAccountName().equals( currentAccount.getAccountName())) {

		    // Check if the accounts are actually identical, or the database account needs an update.
		    if( ! currentDatabaseAccount.equals( currentAccount)) {

			// Add this account to the list of account to update.
			accountsToUpdate.add( currentAccount);
			
			continue nextMemAccount;  // Check the next account from the loaded accounts.
		    }
		}
	    }

	    // This account has to be added to the SQL database.
	    accountsToAdd.add( currentAccount);
	}
    }
}
