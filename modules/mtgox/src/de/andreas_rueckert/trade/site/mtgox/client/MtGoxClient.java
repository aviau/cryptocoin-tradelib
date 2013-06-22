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

package de.andreas_rueckert.trade.site.mtgox.client;

import de.andreas_rueckert.MissingAccountDataException;
import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.persistence.PersistentProperty;
import de.andreas_rueckert.persistence.PersistentPropertyList;
import de.andreas_rueckert.trade.account.CryptoCoinAccountImpl;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.CryptoCoinTrade;
import de.andreas_rueckert.trade.Currency;
import de.andreas_rueckert.trade.CurrencyImpl;
import de.andreas_rueckert.trade.CurrencyNotSupportedException;
import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.CurrencyPairImpl;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.CryptoCoinOrderBook;
import de.andreas_rueckert.trade.order.DepositOrder;
import de.andreas_rueckert.trade.order.Order;
import de.andreas_rueckert.trade.order.OrderNotInOrderBookException;
import de.andreas_rueckert.trade.order.OrderStatus;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.order.WithdrawOrder;
import de.andreas_rueckert.trade.site.TradeDataRequestNotAllowedException;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteImpl;
import de.andreas_rueckert.trade.site.TradeSiteRequestType;
import de.andreas_rueckert.trade.Ticker;
import de.andreas_rueckert.trade.Trade;
import de.andreas_rueckert.util.HttpUtils;
import de.andreas_rueckert.util.LogUtils;
import de.andreas_rueckert.util.TimeUtils;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException; 
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;


/**
 * Main class to handle mtgox requests.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * @see https://en.bitcoin.it/wiki/MtGox/API/HTTP/v1#HTTP_API_version_1_methods
 */
public class MtGoxClient extends TradeSiteImpl implements TradeSite {

    // Static variables

    /**
     * The domain of the service.
     */
    public static String DOMAIN = "mtgox.com";


    // Instance variables

    /**
     * The currently used currency.
     */
    private Currency _currentCurrency = CurrencyImpl.USD;

    /**
     * The MtGox provided key.
     */
    private String _key;

    /**
     * The timestamp of the last requests for the request types, that have 
     * to be limited.
     */
    private long _lastRequestTimestampForDepth = -1L;
    private long _lastRequestTimestampForTicker = -1L;
    private long _lastRequestTimestampForTrades = -1L;
    

    /**
     * The MtGox provided secret.
     */
    private String _secret;

    /**
     * The microsecond base time (approximated from the millisecond timestamp).
     */
    private long _timestampBase = -1;

    /**
     * The nanosecond offset to add to the base.
     */
    private long _nanoTimeOffset;


    // Constructors

    /**
     * Create a new MtGox client.
     */
    public MtGoxClient() {
	super();

	_name = "MtGox";
	_url = "https://mtgox.com/";

	// Define the supported currency pairs for this trading site.
	_supportedCurrencyPairs = new CurrencyPair[1];
	_supportedCurrencyPairs[0] = new CurrencyPairImpl( CurrencyImpl.BTC, CurrencyImpl.USD);

	// Higher the log level.
	this._logLevel = LOGLEVEL_ERROR;
    }


    // Methods

    /**
     * Perform a query with user authentication and return the HTTP post reply as a string.
     *
     * @param requestUrl The URL to query.
     * @param parameters Additional parameters for the request.
     *
     * @return The request result as a JSONObject, if the JSON result object has a success value. null otherwise
     */
    private final JSONObject authenticatedQuery( String requestUrl, Map< String, String> parameters) {
	// Create the post data to identify the logged in user.
	String postData = "nonce=" + TimeUtils.getInstance().getCurrentGMTTimeMicros();

	// Add the otional parameters to the query
	if( parameters != null) {

	    // Loop over the parameters
	    for( Map.Entry< String, String> entry : parameters.entrySet()) {

		// Add each parameter with it's value to the list of parameters.
		postData += "&" + entry.getKey() + "=" + entry.getValue();
	    }
	}

	// Query the MtGox server and return the HTTP result as a string.
	String requestResult = HttpUtils.httpPost( requestUrl, getAuthenticationHeader( postData), postData);

	try {
	    // Convert the HTTP request return value to JSON to parse further.
	    JSONObject jsonResult = JSONObject.fromObject( requestResult);

	    // Get the result value to check for an error
	    if( "error".equals( jsonResult.getString( "result"))) {

		// Output the error to the error stream.
		System.err.println( "MtGox query error: " + jsonResult.getString( "error"));

		return null;  // MtGox query did not success, so just return null.
	    }
	    
	    // Return the entire json result object.
	    return jsonResult;

	} catch( JSONException je) {
	    System.err.println( "Cannot parse json request result: " + je.toString());

	    return null;  // An error occured...
	}
    }

