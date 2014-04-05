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

package de.andreas_rueckert.trade.site.cryptsy.client;

import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.CurrencyNotSupportedException;
import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.CurrencyPairImpl;
import de.andreas_rueckert.trade.DepthImpl;
import de.andreas_rueckert.trade.order.DepthOrderImpl;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.util.LogUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * This class implements the depth of the cryptsy trading site.
 */
public class CryptsyDepth extends DepthImpl {

    // Static variables


    // Instance variables


    // Constructors

    /**
     * Create a new cryptsy depth object from the JSON response from the server.
     * This constructor was written for the markets API method of cryptsy, in which
     * all the depths are returned in one response. The currency pair is part of the
     * market, so we don't have to pass it to the constructor.
     *
     * @param jsonResponse The jsonResponse from the cryptsy server.
     * @param tradeSite The trade site, that delivered the data.
     *
     * @throws CurrencyNotSupportedException if an unknown currency is used in this depth.
     */
    public CryptsyDepth( JSONObject jsonMarket, TradeSite tradeSite) throws CurrencyNotSupportedException {

	super( tradeSite);

	// Get the 2 coin codes from the market, so we don't need the key for this json value.
	String currencyPairString = jsonMarket.getString( "primarycode") + "<=>" + jsonMarket.getString( "secondarycode");

	// Try to create a currency pair for this market.
	CurrencyPair newCurrencyPair = CurrencyPairImpl.findByString( currencyPairString);
	
	if( newCurrencyPair == null) {           // If the new currency pair is not found
		    
	    LogUtils.getInstance().getLogger().error( "Cryptsy: cannot create currency pair for string: " + currencyPairString);
	    
	    return;
	}

	// Set the currency pair for this depth.
	setCurrencyPair( newCurrencyPair);

	// Parse the orders and add them to this depth instance.
	parseOrders( jsonMarket);
    }

    /**
     * Create a new cryptsy depth object from the JSON response from the server.
     *
     * @param jsonResponse The jsonResponse from the cryptsy server.
     * @param currencyPair The currency pair, that was queried.
     * @param tradeSite The trade site, that delivered the data.
     */
    public CryptsyDepth( JSONObject jsonResponse, CurrencyPair currencyPair, TradeSite tradeSite) {

	super( currencyPair, tradeSite);
	
	// Parse the nested JSON arrays in the response and convert them to DepthOrder objects.
	parseOrders( jsonResponse.getJSONObject( "return").getJSONObject( currencyPair.getCurrency().toString()));
    }


    // Methods

    /**
     * Parse the orders of a depth. This method is used by both constructors.
     *
     * @param jsonMarketStatus The market status as a JSONObject.
     */
    private final void parseOrders( JSONObject jsonMarketStatus) {

	// Get the array with the sell orders.
	JSONArray sells = jsonMarketStatus.optJSONArray( "sellorders");

	// Cryptsy sets the value to null (a JSONObject) if there are no orders, it seems.
	if( sells != null) {

	    // Now loop over the sells array and get the entries as arrays.
	    for( int i = 0; i < sells.size(); ++i) {
		
		JSONObject sellOrder = sells.getJSONObject( i);  // Get the current sell order.
		
		_sells.add( new DepthOrderImpl( OrderType.SELL
						, new Price( sellOrder.getString( "price"))
						, _currencyPair
						, new Amount( sellOrder.getString( "quantity"))));
	    }
	}

	// Get the array with the buy orders.
	JSONArray buys = jsonMarketStatus.optJSONArray( "buyorders");

	// Cryptsy sets the value to null (a JSONObject) if there are no orders, it seems.
	if( buys != null) {

	    // Now loop over the buys array and get the entries as arrays.
	    for( int i = 0; i < buys.size(); ++i) {
		
		JSONObject buyOrder = buys.getJSONObject( i);  // Get the current buy order.
		
		_buys.add( new DepthOrderImpl( OrderType.BUY
					       , new Price( buyOrder.getString( "price"))
					       , _currencyPair
					       , new Amount( buyOrder.getString( "quantity"))));
	    }	
	}
    }
}