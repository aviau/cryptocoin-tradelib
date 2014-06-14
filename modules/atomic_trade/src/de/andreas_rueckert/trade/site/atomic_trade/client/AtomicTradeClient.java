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
 * Main class for the Atomic-Trade exchange.
 *
 * @see https://www.atomic-trade.com/MarketAPI
 */
public class AtomicTradeClient extends TradeSiteImpl implements TradeSite {

    // Inner classes


    // Static variables


    // Instance variables

    /**
     * The fees for buys, that differ from the default fee of 0.25% .
     */
    private Map< Currency, BigDecimal> _buyFees = new HashMap< Currency, BigDecimal>();

    /**
     * The fees for sells, that differ from the default fee of 0.25%.
     */
    private Map< Currency, BigDecimal> _sellFees = new HashMap< Currency, BigDecimal>();


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

	// Set the buy fees, that differ from the default 0.25%.
	_buyFees.put( CurrencyImpl.BEL, new BigDecimal( "0.001"));    // BellsCoin has 0.1%
	_buyFees.put( CurrencyImpl.CCS, new BigDecimal( "0"));        // CCS has 0%
	_buyFees.put( CurrencyImpl.CINNI, new BigDecimal( "0.005"));  // CinniCoin has 0.5%
	_buyFees.put( CurrencyImpl.SVC, new BigDecimal( "0.0015"));   // Sovereigncoin has 0.15%
	_buyFees.put( CurrencyImpl.USD, new BigDecimal( "0.0075"));   // US Dollar has 0.75%
	_buyFees.put( CurrencyImpl.XDQ, new BigDecimal( "0.003"));    // Dirac has 0.3%

	// Set the sell fees, that differ from the default 0.25%.
	_sellFees.put( CurrencyImpl.CCS, new BigDecimal( "0.001"));   // CCS has 1%
	_sellFees.put( CurrencyImpl.CINNI, new BigDecimal( "0.005")); // Cinnicoin has 0.5%
	_sellFees.put( CurrencyImpl.USD, new BigDecimal( "0.0075"));  // US Dollar has 0.75%
	_sellFees.put( CurrencyImpl.XDQ, new BigDecimal( "0.003"));   // Dirac has 0.3%
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

	// Do the actual request.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?

	    try {

		// Convert the result to JSON.
		JSONObject requestResultJSON = (JSONObject)JSONObject.fromObject( requestResult);

		// Try to parse the market data and turn them into a depth object.
		return new AtomicTradeDepth( requestResultJSON.getJSONObject( "market"), currencyPair, this);

	    } catch( JSONException je) {

		LogUtils.getInstance().getLogger().error( "Cannot parse " + this._name + " depth return: " + je.toString());

		throw new TradeDataNotAvailableException( "cannot parse depth data from " + this._name);
	    }
	}

	// Response from server was null.
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
     * @see https://www.atomic-trade.com/Fees
     */
    public synchronized Price getFeeForOrder( SiteOrder order) {

	if( order instanceof WithdrawOrder) {   // If this is a withdraw order

	    throw new NotYetImplementedException( "Getting the withdraw fee is not yet implemented for " + _name);

	} else if( order instanceof DepositOrder) {   // If this is a deposit order

	    throw new NotYetImplementedException( "Getting the deposit fee is not yet implemented for " + _name);

	} else if( order.getOrderType() == OrderType.BUY) {  // Is this a buy trade order?

	    // Get the bought currency.
	    Currency boughtCurrency = order.getCurrencyPair().getCurrency();

	    // Check, if this currency has a non-default fee.
	    BigDecimal fee = _buyFees.get( boughtCurrency);

	    if( fee == null) {  // If no fee is returned,

		fee = new BigDecimal( "0.0025");  // use the default fee of 0.25%.
	    }
		
	    // Create the fee as a price with the bought currency.
	    return new Price( order.getAmount().multiply( fee), order.getCurrencyPair().getCurrency());
	     
	} else if( order.getOrderType() == OrderType.SELL) {  // This is a sell trade order

	    // Get the sold currency.
	    Currency soldCurrency = order.getCurrencyPair().getCurrency();

	    // Check, if this currency has a non-default fee.
	    BigDecimal fee = _sellFees.get( soldCurrency);

	    if( fee == null) {  // If no fee is returned,

		fee = new BigDecimal( "0.0025");  // use the default fee of 0.25%.
	    }
		
	    // A sell order has the payment currency as the target currency.
	    return new Price( order.getAmount().multiply( order.getPrice()).multiply( fee), order.getCurrencyPair().getPaymentCurrency());
	    
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
