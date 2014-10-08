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

package de.andreas_rueckert.trade.currency;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.persistence.database.DatabasePersistenceImpl;
import de.andreas_rueckert.persistence.database.DatabasePersistenceManager;
import de.andreas_rueckert.util.LogUtils;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A provider to handle all kinds of currencies.
 */
public class CurrencyProvider extends DatabasePersistenceImpl {

    // Inner classes


    // Static variables
    
    /**
     * The only instance of this class (singleton pattern).
     */
    private static CurrencyProvider _instance = null;


    // Instance variables

    /**
     * Flag to indicate, if requested currencies should automatically be added, if they aren't
     * registered yet.
     */
    private boolean _autoAddCurrencies = true;

    /**
     * A code => currency map of the registered currencies.
     */
    private Map< String, Currency> _registeredCurrencies = new HashMap< String, Currency>();


    // Constructors

    /**
     * Private constructor for singleton pattern.
     */
    private CurrencyProvider() {

	super();  // Register this object as SQL persistent.
    }


    // Methods

    /**
     * Add a new currency to the list of registered currencies.
     *
     * @param newCurrency The new currency to add to the list of registered currencies.
     *
     * @return true, if the currency was successfully added. False, if a currency with this code was already in the map.
     */
    public final synchronized boolean addCurrency( Currency newCurrency) {

	// Get the code of the new currency.
	String currencyCode = newCurrency.getCode();

	// If the currency is already in the list, return false.
	if( _registeredCurrencies.containsKey( currencyCode)) {

	    return false;
	}

	// Use the currency code as the key for the currency map.
	// This code should always be unique.
	_registeredCurrencies.put( currencyCode, newCurrency);

	return true;  // Adding the new currency worked.
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
	    addCreateTableStatement( "CREATE TABLE currencies("
				     + "id INTEGER PRIMARY KEY"
				     + ", code VARCHAR( 8)"
				     + ", name VARCHAR( 24)"
				     + ", description VARCHAR( 128)"
				     + ", type VARCHAR( 8)"
				     + ", activated BOOLEAN"
				     + ", created TIMESTAMP NOT NULL)"
				     );

	    // Add indexes to the table.
	    addCreateTableStatement( "CREATE UNIQUE INDEX public.code ON public.currencies ( code)");
	    addCreateTableStatement( "CREATE INDEX public.name ON public.currencies ( name)");
	    addCreateTableStatement( "CREATE INDEX public.type ON public.currencies ( type)");
            addCreateTableStatement( "CREATE INDEX public.created ON public.currencies ( created)");
	}

