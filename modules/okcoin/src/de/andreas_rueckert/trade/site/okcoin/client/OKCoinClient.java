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

package de.andreas_rueckert.trade.site.okcoin.client;

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
import java.util.Collection;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * Main class for the OKCoin API.
 *
 * @see https://www.okcoin.com/about/tradeApi.do
 */
public class OKCoinClient extends TradeSiteImpl implements TradeSite {

    // Inner classes


    // Static variables


    // Instance variables


    // Constructors
    
    /**
     * Create a new OKCoin client instance.
     */
    public OKCoinClient() {

	_name = "OKCoin";  // Set the name of this exchange.

	_url = "https://www.okcoin.cn/api/";  // Base URL for API calls.

	// Set the supported currency pairs.
	_supportedCurrencyPairs = new CurrencyPair[2];
	_supportedCurrencyPairs[0] = new CurrencyPairImpl( CurrencyImpl.BTC, CurrencyImpl.CNY);
	_supportedCurrencyPairs[1] = new CurrencyPairImpl( CurrencyImpl.LTC, CurrencyImpl.CNY);
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
	String url = _url + "depth.do?symbol=" + getOKCoinCurrencyCode( currencyPair);

	// Do the actual request.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?

	    try {

		// Convert the result to JSON.
		JSONObject requestResultJSON = (JSONObject)JSONObject.fromObject( requestResult);

		// It seems OKCoin always returns a depth, no matter what the currencypair code is?
		// @see: https://bitcointalk.org/index.php?topic=342952.msg7293837#msg7293837

		// So try to convert the response to a depth object and return it.
		return new OKCoinDepth( requestResultJSON, currencyPair, this);

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
     * @see https://www.okcoin.com/about/vip.do
     */
    public synchronized Price getFeeForOrder( SiteOrder order) {

	if( order instanceof WithdrawOrder) {   // If this is a withdraw order

	    throw new NotYetImplementedException( "Getting the withdraw fee is not yet implemented for " + _name);

	} else if( order instanceof DepositOrder) {   // If this is a deposit order

	    throw new NotYetImplementedException( "Getting the deposit fee is not yet implemented for " + _name);

	} else if( order.getOrderType() == OrderType.BUY) {  // Is this a buy trade order?

	    // According to the fee schedule (see method header)m the fee is always 0?
	    return new Price( "0", order.getCurrencyPair().getCurrency());
	    
	} else if( order.getOrderType() == OrderType.SELL) {  // This is a sell trade order

	    // A sell order has the payment currency as the target currency.
	    // According to the fee schedule (see method header)m the fee is always 0?
	    return new Price( "0", order.getCurrencyPair().getPaymentCurrency());
	    
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
     * Get the OKCoin code for a given currency pair.
     *
     * @param currencyPair The currency pair to get the code for.
     *
     * @return The String representation of the currency pair, or null, if it cannot be encoded.
     */
    private final String getOKCoinCurrencyCode( CurrencyPair currencyPair) {

	// OKCoin uses codes like 'ltc_cny'.
	return currencyPair.getCurrency().toString().toLowerCase()
	    + "_"
	    + currencyPair.getPaymentCurrency().toString().toLowerCase();
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
