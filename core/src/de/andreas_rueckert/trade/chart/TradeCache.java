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

package de.andreas_rueckert.trade.chart;

import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.Trade;
import de.andreas_rueckert.util.LogUtils;
import de.andreas_rueckert.util.TimeUtils;
import java.util.ArrayList;


/**
 * This class caches a sorted list of Trade objects.
 */
class TradeCache {

    // Inner classes

    /**
     * A thread to update this cache regurlarly.
     */
    class UpdateCacheThread extends Thread {

	// Instance variables
	
	/**
	 * The currency pair to query.
	 */
	private CurrencyPair _currencyPair = null;

	/**
	 * The timestamp of the last trade site request.
	 */
	private long _lastCheckTimestamp;

	/**
	 * The interval to sleep after every loop in milliseconds(!). getUpdateInterval() returns
	 * this as microseconds, so we have to convert this!
	 */
	private long _sleepInterval;

	/**
	 * The trade site to monitor.
	 */
	private TradeSite _tradeSite = null;


	// Constructors

	/**
	 * Create a new update thread instance.
	 *
	 * @param tradeSite The trade site to query.
	 * @param currencyPair The currency pair to query.
	 */
	UpdateCacheThread( TradeSite tradeSite, CurrencyPair currencyPair) {
	    super( "TradeCacheUpdater");

	    _tradeSite = tradeSite;        // Store the trade site in the update thread.
	    _currencyPair = currencyPair;  // Store the currency pair in the thread.

	    // Init the last request timestamp to about 1hr before now.
	    _lastCheckTimestamp = TimeUtils.getInstance().getCurrentGMTTimeMicros() - INITIAL_UPDATE_INTERVAL - 3000000L;

	    // Get the update interval from the trade site and add some offset for additional security (not to get banned).
	    _sleepInterval = ( _tradeSite.getUpdateInterval() / 1000) + 100;
	}


	// Methods
	
	/**
	 * The actual code of the the thread.
	 */
	@Override public void run() {

	    while( _updateCacheThread != null) {

		// Check for new trades since the last poll.
		Trade [] newTrades = _tradeSite.getTrades( _lastCheckTimestamp, _currencyPair);

		// Set the timestamp to the request init - 3s (for the tcp/ip connection establishment).
		_lastCheckTimestamp = TimeUtils.getInstance().getCurrentGMTTimeMicros() - 3000000L;

		// Now merge the newly requested trades into the cache.
		merge( newTrades);

		// Remove all the trades, that are too old.
		removeDatedTrades();
		
		try {
		    Thread.sleep( _sleepInterval);  // Wait for the next update.
		} catch( InterruptedException ie) {
		    LogUtils.getInstance().getLogger().error( "Error while waiting in the trade cache updater: " + ie);
		}
	    }
	}
    }


    // Static variables

    /**
     * The initial update interval for the cache (1hour).
     */
    static final long INITIAL_UPDATE_INTERVAL = 60L * 60L * 1000000L;

    /**
     * The max. cached time interval (24 hrs for now).
     */
    static final long MAX_CACHED_INTERVAL = 24L * 60L * 60L * 1000000L;
    

    // Instance variables

    /**
     * The actual data in the cache.
     */
    private ArrayList<Trade> _cache = null;

    /**
     * The currency pair to query.
     */
    private CurrencyPair _currencyPair = null;

    /**
     * The trade site, the trades are from. null, if the trades are not from one specific site.
     */
    private TradeSite _tradeSite = null;

    /**
     * A thread to update this cache.
     */
    private UpdateCacheThread _updateCacheThread = null;


    // Constructors

    /**
     * Create a new trade cache.
     *
     * @param tradeSite The trade site, the trades were made.
     * @param currencyPair The currency pair to query.
     */
    public TradeCache( TradeSite tradeSite, CurrencyPair currencyPair) {
	_tradeSite = tradeSite;
	_currencyPair = currencyPair;

	_cache = new ArrayList<Trade>();
    }


    // Methods

