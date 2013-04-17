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

import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.util.LogUtils;
import de.andreas_rueckert.util.TimeUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Abstract base class of an order book.
 */
public abstract class OrderBook {

    // Inner classes
    
    /**
     * Order monitor thread.
     */
    class OrderMonitoringThread extends Thread {

	// Instance variables


	// Constructors


	// Methods

	/**
	 * The actual code of the the thread.
	 */
	@Override public void run() {

	    while( _orderMonitoringThread != null) {

		// System.out.println( "Order monitor thread incomplete!!!");

		// Update all the current orders.
		updateOrders();

		// Collect all the used trade sites.
		List<TradeSite> currentTradeSites = new ArrayList<TradeSite>();
		
		for( Order currentOrder : _executedOrders.values()) {
		    if( currentOrder instanceof SiteOrder) {
			SiteOrder currentSiteOrder = (SiteOrder)currentOrder;
			
			TradeSite tradeSite = currentSiteOrder.getTradeSite();  // Get the trade site of the current order.

			if( ! currentTradeSites.contains( tradeSite)) {  // If this trade site is not already in the list of
			    currentTradeSites.add( tradeSite);           // currently used trade sites, add it to the list.
			}
		    }
		}
		
		// Now merge all the open order into a big map and remove all the executed orders, that are no longer in the map.
		// This way, we request the open orders from one trade site only once and not for each order from this trade site.
		// The data structure is a map, so we can separate the open orders for the trade sites and don't have to search
		// all the orders from all the trade sites for each order.
		Map<TradeSite, Collection<SiteOrder>> allOpenOrders = new HashMap< TradeSite, Collection<SiteOrder>>();
		for( TradeSite currentTradeSite : currentTradeSites) {
		    Collection<SiteOrder> openOrders = currentTradeSite.getOpenOrders();
		    if( openOrders != null) {                              // If the trade site returned the open orders,
			allOpenOrders.put( currentTradeSite, openOrders);  // add them to the list of open orders.
		    }
		}

		// Check and update all the orders.
		for( Order currentOrder : _executedOrders.values()) {
		    
		    if( currentOrder instanceof SiteOrder) {  // We can only find SiteOrder's here...

			// The following code is a hack, since an order might be aborted, or returned an error, or so.
			// But at the moment, I don't see a method in btc-e API to check for a specific order, like
			// the MtGox API supports it (Andreas Rueckert <a_rueckert@gmx.net>)...

			// Try to get all the open orders from the trade site of this site order.
			Collection<SiteOrder> openSiteOrders = allOpenOrders.get( ( (SiteOrder)currentOrder).getTradeSite());

			boolean orderIsStillOpen = false;  // Assume, that the order is filled.

			if( openSiteOrders != null) {  // If there are open orders.
			    for( SiteOrder currentSiteOrder : openSiteOrders) {  // Check all of them, if they are identical to our current order.
				// That has to be done, since the API usually creates new orders, so a '==' won't work.
				// equals might also not work, since the status of the order might have changed from UNKNOWN to PARTIALLY_FILLED, or so.

				if( currentSiteOrder.getSiteId().equals( ((SiteOrder)currentOrder).getSiteId())) {

				    orderIsStillOpen = true;  // This order is actually still open...
				}
			    }
			}
			
			if( ! orderIsStillOpen) {  // If the order is no longer open.
			    currentOrder.setStatus( OrderStatus.FILLED);  // Set the order status to filled (Ugly hack!)
			    _executedOrders.remove( currentOrder);        // Remove this order from the executed orders,
			    _completedOrders.put( currentOrder.getId(), currentOrder);  // and add it to the completed orders.
			}
		    }
		}

		// Now wait the maximum of the minimum request intervals of the used trade sites.
		long currentLoopInterval = 10000000L;  // Use 10s as the default...
		for( TradeSite currentTradeSite : currentTradeSites) {
		    long siteUpdateInterval = currentTradeSite.getUpdateInterval();

		    if( siteUpdateInterval > currentLoopInterval) {
			currentLoopInterval = siteUpdateInterval;
		    }
		}
		try {
		    Thread.sleep( currentLoopInterval / 1000);  // Thread.sleep expects milliseconds, not microseconds.
		} catch( InterruptedException ie) {
		    LogUtils.getInstance().getLogger().error( "OrderBook order monitoring interrupted while waiting for the next iteration: " + ie);
		}
	    }
	}

