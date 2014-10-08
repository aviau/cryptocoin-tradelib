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

import java.util.Date;


/**
 * Currency implementation.
 */
public class CurrencyImpl implements Currency {

    // Static variables


    // Instance variables

    /**
     * Flag to indicate, if this currency is currently activated. A trading could(!) honor this
     * flag and stop trading this currency.
     */
    private boolean _activated = true;

    /**
     * The ISO 4217 code of this currency.
     * It seems, some exchanges use different capitalizations of the same code for their site.
     * Maybe add a map of codes, so the code for tradesite x could be requested? ( getCode( TradeSite tradeSite); ) ...
     */
    private String _code = null;

    /**
     * The time, when this currency was created.
     */
    private Date _created;

    /**
     * A description for this currency.
     */
    private String _description = null;

    /**
     * The name of this currency.
     */
    private String _name = null;

    /**
     * The type of this currency (default is unknown).
     */
    private CurrencyType _currencyType = CurrencyType.UNKNOWN;


    // Constructors

    /**
     * Create a new currency implementation with code, name and description.
     *
     * @param code The ISO 4217 code of the currency.
     */
    public CurrencyImpl( String code) {

	// Convert the code to uppercase, just in case 2 exchanges use different
	// capitalization.
	_code = code.toUpperCase().trim();

	// Set the created date to now as the default.
	_created = new Date();
    }

    /**
     * Create a new currency implementation with code, name and description.
     *
     * @param code The ISO 4217 code of the currency.
     * @param name The name of the currency.
     */
    public CurrencyImpl( String code, String name) {

	this( code);  // Convert the code just in case...

	_name = name;
    }

    /**
     * Create a new currency implementation with code, name and description.
     *
     * @param code The ISO 4217 code of the currency.
     * @param name The name of the currency.
     * @param description A optional description of the currency.
     */
    public CurrencyImpl( String code, String name, String description) {

	this( code, name);

	_description = description;
    }

   /**
     * Create a new currency implementation with code, name and description.
     *
     * @param code The ISO 4217 code of the currency.
     * @param name The name of the currency.
     * @param description A optional description of the currency.
     * @param type The type of this currency (FIAT, cryptocurrency etc).
     */
    public CurrencyImpl( String code, String name, String description, CurrencyType currencyType) {

	this( code, name, description);

	_currencyType = currencyType;  // Store the type in the instance.
    }


    // Methods

    /**
     * Check, if 2 currencies are the same.
     *
     * @param currency The second currency to check for equality.
     *
     * @return true, if the 2 currencies are equal. False otherwise.
     */
    public final boolean equals( Currency currency) {

	//System.out.println( "CurrencyImpl.equals: " + getCode() + " to " + currency.getCode());
	//System.out.flush();

	return getCode().equals( currency.getCode());
    }


    /**
     * Get the ISO 4217 currency code (or similar for some newer cryptocurrencies)
     * for the currency.
     *
     * @return The code of this currency.
     */
    public String getCode() {

	return _code;
    }

    /**
     * Get the date, when this currency was created.
     *
     * @return The date, when this currency was created.
     */
    public Date getCreated() {

	return _created;
    }

    /**
     * Get a description of this currency. This might be null, if the currency was
     * was auto-generated from a market request.
     *
     * @return A description of this currency or null, if no description is available.
     */
    public String getDescription() {

	return _description;
    }

    /**
     * Get the name of this currency. Like 'bitcoin', or so.
     * This name might be null(!), if this currency object was auto-generated
     * from a market request.
     *
     * @return The name of this currency as a String object or null, if no name was
     *         stored.
     */
    public String getName() {

	return ( _name == null ? "<no name for " + getCode()+ ">": _name);
    }

    /**
     * Get the currency type of this currency.
     *
     * @return The type of this currency.
     */
    public final CurrencyType getCurrencyType() {

	return _currencyType;
    }

    /**
     * Check, if this currency has a given currency code.
     *
     * @param currencyCode The currency code to check for.
     *
     * @return true, if the code of this currency equals the given code.
     */
    public boolean hasCode( String currencyCode) {

	return currencyCode.equals( getCode());
    }

    /**
     * Check, if this currency has one of the given currency codes.
     *
     * @param currencyCodes The currency codes to check for as an array.
     *
     * @return true, if the code of this currency equals pne of the given codes.
     */
    public boolean hasCode( String [] currencyCodes) {

	// Loop over the given code and check them one by one.
	for( String currentCode : currencyCodes) {

	    if( hasCode( currentCode)) {  // If the current code matches,

		return true;              // return true;
	    }
	}

	return false;  // None of the codes matched.
    }

    /**
     * Check if this currency is currently activated.
     *
     * @return true, if this currency is currently activated. False otherwise.
     */
    public boolean isActivated() {

	return _activated;
    }
    
    /**
     * Check, if this currency is a crypto currency.
     *
     * @return true, if this currency is a crypto currency.
     */
    public boolean isCrypto() {

	return ( getCurrencyType() == CurrencyType.CRYPTO);
    }

    /**
     * Check, if this currency is a FIAT currency.
     *
     * @return true, if this currency is a FIAT currency.
     */
    public boolean isFIAT() {

	return ( getCurrencyType() == CurrencyType.FIAT);
    }

    /**
     * Set the activated flag of this currency.
     *
     * @param activated The new activated flag of this currency.
     */
    public void setActivated( boolean activated) {

	_activated = activated;
    }

    /**
     * Set a new date, when this currency was created.
     *
     * @param created The new date, when this currency was created.
     */
    public void setCreated( Date created) {

	_created = created;
    }

    /**
     * Set a new type for this currency.
     *
     * @param currencyType The new type to set for this currency.
     */
    public void setCurrencyType( CurrencyType currencyType) {

	_currencyType = currencyType;
    }

    /**
     * Set a new description for this currency.
     *
     * @param description The new description of this currency.
     */
    public void setDescription( String description) {

	_description = description;
    }

    /**
     * Set a new name for this currency.
     *
     * @param name The new name of this currency.
     */
    public void setName( String name) {

	_name = name;
    }


}
