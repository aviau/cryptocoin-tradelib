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

package de.andreas_rueckert.trade.site.vircurex.client;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.persistence.PersistentProperty;
import de.andreas_rueckert.persistence.PersistentPropertyList;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.CryptoCoinTrade;
import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.currency.CurrencyImpl;
import de.andreas_rueckert.trade.currency.CurrencyNotSupportedException;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.currency.CurrencyPairImpl;
import de.andreas_rueckert.trade.currency.CurrencyProvider;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.DepositOrder;
import de.andreas_rueckert.trade.order.OrderStatus;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.order.WithdrawOrder;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.request.ProxyRequestHandler;
import de.andreas_rueckert.trade.site.request.ProxyRequestResult;
import de.andreas_rueckert.trade.site.request.ProxyRequestResultType;
import de.andreas_rueckert.trade.site.request.RatedProxy;
import de.andreas_rueckert.trade.site.request.TradeSiteProxyInfo;
import de.andreas_rueckert.trade.site.TradeDataRequestNotAllowedException;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteImpl;
import de.andreas_rueckert.trade.site.TradeSiteRequestType;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.trade.Ticker;
import de.andreas_rueckert.trade.Trade;
import de.andreas_rueckert.trade.TradeDataNotAvailableException;
import de.andreas_rueckert.util.HttpUtils;
import de.andreas_rueckert.util.HttpUtilsProxy;
import de.andreas_rueckert.util.LogUtils;
import de.andreas_rueckert.util.TimeUtils;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * Main class for the Vircurex API client.
 *
 * @see https://vircurex.com/welcome/api
 */
public class VircurexClient extends TradeSiteImpl implements TradeSite {

    // Inner classes


    // Static variables

    /**
     * The domain of the trading site.
     */
    final static String DOMAIN = "api.vircurex.com";


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

    /**
     * A map to store the withdrawal fees for each coin type as percent.
     */
    Map< Currency, BigDecimal> _withdrawal_fees = new HashMap< Currency, BigDecimal>();


    // Constructors

