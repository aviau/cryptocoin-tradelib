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

package de.andreas_rueckert.trade.site.bitfinex.client;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.CryptoCoinTrade;
import de.andreas_rueckert.trade.CurrencyNotSupportedException;
import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.DepositOrder;
import de.andreas_rueckert.trade.order.OrderStatus;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.order.WithdrawOrder;
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
import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;


/**
 * Main class for the Bitfinex API.
 *
 * @see https://www.bitfinex.com/pages/api
 */
public class BitfinexClient extends TradeSiteImpl implements TradeSite {

    // Inner classes


    // Static variables


    // Instance variables


    // Constructors

    /**
     * Create a new Bitfinex client instance.
     */
    public BitfinexClient() {

	_name = "Bitfinex";  // Set the name of this exchange.

	_url = "https://api.bitfinex.com/v1/";  // Base URL for API calls.
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
     * Get the Bitfinex name for a currency pair.
     *
     * @param currencyPair The currency pair.
     *
     * @return The Bitfinex symbol for the currency pair as a string.
     */
    private final String getBitfinexCurrencyPairSymbol( CurrencyPair currencyPair) {

	// The Bitfinex symbol is just a concat of the 2 codes as lowercase (i.e. 'ltcusd').
	return currencyPair.getCurrency().getName().toLowerCase() 
	    + currencyPair.getPaymentCurrency().getName().toLowerCase();
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

	// Create the URL to fetch the depth.
	String url = _url + "book/" + getBitfinexCurrencyPairSymbol( currencyPair);


	
	throw new NotYetImplementedException( "Getting the depth is not yet implemented for " + _name);
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

	throw new NotYetImplementedException( "Getting the ticker is not yet implemented for " + _name);
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
     * Request the supported currency pairs from the Bitfinex server.
     *
     * @return true, if the currencies were returned, false in case of an error.
     */
    private final boolean requestSupportedCurrencyPairs() {

	String url = _url + "symbols";  // The URL to fetch the traded markets.

	
	// Request info on the traded pairs from the server.
	String requestResult = HttpUtils.httpGet( url);
	
	if( requestResult != null) {  // If the server returned a response.

	    try {

		// Try to parse the response.
		// If it is an error message, this will be a JSONObject(!).
		JSONArray jsonResult = JSONArray.fromObject( requestResult);

		// Create a buffer for the parsed currency pairs.
		List< CurrencyPair> resultBuffer = new ArrayList< CurrencyPair>();

		// Loop over the created JSON array and read every currency pair as a string.
		for( int currentPairIndex = 0; currentPairIndex < jsonResult.size(); ++currentPairIndex) {

		    // Get the current pair as a string (i.e 'ltcusd').
		    String currencyPairName = jsonResult.getString( currentPairIndex);

		    // Since some coins have now codes from 2 to 4 chars, the following code is rough at best...
		    // It might fail, once Bitfinex adds new coin types, but then the whole concept is suboptimal anyway.

		    // Get the traded currency from the JSON object.
		    de.andreas_rueckert.trade.Currency currency = de.andreas_rueckert.trade.CurrencyImpl.findByString( currencyPairName.substring( 0, 3).toUpperCase());
		    
		    // Get the payment currency from the JSON object.
		    de.andreas_rueckert.trade.Currency paymentCurrency = de.andreas_rueckert.trade.CurrencyImpl.findByString( currencyPairName.substring( 3).toUpperCase());

		    // Create a pair from the currencies.
		    de.andreas_rueckert.trade.CurrencyPair currentPair = new de.andreas_rueckert.trade.CurrencyPairImpl( currency, paymentCurrency);
		
		    // Add the current pair to the result buffer.
		    resultBuffer.add( currentPair);
		}

		// Convert the buffer to an array and store the currency pairs into the default client array.
		_supportedCurrencyPairs = resultBuffer.toArray( new CurrencyPair[ resultBuffer.size()]);
		
		return true;  // Reading the currency pairs worked ok.
		
	    } catch( JSONException je) {

		// Write the exception to the log. Should help to identify the problem.
		LogUtils.getInstance().getLogger().error( "Cannot parse " + this._name + " market stats return: " + je.toString());
		
		return false;  // Reading the currency pairs failed.
	    }
	    
	} else {  // The server did not return any reply.
		    
	    // Write the error message to the log. Should help to identify the problem.
	    LogUtils.getInstance().getLogger().error( "Error while fetching the " 
						      + _name 
						      + " supported currency pairs. Server returned no reply.");
	}

	return false;   // Fetching the traded currency pairs failed.
    }
}