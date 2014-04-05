/**
 * Java implementation for cryptocoin trading.
 *
 * Copyright (c) 2013 the authors:
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

package de.andreas_rueckert.trade;

import java.math.BigDecimal;
import java.math.MathContext;


/**
 * This class implements a price in the trading app.
 */
public class Price extends BigDecimal{

    // Static variables

    /**
     * A price of -1.
     */
    public final static Price MINUS_ONE = new Price( "-1");

    /**
     * A price of zero.
     */
    public final static Price ZERO = new Price( "0");


    // Instance variables

    /**
     * Get the currency of this price.
     */
    private Currency _currency;


    // Constructors

    /**
     * Create a new price from a string.
     *
     * @param price The price as a string.
     */
    public Price( String price) {
	super( price, MathContext.DECIMAL128);
    }

    /**
     * Create a new price from a string and a currency.
     *
     * @param price The price as a string.
     * @param currency The currency of the price.
     */
    public Price( String price, Currency currency) {
	super( price, MathContext.DECIMAL128);

	_currency = currency;
    }


    /**
     * Create a price from a BigDecimal.
     *
     * @param decimal The BigDecimal with the initial value.
     */
    public Price( BigDecimal decimal) {
	this( decimal.toString());
    }

    /**
     * Create a price from a BigDecimal and a Currency (as it should be in real life.
     *
     * @param decimal The BigDecimal with the initial value.
     * @param currency The currency of this price.
     */
    public Price( BigDecimal decimal, Currency currency) {

	this( decimal);  // Create the Price object.

	_currency = currency;  // Set the currency for this price.
    }


    // Methods

    /**
     * Add another price to this price.
     *
     * @param price The price to add.
     */
    public final Price add( Price price) {
	return new Price( super.add( price));
    }

    /**
     * Get the currency of this price.
     *
     * @return The currency of this price.
     */
    public Currency getCurrency() {
	return _currency;
    }
    
    /**
     * Subtract another price from this price.
     *
     * @param price The price to subtract.
     */
    public final Price subtract( Price price) {
	return new Price( super.subtract( price));
    }
}