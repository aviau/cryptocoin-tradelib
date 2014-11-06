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

package de.andreas_rueckert.trade.site.btc_china.client;

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
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.order.WithdrawOrder;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteImpl;
import de.andreas_rueckert.trade.site.TradeSiteRequestType;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.trade.Trade;
import de.andreas_rueckert.trade.TradeDataNotAvailableException;
import de.andreas_rueckert.util.HttpUtils;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Main class for the btc-china API.
 *
 * @see <a href="https://gist.github.com/mkraemer/7483878">PHP Btc-China API implementation</a>
 * @see <a href="http://btcchina.org/api-market-data-documentation-en">Btc-China API market data</a>
 */
public class BtcChinaClient extends TradeSiteImpl implements TradeSite {

    // Static variables

    /**
     * The domain of the service.
     */
    public static String DOMAIN = "btcchina.com";


    // Instance variables


    // Constructors

    /**
     * Create a new connection to the btcchina.com website.
     */
    public BtcChinaClient() {

	super();

	_name = "BtcChina";
	_url = "https://btcchina.com/";

	// Define the supported currency pairs for this trading site.
	_supportedCurrencyPairs = new CurrencyPair[3];
	_supportedCurrencyPairs[0] = new CurrencyPairImpl( "BTC", "CNY");
	_supportedCurrencyPairs[1] = new CurrencyPairImpl( "LTC", "CNY");
	_supportedCurrencyPairs[2] = new CurrencyPairImpl( "LTC", "BTC");
    }


    // Methods

    /**
     * Execute a authenticated query on this trade site.
     *
     * @param method The method to execute.
     * @param arguments The arguments to pass to the server.
     * @param userAccount The user account on the exchange, or null if the default account should be used.
     *
     * @return The returned data as JSON or null, if the request failed.
     *
     * @see <a href="https://gist.github.com/mkraemer/7483878">Btc-China PHP sources</a>
     */
    private final JSONObject authenticatedHTTPRequest( String method, Map<String, String> arguments, TradeSiteUserAccount userAccount) {

	HashMap<String, String> headerLines = new HashMap<String, String>();  // Create a new map for the header lines.

	throw new NotYetImplementedException( "Authenticated requests are not yet implemented for " + this._name);
    }

    /**
     * Cancel an order on the trade site.
     *
     * @param order The order to cancel.
     *
     * @return true, if the order was canceled. False otherwise.
     */
    public boolean cancelOrder( SiteOrder order) {

	throw new NotYetImplementedException( "Cancelling an order is not implemented for " + this._name);
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

	OrderType orderType = order.getOrderType();  // Get the type of this order.

	if( ( orderType == OrderType.BUY) || ( orderType == OrderType.SELL)) {  // If this is a buy or sell order, run the trade code.

	    throw new NotYetImplementedException( "Trade orders are not yet implemented for " + this._name);
	    
	} else if( orderType == OrderType.DEPOSIT) {  // This is a deposit order..

	    throw new NotYetImplementedException( "Deposit orders are not yet implemented for " + this._name);

	} else if( orderType == OrderType.WITHDRAW) {  // This is a withdraw order.

	    throw new NotYetImplementedException( "Withdraw orders are not yet implemented for " + this._name);

	}

	return null;  // An error occured, or this is an unknow order type?
    }

    
    /**
     * Get the current funds of the user.
     *
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The accounts with the current balance as a collection of Account objects, or null if the request failed.
     */
    public Collection<TradeSiteAccount> getAccounts( TradeSiteUserAccount userAccount) {

	throw new NotYetImplementedException( "Getting the accounts is not yet implemented for " + this._name);
    }

