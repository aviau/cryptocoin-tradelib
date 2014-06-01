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

package de.andreas_rueckert.trade.site.bittrex.client;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.CryptoCoinTrade;
import de.andreas_rueckert.trade.Currency;  // <= to be replaced!
import de.andreas_rueckert.trade.CurrencyNotSupportedException;
import de.andreas_rueckert.trade.CurrencyPair;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * Main class for the Bittrex API.
 *
 * @see https://bittrex.com/Home/Api
 */
public class BittrexClient extends TradeSiteImpl implements TradeSite {

    // Inner classes


    // Static variables


    // Instance variables

    /**
     * The trade fees are flat for now, so return a fixed price for each trade.
     */
    private Map< Currency, Price> _tradeFees = new HashMap< Currency, Price>();


    // Constructors

    /**
     * Create a new Bittrex client instance.
     */
    public BittrexClient() {

	_name = "Bittrex";  // Set the name of this exchange.

	_url = "https://bittrex.com/api/v1/";  // The URL for the version 1 API.

	// Fetch the traded currency pairs.
	if( ! requestSupportedCurrencyPairs()) {
	    
	    // Write the problem to the log. 
	    LogUtils.getInstance().getLogger().error( "Error while fetching the traded currency pairs at " + _name);
	}

	// Fetch the fees for the currencies.
	if( ! requestCurrencyInfo()) {

	    // Write the problem to the log. 
	    LogUtils.getInstance().getLogger().error( "Error while fetching the trade fees at " + _name);
	}
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
	
	// The URL for the depth request.
	// I use max 50 orders for now. Should be enough for most trading apps, I guess.
	// @see 
	String url = _url + "public/getorderbook?market=" 
	    + currencyPair.getPaymentCurrency().getName() + "-" + currencyPair.getCurrency().getName()
	    + "&type=both&depth=50";

	// Do the actual request.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?

	    try {

		// System.out.println( "DEBUG: depth result: " + requestResult);
		
		// Try to parse the response.
		JSONObject jsonResult = JSONObject.fromObject( requestResult);

		// Check, if the success field indicates success.
		boolean successFlag = jsonResult.getBoolean( "success");
		
		if( successFlag) {  // If the flag is set, parse the actual result.
		    
		    // The depth is returned as a JSON object. Parse it in the created BittrexDepth instance.
		    return new BittrexDepth( jsonResult.getJSONObject( "result"), currencyPair, this); 

		} else {  // There is an error message returned hopefully...

		    // Write the error message to the log. Should help to identify the problem.
		    LogUtils.getInstance().getLogger().error( "Error while fetching the " 
							      + _name 
							      + " depth for " 
							      + currencyPair.toString() 
							      + ". Error is: " + jsonResult.getString( "message"));
		    
		    // and return null.
		    return null;
		}

	    } catch( JSONException je) {

		System.err.println( "Cannot parse " + this._name + " depth return: " + je.toString());

		throw new TradeDataNotAvailableException( "cannot parse data from " + this._name);
	    }
	}

	throw new TradeDataNotAvailableException( this._name + " server did not respond to depth request");
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

