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

package de.andreas_rueckert.trade.site.anx.client;

import de.andreas_rueckert.MissingAccountDataException;
import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.account.TradeSiteAccountImpl;
import de.andreas_rueckert.trade.CryptoCoinTrade;
import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.currency.CurrencyImpl;
import de.andreas_rueckert.trade.currency.CurrencyNotSupportedException;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.currency.CurrencyPairImpl;
import de.andreas_rueckert.trade.currency.CurrencyProvider;
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
import de.andreas_rueckert.trade.Ticker;
import de.andreas_rueckert.trade.Trade;
import de.andreas_rueckert.trade.TradeDataNotAvailableException;
import de.andreas_rueckert.util.HttpUtils;
import de.andreas_rueckert.util.LogUtils;
import de.andreas_rueckert.util.TimeUtils;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
 * Main class for the ANX API.
 *
 * @see http://docs.anxv2.apiary.io/
 */
public class ANXClient extends TradeSiteImpl implements TradeSite {

    // Inner classes


    // Static variables

    /**
     * This flag is mainly for debugging and deactivates the actual trading.
     */
    private final static boolean SIMULATION = true;


    // Instance variables

    /**
     * A mapping from the ANX currency pair code to a CurrencyPair object.
     */
    private Map<String, CurrencyPair> _currencyPairCodeMap = null;


    // Constructors
    
