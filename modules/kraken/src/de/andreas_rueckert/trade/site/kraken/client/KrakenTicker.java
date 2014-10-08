/**
 * Java implementation for cryptocoin trading.
 *
 * Copyright (c) 2014 the authors:
 * 
 * @author Andreas Rueckert <mail@andreas-rueckert.de>
 * @author gosucymp <gosucymp@gmail.com>
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

package de.andreas_rueckert.trade.site.kraken.client;

import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.TickerImpl;
import net.sf.json.JSONObject;
import net.sf.json.JSONException;


/**
 * This class implements a ticker object for the Kraken trading platform.
 *
 * @see https://www.kraken.com/help/api#get-ticker-info
 */
public class KrakenTicker extends TickerImpl {

    // Inner classes


    // Static variables


    // Instance variables


    // Constructors

    /**
     * Create a new ticker object from the Kraken return value.
     *
     * @param jsonTicker A ticker state as a JSONObject.
     * @param currencyPair The currency pair to query in this ticker object.
     * @param tradeSite The trade site, this ticker belongs to.
     */
    public KrakenTicker( JSONObject jsonTicker, CurrencyPair currencyPair, TradeSite tradeSite) throws JSONException {

	super( currencyPair, tradeSite);  // Initialize the ticker functionality.

	// The key names are somewhat unusual at Kraken and some values have volumes, that I ignore for now.
	// Kraken offers the data of today or alternatively of the last 24 hours. I use the last 24 hours as the default.

	// 'a' is ask.
	_values.put( "sell", new Price( jsonTicker.getJSONArray( "a").getString( 0)));

	// 'b' is bid.
	_values.put( "buy", new Price( jsonTicker.getJSONArray( "b").getString( 0)));
		
	// 'l' has the low value of the last 24 hours in entry 1.
	_values.put( "low", new Price( jsonTicker.getJSONArray( "l").getString( 1)));

	// 'h' has the high value of the last 24 hours in entry 1.
	_values.put( "high", new Price( jsonTicker.getJSONArray( "h").getString( 1)));
	
	// 'v' has the volume of the last 24 hours in entry 1 (0 is volume of today).
	_values.put( "vol", new Amount( jsonTicker.getJSONArray( "v").getString( 1)));

	// 'c' has the price of the last trade in entry 0.
	_values.put( "last", new Price( jsonTicker.getJSONArray( "c").getString( 0)));

	// 'p' has the vwap value of the last 24 hours in entry 1.
	_values.put( "vwap", new Price( jsonTicker.getJSONArray( "p").getString( 1)));
    }


    // Methods

}
