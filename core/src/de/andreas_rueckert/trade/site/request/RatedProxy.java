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
import de.andreas_rueckert.util.LogUtils;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;


/**
 * This class holds the info on one proxy server.
 */
public class RatedProxy extends Proxy {

    // Static variables

    /**
     * The maximum rating.
     */
    public static final int MAX_RATING = 10;

    /**
     * The minimum rating.
     */
    public static final int MIN_RATING = -10;


    // Instance variables

    /**
     * Flag to indicate, if this proxy is active at the moment. Proxies might disappear after
     * a while, but might get active at some point again.
     */
    private boolean _active = true;

    /**
     * The timestamp of the last request of this proxy to a given trade site. -1 if we had no request with this proxy yet.
     */
    private Map< TradeSite, Long> _lastRequests = new HashMap< TradeSite, Long>();

    /**
     * The rating for this proxy server (0 = neutral, > 0 positive).
     * This is a combined rating for performance and reliability.
     */
    private int _rating = 0;


    // Constructors

    /**
     * Create a new proxy instance for requests.
     *
     * @param type The type of the proxy (socks, http, transparent).
     * @param socketAddress The IP address and port of the proxy.
     */
    public RatedProxy( Proxy.Type type, InetSocketAddress socketAddress) {

	super( type, socketAddress);
    }


    // Methods

    /**
     * Activate this proxy.
     */
    public final void activate() {

	setActive( true);
    }

    /**
     * Deactivate this proxy.
     */
    public final void deactivate() {

	setActive( false);
    }

    /**
     * Check if 2 proxies are identical.
     *
     * @param proxy Another proxy.
     *
     * @return true, if the 2 proxies are the same. False otherwise.
     */
    public boolean equals( RatedProxy proxy) {

	// Just compare the 2 socket addresses.
	return address().equals( proxy.address());
    }

    /**
     * Get the timestamp of the last request.
     *
     * @param tradeSite The trade site we want to check for requests.
     *
     * @return The timestamp of the last request or null, if there was no previous request.
     */
    public Long getLastRequestTimestamp( TradeSite tradeSite) {

	return _lastRequests.get( tradeSite);
    }

    /**
     * Get the rating of this proxy server.
     *
     * @return The rating of this proxy server.
     */
    public int getRating() {

	return _rating;
    }

    /**
     * Check, if this proxy is active at the moment.
     *
     * @return true, if this proxy seems to be active at the moment.
     */
    public final boolean isActive() {

	return _active;
    }

    /**
     * Give this proxy a worse rating.
     */
    public final void rateDown() {

	if( --_rating < -10) {  // -10 should be the worst rating possible.

	    _rating = -10;
	}
    }

    /**
     * Give this proxy a better rating.
     */
    public final void rateUp() {

	if( ++_rating > 10) {  // 10 should be the best possible rating.

	    _rating = 10;
	}
    }

    /**
     * Remove the last request timestamp for a given tradesite.
     *
     * @param tradeSite The trade site, we want to remove the last request timestamp for.
     */
    public void removeLastRequestTimestamp( TradeSite tradeSite) {

	_lastRequests.remove( tradeSite);
    }

    /**
     * Set a new activated status for this proxy.
     *
     * @param active The new active status for this proxy.
     */
    public final void setActive( boolean active) {

	_active = active;
    }

    /**
     * Set a new rating for this proxy server.
     *
     * @param rating The new rating for this server.
     */
    public void setRating( int rating) {

	_rating = rating;
    }
}