    /**
     * Check, if the cache contains a given interval of timestamps.
     *
     * @param startTime A microsecond timestamp relative to GMT.
     * @param endTime A microsecond timestamp relative to GMT.
     */
    public boolean contains( long startTime, long endTime) {

	if( _cache.size() == 0) {  // If the cache is empty, 
	    return false;          // we don't have to check further.
	}

	// Check, if the requested timespan is already in the cache.
	if( ( _cache.get( 0).getTimestamp() <= startTime) 
	    && ( _cache.get( _cache.size() - 1).getTimestamp() >= endTime)) {
	    return true;
	}

	return false;  // Cache does not contain the entire timespan.
    }

    /**
     * Get the cached currency pair of this cache.
     *
     * @return The cached currency pair.
     */
    public CurrencyPair getCurrencyPair() {
	return _currencyPair;
    }

    /**
     * Get the timestamp of the newest trade.
     *
     * @return The timestamp of the newest trade.
     */
    public long getNewestTradeTimestamp() {
	return isEmpty() ? -1 : _cache.get( _cache.size() - 1).getTimestamp();
    }

    /**
     * Get the trade site, the trades were made on, or null, if the cache is not for a specific site.
     *
     * @return The trade site, the trades were made on or null, if the cache is not for a specific site.
     */
    public TradeSite getTradeSite() {
	return _tradeSite;
    }

    /**
     * Check, if the cache is empty.
     *
     * @return true, if the cache is empty. False otherwise.
     */
    public boolean isEmpty() {
	return ( _cache.size() == 0);
    }

    /**
     * Check, if the cache update thread is currently stopped.
     *
     * @return true, if the update thread is currently stopped.
     */
    public boolean isUpdateThreadStopped() {
	return (_updateCacheThread == null);
    }
	
    /**
     * Merge an array with sorted trades to this cache, so the double trades are removed
     * and the trades kept sorted for the timestamp.
     *
     * @param trades The trades to merge.
     */
    public void merge( Trade [] trades) {

	// Get the timestamp of the last trade in the cache or Long.MIN_VALUE in case the 
	// cache is currently emtpy.
	long lastTimestamp = _cache.size() > 0 ? _cache.get( _cache.size() - 1).getTimestamp() : Long.MIN_VALUE;

	// Find the first new trade with a timestamp newer than the last trade in the cache.
	// Since we assume, the trades in the array are sorted, we can just add the rest.
	int firstNewTrade = 0;

	while( trades[ firstNewTrade].getTimestamp() <= lastTimestamp) {  // Skip all the trades, that are older than the ones in
	    ++firstNewTrade;                                             // the cache.
	}
	 
	for( int index = firstNewTrade; index < trades.length; ++index) {  // Then just add the rest.
	    _cache.add( trades[ index]);
	}
    }

    /**
     * Remove trades, that are too old.
     */
    private void removeDatedTrades() {
	
	// Computer the oldest allowed timestamp
	long oldestAllowedTimestamp =  TimeUtils.getInstance().getCurrentGMTTimeMicros() - MAX_CACHED_INTERVAL;

	// Since we assume, that the cache is sorted, just remove the oldest trades from 
	// the beginning of the list.
	while( _cache.get( 0).getTimestamp() < oldestAllowedTimestamp) {
	    _cache.remove( 0);
	}
    }

    /**
     * If there is no update thread at the moment, start it.
     */
    public void startUpdateThread() {
	if( isUpdateThreadStopped()) {                                               // If there is no update thread at the moment.
	    _updateCacheThread = new UpdateCacheThread( _tradeSite, _currencyPair);  // Create a new thread,
	    _updateCacheThread.start();                                              // and start it.
	}
    }

    /**
     * If there is an update thread currently running, stop it.
     */
    public void stopUpdateThread() {
	if( ! isUpdateThreadStopped()) {               // If there is an update thread currently running.
	    Thread updateThread = _updateCacheThread;  // Save the reference to it,
	    _updateCacheThread = null;                 // and indicate the thread to stop.
	    try {
		updateThread.join();                   // Wait for the thread to stop.
	    } catch( InterruptedException ie) {
		LogUtils.getInstance().getLogger().error( "Error while stopping trade cache update thread: " + ie);
	    }
	}
    }
}