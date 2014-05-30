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

package de.andreas_rueckert.trade.site.mintpal.client;

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
import java.util.Collection;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * Main class for the MintPal API.
 *
 * @see https://www.mintpal.com/api
 */
public class MintPalClient extends TradeSiteImpl implements TradeSite {

    // Inner classes


    // Static variables


    // Instance variables


    // Constructors

    /**
     * Create a new MintPal client instance.
     */
    public MintPalClient() {

	_name = "MintPal";  // Set the name of this exchange.

	_url = "https://api.mintpal.com/v1/";  // Current URL for API requests.
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
     *
     * @see https://www.mintpal.com/api#marketorders
     */
    public Depth getDepth( CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	if( ! isSupportedCurrencyPair( currencyPair)) {
	    throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on " + _name);
	}

	// Create 2 vars for the buy and sell orders.
	JSONObject buyJSON = null,  sellJSON = null;

	// Create the base URL for the requests (buy and sell have to be requested separately).
	String url = _url + "market/orders/" + currencyPair.getCurrency() + "/" + currencyPair.getPaymentCurrency() + "/";

	// Do the first request.
	String requestResult = HttpUtils.httpGet( url + "BUY");
	
	if( requestResult != null) {  // Request sucessful?

	    try {

		// Try to parse the response.
		JSONObject jsonResult = JSONObject.fromObject( requestResult);

		// Check, if there is an error field in the response.
		if( jsonResult.containsKey( "error")) {  // An error occurred.
		    
		    // Write the error message to the log. Should help to identify the problem.
		    LogUtils.getInstance().getLogger().error( "Error while fetching the buy orders for the "
							      + _name
							      + " depth for "
							      +  currencyPair.toString() 
							      + ". Error is: " + jsonResult.getJSONObject( "error").getString( "message"));

		    throw new TradeDataNotAvailableException( "cannot fetch buy orders for depth for " + this._name);

		} else {  // Fetching the data worked

		    buyJSON = jsonResult;  // Set the result for the buy orders.
		}

	    } catch( JSONException je) {

		System.err.println( "Cannot parse " + this._name + " depth return: " + je.toString());

		throw new TradeDataNotAvailableException( "cannot parse data from " + this._name);
	    }

	} else {

	    	throw new TradeDataNotAvailableException( this._name + " server did not respond to depth request for buy orders");
	}
	// End of first request.

	// Respect the minimum request interval.
	try {

	    Thread.sleep( getMinimumRequestInterval() / 1000L);

	} catch( InterruptedException ie) {

	    // Should not happen...
	}

	// Do the second request.
	requestResult = HttpUtils.httpGet( url + "SELL");

	if( requestResult != null) {  // Request sucessful?

	    try {

		// Try to parse the response.
		JSONObject jsonResult = JSONObject.fromObject( requestResult);

		// Check, if there is an error field in the response.
		if( jsonResult.containsKey( "error")) {  // An error occurred.
		    
		    // Write the error message to the log. Should help to identify the problem.
		    LogUtils.getInstance().getLogger().error( "Error while fetching the sell orders for the "
							      + _name
							      + " depth for "
							      +  currencyPair.toString() 
							      + ". Error is: " + jsonResult.getJSONObject( "error").getString( "message"));

		    throw new TradeDataNotAvailableException( "cannot fetch sell orders for depth for " + this._name);

		} else {  // Fetching the data worked.

			sellJSON = jsonResult;  // Set the result for the sell orders.
		}

	    } catch( JSONException je) {

		System.err.println( "Cannot parse " + this._name + " depth return: " + je.toString());

		throw new TradeDataNotAvailableException( "cannot parse data from " + this._name);
	    }
	} else {
	    throw new TradeDataNotAvailableException( this._name + " server did not respond to depth request for sell orders");
	}
	// End of second request.

	// Now create a new depth object from the returned buy and sell orders.
	return new MintPalDepth( buyJSON, sellJSON, currencyPair, this); 
    }

   /**
     * Get the shortest allowed requet interval in microseconds.
     *
     * @return The shortest allowed request interval in microseconds.
     */
    public final long getMinimumRequestInterval() {

	// The actual limit seems to be 10 requests / second.
	// @see https://www.mintpal.com/api
	// (in the overview.)
	return 2L * 100000L;  // Use 5 requests per second just to make sure we don't violate the limit.
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
}