    /**
     * Get the accounts with the current funds on this trading site.
     *
     * @return The accounts with the current balance as an array of Account objects.
     */
    public Collection<TradeSiteAccount> getAccounts() {
	
	JSONObject privateInfo = getPrivateInfo();

	if( privateInfo != null) {
	    System.out.println( "Private info is: " + privateInfo.toString());
	} else {
	    System.out.println( "Private info is null!");
	}

	throw new NotYetImplementedException( "Get accounts is not yet implemented for MtGox");
    }
    
    

    /**
     * Execute an order on the trade site.
     *
     * @param order The order to execute.
     *
     * @return The new status of the order.
     */
    public OrderStatus executeOrder( SiteOrder order) {

	OrderType orderType = order.getOrderType();  // Get the type of this order.

	if( ( orderType == OrderType.BUY) || ( orderType == OrderType.SELL)) {  // If this is a buy or sell order, run the trade code.

	    if( ! isSupportedCurrencyPair( order.getCurrencyPair())) {
		throw new CurrencyNotSupportedException( "This currency pair is not support in MtGox orders: " 
							 + order.getCurrencyPair().getCurrency().toString() 
							 + " and payment in "
							 + order.getCurrencyPair().getPaymentCurrency());
		}

	    String url = "https://" + DOMAIN + "/api/1/" 
		+ order.getCurrencyPair().getCurrency().getName().toUpperCase() 
		+ order.getCurrencyPair().getPaymentCurrency().getName().toUpperCase()
		+ "/private/order/add";
		
	    // The parameters for the HTTP post call.
	    HashMap<String, String> parameters = new HashMap<String, String>();
	    
	    parameters.put( "type", order.getOrderType() == OrderType.BUY ? "bid" : "ask");  // Indicate buy or sell.
	    parameters.put( "amount", "" + order.getAmount());
	    parameters.put( "price", "" + order.getPrice());  // ToDo: format price and amount? Don't know, how picky mtgox is about the format...
	    
	    // Query the MtGox server and fetch the result as a json object.
	    JSONObject jsonResult = authenticatedQuery( url, parameters);
		
	    if( jsonResult != null) {
		if( "success".equalsIgnoreCase( jsonResult.getString( "result"))) {  // The order was executed.
		    
		    // Now store the site id of the order in the order object,
		    // so we can check the status of the order later.
		    order.setSiteId( jsonResult.getString( "return"));  
		    
		    // We could check the status of the order now, or just return, that it is unknown.
		    order.setStatus( OrderStatus.UNKNOWN);
		    return order.getStatus();
		} else {
		    order.setStatus( OrderStatus.ERROR);
		    return order.getStatus();
		}
	    }

	} else if( orderType == OrderType.DEPOSIT) {  // This is a deposit order...

	    DepositOrder depositOrder = (DepositOrder)order;  // Just to avoid constant casting.

	      // Get the deposited currency from the order.
	    Currency depositedCurrency = depositOrder.getCurrency();

	    // Check, if this currency is supported yet in this implementation.
	    if( depositedCurrency.equals( CurrencyImpl.BTC)) {
		
		// Get the address for a deposit from the trade site.
		String depositAddress = getDepositAddress( depositedCurrency);

		// Attach a new account for depositing to this order.
		depositOrder.setAccount( new CryptoCoinAccountImpl( depositAddress
								    , new BigDecimal( "0")
								    , depositedCurrency));

		// Now return a new order status to indicate, that the order was modified.
		return OrderStatus.DEPOSIT_ADDRESS_GENERATED;

	    } else {  // This currency is not supported yet.

		throw new CurrencyNotSupportedException( "Depositing the currency " 
							 + depositedCurrency 
							 + " is not supported yet in this implementation");
	    }


	} else if( orderType == OrderType.WITHDRAW) {  // This is a withdrawal order...

	    throw new NotYetImplementedException( "Withdrawal from MtGox is not yet implemented");

	} 

	return null;
    }