	// Return the list of statements.
	return _createTableStatements;
    }

    /**
     * Get a currency for a given code. if the currency is not registered yet,
     * it is created.
     *
     * @param code The ISO code of the currency.
     *
     * @return The currency. If it has to be created, it won't contain a name or description. Just the code.
     */
    public final synchronized Currency getCurrencyForCode( String code) {

	// Try to get a registered currency for this code.
	Currency currency = getRegisteredCurrencyForCode( code);  

	if( currency == null) {  // If this currency is not registered yet.

	    if( isAutoAddingEnabled()) {  // If new currencies should be added automatically
		
		currency = new CurrencyImpl( code);  // Create a new currency.
	    
		addCurrency( currency);  // Add it to the list of registered currencies.
		
		System.out.println( "DEBUG: CurrencyProvider automatically added new currency " + code);
		
	    } else {

		throw new CurrencyNotSupportedException( "Currency " + code + " is not registered in the CurrecyProvider, and auto-adding is disabled.");
	    }
	}

	return currency;  
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
	    addDropTableStatement( "DROP TABLE currencies IF EXISTS");
	}

	return _dropTableStatements;  // Return the list of statements.
    }

    /**
     * Get the only instance of this class (singleton pattern).
     *
     * @return The only instance of this class.
     */
    public static synchronized CurrencyProvider getInstance() {

	if( _instance == null) {  // If there is no instance yet,

	    _instance = new CurrencyProvider();  // create one.
	}

	return _instance;  // Return the only instance.
    }

    /**
     * Get all the registered currencies as an array.
     *
     * @return The registered currencies as an array.
     */
    @SuppressWarnings("unchecked")
    public final synchronized Currency [] getRegisteredCurrencies() {

	return _registeredCurrencies.values().toArray( new Currency[ _registeredCurrencies.size()]);
    }

    /**
     * Get the registered currency for a given currency code.
     * This code is case insensitive! (Always converted to upper case.)
     *
     * @param code The currency code of the currency.
     *
     * @return The registered currency, or null if no currency with this code is registered.
     */
    public final synchronized Currency getRegisteredCurrencyForCode( String code) {

	// Get the currency from the map. Will return null, if no such 
	// currency is in the map.
	return _registeredCurrencies.get( code.toUpperCase().trim());
    }
    
    /**
     * Check, if new currencies are automatically added.
     *
     * @return true, if new currencies are automatically added. False otherwise.
     */
    public final boolean isAutoAddingEnabled() {

	// Return the flag for auto adding currencies.
	return _autoAddCurrencies;
    }

    /**
     * Load all the currencies.
     */
    public void loadAll() {
	
	// Read all the currencies and set the registered currencies from them.
	_registeredCurrencies = readAll();

	// If an error occurred, just use an empty map as the default.
	if( _registeredCurrencies == null) {

	    _registeredCurrencies = new HashMap<String, Currency>();
	}
    }

    /**
     * Merge 2 lists of currencies into a new list of merged currencies.
     *
     * @param registeredCurrencies The registered currencies in ram.
     * @param mergeCurrencies The currencies in the database, that should be merged (not all(!) currencies from the database).
     *
     * @return A list of merged currencies.
     */
    List<Currency> mergeCurrencies( Map<String,Currency> registeredCurrencies, List<Currency> mergeCurrencies) {

	// Create a buffer for the result.
	List<Currency> resultBuffer = new ArrayList<Currency>();

	// Check, if both lists have the same size, so no currencies could be lost!
	if( registeredCurrencies.size() != mergeCurrencies.size()) {

	    LogUtils.getInstance().getLogger().error( "Cannot merge new currencies to registered currencies! Size is differing!");

	    return null;  // In case of an error..
	}

	// Loop over the currencies
	for( Currency currentCurrency : mergeCurrencies) {

	    // Get the matching currency from the registered currencies.
	    Currency registeredCurrency = registeredCurrencies.get( currentCurrency.getCode());

	    if( registeredCurrency == null) {  // Should never happen.
		
		LogUtils.getInstance().getLogger().error( "No registered currency found for merge currency '" 
							  + currentCurrency.getCode() 
							  + "'. Aborting.");

		return null;
	    }

	    // Merge the 2 currencies.
	    resultBuffer.add( mergeCurrency( registeredCurrency, currentCurrency));
	}

	return resultBuffer;  // Return the buffer.
    }

    /**
     * Merge a registered currency with a loaded currency and return the resulting currency.
     *
     * @param registeredCurrency The currency, that is currently loaded in memory.
     * @param loadedCurrency The currency, that was just loaded from a database, or another media.
     *
     * @return The merged currency.
     */
    Currency mergeCurrency( Currency registeredCurrency, Currency loadedCurrency) {

	// Both currencies got the same code, so just use one of them.
	Currency resultingCurrency = new CurrencyImpl( registeredCurrency.getCode());

	// If the registered currency already has a name, use this one.
	if( registeredCurrency.getName().indexOf( "<no name for ") != -1) {  // No name yet given for the registered currency.

	    // If the loaded currency has a name, use this one.
	    if( ( loadedCurrency.getName() != null) 
		&& ( loadedCurrency.getName().length() > 0)) {

		resultingCurrency.setName( loadedCurrency.getName());
	    }

	} else {  // Use the name of the registered currency in memory.
	    
	    resultingCurrency.setName( registeredCurrency.getName());
	}

	// Use the registered description by default.
	if( registeredCurrency.getDescription() != null) {

	    resultingCurrency.setDescription( registeredCurrency.getDescription());

	} else {

	    resultingCurrency.setDescription( loadedCurrency.getDescription());
	}
	    
	// Try to find a currency type other than unknown.
	if( registeredCurrency.getCurrencyType().equals( CurrencyType.UNKNOWN)) {

	    resultingCurrency.setCurrencyType( loadedCurrency.getCurrencyType());

	} else {

	    resultingCurrency.setCurrencyType( registeredCurrency.getCurrencyType());
	} 
	

	// Use the earlier creation date of the 2 currencies.
	if( registeredCurrency.getCreated().compareTo( loadedCurrency.getCreated()) < 0) {

	    // The registered currency is older.
	    resultingCurrency.setCreated( registeredCurrency.getCreated());

	} else {

	    // The loaded currency is older.
	    resultingCurrency.setCreated( loadedCurrency.getCreated());
	}

	// If one of the currencies is activated, also activate the result.
	resultingCurrency.setActivated( registeredCurrency.isActivated() || loadedCurrency.isActivated());

	return resultingCurrency;  // Return the merged currency.
    }

    /**
     * Read all the currencies from the database and return them as a map.
     *
     * @return The currencies as a code => currency mapping or null, if an error occurred.
     */
    private Map<String,Currency> readAll() {

	// Statement to read all the currencies from the database.
	String selectStatement = "SELECT * FROM currencies";

	try {
	    ResultSet resultSet = DatabasePersistenceManager.getInstance( null).executeQuery( selectStatement);

	    // Create a buffer for the result.
	    Map<String,Currency> resultBuffer = new HashMap<String,Currency>();

	    while( resultSet.next()) {  // While there are returned records.

		// Get the code of the currency.
		String code = resultSet.getString( "code");

		// Create a new currency for the code and add the data to it.
		Currency newCurrency = new CurrencyImpl( code);
		
		newCurrency.setName( resultSet.getString( "name"));
		newCurrency.setDescription( resultSet.getString( "description"));
		newCurrency.setCurrencyType( CurrencyType.valueOf( resultSet.getString( "type")));
		newCurrency.setActivated( resultSet.getBoolean( "activated"));
		newCurrency.setCreated( new Date( resultSet.getTimestamp( "created").getTime()));

		// Add the new currency to the result buffer.
		resultBuffer.put( code, newCurrency);
	    }

	    // Return the result buffer.
	    return resultBuffer;

	} catch( SQLException sqle) {

	    LogUtils.getInstance().getLogger().error( "CurrencyProvider: loading the currencies failed: " + sqle);
	}

	return null;  // In case of an error.
    }

    /**
     * Save all the currencies.
     */
    public void saveAll() {
	
	// Start with reading all the currencies to figure, which are completely new, and which should be merged.
	Map<String,Currency> storedCurrencies = readAll();

	// List of new currencies, that are not yet in the database.
	List<Currency> newCurrencies = new ArrayList<Currency>();
	
	// List of currencies, that has to be merge with the database currencies.
	List<Currency> mergeCurrencies = new ArrayList<Currency>();

	// Loop over the registered currencies to find all the new currencies.
	for( Map.Entry currentCurrency : _registeredCurrencies.entrySet()) {

	    // Get the code of the current currency.
	    String currentCode = (String)currentCurrency.getKey();

	    // Check, if a currency with the current code is already in the database.
	    if( ! storedCurrencies.containsKey( currentCode)) {

		// This currency is completely new to the database.
		newCurrencies.add( (Currency)currentCurrency.getValue());

	    } else {  // This currency has to by merged into the database.

		mergeCurrencies.add( (Currency)currentCurrency.getValue());
	    }
	}

	// Merge the currencies, that are in the in-memory list and in the database.
	List<Currency> mergedCurrencies = mergeCurrencies( _registeredCurrencies, mergeCurrencies);

	// Create a buffer for the update statements.
	List<String> updateStatements = new ArrayList<String>();

	// Create an update statement for each currency.
	for( Currency currentCurrency : mergedCurrencies) {

	    // Create an update statement for the current currency.
	    String updateStatement = "UPDATE currencies SET "
		+ "name=" + currentCurrency.getName() + ","
		+ "description=" + ( currentCurrency.getDescription() == null ? "" : currentCurrency.getDescription()) + ","
		+ "type=" + currentCurrency.getCurrencyType().name() + ","
		+ "activated=" + ( currentCurrency.isActivated() ? "TRUE" : "FALSE") + ","
		+ "created=" + new Timestamp( currentCurrency.getCreated().getTime())
		+ " WHERE code=" + currentCurrency.getCode();

	    // Add the statement to the buffer.
	    updateStatements.add( updateStatement);
	}

	// Execute all the update statements.
	if( ! DatabasePersistenceManager.getInstance( null).executeStatements( updateStatements)) {

	    LogUtils.getInstance().getLogger().error( "CurrencyProvider: error while updating the currencies in the database");
	}

	// Add the new currencies to the database.

	// Create a buffer for the insert statements.
	List<String> insertStatements = new ArrayList<String>();

	// Create an insert statement for each currency.
	for( Currency currentCurrency : newCurrencies) {

	    // Create a insert statement for the curren currency.
	    String insertStatement = "INSERT INTO currencies " 
		+ "(code,name,description,type,activated,created) " 
		+ "VALUES (" + currentCurrency.getCode() + "," 
		+ currentCurrency.getName() + "," 
		+ ( currentCurrency.getDescription() == null ? "" : currentCurrency.getDescription()) + ","
		+ currentCurrency.getCurrencyType().name() + "," 
		+ ( currentCurrency.isActivated() ? "TRUE" : "FALSE") + ","
		+ new Timestamp( currentCurrency.getCreated().getTime())
		+ ")";

	    // Add the statement to the list of statements.
	    insertStatements.add( insertStatement);
	}

	// Execute all the statements.
	if( ! DatabasePersistenceManager.getInstance( null).executeStatements( insertStatements)) {

	    LogUtils.getInstance().getLogger().error( "CurrencyProvider: error while adding new currencies to the database");
	}

	throw new NotYetImplementedException( "Saving all the currencies is not yet implemented");
    }
}
