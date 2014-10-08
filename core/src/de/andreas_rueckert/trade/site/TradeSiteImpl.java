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

package de.andreas_rueckert.trade.site;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.persistence.PersistentProperty;
import de.andreas_rueckert.persistence.PersistentPropertyList;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.DepositOrder;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.order.WithdrawOrder;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.request.TradeSiteProxyInfo;
import de.andreas_rueckert.trade.TradeDataNotAvailableException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A basic TradeSite implementation.
 * It does not implement the full interface yet, so it might better be called 'core'?
 */
public class TradeSiteImpl {

    // Static variables

    /**
     * The various log levels.
     */
    protected final static int LOGLEVEL_ERROR        = 0;  // Minimal log level. Just log errors.
    protected final static int LOGLEVEL_WARNING      = 1;  // More info. Log warnings.
    protected final static int LOGLEVEL_NOTIFICATION = 2;  // Notify about cache findings etc.
    protected final static int LOGLEVEL_DEBUG        = 4;  // Log more or less everything.

    /**
     * The maximum number of failures, while trying to fetch depths from this trade site.
     */
    private final static int MAX_FAILS_IN_ROW = 5;
    

    // Instance variables

    /**
     * The fee for deposits.
     */
    //protected BigDecimal _feeForDeposit = BigDecimal.ZERO;  // Set a default value;

    /**
     * The fee for trades.
     */
    //protected BigDecimal _feeForTrade = BigDecimal.ZERO;  // Set a default value.

    /**
     * The fee for withdrawals.
     */
    //protected BigDecimal _feeForWithdrawal = BigDecimal.ZERO;  // Set a default value.

    /**
     * The current log level.
     */
    protected int _logLevel = LOGLEVEL_ERROR;

    /**
     * The name of the trading site.
     */
    protected String _name;

    /**
     * The proxy info for this trade site.
     * Default is null, so no proxies are allowed.
     */
    protected TradeSiteProxyInfo _proxyInfo = null;

    /**
     * The supported currency pairs of this trading site.
     */
    protected CurrencyPair [] _supportedCurrencyPairs = null;

    /**
     * The url of the trading site.
     */
    protected String _url;


    // Constructors
    

    // Methods

    /**
     * Get the current market depth (minimal data of the orders).
     * This is just a dummy to compile the code. API implementations overwrite this method.
     *
     * @param currencyPair The currency pair to query.
     *
     * @return The current market depth.
     *
     * @throws TradeDataNotAvailableException if the depth is not available.
     */
    public Depth getDepth( CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	throw new NotYetImplementedException( "Getting the depth is not implemented in this base class, but in the actual exchange API implementations");
    }

    /**
     * Get the market depths for all supported currency pairs from the trade site.
     *
     * @return The current market depths for all supported currency pairs.
     *
     * @throws TradeDataNotAvailableException if the depth is not available.
     */
    public List<Depth> getDepths() throws TradeDataNotAvailableException {

	// This is just a default implementation, that should be overwritten with
	// an optimized version for a specific trade site, if possible.
	// At least some exchanges offer API methods to fetch multiple depths with
	// 1 request.

	// Just use the method to fetch the depths for a given list if currency pairs.
	return getDepths( getSupportedCurrencyPairs());
    }

    /**
     * Get the current market depths (minimal data of the orders) for a given list of currency pairs.
     * This is just a very slow default implementation. TradeSite implementations should overwrite
     * this with optimized implementations.
     *
     * @param currencyPairs The currency pairs to query.
     *
     * @return The current market depths for the given currency pairs.
     *
     * @throws TradeDataNotAvailableException if to many depths are not available.
     */
    public List<Depth> getDepths( CurrencyPair [] currencyPairs) throws TradeDataNotAvailableException {

	// If no proxies are allowed, do the requests sequentially.
	// Otherwise do parallel requests via proxies.
	return ( ( getProxyInfo() == null) || ! getProxyInfo().isProxyAllowed()) 
	    ? getDepthsSequentially( currencyPairs) 
	    : getDepthsViaProxies( currencyPairs);
    }
       