    /**
     * Create authentication entries for a HTTP post header.
     *
     * @param postData The data to post via HTTP.
     *
     * @return The header entries as a map or null if an error occured.
     */
    Map< String, String> getAuthenticationHeader( String postData) {
	HashMap<String, String> result = new HashMap<String, String>();
	Mac mac;

	// Check, if _key and _secret are available for the request.
	if( _key == null) {
	    throw new MissingAccountDataException( "Key not available for authenticated request to btc-e");
	}
	if( _secret == null) {
	    throw new MissingAccountDataException( "Secret not available for authenticated request to btc-e");
	}

	result.put( "Rest-Key", _key);

	// Create a new secret key
	SecretKeySpec key = new SecretKeySpec( Base64.decodeBase64( _secret), "HmacSHA512" );       

	// Create a new mac
	try {
	    mac = Mac.getInstance( "HmacSHA512" );
	} catch( NoSuchAlgorithmException nsae) {
	    System.err.println( "No such algorithm exception: " + nsae.toString());
	    return null;
	}

	// Init mac with key.
	try {
	    mac.init( key );
	} catch( InvalidKeyException ike) {
	    System.err.println( "Invalid key exception: " + ike.toString());
	    return null;
	}

	// Encode the post data by the secret and encode the result as base64.
	try {
	    result.put( "Rest-Sign", Base64.encodeBase64String( mac.doFinal( postData.getBytes( "UTF-8"))));
	} catch( UnsupportedEncodingException uee) {
	    System.err.println( "Unsupported encoding exception: " + uee.toString());
	    return null;
	}

	return result;
    }

    /**
     * Get all the cancelled trades of the current user.
     *
     * @param currencyPair The currency pair to use.
     *
     * @return The cancelled trades as an array of Trade objects or null if the request failed.
     */
    Trade [] getCancelledTrades( CurrencyPair currencyPair) {

	// Query the MtGox server and fetch the result as a json object.
	JSONObject jsonResult = authenticatedQuery( "https://" + DOMAIN + "/api/1/"
						    + currencyPair.getCurrency().getName() + currencyPair.getPaymentCurrency().getName() 
						    + "/public/cancelledtrades"
						    , null);

	if( jsonResult != null) {
	    try {
		// Get the return value and convert it to a Trade array.
		JSONArray jsonTrades = jsonResult.getJSONArray( "return");

		CryptoCoinTrade [] trades = new CryptoCoinTrade[ jsonTrades.size()];
		
		try {
		    for( int i = 0; i < jsonTrades.size(); i++) {
			trades[ i] = new MtGoxTradeImpl( jsonTrades.getJSONObject( i), this, currencyPair);
		    }
		} catch( ParseException pe) {  // Cannot parse the JSON trade.
		    System.err.println( "Cannot parse JSON trade object: " + pe.toString());
		    
		    return null;  // Error => return null...
		}
		
		return trades;  // Return the parsed trades.

	    } catch( JSONException je) {
		System.err.println( "Cannot parse trade objects: " + je.toString());
	    }
	}

	return null;  // The query failed.
    }

    /**
     * Get the currently set currency.
     *
     * @return The currently set currency.
     */
    public Currency getCurrentCurrency() {
	return _currentCurrency;
    }

