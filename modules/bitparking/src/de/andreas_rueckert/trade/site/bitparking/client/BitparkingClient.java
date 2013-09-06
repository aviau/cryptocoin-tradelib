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

package de.andreas_rueckert.trade.site.bitparking.client;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.CryptoCoinTrade;
import de.andreas_rueckert.trade.Currency;
import de.andreas_rueckert.trade.CurrencyImpl;
import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.CurrencyPairImpl;
import de.andreas_rueckert.trade.CurrencyNotSupportedException;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.OrderStatus;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.site.TradeDataRequestNotAllowedException;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteImpl;
import de.andreas_rueckert.trade.site.TradeSiteRequestType;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.util.HttpUtils;
import de.andreas_rueckert.util.TimeUtils;
import java.util.ArrayList;
import java.util.Collection;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONException;


/**
 * Implementation of the bitparking API.
 * @see: https://ltcexchange.bitparking.com/api
 *
 * Edit: the bitparking exchange seems to close now, but maybe this code
 * helps to implement other exchange APIs, so it's included in the sources
 * for now.
 * @see: https://bitcointalk.org/index.php?topic=106356.0
 */
public class BitparkingClient extends TradeSiteImpl implements TradeSite {

    // Static variables
    
    /**
     * The domain of the service.
     */
    public static String DOMAIN = "ltcexchange.bitparking.com";


    // Instance variables

    /**
     * The timestamp of the last request to the trade site.
     * Just to make sure, that the request interval limits are respected.
     */
    private long _lastRequest = -1L;


    // Constructors

    /**
     * Create a new connection to the trading site.
     */
    public BitparkingClient() {
	super();

	_name = "Bitparking";
	_url = "https://" + DOMAIN + "/";

	// Define the supported currency pairs for this trading site.
	_supportedCurrencyPairs = new CurrencyPair[1];
	_supportedCurrencyPairs[0] = new CurrencyPairImpl( CurrencyImpl.LTC, CurrencyImpl.BTC);
    }


    // Methods

    /**
     * Cancel an order on the trade site.
     *
     * @param order The order to cancel.
     *
     * @return true, if the order was canceled. False otherwise.
     */
    public boolean cancelOrder( SiteOrder order) {

	throw new NotYetImplementedException( "Cancelling an order is not yet implemented for " + this._name);
    }

   /**
     * Execute an order on the trade site.
     *
     * @param order The order to execute.
     *
     * @return The new status of the order.
     */
    public OrderStatus executeOrder( SiteOrder order) {
	throw new NotYetImplementedException( "Execute order is not yet implemented for Bitparking");
    }

    /**
     * Get the accounts with the current funds on this trading site.
     *
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The accounts with the current balance as a collection of Account objects, or null if the request failed.
     */
    public Collection<TradeSiteAccount> getAccounts( TradeSiteUserAccount userAccount) {

	throw new NotYetImplementedException( "Getting the accounts is not yet implemented for Bitparking");
    }

    /**
     * Get the market depth as a Depth object.
     *
     * @param currencyPair The queried currency pair.
     * @param paymentCurrency The currency to use for the payment
     */
    public Depth getDepth( CurrencyPair currencyPair) {

	// If a request for the depth is allowed at the moment
	if( isRequestAllowed( TradeSiteRequestType.Depth)) { 

	    if( ! isSupportedCurrencyPair( currencyPair)) {
		throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on Bitparking");
	}

	    String url = "https://" + DOMAIN + "/api/o";

	    String requestResult = HttpUtils.httpGet( url);

	    if( requestResult != null) {  // Request sucessful?
		try {
		    // Convert the HTTP request return value to JSON to parse further.
		    Depth depth =  new BitparkingDepth( JSONObject.fromObject( requestResult), currencyPair, this);

		    updateLastRequest();  // Update the timestamp of the last request.

		    return depth;  // Return the parsed depth.

		} catch( JSONException je) {
		    System.err.println( "Cannot parse Bitparking depth return: " + je.toString());
		}
	    }
	    
	    return null;  // The depth request failed.
	}
	
	// The request is not allowed at the moment, so throw an exception.
	throw new TradeDataRequestNotAllowedException( "Request for depth not allowed at the moment at Bitparking site");	
    }

