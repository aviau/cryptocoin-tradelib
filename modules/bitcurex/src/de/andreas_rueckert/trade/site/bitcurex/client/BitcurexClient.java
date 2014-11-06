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

package de.andreas_rueckert.trade.site.bitcurex.client;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.CryptoCoinTrade;
import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.currency.CurrencyImpl;
import de.andreas_rueckert.trade.currency.CurrencyNotSupportedException;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.currency.CurrencyPairImpl;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.DepositOrder;
import de.andreas_rueckert.trade.order.OrderStatus;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * Main class for the bitcurex API.
 *
 * @see <a href="https://eur.bitcurex.com/reading-room/API">Bitcurex API</a>
 */
public class BitcurexClient extends TradeSiteImpl implements TradeSite {

    // Static variables

    /**
     * The domain of the service.
     */
    public static String DOMAIN = "eur.bitcurex.com";

    /**
     * Nonce for API request. Must be increased for each API call.
     */
    private static long _nonce;


    // Instance variables

    /**
     * The API key.
     */
    private String _key = null;


    // Constructors

    /**
     * Create a new connection to the Bitcurex website.
     */
    public BitcurexClient() {

	super();  // Initialize the base class.

	_name = "Bitcurex";
	_url = "https://" + this.DOMAIN + "/";

	// Define the supported currency pairs for this trading site.
	_supportedCurrencyPairs = new CurrencyPair[1];
	_supportedCurrencyPairs[0] = new CurrencyPairImpl( "BTC", "EUR");

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
    public OrderStatus executeOrder( SiteOrder order) {

	throw new NotYetImplementedException( "Executing an order is not yet implemented for bitcurex");	
    }

    /**
     * Get the current funds of the user.
     *
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The accounts with the current balance as a collection of Account objects, or null if the request failed.
     */
    public Collection<TradeSiteAccount> getAccounts( TradeSiteUserAccount userAccount) {

	throw new NotYetImplementedException( "Getting the accounts is not yet implemented for bitcurex");	
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
	    throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on " + this._name);
	}

	String url = "https://" + this.DOMAIN + "/data/orderbook.json";

	// Fetch the depth.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?

	    try {
		// Convert the HTTP request return value to JSON to parse further.
		return new BitcurexDepth( JSONObject.fromObject( requestResult), currencyPair, this);

	    } catch( JSONException je) {
		System.err.println( "Cannot parse " + this._name + " depth return: " + je.toString());

		throw new TradeDataNotAvailableException( "cannot parse data from " + this._name);
	    }
	}
	
	throw new TradeDataNotAvailableException( this._name + " server did not respond to depth request");
    }

    /**
     * Get the fee for an order in the resulting currency.
     *
     * @param order The order to use for the fee computation.
     *
     * @return The fee in the resulting currency (currency value for buy, payment currency value for sell).
     */
    public Price getFeeForOrder( SiteOrder order) {

	if( order instanceof WithdrawOrder) {
		    
	    // Get the ordered currency.
	    Currency orderedCurrency = order.getCurrencyPair().getCurrency();

	    if( orderedCurrency.hasCode( "BTC")) {

		return new Price( "0.005", orderedCurrency);  // Current Bitcurex withdraw fee...

	    } else if( orderedCurrency.hasCode( "EUR")) {

		return new Price( "1.23", orderedCurrency);  // Current Bitcurex withdraw fee..

	    } else {

		System.out.println( "Compute withdaw fees for currencies other than btc or eur");

		throw new CurrencyNotSupportedException( "Cannot compute fee for this order: " + order.toString());
	    }
	} else if( order instanceof DepositOrder) {

	    Currency depositedCurrency = ((DepositOrder)order).getCurrency();
	    
	    if( depositedCurrency.hasCode( "BTC")) {
		
		// BTC deposits are free as far as I know.
		return new Price( "0.0", depositedCurrency);
	    
	} else {

		throw new NotYetImplementedException( "Deposit fees are not implemented for trade site " 
						      + getName() 
						      + " and currency " 
						      + depositedCurrency.getCode());
	    }

	} else {  // Bitcurex seems to have no trade fees?

	    Currency tradedCurrency = order.getCurrencyPair().getCurrency();

	    return new Price( "0.0", tradedCurrency);
	}
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

	throw new NotYetImplementedException( "Getting the open orders is not yet implemented for bitcurex");	
    }

    /**
     * Get the section name in the global property file.
     *
     * The name of the property section as a String.
     */
    public String getPropertySectionName() {
	return "Bitcurex";
    }

    /**
     * Get the current ticker from the bitcurex API.
     *
     * @param currencyPair The currency pair to query.
     *
     * @return The current bitcurex ticker.
     *
     * @throws TradeDataNotAvailableException if the ticker is not available.
     */
    public Ticker getTicker( CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	if( ! isSupportedCurrencyPair( currencyPair)) {
	    throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on Bitcurex");
	}

	String url = "https://" + this.DOMAIN + "/data/ticker.json";

	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?

	    try {

		// Convert the HTTP request return value to JSON to parse further.
		return new BitcurexTicker( JSONObject.fromObject( requestResult), currencyPair, this);
		
	    } catch( JSONException je) {
		System.err.println( "Cannot parse ticker object: " + je.toString());
	    }
	}
	
	throw new TradeDataNotAvailableException( "The " + this._name + " ticker request failed");
    }

    /**
     * Get a list of recent trades.
     *
     * @param since_micros The GMT-relative epoch in microseconds.
     * @param currencyPair The currency pair to query.
     *
     * @return The trades as a list of Trade objects.
     */
    public List<Trade> getTrades( long since_micros, CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	if( ! isSupportedCurrencyPair( currencyPair)) {
	    throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on Bitcurex");
	}

	String url = "https://" + this.DOMAIN + "/data/trades.json";

	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // If the HTTP request worked ok.

	    ArrayList<Trade> resultBuffer = new ArrayList<Trade>();

	    try {
		// Convert the result to an JSON array.
		JSONArray resultArray = JSONArray.fromObject( requestResult);
		
		// Iterate over the array and convert each trade from json to a Trade object.
		for( int i = 0; i < resultArray.size(); i++) {

		    JSONObject tradeObject = resultArray.getJSONObject(i);
		    
		    // Parse the json to create a new cryptocoin trade.
		    CryptoCoinTrade newTrade = new BitcurexTradeImpl( tradeObject, this, currencyPair);
		    
		    // If the trade fits the given timespan, add it to the result buffer.
		    if( newTrade.getTimestamp() > since_micros) {

			resultBuffer.add( newTrade);  // Add the new Trade object to the list.
		    }
		}
		
		// Return the list.
		return resultBuffer;

	    } catch( JSONException je) {
		System.err.println( "Cannot parse trade object: " + je.toString());
	    }
	}

	return null;  // An error occurred.
    }

    /**
     * Get the interval, in which the trade site updates it's depth, ticker etc. 
     * in microseconds.
     *
     * @return The update interval in microseconds.
     */
    public long getUpdateInterval() {
	return 30L * 1000000L;  // Bitcurex is quite low-volume, so this should be often enough.
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
