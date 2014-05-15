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

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.CryptoCoinTrade;
import de.andreas_rueckert.trade.currency.CurrencySymbolMapper;
import de.andreas_rueckert.trade.CurrencyNotSupportedException;
import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.OrderStatus;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteImpl;
import de.andreas_rueckert.trade.site.TradeSiteRequestType;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.trade.Ticker;
import de.andreas_rueckert.trade.TradeDataNotAvailableException;
import de.andreas_rueckert.util.HttpUtils;
import de.andreas_rueckert.util.LogUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * Main class for the kraken API.
 *
 * @see https://www.kraken.com/help/api
 */
public class KrakenClient extends TradeSiteImpl implements TradeSite {

    // Static variables

    /**
     * The domain of the service.
     */
    public static String DOMAIN = "kraken.com";
    

    // Instance variables

    /**
     * A mapping from currency pair objects to the kraken specific names of currency pairs.
     */
    private Map< CurrencyPair, String> _registeredCurrencyPairNames = new HashMap< CurrencyPair, String>();


    // Constructors

    /**
     * Create a new connection to the btc-e.com website.
     */
    public KrakenClient() {

	super();

	_name = "Kraken";
	_url = "https://api.kraken.com/";

	// Fetch the supported currency pairs.
	requestSupportedCurrencyPairs();
    }


    // Methods

    /**
     * Add the name for a new currency pair.
     *
     * @param currencyPair The currency pair.
     * @param krakenName The name of the pair at kraken.
     */
    private final void addCurrencyPairName( CurrencyPair currencyPair, String krakenName) {

	// Add the name of the pair to the map.
	_registeredCurrencyPairNames.put( currencyPair, krakenName);
    }

    /**
     * Cancel an order on the trade site.
     *
     * @param order The order to cancel.
     *
     * @return true, if the order was canceled. False otherwise.
     */
    public boolean cancelOrder( SiteOrder order) {

	throw new NotYetImplementedException( "Cancelling an order is not yet implemented for " + _name);
    }

    /**
     * Execute an order on the trade site.
     * Synchronize this method, since several users might execute orders in parallel via an API implementation instance.
     *
     * @param order The order to execute.
     *
     * @return The new status of the order.
     */
    public synchronized OrderStatus executeOrder( SiteOrder order) {

	throw new NotYetImplementedException( "Executing an order is not yet implemented for " + _name);
    }

    /**
     * Get the current funds of the user via the API.
     *
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The accounts with the current balance as a collection of Account objects, or null if the request failed.
     */
    public Collection<TradeSiteAccount> getAccounts( TradeSiteUserAccount userAccount) {

	throw new NotYetImplementedException( "Getting the accounts is not yet implemented for " + _name);
    }

    /**
     * Get the Kraken name for a given currency pair.
     *
     * @param currencyPair The currency pair.
     *
     * @return The Kraken name for the given pair or null, if it is not known.
     */
    private final String getCurrencyPairName( CurrencyPair currencyPair) {

	// Get the Kraken name of the pair from the map of registered names.
	return _registeredCurrencyPairNames.get( currencyPair);
    }
    
    /**
     * Get the market depth as a Depth object.
     *
     * @param currencyPair The queried currency pair.
     *
     * @throws TradeDataNotAvailableException if the depth is not available.
     */
    public Depth getDepth( CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	if( ! isSupportedCurrencyPair( currencyPair)) {
	    throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on " + _name);
	}

	// Get the Kraken name for the currency pair.
	String krakenPairName = getCurrencyPairName( currencyPair);

	// The URL for the depth request.
	String url = "https://api.kraken.com/" + "0/public/Depth?pair=" + krakenPairName;

	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?

	    try {

		// Convert the result to JSON.
		JSONObject requestResultObj = (JSONObject)JSONObject.fromObject( requestResult);

		// Check for errors.
		JSONArray errors = requestResultObj.getJSONArray( "errors!");

		// If there are errors
		if( errors.size() > 0) {

		    // Write the first error to the log for now. Hopefully it should explain the problem.
		    LogUtils.getInstance().getLogger().error( "Error while getting the Kraken depth. Error 0 is: " + errors.get( 0));
		    
		    // and return null.
		    return null;
		}

		// Get the JSON object with the ask and bid arrays from the JSON result.
		return new KrakenDepth( requestResultObj.getJSONObject( "result").getJSONObject( krakenPairName), currencyPair, this);

	    } catch( JSONException je) {

		System.err.println( "Cannot parse " + this._name + " depth return: " + je.toString());

		throw new TradeDataNotAvailableException( "cannot parse data from " + this._name);
	    }
	}
	
