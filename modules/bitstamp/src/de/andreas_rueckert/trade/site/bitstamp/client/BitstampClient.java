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

package de.andreas_rueckert.trade.site.bitstamp.client;

import de.andreas_rueckert.MissingAccountDataException;
import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.persistence.PersistentProperty;
import de.andreas_rueckert.persistence.PersistentPropertyList;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.account.TradeSiteAccountImpl;
import de.andreas_rueckert.trade.CryptoCoinTrade;
import de.andreas_rueckert.trade.Currency;
import de.andreas_rueckert.trade.CurrencyImpl;
import de.andreas_rueckert.trade.CurrencyNotSupportedException;
import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.CurrencyPairImpl;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.CryptoCoinOrderBook;
import de.andreas_rueckert.trade.order.DepositOrder;
import de.andreas_rueckert.trade.order.OrderNotInOrderBookException;
import de.andreas_rueckert.trade.order.OrderStatus;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.order.WithdrawOrder;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteImpl;
import de.andreas_rueckert.trade.site.TradeSiteRequestType;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.trade.TradeDataNotAvailableException;
import de.andreas_rueckert.util.HttpUtils;
import de.andreas_rueckert.util.LogUtils;
import de.andreas_rueckert.util.TimeUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * Main class for the Bitstamp API.
 *
 * @see https://www.bitstamp.net/api/
 */
public class BitstampClient extends TradeSiteImpl implements TradeSite {

    // Inner classes

    /**
     * Class to hold the current fee of Bitstamp.
     *
     * @see https://www.bitstamp.net/fee_schedule/
     */
    class BitstampFee {

	// Instance variables

	/**
	 * The current fee.
	 */
	BigDecimal _fee;
	
	/**
	 * The timestamp, when this fee was updated.
	 */
	long _timestamp = -1L;


	// Constructors


	// Methods

	/**
	 * Get the stored fee, or null, if no fee was set yet.
	 *
	 * @return The fee or null, of no fee was set yet.
	 */
	public BigDecimal getFee() {
	 
	    // Return the fee (might be null, if it is not set yet).
	    return _fee;
	}

	/**
	 * Invalidate fee. This might be necessary after executing a trade, so the 
	 * volume of the last 30 days has changed. This might mean, that the fee is
	 * lowered then.
	 */
	public void invalidateFee() {
	    
	    _timestamp = -1L;  // Reset the timestamp to default.
	    _fee = null;       // And remove the stored fee.
	}

	/**
	 * Check, if this fee is dated (older than 24 hours).
	 *
	 * @return true, if the fee is older than 24 hours.
	 */
	public boolean isDated() {
	    
	    // Check, if this timestamp was never updated or is older than 24 hours.
	    return ( _timestamp == -1L) 
		|| ( ( TimeUtils.getInstance().getCurrentGMTTimeMicros() - _timestamp) > ( 24L * 60L * 60L * 1000000L));
	}

	/**
	 * Set a new fee for Bitstamp.
	 *
	 * @param newFee The new fee to set.
	 */
	public void updateFee( BigDecimal newFee) {

	    System.out.println( "Updating fee");

	    // Store the new fee in this instance.
	    _fee = newFee;

	    // And update the timestamp.
	    updateTimestamp();
	}
	
	/**
	 * Update the timestamp to the current time.
	 */
	private void updateTimestamp() {
	    
	    // Store the current microsecond time.
	    _timestamp = TimeUtils.getInstance().getCurrentGMTTimeMicros();
	}
    }

    // Static variables

    /**
     * The domain of the service.
     */
    public static String DOMAIN = "bitstamp.net";


    // Instance variables

    /**
     * An object to store the current bitstamp fee.
     */
    private BitstampFee _fee = new BitstampFee();

    /**
     * The password of the user.
     */
    private String _password = null;
    
    /**
     * The user id of the Bitstamp user.
     */
    private String _userId = null;


    // Constructors

    /**
     * Create a new connection to the Bitstamp website.
     */
    public BitstampClient() {
	super();

	_name = "Bitstamp";
	_url = "https://www." + this.DOMAIN + "/";

		// Define the supported currency pairs for this trading site.
	_supportedCurrencyPairs = new CurrencyPair[1];
	_supportedCurrencyPairs[0] = new CurrencyPairImpl( CurrencyImpl.BTC, CurrencyImpl.USD);
    }
    

