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

package de.andreas_rueckert.trade.site.atomic_trade.client;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.CryptoCoinTrade;
import de.andreas_rueckert.trade.Currency;
import de.andreas_rueckert.trade.CurrencyImpl;
import de.andreas_rueckert.trade.CurrencyNotSupportedException;
import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.CurrencyPairImpl;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.OrderStatus;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.order.SiteOrder;
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
 * Main class for the Atomic-Trade exchange.
 *
 * @see https://www.atomic-trade.com/MarketAPI
 */
public class AtomicTradeClient extends TradeSiteImpl implements TradeSite {

    // Inner classes


    // Static variables


    // Instance variables


    // Constructors
    
    /**
     * Create a new Atomic-Trade client instance.
     */
    public AtomicTradeClient() {

	_name = "Atomic-Trade";  // Set the name of this exchange.

	_url = "https://www.atomic-trade.com/";  // Base URL for API calls.

	// Try to request the supported currency pairs.
	if( ! requestSupportedCurrencyPairs()) {

	    LogUtils.getInstance().getLogger().error( "Cannot fetch the supported currency pairs for " + _name);
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

	// Create the URL to fetch the depth.
	String url = _url 
	    + "SimpleAPI?a=orderbook&c="
	    + currencyPair.getCurrency().toString()
	    + "&p="
	    + currencyPair.getPaymentCurrency().toString();

	// There is a problem with this API call.
	// @see: https://bitcointalk.org/index.php?topic=395192.msg7292671#msg7292671

	throw new NotYetImplementedException( "Getting the depth is not yet implemented for " + _name);
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

	return "AtomicTrade";
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

    /**
     * Request a list of active markets (= supported currency pairs) from the server.
     *
     * @return true, if the markets were read. False otherwise.
     */
    private final boolean requestSupportedCurrencyPairs() {

	// Create the URL to fetch the active markets.
	String url = _url + "SimpleAPI?a=markets";

	// Request info on the traded pairs from the server.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // If the server returned a response.

	    // Try to parse the response, which should be a JSON array.
	    JSONArray jsonPairList = JSONArray.fromObject( requestResult);

	    // Create a buffer for the result.
	    List<CurrencyPair> resultBuffer = new ArrayList<CurrencyPair>();

	    // Loop over the array and parse every pair string.
	    for( int currentPairIndex = 0; currentPairIndex < jsonPairList.size(); ++currentPairIndex) {

		// Each pair string has the form of "AC/BTC" as an example.
		String currentPairString = jsonPairList.getString( currentPairIndex);

		// Split the string into currency and payment currency.
		String [] currencyCodes = currentPairString.split( "/");

		// Create a currency object for the currency.
		Currency currency = CurrencyImpl.findByString( currencyCodes[0]);

		// Create a currency object for the payment currency.
		Currency paymentCurrency = CurrencyImpl.findByString( currencyCodes[1]);

		// Create a currency pair from the 2 currencies.
		CurrencyPair newCurrencyPair = new CurrencyPairImpl( currency, paymentCurrency);
		
		// Add the new pair to the result buffer.
		resultBuffer.add( newCurrencyPair);
	    }

	    // After the loop, convert the result buffer to an array and store it in the instance array for
	    // supported currency pairs.
	    _supportedCurrencyPairs = resultBuffer.toArray( new CurrencyPair[ resultBuffer.size()]);

	    return true;  // Fetching the supported currency pairs worked ok!

	} else {

	    LogUtils.getInstance().getLogger().error( _name + " returned null when the supported currency pairs were requested");

	    return false;
	}
    }
}
