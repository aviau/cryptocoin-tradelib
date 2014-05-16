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
import java.util.ArrayList;


/**
 * This class holds the info on proxy requests to one tradesite.
 */
public class TradeSiteProxyInfo {

    // Static variables

    /**
     * The maximum number of parallel proxy requests to a website.
     */
    private static final int MAX_PARALLEL_PROXY_REQUESTS = 60;

    /**
     * The minimum timespan between 2 proxy request to a trade site.
     */
    private static final long MIN_TIME_BETWEEN_PROXY_REQUESTS = 100000L;


    // Instance variables
    
    /**
     * The maximum number of simultaneous proxy requests.
     */
    private int _maxNumberOfParallelProxyRequests = 1;

    /**
     * Flag to indicate, if proxy requests to this trade site are allowed.
     */
    private boolean _proxyAllowed = false;
    
    /**
     * The trade site, this info is for.
     */
    private TradeSite _tradeSite = null;


    // Constructors

    /**
     * Create a new proxy info for a trade site.
     *
     * @param tradeSite The trade site, the info is for.
     * @param proxyAllowed Flag to indicate, if proxies are allowed.
     * @param maxNumberOfParallelProxyRequests The maximum number of parallel proxy requests.
     */
    public TradeSiteProxyInfo( TradeSite tradeSite
			       , boolean proxyAllowed
			       , int maxNumberOfParallelProxyRequests) {

	// Store the data in the instance.
	_tradeSite = tradeSite;
	_proxyAllowed = proxyAllowed;
	_maxNumberOfParallelProxyRequests = maxNumberOfParallelProxyRequests;
    }


    // Methods

    /**
     * Get the number of parallel proxy requests.
     *
     * @return The number of parallel proy requests.
     */
    public int getMaxNumberOfParallelProxyRequests() {

	return _maxNumberOfParallelProxyRequests;
    }

    public boolean isProxyAllowed() {

	return _proxyAllowed;
    }
}