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

package de.andreas_rueckert.trade;

import de.andreas_rueckert.trade.order.DepthOrder;
import de.andreas_rueckert.trade.site.TradeSite;
import java.util.List;


/**
 * Interface for the market depth.
 */
public interface Depth {

    // Static variables


    // Methods

    /**
     * Get a buy order with a given index.
     *
     * @return The DepthOrder with the given index.
     *
     * @throws TradeDataNotAvailableException if the order with the given index is not in the list of orders.
     */
    public DepthOrder getBuy( int index) throws TradeDataNotAvailableException;

    /**
     * Get the buy orders as a list of DepthOrder objects.
     *
     * @return The buy orders as a list of DepthOrder objects.
     */
    public List<DepthOrder> getBuyOrders();
    
    /**
     * Get the number of buy orders.
     *
     * @return The number of buy orders.
     */
    public int getBuySize();

    /**
     * Get the currency pair, that is used for this depth.
     *
     * @return The currency pair, that is used for this depth.
     */
    public CurrencyPair getCurrencyPair();

    /**
     * Get a sell order with a given index.
     *
     * @return The sell order with the given index.
     *
     * @throws TradeDataNotAvailableException if the order with the given index is not in the list of orders.
     */
    public DepthOrder getSell( int index) throws TradeDataNotAvailableException ;

    /**
     * Get the sell orders as a list of DepthOrder objects.
     *
     * @return The sell orders as a list of DepthOrder objects.
     */
    public List<DepthOrder> getSellOrders();

    /**
     * Get the number of sell orders.
     *
     * @return The number of sell orders.
     */
    public int getSellSize();
    
    /**
     * Get the timestamp, when this depth object was created (received from the trade site,
     *
     * @return The timestamp, when this Depth object was created (received from the trade site).
     */
    public long getTimestamp();

    /**
     * Get the trade site, this depth belongs to.
     *
     * @return The trade site, thisn depth belongs to.
     */
    public TradeSite getTradeSite();
}