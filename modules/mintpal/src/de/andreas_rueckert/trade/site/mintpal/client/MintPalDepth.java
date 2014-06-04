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

package de.andreas_rueckert.trade.site.mintpal.client;

import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.DepthImpl;
import de.andreas_rueckert.trade.order.DepthOrderImpl;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import java.util.Collections;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * This class implements the depth of the MintPal trading site.
 *
 * @see https://www.mintpal.com/api#marketorders
 */
public class MintPalDepth extends DepthImpl {

    // Inner classes


    // Static variables


    // Instance variables


    // Constructors

    /**
     * Create a new MintPal depth object from the JSON responses from the server.
     *
     * @param jsonBuy The buy orders as a JSON object.
     * @param jsonSell The sell orders as a JSON object.
     * @param currencyPair The currency pair, that was queried.
     * @param tradeSite The trade site, that delivered the data.
     */
    public MintPalDepth( JSONObject jsonBuy, JSONObject jsonSell, CurrencyPair currencyPair, TradeSite tradeSite) {

	super( currencyPair, tradeSite);  // Init the base depth variables.

	// Get the array with the buy orders.
	JSONArray buys = jsonBuy.getJSONArray( "orders");

	// Loop over the orders and create a DepthOrder object from each of them
	for( int i = 0; i < buys.size(); ++i) {

	    JSONObject buyOrder = buys.getJSONObject( i);  // Get the current buy order

	    _buys.add(  new DepthOrderImpl( OrderType.BUY
					    , new Price( buyOrder.getDouble( "price"))
					    , _currencyPair
					    , new Amount( buyOrder.getString( "amount"))));
	}
	
	// Now sort the buy orders just in case...
	Collections.sort( _buys);

	// Get the array with the sell orders.
	JSONArray sells = jsonSell.getJSONArray( "orders");

	// Loop over the orders and create a DepthOrder object from each of them
	for( int i = 0; i < sells.size(); ++i) {

	    JSONObject sellOrder = sells.getJSONObject( i);  // Get the current sell order

	    _sells.add(  new DepthOrderImpl( OrderType.SELL
					    , new Price( sellOrder.getDouble( "price"))
					    , _currencyPair
					    , new Amount( sellOrder.getString( "amount"))));
	}
	
	// Now sort the buy orders just in case...
	Collections.sort( _sells);
    }


    // Methods
}