    // Methods
    
    /**
     * Execute a authenticated HTTP request on Bitstamp.
     *
     * @param url The url to request from.
     * @param arguments The argments for the request.
     * @param returnArray if true, return a JSONArray. If false, return a JSONObject.
     * @param userAccount The account of the user on the exchange.
     *
     * @return The returned data as JSON or null, if the request failed.
     */
    private Object authenticatedHTTPRequest( String url, Map<String, String> arguments, boolean returnArray, TradeSiteUserAccount userAccount) {
	String userId = null;
	String password;

	HashMap<String, String> headerLines = new HashMap<String, String>();  // Create a new map for the header lines.

	if( arguments == null) {  // If the user provided no arguments, just create an empty argument array.
	    arguments = new HashMap<String, String>();
	}

	// Try to add username and password to the parameters.
	if( userAccount != null) {

	    userId = userAccount.getUserId();
	    password = userAccount.getPassword();

	} else { // User the default identity from the API implementation.
	    
	    userId = _userId;
	    password = _password;
	}

	if( userId == null || password == null) {
	    throw new MissingAccountDataException( "User ID or password missing in Bitstamp method getOpenOrders()");
	}

	// Add the account data to the parameters.
	arguments.put( "user", userId);
	arguments.put( "password", password);

	// Convert the arguments into a string to post them.
	String postData = "";

	for( Iterator argumentIterator = arguments.entrySet().iterator(); argumentIterator.hasNext(); ) {
	    Map.Entry argument = (Map.Entry)argumentIterator.next();
	    
	    if( postData.length() > 0) {
		postData += "&";
	    }
	    postData += argument.getKey() + "=" + argument.getValue();
	}

    	// Now do the actual request
	String requestResult = HttpUtils.httpPost( "https://" + DOMAIN + "/tapi", headerLines, postData);

	if( requestResult != null) {   // The request worked

	    try {

		// Convert the HTTP request return value to JSON to parse further.
		// The returned type depends on the returnArray flag.
		return returnArray ? JSONArray.fromObject( requestResult) : JSONObject.fromObject( requestResult);

	    } catch( JSONException je) {
		System.err.println( "Cannot parse json request result: " + je.toString());

		return null;  // An error occured...
	    }
	} 

	return null;  // An error occurred.
    }

    /**
     * Execute a authenticated HTTP request on Bitstamp and return an array.
     * This is just a convenience wrapper for the authenticatedHTTPRequest method.
     *
     * @param url The url to request from.
     * @param arguments The argments for the request.
     * @param userAccount The account of the user on the exchange.
     *
     * @return The returned data as JSON array or null, if the request failed.
     */
    private JSONArray authenticatedHTTPRequestArray( String url, Map<String, String> arguments, TradeSiteUserAccount userAccount) {
	return (JSONArray)authenticatedHTTPRequest( url, arguments, true, userAccount);
    }

    /**
     * Execute a authenticated HTTP request on Bitstamp and return an JSON object.
     * This is just a convenience wrapper for the authenticatedHTTPRequest method.
     *
     * @param url The url to request from.
     * @param arguments The argments for the request.
     * @param userAccount The account of the user on the exchange.
     *
     * @return The returned data as a JSON object or null, if the request failed.
     */
    private JSONObject authenticatedHTTPRequestObject( String url, Map<String, String> arguments, TradeSiteUserAccount userAccount) {
	return (JSONObject)authenticatedHTTPRequest( url, arguments, false, userAccount);
    }

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
     * Since many users might execute orders via one API implementation, this method is synchronized.
     *
     * @param order The order to execute.
     *
     * @return The new status of the order.
     */
    public synchronized OrderStatus executeOrder( SiteOrder order) {

	OrderType orderType = order.getOrderType();  // Get the type of this order.

	if( ( orderType == OrderType.BUY) || ( orderType == OrderType.SELL)) {  // If this is a buy or sell order, run the trade code.

	    // Execute the order.

	    // Invalidate the current fee, because the traded volumen has changed now.
	    _fee.invalidateFee();

	    throw new NotYetImplementedException( "Executing trades is not yet implemented for " + this.getName());

	} else if( orderType == OrderType.DEPOSIT) { // This is a deposit order..

	    throw new NotYetImplementedException( "Executing deposits is not yet implemented for " + this.getName());

	} else if( orderType == OrderType.WITHDRAW) {

	    throw new NotYetImplementedException( "Executing withdraws is not yet implemented for " + this.getName());

	}

	return null;  // An error occured, or this is an unknown order type?
    }
    