    /**
     * Create a new ANX client instance.
     */
    public ANXClient() {

	_name = "ANX";  // Set the name of this exchange.

	_url = "https://anxpro.com/api/2/";  // Base URL for API calls.

	// Set the supported currency pairs manualls, since it seems there's no API method to fetch them?
	List<CurrencyPair> supportedCurrencyPairs = new ArrayList<CurrencyPair>();
	supportedCurrencyPairs.add( new CurrencyPairImpl( "BTC", "USD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "BTC", "HKD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "BTC", "EUR"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "BTC", "CAD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "BTC", "AUD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "BTC", "SGD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "BTC", "JPY"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "BTC", "CHF"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "BTC", "GBP"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "BTC", "NZD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "LTC", "BTC")); 
	supportedCurrencyPairs.add( new CurrencyPairImpl( "LTC", "USD")); 
	supportedCurrencyPairs.add( new CurrencyPairImpl( "LTC", "HKD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "LTC", "EUR"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "LTC", "CAD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "LTC", "AUD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "LTC", "SGD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "LTC", "JPY"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "LTC", "CHF"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "LTC", "GBP"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "LTC", "NZD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "PPC", "BTC")); 
	supportedCurrencyPairs.add( new CurrencyPairImpl( "PPC", "LTC"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "PPC", "USD")); 
	supportedCurrencyPairs.add( new CurrencyPairImpl( "PPC", "HKD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "PPC", "EUR"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "PPC", "CAD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "PPC", "AUD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "PPC", "SGD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "PPC", "JPY"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "PPC", "CHF"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "PPC", "GBP"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "PPC", "NZD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "NMC", "BTC")); 
	supportedCurrencyPairs.add( new CurrencyPairImpl( "NMC", "LTC"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "NMC", "USD")); 
	supportedCurrencyPairs.add( new CurrencyPairImpl( "NMC", "HKD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "NMC", "EUR"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "NMC", "CAD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "NMC", "AUD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "NMC", "SGD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "NMC", "JPY"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "NMC", "CHF"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "NMC", "GBP"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "NMC", "NZD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "DOGE", "BTC")); 
	supportedCurrencyPairs.add( new CurrencyPairImpl( "DOGE", "USD")); 
	supportedCurrencyPairs.add( new CurrencyPairImpl( "DOGE", "HKD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "DOGE", "EUR"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "DOGE", "CAD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "DOGE", "AUD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "DOGE", "SGD"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "DOGE", "JPY"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "DOGE", "CHF"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "DOGE", "GBP"));
	supportedCurrencyPairs.add( new CurrencyPairImpl( "DOGE", "NZD"));

	// Copy the list to the instance array (defined in super class).
	_supportedCurrencyPairs = supportedCurrencyPairs.toArray( new CurrencyPair[ supportedCurrencyPairs.size()]);
    }


    // Methods

    /**
     * Perform a query with user authentication and return the HTTP post reply as a string.
     *
     * @param requestUrl The URL to query.
     * @param parameters Additional parameters for the request.
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The request result as a JSONObject, if the JSON result object has a success value. null otherwise
     */
    private final JSONObject authenticatedQuery( String requestUrl, Map< String, String> parameters, TradeSiteUserAccount userAccount) {

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

	// Query the exchange server and return the HTTP result as a string.
	String requestResult = HttpUtils.httpPost( requestUrl, getAuthenticationHeader( postData, userAccount), postData);

	try {
	    // Convert the HTTP request return value to JSON to parse further.
	    JSONObject jsonResult = JSONObject.fromObject( requestResult);

	    // Get the result value to check for an error
	    if( "error".equals( jsonResult.getString( "result"))) {

		// Output the error to the error stream.
		System.err.println( _name + " query error: " + jsonResult.getString( "error"));

		return null;  // Query did not success, so just return null.
	    }
	    
	    // Return the entire json result object.
	    return jsonResult;

	} catch( JSONException je) {
	    System.err.println( "Cannot parse json request result: " + je.toString());

	    return null;  // An error occured...
	}
    }

    /**
     * Cancel an order on the trade site.
     *
     * @param order The order to cancel.
     *
     * @return true, if the order was canceled. False otherwise.
     */
    public boolean cancelOrder( SiteOrder order) {

	// Create the URL for the request.
	String url = _url + getANXCurrencyPairString( order.getCurrencyPair()) + "/money/order/cancel";

	// The parameters for the HTTP post call.
	HashMap<String, String> parameter = new HashMap<String, String>();

	// Get the site id of this order.
	String site_id =  order.getSiteId();

	// If there is no site id, we cannot cancel the order.
	if( site_id == null) {

	    return false;

	}

	parameter.put( "oid", order.getSiteId());  // Pass the site id of the order.

	// Do the actual request.
	JSONObject jsonResponse = authenticatedQuery( url, parameter, order.getTradeSiteUserAccount());

	if( jsonResponse == null) {

	    LogUtils.getInstance().getLogger().error( "No response from " + getName() + " while attempting to cancel an order");

	    return false;

	} else {  // Check, if the tradesite signals success.
	    
	    String tradeSiteResult = jsonResponse.getString( "result");

	    if( tradeSiteResult.equalsIgnoreCase( "success")) {  // If the tradesite signals success..

		return true;  // canceling worked!

	    } else if( tradeSiteResult.equalsIgnoreCase( "error"))  {

		LogUtils.getInstance().getLogger().error( "Tradesite " 
							  + getName() 
							  + " signaled error while trying to cancel an order: "
							  + jsonResponse.getString( "error"));

		return false;

	    } else {  // This is an unknown condition. Should never be reached, if this implementation is complete and the API did not change.

		LogUtils.getInstance().getLogger().error( "Tradesite " 
							  + getName() 
							  + " signaled an unknown condition while trying to cancel an order.");
		
		return false;
	    }
	}
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

	OrderType orderType = order.getOrderType();  // Get the type of this order.

	if( ( orderType == OrderType.BUY) || ( orderType == OrderType.SELL)) {  // If this is a buy or sell order, run the trade code.

	    // Get the traded currency pair.
	    CurrencyPair tradedPair = order.getCurrencyPair();

	    if( ! isSupportedCurrencyPair( tradedPair)) {

		throw new CurrencyNotSupportedException( _name + " does not support this currency pair: " + tradedPair.toString());
	    }

	    // Create the URL for the request.
	    String url = _url + getANXCurrencyPairString( tradedPair) + "/money/order/add";
		
	    // The parameters for the HTTP post call.
	    HashMap<String, String> parameters = new HashMap<String, String>();
	    
	    parameters.put( "type", order.getOrderType() == OrderType.BUY ? "bid" : "ask");  // Indicate buy or sell.
	    
	    // The amount seems to be formatted with 10^8 with the current pairs:
	    // http://docs.anxv2.apiary.io/#Multiplier
	    parameters.put( "amount_int", "" + order.getAmount().multiply( new BigDecimal( "100000000")).longValue());
	    
	    // The price is multiplied with 10000 if the currency is BTC or LTC. 100000000 otherwise.
	    BigDecimal multiplier = tradedPair.getCurrency().hasCode( new String [] { "BTC", "LTC"}) 
		? new BigDecimal( "10000") 
		: new BigDecimal( "100000000");

	    parameters.put( "price_int", "" + order.getPrice().multiply( multiplier).longValue());
	    
	    // If this is a simulation, don't execute the query
	    if( SIMULATION) {

		System.out.println( "Simulation mode for " + _name + ".\nWould make request to " + url + " with parameters:");
		for( Map.Entry parameterEntry : parameters.entrySet()) {

		    System.out.println( "Parameter " + parameterEntry.getKey() + " has value " + parameterEntry.getValue());
		}
		
		return OrderStatus.UNKNOWN;  // What else can we say?
	    }

	    // Query the server and fetch the result as a json object.
	    JSONObject jsonResult = authenticatedQuery( url, parameters, order.getTradeSiteUserAccount());
	    
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
	}

	// The other order types are not yet implemented.
	throw new NotYetImplementedException( "Executing this order type is not yet implemented for " + _name);
    }

    /**
     * Get the current funds of the user via the API.
     *
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The accounts with the current balance as a collection of Account objects, or null if the request failed.
     */
    public Collection<TradeSiteAccount> getAccounts( TradeSiteUserAccount userAccount) {

	// Create the URL for the request.
	String url = _url + "money/info";

	// Do the actual request.
	JSONObject jsonResponse = authenticatedQuery( url, null, userAccount);

	if( jsonResponse == null) {

	    LogUtils.getInstance().getLogger().error( "No response from " + getName() + " while attempting to get the funds");
 
	    return null;

	} else {

	    String tradeSiteResult = jsonResponse.getString( "result");

	    if( tradeSiteResult.equalsIgnoreCase( "success")) {  // If the tradesite signals success..

		// Get the wallets as a JSON object.
		JSONObject jsonWallet = jsonResponse.getJSONObject( "data").getJSONObject( "Wallets");

		// A buffer for the parsed funds.
		List<TradeSiteAccount> result = new ArrayList<TradeSiteAccount>();

		// Now iterate over all the currencies in the funds.
		for( Iterator currencyIterator = jsonWallet.keys(); currencyIterator.hasNext(); ) {

		    String currentCurrency = (String)currencyIterator.next();  // Get the next currency.
		    
		    // Get the balance for this currency.
		    BigDecimal balance = new BigDecimal( jsonWallet.getJSONObject( currentCurrency).getJSONObject( "Balance").getString( "value"));

		    // Create an account and add it to the result.
		    result.add( new TradeSiteAccountImpl( balance, CurrencyProvider.getInstance().getCurrencyForCode( currentCurrency.toUpperCase()), this));
		}
		
		return result; // Return the list with the accounts.

	    } else if( tradeSiteResult.equalsIgnoreCase( "error"))  {
		
		LogUtils.getInstance().getLogger().error( "Tradesite " 
							  + getName() 
							  + " signaled error while trying to get the funds: "
							  + jsonResponse.getString( "error"));
		
		return null;
	    }
	}

	return null;  // Should never be reached.
    }

    /**
     * Get the ANX code for a currency pair.
     *
     * @param currencyPair The currency pair.
     *
     * @return The ANX code for the currency pair as a string.
     */
    private final String getANXCurrencyPairString( CurrencyPair currencyPair) {

	return currencyPair.getCurrency().getCode() + currencyPair.getPaymentCurrency().getCode();
    }

    /**
     * Create authentication entries for a HTTP post header.
     *
     * @param postData The data to post via HTTP.
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The header entries as a map or null if an error occured.
     */
    Map< String, String> getAuthenticationHeader( String postData, TradeSiteUserAccount userAccount) {

	HashMap<String, String> result = new HashMap<String, String>();
	Mac mac;
	String accountKey = null;
	String accountSecret = null;

	// Try to get user account and secret.
	if( userAccount != null) {

	    accountKey = userAccount.getAPIkey();
	    accountSecret = userAccount.getSecret();

	} else {  // Throw an error.

	    throw new MissingAccountDataException( "No user account given for " + _name + " request");
	}

	// Check, if key and secret are available for the request.
	if( accountKey == null) {
	    throw new MissingAccountDataException( "Key not available for authenticated request to " + _name);
	}
	if( accountSecret == null) {
	    throw new MissingAccountDataException( "Secret not available for authenticated request to " + _name);
	}

	result.put( "Rest-Key", accountKey);

	// Create a new secret key
	SecretKeySpec key = new SecretKeySpec( Base64.decodeBase64( accountSecret), "HmacSHA512" );       

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
     * Get the currency pair object for a given currency pair code.
     *
     * @param currencyPairCode The ANX code for a currency pair (i.e. DOGEUSD).
     *
     * @return The currency pair as a CurrencyPair object or null of no such pair was found.
     */
    private final CurrencyPair getCurrencyPairForANXCode( String currencyPairCode) {

	// If there is no currency pair mapping yet, create one.
	if( _currencyPairCodeMap == null) {
	    
	    // Create a new hash map for the mapping.
	    _currencyPairCodeMap = new HashMap< String, CurrencyPair>();

	    // Loop over the currency pairs 
	    for( CurrencyPair currentCurrencyPair : getSupportedCurrencyPairs()) {

		// Generate the ANX code for the pair as the key and add the pair to the mapping.
		_currencyPairCodeMap.put( getANXCurrencyPairString( currentCurrencyPair), currentCurrencyPair);
	    }
	}

	// Fetch the pair for the given code from the map.
	return _currencyPairCodeMap.get( currencyPairCode);
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

	// Create the URL for the depth request.
	String url = _url + getANXCurrencyPairString( currencyPair) + "/money/depth/full";

	// Do the actual request.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?
	    
	    try {
		
		// Convert the result to JSON.
		JSONObject requestResultJSON = (JSONObject)JSONObject.fromObject( requestResult);
		
		// Check the result field for success.
		String resultField = requestResultJSON.getString( "result");

		if( resultField.equalsIgnoreCase( "success")) {  // Check, if the request worked ok.

		    // Get the data and convert them to a depth object.
		    return new ANXDepth( requestResultJSON.getJSONObject( "data"), currencyPair, this);
		    
		} else {

		    // Get the error message.
		    String errorField = requestResultJSON.getString( "error");

		    // Write the first error to the log for now. Hopefully it should explain the problem.
		    LogUtils.getInstance().getLogger().error( "Error while fetching the depth from " 
							      + _name 
							      + ". Error message is: " + requestResultJSON.getString( "error"));

		    throw new TradeDataNotAvailableException( "Error while fetching the depth from " + this._name);
		}

	    } catch( JSONException je) {

		LogUtils.getInstance().getLogger().error( "Cannot parse " + this._name + " depth return: " + je.toString());

		throw new TradeDataNotAvailableException( "cannot parse depth data from " + this._name);
	    }

	} 

	// The server return was null...
	throw new TradeDataNotAvailableException( this._name + " server did not respond to depth request");
    }

    /**
     * Get the current market depths (minimal data of the orders) for a given list of currency pairs.
     *
     * @param currencyPairs The currency pairs to query.
     *
     * @return The current market depths for the given currency pairs.
     *
     * @throws TradeDataNotAvailableException if to many depths are not available.
     */
    public List<Depth> getDepths( CurrencyPair [] currencyPairs) throws TradeDataNotAvailableException {

	// ANX has an optional argument for extra currency pairs, that could be used to fetch
	// many depths at once.
	// @see: http://docs.anxv2.apiary.io/#get-%2Fapi%2F2%2F{currency_pair}%2Fmoney%2Fdepth%2Ffull

	// Create the URL for the depth request.
	String url = _url + getANXCurrencyPairString( currencyPairs[0]) + "/money/depth/full?extraCcyPairs=";

	// Append the extra currency pairs to the URL.
	for( int currentPairIndex = 1; currentPairIndex < currencyPairs.length; ++currentPairIndex) {

	    if( currentPairIndex > 1) {  // Separate the pairs with colons.
		url += ",";
	    }

	    // Append the pair to the URL.
	    url += getANXCurrencyPairString( currencyPairs[ currentPairIndex]);
	}

	// Do the actual request.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?
	    
	    try {
		
		// Convert the result to JSON.
		JSONObject requestResultJSON = (JSONObject)JSONObject.fromObject( requestResult);
		
		// Check the result field for success.
		String resultField = requestResultJSON.getString( "result");

		if( resultField.equalsIgnoreCase( "success")) {  // Check, if the request worked ok.

		    // This is an JSON object, but represents an array with the currency pair data.
		    JSONObject currencyPairData = requestResultJSON.getJSONObject( "data");

		    // Create a buffer for the resulting depth array.
		    List<Depth> resultBuffer = new ArrayList<Depth>();
		    
		    // Loop over the currency pairs.
		    for( Iterator<String> keys = currencyPairData.keys(); keys.hasNext(); ) {

			// Get the ANX code for the next currency pair.
			String currentPairCode = (String)keys.next();

			// Try to get the currency pair for this code.
			CurrencyPair currentPair = getCurrencyPairForANXCode( currentPairCode);

			if( currentPair == null) {  // If there is no pair, this code is unknown.

			    throw new CurrencyNotSupportedException( "Cannot find a currency pair for the ANX code: " + currentPairCode);

			} else {
			    
			    // Convert the data of this pair to a Depth object and add it to the result buffer.
			    resultBuffer.add( new ANXDepth( currencyPairData.getJSONObject( currentPairCode), currentPair, this));
			}
		    }

		    // Return the result buffer.
		    return resultBuffer;

		} else {
		    
		    // Get the error message.
		    String errorField = requestResultJSON.getString( "error");
		    
		    // Write the first error to the log for now. Hopefully it should explain the problem.
		    LogUtils.getInstance().getLogger().error( "Error while fetching the depth from " 
							      + _name 
							      + ". Error message is: " + requestResultJSON.getString( "error"));
		    
		    throw new TradeDataNotAvailableException( "Error while fetching the depth from " + this._name);
		}
	    } catch( JSONException je) {

		LogUtils.getInstance().getLogger().error( "Cannot parse " + this._name + " depth return: " + je.toString());

		throw new TradeDataNotAvailableException( "cannot parse depth data from " + this._name);
	    }
	}

	// The server return was null...
	throw new TradeDataNotAvailableException( this._name + " server did not respond to depth request");
    }

    /**
     * Get the fee for an order.
     * Synchronize this method, since several users might use this method with different
     * accounts and therefore different fees via a single API implementation instance.
     *
     * @param order The order to use for the fee computation.
     *
     * @return The fee in the resulting currency (currency value for buy, payment currency value for sell).
     *
     * @see https://www.bitfinex.com/pages/fees
     */
    public synchronized Price getFeeForOrder( SiteOrder order) {

	if( order instanceof WithdrawOrder) {   // If this is a withdraw order

	    // Get the withdrawn currency.
	    Currency withdrawnCurrency = order.getCurrencyPair().getCurrency();

	    if( withdrawnCurrency.hasCode( new String [] { "BTC", "LTC", "NMC", "PPC", "DOGE"})) {

		// Withdrawing cryptocoins is free.
		return new Price( "0", withdrawnCurrency);

	    } else {

		// Since I cannot check, if FIAT is send via wire or SEPA, I just leave it away for now.
		throw new CurrencyNotSupportedException( "The fees for FIAT withdraw are currently not implemented for " + _name);
	    }

	} else if( order instanceof DepositOrder) {   // If this is a deposit order

	     // Get the deposited currency.
	    Currency depositedCurrency = order.getCurrencyPair().getCurrency();

	    if( depositedCurrency.hasCode( new String [] { "BTC", "LTC", "NMC", "PPC", "DOGE" })) {

		// Depositing cryptocoins is free.
		return new Price( "0", depositedCurrency);

	    } else {

		// Since I cannot check, if FIAT is send via wire or SEPA, I just leave it away for now.
		throw new CurrencyNotSupportedException( "The fees for FIAT deposit are currently not implemented for " + _name);
	    }

	} else if( order.getOrderType() == OrderType.BUY) {  // Is this a buy trade order?

	    // @see: https://anxpro.com/faq#tab1
	    // It says 0.05% fee?
	    return new Price( order.getAmount().multiply( new BigDecimal( "0.0005")), order.getCurrencyPair().getCurrency());
	    
	} else if( order.getOrderType() == OrderType.SELL) {  // This is a sell trade order

	    // A sell order has the payment currency as the target currency.
	    return new Price( order.getAmount().multiply( order.getPrice()).multiply( new BigDecimal( "0.0005"))
			      , order.getCurrencyPair().getPaymentCurrency());
	    
	} else {  // This is an unknown order type?

	    return null;  // Should never happen...
	}
    }

    /**
     * Get the shortest allowed requet interval in microseconds.
     *
     * @return The shortest allowed request interval in microseconds.
     */
    public long getMinimumRequestInterval() {

	return 15L * 1000000L;  // Just a default of 15s for now.
    }

    /**
     * Get the open orders on this trade site.
     *
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The open orders as a collection, or null if the request failed.
     */
    public Collection<SiteOrder> getOpenOrders( TradeSiteUserAccount userAccount) {

	// Create a URL to request the open orders.
	// Note 1: no matter what the currency pair is, orders from _all_ pairs will be returned! 
	// ( @see http://docs.anxv2.apiary.io/#post-%2Fapi%2F2%2F{currency_pair}%2Fmoney%2Forders )
	// So I just use the first supported currency pair as a dummy for now.
	JSONObject jsonResult = authenticatedQuery( _url + _supportedCurrencyPairs[0] + "/money/orders", null, userAccount);

	if( jsonResult != null) {

	    JSONArray requestReturn = jsonResult.getJSONArray( "return");  // Get the return value as an array

	    // Create a buffer for the result.
	    List<SiteOrder> result = new ArrayList<SiteOrder>();

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

		    throw new OrderNotInOrderBookException( "Error: " + _name + " order with site id " + currentSiteId + " is not in order book!");
		}
	    }
	    
	    return result;  // Return the buffer with the orders.
	}

	return null;  // The query failed.
    }

    /**
     * Get the section name in the global property file.
     *
     * @return The name of the property section as a String.
     */
    public String getPropertySectionName() {

	return _name;
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

	// Create the URL to fetch the ticker
	String url = _url + getANXCurrencyPairString( currencyPair) + "/money/ticker";
	    
	// Do the actual request.
	String requestResult = HttpUtils.httpGet( url);
	
	if( requestResult != null) {  // Request sucessful?

	    try {
		
		// Convert the result to JSON.
		JSONObject requestResultJSON = (JSONObject)JSONObject.fromObject( requestResult);

		// Check the result field for success.
		String resultField = requestResultJSON.getString( "result");

		if( resultField.equalsIgnoreCase( "success")) {  // Check, if the request worked ok.
		    
		    // Get the data and convert them to a depth object.
		    return new ANXTicker( requestResultJSON.getJSONObject( "data"), currencyPair, this);

		} else {

		     // Get the error message.
		    String errorField = requestResultJSON.getString( "error");

		    // Write the error to the log. Hopefully it should explain the problem.
		    LogUtils.getInstance().getLogger().error( "Error while fetching the depth from " 
							      + _name 
							      + ". Error message is: " + requestResultJSON.getString( "error"));

		    throw new TradeDataNotAvailableException( "Error while fetching the ticker from " + this._name);
		}

	    } catch( JSONException je) {
		
		LogUtils.getInstance().getLogger().error( "Cannot parse " + this._name + " ticker return: " + je.toString());

		throw new TradeDataNotAvailableException( "cannot parse depth data from " + this._name);
	    }
	}
		    
	// The server return was null...
	throw new TradeDataNotAvailableException( this._name + " server did not respond to ticker request");
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
    public List<Trade> getTrades( long since_micros, CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	if( ! isSupportedCurrencyPair( currencyPair)) {
	    throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on " + _name);
	}

	// Create the URL to fetch the trades.
	String url = _url 
	    + getANXCurrencyPairString( currencyPair) 
	    + "/money/trade/fetch?since=" 
	    + (since_micros / 1000);

	// Do the actual request.
	String requestResult = HttpUtils.httpGet( url);
	
	if( requestResult != null) {  // Request sucessful?

	    try {

		// Convert the HTTP request return value to JSON to parse further.
		JSONObject jsonResult = JSONObject.fromObject( requestResult);

		// Create a buffer for the result.
		List<Trade> trades = new ArrayList<Trade>();

		// Get the result value to check for success
		if( ! "success".equals( jsonResult.getString( "result"))) {

		    // Write the error to the log. Hopefully it should explain the problem.
		    LogUtils.getInstance().getLogger().error( "Error while fetching the trades from " 
							      + _name 
							      + ". Error message is: " + jsonResult.getString( "error"));

		    throw new TradeDataNotAvailableException( this._name + " reported an error while fetching the trades.");
		}

		// Convert the trade data to a JSON array.
		JSONArray jsonData = jsonResult.getJSONArray( "data");

		// Iterate over the json array and convert each trade from json to a Trade object.
		for( int i = 0; i < jsonData.size(); ++i) {

		    JSONObject tradeObject = jsonData.getJSONObject(i);
		    
		    try {
			
			trades.add( new ANXTrade( tradeObject, this, currencyPair));  // Add the new Trade object to the list.
			
		    } catch( JSONException je) {  // Cannot parse the JSON trade.

			// Write the error to the log. Hopefully it should explain the problem.
			LogUtils.getInstance().getLogger().error( "Error while parsing a trades from " 
								  + _name 
								  + ". Error message is: " + je.toString());

			throw new TradeDataNotAvailableException( this._name + " reported an error while parsing a trade.");
		    }
		}

		// updateLastRequest( TradeSiteRequestType.Trades);  // Update the timestamp of the last request.

		return trades;  // And return the list of trades.

	    } catch( JSONException je) {

		    System.err.println( "Cannot parse trade object: " + je.toString());
	    }
	}

	// The server return was null...
	throw new TradeDataNotAvailableException( this._name + " server did not respond to trades request");
    }

    /**
     * Get the interval, in which the trade site updates it's depth, ticker etc. 
     * in microseconds.
     *
     * @return The update interval in microseconds.
     */
    public final long getUpdateInterval() {

	return 15L * 1000000L;  // 15s should work for most exchanges as a default.
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
}
