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

package de.andreas_rueckert.trade.order;

import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import java.math.BigDecimal;


/**
 * This is the base class for all orders on trade sites.
 */
public class SiteOrderImpl extends OrderImpl implements SiteOrder {

    // Static variables

    
    // Instance variables

    /**
     * The id of this order on the trading site.
     */
    private String _siteId;

    /**
     * The site, where this order will be placed.
     */
    private TradeSite _tradeSite;


    // Constructors

    /**
     * Create a new SiteOrder object.
     *
     * @param TradeSite tradeSite The site to trade on.
     * @param orderType The type of the order (buy or sell).
     * @param price The price of the order.
     * @param currencyPair The currency pair that is used for this order.
     * @param amount The amount of the traded good.
     */
    public SiteOrderImpl( TradeSite tradeSite, OrderType orderType, Price price, CurrencyPair currencyPair, Amount amount) {
	super( orderType, price, currencyPair, amount);

	_tradeSite = tradeSite;
    }


    // Methods

    /**
     * Get the id of the order at the targeted trading site.
     *
     * @return The id of this order at the targeted trading site.
     */
    public String getSiteId() {
	return _siteId;
    }

    /**
     * Get the trade site, where this order will be placed.
     *
     * @return The trade site, where this order will be placed.
     */
    public TradeSite getTradeSite() {
	return _tradeSite;
    }

    /**
     * Set a new site id for this order.
     *
     * @param siteId The new site id for this order.
     */
    public void setSiteId( String siteId) {
	_siteId = siteId;
    }

    /**
     * Convert this order to a string for logging purposes.
     *
     * @return This order as a string.
     */
    public String toString() {
	StringBuffer resultBuffer = new StringBuffer();

	resultBuffer.append( getOrderType() == OrderType.BUY ? "buy" : "sell");
	resultBuffer.append( " order with id ");
	resultBuffer.append( getId());
	resultBuffer.append( " , amount of ");
	resultBuffer.append( getAmount().toPlainString());
	resultBuffer.append( " and a price of ");
	resultBuffer.append( getPrice().toPlainString());

	// ToDo: add the current status to the info?

	return resultBuffer.toString();  // Convert the StringBuffer to a String object and return it.
    }
}