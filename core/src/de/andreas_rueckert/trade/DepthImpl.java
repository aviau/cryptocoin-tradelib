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

package de.andreas_rueckert.trade;

import de.andreas_rueckert.trade.Currency;
import de.andreas_rueckert.trade.order.DepthOrder;
import de.andreas_rueckert.trade.order.DepthOrderImpl;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.util.TimeUtils;
import java.util.Collections;
import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


/**
 * Simple implementation for the market depth.
 */
public class DepthImpl implements Depth {

    // Static variables

    
    // Instance variable

    /**
     * The buy orders as a list of DepthOrder objects.
     */
    protected TradeDataList<DepthOrder> _buys = new TradeDataList<DepthOrder>();

    /**
     * The currency pair to be used for the depth.
     */
    protected CurrencyPair _currencyPair;

    /**
     * The sell orders as a list of DepthOrder objects.
     */
    protected TradeDataList<DepthOrder> _sells = new TradeDataList<DepthOrder>();

    /**
     * The timestamp as a GMT relative epoch.
     */
    private long _timestamp;

    /**
     * The trade site, this depth belongs to.
     */
    private TradeSite _tradeSite;


    // Constructors

    /**
     * Create a new depth object. To be overwritten by subclasses.
     *
     * @param currencyPair The currency pair, that was queried.
     * @param tradeSite The trade, that send this ticker.
     */
    protected DepthImpl( CurrencyPair currencyPair, TradeSite tradeSite) {

	_tradeSite = tradeSite;              // Store the trade site in the depth object.

	_currencyPair = currencyPair;  // Store the currencies in the depth object.

	_timestamp = TimeUtils.getInstance().getCurrentGMTTimeMicros();  // Store the timestamp in the object.
    }


    // Methods

    /**
     * Get a buy order with a given index.
     *
     * @return The DepthOrder with the given index.
     *
     * @throws TradeDataNotAvailableException if the order with the given index is not in the list of orders.
     */
    public DepthOrder getBuy( int index) throws TradeDataNotAvailableException {
	return _buys.get( index); 
    }

    /**
     * Get the buy orders as a list of DepthOrder objects.
     *
     * @return The buy orders as a list of DepthOrder objects.
     */
    public List<DepthOrder> getBuyOrders() {
	return _buys;
    }

    /**
     * Get the number of buy orders.
     *
     * @return The number of buy orders.
     */
    public int getBuySize() {
	return _buys.size();
    }

    /**
     * Get the currency pair, that is used for this depth.
     *
     * @return The currency pair, that is used for this depth.
     */
    public CurrencyPair getCurrencyPair() {
	return _currencyPair;
    }

    /**
     * Get a sell order with a given index.
     *
     * @return The sell order with the given index.
     *
     * @throws TradeDataNotAvailableException if the order with the given index is not in the list of orders.
     */
    public DepthOrder getSell( int index) throws TradeDataNotAvailableException {
	return _sells.get( index);
    }

    /**
     * Get the sell orders as a list of DepthOrder objects.
     *
     * @return The sell orders as a list of DepthOrder objects.
     */
    public List<DepthOrder> getSellOrders() {
	return _sells;
    }

    /**
     * Get the number of sell orders.
     *
     * @return The number of sell orders.
     */
    public int getSellSize() {
	return _sells.size();
    }

    /**
     * Get the timestamp, when this depth object was created (received from the trade site,
     *
     * @return The timestamp, when this Depth object was created (received from the trade site).
     */
    public long getTimestamp() {
	return _timestamp;
    }

    /**
     * Get the trade site, this depth belongs to.
     *
     * @return The trade site, thisn depth belongs to.
     */
    public TradeSite getTradeSite() {
	return _tradeSite;
    }

    /**
     * Parse a JSON reponse, that consists of nested arrays and convert them to
     * DepthOrder objects.
     * This is just a utility method, since several trade sites use this format, 
     * and the code doesn't have to be duplicated in every derived depth class.
     *
     * @param jsonDepth The depth as nested JSON arrays.
     */
    protected void parseJSONDepthArrays( JSONObject jsonDepth) {

	// Get the array with the sell orders.
	JSONArray asks = jsonDepth.getJSONArray( "asks");

	// Now loop over the asks array and get the entries as arrays.
	for( int i = 0; i < asks.size(); ++i) {

	    JSONArray sellOrder = asks.getJSONArray( i);  // Get the current sell order.

	    // System.out.println( "Creating order with price: " + sellOrder.getString( 0));

	    _sells.add( new DepthOrderImpl( OrderType.SELL
					    , new Price( sellOrder.getString( 0))
					    , _currencyPair
					    , new Amount( sellOrder.getString( 1))));
	}

	// Make sure, the sells are sorted (they should be anyway, but just in case...)
	Collections.sort( _sells);

	// Get the array with the buy orders.
	JSONArray bids = jsonDepth.getJSONArray( "bids");

	// Now loop over the bids array and get the entries as arrays.
	for( int i = 0; i < bids.size(); ++i) {

	    JSONArray buyOrder = bids.getJSONArray( i);  // Get the current buy order.

	    _buys.add( new DepthOrderImpl( OrderType.BUY
					   , new Price( buyOrder.getString( 0))
					   , _currencyPair
					   , new Amount( buyOrder.getString( 1))));
	}

	// Make sure, the buys are sorted (they should be anyway, but just in case...)
	Collections.sort( _buys);
    }
}