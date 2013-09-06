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

package de.andreas_rueckert.trade.site.vircurex.client;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.persistence.PersistentProperty;
import de.andreas_rueckert.persistence.PersistentPropertyList;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.CryptoCoinTrade;
import de.andreas_rueckert.trade.Currency;
import de.andreas_rueckert.trade.CurrencyImpl;
import de.andreas_rueckert.trade.CurrencyNotSupportedException;
import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.CurrencyPairImpl;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.OrderStatus;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.site.TradeDataRequestNotAllowedException;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteImpl;
import de.andreas_rueckert.trade.site.TradeSiteRequestType;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.trade.Ticker;
import de.andreas_rueckert.util.HttpUtils;
import de.andreas_rueckert.util.TimeUtils;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * Main class for the Vircurex API client.
 *
 * @see https://vircurex.com/welcome/api
 */
public class VircurexClient extends TradeSiteImpl implements TradeSite {

    // Static variables

    /**
     * The domain of the trading site.
     */
    final static String DOMAIN = "vircurex.com";


    // Instance variables

    /**
     * The current currency to be queried by default.
     */
    private Currency _currency;

    /**
     * The timestamp of the last request to the trade site.
     * Just to make sure, that the request interval limits are respected.
     */
    private long _lastRequest = -1L;

    /**
     * The current currency to be used for payments by default.
     */
    private Currency _paymentCurrency;


    // Constructors

    /**
     * Create a new connection to the Vircurex trading site.
     */
    public VircurexClient() {
	
	super();

	_name = "Vircurex";
	_url = "https://vircurex.com/";

	// Define the supported currency pairs for this trading site.
	// This list is actually not complete, since Vircurex supports lots of pairs, but
	// it should be sufficient for most apps, I guess.
	_supportedCurrencyPairs = new CurrencyPair[4];
	_supportedCurrencyPairs[0] = new CurrencyPairImpl( CurrencyImpl.BTC, CurrencyImpl.EUR);
	_supportedCurrencyPairs[1] = new CurrencyPairImpl( CurrencyImpl.BTC, CurrencyImpl.USD);
	_supportedCurrencyPairs[2] = new CurrencyPairImpl( CurrencyImpl.LTC, CurrencyImpl.BTC);
	_supportedCurrencyPairs[3] = new CurrencyPairImpl( CurrencyImpl.NMC, CurrencyImpl.BTC);
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
    public synchronized OrderStatus executeOrder( SiteOrder order) {
	throw new NotYetImplementedException( "Execute an order is not implemented for Vircurex");
    }

    /**
     * Get the accounts of the user on this trading site.
     *
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The accouts as a collection of account objects.
     */
    public Collection<TradeSiteAccount> getAccounts( TradeSiteUserAccount userAccount) {
	throw new NotYetImplementedException( "getAccounts() is not yet implemented for Vircurex");
    }

    /**
     * Get the current depth.
     *
     * @param currencyPair The currency pair to be queried.
     *
     * @return The current depth.
     */
    public Depth getDepth( CurrencyPair currencyPair) {

	// If a request for the depth is allowed at the moment
	if( isRequestAllowed( TradeSiteRequestType.Depth)) { 

	    if( ! isSupportedCurrencyPair( currencyPair)) {
		throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on Vircurex");
	    }

	    String url = "https://" 
		+ DOMAIN + "/api/orderbook.json?alt=" 
		+ currencyPair.getPaymentCurrency().getName() + "&base=" + currencyPair.getCurrency().getName();
	    
	    String requestResult = HttpUtils.httpGet( url);

	    if( requestResult != null) {  // Request sucessful?
		try {
		    // Convert the HTTP request return value to JSON to parse further.
		    Depth depth = new VircurexDepth( JSONObject.fromObject( requestResult), currencyPair, this);

		    updateLastRequest();  // Update the timestamp of the last request.

		    return depth;

		} catch( JSONException je) {
		    System.err.println( "Cannot parse Vircurex depth return: " + je.toString());
		}
	    }

	    return null;  // the depth request failed.
	}

	// The request is not allowed at the moment, so throw an exception.
	throw new TradeDataRequestNotAllowedException( "Request for depth not allowed at the moment at Vircurex site");	
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
	return "Vircurex";
    }

    /**
     * Get the settings of the vircurex client.
     *
     * @return The setting of the vircurex client.
     */
    public PersistentPropertyList getSettings() {

	// Get the settings from the base class.
	PersistentPropertyList result = super.getSettings();

	return result;
    }

    /**
     * Get the current ticker.
     *
     * @param currencyPair The currency pair to query.
     * @param paymentCurrency The currency to use for payments.
     *
     * @return The current ticker.
     */
    public Ticker getTicker( CurrencyPair currencyPair) {
	throw new NotYetImplementedException( "Fetching the ticker is not yet implemented for Vircurex");
    }

    /**
     * Get the trades from this trading site.
     *
     * @param since_micros The GMT-relative epoch since to fetch the trades.
     * @param currencyPair The currency pair to query.
     *
     * @return  The trades as an array of Trade objects.
     */
    public CryptoCoinTrade [] getTrades( long since_micros,  CurrencyPair currencyPair) {

	// If a request for trades is allowed
	if( isRequestAllowed( TradeSiteRequestType.Trades)) {

	    if( ! isSupportedCurrencyPair( currencyPair)) {
		throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on Vircurex");
	    }

	    // Vircurex has a 'since' parameter, but that only for the id and not for the date,
	    // so we have to filter the trade manually after the fetch.
	    String url = "https://" 
		+ DOMAIN + "/api/trades.json?alt=" 
		+ currencyPair.getPaymentCurrency().getName() + "&base=" + currencyPair.getCurrency().getName();

	    ArrayList<CryptoCoinTrade> trades = new ArrayList<CryptoCoinTrade>();
	    
	    String requestResult = HttpUtils.httpGet( url);
	    
	    if( requestResult != null) {  // If the HTTP request worked ok.
		try {
		    // Convert the result to an JSON array.
		    JSONArray resultArray = JSONArray.fromObject( requestResult);

		    // Iterate over the array and convert each trade from json to a Trade object.
		    for( int i = 0; i < resultArray.size(); i++) {
			JSONObject tradeObject = resultArray.getJSONObject(i);
		    
			CryptoCoinTrade newTrade = new VircurexTradeImpl( tradeObject, this, currencyPair);
		    
			// Since vircurex doesn't deliver the trades since a given trade, we have to filter them here...
			if( newTrade.getTimestamp() > since_micros) {
			    trades.add( newTrade);  // Add the new Trade object to the list.
			}
		    }
		    
		    CryptoCoinTrade [] tradeArray = trades.toArray( new CryptoCoinTrade[ trades.size()]);  // Convert the list to an array
		    
		    updateLastRequest();  // Update the timestamp of the last request.

		    return tradeArray;

		} catch( JSONException je) {
		    System.err.println( "Cannot parse trade object in vircurex client: " + je.toString());
		}
	    }
	    return null;  // Fetching the trades failed.
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
	return 61L * 1000000L;  // Vircurex doesn't want to get polled more often than once per minute.
    }


    /**
     * Set new settings for the btc-e client.
     *
     * @param settings The new settings for the vircurex client.
     */
    public void setSettings( PersistentPropertyList settings) {

	super.setSettings( settings);
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