	    // Bittrex doesn't charge for withdraw orders.
	    return new Price( "0", order.getCurrencyPair().getCurrency());

	} else if( order instanceof DepositOrder) {

	    // Bittrex doesn't charge for deposit orders.
	    return new Price( "0", order.getCurrencyPair().getCurrency());

	} if(( order.getOrderType() == OrderType.BUY) || ( order.getOrderType() == OrderType.SELL)) {

	    // Try to get the fee for the given currency.
	    Price fee = _tradeFees.get( order.getCurrencyPair().getCurrency());

	    // If there is no fee availble, log the problem and throw an execption.
	    if( fee == null) {

		// Write the an message to the log. Should help to identify the problem.
		LogUtils.getInstance().getLogger().error( this._name + ": error while fetching the fee for " 
							  + order.getCurrencyPair().getCurrency().getName());

		throw new CurrencyNotSupportedException( this._name + ": cannot compute fee for this order: " + order.toString());
	    }
	    
	    return fee;

	} else {  // Just the default implementation for the other order forms.

	    return super.getFeeForOrder( order);
	}
    }

    /**
     * Get the shortest allowed requet interval in microseconds.
     *
     * @return The shortest allowed request interval in microseconds.
     */
    public final long getMinimumRequestInterval() {

	return 15L * 1000000L;  // Use a reasonable default value. Don't know any better for now.
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

	// The URL for the ticker request.
	String url = _url + "public/getticker?market="
	    + currencyPair.getPaymentCurrency().getName() + "-" + currencyPair.getCurrency().getName();

	// Do the actual request.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?

	    try {

		// Try to parse the response.
		JSONObject jsonResult = JSONObject.fromObject( requestResult);

		// Check, if the success field indicates success.
		boolean successFlag = jsonResult.getBoolean( "success");
		
		if( successFlag) {  // If the flag is set, parse the actual result.
		    
		    // The ticker is returned as a JSON object. Parse it in the created BittrexTicker instance.
		    return new BittrexTicker( jsonResult.getJSONObject( "result"), currencyPair, this); 

		} else {

		    // Write the error message to the log. Should help to identify the problem.
		    LogUtils.getInstance().getLogger().error( "Error while fetching the " 
							      + _name 
							      + " ticker for " 
							      + currencyPair.toString() 
							      + ". Error is: " + jsonResult.getString( "message"));

		    // and return null.
		    return null;
		}

	    }  catch( JSONException je) {

		System.err.println( "Cannot parse " + this._name + " ticker return: " + je.toString());

		throw new TradeDataNotAvailableException( "cannot parse data from " + this._name);
	    }
	}

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
     * Request info on the traded currencies including the trading fees(!).
     * (That's the main reason to implement this method here.)
     *
     * @return true, if the currency info including the fees was returned. False otherwise.
     */
    private final boolean requestCurrencyInfo() {

	String url = _url + "public/getcurrencies";  // The URL for fetching the traded currencies.

	// Request info on the traded currencies from the server.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // If the server returned a response.

	    // Try to parse the response.
	    JSONObject jsonResult = JSONObject.fromObject( requestResult);

	    // Check, if the success field indicates success.
	    boolean successFlag = jsonResult.getBoolean( "success");

	    if( successFlag) {  // If the flag is set, parse the actual result.

		// The info on the currencies is returned as a JSON array of JSON objects.
		JSONArray currencyListJSON = jsonResult.getJSONArray( "result");

		// Loop over the currency array and convert the entries to a Currency <=> Price map.
		for( int currentCurrencyIndex = 0; currentCurrencyIndex < currencyListJSON.size(); ++currentCurrencyIndex) {

		    // Get the current currency info as a JSON object.
		    JSONObject currentCurrencyJSON = currencyListJSON.getJSONObject( currentCurrencyIndex);

		    // Get the traded currency from the JSON object.
		    de.andreas_rueckert.trade.Currency currency = de.andreas_rueckert.trade.CurrencyImpl.findByString( currentCurrencyJSON.getString( "Currency"));

		    // Create a price from the tx fee and the parsed currency.
		    Price fee = new Price( currentCurrencyJSON.getDouble( "TxFee"), currency);

		    // Put the fee into the currency <=> fee map.
		    _tradeFees.put( currency, fee);
		}

		return true;  // Fees successfully fetched.

	    } else {  // There is an error message returned hopefully...

		// Write the error message to the log. Should help to identify the problem.
		LogUtils.getInstance().getLogger().error( "Error while fetching the " 
							  + _name 
							  + " currency info. Error is: " + jsonResult.getString( "message"));
	    }
	}

	return false;   // Fetching the traded currencies failed.
    }

    /**
     * Request the supported currency pairs from the bittrex server.
     *
     * @return true, if the currencies were returned, false in case of an error.
     */
    private final boolean requestSupportedCurrencyPairs() {

	String url = _url + "public/getmarkets";  // The URL for fetching the traded pairs.

	// Request info on the traded pairs from the server.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // If the server returned a response.

	    try {
	    
		// Try to parse the response.
		JSONObject jsonResult = JSONObject.fromObject( requestResult);
		
		// Check, if the success field indicates success.
		boolean successFlag = jsonResult.getBoolean( "success");
		
		if( successFlag) {  // If the flag is set, parse the actual result.
		    
		    // The pairs are returned as a JSON array.
		    JSONArray pairListJSON = jsonResult.getJSONArray( "result");
		    
		    // Create a buffer for the parsed currency pairs.
		    List< CurrencyPair> resultBuffer = new ArrayList< CurrencyPair>();
		    
		    // Loop over the pair array and convert the entries to CurrencyPair objects.
		    for( int currentPairIndex = 0; currentPairIndex < pairListJSON.size(); ++currentPairIndex) {
			
			// Get the current pair as a JSON object.
			JSONObject currentPairJSON = pairListJSON.getJSONObject( currentPairIndex);
			
			// Get the traded currency from the JSON object.
			de.andreas_rueckert.trade.Currency currency = de.andreas_rueckert.trade.CurrencyImpl.findByString( currentPairJSON.getString( "MarketCurrency"));
			
			// Get the payment currency from the JSON object.
			de.andreas_rueckert.trade.Currency paymentCurrency = de.andreas_rueckert.trade.CurrencyImpl.findByString( currentPairJSON.getString( "BaseCurrency"));

			// Create a pair from the currencies.
			de.andreas_rueckert.trade.CurrencyPair currentPair = new de.andreas_rueckert.trade.CurrencyPairImpl( currency, paymentCurrency);
			
			// ToDo: check, if this market is currently active? (really required? Are market deactivated frequently?)
			
			// Add the current pair to the result buffer.
			resultBuffer.add( currentPair);
		    }

		    // Convert the buffer to an array and store the currency pairs into the default client array.
		    _supportedCurrencyPairs = resultBuffer.toArray( new CurrencyPair[ resultBuffer.size()]);
		    
		    return true;  // Reading the currency pairs worked ok.

		} else {  // There is an error message returned hopefully...
		    
		    // Write the error message to the log. Should help to identify the problem.
		    LogUtils.getInstance().getLogger().error( "Error while fetching the " 
							      + _name 
							      + " supported currency pairs. Error is: " + jsonResult.getString( "message"));
		}
	    } catch( JSONException je) {

		LogUtils.getInstance().getLogger().error( "Cannot parse " + this._name + " depth return: " + je.toString());

		return false;
	    }
	} else {

	    // Write the error message to the log. Should help to identify the problem.
	    LogUtils.getInstance().getLogger().error( "Error while fetching the " 
						      + _name 
						      + " supported currency pairs. Server returned no reply.");
	}

	return false;   // Fetching the traded currency pairs failed.
    }
}