    /**
     * Get the current funds of the user.
     *
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The accounts with the current balance as a collection of Account objects, or null if the request failed.
     */
    public Collection<TradeSiteAccount> getAccounts( TradeSiteUserAccount userAccount) {

	// Create the url to fetch the balance details.
	String url = this._url + "api/balance/";

	// Try to get some info on the user (including the current funds).
	JSONObject jsonResponse = authenticatedHTTPRequestObject( url, null, userAccount);

	if( jsonResponse != null) {

	    // An array for the parsed funds.
	    ArrayList<TradeSiteAccount> result = new ArrayList<TradeSiteAccount>();

	    // Just add the free for trade funds to the list of accounts.

	    String [] fetchedCurrencies = { "usd" , "btc"};  // List of checked currencies.

	    // Loop over the currencies to add to the list of accounts.
	    for( int currentCurrencyIndex = 0; currentCurrencyIndex < fetchedCurrencies.length; ++currentCurrencyIndex) {
		
		// Get the balance for the current currency from the response.
		// I use only the balance, that is available for trading here, since it's the only balance, a bot could use.
		BigDecimal currentBalance = new BigDecimal( jsonResponse.getString( fetchedCurrencies[ currentCurrencyIndex] + "_available"));

		// Create a new account object and add it to the list of returned accounts.
		result.add( new TradeSiteAccountImpl( currentBalance, CurrencyImpl.findByString( fetchedCurrencies[ currentCurrencyIndex].toUpperCase()), this));
	    }

	    // Get balance also returns the current fee for the user, so update it, too...
	    _fee.updateFee( new BigDecimal( jsonResponse.getString( "fee")));

	    return result;  // Return the list of create accounts for this request.
	}

	return null;  // The account request failed.
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
	    throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on Bitstamp");
	}
	
	// Create the url for the depth request.
	String url = this._url + "api/order_book/";

	// System.out.println( "DEBUG: fetching depth from: " + url);

