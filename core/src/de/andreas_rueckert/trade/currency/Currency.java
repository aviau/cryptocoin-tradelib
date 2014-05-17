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
 * Interface for a trading currency.
 *
 * This package is meant to replace the trade.Currency* interface/enum implementation at some point,
 * because it should be easier to add new currencies here.
 */
public interface Currency {

    /**
     * Get the ISO 4217 currency code (or similar for some newer cryptocurrencies)
     * for the currency.
     *
     * @return The code of this currency.
     */
    public String getCode();

    /**
     * Get a description of this currency. This might be null, if the currency was
     * was auto-generated from a market request.
     *
     * @return A description of this currency or null, if no description is available.
     */
    public String getDescription();

    /**
     * Get the name of this currency. Like 'bitcoin', or so.
     * This name might be null(!), if this currency object was auto-generated
     * from a market request.
     *
     * @return The name of this currency as a String object or null, if no name was
     *         stored.
     */
    public String getName();
    
    /**
     * Get the currency type of this currency.
     *
     * @return The type of this currency.
     */
    public CurrencyType getCurrencyType();

    /**
     * Check, if this currency is a crypto currency.
     *
     * @return true, if this currency is a crypto currency.
     */
    public boolean isCrypto();

    /**
     * Check, if this currency is a FIAT currency.
     *
     * @return true, if this currency is a FIAT currency.
     */
    public boolean isFIAT();
}