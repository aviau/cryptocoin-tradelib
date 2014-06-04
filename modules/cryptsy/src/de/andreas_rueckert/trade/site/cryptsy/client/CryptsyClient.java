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

import de.andreas_rueckert.MissingAccountDataException;
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
import de.andreas_rueckert.trade.order.DepositOrder;
import de.andreas_rueckert.trade.order.OrderStatus;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.order.WithdrawOrder;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteImpl;
import de.andreas_rueckert.trade.site.TradeSiteRequestType;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.trade.Ticker;
import de.andreas_rueckert.trade.TradeDataNotAvailableException;
import de.andreas_rueckert.util.HttpUtils;
import de.andreas_rueckert.util.LogUtils;
import de.andreas_rueckert.util.TimeUtils;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * Main class for the cryptsy API.
 *
 * @see https://www.cryptsy.com/pages/api
 * @see https://github.com/abwaters/cryptsy-api/blob/master/src/com/abwaters/cryptsy/Cryptsy.java
 */
public class CryptsyClient extends TradeSiteImpl implements TradeSite {

    // Static variables

    /**
     * The domain of the service.
     */
    public static String DOMAIN = "cryptsy.com";


    // Instance variables

    /**
     * The default user account, if no other account is given for a authenticated request.
     */
    private TradeSiteUserAccount _defaultUserAccount = null;

    /**
     * A mapping for currency pairs and ID's.
     */
    private Map< CurrencyPair, String> _marketIDs = null;

    /**
     * I'll just use the same code for the nonce as in the btc-e implementation.
     */
    private static long _nonce;

    /**
     * The URL for public API calls.
     */
    private String _public_url;


    // Constructors

    /**
     * Create a new connection to the cryptsy trading site.
     */
    public CryptsyClient() {

	super();

	_name = "Cryptsy";  // The name of the trade site.

	_url = "https://api.cryptsy.com/api";  // The URL for authenticated API calls.

	_public_url = "http://pubapi.cryptsy.com/";  // The URL for public API calls.

	// Create a unixtime nonce for the new API.
	_nonce = ( TimeUtils.getInstance().getCurrentGMTTimeMicros() / 1000000);

	// We would normally set the supported currency pairs here,
	// but to request them from the cryptsy exchange (there are too
	// many to set them manually), we'd need a user account.
	// So we have to delay this setting a bit and do the actual
	// request when the pairs are requested from the user.
    }


    // Methods

