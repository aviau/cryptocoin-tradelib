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

package de.andreas_rueckert.trade.site.request;

import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.util.TimeUtils;
import java.util.LinkedList;


/**
 * This class handles the proxies for 1 given trade site.
 */
class TradeSiteProxyList {

    // Inner classes

    /**
     * The data of a proxy, that was returned to query a given
     * trade site.
     */
    class RecommendedProxy {

	// Instance variables

	/**
	 * The returned proxy.
	 */
	private RatedProxy _proxy = null;

	/**
	 * The microsecond timestamp, when this proxy was returned
	 * to the API client.
	 */
	private long _timestamp = -1L;
	
	
	// Constructors

	/**
	 * Create a new recommendation for a proxy to use.
	 *
	 * @param proxy The returned proxy.
	 */
	public RecommendedProxy( RatedProxy proxy) {

	    // Store the data in the instance
	    _proxy = proxy;

	    // Add the current timestamp.
	    _timestamp = TimeUtils.getInstance().getCurrentGMTTimeMicros();
	}


	// Methods

	/**
	 * Get the returned proxy.
	 *
	 * @return The proxy, that was returned.
	 */
	public RatedProxy getProxy() {

	    return _proxy;
	}

	/**
	 * Get the timestamp of this proxy return.
	 *
	 * @return The timestamp, when this proxy was recommended.
	 */
	public long getTimestamp() {

	    return _timestamp;
	}

	/**
	 * If the recommendation is older than the current time - minimum request interval of the exchange,
	 * we can use this proxy for another request to this exchange.
	 * So we can remove this recommendation from the log of recent recommendations.
	 *
	 * @return true, if the recommendation could be removed. False otherwise.
	 */
	public boolean isOutdated() {
	    
	    // This is not a very efficient implementation (because the GMT time is requested for each entry as an
	    // example), but a very clean one.
	    // So I leave this for further optimization. :-)
	    return ( ( getTimestamp() + getTradeSite().getMinimumRequestInterval()) < TimeUtils.getInstance().getCurrentGMTTimeMicros());
	}
    }


    // Static variables
    

    // Instance variables

    /**
     * The FIFO queue for the available proxies.
     */
    private LinkedList<RatedProxy> _availableProxies = new LinkedList<RatedProxy>();

    /**
     * The FIFO queue for the returned proxies, that where hopefully used.
     */
    private LinkedList<RecommendedProxy> _recommendedProxies = new LinkedList<RecommendedProxy>();

    /**
     * The trade site, that we want to connect to.
     */
    private TradeSite _tradeSite = null;


    // Constructors

    /**
     * Create a new proxy list for a trade site.
     *
     * @param tradeSite The trade site, we want to connect to.
     */
    public TradeSiteProxyList( TradeSite tradeSite) {

	_tradeSite = tradeSite;  // Store the trade site in the instance.
    }


    // Methods

    /**
     * Add an available proxy.
     *
     * @param newProxy The proxy to add.
     */
    public void addProxy( RatedProxy newProxy) {

	// If the new proxy is not already in the list of available proxies,
	// add it to the list.
	// ToDo: this code will only work, if the proxy was not already used
	// and moved to the recommended proxies. But since we also check
	// against the complete list of the handler, this should work 99% of 
	// the time (hopefully).
	if( ! _availableProxies.contains( newProxy)) {

	    // Add the new proxy to the list of available proxies.
	    _availableProxies.add( newProxy);
	}
    }

    /**
     * Remove logs from recommendations, that are older than the minimum request
     * interval, so we can reuse those proxies again with no delay.
     */
    private synchronized void cleanRecommendationList() {

	// While there are proxies, that were already recommended, clean their logs.
	while( ! _recommendedProxies.isEmpty() &&  _recommendedProxies.getFirst().isOutdated()) {  // While the oldest elements are outdated,
		
	    RatedProxy proxy =_recommendedProxies.removeFirst().getProxy();  // Get the proxy from this recommendation.
	    
	    if( proxy.isActive()) {  // If this proxy is still active,
		
		_availableProxies.add( proxy);  // add this proxy to the list of available proxies.
	    }
	}
    }

    /**
     * Get the trade site of this proxy list.
     *
     * @return The trade site of this proxy list.
     */
    final TradeSite getTradeSite() {

	return _tradeSite;
    }

    /**
     * Try to get a proxy for a trade site request.
     *
     * @return A proxy for the next trade site request.
     *
     * @throws NoProxyAvailableException If there's no proxy available for the request.
     */
    public synchronized RatedProxy recommendProxy() throws NoProxyAvailableException {

	RatedProxy proxy;  // Buffer for the result.

	while( true) {  // While we have not given up...
	
	    // If there are new proxies available, get the next proxy from there.
	    if( ! _availableProxies.isEmpty()) {

		// Get the first of the available proxies.
		proxy = _availableProxies.removeFirst();

		if( proxy.isActive()) {  // If this proxy is active,

		    // Add a new recommendation to our internal list.
		    _recommendedProxies.add( new RecommendedProxy( proxy));

		    // And return this proxy.
		    return proxy;

		} else {  // If the proxy is not active at the moment, it is just removed and might be re-added later, once it's activated again...

		    continue;  // Try to get another active proxy from the available proxies.
		}

	    } else {  // There are no new proxies, so check out earlier recommendations, if those
		      // proxies are available again with no delay.
	    
		// Remove those recommendation, that are older than the minimum request interval of the trade site.
		cleanRecommendationList();  

		// Now recheck, if there are new proxies available again.
		if( ! _availableProxies.isEmpty()) {

		    // Yes! => try to find an available proxy again.
		    continue;

		} else { // We have to reuse a proxy, that was used recently... :-( 

		    // If there are proxies in the recommendation list.
		    while( ! _recommendedProxies.isEmpty()) {

			// Get the proxy, that was recommended first and reuse it.
			proxy = _recommendedProxies.removeFirst().getProxy();

			if( ! proxy.isActive()) {  // Should rarely happen, so it's ok, even if it's not very efficient...

			    continue;

			} else {
	
			    // Add a new recommendation to our internal list.
			    _recommendedProxies.add( new RecommendedProxy( proxy));

			    return proxy;  // Return the fetched proxy.
			}
		    }

		    // If no proxy was available so far, we have to throw an exception to indicate that.
		    throw new NoProxyAvailableException( "no proxy available in TradeSiteProxyList.recommendProxy()");
		}
	    }
	}
    }
}