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
 * This interface defines a currency pair.
 */
public interface CurrencyPair extends Comparable<CurrencyPair> {

    // Methods

    /**
     * Check, if 2 currency pairs are the same.
     *
     * @param currencyPair The second currency pair to check for equality.
     *
     * @return true, if the 2 currency pairs are equal. False otherwise.
     */
    public boolean equals( CurrencyPair currencyPair);

    /**
     * Get the queried currency.
     *
     * @return The queried currency.
     */
    public Currency getCurrency();

    /**
     * Get the name of this currency pair.
     *
     * @return The name of this currency pair.
     */
     public String getName();

    /**
     * Get the currency, that is used for payments.
     *
     * @return The currency, that is used for payments.
     */
    public Currency getPaymentCurrency();

    /**
     * Get the hashcode for a currency pair.
     *
     * @return The hash code for this currency pair.
     */
    public int hashCode();
}