    /**
     * Get the shortest allowed requet interval in microseconds.
     *
     * @return The shortest allowed request interval in microseconds.
     */
    public long getMinimumRequestInterval() {
	return getUpdateInterval();
    }

    /**
     * Get the open orders on this trade site.
     *
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The open orders as a collection.
     */
    public Collection<SiteOrder> getOpenOrders( TradeSiteUserAccount userAccount) {
	throw new NotYetImplementedException( "Get the open orders is not yet implemented for Bitparking");
    }

    /**
     * Get the section name in the global property file.
     */
    public String getPropertySectionName() {
	return "Bitparking";
    }

    /**
     * Get the current ticker from the Bitparking API.
     *
     * @param currencyPair The currency pair to query.
     * @param paymentCurrency The currency for the payments.
     *
     * @return The current Bitparking ticker.
     */
    public BitparkingTicker getTicker( CurrencyPair currencyPair) {
	throw new NotYetImplementedException( "Getting the ticker is not yet implemented for the Bitparking API");
    }

    /**
     * Get a list of recent trades.
     *
     * @param id The id of the first trade to fetch (ignored here).
     * @param currencyPair The currency pair to query.
     *
     * @return The trades as a list of Trade objects.
     */
    public CryptoCoinTrade [] getTrades( long id, CurrencyPair currencyPair) {

	if( ! isSupportedCurrencyPair( currencyPair)) {
	    throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on Bitparking");
	}

	String url = "https://" + DOMAIN + "/api/t";

	return getTradesFromURL( url, currencyPair);
    }

    /**
     * Fetch some trades from a given URL.
     *
     * @param url The url to use for fetching the trades.
     * @param currencyPair The requested currency pair.
     *
     * @return The trades as an array of Trade objects.
     */
    private CryptoCoinTrade [] getTradesFromURL( String url, CurrencyPair currencyPair) {

	// If a request for trades is allowed
	if( isRequestAllowed( TradeSiteRequestType.Trades)) {

	    ArrayList<CryptoCoinTrade> trades = new ArrayList<CryptoCoinTrade>();
	    
	    String requestResult = HttpUtils.httpGet( url);
	
	    if( requestResult != null) {  // Request successful?
		try {
		    // System.out.println( "Debug: Bitparking return value: " + requestResult);

		    // Concert the HTTP result into a JSON object.
		    JSONArray resultArray = JSONArray.fromObject( requestResult);

		    // Iterate over the json array and convert each trade from json to a Trade object.
		    for( int i = 0; i < resultArray.size(); i++) {
			JSONObject tradeObject = resultArray.getJSONObject(i);
		    
			trades.add( new BitparkingTradeImpl( tradeObject, this, currencyPair));  // Add the new Trade object to the list.
		    }

		    CryptoCoinTrade [] tradeArray = trades.toArray( new CryptoCoinTrade[ trades.size()]);  // Convert the list to an array.

		    updateLastRequest();  // Update the timestamp of the last request.

		    return tradeArray;  // And return the array.
		    
		} catch( JSONException je) {
		    System.err.println( "Cannot convert HTTP response to JSON array: " + je.toString());
		}
	    }
	    
	    return null;  // An error occured.
	}

	// The request is not allowed at the moment, so throw an exception.
	throw new TradeDataRequestNotAllowedException( "Request for trades not allowed at the moment at Bitparking site");
    }

    /**
     * Get the interval, in which the trade site updates it's depth, ticker etc. 
     * in microseconds.
     *
     * @return The update interval in microseconds.
     */
    public long getUpdateInterval() {
	return 61L * 1000000L;  // Bitparking is rather quiet at the moment, so 1 min should be enough.
    }

    /**
     * Check, if some request type is allowed at the moment. Most
     * trade site have limits on the number of request per time interval.
     *
     * @param requestType The type of request (trades, depth, ticker, order etc).
     *
     * @return true, if the given type of request is possible at the moment.
     */
    public boolean isRequestAllowed( TradeSiteRequestType requestType) {
	return ((_lastRequest + getMinimumRequestInterval()) < TimeUtils.getInstance().getCurrentGMTTimeMicros());
    }

    /**
     * Update the timestamp of the last request.
     */
    private void updateLastRequest() {
	_lastRequest = TimeUtils.getInstance().getCurrentGMTTimeMicros();
    }
}
