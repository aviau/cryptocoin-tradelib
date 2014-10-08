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
import de.andreas_rueckert.persistence.PersistentProperty;
import de.andreas_rueckert.persistence.PersistentPropertyList;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.CryptoCoinTrade;
import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.currency.CurrencyImpl;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.currency.CurrencyPairImpl;
import de.andreas_rueckert.trade.currency.CurrencyNotSupportedException;
import de.andreas_rueckert.trade.currency.CurrencyProvider;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.OrderStatus;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeDataRequestNotAllowedException;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteImpl;
import de.andreas_rueckert.trade.site.TradeSiteRequestType;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.trade.Ticker;
import de.andreas_rueckert.trade.Trade;
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
 * Main class for the Intersango client.
 * I named this class IntersangoClient in case, there will be an IntersangoServer and they are used 
 * in the same app.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * @see https://intersango.com/api.php
 */
public class IntersangoClient extends TradeSiteImpl implements TradeSite {

    // Static variables

    /**
     * The domain of the service.
     */
    public static String DOMAIN = "intersango.com";


    // Instance variables

    /**
     * The API key of the user.
     */
    private String _api_key = null;

    /**
     * The current default currency.
     */
    private Currency _currentCurrency = CurrencyProvider.getInstance().getCurrencyForCode( "USD");

    /**
     * The timestamp of the last request to the trade site.
     * Just to make sure, that the request interval limits are respected.
     */
    private long _lastRequest = -1L;


    // Constructors

    /**
     * Create a new connection to the Intersango trading site.
     */
    public IntersangoClient() {
	super();

	_name = "Intersango";
	_url = "https://intersango.com";

	// Define the supported currency pairs for this trading site.
	_supportedCurrencyPairs = new CurrencyPair[4];
	_supportedCurrencyPairs[0] = new CurrencyPairImpl( "BTC", "GBP");
	_supportedCurrencyPairs[1] = new CurrencyPairImpl( "BTC", "EUR");
	_supportedCurrencyPairs[2] = new CurrencyPairImpl( "BTC", "USD");
	_supportedCurrencyPairs[3] = new CurrencyPairImpl( "BTC", "PLN");
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

	throw new NotYetImplementedException( "Executing orders is not yet implemented for Intersango");
    }

    /**
     * Get the accounts with the current funds on this trading site.
     *
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The accounts with the current balance as a collection of Account objects.
     */
    public Collection<TradeSiteAccount> getAccounts( TradeSiteUserAccount userAccount) {
	throw new NotYetImplementedException( "Get accounts is not yet implemented for Intersango");
    }

    /**
     * Get the current default currency.
     */
    private Currency getCurrentCurrency() {
	return _currentCurrency;
    }

    /**
     * Get the id for a currency pair.
     *
     * @param currencyPair The queried currency pair.
     *
     * @return The id for the currency pair.
     */
    private int getCurrencyPairId( CurrencyPair currencyPair) {
	
	if( currencyPair.getCurrency().hasCode( "BTC")) {
	    if( currencyPair.getPaymentCurrency().hasCode( "GBP")) { return 1;
	    } else if( currencyPair.getPaymentCurrency().hasCode( "EUR")) { return 2;
	    } else if( currencyPair.getPaymentCurrency().hasCode( "USD")) { return 3;
	    } else if( currencyPair.getPaymentCurrency().hasCode( "PLN")) { return 4;
	    }
	}

	// If this is a unknown currency pair, throw an exception.
	throw new CurrencyNotSupportedException( "The currency pair: " 
						 + currencyPair.getCurrency().getCode() 
						 + " with payment currency: " 
						 + currencyPair.getPaymentCurrency().getCode() 
						 + " is not supported on Intersango");
    }

    /**
     * Get the market depth as a Depth object.
     *
     * @param currencyPair The currency pair to query.
     *
     * @return The market depth.
     */
    public Depth getDepth( CurrencyPair currencyPair) {

	// If a request for the depth is allowed at the moment
	if( isRequestAllowed( TradeSiteRequestType.Depth)) { 

	    if( ! isSupportedCurrencyPair( currencyPair)) {
		throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on Intersango");
	    }

	    int currencyPairId = getCurrencyPairId( currencyPair);
	    
	    String url= "https://" + DOMAIN + "/api/depth.php?currency_pair_id=" + currencyPairId;

	    String requestResult = HttpUtils.httpGet( url);

	    if( requestResult != null) {  // Request sucessful?
		try {
		    // Convert the HTTP request return value to JSON to parse further.
		    // Get only the requested currency pair from the returned JSON object (might contain several currencies).
		    Depth depth = new IntersangoDepth( JSONObject.fromObject( requestResult), currencyPair, this);

		    updateLastRequest();  // Update the timestamp of the last request.

		    return depth;

		} catch( JSONException je) {
		    System.err.println( "Cannot parse Intersango depth return: " + je.toString());
		}
	    }
	
	    return null;  // The depth request failed.
	}

	// The request is not allowed at the moment, so throw an exception.
	throw new TradeDataRequestNotAllowedException( "Request for depth not allowed at the moment at Intersango site");	
    }
 
