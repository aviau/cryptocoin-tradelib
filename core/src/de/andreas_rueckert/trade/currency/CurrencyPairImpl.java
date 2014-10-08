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
 * This class holds the data of a currency pair.
 */
public class CurrencyPairImpl implements CurrencyPair {

    // Static variables

    /**
     * The delimiter for currencies in the pair.
     */
    private static String PAIR_DELIMITER = "<=>";


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

    /**
     * Create a new currency pair object from 2 currency codes. This is just a convenience method.
     *
     * @param currencyCode The queried currency.
     * @param paymentCurrencyCode The currency to be used for the payments.
     */
    public CurrencyPairImpl( String currencyCode, String paymentCurrencyCode) {
	_currency = CurrencyProvider.getInstance().getCurrencyForCode( currencyCode);
	_paymentCurrency = CurrencyProvider.getInstance().getCurrencyForCode( paymentCurrencyCode);;
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
            return getCurrency().getCode().compareTo( pair.getCurrency().getCode());
	} else {
	    return getPaymentCurrency().getCode().compareTo( pair.getPaymentCurrency().getCode());
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

	/* System.out.println( "CurrencyPairImpl.equals: " + getCode() + " to " + currencyPair.getCode());

	if( getCurrency().getCode().equals( currencyPair.getCurrency().getCode())) {

	    System.out.println(  getCurrency().getCode() + " equals " + currencyPair.getCurrency().getCode());

	} else {

	    System.out.println(  getCurrency().getCode() + " does not equal " + currencyPair.getCurrency().getCode());

	}

	if( getPaymentCurrency().getCode().equals( currencyPair.getPaymentCurrency().getCode())) {

	    System.out.println(  getPaymentCurrency().getCode() + " equals " + currencyPair.getPaymentCurrency().getCode());

	} else {

	    System.out.println(  getPaymentCurrency().getCode() + " does not equal " + currencyPair.getPaymentCurrency().getCode());

	}
	System.out.flush(); */

	return getCurrency().getCode().equals( currencyPair.getCurrency().getCode())
	    && getPaymentCurrency().getCode().equals( currencyPair.getPaymentCurrency().getCode());
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
    public static CurrencyPairImpl getCurrencyPairForCode( String currencyPairString) {

	String [] currencies = currencyPairString.split( PAIR_DELIMITER);
	
	if( currencies.length != 2) {
	    throw new CurrencyNotSupportedException( "Cannot split " + currencyPairString + " into a currency pair");
	}

	// Convert the 2 string into Currency objects.
	Currency currency = CurrencyProvider.getInstance().getCurrencyForCode( currencies[ 0]);
	Currency paymentCurrency = CurrencyProvider.getInstance().getCurrencyForCode( currencies[ 1]);

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
     * Get the code of this currency pair.
     *
     * @return The code of this currency pair.
     */
    public String getCode() {
	return toString();
    }

    /**
     * Get the name of this currency pair.
     *
     * @return The name of this currency pair.
     */
    public String getName() {

	// Use the names of the 2 currencies.
	return getCurrency().getName() + PAIR_DELIMITER + getPaymentCurrency().getName();
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
      * Invert this pair (make the currency the payment currency and vice versa).
      *
      * @return The inverted currency pair.
      */
    public CurrencyPair invert() {

	// Just create a new pair with the currencies switched.
	return new CurrencyPairImpl( getPaymentCurrency(), getCurrency());
    }

    /**
     * Convert this currency pair to a string.
     *
     * @return This currency pair as a string.
     */
    public final String toString() {
	return getCurrency().getCode() + PAIR_DELIMITER + getPaymentCurrency().getCode();
    }
}