    /**
     * Create a new connection to the Vircurex trading site.
     */
    public VircurexClient() {
	
	super();

	_name = "Vircurex";
	_url = "https://api.vircurex.com/";

	// Define the supported currency pairs for this trading site.
	// This list is actually not complete, since Vircurex supports lots of pairs, but
	// it should be sufficient for most apps, I guess.
	_supportedCurrencyPairs = requestSupportedCurrencyPairs();

	// Set the known withdrawal fees.
	// Available at: https://vircurex.com/welcome/help?locale=en
	_withdrawal_fees.put( CurrencyProvider.getInstance().getCurrencyForCode( "ANC"), new BigDecimal( "0.1"));
	_withdrawal_fees.put( CurrencyProvider.getInstance().getCurrencyForCode( "BTC"), new BigDecimal( "0.002"));
	_withdrawal_fees.put( CurrencyProvider.getInstance().getCurrencyForCode( "DGC"), new BigDecimal( "0.2"));
	_withdrawal_fees.put( CurrencyProvider.getInstance().getCurrencyForCode( "DOGE"), new BigDecimal( "5.0"));
	_withdrawal_fees.put( CurrencyProvider.getInstance().getCurrencyForCode( "DVC"), new BigDecimal( "100.0"));
	_withdrawal_fees.put( CurrencyProvider.getInstance().getCurrencyForCode( "FRC"), new BigDecimal( "10.0"));
	_withdrawal_fees.put( CurrencyProvider.getInstance().getCurrencyForCode( "FTC"), new BigDecimal( "0.01"));
	_withdrawal_fees.put( CurrencyProvider.getInstance().getCurrencyForCode( "I0C"), new BigDecimal( "0.01"));
	_withdrawal_fees.put( CurrencyProvider.getInstance().getCurrencyForCode( "IXC"), new BigDecimal( "8.0"));
	_withdrawal_fees.put( CurrencyProvider.getInstance().getCurrencyForCode( "LTC"), new BigDecimal( "0.1"));
	_withdrawal_fees.put( CurrencyProvider.getInstance().getCurrencyForCode( "NMC"), new BigDecimal( "0.1"));
	_withdrawal_fees.put( CurrencyProvider.getInstance().getCurrencyForCode( "NVC"), new BigDecimal( "0.1"));
	_withdrawal_fees.put( CurrencyProvider.getInstance().getCurrencyForCode( "PPC"), new BigDecimal( "0.002"));
	_withdrawal_fees.put( CurrencyProvider.getInstance().getCurrencyForCode( "QRK"), new BigDecimal( "0.5"));
	_withdrawal_fees.put( CurrencyProvider.getInstance().getCurrencyForCode( "TRC"), new BigDecimal( "0.01"));
	_withdrawal_fees.put( CurrencyProvider.getInstance().getCurrencyForCode( "WDC"), new BigDecimal( "0.1"));
	_withdrawal_fees.put( CurrencyProvider.getInstance().getCurrencyForCode( "XPM"), new BigDecimal( "0.01"));

	// Set some proxy info for faster requests.
	//_proxyInfo = new TradeSiteProxyInfo( this, true, 10);
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
		+ currencyPair.getPaymentCurrency().getCode() + "&base=" + currencyPair.getCurrency().getCode();
	    
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
     * Get the depths of all pairs with a given payment currency.
     *
     * @param paymentCurrency The currency to use for the payment.
     *
     * @return The depths of the pairs as an array.
     *
     * @throws TradeDataNotAvailableException if many of the depths are not available. A few missing depths are just set to null.
     */
    public List<Depth> getDepthsForPaymentCurrency( Currency paymentCurrency) throws TradeDataNotAvailableException {

	// Compute the url for the request.
	String url = "https://" + DOMAIN + "/api/orderbook_alt.json?alt=" + paymentCurrency.getCode();
	
	// Do the actual request on vircurex.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?

	    // Convert the result to a JSON object.
	    JSONObject resultJSON = JSONObject.fromObject( requestResult);

	    // Check the status value for errors.
	    int errorStatus = resultJSON.getInt( "status");

	    if( errorStatus != 0) {  // If the exchange indicated an error.

		String errorMessage = getErrorMessageForErrorCode( errorStatus);

		if( errorMessage == null) {  // If this error is unknown to this API implementation.

		    errorMessage = "(error type unknown to this API implementation)";
		}
		
		throw new TradeDataNotAvailableException( _name 
							  + " returned an error status of " 
							  + errorStatus 
							  + " : " 
							  + errorMessage);
	    }

	    // Create a result buffer for the depths.
	    List< Depth> resultBuffer = new ArrayList< Depth>();

	    // Loop over the keys of the JSON result.
	    for( Object currentKeyObject : resultJSON.keySet()) {

		// Cast the key to a string.
		String currentKey = (String)currentKeyObject;

		// Filter the status valus.
		if( currentKey.equalsIgnoreCase( "status") 
		    || currentKey.equalsIgnoreCase( "statustext")) {

		    continue;  // Ignore those values for now. (ToDo: check if status != 0 ?)
		}

		// Convert the key string to a currency object.
		Currency currentCurrency = CurrencyProvider.getInstance().getCurrencyForCode( currentKey.toUpperCase());

		// Since this key is a currency, get the depth for this currency.
		// The JSON for each currency is the same format as in the orderbook method.
		Depth depth = new VircurexDepth( resultJSON.getJSONObject( currentKey), new CurrencyPairImpl( currentCurrency, paymentCurrency), this);

		//Add this depth to the result buffer.
		resultBuffer.add( depth);
	    }

	    // Return the result.
	    return resultBuffer;
	}

	// Since fetching the depths failed, just throw an exception, that the trade data are not available.
	throw new TradeDataNotAvailableException( "Fetching the depths from " + this._name + " returned null");
    }

