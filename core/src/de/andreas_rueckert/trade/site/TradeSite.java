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

import de.andreas_rueckert.persistence.PersistentProperties;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.CryptoCoinTrade;
import de.andreas_rueckert.trade.Currency;
import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.OrderStatus;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.Ticker;
import de.andreas_rueckert.trade.TradeDataNotAvailableException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;


/**
 * An API for trade sites.
 */
public interface TradeSite extends PersistentProperties {

    // Methods

    /**
     * Cancel an order on the trade site.
     *
     * @param order The order to cancel.
     *
     * @return true, if the order was canceled. False otherwise.
     */
    public boolean cancelOrder( SiteOrder order);

    /**
     * Execute an order on the trade site.
     *
     * @param order The order to execute.
     *
     * @return The new status of the order.
     */
    public OrderStatus executeOrder( SiteOrder order);

    /**
     * Get the accounts of the user on this trading site.
     *
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The accounts of the user on this trading site as a list of Account objects or null if no accounts were found.
     */
    public Collection<TradeSiteAccount> getAccounts( TradeSiteUserAccount userAccount);

    /**
     * Get the current market depth (minimal data of the orders).
     *
     * @param currencyPair The currency pair to query.
     *
     * @return The current market depth.
     *
     * @throws TradeDataNotAvailableException if the depth is not available.
     */
    public Depth getDepth( CurrencyPair currencyPair) throws TradeDataNotAvailableException;

    /**
     * Get the fee for deposits as percent.
     *
     * @return The fee for deposits in percent.
     */
    public BigDecimal getFeeForDeposit();

    /**
     * Get the fee for an order in the resulting currency.
     *
     * @param order The order to use for the fee computation.
     *
     * @return The fee in the resulting currency (currency value for buy, payment currency value for sell).
     */
    public Price getFeeForOrder( SiteOrder order);

    /**
     * Get the fee for trades in percent.
     *
     * @return The fee for trades in percent.
     */
    public BigDecimal getFeeForTrade();

    /**
     * Get the fee for a withdrawal in percent.
     *
     * @return The fee for a withdrawal in percent.
     */
    public BigDecimal getFeeForWithdrawal();
    
    /**
     * Get the shortest allowed requet interval in microseconds.
     *
     * @return The shortest allowed request interval in microseconds.
     */
    public long getMinimumRequestInterval();

    /**
     * Get the name of the trading site.
     *
     * @return The name of the trading site.
     */
    public String getName();

    /**
     * Get the open orders on this trade site.
     *
     * @param userAccount The account of the user on the exchange.
     *
     * @return The open orders as a collection.
     */
    public Collection<SiteOrder> getOpenOrders( TradeSiteUserAccount userAcount);

    /**
     * Get the supported currency pairs of this trading site.
     *
     * @return The supported currency pairs of this trading site.
     */
    public CurrencyPair [] getSupportedCurrencyPairs();

    /**
     * Get a current bitcoin ticker from a trade site.
     *
     * @param currencyPair The currency pair to query.
     *
     * @return The bitcoin value in the passed currency or null, of the site cannot deliver this ticker.
     *
     * @throws TradeDataNotAvailableException if the ticker is not available.
     */
    public Ticker getTicker( CurrencyPair currencyPair) throws TradeDataNotAvailableException;

    /**
     * Get a list of trades from the trading site.
     *
     * @param since_micros The GMT-relative epoch in microseconds.
     * @param currencyPair The currency pair to use for the trades.
     *
     * @return The trades as an array.
     *
     * @throws TradeDataNotAvailableException if the depth is not available.
     */
    public CryptoCoinTrade [] getTrades( long since_micros, CurrencyPair currencyPair) throws TradeDataNotAvailableException;

    /**
     * Get the interval, in which the trade site updates it's depth, ticker etc. 
     * in microseconds.
     *
     * @return The update interval in microseconds.
     */
    public long getUpdateInterval();

    /**
     * Get the URL of the trading site.
     *
     * @return The URL of the trading site as a String object.
     */
    public String getURL();

    /**
     * Check, if some request type is allowed at the moment. Most
     * trade site have limits on the number of request per time interval.
     *
     * @param requestType The type of request (trades, depth, ticker, order etc).
     *
     * @return true, if the given type of request is possible at the moment.
     */
    public boolean isRequestAllowed( TradeSiteRequestType requestType);

    /**
     * Check, if a given currency pair is supported on this site.
     *
     * @param currencyPair The currency pair to check for this site.
     *
     * @return true, if the currency pair is supported. False otherwise.
     */
    public boolean isSupportedCurrencyPair( CurrencyPair currencyPair);

    /**
     * Get the fee for deposits as percent.
     *
     * @param fee The fee for deposits in percent.
     */
    public void setFeeForDeposit( BigDecimal fee);

    /**
     * Get the fee for trades in percent.
     *
     * @param fee The fee for trades in percent.
     */
    public void setFeeForTrade( BigDecimal fee);

    /**
     * Get the fee for a withdrawal in percent.
     *
     * @param fee The fee for a withdrawal in percent.
     */
    public void setFeeForWithdrawal( BigDecimal fee);

    /**
     * Get a string for the trading site. This is just used to display
     * a name for the node in the JTree, so we don't need a cell renderer.
     */
    public String toString();
}