	// Perform the actual request.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?
	    try {

		// Convert the HTTP request return value to JSON to parse further.
		return new BitstampDepth( JSONObject.fromObject( requestResult), currencyPair, this);

	    } catch( JSONException je) {

		System.err.println( "Cannot parse Bitstamp depth return: " + je.toString());

		throw new TradeDataNotAvailableException( "cannot parse data from Bitstamp");
	    }
	}

	throw new TradeDataNotAvailableException( "Bitstamp server did not respond to depth request");
    }

    /**
     * Get the fee for an order in the resulting currency.
     * Since the fee might depend on the user and many users might use 1 API implementation instance, 
     * synchronize this method.
     *
     * @param order The order to use for the fee computation.
     *
     * @return The fee in the resulting currency (currency value for buy, payment currency value for sell).
     */
    public synchronized Price getFeeForOrder( SiteOrder order) {

	if( order instanceof WithdrawOrder) {
	    
	    // Get the withdrawn currency.
	    Currency withdrawnCurrency = ((WithdrawOrder)order).getCurrency();

	    // Although usd is withdrawn (and converted to euros), 
	    // the fee is in euro...
	    if( withdrawnCurrency.equals( CurrencyImpl.USD)) {

		return new Price( "0.90", CurrencyImpl.EUR);  // Fee is 90 eurocent at the moment.

	    } else if( withdrawnCurrency.equals( CurrencyImpl.BTC)) {

		return new Price( "0.0", CurrencyImpl.BTC);  // BTC withdrawals seem to be free?

	    } else {

		// AFAIK, Bitstamp currently supports only USD and BTC withdrawals...
		throw new CurrencyNotSupportedException( "Withdrawing " 
							 + withdrawnCurrency.getName() 
							 + " is currently not supported in this implementation");
	    }
		
	} else if( order instanceof DepositOrder) {

	    Currency depositedCurrency = ((DepositOrder)order).getCurrency();

	    if( depositedCurrency.equals( CurrencyImpl.BTC)) {

		// BTC deposits are free as far as I know.
		return new Price( "0.0", CurrencyImpl.BTC);

	    } else {

		throw new NotYetImplementedException( "Deposit fees are not implemented for trade site " 
						      + getName() 
						      + " and currency " 
						      + depositedCurrency.getName());
	    }

	} else {  // This is a regular trade.

	    return super.getFeeForOrder( order);
	}
    }

    /**
     * Get the current fee for regular trades.
     *
     * @param userAccount The account of the user on the exchange.
     *
     * @return The current fee for this user and the current volume.
     */
    public BigDecimal getFeeForTrade( TradeSiteUserAccount userAccount) {

	// If the fee is dated or not available at the moment.
	if( _fee.isDated() || ( _fee.getFee() == null)) {

	    // Check, if some user account data are available
	    if( ( userAccount != null) || ( ( _userId != null) && ( _password != null))) {
		
		// Fetch the accounts to get the current fee.
		getAccounts( userAccount);

		if( _fee.getFee() != null) {  // If the fee is available now,
		    
		    return _fee.getFee();     // return it.

		} else {
		    
		    // Log this error.
		    LogUtils.getInstance().getLogger().error( "BitstampClient cannot fetch the current fees.");

		    // Better return null here? At the moment, the default fee is returned...
		}				   
	    }
	} else {  // If there is a current fee in the cache.

	    // Return this fee.
	    return _fee.getFee();
	}

	// Use the default fee of this site.
	BigDecimal defaultFee = new BigDecimal( "0.5");  // Default is 0.5%.
	
	// Log this info, so the user can add his account data.
	LogUtils.getInstance().getLogger().warn( "There are no Bitstamp account data available, so the default trade fee of " 
						 + defaultFee 
						 + "% is used!");

	// Return the default fee...don't know if this is perfect, but better than nothing?
	return defaultFee;
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
     * @return The open orders as a collection, or null if the request failed.
     */
    public Collection<SiteOrder> getOpenOrders( TradeSiteUserAccount userAccount) {

	String url = this._url + "api/open_orders/";

	// Try to get some info on the open orders.
	JSONArray jsonResponse = authenticatedHTTPRequestArray( url, null, userAccount);

	if( jsonResponse != null) {  // If the request succeeded.
	    
	    // Create a buffer for the result.
	    ArrayList<SiteOrder> result = new ArrayList<SiteOrder>();
	    
	    // The answer is an JSON array with the orders as JSON objects.
	    for( int i = 0; i < jsonResponse.size(); ++i) {
		
		// Get the current order from the list.
		JSONObject currentOrder = jsonResponse.getJSONObject( i);
		
		// Get the next site id from the order.
		String currentSiteId = currentOrder.getString( "id");

		// Since we know the tradesite and the site id now, we can query the order book for the order.
		SiteOrder currentSiteOrder = CryptoCoinOrderBook.getInstance().getOrder( this, currentSiteId);

		if( currentOrder != null) {     // If the order book returned an order,
		    result.add( currentSiteOrder);  // add it to the result buffer.
		} else {  // It seems, this order is not in the order book. I can consider this an error at the moment,
		          // since every order should go through the order book.

		    throw new OrderNotInOrderBookException( "Error: Bitstamp order with site id " + currentSiteId + " is not in order book!");
		}
	    }

	    return result;  // Return the buffer with the orders.
	}

	return null;  // An error occured.
    }

    /**
     * Get the section name in the global property file.
     */
    public String getPropertySectionName() {
	return "Bitstamp";
    }

    /**
     * Get the settings of the Bitstamp client.
     *
     * @return The setting of the Bitstamp client as a list.
     */
    public PersistentPropertyList getSettings() {

	// Get the settings from the base class.
	PersistentPropertyList result = super.getSettings();

	result.add( new PersistentProperty( "UserId", null, _userId, 3));
	result.add( new PersistentProperty( "Password", null, _password, 4));

	return result;
    }

    /**
     * Get the current ticker from the Bitstamp API.
     *
     * @param currencyPair The currency pair to query.
     * @param paymentCurrency The currency for the payments.
     *
     * @return The current Bitstamp ticker.
     *
     * @throws TradeDataNotAvailableException if the ticker is not available.
     */
    public BitstampTicker getTicker( CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	if( ! isSupportedCurrencyPair( currencyPair)) {
	    throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on " + this._name);
	}
	
	String url = this._url + "api/ticker/";

	String requestResult = HttpUtils.httpGet( url);
	
	if( requestResult != null) {  // Request sucessful?
	    try {
		// Convert the HTTP request return value to JSON to parse further.
		return new BitstampTicker( JSONObject.fromObject( requestResult), currencyPair, this);
	    } catch( JSONException je) {
		System.err.println( "Cannot parse ticker object: " + je.toString());
	    }
	}
	
	throw new TradeDataNotAvailableException( "The Bitstamp ticker request failed");
    }

    /**
     * Get a list of recent trades.
     *
     * @param since_micros The GMT-relative epoch in microseconds.
     * @param currencyPair The currency pair to query.
     *
     * @return The trades as a list of Trade objects.
     */
    public CryptoCoinTrade [] getTrades( long since_micros, CurrencyPair currencyPair) {

	if( ! isSupportedCurrencyPair( currencyPair)) {
	    throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on Bitstamp");
	}

	// Bitstamp currently supports only 1 currency pair and only timedeltas in seconds.... :-( 
	String url = this._url + "api/transactions/?timedelta=" + ( since_micros / 1000000);

	// System.out.println( "Fetching Bitstamp trades from: " + url);

	CryptoCoinTrade [] tempResult =  getTradesFromURL( url, currencyPair);

	if( tempResult != null) {

	    // Now filter the trades for the timespan.
	    ArrayList<CryptoCoinTrade> resultBuffer = new ArrayList<CryptoCoinTrade>();
	    for( int i = 0; i < tempResult.length; ++i) {
		if( tempResult[i].getTimestamp() > since_micros) {
		    resultBuffer.add( tempResult[i]);
		}
	    }
	
	    // Now convert the buffer back to an array and return it.
	    return resultBuffer.toArray( new CryptoCoinTrade[ resultBuffer.size()]);
	}

	throw new TradeDataNotAvailableException( "trades request on btc-e failed");
    }

    /**
     * Get a list of trades from a URL.
     *
     * @param url The url to fetch the trades from.
     * @param currencyPair The requested currency pair.
     *
     * @return A list of trades or null, if an error occurred.
     */
    private CryptoCoinTrade [] getTradesFromURL( String url, CurrencyPair currencyPair) {
	ArrayList<CryptoCoinTrade> trades = new ArrayList<CryptoCoinTrade>();

        String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // If the HTTP request worked ok.
	    try {
		// Convert the result to an JSON array.
		JSONArray resultArray = JSONArray.fromObject( requestResult);
		
		// Iterate over the array and convert each trade from json to a Trade object.
		for( int i = 0; i < resultArray.size(); i++) {
		    JSONObject tradeObject = resultArray.getJSONObject(i);
		    
		    trades.add( new BitstampTradeImpl( tradeObject, this, currencyPair));  // Add the new Trade object to the list.
		}

		CryptoCoinTrade [] tradeArray = trades.toArray( new CryptoCoinTrade[ trades.size()]);  // Convert the list to an array.
		
		return tradeArray;  // And return the array.

	    } catch( JSONException je) {
		System.err.println( "Cannot parse trade object: " + je.toString());
	    }
	}

	return null;  // An error occured.
    }

    /**
     * Get the interval, in which the trade site updates it's depth, ticker etc. 
     * in microseconds.
     *
     * @return The update interval in microseconds.
     */
    public long getUpdateInterval() {
	return 15L * 1000000L;  // The default Bitstamp update.
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

	return true;  // Just a dummy for now...
    }
}