    /**
     * Get the current market depths sequentially via orderbook_alt method.
     *
     * @param currencyPairs The currency pairs to query.
     *
     * @return The current market depths for the given currency pairs.
     *
     * @throws TradeDataNotAvailableException if to many depths are not available.
     */
    public List<Depth> getDepthsSequentially( CurrencyPair [] currencyPairs) throws TradeDataNotAvailableException {

	// Define a map to resort all the currency pairs according to their payment currencies.
	Map< Currency, ArrayList< Currency>> _resortedCurrencyPairs = new HashMap< Currency, ArrayList< Currency>>();
	
	// Now loop over the currency pairs and resort them according to their payment currency.
	for( CurrencyPair currentPair : currencyPairs) {

	    // Get the current payment currency.
	    Currency currentPaymentCurrency = currentPair.getPaymentCurrency();

	    // If there is not list of currencies for this payment currency, create one.
	    if( ! _resortedCurrencyPairs.containsKey( currentPaymentCurrency)) {
		
		// Create an empty list and add it to the map.
		_resortedCurrencyPairs.put( currentPaymentCurrency, new ArrayList< Currency>());
	    }

	    // Now get the list for the current payment currency and add the currency, if it's not already 
	    // in the list.
	    Currency currentCurrency = currentPair.getCurrency();

	    // Get the list from the map.
	    ArrayList< Currency> currentList = _resortedCurrencyPairs.get( currentPaymentCurrency);

	    if( currentList == null) {  // <= this should never happen!
		
		LogUtils.getInstance().getLogger().error( "Current currency list is null is getDepthsSequentially() !");

		return null;  // Giving up here...
	    }

	    // If the current currency is not already in the list for the current payment currency.
	    if( ! currentList.contains( currentCurrency)) {
		
		// Add it to the list.
		currentList.add( currentCurrency);
	    }
	}

	// Create a result buffer for the depths.
	Map< Currency, List<Depth>> resultBuffer = new HashMap< Currency, List<Depth>>();

	// Precalculate the sleeping time.
	// sleep() works with miliseconds. The method returns microseconds,
	// so divide by 1000.
	long sleepInterval = getMinimumRequestInterval() / 1000 + 100;	

	// The pairs are resorted now, so we can query the depths for each payment currency.
	for( Map.Entry< Currency, ArrayList< Currency>> currentEntry : _resortedCurrencyPairs.entrySet()) {
	    
	    Currency currentPaymentCurrency = currentEntry.getKey();  // Get the payment currency for the currency list.
	    ArrayList< Currency> currentList = currentEntry.getValue();  // Get the list for the payment currency.
    
	    // ToDo: check if there is only 1 currency in the list and do a regular getDepth() then?

	    // Fetch all the depths for this payment currency.
	    List<Depth> currentDepths = getDepthsForPaymentCurrency( currentPaymentCurrency);

	    // Write the result in the buffer.
	    resultBuffer.put( currentPaymentCurrency, currentDepths);

	    // Wait until the exchange allows another request.
	    try {
		
		Thread.sleep( sleepInterval);
		
	    } catch( InterruptedException ie) {  // Should never happen, I guess...
		
		// So do nothing here.
	    }	    
	}

	// Now resort those lists according to the currency pair parameter back again.
	List<Depth> result = new ArrayList<Depth>();
	int currentIndex = 0;

	// Loop over the pair parameter.
	pairLoop:
	for( CurrencyPair currentPair : currencyPairs) {

	    // Find the matching pair in the list
	    for( Depth currentResult : resultBuffer.get( currentPair.getPaymentCurrency())) {

		// If this is the depth for the current currency pair.
		if( currentResult.getCurrencyPair().equals( currentPair)) {

		    // Store the depth in the result array.
		    result.add( currentIndex++, currentResult);

		    // And continue with the next currency pair.
		    continue pairLoop;
		}
	    }

	    // We have found no matching result, so just set this depth to null;
	    result.add( currentIndex++, null);

	    // System.out.println( "Setting depth to null");
	}
	

	return result;  // Return the array with the result.
    }