    /**
     * Execute a authenticated query on cryptsy.
     *
     * @param method The method to execute.
     * @param arguments The arguments to pass to the server.
     * @param userAccount The user account on the exchange, or null if the default account should be used.
     *
     * @return The returned data as JSON or null, if the request failed.
     */
    private final JSON authenticatedHTTPRequest( String method, Map<String, String> arguments, TradeSiteUserAccount userAccount) {

	HashMap<String, String> headerLines = new HashMap<String, String>();  // Create a new map for the header lines.
	Mac mac;
	SecretKeySpec key = null;
	String accountKey = null;     // The used key of the account.
	String accountSecret = null;  // The used secret of the account.

	// Try to get an account key and secret for the request.
	if( userAccount != null) {

	    accountKey = userAccount.getAPIkey();
	    accountSecret = userAccount.getSecret();

	}  else if( _defaultUserAccount != null) {  // Use the default values from the API implementation.

	    accountKey = _defaultUserAccount.getAPIkey();
	    accountSecret = _defaultUserAccount.getSecret();
	} 

	// Check, if account key and account secret are available for the request.
	if( accountKey == null) {
	    throw new MissingAccountDataException( "Public key not available for authenticated request to " + _name);
	}
	if( accountSecret == null) {
	    throw new MissingAccountDataException( "Private key not available for authenticated request to " + _name);
	}

	if( arguments == null) {  // If the user provided no arguments, just create an empty argument array.
	    arguments = new HashMap<String, String>();
	}
	
	arguments.put( "method", method);  // Add the method to the post data.
	arguments.put( "nonce",  "" + ++_nonce);  // Add the dummy nonce.

	// Convert the arguments into a string to post them.
	String postData = "";

	for( Iterator argumentIterator = arguments.entrySet().iterator(); argumentIterator.hasNext(); ) {
	    Map.Entry argument = (Map.Entry)argumentIterator.next();
	    
	    if( postData.length() > 0) {
		postData += "&";
	    }
	    postData += argument.getKey() + "=" + argument.getValue();
	}

	// Create a new secret key
	try {

	    key = new SecretKeySpec( accountSecret.getBytes( "UTF-8"), "HmacSHA512" ); 

	} catch( UnsupportedEncodingException uee) {

	    System.err.println( "Unsupported encoding exception: " + uee.toString());
	    return null;
	} 

	// Create a new mac
	try {

	    mac = Mac.getInstance( "HmacSHA512" );

	} catch( NoSuchAlgorithmException nsae) {

	    System.err.println( "No such algorithm exception: " + nsae.toString());
	    return null;
	}

	// Init mac with key.
	try {
	    mac.init( key);
	} catch( InvalidKeyException ike) {
	    System.err.println( "Invalid key exception: " + ike.toString());
	    return null;
	}

	// Add the key to the header lines.
	headerLines.put( "Key", accountKey);

	// Encode the post data by the secret and encode the result as base64.
	try {

	    headerLines.put( "Sign", Hex.encodeHexString( mac.doFinal( postData.getBytes( "UTF-8"))));
	} catch( UnsupportedEncodingException uee) {

	    System.err.println( "Unsupported encoding exception: " + uee.toString());
	    return null;
	} 
	
	// Now do the actual request
	String requestResult = HttpUtils.httpPost( _url, headerLines, postData);

	if( requestResult != null) {   // The request worked

	    try {
		// Convert the HTTP request return value to JSON to parse further.
		JSONObject jsonResult = JSONObject.fromObject( requestResult);

		// Check, if the request was successful
		int success = jsonResult.getInt( "success");

		if( success == 0) {  // The request failed.
		    String errorMessage = jsonResult.getString( "error");

		    LogUtils.getInstance().getLogger().error( _name + " trade API request failed: " + errorMessage);

		    return null;

		} else {  // Request succeeded!

		    // Try to figure, what the return actually is: json object or json array?

		    // Test, if the return value is an JSONArray.
		    JSONArray arrayReturn = jsonResult.optJSONArray( "return");

		    if( arrayReturn != null) {  // Converting the result into a JSON array worked, so return it.
			
			return arrayReturn;
		    }

		    // Now test, if the return value is a JSONObject.
		    JSONObject objectReturn = jsonResult.optJSONObject( "return");

		    if( objectReturn != null) {  // Converting the result into a JSON object worked, so return it.

			return objectReturn;
		    }

		    if( ! jsonResult.has( "return")) {  // Has this object no return value?

			LogUtils.getInstance().getLogger().error( _name + " trade API request '" + method + "' has no return value.");

			return null;  // No reasonable return value possible.

		    } else {  // There is a return value, but it's neither an array or a object, so we cannot convert it.

			LogUtils.getInstance().getLogger().error( _name 
								  + " trade API request '" 
								  + method 
								  + "' has a return value, that is neither a JSONObject or a JSONArray. Don't know, what to do with it.");

			return null;  // Not much we can do here...
		    }
		}

	    } catch( JSONException je) {
		System.err.println( "Cannot parse json request result: " + je.toString());

		return null;  // An error occured...
	    }
	} 

	return null;  // The request failed.
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
     *
     * @param order The order to execute.
     *
     * @return The new status of the order.
     */
    public synchronized OrderStatus executeOrder( SiteOrder order) {

	throw new NotYetImplementedException( "Executing an order is not yet implemented for cryptsy");	
    }

    /**
     * Get the current funds of a user.
     *
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used. 
     *
     * @return The accounts with the current balance as a collection of Account objects, or null if the request failed.
     */
    public Collection<TradeSiteAccount> getAccounts( TradeSiteUserAccount userAccount) {

	throw new NotYetImplementedException( "Getting the accounts is not yet implemented for cryptsy");	
    }

    /**
     * Get the market depth as a Depth object.
     *
     * @param currencyPair The queried currency pair.
     *
     * @throws TradeDataNotAvailableException if the depth is not available.
     */
    public Depth getDepth( CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	// If the request for the depth is allowed at the moment
	if( isRequestAllowed( TradeSiteRequestType.Depth)) { 
	    
	    if( ! isSupportedCurrencyPair( currencyPair)) {
		throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on " + this._name);
	    }
	}	    
	    
	// Compute the URL for the orderbook request.
	// I use the public methods for now, since I hope, that they scale better
	// than the authenticated methods.
	String url = _public_url + "api.php?method=singleorderdata&marketid=" + getMarketIdForCurrencyPair( currencyPair);

	String requestResult = HttpUtils.httpGet( url);  // Do the actual request.

	if( requestResult != null) {  // Request sucessful?
	    try {

		// Convert the HTTP request return value to JSON to parse further.
		return new CryptsyDepth( JSONObject.fromObject( requestResult), currencyPair, this);
	
	    } catch( JSONException je) {

		System.err.println( "Cannot parse " + this._name + " depth return: " + je.toString());

		throw new TradeDataNotAvailableException( "cannot parse data from " + this._name);
	    }
	}

	throw new TradeDataNotAvailableException( this._name + " server did not respond to depth request");
    }

    /**
     * Get the depths for all the currency pairs. They are fetched with a single orderbook request,
     * so only 1 request is done to the cryptsy exchange.
     *
     * @return All the depths as an array.
     */
    public Depth [] getDepths() {

	// The URL to fetch all the orderbooks.
	String url = _public_url + "api.php?method=orderdatav2";

	// Do the actual request on vircurex.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?

	    try {

		// Convert the result to a JSON object.
		JSONObject resultJSON = JSONObject.fromObject( requestResult);

		// Check the status value for errors.
		int successStatus = resultJSON.getInt( "success");
		
		if( successStatus != 1) {  // If the request was not successful
		    
		    throw new TradeDataNotAvailableException( _name + " returned an error");
		}
		
		// Get the actual market data.
		JSONObject marketDataJSON = resultJSON.getJSONObject( "return");
		
		// Create a buffer for the results.
		ArrayList< Depth> resultBuffer = new ArrayList< Depth>();
	    
		// Loop over the entries of this object.
		for( JSONObject currentMarketJSON : ( ( Map< String, JSONObject>)marketDataJSON).values()) {

		    try {
			
			// Parse the market data including the currency pair.
			Depth currentDepth = new CryptsyDepth( currentMarketJSON, this);
			
			// Add the depth to the buffer.
			resultBuffer.add( currentDepth);
			
		    } catch( CurrencyNotSupportedException cnse) {
			
			// The depth already logs the error, so just continue here.
			
			continue;  // Contiue with the next market.
		    }
		    
		}

			// Convert the buffer to an array and return it.
		return resultBuffer.toArray( new Depth[ resultBuffer.size()]);

	    } catch( JSONException je) {

		LogUtils.getInstance().getLogger().error( "Cannot parse " + _name + " depths return: " + je.toString());
	    }
	}

	// Since fetching the depths failed, just throw an exception, that the trade data are not available.
	throw new TradeDataNotAvailableException( "Fetching the depths from " + this._name + " returned null");
    }