	/**
	 * Check all the orders and change their status eventually.
	 */
	void updateOrders() {
	    boolean continueChecks = true;  // Flag to indicate, that we have to do another loop of checking.

	    while( continueChecks) {  // While we have to continue checking.
		
		continueChecks = false;  // Assume, that we don't have to continue checks.

		// Loop over all the orders. Do not(!) use an iterator to avoid a concurrent modification exception!
		for( int orderIndex = 0; orderIndex < getOrders().values().size(); ++orderIndex) {

		    Order currentOrder = getOrders().get( orderIndex);

		    if( currentOrder.hasDependencies()) {  // If this order has dependencies..

			// Check, if some of them are filled and can be removed from the dependencies.
			for( int currentDependencyIndex = 0; currentDependencyIndex < currentOrder.getDependencies().size(); ) {

			    if( currentOrder.getDependencies().get( currentDependencyIndex).getStatus() == OrderStatus.FILLED) {
				
				currentOrder.getDependencies().remove( currentDependencyIndex);  // Remove this dependency

				continueChecks = true;  // Do another loop with checking.
			    } else {
				++currentDependencyIndex;
			    }
			}

			// If all the dependencies have been removed, execute the order now.
			if( ! currentOrder.hasDependencies()) {
			    executeOrder( currentOrder);
			}
		    }
		}
	    }
	}
    }

    
    // Static variables

    /**
     * The only OrderBook instance for now, until someone requires multiple order books.
     * (Singleton pattern for now.)
     */
    protected static OrderBook _instance = null;


    // Instance variables

    /**
     * The completed orders.
     */
    private HashMap< String, Order> _completedOrders;

    /**
     * The currently executed orders.
     */
    private HashMap< String, Order> _executedOrders;

    /**
     * The id for the next order.
     */
    private long _nextOrder;
 
    /**
     * The thread, that monitors and updates the orders.
     */
    private OrderMonitoringThread _orderMonitoringThread = null;

    /**
     * The current list of orders.
     */
    private HashMap< String, Order> _orders;


    // Constructors

    /**
     * Create a new order book.
     */
    protected OrderBook() {
	
	_orders = new HashMap< String, Order>();  // Create a list of new orders.

	_completedOrders = new HashMap< String, Order>();  // Create a list for the completed orders.

	_executedOrders = new HashMap< String, Order>();  // The list of currently executed orders.

	_nextOrder = TimeUtils.getInstance().getCurrentGMTTimeMicros();

	// Start the thread to monitor the orders.
	startOrderMonitoring();
    }


    // Methods

    /**
     * Add a new order.
     *
     * @param order The order to add.
     *
     * @return The id of the new order in the book.
     */
    public String add( Order order) {

	order.setId( "" + _nextOrder++);  // Set a new id and increase the next id.

	_orders.put( order.getId(), order);

	return order.getId();  // Return the id of the order.
    }

    /**
     * Check the status of the order with a given id.
     *
     * @param orderId The ID of the order to check.
     *
     * @return The current status of this order.
     */
    public OrderStatus checkOrder( String orderId) {

	return OrderStatus.UNKNOWN;  // Default is, that the status of an order is unknown.
    }

    /**
     * Get the list of currently executed orders.
     *
     * @return The list of currently executed orders.
     */
    public Collection<Order> getExecutedOrders() {
	return _executedOrders.values();
    }

    /**
     * Get the order with a given id (or null, if no such orders exist).
     *
     * @param id The id of the order.
     *
     * @return The order with this id or null, if no such order exists).
     */
    public Order getOrder( String id) {
	return _orders.get( id);
    }

    /**
     * Get an order from a given trade site and with a given site id.
     *
     * @param tradeSite The trade site, the order is for.
     * @param siteId The id of the order on the given trade site.
     *
     * @return The first matching order, or null if no matching order was found.
     */
    public SiteOrder getOrder( TradeSite tradeSite, String siteId) {
	
	for( Iterator<Order> iterator = _orders.values().iterator(); iterator.hasNext(); ) {
	    Order currentOrder = iterator.next();

	    if( currentOrder instanceof SiteOrder) {  // If this order is for a trade site.
		SiteOrder currentSiteOrder = (SiteOrder)currentOrder;
		
		// If this order is for the given trade site, and
		// the site id matches the given id,
		if( ( currentSiteOrder.getTradeSite() == tradeSite) 
		    && siteId.equals( currentSiteOrder.getSiteId())) {
		    
		    return currentSiteOrder;  // return this matching order.
		}
	    }
	}
	
	return null;  // No matching order found.
    }