    /**
     * Get the current market depths (minimal data of the orders)for a given list of currency pairs.
     * This is an attempt for a optimized implementation for the vircurex trading site using proxies.
     *
     * @param currencyPairs The currency pairs to query.
     *
     * @return The current market depths for the given currency pairs.
     *
     * @throws TradeDataNotAvailableException if the depth is not available.
     */
    public List<Depth> getDepthsViaProxies( CurrencyPair [] currencyPairs) throws TradeDataNotAvailableException {

	// Create an array for the result.
	List<Depth> result = new ArrayList<Depth>();

	// Check, if all the currency pairs are actually supported here, so we
	// don't have to interrupt the requests later.
	for( CurrencyPair currentPair : currencyPairs) {

	    if( ! isSupportedCurrencyPair( currentPair)) {

		throw new CurrencyNotSupportedException( "Currency pair: " + currentPair.toString() + " is currently not supported on Vircurex");
	    }
	}

	// @see: https://blogs.oracle.com/CoreJavaTechTips/entry/get_netbeans_6

	// Create a thread pool to limit the number of concurrent requests.
	ExecutorService pool = Executors.newFixedThreadPool( getProxyInfo().getMaxNumberOfParallelProxyRequests());
	
	// Create a new set to store the results of the callables.
	Set<Future> fetchResults = new HashSet<Future>();

	// Loop over the currency pairs to request but keep the number of running threads 
	// within the proxy info limits.
	for( CurrencyPair currentPair : currencyPairs) {

	    // Copy the current pair to a final copy, so it's available in the inner class.
	    final CurrencyPair currentPairCopy = currentPair;
	    
	    // Create and submit a new callable to the thread pool, so it's executed later.
	    Future< Depth> future = pool.submit( new Callable< Depth>() {

		    /**
		     * This is the actual main method of the callable, that connections the server.
		     *
		     * @returns The depth or null, if the request failed.
		     */
		    @Override
		    public Depth call() {

			// Compute the url of the next currency pair.
			String url = "https://" 
			+ DOMAIN + "/api/orderbook.json?alt=" 
			+ currentPairCopy.getPaymentCurrency().getCode() + "&base=" + currentPairCopy.getCurrency().getCode();

			// Now do the actual request.
			ProxyRequestResult requestResult = HttpUtilsProxy.getInstance().httpGet( url, null, VircurexClient.this);

			// If the request went through
			if( requestResult.getType().equals( ProxyRequestResultType.SUCCESS)) {

			    try {

				// Convert the HTTP request return value to JSON to parse further.
				Depth depth = new VircurexDepth( JSONObject.fromObject( requestResult), currentPairCopy, VircurexClient.this);

				return depth;  // If the parsing worked, return the depth.

			    } catch( JSONException je) {

				LogUtils.getInstance().getLogger().error( "Cannot parse Vircurex depth return: " + je.toString());
			    }

			} else {  // The request failed.
			    
			    LogUtils.getInstance().getLogger().error( "HTTP get to vircurex failed: " + requestResult.getType().toString());
			}

			return null;  // Couldn't return a depth successfully, so just return null.
		    }
		});

	    // Add the future result to the set of results.
	    fetchResults.add( future);

	    // Loop over the list of thread results and copy them into a result array.
	    int currentDepthIndex = 0;
	    for( Future< Depth> currentFuture : fetchResults) { 

		try {

		    result.add( currentDepthIndex, currentFuture.get());

		} catch( ExecutionException ee) {  // If we cannot execute the callable,

		    result.add( currentDepthIndex, null);  // Just skip it.
		
		} catch( InterruptedException ie) {   // If we cannot get the result from the callable,

		    result.add( currentDepthIndex, null);  // Just skip it. 

		}

		++currentDepthIndex;
	    }
	}

	// Shutdown the pool, so all threads are terminated (should be empty anyway now
	// , since all the results are (hopefully) returned.
	pool.shutdown();

	return result;  // Return the list with the results.
    }

