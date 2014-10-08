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
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.Price;


/**
 * Implementation of a depth order.
 */
public class DepthOrderImpl implements DepthOrder {

    // Static variables

    
    // Instance variables

    /**
     * The amount of the traded good.
     */
    private Amount _amount;

    /**
     * The currency, that is used for this order.
     */
    private CurrencyPair _currencyPair;

    /**
     * The type of the order (buy / sell).
     */
    private OrderType _orderType;

    /**
     * The price, that was used for the order.
     */
    private Price _price;


    // Constructors

    /**
     * Create a new DepthOrder object.
     *
     * @param orderType The type of the order (buy or sell).
     * @param price The price of the order.
     * @param currency The currency that is used for the order.
     * @param amount The amount of the traded good.
     */
    public DepthOrderImpl( OrderType orderType, Price price, CurrencyPair currencyPair, Amount amount) {
	_orderType = orderType;
	_price = price;
	_currencyPair = currencyPair;
	_amount = amount;
    }


    // Methods
    
    /**
     * Compare 2 depth order to sort the depth from
     * best price to worst price.
     *
     * @param order2 The other DepthOrder to compare to.
     */
    public int compareTo( DepthOrder order2) {
	return _orderType == OrderType.SELL ? getPrice().compareTo( order2.getPrice()): order2.getPrice().compareTo( getPrice());
    }

    /**
     * Get the amount of the traded good/currency.
     *
     * @return The traded amount.
     */
    public Amount getAmount() {
	return _amount;
    }

    /**
     * Get the currency pair, that is used for this order.
     *
     * @return The currency pair, that was used for this order.
     */
    public CurrencyPair getCurrencyPair() {
	return _currencyPair;
    }

    /**
     * Get the type of this order (buy/sell/meta).
     *
     * @return The type of this order.
     */
    public OrderType getOrderType() {
	return _orderType;
    }

    /**
     * Get the price for this order.
     *
     * @return The price for this order.
     */
    public Price getPrice() {
	return _price;
    }			   
}
