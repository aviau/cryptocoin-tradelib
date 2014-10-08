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

package de.andreas_rueckert.trade.site.bitstamp.client;

import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.TickerImpl;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * This class implements a ticker object for the Bitstamp trading platform.
 */
public class BitstampTicker extends TickerImpl {

    // Static variables

    /**
     * The hash keys of the values in the array.
     */
    private static String [] HASHKEYS = { "high" ,"low", "volume", "bid", "ask", "last" };
    

    // Instance variables


    // Constructors

    /**
     * Create a new ticker object from the Bitstamp return value.
     *
     * @param jsonTicker A ticker state as a JSONObject.
     * @param currencyPair The currency pair to query in this ticker object.
     * @param tradeSite The trade site, this ticker belongs to.
     */
    public BitstampTicker( JSONObject jsonTicker, CurrencyPair currencyPair, TradeSite tradeSite) throws JSONException {

	super( currencyPair, tradeSite);  // Initialize the ticker functionality.

	JSONObject ticker = jsonTicker.getJSONObject( "ticker");
	
	// Parse the ticker values in the json object and store the values in the hash map.
	for( String key : HASHKEYS) {

	    // Convert some keynames to some more usual key names.
	    String newKey = key;

	    if( "ask".equals( key)) { 
		newKey = "sell"; 		  
	    } else if( "bid".equals( key)) { 
		newKey = "buy";
	    } else if( "volume".equals( key)) {
		newKey = "vol";
	    }
	
	    // Store the values in the hash map.
	    _values.put( newKey, new Price( ticker.getString( key), currencyPair.getPaymentCurrency()));
	}
    }


    // Methods
}
