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
 * This class implements an amount in the trading library.
 */
public class Amount extends BigDecimal {

    // Static variables

    /**
     * A -1 amount.
     */
    public final static Amount MINUS_ONE = new Amount( "-1");

    /**
     * A zero amount.
     */
    public final static Amount ZERO = new Amount( "0");

    /**
     * 100 as an amount.
     */
    public final static Amount HUNDRED = new Amount( "100");


    // Instance variables


    // Constructors

    /**
     * Create a new amount from a string.
     *
     * @param amount The amount as a string.
     */
    public Amount( String amount) {
	super( amount, MathContext.DECIMAL128);
    }

    /**
     * Create an amount from a BigDecimal.
     *
     * @param decimal The BigDecimal with the initial value.
     */
    public Amount( BigDecimal decimal) {
	this( decimal.toString());
    }

    
    // Methods

    /**
     * Add an amount to this amount.
     *
     * @param amount The amount to add.
     */
    public final Amount add( Amount amount) {
	return (Amount)super.add( amount);
    }

    /**
     * Return a given percentage of this amount.
     * This is just a convenience method.
     *
     * @return The given percentage of this amount as a new Amount object.
     */
    public final Amount percent( int percentage) {

	return this.percent( new BigDecimal( percentage));
    }

    /**
     * Return a given percentage of this amount.
     * This is just a convenience method.
     *
     * @return The given percentage of this amount as a new Amount object.
     */
    public final Amount percent( float percentage) {

	return this.percent( new BigDecimal( percentage));
    }

    /**
     * Return a given percentage of this amount.
     * This is just a convenience method.
     *
     * @return The given percentage of this amount as a new Amount object.
     */
    public final Amount percent( BigDecimal percentage) {

	return new Amount( super.multiply( percentage.divide( HUNDRED, MathContext.DECIMAL128)));
    }
}
