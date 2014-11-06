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

import java.util.List;


/** 
 * Interface for any kinda of order.
 */
public interface Order extends DepthOrder {

    // Static variables


    // Instance variables


    // Constructors

    
    // Methods
    
    /**
     * Add an order, that this order depends on.
     *
     * @param dependency The order, that this order depends on.
     */
    public void addDependency( Order dependency);

    /**
     * Get the id of the order in our trading app.
     * This is a String at the moment, since it might make
     * sense to use characters to identify trading sites (i.e. M<number> to identify a MtGox order).
     *
     * @return The id of the order in the local trading system.
     */
    public String getId();
    
    /**
     * Get all the orders, this order depends on. So this order will only be executed, if all
     * the depencies are filled.
     *
     * @return The id's of all the orders, this order depends on.
     */
    public List<Order> getDependencies();

    /**
     * Get the status of this order.
     *
     * @return The status of this order.
     */
    public OrderStatus getStatus();

    /**
     * Get the GMT relative timestamp, this order was created on.
     *
     * @return The epoch (GMT-relative) this order was created on.
     */
    public long getTimestamp();
    
    /**
     * Check, if this order depends on other orders.
     *
     * @return true, if this order depends on other orders. False otherwise.
     */
    public boolean hasDependencies();

    /**
     * Set a new id for this order.
     *
     * @param id The new id for this order.
     */
    public void setId( String id);

    /**
     * Set a new status for this order.
     *
     * @param status The new status of this order.
     */
    public void setStatus( OrderStatus status);

    /**
     * Convert this order to a string (for logging purposes as an example). It should be an actual readable representation of this
     * order and not just a serialized class. That's why this method is declared here.
     *
     * @return This order as a string.
     */
    public String toString();
}