	throw new TradeDataNotAvailableException( this._name + " server did not respond to depth request");
    }

    /**
     * Get the shortest allowed requet interval in microseconds.
     *
     * @return The shortest allowed request interval in microseconds.
     */
    public final long getMinimumRequestInterval() {
	return getUpdateInterval();
    }


    /**
     * Get the open orders on this trade site.
     *
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The open orders as a collection, or null if the request failed.
     */
    public Collection<SiteOrder> getOpenOrders( TradeSiteUserAccount userAccount) {

	throw new NotYetImplementedException( "Getting the open orders is not yet implemented for " + _name);
    }

    /**
     * Get the section name in the global property file.
     *
     * @return The name of the property section as a String.
     */
    public String getPropertySectionName() {

	return "Kraken";
    }

    /**
     * Get the current ticker from the API.
     *
     * @param currencyPair The currency pair to query.
     * @param paymentCurrency The currency for the payments.
     *
     * @return The current ticker.
     *
     * @throws TradeDataNotAvailableException if the ticker is not available.
     */
    public Ticker getTicker( CurrencyPair currencyPair) throws TradeDataNotAvailableException {
	
	if( ! isSupportedCurrencyPair( currencyPair)) {
	    throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on " + _name);
	}

	// Get the Kraken name for the currency pair.
	String krakenPairName = getCurrencyPairName( currencyPair);

	// The URL for the ticker request.
	String url = "https://api.kraken.com/" + "0/public/Ticker?pair=" + krakenPairName;

	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?

	    try {

		// Convert the result to JSON.
		JSONObject requestResultObj = (JSONObject)JSONObject.fromObject( requestResult);

		// Check for errors.
		JSONArray errors = requestResultObj.getJSONArray( "errors!");
		
		// If there are errors
		if( errors.size() > 0) {

		    // Write the first error to the log for now. Hopefully it should explain the problem.
		    LogUtils.getInstance().getLogger().error( "Error while getting the Kraken depth. Error 0 is: " + errors.get( 0));
		    
		    // and return null.
		    return null;
		}

		// Convert the HTTP request return value to JSON to parse further.
		return new KrakenTicker( requestResultObj.getJSONObject( "result").getJSONObject( krakenPairName), currencyPair, this);

	    } catch( JSONException je) {

		System.err.println( "Cannot parse ticker object: " + je.toString());
	    }
	}
	
	throw new TradeDataNotAvailableException( "The " + _name + " ticker request failed");
    }

    /**
     * Get a list of recent trades.
     *
     * @param since_micros The GMT-relative epoch in microseconds.
     * @param currencyPair The currency pair to query.
     *
     * @return The trades as a list of Trade objects.
     *
     * @throws TradeDataNotAvailableException if the ticker is not available.
     */
    public CryptoCoinTrade [] getTrades( long since_micros, CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	throw new NotYetImplementedException( "Getting the trades is not yet implemented for " + _name);
    }

    /**
     * Get the interval, in which the trade site updates it's depth, ticker etc. 
     * in microseconds.
     *
     * @return The update interval in microseconds.
     */
    public final long getUpdateInterval() {
	return 15L * 1000000L;  // 15s should work for most exchanges. Dont't know the actual frequency (a_rueckert).
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

	return true;  // Just a dummy for now.
    }

    /**
     * Check, if a given currency pair is supported on this site.
     *
     * @param currencyPair The currency pair to check for this site.
     *
     * @return true, if the currency pair is supported. False otherwise.
     */
    public boolean isSupportedCurrencyPair( CurrencyPair currencyPair) {

	// If we have a Kraken name for this pair, it should be supported.
	return _registeredCurrencyPairNames.containsKey( currencyPair);
    }

    /**
     * Fetch the supported currency pairs from the Kraken server.
     */
    private boolean requestSupportedCurrencyPairs() {

	String url = _url + "0/public/AssetPairs";  // The URL for fetching the traded pairs.

	// Request info on the traded pairs from the server.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // If the server returned a response.

	    // Try to parse the response.
	    JSONObject jsonResult = JSONObject.fromObject( requestResult);

	    // Test, if there is a result returned (might be missing, if an error occurred).
	    if( jsonResult.containsKey( "result")) {

		// The pairs are returned as a JSON object.
		JSONObject pairListJSON = jsonResult.getJSONObject( "result");

		// Iterate over the entries of this object.
		for( Iterator keyIterator = pairListJSON.keys(); keyIterator.hasNext(); ) {

		    // The key is the name of the pair in kraken style...
		    String krakenPairName = (String)keyIterator.next(); 

		    // Get the next currency pair as a JSON object.
		    JSONObject currentCurrencyPairJSON = pairListJSON.getJSONObject( krakenPairName);

		    de.andreas_rueckert.trade.Currency currency = CurrencySymbolMapper.getCurrencyForIso4217Name( currentCurrencyPairJSON.getString( "base"));

		    de.andreas_rueckert.trade.Currency paymentCurrency = CurrencySymbolMapper.getCurrencyForIso4217Name( currentCurrencyPairJSON.getString( "quote"));

		    // Create a pair from the currencies.
		    de.andreas_rueckert.trade.CurrencyPair currentPair = new de.andreas_rueckert.trade.CurrencyPairImpl( currency, paymentCurrency);

		    // Add the pair with it's kraken name to the map of pair names.
		    addCurrencyPairName( currentPair, krakenPairName);

		    System.out.println( "DEBUG: Kraken: found currency pair " + currentPair.toString());

		    // ToDo: also parse decimals for precision etc?

		    
		}
	    }
	}

	return false;   // Fetching the traded currency pairs failed.

	// throw new NotYetImplementedException( "Fetching the supported currency pairs is not yet implemented for Kraken");
    }
}