    /**
     * Get the current market depths (minimal data of the orders) for a given list of currency pairs.
     * This is just a very slow default implementation.
     *
     * @param currencyPairs The currency pairs to query.
     *
     * @return The current market depths for the given currency pairs.
     *
     * @throws TradeDataNotAvailableException if to many depths are not available.
     */
    public List<Depth> getDepthsSequentially( CurrencyPair [] currencyPairs) throws TradeDataNotAvailableException {

	// Precalculate the sleeping time.
	// sleep() works with miliseconds. The method returns microseconds,
	// so divide by 1000.
	long sleepInterval = getMinimumRequestInterval() / 1000 + 100;
	
	// Create an array for the result.
	List<Depth> result = new ArrayList<Depth>();
	int currentIndex = 0;
	
	// The number of requests, that failed in a row.
	int failsInRow = 0;
	    
	// Just loop over the pairs and request them one after the other.
	for( CurrencyPair currentPair : currencyPairs) {
	    
	    try {
		
		// Try to get the depth for this pair.
		result.add( currentIndex, getDepth( currentPair));
		
		// Reset the fails to 0.
		failsInRow = 0;
		    
	    } catch( TradeDataNotAvailableException tdnae) {

		// Fetching this pair did not work, so just set this depth to null.
		result.add( currentIndex, null);
		
		if( ++failsInRow >= MAX_FAILS_IN_ROW) {  // After 5 fails in a row, we give up
		                                         // , because the server is most likely down?
		    throw new TradeDataNotAvailableException( MAX_FAILS_IN_ROW 
							      + " failures while trying to fetch depths from " 
							      + _name
							      + ", so I give up.");
		}
	    }
		

	    // Wait until the exchange allows another request.
	    try {
		    
		Thread.sleep( sleepInterval);
		    
	    } catch( InterruptedException ie) {  // Return if the requests are interrupted.

		return result;  // Return, what we got so far.
	    }	    

	    ++currentIndex;
	}

	// Return the buffer.
	return result;
    }

   /**
     * Get the current market depths (minimal data of the orders) for a given list of currency pairs.
     * This implementation uses proxies, if they are available.
     *
     * @param currencyPairs The currency pairs to query.
     *
     * @return The current market depths for the given currency pairs.
     *
     * @throws TradeDataNotAvailableException if to many depths are not available.
     */
    public List<Depth> getDepthsViaProxies( CurrencyPair [] currencyPairs) throws TradeDataNotAvailableException {
	
	// The actual API must implement this method, since we don't know the URL etc.
	throw new NotYetImplementedException( "The API client " 
					      + _name 
					      + " seems to allow proxy requests, but does not implement getDepthsViaProxies.");
    }	

    /**
     * Get the fee for deposits as percent.
     *
     * @return The fee for deposits in percent.
     */
    /* public BigDecimal getFeeForDeposit() {
	return _feeForDeposit;
	} */

    /**
     * Get the fee for an order in the resulting currency. This is just a default implementation, that
     * could (and sometimes should) be overwritten in the mplementation for a specific trade site.
     * Synchronize this method, since several users might use this method with different
     * accounts and therefore different fees via a single API implementation instance.
     *
     * @param order The order to use for the fee computation.
     *
     * @return The fee in the resulting currency (currency value for buy, payment currency value for sell).
     */
    /*    public synchronized Price getFeeForOrder( SiteOrder order) {
	
	if( order instanceof WithdrawOrder) {  // If this is a withdrawal...

	    // Just multiply the withdraw fee with the amount, since the currency doesn't change.
	    return new Price( order.getAmount().multiply( order.getTradeSite().getFeeForWithdrawal()).divide( new BigDecimal( "100"), MathContext.DECIMAL128)
			      , order.getCurrencyPair().getCurrency());

	} else if( order instanceof DepositOrder) {  // If this is a deposit...

	    // Deposit works similar to withdrawal.
	    return new Price( order.getAmount().multiply( order.getTradeSite().getFeeForDeposit()).divide( new BigDecimal( "100"), MathContext.DECIMAL128)
			      , order.getCurrencyPair().getCurrency());
	    
	} else {  // This seems to be a regular trade (buy or sell).

	    // Trade is more complicated, since the currency changes in a sell.

	    if( order.getOrderType() == OrderType.BUY) {  // If this is a buy order

		return new Price( order.getAmount().multiply( order.getTradeSite().getFeeForTrade()).divide( new BigDecimal( "100"), MathContext.DECIMAL128)
				  , order.getCurrencyPair().getCurrency());

	    } else {  // this is a sell order, so the currency changes!

		return new Price( order.getAmount().multiply( order.getPrice()).multiply( order.getTradeSite().getFeeForTrade()).divide( new BigDecimal( "100"), MathContext.DECIMAL128)
				  , order.getCurrencyPair().getPaymentCurrency());
	    }
	}
	} */

    /**
     * Get the fee for trades in percent.
     *
     * @return The fee for trades in percent.
     */
    /* public BigDecimal getFeeForTrade() {
	return _feeForTrade;
	} */

