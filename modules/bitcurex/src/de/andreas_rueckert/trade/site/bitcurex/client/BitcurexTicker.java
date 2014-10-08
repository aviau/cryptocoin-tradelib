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

package de.andreas_rueckert.trade.site.bitcurex.client;

import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.TickerImpl;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * This class implements a ticker object for the Bitcurex trading platform.
 */
public class BitcurexTicker extends TickerImpl {

    // Static variables

    /**
     * The hash keys of the values in the array.
     */
    private static String [] HASHKEYS = { "high" ,"low", "avg", "vwap", "vol", "last", "buy", "sell", "time" };


    // Instance variables

    /**
     * The traded volume.
     */
    private Amount _volume;


    // Constructors

    /**
     * Create a new ticker object from the Bitcurex return value.
     *
     * @param jsonTicker A ticker state as a JSONObject.
     * @param currencyPair The currency pair to query in this ticker object.
     * @param tradeSite The trade site, this ticker belongs to.
     */
    public BitcurexTicker( JSONObject jsonTicker, CurrencyPair currencyPair, TradeSite tradeSite) throws JSONException {

	super( currencyPair, tradeSite);  // Initialize the ticker functionality.

	// Parse the ticker values in the json object and store the values in the hash map.
	// The returned time is ignored for now, since we set the current time as the
	// timestamp anyway...
	for( String key : HASHKEYS) {

	    if( key.equals( "vol")) {  // Store the volume as an amount, not as a price.

		_volume = new Amount( jsonTicker.getString( key));  

	    } else if( key.equals( "time")) {

		// Ignore the time for now.

	    } else {  // The other values should be prices.

		_values.put( key, new Price( jsonTicker.getString( key)));
	    }
	}
    }


    // Methods

    /**
     * Get the volume field of this ticker.
     *
     * @return The volume field of this ticker as an Amount object.
     */
    public Amount getVolume() {

	return _volume;
    }
}
