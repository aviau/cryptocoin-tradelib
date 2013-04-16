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


/**
 * Interface for an order in the market depth.
 * The depth should be sorted from lowest to highest price,
 * so make an order comparable.
 */
public interface DepthOrder extends Comparable<DepthOrder> {

    // Static variables


    // Instance variables


    // Methods

    /**
     * Get the amount of the traded good/currency.
     *
     * @return The traded amount.
     */
    public Amount getAmount();

    /**
     * Get the currency pair, that was used for the ordered good.
     *
     * @return The currency pair, that was used for the ordered good.
     */
    public CurrencyPair getCurrencyPair();
    
    /**
     * Get the type of this order (buy/sell/meta).
     *
     * @return The type of this order.
     */
    public OrderType getOrderType();

    /**
     * Get the price for this order.
     *
     * @return The price for this order.
     */
    public Price getPrice();
}