    /**
     * Get the fee for a withdrawal in percent.
     *
     * @return The fee for a withdrawal in percent.
     */
    /* public BigDecimal getFeeForWithdrawal() {
	return _feeForWithdrawal;
	} */

    /**
     * Get the current log level.
     *
     * @return the current log level.
     */
    protected final int getLogLevel() {
	return this._logLevel;
    }

    /**
     * Get the shortest allowed requet interval in microseconds.
     * This is just a dummy implementation to compile the code. API implementations overwrite this method.
     *
     * @return The shortest allowed request interval in microseconds.
     */
    public long getMinimumRequestInterval() {

	throw new NotYetImplementedException( "Getting the minimum request interval is not implemented in the base class, but the actual API implementations should overwrite this method. See " + _name);
    }

    /**
     * Get the name of the trading site.
     *
     * @return The name of the trading site.
     */
    public String getName() {

	return _name;
    }

    /**
     * Get some info, if proxies are allowed for requests to this trade site.
     * I provide a default implementation here, so API implementations just have
     * to set the _proxyInfo variable by default.
     *
     * @return Info if proxies are allowed for requests to this trade site or null. Null means no proxies are allowed.
     */
    public TradeSiteProxyInfo getProxyInfo() {

	return _proxyInfo;   // Just return the _proxyInfo variables as the default.
    }
    
    /**
     * Get some of the settings of a trading site.
     *
     * @return Some of the settings of a trading site as a list.
     */
    public PersistentPropertyList getSettings() {

	// Create a new list for the settings.
	PersistentPropertyList result = new PersistentPropertyList();

	//result.add( new PersistentProperty( "Fee for deposit", null, "" + getFeeForDeposit(), 1));
	//result.add( new PersistentProperty( "Fee for trades", null, "" + getFeeForTrade(), 2));
	//result.add( new PersistentProperty( "Fee for withdrawal", null, "" + getFeeForWithdrawal(), 0));

	return result;
    }

    /**
     * Get the supported currency pairs of this trading site.
     *
     * @return The supported currency pairs of this trading site.
     */
    public CurrencyPair [] getSupportedCurrencyPairs() {
	return _supportedCurrencyPairs;
    }

    /**
     * Get the URL of the trading site.
     *
     * @return The URL of the trading site as a String object.
     */
    public String getURL() {
	return _url;
    }

    /**
     * Get the hashcode for this trade site.
     *
     * @return The hash code for this trade site.
     */
    public int hashCode() {

	// Just use the has code of the name.
	return getName().hashCode();
    }

    /**
     * Check, if a given currency pair is supported on this site.
     *
     * @param currencyPair The currency pair to check for this site.
     *
     * @return true, if the currency pair is supported. False otherwise.
     */
    public boolean isSupportedCurrencyPair( CurrencyPair currencyPair) {
	if( _supportedCurrencyPairs != null) {
	    for( int i = 0; i < _supportedCurrencyPairs.length; ++i) {
		if( currencyPair.equals( _supportedCurrencyPairs[ i])) {  // Is this currency pair in the list of
		    return true;  // supported currency pairs => return true.
		}
	    }
	}
	return false;  // Currency pair seems not supported here.
    }
    
    /**
     * Get the fee for deposits as percent.
     *
     * @param fee The fee for deposits in percent.
     */
    /*public void setFeeForDeposit( BigDecimal fee) {
	_feeForDeposit = fee;
	}*/

    /**
     * Get the fee for trades in percent.
     *
     * @param fee The fee for trades in percent.
     */
    /*public void setFeeForTrade( BigDecimal fee) {
	_feeForTrade = fee;
	}*/

    /**
     * Get the fee for a withdrawal in percent.
     *
     * @param fee The fee for a withdrawal in percent.
     */
    /*public void setFeeForWithdrawal( BigDecimal fee) {
	_feeForWithdrawal = fee;
	}*/

    /**
     * Set new settings for the trading site client.
     *
     * @param settings The new settings for the trading site client.
     */
    public void setSettings( PersistentPropertyList settings) {
	
	// Use 0 as the fee, if no fee was stored in the settings.

	/*String currentFee = settings.getStringProperty( "Fee for deposit");
	setFeeForDeposit( currentFee != null ?  new BigDecimal( currentFee) : BigDecimal.ZERO);
	currentFee = settings.getStringProperty( "Fee for trades");
	setFeeForTrade( currentFee != null ? new BigDecimal( currentFee) : BigDecimal.ZERO);
	currentFee = settings.getStringProperty( "Fee for withdrawal");
	setFeeForWithdrawal( currentFee != null ? new BigDecimal( currentFee) : BigDecimal.ZERO); */
    }
}