    /**
     * Get the current market depths sequentially via 1 market call. So the sequence is just 1 call long... :-) 
     * This method is identical to the coins-e method with the same name.
     *
     * @param currencyPairs The currency pairs to query.
     *
     * @return The current market depths for the given currency pairs.
     *
     * @throws TradeDataNotAvailableException if to many depths are not available.
     */
    public Depth [] getDepthsSequentially( CurrencyPair [] currencyPairs) throws TradeDataNotAvailableException {

	// Just get all the depths and then extract the requested depths from there. Since
	// we will usually request many depths, this should be a fastest solution.

	Depth [] fetchedDepths = getDepths();

	// Now we have to resort those depths according to the order of the requested currency pairs.
	// To do so, I convert the depth array to a linked list, and remove any found depth from there,
	// so the list should get shorter with any found depth.
	LinkedList< Depth> fetchedDepthList = new LinkedList< Depth>( Arrays.asList( fetchedDepths));

	// Now create an array for the result, that will be sorted according to the passed currency pair array.
	Depth [] result = new Depth[ currencyPairs.length];
	int currentIndex = 0;

	// Loop over the currecy pairs parameter and add the returned depths to the result array.
	currency_pair_loop:
	for( CurrencyPair currentCurrencyPair : currencyPairs) {

	    // Search this depth in the list of fetched depths.
	    for( int currentListIndex = 0; currentListIndex < fetchedDepthList.size(); ) {

		Depth fetchedDepth = fetchedDepthList.get( currentListIndex);

		if( fetchedDepth == null) {  // If there was no depth returned, remove this entry from the search list.

		    fetchedDepthList.remove( currentListIndex);
		    
		// If there was actually a depth returned and it's for the currency pair , we are
	        // currently processing..
		} else if( fetchedDepth.getCurrencyPair().equals( currentCurrencyPair)) {
		    
		    // Add this depth to the result.
		    result[ currentIndex++] = fetchedDepth;
		    
		    // And remove this depth from the list of searched depths, to make the searching
		    // a bit quicker in the next loop iteration. Currency pairs should never be requested
		    // twice, so this should work.
		    fetchedDepthList.remove( currentListIndex);

		    // Now continue with the next currency pair.
		    continue currency_pair_loop;

		} else {   // Just continue search the list of depth returns.

		    ++currentListIndex;
		}
	    }

	    // If the requested currency pair was not in  the list of returned depths, just set it to null 
	    // in the result for now. ToDo: throw a TradeDataNotAvailableException if more than x depths
	    // are not available?
	    result[ currentIndex++] = null;
	}

	// Return the array with the result.
	return result;
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
	
	if( order instanceof WithdrawOrder) {

	    if( order.getCurrencyPair().getCurrency().equals( CurrencyImpl.BTC)) {
		return new Price( "0.0");  // No clue what cryptsy charges for withdrawals?
	    } else {
		// System.out.println( "Compute withdaw fees for currencies other than btc");

		throw new CurrencyNotSupportedException( "Cannot compute fee for this order: " + order.toString());
	    }
	} else if( order instanceof DepositOrder) {

	    Currency depositedCurrency = ((DepositOrder)order).getCurrency();

	    // Most cryptocoin deposits are free, it seems?
	    if( depositedCurrency.equals( CurrencyImpl.BTC)
		|| depositedCurrency.equals( CurrencyImpl.LTC)) {
		
		return new Price( "0.0", depositedCurrency);
	    
	    } else {
		
		throw new NotYetImplementedException( "Deposit fees are not implemented for trade site " 
						      + getName() 
						      + " and currency " 
						      + depositedCurrency.getName());
	    }
    
	} else if( order.getOrderType() == OrderType.BUY) { // This seems to be a trade order
	    
	    // Since buy and sell have different fees, I split the trade orders here
	    // in 2 conditions.
	    
	    // According to
	    // @see https://cryptsy.freshdesk.com/support/articles/173970-what-are-cryptsy-s
	    // Cryptsy charges 0.2% for buys and 0.3% for sales.

	    return new Price( order.getAmount().multiply( new BigDecimal( "0.002")), order.getCurrencyPair().getCurrency());
		  
	} else if ( order.getOrderType() == OrderType.SELL) {  // Also a trade order.

	    return new Price( order.getAmount().multiply( new BigDecimal( "0.003")), order.getCurrencyPair().getCurrency());
	}

	return null;  // Should never be reached.
    }
 

