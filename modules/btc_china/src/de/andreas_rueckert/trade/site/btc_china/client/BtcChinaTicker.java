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

package de.andreas_rueckert.trade.site.btc_china.client;

import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.TickerImpl;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * This class implements a ticker object for the btc-china trading platform.
 */
public class BtcChinaTicker extends TickerImpl {


    // Static variables

    /**
     * The hash keys of the values in the array.
     */
    private static String [] HASHKEYS = { "high" ,"low", "buy", "sell", "last", "vol" };


    // Instance variables


    // Constructors
    /**
     * Create a new ticker object from the btc-e return value.
     *
     * @param jsonTicker A ticker state as a JSONObject.
     * @param currencyPair The currency pair to query in this ticker object.
     * @param tradeSite The trade site, this ticker belongs to.
     */
    public BtcChinaTicker( JSONObject jsonTicker, CurrencyPair currencyPair, TradeSite tradeSite) throws JSONException {

	super( currencyPair, tradeSite);  // Initialize the ticker functionality.

	JSONObject ticker = jsonTicker.getJSONObject( "ticker");
	
	// Parse the ticker values in the json object and store the values in the hash map.
	for( String key : HASHKEYS) {

	    if( key.equals( "vol")) {  // the volume is rather an amount than a price? (Although it has a currency attached).

		_values.put( key, new Amount( ticker.getString( key)));

	    } else {  // Everything should be a price?

		_values.put( key, new Price( ticker.getString( key), currencyPair.getPaymentCurrency()));
	    }
	}
    }


    // Methods
}