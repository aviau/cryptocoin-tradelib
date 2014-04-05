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


/**
 * This class holds the data of a currency pair.
 */
public class CurrencyPairImpl implements CurrencyPair {

    // Static variables


    // Instance variables

    /**
     * The queried currency.
     */
    Currency _currency = null;

    /**
     * The currency, that is used for payments.
     */
    Currency _paymentCurrency = null;


    // Constructors

    /**
     * Create a new currency pair object.
     *
     * @param currency The queried currency.
     * @param paymentCurrency The currency to be used for the payments.
     */
    public CurrencyPairImpl( Currency currency, Currency paymentCurrency) {
	_currency = currency;
	_paymentCurrency = paymentCurrency;
    }

    
    // Methods

    /**
     * Compare 2 currency pairs.
     *
     * @param pair The other currency pair to compare.
     *
     * @return The result of the pair comparison.
     */
    @Override public final int compareTo( CurrencyPair pair) {

	if( pair == null) {
	    throw new IllegalArgumentException();
	}
 
	if( getPaymentCurrency().equals( pair.getPaymentCurrency())) {
            return getCurrency().getName().compareTo( pair.getCurrency().getName());
	} else {
	    return getPaymentCurrency().getName().compareTo( pair.getPaymentCurrency().getName());
	}
    }

    /**
     * Check, if 2 currency pairs are the same.
     *
     * @param currencyPair The second currency pair to check for equality.
     *
     * @return true, if the 2 currency pairs are equal. False otherwise.
     */
    public final boolean equals( CurrencyPair currencyPair) {

	return getCurrency().equals( currencyPair.getCurrency())
	    && getPaymentCurrency().equals( currencyPair.getPaymentCurrency());
    }

    /**
     * Overwrite compare method from Object.
     *
     * @param object The object to compaire.
     */
    public boolean equals( Object object) {

	if( object instanceof CurrencyPair) {      // If this is an CurrencyPair object.
	    return equals( (CurrencyPair)object);  // Compare the two currency pairs.
	}
 
	return false;  // If the types differ, they cannot be equal.
    }

    /**
     * Convert a string to a CurrencyPairImpl object.
     * 
     * @param currencyPairString The string to convert.
     *
     * @return A CurrencyPairImpl or null, if no matching currency pair was found.
     */
    public static CurrencyPairImpl findByString( String currencyPairString) {

	String [] currencies = currencyPairString.split( "<=>");
	
	if( currencies.length != 2) {
	    throw new CurrencyNotSupportedException( "Cannot split " + currencyPairString + " into a currency pair");
	}

	// Convert the 2 string into Currency objects.
	Currency currency = CurrencyImpl.findByString( currencies[ 0]);
	Currency paymentCurrency = CurrencyImpl.findByString( currencies[ 1]);

	// Create a new currency pair and return it.
	return new CurrencyPairImpl( currency, paymentCurrency);
    }

    /**
     * Get the queried currency.
     *
     * @return The queried currency.
     */
    public final Currency getCurrency() {
	return _currency;
    }

    /**
     * Get the name of this currency pair.
     *
     * @return The name of this currency pair.
     */
    public String getName() {
	return toString();
    }

    /**
     * Get the currency, that is used for payments.
     *
     * @return The currency, that is used for payments.
     */
    public final Currency getPaymentCurrency() {
	return _paymentCurrency;
    }

    /**
     * Get the hashcode for a currency pair.
     *
     * @return The hash code for this currency pair.
     */
    public int hashCode() {

	// Just use the hash code of the string representation.
	return toString().hashCode();
    }

    /**
     * Convert this currency pair to a string.
     *
     * @return This currency pair as a string.
     */
    public final String toString() {
	return getCurrency().getName() + "<=>" + getPaymentCurrency().getName();
    }
}
