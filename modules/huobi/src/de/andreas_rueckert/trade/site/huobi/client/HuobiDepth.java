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

package de.andreas_rueckert.trade.site.huobi.client;

import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.DepthImpl;
import de.andreas_rueckert.trade.site.TradeSite;
import net.sf.json.JSONObject;


/**
 * This class implements the depth of the Huobi trading site.
 *
 * @see <a href="https://www.huobi.com/help/index.php?a=market_help">Huobi depth</a>
 */
public class HuobiDepth extends DepthImpl {

    // Inner classes


    // Static variables


    // Instance variables


    // Constructors

    /**
     * Create a new Huobi depth object from the JSON response from the server.
     *
     * @param jsonResponse The jsonResponse from the server.
     * @param currencyPair The currency pair, that was queried.
     * @param tradeSite The trade site, that delivered the data.
     */
    public HuobiDepth( JSONObject jsonResponse, CurrencyPair currencyPair, TradeSite tradeSite) {

	super( currencyPair, tradeSite);  // Init the base depth variables.

	// Just use the default method to parse depth arrays.
	parseJSONDepthArrays( jsonResponse);
    }


    // Methods
}
