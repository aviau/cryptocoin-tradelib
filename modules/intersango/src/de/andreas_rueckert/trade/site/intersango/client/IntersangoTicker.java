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

package de.andreas_rueckert.trade.site.intersango.client;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.TickerImpl;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * Implementation of a Intersango ticker object.
 */
public class IntersangoTicker extends TickerImpl {

    // Static variables

    /**
     * The hash keys of the values in the array.
     */
    private static String [] HASHKEYS = { "buy", "sell", "last"};


    // Instance variables


    // Constructors

    /**
     * Create a new Intersango ticker state from a JSON reply.
     *
     * @param jsonTicker The JSON reply from the Intersango server.
     * @param currencyPair The currency pair to use.
     * @param tradeSite The trading site, this ticker belongs to.
     */
    public IntersangoTicker( JSONObject jsonTicker, CurrencyPair currencyPair, TradeSite tradeSite) {
	super( currencyPair, tradeSite);
	
	// Parse the ticker values in the json object and store the values in the hash map.
	for( String key : HASHKEYS) {
	    // Those values in json are floats, to I convert them to BigDecimal before putting them into the hashmap.
	    _values.put( key, new Price( jsonTicker.getString( key)));

	    // I leave the 'vol' field out for now...
	}
    }


    // Methods
}