    /**
     * Get all the orders for a given trade site.
     *
     * @param tradeSite The trade site, that the orders are for.
     *
     * @return An array with the orders (potentially empty, if there are no orders for this trade site).
     */
    public SiteOrder [] getOrders( TradeSite tradeSite) {
	ArrayList<SiteOrder> result = new ArrayList<SiteOrder>();

	for( Iterator<Order> iterator = _orders.values().iterator(); iterator.hasNext(); ) {
	    Order currentOrder = iterator.next();

	    if( currentOrder instanceof SiteOrder) {  // If this order is for a trade site.
		SiteOrder currentSiteOrder = (SiteOrder)currentOrder;

		// If this order is for the given trade site.
		if( currentSiteOrder.getTradeSite() == tradeSite) {
		    result.add( currentSiteOrder);
		}
	    }
	}

	// Convert the result to an array.
	return result.toArray( new SiteOrder[ result.size()]);
    }

    /**
     * Execute the order with the given id.
     *
     * @param orderId The id of the order to execute.
     *
     * @return The new status of the order.
     */
    public OrderStatus executeOrder( String orderId) {
	return executeOrder( getOrder( orderId));
    }

    /**
     * Execute a given order.
     *
     * @param orderToExecute The order to execute.
     *
     * @return The new status of the order.
     */
    public OrderStatus executeOrder( Order orderToExecute) {

	if( orderToExecute != null) {  // Does this order exists in the orderbook?
	    if( orderToExecute instanceof SiteOrder) {  // Is this an order for a tradesite?

		SiteOrder siteOrderToExecute = (SiteOrder)orderToExecute;

		TradeSite tradeSite = siteOrderToExecute.getTradeSite();

		if( tradeSite != null) {

		    OrderStatus newStatus = tradeSite.executeOrder( siteOrderToExecute); // If the order could be executed,

		    // Log the executed order.
		    LogUtils.getInstance().getLogger().info( "Executed order " + siteOrderToExecute.toString());

		    // Check, if the trade site set the site id, so we can check the order status later.
		    if( siteOrderToExecute.getSiteId() == null) {
			LogUtils.getInstance().getLogger().error( "OrderBook.executeOrder: TradeSite did not set siteId when executing order");
		    }

		    // Check, if the order has a status now.
		    if( siteOrderToExecute.getStatus() == null) {
			LogUtils.getInstance().getLogger().error( "OrderBook.executeOrder: TradeSite did not set a status when executing order");
		    }

		    return newStatus;  // Return the new status of the order.

		}
	    }
	}

	return null;
    }

    /**
     * Get a list of the orders.
     *
     * @return a map of the orders.
     */
    public Map< String, Order> getOrders() {
	return _orders;
    }

    /**
     * Check, if an order is completed.
     *
     * @param orderId The ID of the order.
     *
     * @return true, if the order is completed. false otherwise.
     */
    public final boolean isCompleted( String orderId) {

	// Just check, if this order is in the list of completed orders.
	return ( _completedOrders.get( orderId) != null);
    }

    /**
     * Start the order monitoring.
     */
    private final void startOrderMonitoring() {
	_orderMonitoringThread = new OrderMonitoringThread();
	_orderMonitoringThread.start();
    }

    /**
     * Stop the order monitoring thread.
     */
    private final synchronized void stopOrderMonitoring() {

	// Copy the thread reference, so we can delete the instance var.
	Thread orderMonitoringThread = _orderMonitoringThread;

	_orderMonitoringThread = null;  // Indicate the thread to stop now.

	try {
	    orderMonitoringThread.join();  // Wait for the thread to stop.
	} catch( InterruptedException ie) {
	    LogUtils.getInstance().getLogger().error( "Error while stopping the order monitoring: " + ie);
	}
    }
}