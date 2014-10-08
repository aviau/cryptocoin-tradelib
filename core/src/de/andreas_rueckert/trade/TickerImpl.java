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

import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.site.TradeSite;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;


/**
 * Minimal implementation of a ticker state.
 * To be extended by the actual implementation of exchange specific ticker objects.
 */
public class TickerImpl implements Ticker {

    // Static variables

    /**
     * The expiration interval of the ticker is 10 * 1000 millis = 10 seconds.
     */
    public long EXPIRATION_INTERVAL = 10 * 1000;


    // Instance variables

    /**
     * The currency pair used for the tickers values in this ticker object.
     */
    protected CurrencyPair _currencyPair;

    /**
     * The expiration time of this ticker object in milliseconds sine the GMT epoch (1.1.1970).
     */
    protected long _expirationTime;

    /**
     * The trading site, this ticker, belongs to.
     */
    protected TradeSite _site;

    /**
     * Timestamp, when this ticker result was received in milliseconds since 1.1.1970 GMT.
     */
    protected long _timestamp;
    
    /**
     * Store the values in a hash map for easier setting in the constructor.
     */
    protected HashMap< String, Object> _values = new HashMap< String, Object>();


    // Constructors
    
    /**
     * Create a new ticker object.
     *
     * @param currencyPair The currency pair to use for the ticker.
     * @param site The trading site, this ticker belongs to.
     */
    public TickerImpl( CurrencyPair currencyPair, TradeSite site) {

	// Get the timestamp in milliseconds since 1.1.1970.
	_timestamp = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis();

	// Compute the GMT millis, when this ticker will expire. 
	_expirationTime = _timestamp + EXPIRATION_INTERVAL;

	// Store the used currencies in this ticker object.
	_currencyPair = currencyPair;

	// Store a trade site reference in this ticker object.
	_site = site;
    }


    // Methods

    /**
     * Get the BTC rate for buy orders.
     *
     * @return The BTC rate for buy orders.
     */
    public Price getBuy() {
	return (Price)_values.get( "buy");
    }

    /**
     * Get the currency pair used in this ticker object.
     *
     * return The currency pair used in this ticker object.
     */
    public CurrencyPair getCurrencyPair() {
	return _currencyPair;
    }

    /**
     * Get the GMT expiration time of this ticker.
     *
     * @return The GMT expiration time of this ticker.
     */
    public long getExpirationTime() {
	return _expirationTime;
    }

    /**
     * Get the timestamp, when this ticker state was received.
     *
     * @return The timestamp, when this ticker state was received.
     */
    public long getTimestamp() {
	return _timestamp;
    }

    /**
     * Get the BTC rate for sell orders.
     *
     * @return The BTC rate for sell orders.
     */
    public Price getSell() {
	return (Price)_values.get( "sell");
    }

    /**
     * Get the trading site, this ticker belongs to.
     *
     * @return The trading site, this ticker belongs to.
     */
    public TradeSite getSite() {
	return _site;
    }

    /**
     * Get a value for a given key.
     *
     * @param key The key for the value (which is a usually a price).
     *
     * @return The value/price for the key or null, if there is no such value.
     */
    public Object getValue( String key) {
	return _values.get( key);
    }
}