    /**
     * Get the btc-china name for a given currency pair.
     *
     * @param currencyPair The given currency pair.
     *
     * @return The currency pair as a String.
     */
    private final String getBtcChinaCurrencyPairName( CurrencyPair currencyPair) {

	return currencyPair.getCurrency().getCode().toLowerCase() 
	    + currencyPair.getPaymentCurrency().getCode().toLowerCase();
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
	
	// Create the URL for the request.
	// This URL scheme is not in the docs yet, but it seems to work.
	String url = "https://" + "data." + DOMAIN + "/data/orderbook?market=" + getBtcChinaCurrencyPairName( currencyPair);

	// Perform the actual request.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?
	    try {
		// Convert the HTTP request return value to JSON to parse further.
		return new BtcChinaDepth( JSONObject.fromObject( requestResult), currencyPair, this);

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

	    // Get the withdrawn currency.
	    Currency currency = order.getCurrencyPair().getCurrency();

	    if( currency.hasCode( "CNY")) {
		
		// CNY: 0.38%,withdrawal amount less than 200 CNY cost 2 CNY fee.
		if( order.getAmount().compareTo( new BigDecimal( "200")) < 0) {

		    return new Price( "2", currency);

		} else {

		    return new Price( order.getAmount().multiply( new BigDecimal( "0.0038")), currency);

		}

		    
	    } else if( currency.hasCode( "BTC")) {

		// BTC Withdrawal: 0.0001 BTC for each withdrawal.
		return new Price( "0.0001", currency);

	    } else if( currency.hasCode( "LTC")) {

		// LTC Withdrawal: 0.001 LTC for each withdrawal.
		return new Price( "0.001", order.getCurrencyPair().getCurrency());

	    } else {  // This is a unknow currency.
		
		throw new CurrencyNotSupportedException( this._name + ": cannot compute withdraw fee for currency: " + currency.getCode());
	    }

	} else if( order instanceof DepositOrder) {

	    // Btc-China doesn't charge for deposit orders.
	    return new Price( "0", order.getCurrencyPair().getCurrency());

	} else if( order.getOrderType() == OrderType.BUY) {  // If this is a buy order
		
	    return new Price( "0", order.getCurrencyPair().getCurrency());
	    
	} else if( order.getOrderType() == OrderType.SELL) {  // this is a sell order, so the currency changes!

	    return new Price( "0", order.getCurrencyPair().getPaymentCurrency());

	} else {  // Unknown order type.

	    return null;  // Should never happen.
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
	
	throw new NotYetImplementedException( "Getting the open orders is not yet implemented for " + this._name);
    }

    /**
     * Get the section name in the global property file.
     *
     * @return The name of the property section as a String.
     */
    public String getPropertySectionName() {
	return "BtcChina";
    }

    /**
     * Get the current ticker from the btc-china API.
     *
     * @param currencyPair The currency pair to query.
     *
     * @return The current btc-china ticker.
     *
     * @throws TradeDataNotAvailableException if the ticker is not available.
     */
    public BtcChinaTicker getTicker( CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	if( ! isSupportedCurrencyPair( currencyPair)) {
	    throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on " + this._name);
	}

	// Create the URL for the request.
	// This URL scheme is not in the docs yet, but it seems to work.
	String url = "https://" + "data." + DOMAIN + "/data/ticker?market=" + getBtcChinaCurrencyPairName( currencyPair);

	// Perform the actual request.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?
	    try {

		// Convert the HTTP request return value to JSON to parse further.
		return new BtcChinaTicker( JSONObject.fromObject( requestResult), currencyPair, this);

	    } catch( JSONException je) {

		System.err.println( "Cannot parse ticker object: " + je.toString());
	    }
	}
	
	throw new TradeDataNotAvailableException( "The " + this._name + " ticker request failed");
    }

    /**
     * Get a list of recent trades.
     *
     * @see <a href="http://www.reddit.com/r/Bitcoin/comments/1qteyu/any_apis_for_chinese_exchanges/">Chinese exchange APIs</a>
     *
     * @param since_micros The GMT-relative epoch in microseconds.
     * @param currencyPair The currency pair to query.
     *
     * @return The trades as a list of Trade objects.
     *
     * @throws TradeDataNotAvailableException if the trades are not available.
     */
    public List<Trade> getTrades( long since_micros, CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	if( ! isSupportedCurrencyPair( currencyPair)) {
	    throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on " + this._name);
	}

	// Create the URL for the request.
	// This URL scheme is not in the docs yet, but it seems to work.
	String url = "https://" + "data." + DOMAIN + "/data/trades?market=" + getBtcChinaCurrencyPairName( currencyPair);

	throw new NotYetImplementedException( "Getting the trades is not implemented for " + this._name);

	// return null;
    }

    /**
     * Get the interval, in which the trade site updates it's depth, ticker etc. 
     * in microseconds.
     *
     * @return The update interval in microseconds.
     */
    public long getUpdateInterval() {

	return 15L * 1000000L;  // Just a guess. No clue yet, how btc-china handles this.
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

	return true;  // Just a dummy for now, since I don't have any info, how btc-china handles things...
	              // If anyone has more info, please mail me!
    }

}
