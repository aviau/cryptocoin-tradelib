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

package de.andreas_rueckert.trade.site.coins_e.client;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.persistence.PersistentPropertyList;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * Main class for the coins-e API.
 *
 * @see http://www.coins-e.com/exchange/api/documentation/
 */
public class CoinsEClient extends TradeSiteImpl implements TradeSite {

    // Static variables

    /**
     * The domain of the service.
     */
    public static String DOMAIN = "coins-e.com";


    // Instance variables

    /**
     * A map to store the trade fees for each coin type as percent.
     */
    Map< Currency, BigDecimal> _trade_fees = new HashMap< Currency, BigDecimal>();

    /**
     * A map to store the withdrawal fees for each coin type as percent.
     */
    Map< Currency, BigDecimal> _withdrawal_fees = new HashMap< Currency, BigDecimal>();


    // Constructors

    /**
     * Create a new connection to the Coins-E website.
     */
    public CoinsEClient() {

	super();  // Initialize the base class.

	_name = "Coins-E";
	_url = "https://" + this.DOMAIN + "/";

	// This request is just helpful to list the supported coin types
	// requestSupportedCurrencies();

	// I ignore most of the coins for now. Use the following code as an alternative with
	// a better Currency implementation maybe.
	// _supportedCurrencyPairs = new CurrencyPair[1];
	// _supportedCurrencyPairs[0] = new CurrencyPairImpl( CurrencyImpl.LTC, CurrencyImpl.BTC);

	// Fetch the supported currency pairs from the server. The number is just to high
	// to list them manually.
	// Also set the trade fees for each coin type here!
	_supportedCurrencyPairs = requestSupportedCurrencyPairs();

	// Set the deposit fee (always 0 % as it seems).
	_feeForDeposit = new BigDecimal( "0");

	// I cannot set the other fees here, because they depend on the coin type!
	// They are parse in the requestSupportedCurrencyPairs() call!
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
    public synchronized OrderStatus executeOrder( SiteOrder order) {

	throw new NotYetImplementedException( "Executing an order is not yet implemented for coins-e");	
    }

    /**
     * Get the current funds of the user.
     *
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The accounts with the current balance as a collection of Account objects, or null if the request failed.
     */
    public Collection<TradeSiteAccount> getAccounts( TradeSiteUserAccount userAccount) {

	throw new NotYetImplementedException( "Getting the accounts is not yet implemented for coins-e");	
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

	// Create an URL to request the depth.
	String url = "https://www." 
	    + this.DOMAIN 
	    + "/api/v2/market/" 
	    + currencyPair.getCurrency().getName().toUpperCase() + "_" + currencyPair.getPaymentCurrency().getName().toUpperCase()
	    + "/depth/";

	// Fetch the depth.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?
	    
	    try {

		// Convert the depth to JSON and check it's status.
		JSONObject jsonDepth = JSONObject.fromObject( requestResult);

		// Check the status of the returned depth.
		if( jsonDepth.getBoolean( "status") == true) {

		    // Convert the actual depth to a Depth object and return it.
		    return new CoinsEDepth( jsonDepth.getJSONObject( "marketdepth"), currencyPair, this);
		}
		
	    } catch( JSONException je) {

		System.err.println( "Cannot parse " + this._name + " depth return: " + je.toString());

		throw new TradeDataNotAvailableException( "cannot parse data from " + this._name);
	    }

	}

	return null;  // Request failed.
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

	    // Try to get a withdrawal fee from the fee map.
	    BigDecimal currentFee = _withdrawal_fees.get( order.getCurrencyPair().getCurrency());

	    // If there's no fee stored, throw an exception.
	    if( currentFee == null) {
		
		throw new CurrencyNotSupportedException( "No withdrawal fee for currency " 
							 + order.getCurrencyPair().getCurrency() 
							 + " stored, so I cannot compute fee for this order: " + order.toString());	
	    }

	    // If we have a fee, just multiply the percentage with the amount and get a price.
	    return new Price( order.getAmount().multiply( currentFee), order.getCurrencyPair().getCurrency());

	} else if( order instanceof DepositOrder) {  // If this is a deposit...

	    // It seems deposits are free at coins-e at the moment.
	    return new Price( "0", order.getCurrencyPair().getCurrency());

	} else {  // This seems to be a regular trade (buy or sell).

	    // Trade is more complicated, since the currency changes in a sell.
	    
	    if( order.getOrderType() == OrderType.BUY) {  // If this is a buy order

		// Try to get the fee for the currency.
		BigDecimal currentFee = _trade_fees.get( order.getCurrencyPair().getCurrency());

		if( currentFee == null) {  // If there is no fee stored.

		    throw new CurrencyNotSupportedException( "No trading fee for currency " 
							     + order.getCurrencyPair().getCurrency() 
							     + " stored, so I cannot compute fee for this order: " + order.toString());
		}

		// Just multiply the bought amount with the percentage to get the fee.
		return new Price( order.getAmount().multiply( currentFee), order.getCurrencyPair().getCurrency());

	    } else {  // this is a sell order, so the currency changes!

		// Try to get the fee for the payment(!) currency now.
		BigDecimal currentFee = _trade_fees.get( order.getCurrencyPair().getPaymentCurrency());

		if( currentFee == null) {  // If there is no fee stored.

		    throw new CurrencyNotSupportedException( "No trading fee for currency " 
							     + order.getCurrencyPair().getPaymentCurrency() 
							     + " stored, so I cannot compute fee for this order: " + order.toString());
		}

		// Compute the amount of the received payment currency and then multiply with the fee percentage.
		return new Price( order.getAmount().multiply( order.getPrice()).multiply( currentFee)
				  , order.getCurrencyPair().getPaymentCurrency());
	    }
	}
    }

    /**
     * Get the fee for trades in percent.
     *
     * @return The fee for trades in percent.
     */
    public BigDecimal getFeeForTrade() {
		
        throw new NotYetImplementedException( "Calculating the fee for trades is not possible at coins-e without knowing the coin type. Please use getFeeForOrder() to get a correct fee for your order. The getFeeForTrade() method is most likely deprecated in a later version of this lib.");
    }

    /**
     * Get the fee for a withdrawal in percent.
     *
     * @return The fee for a withdrawal in percent.
     */
    public BigDecimal getFeeForWithdrawal() {

	throw new NotYetImplementedException( "Calculating the fee for withdrawals is not possible at coins-e without knowing the coin type. Please use getFeeForOrder() to get a correct fee for your order. The getFeeForWithdrawal() method is most likely deprecated in a later version of this lib.");
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
     * Get the section name in the global property file.
     *
     * The name of the property section as a String.
     */
    public String getPropertySectionName() {
	return "CoinsE";
    }

    /**
     * Get the open orders on this trade site.
     *
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The open orders as a collection, or null if the request failed.
     */
    public Collection<SiteOrder> getOpenOrders( TradeSiteUserAccount userAccount) {

	throw new NotYetImplementedException( "Getting the open orders is not yet implemented for coins-e");	
    }

    /**
     * Get the settings of the coins-e API implementation.
     *
     * @return The settings of the coins-e API implementation.
     */
    public PersistentPropertyList getSettings() {

	// Create a new list for the settings.
	PersistentPropertyList result = new PersistentPropertyList();
	
	/* 
	 * Since the fees of coins-e are coin type specific, I don't store them in
	 * the properties for now.
	 * 
	result.add( new PersistentProperty( "Fee for deposit", null, "" + getFeeForDeposit(), 1));
	result.add( new PersistentProperty( "Fee for trades", null, "" + getFeeForTrade(), 2));
	result.add( new PersistentProperty( "Fee for withdrawal", null, "" + getFeeForWithdrawal(), 0));
	*/
	
	return result;
    }

    /**
     * Get the current ticker from the coins-e API.
     *
     * @param currencyPair The currency pair to query.
     * @param paymentCurrency The currency for the payments.
     *
     * @return The current coins-e ticker.
     *
     * @throws TradeDataNotAvailableException if the ticker is not available.
     */
    public Ticker getTicker( CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	throw new NotYetImplementedException( "Getting the ticker is not yet implemented for coins-e");
    }

    /**
     * Get a list of recent trades.
     *
     * @param since_micros The GMT-relative epoch in microseconds.
     * @param currencyPair The currency pair to query.
     *
     * @return The trades as a list of Trade objects.
     */
    public CryptoCoinTrade [] getTrades( long since_micros, CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	throw new NotYetImplementedException( "Getting the trades is not yet implemented for coins-e");
    }

    /**
     * Get the interval, in which the trade site updates it's depth, ticker etc. 
     * in microseconds.
     *
     * @return The update interval in microseconds.
     */
    public long getUpdateInterval() {
	return 60L * 1000000L;  // Just a default value for low volume.
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

    
    /**
     * Request the supported currencies from the coins-e server.
     *
     * @return The supported currencies as an array or null if an error occurred.
     */
    private Currency [] requestSupportedCurrencies() {

	String url = "http://www." + DOMAIN + "/api/v2/coins/list/";

	String requestResult = HttpUtils.httpGet( url);  // Request the list of supported markets.

	if( requestResult != null) {  // Request sucessful?

	    try {
		// Convert the HTTP request return value to JSON to parse further.
	        JSONObject jsonResult = JSONObject.fromObject( requestResult);

		if( jsonResult.getBoolean( "status") 
		    && jsonResult.getString( "message").equals( "success")) {  // Are there markets to parse?

		    // Get the coins as a JSON array.
		    JSONArray coinJSONArray = jsonResult.getJSONArray( "coins");

		    // Create a buffer for the result.
		    ArrayList<Currency> resultBuffer = new ArrayList<Currency>();

		    for( int currentCoinIndex = 0; currentCoinIndex < coinJSONArray.size(); ++currentCoinIndex) {
			
			// Get the current coin as a json object.
			JSONObject currentCoin = coinJSONArray.getJSONObject( currentCoinIndex);

			// Check if the status of this coin is ok...
			if( currentCoin.getString( "status").equals( "healthy")) {

			    String coinCode = currentCoin.getString( "coin");
			    
			    // This code is just to list the coin codes...
			    // System.out.print( coinCode + ", ");
			    //System.out.flush();

			    Currency newCurrency = CurrencyImpl.findByString( coinCode);

			    // Get the trading fee for this coin type.
			    BigDecimal trade_fee = new BigDecimal( currentCoin.getString( "trade_fee"));
			    
			    // Get the withdrawal fee for this coin type.
			    BigDecimal withdrawal_fee = new BigDecimal( currentCoin.getString( "withdrawal_fee"));

			    if( newCurrency != null) {           // If the new currency is found
				resultBuffer.add( newCurrency);  // Add it to the result buffer.
			    }
			}
		    }

		    // Convert the result buffer to an array an return it.
		    return resultBuffer.toArray( new Currency[ resultBuffer.size()]);
		}
	
	    } catch( JSONException je) {
	
		System.err.println( "Cannot parse " + this._name + " currency list: " + je.toString());
		
		throw new TradeDataNotAvailableException( "cannot parse data from " + this._name);
	    }
	}

	return null;  // An error occurred.
    }

    /**
     * Request the supported currency pairs from the coins-e server.
     *
     * @return The supported currency pairs as an array or null if an error occurred.
     */
    private CurrencyPair [] requestSupportedCurrencyPairs() {

	String url = "http://www." + DOMAIN + "/api/v2/markets/list/";

	String requestResult = HttpUtils.httpGet( url);  // Request the list of supported markets.

	if( requestResult != null) {  // Request sucessful?

	    try {
		// Convert the HTTP request return value to JSON to parse further.
	        JSONObject jsonResult = JSONObject.fromObject( requestResult);

		if( jsonResult.getBoolean( "status") 
		    && jsonResult.getString( "message").equals( "success")) {  // Are there markets to parse?

		    // Get the markets as a JSON array.
		    JSONArray marketJSONArray = jsonResult.getJSONArray( "markets");

		    // Create a buffer for the result.
		    ArrayList<CurrencyPair> resultBuffer = new ArrayList<CurrencyPair>();

		    for( int currentMarketIndex = 0; currentMarketIndex < marketJSONArray.size(); ++currentMarketIndex) {
			
			// Get the current market as a json object.
			JSONObject currentMarket = marketJSONArray.getJSONObject( currentMarketIndex);

			// Check if the status of this market is ok...
			if( currentMarket.getString( "status").equals( "healthy")) {

			    String [] currencyName = currentMarket.getString( "pair").split( "_");
			    
			    CurrencyPair newCurrencyPair = CurrencyPairImpl.findByString( currencyName[ 0] 
											  + "<=>" 
											  + currencyName[ 1]);

			    if( newCurrencyPair != null) {           // If the new currency pair is found
				resultBuffer.add( newCurrencyPair);  // Add it to the result buffer.
			    }
			}
		    }

		    // Convert the result buffer to an array an return it.
		    return resultBuffer.toArray( new CurrencyPair[ resultBuffer.size()]);
		}
	
	    } catch( JSONException je) {
	
		System.err.println( "Cannot parse " + this._name + " currency pair list: " + je.toString());
		
		throw new TradeDataNotAvailableException( "cannot parse data from " + this._name);
	    }
	}

	return null;  // An error occurred.
    }
}
