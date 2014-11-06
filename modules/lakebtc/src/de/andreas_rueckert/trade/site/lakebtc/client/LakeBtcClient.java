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

package de.andreas_rueckert.trade.site.lakebtc.client;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.CryptoCoinTrade;
import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.currency.CurrencyNotSupportedException;
import de.andreas_rueckert.trade.currency.CurrencyImpl;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.currency.CurrencyPairImpl;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.DepositOrder;
import de.andreas_rueckert.trade.order.OrderStatus;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.order.WithdrawOrder;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteImpl;
import de.andreas_rueckert.trade.site.TradeSiteRequestType;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.trade.Ticker;
import de.andreas_rueckert.trade.Trade;
import de.andreas_rueckert.trade.TradeDataNotAvailableException;
import de.andreas_rueckert.util.HttpUtils;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;


/**
 * Main class for the LakeBTC API.
 *
 * @see <a href="https://www.lakebtc.com/s/api?locale=en">LakeBTC API</a>
 */
public class LakeBtcClient extends TradeSiteImpl implements TradeSite {

    // Inner classes

    
    // Static variables


    // Instance variables


    // Constructors
    
    /**
     * Create a new connection to the LakeBTC website.
     */
    public LakeBtcClient() {

	super();

	_name = "LakeBTC";
	_url = "https://www.lakebtc.com/api_v1/";

	// Define the supported currency pairs for this trading site.
	_supportedCurrencyPairs = new CurrencyPair[2];
	_supportedCurrencyPairs[0] = new CurrencyPairImpl( "BTC", "CNY");
	_supportedCurrencyPairs[1] = new CurrencyPairImpl( "BTC", "USD");
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

	throw new NotYetImplementedException( "Executing is not yet implemented for " + this._name);
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
	String url = _url +  "bcorderbook";
	if( currencyPair.getPaymentCurrency().hasCode( "CNY")) {
	    url += "_cny";
	}

	// Perform the actual request.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?

	    try {
		// Convert the HTTP request return value to JSON to parse further.
		return new LakeBtcDepth( JSONObject.fromObject( requestResult), currencyPair, this);

	    } catch( JSONException je) {

		System.err.println( "Cannot parse " + this._name + " depth return: " + je.toString());

		throw new TradeDataNotAvailableException( "cannot parse data from " + this._name);
	    }
	}
	
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
     * @see <a href="https://lakebtc.com/s/fees?locale=en">LakeBTC fees</a>
     */
    public synchronized Price getFeeForOrder( SiteOrder order) {

	if( order instanceof WithdrawOrder) {   // If this is a withdraw order

	    // Get the withdrawn currency.
	    Currency withdrawnCurrency = order.getCurrencyPair().getCurrency();

	    if( withdrawnCurrency.hasCode( "BTC")) {

		// I always use the worst-case fee for now.
		// That's 0.5% + 0.001 btc
		// @see https://lakebtc.com/s/fees?locale=en
		return new Price( order.getAmount().multiply( new BigDecimal( "0.005")).add( new BigDecimal( "0.001")), withdrawnCurrency);

	    } else if( withdrawnCurrency.hasCode( "CNY")) {

		// Use 0.5% + 10 CNY
		return new Price( order.getAmount().multiply( new BigDecimal( "0.005")).add( new BigDecimal( "10")), withdrawnCurrency);

	    } else if( withdrawnCurrency.hasCode( "USD")) {

		// Use 0.5% + 5 USD. Bank fees might be added, but are not known to the lib.
		return new Price( order.getAmount().multiply( new BigDecimal( "0.005")).add( new BigDecimal( "10")), withdrawnCurrency);

	    } else {

		// This currency is not known to the lib for now.
		throw new CurrencyNotSupportedException( "The fees for " + withdrawnCurrency+ " withdraw are currently not implemented for " + _name);
	    }

	} else if( order instanceof DepositOrder) {   // If this is a deposit order

	    // Get the deposited currency.
	    Currency depositedCurrency = order.getCurrencyPair().getCurrency();

	    // These currencies are currently mentioned with a 0% fee at the fee page, so I check
	    // for them in case a new currency is added. Better throw an exception then.
	    if( depositedCurrency.hasCode( new String [] { "BTC", "USD", "CNY" })) {

		// Depositing cryptocoins is free.
		return new Price( "0", depositedCurrency);

	    } else {

		// Since I cannot request a fee for a different currency, I just throw an exception for now.
		throw new CurrencyNotSupportedException( "The fees for " 
							 + depositedCurrency 
							 + "  are currently not implemented for " + _name);
	    }

	} else if( order.getOrderType() == OrderType.BUY) {  // Is this a buy trade order?

	    // Just use 0.5% for now. Discounts may apply, but are not known to the lib.
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

	return _name;
    }

    /**
     * Get the current ticker from the LakeBTC API.
     *
     * @param currencyPair The currency pair to query.
     *
     * @return The current LakeBTC ticker.
     *
     * @throws TradeDataNotAvailableException if the ticker is not available.
     */
    public Ticker getTicker( CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	throw new NotYetImplementedException( "Getting the ticker is not implemented for " + this._name);
    }	

    /**
     * Get a list of recent trades.
     *
     * @param since_micros The GMT-relative epoch in microseconds.
     * @param currencyPair The currency pair to query.
     *
     * @return The trades as a list of Trade objects.
     *
     * @throws TradeDataNotAvailableException if the trades are not available.
     */
    public List<Trade> getTrades( long since_micros, CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	throw new NotYetImplementedException( "Getting the trades is not implemented for " + this._name);
    }

    /**
     * Get the interval, in which the trade site updates it's depth, ticker etc. 
     * in microseconds.
     *
     * @return The update interval in microseconds.
     */
    public long getUpdateInterval() {

	return 15L * 1000000L;  // Just a guess. No clue yet, how LakeBTC handles this.
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

	return true;  // Just a dummy for now, since I don't have any info, how LakeBTC handles things...
    }
}
