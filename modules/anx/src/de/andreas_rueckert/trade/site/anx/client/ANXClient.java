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

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.CryptoCoinTrade;
import de.andreas_rueckert.trade.Currency;
import de.andreas_rueckert.trade.CurrencyImpl;
import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.CurrencyPairImpl;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List; 
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * Main class for the ANX API.
 *
 * @see http://docs.anxv2.apiary.io/
 */
public class ANXClient extends TradeSiteImpl implements TradeSite {

    // Inner classes


    // Static variables


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
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.BTC, CurrencyImpl.USD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.BTC, CurrencyImpl.HKD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.BTC, CurrencyImpl.EUR));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.BTC, CurrencyImpl.CAD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.BTC, CurrencyImpl.AUD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.BTC, CurrencyImpl.SGD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.BTC, CurrencyImpl.JPY));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.BTC, CurrencyImpl.CHF));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.BTC, CurrencyImpl.GBP));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.BTC, CurrencyImpl.NZD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.LTC, CurrencyImpl.BTC)); 
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.LTC, CurrencyImpl.USD)); 
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.LTC, CurrencyImpl.HKD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.LTC, CurrencyImpl.EUR));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.LTC, CurrencyImpl.CAD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.LTC, CurrencyImpl.AUD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.LTC, CurrencyImpl.SGD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.LTC, CurrencyImpl.JPY));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.LTC, CurrencyImpl.CHF));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.LTC, CurrencyImpl.GBP));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.LTC, CurrencyImpl.NZD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.PPC, CurrencyImpl.BTC)); 
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.PPC, CurrencyImpl.LTC));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.PPC, CurrencyImpl.USD)); 
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.PPC, CurrencyImpl.HKD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.PPC, CurrencyImpl.EUR));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.PPC, CurrencyImpl.CAD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.PPC, CurrencyImpl.AUD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.PPC, CurrencyImpl.SGD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.PPC, CurrencyImpl.JPY));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.PPC, CurrencyImpl.CHF));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.PPC, CurrencyImpl.GBP));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.PPC, CurrencyImpl.NZD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.NMC, CurrencyImpl.BTC)); 
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.NMC, CurrencyImpl.LTC));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.NMC, CurrencyImpl.USD)); 
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.NMC, CurrencyImpl.HKD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.NMC, CurrencyImpl.EUR));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.NMC, CurrencyImpl.CAD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.NMC, CurrencyImpl.AUD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.NMC, CurrencyImpl.SGD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.NMC, CurrencyImpl.JPY));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.NMC, CurrencyImpl.CHF));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.NMC, CurrencyImpl.GBP));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.NMC, CurrencyImpl.NZD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.DOGE, CurrencyImpl.BTC)); 
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.DOGE, CurrencyImpl.USD)); 
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.DOGE, CurrencyImpl.HKD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.DOGE, CurrencyImpl.EUR));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.DOGE, CurrencyImpl.CAD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.DOGE, CurrencyImpl.AUD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.DOGE, CurrencyImpl.SGD));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.DOGE, CurrencyImpl.JPY));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.DOGE, CurrencyImpl.CHF));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.DOGE, CurrencyImpl.GBP));
	supportedCurrencyPairs.add( new CurrencyPairImpl( CurrencyImpl.DOGE, CurrencyImpl.NZD));

	// Copy the list to the instance array (defined in super class).
	_supportedCurrencyPairs = supportedCurrencyPairs.toArray( new CurrencyPair[ supportedCurrencyPairs.size()]);
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
     * Get the ANX code for a currency pair.
     *
     * @param currencyPair The currency pair.
     *
     * @return The ANX code for the currency pair as a string.
     */
    private final String getANXCurrencyPairString( CurrencyPair currencyPair) {

	return currencyPair.getCurrency().toString() + currencyPair.getPaymentCurrency().toString();
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
    public Depth [] getDepths( CurrencyPair [] currencyPairs) throws TradeDataNotAvailableException {

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

		    // Convert the result buffer to an array and return it.
		    return resultBuffer.toArray( new Depth[ resultBuffer.size()]);

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

	    if( withdrawnCurrency.equals( CurrencyImpl.BTC)
		|| withdrawnCurrency.equals( CurrencyImpl.LTC)
		|| withdrawnCurrency.equals( CurrencyImpl.NMC)
		|| withdrawnCurrency.equals( CurrencyImpl.PPC)
		|| withdrawnCurrency.equals( CurrencyImpl.DOGE)) {

		// Withdrawing cryptocoins is free.
		return new Price( "0", withdrawnCurrency);

	    } else {

		// Since I cannot check, if FIAT is send via wire or SEPA, I just leave it away for now.
		throw new CurrencyNotSupportedException( "The fees for FIAT withdraw are currently not implemented for " + _name);
	    }

	} else if( order instanceof DepositOrder) {   // If this is a deposit order

	     // Get the deposited currency.
	    Currency depositedCurrency = order.getCurrencyPair().getCurrency();

	    if( depositedCurrency.equals( CurrencyImpl.BTC)
		|| depositedCurrency.equals( CurrencyImpl.LTC)
		|| depositedCurrency.equals( CurrencyImpl.NMC)
		|| depositedCurrency.equals( CurrencyImpl.PPC)
		|| depositedCurrency.equals( CurrencyImpl.DOGE)) {

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

	    return super.getFeeForOrder( order);  // Should never happen...
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
    public CryptoCoinTrade [] getTrades( long since_micros, CurrencyPair currencyPair) throws TradeDataNotAvailableException {

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
		List<CryptoCoinTrade> trades = new ArrayList<CryptoCoinTrade>();

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

		CryptoCoinTrade [] tradeArray = trades.toArray( new CryptoCoinTrade[ trades.size()]);  // Convert the list to an array.
		    
		// updateLastRequest( TradeSiteRequestType.Trades);  // Update the timestamp of the last request.

		return tradeArray;  // And return the array.

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
