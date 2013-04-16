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

import de.andreas_rueckert.trade.site.TradeSite;
// import java.math.BigDecimal;


/**
 * Interface for classes to hold the values of a ticker state. 
 * These are only bitcoin values for now.
 */
public interface Ticker {
    
    // Static variables


    // Methods

    /**
     * Get the BTC rate for buy orders.
     *
     * @return The BTC rate for buy orders.
     */
    public Price getBuy();

    /**
     * Get the currency pair used in this ticker object.
     *
     * return The currency pair used in this ticker object.
     */
    public CurrencyPair getCurrencyPair();
    
    /**
     * Get the expiration time of this ticker object.
     *
     * @return The expiration time of this ticker object in millisecones since the GMT epoch.
     */
    public long getExpirationTime();

    /**
     * Get the BTC rate for sell orders.
     *
     * @return The BTC rate for sell orders.
     */
    public Price getSell();

    /**
     * Get the trading site, this ticker belongs to.
     *
     * @return The trading site, this ticker belongs to.
     */
    public TradeSite getSite();

    /**
     * Get a value for a given key.
     *
     * @param key The key for the value.
     *
     * @return The value for the key or null, if there is no such value.
     */
    public Price getValue( String key);
}