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

package de.andreas_rueckert.trade.site.bitcoin_de.client;

import de.andreas_rueckert.MissingAccountDataException;
import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.persistence.PersistentProperty;
import de.andreas_rueckert.persistence.PersistentPropertyList;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.currency.CurrencyNotSupportedException;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.currency.CurrencyPairImpl;
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
import de.andreas_rueckert.trade.Trade;
import de.andreas_rueckert.trade.TradeDataNotAvailableException;
import de.andreas_rueckert.util.HttpUtils;
import de.andreas_rueckert.util.LogUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * Main class for the Bitcoin.de API.
 *
 * @see https://www.bitcoin.de/de/api/marketplace
 */
public class Bitcoin_deClient extends TradeSiteImpl implements TradeSite {

    // Inner classes


    // Static variables

    /**
     * This flag is mainly for debugging and deactivates the actual trading.
     */
    private final static boolean SIMULATION = true;


    // Instance variables

    /**
     * The default API key.
     */
    private String _defaultAPIKey = null;
    

    // Constructors
    
    /**
     * Create a new Bitcoin.de client instance.
     */
    public Bitcoin_deClient() {

	_name = "Bitcoin.de";  // Set the name of this exchange.

	_url = "https://bitcoinapi.de/v1/";  // Base URL for API calls.

	// Set the supported currency pairs manualls, since it seems there's no API method to fetch them?
	List<CurrencyPair> supportedCurrencyPairs = new ArrayList<CurrencyPair>();

	// Add the supported pairs of this exchange.
	supportedCurrencyPairs.add( new CurrencyPairImpl( "BTC", "EUR"));

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

	throw new NotYetImplementedException( "Bitcoin.de doesn't implement order execution in the API yet");
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

	throw new NotYetImplementedException( "Bitcoin.de doesn't implement order execution in the API yet");
    }

    /**
     * Get the current funds of the user via the API.
     *
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The accounts with the current balance as a collection of Account objects, or null if the request failed.
     */
    public Collection<TradeSiteAccount> getAccounts( TradeSiteUserAccount userAccount) {

	throw new NotYetImplementedException( "Bitcoin.de doesn't implement funds fetching in the API yet");
    }

    /**
     * Get the default API key for Bitcoin.de
     *
     * @return The default API key or null, if it is not present.
     */
    private final String getDefaultAPIKey() {

	return _defaultAPIKey;
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

	// Check, if an API key is available.
	if( getDefaultAPIKey() == null) {

	    throw new MissingAccountDataException( "There is not API key available for Bitcoin.de, which is required to request the depth.");
	}

	// Create the URL for the depth request.
	String url = _url + getDefaultAPIKey() + "/orderbook.json";
	
	// Do the actual request.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?
	    
	    try {
		
		// Convert the result to JSON.
		JSONObject requestResultJSON = (JSONObject)JSONObject.fromObject( requestResult);
		
		// Get the data and convert them to a depth object.
		return new Bitcoin_deDepth( requestResultJSON, currencyPair, this);

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
     * @see https://www.bitcoin.de/de/infos#p-main-2
     */
    public synchronized Price getFeeForOrder( SiteOrder order) {

	if( order instanceof WithdrawOrder) {   // If this is a withdraw order

	    return new Price( "0", order.getCurrencyPair().getCurrency());  // Can't find any fees for withdrawals?

	} else if( order instanceof DepositOrder) {   // If this is a deposit order

	    return new Price( "0", order.getCurrencyPair().getCurrency());  // Can't find any fees for deposits?

	} else if( order.getOrderType() == OrderType.BUY) {  // Is this a buy trade order?

	    // 0.5% fee for buyer and seller(!) (= 1% total)
	    return new Price( order.getAmount().multiply( new BigDecimal( "0.005")), order.getCurrencyPair().getCurrency());
	    
	} else if( order.getOrderType() == OrderType.SELL) {  // This is a sell trade order

	    // A sell order has the payment currency as the target currency.
	    return new Price( order.getAmount().multiply( order.getPrice()).multiply( new BigDecimal( "0.005"))
			      , order.getCurrencyPair().getPaymentCurrency());
	    
	} else {  // This is an unknown order type?

	    return null;  // Should never happen...
	}
    }

    /**
     * Get the shortest allowed request interval in microseconds.
     *
     * @return The shortest allowed request interval in microseconds.
     *
     * @see https://www.bitcoin.de/de/api/marketplace
     */
    public long getMinimumRequestInterval() {

	return 30L * 1000000L;  // 30s 
    }

    /**
     * Get the open orders on this trade site.
     *
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The open orders as a collection, or null if the request failed.
     */
    public Collection<SiteOrder> getOpenOrders( TradeSiteUserAccount userAccount) {

	throw new NotYetImplementedException( "Bitcoin.de doesn't implement open orders in the API yet");
    }

    /**
     * Get the section name in the global property file.
     *
     * @return The name of the property section as a String.
     */
    public String getPropertySectionName() {

	// Replace the dot in the name for the section.
	return _name.replace( ".", "_");
    }

    /**
     * Get the settings of the Bitcoin.de client.
     *
     * @return The setting of the Bitcoin.de client as a list.
     */
    public PersistentPropertyList getSettings() {

	// Get the settings from the base class.
	PersistentPropertyList result = super.getSettings();

	result.add( new PersistentProperty( "APIKey", null, _defaultAPIKey, 1));        // The key

	return result;
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

	throw new NotYetImplementedException( "Bitcoin.de doesn't implement a ticker in the API yet. Just a rate.");
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

	// Check, if an API key is available.
	if( getDefaultAPIKey() == null) {

	    throw new MissingAccountDataException( "There is not API key available for Bitcoin.de, which is required to request the trades.");
	}

	// Create the URL to fetch the trades.
	String url = _url + getDefaultAPIKey() + "trades.json";

	// Do the actual request.
	String requestResult = HttpUtils.httpGet( url);
	
	if( requestResult != null) {  // Request sucessful?

	    try {

		// Convert the HTTP request return value to JSON to parse further.
		JSONArray jsonData = JSONArray.fromObject( requestResult);

		// Create a buffer for the result.
		List<Trade> trades = new ArrayList<Trade>();

		// Compute a unix timestamp for fast comparison.
		long since = since_micros / 1000000L;

		// Iterate over the json array and convert each trade from json to a Trade object.
		for( int i = 0; i < jsonData.size(); ++i) {

		    JSONObject tradeObject = jsonData.getJSONObject(i);
		    
		    // Since bitcoin.de cannot filter for the timestamp, do this now...
		    if( tradeObject.getLong( "Date") >= since) {

			try {
			    
			    trades.add( new Bitcoin_deTrade( tradeObject, this, currencyPair));  // Add the new Trade object to the list.
			    
			} catch( JSONException je) {  // Cannot parse the JSON trade.

			    // Write the error to the log. Hopefully it should explain the problem.
			    LogUtils.getInstance().getLogger().error( "Error while parsing a trade from " 
								      + _name 
								      + ". Error message is: " + je.toString());

			    throw new TradeDataNotAvailableException( this._name + " reported an error while parsing a trade.");
			}
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

	return 60L * 1000000L;  // According to the docs, bitcoin.de updates every 60s.
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
     * Set new settings for the Bitcoin.de client.
     *
     * @param settings The new settings for the Bitcoin.de client.
     */
    public void setSettings( PersistentPropertyList settings) {
	
	super.setSettings( settings);
	
	String key = settings.getStringProperty( "APIKey");
	if( key != null) {
	    _defaultAPIKey = key;  // Get the API key from the settings.
	}
    }
}
