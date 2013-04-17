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

package de.andreas_rueckert.trade.site;

import de.andreas_rueckert.persistence.PersistentProperty;
import de.andreas_rueckert.persistence.PersistentPropertyList;
import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.order.DepositOrder;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.order.WithdrawOrder;
import de.andreas_rueckert.trade.Price;
import java.math.BigDecimal;
import java.math.MathContext;
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
    
    
    // Instance variables

    /**
     * The fee for deposits.
     */
    protected BigDecimal _feeForDeposit = BigDecimal.ZERO;  // Set a default value;

    /**
     * The fee for trades.
     */
    protected BigDecimal _feeForTrade = BigDecimal.ZERO;  // Set a default value.

    /**
     * The fee for withdrawals.
     */
    protected BigDecimal _feeForWithdrawal = BigDecimal.ZERO;  // Set a default value.

    /**
     * The current log level.
     */
    protected int _logLevel = LOGLEVEL_ERROR;

    /**
     * The name of the trading site.
     */
    protected String _name;

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
     * Get the fee for deposits as percent.
     *
     * @return The fee for deposits in percent.
     */
    public BigDecimal getFeeForDeposit() {
	return _feeForDeposit;
    }

    /**
     * Get the fee for an order in the resulting currency. This is just a default implementation, that
     * could (and sometimes should) be overwritten in the mplementation for a specific trade site.
     *
     * @param order The order to use for the fee computation.
     *
     * @return The fee in the resulting currency (currency value for buy, payment currency value for sell).
     */
    public Price getFeeForOrder( SiteOrder order) {
	
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
    }

    /**
     * Get the fee for trades in percent.
     *
     * @return The fee for trades in percent.
     */
    public BigDecimal getFeeForTrade() {
	return _feeForTrade;
    }

    /**
     * Get the fee for a withdrawal in percent.
     *
     * @return The fee for a withdrawal in percent.
     */
    public BigDecimal getFeeForWithdrawal() {
	return _feeForWithdrawal;
    }

    /**
     * Get the current log level.
     *
     * @return the current log level.
     */
    protected final int getLogLevel() {
	return this._logLevel;
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
     * Get some of the settings of a trading site.
     *
     * @return Some of the settings of a trading site as a list.
     */
    public PersistentPropertyList getSettings() {

	// Create a new list for the settings.
	PersistentPropertyList result = new PersistentPropertyList();

	result.add( new PersistentProperty( "Fee for deposit", null, "" + getFeeForDeposit(), 1));
	result.add( new PersistentProperty( "Fee for trades", null, "" + getFeeForTrade(), 2));
	result.add( new PersistentProperty( "Fee for withdrawal", null, "" + getFeeForWithdrawal(), 0));

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
    public void setFeeForDeposit( BigDecimal fee) {
	_feeForDeposit = fee;
    }

    /**
     * Get the fee for trades in percent.
     *
     * @param fee The fee for trades in percent.
     */
    public void setFeeForTrade( BigDecimal fee) {
	_feeForTrade = fee;
    }

    /**
     * Get the fee for a withdrawal in percent.
     *
     * @param fee The fee for a withdrawal in percent.
     */
    public void setFeeForWithdrawal( BigDecimal fee) {
	_feeForWithdrawal = fee;
    }

    /**
     * Set new settings for the trading site client.
     *
     * @param settings The new settings for the trading site client.
     */
    public void setSettings( PersistentPropertyList settings) {
	
	// Use 0 as the fee, if no fee was stored in the settings.

	String currentFee = settings.getStringProperty( "Fee for deposit");
	setFeeForDeposit( currentFee != null ?  new BigDecimal( currentFee) : BigDecimal.ZERO);
	currentFee = settings.getStringProperty( "Fee for trades");
	setFeeForTrade( currentFee != null ? new BigDecimal( currentFee) : BigDecimal.ZERO);
	currentFee = settings.getStringProperty( "Fee for withdrawal");
	setFeeForWithdrawal( currentFee != null ? new BigDecimal( currentFee) : BigDecimal.ZERO);
    }
}