    /**
     * Translate an error code to an error message.
     * 
     * @param errorCode The error code to translate.
     *
     * @return An error message as a string, or null if this error code is unknown.
     */
    private String getErrorMessageForErrorCode( int errorCode) {

	// Just check the error code for known error messages.
	switch( errorCode) {

	case 1: return "Order does not exist";
	case 2: return "Order does not belong to the user";
	case 3:	return "Order is already released";
	case 4:	return "Unknown account name";
	case 5:	return "Unknown order type";
	case 6:	return "Missing parameter";
	case 7:	return "Order is not released";
	case 8:	return "Unknown currency";
	case 9:	return "API not configured, either not active or blank security word";
	case 10: return "Insufficient funds. Your available balance is less than the quantity you have specified in the API call";
	case 12: return "Currency is missing";
	case 13: return "Currency is not allowed. Currency1 cannot be a fiat currency";
	case 14: return "Order type is missing";
	case 15: return "Unknown order type";
	case 16: return "Trading the specified currency pair is not allowed";
	case 17: return "Order is already closed";
	case 18: return "Unknown order type. Only values 0 or 1 are allowed.";
	case 100: return "The ID was used already within the last 10 minutes.";
	case 200: return "The order volume (quantity * unitprice) must be at least 0.0001";
	case 201: return "Maximum number of open orders reached. A maximum of 50 are allowed";
	case 7999: return "Functions not active. You have not activated this function in your user profile";
	case 8000: return "Timestamp is off by more than 5 Minutes.";
	case 8001: return "API function is not activated";
	case 8002: return "User is banned from using the API";
	case 8003: return "Authentication failed";
	case 9001: return "API function is not available any more";
	case 9999: return "Unspecified error. Please contact customerservice.";
	default: return null;
	}
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

	    // Try to get a withdrawal fee from the fee map.
	    BigDecimal currentFee = _withdrawal_fees.get( order.getCurrencyPair().getCurrency());

	    // If there's no fee stored, throw an exception.
	    if( currentFee == null) {
		
		throw new CurrencyNotSupportedException( "No withdrawal fee for currency " 
							 + order.getCurrencyPair().getCurrency() 
							 + " stored, so I cannot compute fee for this order: " + order.toString());	
	    }

	    // If we have a fee, just multiply the percentage with the amount and get a price.
	    return new Price( order.getAmount().multiply( currentFee), order.getCurrencyPair().getCurrency());

	} else if( order instanceof DepositOrder) {  // If this is a deposit...

	    // It seems deposits are free at coins-e at the moment.
	    return new Price( "0", order.getCurrencyPair().getCurrency());

	} else if( order instanceof DepositOrder) {  // If this is a deposit...

	    // It seems deposits are free at coins-e at the moment.
	    return new Price( "0", order.getCurrencyPair().getCurrency());

	} else {  // This seems to be a regular trade (buy or sell).

	    // Trade is more complicated, since the currency changes in a sell.
	    
	    if( order.getOrderType() == OrderType.BUY) {  // If this is a buy order

		// Just multiply the bought amount with the percentage to get the fee.
		return new Price( order.getAmount().multiply( new BigDecimal( "0.002")), order.getCurrencyPair().getCurrency());

	    } else {  // this is a sell order, so the currency changes!

		// Compute the amount of the received payment currency and then multiply with the fee percentage.
		return new Price( order.getAmount().multiply( order.getPrice()).multiply( new BigDecimal( "0.002"))
				  , order.getCurrencyPair().getPaymentCurrency());
	    }
	}
    }

    /**
     * Get the fee for a withdrawal in percent.
     *
     * @return The fee for a withdrawal in percent.
     */
    public BigDecimal getFeeForWithdrawal() {

	throw new NotYetImplementedException( "Calculating the fee for withdrawals is not possible at coins-e without knowing the coin type. Please use getFeeForOrder() to get a correct fee for your order. The getFeeForWithdrawal() method is most likely deprecated in a later version of this lib.");
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
	PersistentPropertyList result = new PersistentPropertyList();

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
    public List<Trade> getTrades( long since_micros,  CurrencyPair currencyPair) {

	// If a request for trades is allowed
	if( isRequestAllowed( TradeSiteRequestType.Trades)) {

	    if( ! isSupportedCurrencyPair( currencyPair)) {
		throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on Vircurex");
	    }

	    // Vircurex has a 'since' parameter, but that only for the id and not for the date,
	    // so we have to filter the trade manually after the fetch.
	    String url = "https://" 
		+ DOMAIN + "/api/trades.json?alt=" 
		+ currencyPair.getPaymentCurrency().getCode() + "&base=" + currencyPair.getCurrency().getCode();

	    List<Trade> trades = new ArrayList<Trade>();
	    
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
		    
		    updateLastRequest();  // Update the timestamp of the last request.

		    return trades;  // Return the list.

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
	return 6L * 1000000L;  // Vircurex doesn't want to get polled more often than every 5s. I use 6, just in case...
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
	
	return ( _lastRequest == -1L) || ((_lastRequest + getMinimumRequestInterval()) < TimeUtils.getInstance().getCurrentGMTTimeMicros());
    }
    
    /**
     * Request all the supported currency pairs from vircurex.
     *
     * @return The supported currency pairs as an array, or null if an error occured.
     */
    CurrencyPair [] requestSupportedCurrencyPairs() {

	// The URL to fetch info on all the supported currency pairs.
	// Some pairs are actually returned 2x with currency and payment currency exchanged,
	// like euro_usd and usd_euro as an example.
	String url = _url + "api/get_info_for_currency.json";

	// Request the coin info from the server.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?

	    try {

		// Create a buffer for the result.
		ArrayList< CurrencyPair> resultBuffer = new ArrayList< CurrencyPair>();
		
		// Convert the HTTP request return value to JSON to parse further.
		JSONObject jsonInfo = JSONObject.fromObject( requestResult);
		
		// Get the keys as Strings.
		JSONArray jsonNames = jsonInfo.names();

		// Loop over the names.
		for( Iterator iter = jsonNames.iterator(); iter.hasNext() ; ) {

		    // The first key is the name of the currency.
		    String currency = (String)iter.next();

		    if( ! currency.equalsIgnoreCase( "status") &&           // Is this just the market status?
			! currency.equalsIgnoreCase( "cache_timestamp")) {  // or the cache timestamp?
			
			// Now get the info on the currency as a JSONObject.
			JSONObject jsonCurrencyInfo = jsonInfo.getJSONObject( currency);
			
			// Get the keys as Strings.
			// These are now the payment currencies.
			JSONArray jsonPaymentNames = jsonCurrencyInfo.names();
			
			// Loop over the names.
			for( Iterator iter2 = jsonPaymentNames.iterator(); iter2.hasNext() ; ) {
			    
			    // The second key is the name of the payment currency.
			    String paymentCurrency = (String)iter2.next();
			    
			    // Now create a currency pair.
			    CurrencyPair newPair = new CurrencyPairImpl( currency.toUpperCase(), paymentCurrency.toUpperCase());
			    
			    // Create the same pair also inverted.
			    CurrencyPair newPairInverted = new CurrencyPairImpl( paymentCurrency.toUpperCase(), currency.toUpperCase());

			    // Add the new pair only, if it's not already either exactly or inverted in the buffer.
			    // This is just to minimize the number of pairs, since a huge number slows the arb bot
			    // down.
			    if( ! resultBuffer.contains( newPair) && ! resultBuffer.contains( newPairInverted)) {
				
				resultBuffer.add( new CurrencyPairImpl( currency.toUpperCase(), paymentCurrency.toUpperCase()));
			    }
			}
		    }
		}

		// System.out.println( "DEBUG: created " + resultBuffer.size() + " currency pairs");

		// Now convert the buffer to an array and return it.
		return resultBuffer.toArray( new CurrencyPair[ resultBuffer.size()]);

	    } catch( JSONException je) {

		LogUtils.getInstance().getLogger().error( "Error while parsing the Vircurex info on currencies: " + je);
	    }
	} else {

	    LogUtils.getInstance().getLogger().error( "Vircurex server did not return info on currency pairs."); 
	}

	return null;  // Error while reading the info on currency pairs.
    }

    /**
     * Set new settings for the btc-e client.
     *
     * @param settings The new settings for the vircurex client.
     */
    public void setSettings( PersistentPropertyList settings) {

    }

    /**
     * Update the timestamp of the last request.
     */
    private void updateLastRequest() {
	_lastRequest = TimeUtils.getInstance().getCurrentGMTTimeMicros();
    }
}
