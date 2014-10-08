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
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.util.TimeUtils;
import java.util.ArrayList;
import java.util.List;


/** 
 * Instances of this class represent an order.
 */
public class OrderImpl extends DepthOrderImpl implements Order {

    // Static variables


    // Instance variables

    /**
     * All the orders, this order depends on. This order is only
     * executed, when all the dependencies are filled.
     */
    private List<Order> _dependencies = null;

    /**
     * The ID of this order.
     */
    private String _id;

    /**
     * Status of the order (active or not enough funds).
     */
    private OrderStatus _orderStatus  = null;

    /**
     * The timestamp, this order was created.
     */
    private long _timestamp;


    // Constructors

    /**
     * Create a new Order object.
     *
     * @param orderType The type of the order (buy or sell).
     * @param price The price of the order.
     * @param currencyPair The currency pair that is used for this order.
     * @param amount The amount of the traded good.
     */
    public OrderImpl(  OrderType orderType, Price price, CurrencyPair currencyPair, Amount amount) {
	super( orderType, price, currencyPair, amount);

	// Set the timestamp to the current time in microseconds.
	_timestamp = TimeUtils.getInstance().getCurrentGMTTimeMicros();
    }

    
    // Methods

    /**
     * Add an order, that this order depends on.
     *
     * @param dependendOrder The order, this order depends on.
     */
    public void addDependency( Order dependendOrder) {
	getDependencies().add( dependendOrder);  // Add the order to the dependencies.
    }

    /**
     * Get all the orders, this order depends on. So this order will only be executed, if all
     * the depencies are filled.
     *
     * @return Sll the orders, this order depends on.
     */
    public List<Order> getDependencies() {
	
	if( _dependencies == null) {                  // If there are no dependencies yet, 
	    _dependencies = new ArrayList<Order>();  // create an empty list of denpendencies.
	}

	return _dependencies;
    }

    /**
     * Get the id of this order.
     *
     * @return The id of this order.
     */
    public String getId() {
	return _id;
    }

    /**
     * Get the status of this order.
     *
     * @return The status of this order.
     */
    public OrderStatus getStatus() {
	return _orderStatus;
    }

    /**
     * Get the GMT relative timestamp, this order was created on.
     *
     * @return The epoch (GMT-relative) this order was created on.
     */
    public long getTimestamp() {
	return _timestamp;
    }

    /**
     * Check, if this order depends on other orders.
     *
     * @return true, if this order depends on other orders. False otherwise.
     */
    public boolean hasDependencies() {
	return ( getDependencies().size() > 0);  // Check, if the list of dependencies contains any elements.
    }

    /**
     * Set a new id for this order.
     *
     * @param id The new id for this order.
     */
    public void setId( String id) {
	_id = id;
    }

    /**
     * Set a new status for this order.
     *
     * @param status The new status of this order.
     */
    public void setStatus( OrderStatus status) {
	_orderStatus = status;
    }
}