    /**
     * Get the fee for an order in the resulting currency.
     * Synchronize this method, since several users might use this method with different
     * accounts and therefore different fees via a single API implementation instance.
     *
     * @param order The order to use for the fee computation.
     *
     * @return The fee in the resulting currency (currency value for buy, payment currency value for sell).
     */
    public synchronized Price getFeeForOrder( SiteOrder order) {

	throw new NotYetImplementedException( "Getting the fees is not yet implemented for " + _name);
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
     * Get the section name in the global property file (sort of an hack to avoid duplicated key names).
     */
    public String getPropertySectionName() {
	return "Intersango";
    }

    /**
     * Get the settings of this client.
     *
     * @return The settings of this client.
     */
    public PersistentPropertyList getSettings() {
	
	// Get the settings from the base class.
        PersistentPropertyList result = super.getSettings();

	result.add( new PersistentProperty( "API key", null, _api_key, 4));

	return result;
    }

    /**
     * Get the current ticker state as a Ticker object.
     *
     * @param currencyPair The currency pair to query.
     * @param paymentCurrency The currency to use for payments.
     *
     * @return The current ticker state as a Ticker object or null, if no ticker could be requested.
     */
    public Ticker getTicker( CurrencyPair currencyPair) {

	// If a request for the depth is allowed at the moment
	if( isRequestAllowed( TradeSiteRequestType.Ticker)) { 

	    int currency_pair_id;  // To select the 2 currencies at Intersango.
	    
	    // Determine the id of the currency pair.
	    if( ! currencyPair.getPaymentCurrency().hasCode( "USD")) {
		throw new CurrencyNotSupportedException( "We can query the bitcoin rate from Intersango only in USD at the moment");
	    } else {
		currency_pair_id = 3;
	    }

	    String url = "https://" + DOMAIN + "/api/ticker.php?currency_pair_id=" + currency_pair_id;
	    
	    String requestResult = HttpUtils.httpGet( url);

	    if( requestResult != null) { // Request successful?
		try {
		    // Concert the HTTP result into a JSON object.
		    JSONObject jsonResult = JSONObject.fromObject( requestResult);

		    Ticker ticker = new IntersangoTicker( jsonResult, currencyPair, this);

		    updateLastRequest();  // Update the timestamp of the last request.

		    return ticker;  // Return the parsed ticker.

		} catch( JSONException je) {
		    System.err.println( "Cannot parse ticker object: " + je.toString());
		}
	    }

	    return null;  // The ticker request failed.
	}

	// The request is not allowed at the moment, so throw an exception.
	throw new TradeDataRequestNotAllowedException( "Request for ticker not allowed at the moment at Intersango site");	
    }

    /**
     * Get the trades from this trading site.
     *
     * @param since_micros The GMT-relative epoch since to fetch the trades.
     * @param currencyPair The currency pair to use for the trades.
     *
     * @return  The trades as an array of Trade objects.
     */
    public List<Trade> getTrades( long since_micros,  CurrencyPair currencyPair) {

	int currency_pair_id;  // To select the 2 currencies at Intersango.

	// Determine the id of the currency pair.
	if( ! isSupportedCurrencyPair( currencyPair)) {
	    throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on Intersango");
	} else {
	    currency_pair_id = 3;
	}

	// Construct an URL from the given parameters.
	// Intersango seems to use seconds since epoch as the date format, so divide timestamp by 1000000.
	String url = "https://" + DOMAIN + "/api/trades.php?currency_pair_id=" + currency_pair_id + "&last_trade_date=" + ( since_micros / 1000000);

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
    private List<Trade> getTradesFromURL( String url, CurrencyPair currencyPair) {

	// If a request for trades is allowed
	if( isRequestAllowed( TradeSiteRequestType.Trades)) {

	    List<Trade> trades = new ArrayList<Trade>();
	    
	    String requestResult = HttpUtils.httpGet( url);

	    if( requestResult != null) {  // Request successful?
		try {
		    // Concert the HTTP result into a JSON object.
		    JSONArray resultArray = JSONArray.fromObject( requestResult);
		    
		    // Iterate over the json array and convert each trade from json to a Trade object.
		    for( int i = 0; i < resultArray.size(); i++) {
			JSONObject tradeObject = resultArray.getJSONObject(i);
			
			trades.add( new IntersangoTradeImpl( tradeObject, this, currencyPair));  // Add the new Trade object to the list.
		    }

		    updateLastRequest();  // Update the timestamp of the last request.

		    return trades;  // And return the list.

		} catch( JSONException je) {
		    System.err.println( "Cannot convert HTTP response to JSON array: " + je.toString());
		}
	    }

	    return null;  // An error occured.
	}

	// The request is not allowed at the moment, so throw an exception.
	throw new TradeDataRequestNotAllowedException( "Request for trades not allowed at the moment at Intersango site");
    }

    /**
     * Get the interval, in which the trade site updates it's depth, ticker etc. 
     * in microseconds.
     *
     * @return The update interval in microseconds.
     */
    public long getUpdateInterval() {
	return 61L * 1000000L;  // Since I don't use Intersango much at the moment, above 1 min should be ok.
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
     * Set new settings for this trade site client.
     *
     * @param settings The new settings for this trade site client.
     */
    public void setSettings( PersistentPropertyList settings) {

	super.setSettings( settings);

	String apiKey = settings.getStringProperty( "API key");
	if( apiKey != null) {
	    _api_key = apiKey;
	}
    }

    /**
     * Convert this trading site to a string.
     * At the moment just used for the project tree.
     *
     * @return The class as a String object.
     */
    public String toString() {
	return getName();
    }

    /**
     * Update the timestamp of the last request.
     */
    private void updateLastRequest() {
	_lastRequest = TimeUtils.getInstance().getCurrentGMTTimeMicros();
    }
}