    /**
     * Get the market ID for a given currency pair.
     *
     * @param currencyPair The traded currency pair.
     *
     * @return The market ID as a string.
     *
     * @throws CurrencyNotSupportedException if there is no ID for this market (= currency pair).
     */
    private final String getMarketIdForCurrencyPair( CurrencyPair currencyPair) throws CurrencyNotSupportedException {

	// Try to get the market ID from the local map.
	String marketID = _marketIDs.get( currencyPair);

	// If this pair is not in the map, throw an exception.
	if( marketID == null) {

	    throw new CurrencyNotSupportedException( "The currency pair " 
						     + currencyPair.toString() 
						     + " is not supported at " 
						     + this._name);
	}

	// If the market ID from the map is not null, return it.
	return marketID;
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
     * Get the section name in the global property file.
     *
     * The name of the property section as a String.
     */
    public String getPropertySectionName() {
	return "Cryptsy";
    }

    /**
     * Get the open orders on this trade site.
     *
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The open orders as a collection, or null if the request failed.
     */
    public Collection<SiteOrder> getOpenOrders( TradeSiteUserAccount userAccount) {

	throw new NotYetImplementedException( "Getting the open orders is not yet implemented for cryptsy");	
    }

    /**
     * Get the settings of the cryptsy client.
     *
     * @return The setting of the cryptsy client as a list.
     */
    public PersistentPropertyList getSettings() {

	// Get the settings from the base class.
	PersistentPropertyList result = super.getSettings();

	// If there is a default user account yet, get the public API key from it (might be null, though).
	result.add( new PersistentProperty( "Public key", null, _defaultUserAccount != null ? _defaultUserAccount.getAPIkey() : null, 6)); 

	// Add the private key (I use the secret field from the account class to store it).
	result.add( new PersistentProperty( "Private key", null, _defaultUserAccount != null ? _defaultUserAccount.getSecret() : null, 5)); 
	
	return result;
    }

    /**
     * Get the supported currency pairs of this trading site.
     *
     * @return The supported currency pairs of this trading site.
     */
    public CurrencyPair [] getSupportedCurrencyPairs() {

	// Check, if the available markets were already requested from the cryptsy exchange.
	if( _marketIDs == null) {

	    // If not, request them from the cryptsy exchange.
	    _marketIDs = requestMarketIDs();

	    // Get a set with the keys (= the currency pairs).
	    Set< CurrencyPair> currencyPairs = _marketIDs.keySet();
	    
	    // Convert the set to an array, so we can access the pairs quicker in future requests.
	    _supportedCurrencyPairs = currencyPairs.toArray( new CurrencyPair[ currencyPairs.size()]);
	}
	
	// Return the array with the currency pairs.
	return _supportedCurrencyPairs;
    }


    /**
     * Get the current ticker from the cryptsy API.
     *
     * @param currencyPair The currency pair to query.
     * @param paymentCurrency The currency for the payments.
     *
     * @return The current cryptsy ticker.
     *
     * @throws TradeDataNotAvailableException if the ticker is not available.
     */
    public Ticker getTicker( CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	throw new NotYetImplementedException( "Getting the ticker is not yet implemented for cryptsy");
    }

    /**
     * Get a list of recent trades.
     *
     * @param since_micros The GMT-relative epoch in microseconds.
     * @param currencyPair The currency pair to query.
     *
     * @return The trades as a list of Trade objects.
     */
    public CryptoCoinTrade [] getTrades( long since_micros, CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	throw new NotYetImplementedException( "Getting the trades is not yet implemented for cryptsy");
    }

    /**
     * Get the interval, in which the trade site updates it's depth, ticker etc. 
     * in microseconds.
     *
     * @return The update interval in microseconds.
     */
    public long getUpdateInterval() {
	return 15L * 1000000L;  // Just a default value for low volume.
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

    /**
     * Request the markets from the website.
     */
    private final Map< CurrencyPair, String> requestMarketIDs() {

	// Create a new buffer for the result.
	Map< CurrencyPair, String> resultBuffer = new HashMap< CurrencyPair, String>();

	// Request the markets from the website. 
	// The result should automagically be converted to a JSONArray.
	// ToDo: use the account from the actual current user?
	JSONArray JSONresult = (JSONArray)authenticatedHTTPRequest( "getmarkets", null, null);

	// Iterate over the array and convert each trade from json to a Trade object.
	for( int i = 0; i < JSONresult.size(); i++) {

	    // Get the current market as a JSON object.
	    JSONObject currentMarket = JSONresult.getJSONObject(i);

	    // Get the market ID for this market.
	    String marketID = "" + currentMarket.getInt( "marketid");

	    // Get the currency of this market.
	    Currency currency = CurrencyImpl.findByString( currentMarket.getString( "primary_currency_code").toUpperCase());

	    // Get the payment currency of this market.
	    Currency paymentCurrency = CurrencyImpl.findByString( currentMarket.getString( "secondary_currency_code").toUpperCase());

	    // Add the currency pair with the ID to the result buffer.
	    resultBuffer.put( new CurrencyPairImpl( currency, paymentCurrency), marketID);
	}

	return resultBuffer;  // Return the buffer with the result.
    }

    /**
     * Set the default user account for authenticated requests, if no user account is given.
     *
     * @param defaultUserAccount The new default user account.
     */
    public void setDefaultUserAccount( TradeSiteUserAccount defaultUserAccount) {

	// Store the user account in the instance.
	_defaultUserAccount = defaultUserAccount;
    }

    /**
     * Set new settings for the cryptsy client.
     *
     * @param settings The new settings for the cryptsy client.
     */
    public void setSettings( PersistentPropertyList settings) {
	
	super.setSettings( settings);
	
	String key = settings.getStringProperty( "Public key");
	if( key != null) {

	    // Get the public API key from the settings and store it in the default user account.
	    
	    if( _defaultUserAccount == null) {  // If there is no default user account yet,

		_defaultUserAccount = new TradeSiteUserAccount();  // create one.
	    }

	    // Store the public API key in the settings.
	    _defaultUserAccount.setAPIkey( key);
	}

	String secret = settings.getStringProperty( "Private key");
	if( secret != null) {

	    // Get the private API key from the settings and store it in the default user account.
	    
	    if( _defaultUserAccount == null) {  // If there is no default user account yet,

		_defaultUserAccount = new TradeSiteUserAccount();  // create one.
	    }

	    // Store the public API key in the settings.
	    _defaultUserAccount.setSecret( secret);
	}
    }
}
