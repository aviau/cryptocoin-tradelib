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

package de.andreas_rueckert.trade.site.coins_e.client;

import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.DepthImpl;
import de.andreas_rueckert.trade.order.DepthOrder;
import de.andreas_rueckert.trade.order.DepthOrderImpl;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * This class implements the depth of the coins-e trading site.
 */
public class CoinsEDepth extends DepthImpl {

    // Static variables


    // Instance variables


    // Constructors

    /**
     * Create a new coins-e.com depth object from the JSON response from the server.
     *
     * @param jsonResponse The jsonResponse from the coins-e.com server.
     * @param currencyPair The currency pair, that was queried.
     * @param tradeSite The trade site, that delivered the data.
     */ 
    public CoinsEDepth( JSONObject jsonResponse, CurrencyPair currencyPair, TradeSite tradeSite) {

	super( currencyPair, tradeSite);

	// Since the coins-e depth has a different structure than most of the other depth results,
	// it's parsed in this class and not in the base class.

	// Parse the sell orders
	parseCoinsEArray( jsonResponse.getJSONArray( "asks"), _sells, currencyPair, OrderType.SELL);

	// Parse the buy orders.
	parseCoinsEArray( jsonResponse.getJSONArray( "bids"), _buys, currencyPair, OrderType.BUY);
    }


    // Methods

    /**
     * Parse the buy or sell array of a coins-e json response.
     *
     * @param jsonArray The json array to parse.
     * @param result The array, where the orders are added to.
     * @param currencyPair The currency pair of the depth.
     * @param orderType The order type (buy or sell).
     */
    private void parseCoinsEArray( JSONArray jsonArray, List<DepthOrder> result, CurrencyPair currencyPair, OrderType orderType) {

	// Loop over the json array.
	for( int i = 0; i < jsonArray.size(); ++i) {

	    // Get the order object.
	    JSONObject jsonOrder = jsonArray.getJSONObject( i);

	    // Since coins-e allows serveral user orders in 1 depth order entry, create one depth order 
	    // for each order this object.
	    int nOrders = jsonOrder.getInt( "n");

	    // Get the price and the amount from the order.
	    Price price = new Price( jsonOrder.getString( "r"));
	    Amount amount = new Amount( jsonOrder.getString( "q"));

	    // Loop over the number of orders in the json array and create DepthOrder objects 
	    // for the number of orders.
	    for( int currentUserOrder = 0; currentUserOrder < nOrders; ++currentUserOrder) {

		// Create one DepthOrder object and add it to the result.
		result.add( new DepthOrderImpl( orderType, price, currencyPair, amount));
	    }
	}
    }
}
