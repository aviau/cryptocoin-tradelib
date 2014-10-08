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

package de.andreas_rueckert.trade.site.bitfinex.client;

import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.DepthImpl;
import de.andreas_rueckert.trade.order.DepthOrderImpl;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import java.util.Collections;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * This class implements the depth of the Bitfinex trading site.
 *
 * @see https://www.bitfinex.com/pages/api
 */
public class BitfinexDepth extends DepthImpl {

    // Inner classes

    
    // Static variables


    // Instance variables


    // Constructors

    /**
     * Create a new Bitfinex depth object from the JSON response from the server.
     *
     * @param jsonResponse The jsonResponse from the server.
     * @param currencyPair The currency pair, that was queried.
     * @param tradeSite The trade site, that delivered the data.
     */
    public BitfinexDepth( JSONObject jsonResponse, CurrencyPair currencyPair, TradeSite tradeSite) {

	super( currencyPair, tradeSite);  // Init the base depth variables.

	// Get the array with the sell orders.
	JSONArray asks = jsonResponse.getJSONArray( "asks");

	// Now loop over the asks array and get the entries as objects.
	for( int i = 0; i < asks.size(); ++i) {

	    JSONObject sellOrder = asks.getJSONObject( i);  // Get the current sell order.

	    _sells.add( new DepthOrderImpl( OrderType.SELL
					    , new Price( sellOrder.getString( "price"), currencyPair.getPaymentCurrency())
					    , _currencyPair
					    , new Amount( sellOrder.getString( "amount"))));
	}

	// Make sure, the sells are sorted (they should be anyway, but just in case...)
	Collections.sort( _sells);

	// Get the array with the buy orders.
	JSONArray bids = jsonResponse.getJSONArray( "bids");

	// Now loop over the bids array and get the entries as objects.
	for( int i = 0; i < bids.size(); ++i) {

	    JSONObject buyOrder = bids.getJSONObject( i);  // Get the current buy order.

	    _buys.add( new DepthOrderImpl( OrderType.BUY
					   , new Price( buyOrder.getString( "price"), _currencyPair.getPaymentCurrency())
					   , _currencyPair
					   , new Amount( buyOrder.getString( "amount"))));
	}

	// Make sure, the buys are sorted (they should be anyway, but just in case...)
	Collections.sort( _buys);	
    }


    // Methods
}