    /**
     * Get an address to deposit coins at btc-e.
     *
     * @param currency The currency to deposit.
     *
     * @return The deposit address as a string, or null if no address is found.
     */
    private String getDepositAddress( Currency currency) {

	// The URL to request the address from.
	String url = null;

	// Check, if the currency is supported
	if( currency.equals( CurrencyImpl.BTC)) {

	    // Set the url to fetch the deposit address.
	    url = "https://data.mtgox.com/api/1/generic/bitcoin/address";

	    // Do a authenticated query for the deposit address.
	    JSONObject result = authenticatedQuery( url, null);

	    // Get the return value and convert it to an object.
	    JSONObject jsonAddress = result.getJSONObject( "return");

	    // Get the actual address as a string and return it.
	    return jsonAddress.getString( "addr");

	} else {
	    
	    throw new CurrencyNotSupportedException( "Getting a deposit address for currency " 
						     + currency 
						     + " is not yet supported by this implementation");
	}
    }

    /**
     * Get the trade data from mtgox.
     *
     * @param currencyPair The currency pair to query.
     *
     * @return The trade data as a Depth object.
     */
    public Depth getDepth( CurrencyPair currencyPair) {

	// System.out.println( "Fetching depth from MtGox for " + currencyPair.getName());

	// If the user wants intense logging, add some info.
	if( getLogLevel() > LOGLEVEL_WARNING) {
	    LogUtils.getInstance().getLogger().info( "Fetching depth from " + getName());
	}

	if( isRequestAllowed( TradeSiteRequestType.Depth)) {

	    if( ! isSupportedCurrencyPair( currencyPair)) {
		throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on MtGox");
	    }
	    
	    String requestUrl = "https://" 
		+ DOMAIN + "/api/1/" 
		+ currencyPair.getCurrency().getName() + currencyPair.getPaymentCurrency().getName() 
		+ "/public/depth?raw";

	    // System.out.println( "Fetching mtgox depth from: " + requestUrl);
	    
	    String requestResult = HttpUtils.httpGet( requestUrl);
	
	    if( requestResult != null) {  // Request sucessful?
		try {
		    // Convert the HTTP request return to JSON to parse further.
		    Depth depth = new MtGoxDepth( JSONObject.fromObject( requestResult), currencyPair, this);

		    updateLastRequest( TradeSiteRequestType.Depth);  // Update the timestamp of the last request.

		    return depth;
		} catch( JSONException je) {
		    System.err.println( "Cannot parse mtgox depth return: " + je.toString());
		}
	    }

	    return null;  // The depth request failed.
	}

  	// The request is not allowed at the moment, so throw an exception.
	throw new TradeDataRequestNotAllowedException( "Request for depth not allowed at the moment at MtGox site");	
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
     * Get the open orders of the user.
     *
     * @return The open orders as a collection
     */
    public Collection<SiteOrder> getOpenOrders() {
	JSONObject jsonResult = authenticatedQuery( "https://mtgox.com/api/1/generic/private/orders", null);

	if( jsonResult != null) {
	    JSONArray requestReturn = jsonResult.getJSONArray( "return");  // Get the return value as an array

	    // Create a buffer for the result.
	    ArrayList<SiteOrder> result = new ArrayList<SiteOrder>();

	    // Now loop over the open orders.
	    for( Iterator orderIterator = requestReturn.iterator();  orderIterator.hasNext(); ) {
		JSONObject currentJSONOrder = (JSONObject)orderIterator.next();

		// Get the site id from this order.
		String currentSiteId = currentJSONOrder.getString( "oid");

		// Since we know the tradesite and the site id now, we can query the order book for the order.
		SiteOrder currentOrder = CryptoCoinOrderBook.getInstance().getOrder( this, currentSiteId);

		if( currentOrder != null) {     // If the order book returned an order,
		    result.add( currentOrder);  // add it to the result buffer.
		} else {  // It seems, this order is not in the order book. I can consider this an error at the moment,
		          // since every order should go through the order book.

		    throw new OrderNotInOrderBookException( "Error: btc-e order with site id " + currentSiteId + " is not in order book!");
		}
	    }
	    
	    return result;  // Return the buffer with the orders.
	}

	return null;  // The query failed.
    }

    /**
     * Request the private info of the current user.
     *
     * @return The private info of the current user.
     */
    public JSONObject getPrivateInfo() {
	JSONObject jsonResult = authenticatedQuery( "https://mtgox.com/api/1/generic/private/info", null);

	if( jsonResult != null) {
	    JSONObject requestReturn = jsonResult.getJSONObject( "return");

	    return requestReturn;
	}

	return null;  // Query failed...
    }

    /**
     * Get the section name in the global property file.
     */
    public String getPropertySectionName() {
	return "MtGox";
    }

    /**
     * Get the current settings of the MtGox client.
     *
     * @return The current settings of the MtGox client.
     */
    public PersistentPropertyList getSettings() {

	// Get the settings from the base class.
	PersistentPropertyList result = super.getSettings();

	result.add( new PersistentProperty( "Key", null, _key, 5));        // The key is a parameter.
	result.add( new PersistentProperty( "Secret", null, _secret, 4));  // The secret the next parameter.
	
	return result;  // Return the map with the settings.
    }

    /**
     * Get the current ticker from MtGox API version 1.
     *
     * @param currencyPair The currency pair to use for the data.
     *
     * @return The current MtGox ticker.
     */
    public Ticker getTicker( CurrencyPair currencyPair) {

	if( isRequestAllowed( TradeSiteRequestType.Ticker)) {

	    String url = "https://" + DOMAIN  + "/api/1/" 
		+ currencyPair.getCurrency().getName() + currencyPair.getPaymentCurrency().getName() + "/public/ticker";

	    String requestResult = HttpUtils.httpGet( url);

	    if( requestResult != null) {  // Request successful?
		try {
		    // Convert the HTTP request return value to JSON to parse further.
		    JSONObject jsonResult = JSONObject.fromObject( requestResult);
		
		    // Get the result value to check for success
		    if( ! "success".equals( jsonResult.getString( "result"))) {
			return null;  // MtGox server did not return the ticker.
		    }
		    Ticker ticker = new MtGoxTicker( jsonResult.getJSONObject( "return"), currencyPair, this);

		    updateLastRequest( TradeSiteRequestType.Ticker);  // Update the timestamp of the last request.

		    return ticker;

		} catch( JSONException je) {
		    System.err.println( "Cannot parse ticker object: " + je.toString());
		}
	    }

	    return null;  // The ticker request failed.
	}

  	// The request is not allowed at the moment, so throw an exception.
	throw new TradeDataRequestNotAllowedException( "Request for ticker not allowed at the moment at MtGox site");
    }

    /**
     * Get a number of trades.
     *
     * @param tid The id of the first trade to get or just 0 to get all trades availables. The id is the
     * microsecond timestamp of the trade, so the tid is used as a timespan filter, too!
     * @param currencyPair The currency pair to query.
     *
     * @return The trades as an array of CryptoCoinTrade objects.
     */
    public CryptoCoinTrade [] getTrades( long tid, CurrencyPair currencyPair) {

	if( ! isSupportedCurrencyPair( currencyPair)) {
	    throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on MtGox");
	}

	String requestUrl = "https://" + DOMAIN + "/api/1/" 
	    +  currencyPair.getCurrency().getName() + currencyPair.getPaymentCurrency().getName()  
	    + "/public/trades?since=" + tid;

	return getTradesFromURL( requestUrl, currencyPair);
    }
    
    /**
     * Get a number of trades from a given URL.
     *
     * @param url The URL to fetch the trades from.
     * @param currencyPair The requested currency pair.
     *
     * @return The trades as an array of Trade objects or null if an error occured.
     */
    private CryptoCoinTrade [] getTradesFromURL( String url, CurrencyPair currencyPair) {

	if( isRequestAllowed( TradeSiteRequestType.Trades)) {

	    ArrayList<CryptoCoinTrade> trades = new ArrayList<CryptoCoinTrade>();

	    // System.out.println( "Fetching trades from url: " + url);

	    String requestResult = HttpUtils.httpGet( url);

	    if( requestResult != null) {  // Request sucessful?
		try {
		    // Convert the HTTP request return value to JSON to parse further.
		    JSONObject jsonResult = JSONObject.fromObject( requestResult);

		    // Get the result value to check for success
		    if( ! "success".equals( jsonResult.getString( "result"))) {
			return null;  // MtGox server did not return the ticker.
		    }	

		    // Convert the result to a JSON array.
		    JSONArray resultArray = jsonResult.getJSONArray( "return");

		    // Iterate over the json array and convert each trade from json to a Trade object.
		    for( int i = 0; i < resultArray.size(); i++) {
			JSONObject tradeObject = resultArray.getJSONObject(i);
		    
			try {
			    trades.add( new MtGoxTradeImpl( tradeObject, this, currencyPair));  // Add the new Trade object to the list.
			} catch( ParseException pe) {  // Cannot parse the JSON trade.
			    System.err.println( "Cannot parse JSON trade object: " + pe.toString());
			
			    return null;
			}
		    }

		    CryptoCoinTrade [] tradeArray = trades.toArray( new CryptoCoinTrade[ trades.size()]);  // Convert the list to an array.
		    
		    updateLastRequest( TradeSiteRequestType.Trades);  // Update the timestamp of the last request.

		    return tradeArray;  // And return the array.

		} catch( JSONException je) {
		    System.err.println( "Cannot parse trade object: " + je.toString());
		}
	    }
	    return null;  // An error occured.
	}

       	// The request is not allowed at the moment, so throw an exception.
	throw new TradeDataRequestNotAllowedException( "Request for trades not allowed at the moment at MtGox site");
    }

    /**
     * Get the interval, in which the trade site updates it's depth, ticker etc. 
     * in microseconds.
     *
     * @return The update interval in microseconds.
     */
    public long getUpdateInterval() {
	return 15L * 1000000L;  // The default MtGox update happens every 15s, I think.
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

	// MtGox is quite strict about the request limitations, so I have to check each request type separately...

	// Get the current time.
	long currentTimestamp = TimeUtils.getInstance().getCurrentGMTTimeMicros();

	switch( requestType) {
	    case Depth: return ( _lastRequestTimestampForDepth == -1L) || ( currentTimestamp > _lastRequestTimestampForDepth + 15000000L);
	    case Ticker: return ( _lastRequestTimestampForTicker == -1L) || ( currentTimestamp > _lastRequestTimestampForTicker + 15000000L);
	    case Trades: return ( _lastRequestTimestampForTrades == -1L) || ( currentTimestamp > _lastRequestTimestampForTrades + 15000000L);
	}

	return true;  // I guess, we can always execute orders...
    }

    /**
     * Set a new MtGox key.
     *
     * @param key The new MtGox key.
     */
    private void setKey( String key) {
	_key = key;
    }

    /**
     * Set a new MtGox secret.
     *
     * @param secret The new MtGox secret.
     */
    private void setSecret( String secret) {
	_secret = secret;
    }

    /**
     * Set new settings for the client.
     *
     * @param settings The new settings for the client.
     */
    public void setSettings( PersistentPropertyList settings) {

	super.setSettings( settings);
	
	String key = settings.getStringProperty( "Key");
	if( key != null) {
	    setKey( key);  // Get the API key from the settings.
	}
	String secret = settings.getStringProperty( "Secret");
	if( secret != null) {
	    setSecret( secret);  // Get the secret from the settings.
	}
    }

    /**
     * Return a string for this site (just a name for now).
     * To be used in the project tree.
     */
    public String toString() {
	return getName();
    }

    /**
     * Update the timestamp of the last request.
     *
     * @param requestType The type of the last request.
     */
    public void updateLastRequest( TradeSiteRequestType requestType) {

	// I don't count the orders, since should be posted at any time, I think...
	switch( requestType) {
	    case Depth: _lastRequestTimestampForDepth = TimeUtils.getInstance().getCurrentGMTTimeMicros(); break;
	    case Ticker: _lastRequestTimestampForTicker = TimeUtils.getInstance().getCurrentGMTTimeMicros(); break;
	    case Trades: _lastRequestTimestampForTrades = TimeUtils.getInstance().getCurrentGMTTimeMicros(); break;
	}
    }
}
