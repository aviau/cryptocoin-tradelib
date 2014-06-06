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


/**
 * Currency implementation.
 */
public class CurrencyImpl implements Currency {

    // Static variables


    // Instance variables

    /**
     * The ISO 4217 code of this currency.
     * It seems, some exchanges use different capitalizations of the same code for their site.
     * Maybe add a map of codes, so the code for tradesite x could be requested? ( getCode( TradeSite tradeSite); ) ...
     */
    private String _code = null;

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
	_code = code. toUpperCase();
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
     * Get the ISO 4217 currency code (or similar for some newer cryptocurrencies)
     * for the currency.
     *
     * @return The code of this currency.
     */
    public String getCode() {

	return _code;
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

	return _name;
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
}