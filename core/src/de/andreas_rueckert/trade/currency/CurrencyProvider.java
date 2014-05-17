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

import java.util.HashMap;
import java.util.Map;


/**
 * A provider to handle all kinds of currencies.
 */
public class CurrencyProvider {

    // Inner classes


    // Static variables
    
    /**
     * The only instance of this class (singleton pattern).
     */
    private static CurrencyProvider _instance = null;


    // Instance variables

    /**
     * A code => currency map of the registered currencies.
     */
    private Map< String, Currency> _registeredCurrencies = new HashMap< String, Currency>();


    // Constructors

    /**
     * Private constructor for singleton pattern.
     */
    private CurrencyProvider() {
    }


    // Methods

    /**
     * Add a new currency to the list of registered currencies.
     *
     * @param newCurrency The new currency to add to the list of registered currencies.
     */
    public final void addCurrency( Currency newCurrency) {

	// Use the currency code as the key for the currency map.
	// This code should always be unique.
	_registeredCurrencies.put( newCurrency.getCode(), newCurrency);
    }

    /**
     * Get all the registered currencies as an array.
     *
     * @return The registered currencies as an array.
     */
    @SuppressWarnings("unchecked")
    public Currency [] getRegisteredCurrencies() {

	return _registeredCurrencies.entrySet().toArray( new Currency[ _registeredCurrencies.size()]);
    }

    /**
     * Get a currency for a given code. if the currency is not registered yet,
     * it is created.
     *
     * @param code The ISO code of the currency.
     *
     * @return The currency. If it has to be created, it won't contain a name or description. Just the code.
     */
    public final Currency getCurrencyForCode( String code) {

	// Try to get a registered currency for this code.
	Currency currency = getRegisteredCurrencyForCode( code);  

	if( currency == null) {  // If this currency is not registered yet.

	    currency = new CurrencyImpl( code);  // Create a new currency.

	    addCurrency( currency);  // Add it to the list of registered currencies.
	}

	return currency;  
    }

    /**
     * Get the only instance of this class (singleton pattern).
     *
     * @return The only instance of this class.
     */
    public static CurrencyProvider getInstance() {

	if( _instance == null) {  // If there is no instance yet,

	    _instance = new CurrencyProvider();  // create one.
	}

	return _instance;  // Return the only instance.
    }

    /**
     * Get the registered currency for a given currency code.
     * This code is case insensitive! (Always converted to upper case.)
     *
     * @param code The currency code of the currency.
     *
     * @return The registered currency, or null if no currency with this code is registered.
     */
    public final Currency getRegisteredCurrencyForCode( String code) {

	// Get the currency from the map. Will return null, if no such 
	// currency is in the map.
	return _registeredCurrencies.get( code.toUpperCase());
    }
}