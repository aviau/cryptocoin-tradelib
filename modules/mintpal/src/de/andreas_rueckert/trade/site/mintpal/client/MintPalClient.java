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
import de.andreas_rueckert.trade.Currency;
import de.andreas_rueckert.trade.CurrencyImpl;
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
import java.util.List;
import java.util.Map;
import net.sf.json.JSONArray;
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

    /**
     * The fees for withdraw orders.
     */
    Map< Currency, BigDecimal> _withdrawFees = null;


    // Constructors

    /**
     * Create a new MintPal client instance.
     */
    public MintPalClient() {

	_name = "MintPal";  // Set the name of this exchange.

	_url = "https://api.mintpal.com/v1/";  // Current URL for API requests.

	// Fetch the traded currency pairs.
	if( ! requestSupportedCurrencyPairs()) {
	    
	    // Write the problem to the log. 
	    LogUtils.getInstance().getLogger().error( "Error while fetching the traded currency pairs at " + _name);
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
	String url = _url + "market/orders/" + currencyPair.getCurrency().getName() + "/" + currencyPair.getPaymentCurrency().getName() + "/";

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

		LogUtils.getInstance().getLogger().error( "Cannot parse " + this._name + " depth return: " + je.toString());

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

		LogUtils.getInstance().getLogger().error( "Cannot parse " + this._name + " depth return: " + je.toString());

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
     * Get the fee for an order in the resulting currency.
     * Synchronize this method, since several users might use this method with different
     * accounts and therefore different fees via a single API implementation instance.
     *
     * @param order The order to use for the fee computation.
     *
     * @return The fee in the resulting currency (currency value for buy, payment currency value for sell).
     *
     * @see https://www.mintpal.com/fees 
     */
    public synchronized Price getFeeForOrder( SiteOrder order) {

	if( order instanceof DepositOrder) {

	    // MintPal doesn't seem to charge for deposit orders.
	    return new Price( "0", order.getCurrencyPair().getCurrency());

	} else if(( order.getOrderType() == OrderType.BUY) || ( order.getOrderType() == OrderType.SELL)) {

	    // Currently the fee for buy and sell orders seems to be 0.15 % for all currencies...
	    // @see https://www.mintpal.com/fees
	    // but the fee for buy orders is added to the paid amount?
	    return new Price( order.getPrice().multiply( order.getAmount().multiply( new BigDecimal( "0.0015"))), order.getCurrencyPair().getPaymentCurrency());

	} else if( order instanceof WithdrawOrder) {

	    // We have to switch the coin types here, since there seems to be no API call to fetch the fees... :-(
	    
	    BigDecimal fee = getWithdrawFee( order.getCurrencyPair().getCurrency());

	    if( fee == null) {

		throw new CurrencyNotSupportedException( this._name + ": cannot compute withdraw fee for this order: " + order.toString());
	    }

	    // The fee is in percent so divide by 100.
	    return new Price( order.getAmount().multiply( fee).multiply( new BigDecimal( "0.01")), order.getCurrencyPair().getCurrency());

	} else {  // Unknown order type? Just use the default implementation for it.

	    return super.getFeeForOrder( order);
	}
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

	// Create the URL to fetch stats on the given market (there is no real ticker request).
	String url = _url +"market/stats/" + currencyPair.getCurrency().getName() + "/" + currencyPair.getPaymentCurrency().getName();

	// Do the actual request.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?

	    try {
		
		// Try to parse the response.
		JSONObject jsonResult = JSONObject.fromObject( requestResult);
		
		// Check, if there is an error field in the response.
		if( jsonResult.containsKey( "error")) {  // An error occurred.

		    // Write the error message to the log. Should help to identify the problem.
		    LogUtils.getInstance().getLogger().error( "Error while fetching the market stats (ticker) on "
							      + _name
							      + " for "
							      +  currencyPair.toString() 
							      + ". Error is: " + jsonResult.getJSONObject( "error").getString( "message")); 
		    
		    throw new TradeDataNotAvailableException( "cannot fetch market stats (ticker) for " + this._name);

		} else {  // Fetching the data worked

		    // Create a new ticker object and return it.
		    return new MintPalTicker( jsonResult, currencyPair, this);
		}

	    } catch( JSONException je) {

		System.err.println( "Cannot parse " + this._name + " ticker return: " + je.toString());

		throw new TradeDataNotAvailableException( "cannot parse data from " + this._name);
	    }
	}

	// If the exchange returned no response throw an exception.
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
     * Get the withdraw fees as percent(!) for a given currency.
     *
     * @param currency The currency, we want the fee for.
     *
     * @return The withdraw fee as percent(!).
     */
    public BigDecimal getWithdrawFee( Currency currency) {

	if( _withdrawFees == null) {  // No fees stored yet.

	    // Create the map with the fees.
	    _withdrawFees = new HashMap< Currency, BigDecimal>();

	    // Add the known currencies to the map.
	    _withdrawFees.put( CurrencyImpl.n365, new BigDecimal( "0.000002"));
	    _withdrawFees.put( CurrencyImpl.AC, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.AUR, new BigDecimal( "0.002000"));
	    _withdrawFees.put( CurrencyImpl.BC, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.BTC, new BigDecimal( "0.000200"));
	    _withdrawFees.put( CurrencyImpl.BTCS, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.CAIX, new BigDecimal( "0.200000"));
	    _withdrawFees.put( CurrencyImpl.CINNI, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.COMM, new BigDecimal( "0.200000"));
	    _withdrawFees.put( CurrencyImpl.CTM, new BigDecimal( "0.200000"));
	    _withdrawFees.put( CurrencyImpl.DGB, new BigDecimal( "0.200000"));
	    _withdrawFees.put( CurrencyImpl.DOGE, new BigDecimal( "1.000000"));
	    _withdrawFees.put( CurrencyImpl.DOPE, new BigDecimal( "2.000000"));
	    _withdrawFees.put( CurrencyImpl.DRK, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.ECC, new BigDecimal( "0.200000"));
	    _withdrawFees.put( CurrencyImpl.EMC2, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.EMO, new BigDecimal( "2.000000"));
	    _withdrawFees.put( CurrencyImpl.FAC, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.FLT, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.GRS, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.GRUMP, new BigDecimal( "0.200000"));
	    _withdrawFees.put( CurrencyImpl.HIRO, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.HVC, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.IVC, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.KARM, new BigDecimal( "0.200000"));
	    _withdrawFees.put( CurrencyImpl.KDC, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.LTC, new BigDecimal( "0.002000"));
	    _withdrawFees.put( CurrencyImpl.METH, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.MINT, new BigDecimal( "0.200000"));
	    _withdrawFees.put( CurrencyImpl.MRC, new BigDecimal( "0.200000"));
	    _withdrawFees.put( CurrencyImpl.MRS, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.MYR, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.MZC, new BigDecimal( "0.200000"));
	    _withdrawFees.put( CurrencyImpl.NAUT, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.NC2, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.NOBL, new BigDecimal( "0.200000"));
	    _withdrawFees.put( CurrencyImpl.OLY, new BigDecimal( "0.200000"));
	    _withdrawFees.put( CurrencyImpl.PANDA, new BigDecimal( "0.200000"));
	    _withdrawFees.put( CurrencyImpl.PENG, new BigDecimal( "0.200000"));
	    _withdrawFees.put( CurrencyImpl.PLC, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.PND, new BigDecimal( "2.000000"));
	    _withdrawFees.put( CurrencyImpl.POT, new BigDecimal( "0.200000"));
	    _withdrawFees.put( CurrencyImpl.Q2C, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.RBBT, new BigDecimal( "2.000000"));
	    _withdrawFees.put( CurrencyImpl.RIC, new BigDecimal( "0.002000"));
	    _withdrawFees.put( CurrencyImpl.SAT, new BigDecimal( "0.200000"));
	    _withdrawFees.put( CurrencyImpl.SPA, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.SUN, new BigDecimal( "0.200000"));
	    _withdrawFees.put( CurrencyImpl.SYNC, new BigDecimal( "0.000002"));
	    _withdrawFees.put( CurrencyImpl.TAK, new BigDecimal( "0.200000"));
	    _withdrawFees.put( CurrencyImpl.TES, new BigDecimal( "0.200000"));
	    _withdrawFees.put( CurrencyImpl.TOP, new BigDecimal( "0.200000"));
	    _withdrawFees.put( CurrencyImpl.UNO, new BigDecimal( "0.001000"));
	    _withdrawFees.put( CurrencyImpl.USDE, new BigDecimal( "0.200000"));
	    _withdrawFees.put( CurrencyImpl.UTC, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.VTC, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.WC, new BigDecimal( "1.000000"));
	    _withdrawFees.put( CurrencyImpl.XC, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.XLB, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.XXL, new BigDecimal( "2.000000"));
	    _withdrawFees.put( CurrencyImpl.YC, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.ZED, new BigDecimal( "0.020000"));
	    _withdrawFees.put( CurrencyImpl.ZEIT, new BigDecimal( "0.200000"));
	    _withdrawFees.put( CurrencyImpl.ZET, new BigDecimal( "0.020000"));
	}

	// Now try to fetch the fee from the map.
	return _withdrawFees.get( currency);
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
     * Request the supported currency pairs from the bittrex server.
     *
     * @return true, if the currencies were returned, false in case of an error.
     */
    private final boolean requestSupportedCurrencyPairs() {

	String url = _url + "market/summary/";  // The URL to fetch data on all the traded markets.

	// Request info on the traded pairs from the server.
	String requestResult = HttpUtils.httpGet( url);
	
	if( requestResult != null) {  // If the server returned a response.

	    try {

		// Try to parse the response.
		// If it is an error message, this will be a JSONObject(!).
		JSONArray jsonResult = JSONArray.fromObject( requestResult);
		
		// Create a buffer for the parsed currency pairs.
		List< CurrencyPair> resultBuffer = new ArrayList< CurrencyPair>();
		
		// Loop over the returned JSON array and convert the entries to CurrencyPair objects.
		for( int currentPairIndex = 0; currentPairIndex < jsonResult.size(); ++currentPairIndex) {
		    
		    // Get the current pair as a JSON object.
		    JSONObject currentPairJSON = jsonResult.getJSONObject( currentPairIndex);
		    
		    // Get the traded currency from the JSON object.
		    // MintPal added CAIX to their coins, but uses CAIx as the spelling. Therefore the upper case.
		    de.andreas_rueckert.trade.Currency currency = de.andreas_rueckert.trade.CurrencyImpl.findByString( currentPairJSON.getString( "code").toUpperCase());
		    
		    // Get the payment currency from the JSON object.
		    de.andreas_rueckert.trade.Currency paymentCurrency = de.andreas_rueckert.trade.CurrencyImpl.findByString( currentPairJSON.getString( "exchange").toUpperCase());

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