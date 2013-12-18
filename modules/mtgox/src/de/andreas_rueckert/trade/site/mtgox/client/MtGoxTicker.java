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

package de.andreas_rueckert.trade.site.mtgox.client;

import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.TickerImpl;
import java.util.HashMap;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * Class to hold the values of a mtgox ticker state. 
 * These are only bitcoin values at the moment.
 */
public class MtGoxTicker extends TickerImpl {
    
    // Instance variables

    /**
     * The hash keys of the values in the array.
     * Volume is not a price, so I just leave it away for now, to make storage easier...
     */
    private static String [] HASHKEYS = { "high" ,"low", "avg", "vwap", /* "vol" , */ "last_local", "last_orig", "last", "buy", "sell" };
    

    // Constructors

    /**
     * Create a new ticker rate.
     *
     * @param jsonTicker A ticker state as a JSONObject.
     * @param currencyPair The queried currency pair in this ticker.
     * @param tradeSite The trade site, this ticker belongs to.
     */
    public MtGoxTicker( JSONObject jsonTicker, CurrencyPair currencyPair, TradeSite tradeSite) throws JSONException {

	super( currencyPair, tradeSite);  // Initialize the ticker functionality.

	// Parse the various ticker values.

	// Parse the ticker values in the json object and store the values in the hash map.
	for( String key : HASHKEYS) {
	    _values.put( key, new Price( jsonTicker.getJSONObject( key).getString( "value"), currencyPair.getPaymentCurrency()));
	}
    }
    

    // Methods

    /**
     * Get the average BTC rate.
     *
     * @return The average BTC rate.
     */
    public Price getAVG() {
	return (Price)_values.get( "avg");
    }
    
    /**
     * Get the highest BRC rate.
     *
     * @return The highest BTC rate.
     */
    public Price getHigh() {
	return (Price)_values.get( "high");
    }

    /**
     * Get the last BTC rate.
     *
     * @return The last BTC rate.
     */
    public Price getLast() {
	return (Price)_values.get( "last");
    }

    /**
     * Get the lowest BTC rate.
     *
     * @return The lowest BTC rate.
     */
    public Price getLow() {
	return (Price)_values.get( "low");
    }

    /**
     * Get the volume of traded BTC.
     *
     * @return The volume of traded BTC.
     */
    /* public Amount getVolume() {
	return _values.get( "vol");
	} */

    /**
     * Get the weighest average BTCV rate.
     *
     * @return The weighted average BTC rate.
     */
    public Price getVWAP() {
	return (Price)_values.get( "vwap");
    }
}