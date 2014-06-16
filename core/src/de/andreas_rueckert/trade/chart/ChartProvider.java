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

//import de.andreas_rueckert.trade.chart.persistence.CachePersistence;
//import de.andreas_rueckert.trade.chart.persistence.CachePersistenceMySQL;
import de.andreas_rueckert.trade.Currency;
import de.andreas_rueckert.trade.CurrencyImpl;
import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.CurrencyPairImpl;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.Ticker;
import de.andreas_rueckert.trade.Trade;
import de.andreas_rueckert.trade.TradeDataNotAvailableException;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.util.LogUtils;
import de.andreas_rueckert.util.ModuleLoader;
import de.andreas_rueckert.util.TimeUtils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


/**
 * This class provides various aspects of charts.
 */
public class ChartProvider {

    // Inner classes

    /**
     * Cache for trade site calls.
     */
    class TradeSiteCache {
	
	// Instance variables

	/**
	 * The cached calls to trade sites with their results.
	 */
	private HashMap<TradeSiteCall, Object> _cachedCalls = null;


	// Constructors

	/**
	 * Create a new cache for calls to trade sites.
	 */
	TradeSiteCache() {

	    // Create a map of cached calls with the returned results.
	    _cachedCalls = new HashMap<TradeSiteCall, Object>();
	}


	// Methods

	/**
	 * Try to get a valid trade site call from the cache. If there
	 * are no valid calls in the cache, return null.
	 *
	 * @param tradeSiteCall The trade site request to cache.
	 * 
	 * @return A valid call result from the cache of null.
	 */
	public synchronized Object getValidCacheResult( TradeSiteCall tradeSiteCall) {

	    ArrayList<TradeSiteCall> callsToRemove = new ArrayList<TradeSiteCall>();

	    for( Map.Entry< TradeSiteCall, Object> cachedCallEntry : _cachedCalls.entrySet()) {
		
		TradeSiteCall cachedCall = cachedCallEntry.getKey();
		
		if( cachedCall.equals( tradeSiteCall)) {  // If the calls are the same

		    // Check if the cached call is not too old.
		    if( ! cachedCall.isDated()) {
			
			// Get the returned value from this previous call.
			Object returnValue = cachedCallEntry.getValue();
			
			// And return it.
			return returnValue;  

		    } else {  // Remove this call from the cache after the loop(!)

			callsToRemove.add( cachedCall);

			// If the user wants excessive logging, let him know about the dated result.
			if( getLogLevel() > LOGLEVEL_WARNING) {
			    LogUtils.getInstance().getLogger().info( "TradeSite call found in cache, but object is too old");
			}
		    }
		}
	    }
	    
	    // Now remove all the deprecated calls.
	    for( TradeSiteCall currentCall : callsToRemove) {
		_cachedCalls.remove( currentCall);
	    }

	    return null;  // No previous result found.
	}

	/**
	 * Add a new trade site call along with the returned value to the cache.
	 *
	 * @param tradeSiteCall The trade site call, that was performed.
	 * @param returnValue The value, that the call returned.
	 */
	public synchronized void putCall( TradeSiteCall tradeSiteCall, Object returnValue) {

	    // If the user wants intense logging, add some info...
	    if( getLogLevel() > LOGLEVEL_WARNING) {
		LogUtils.getInstance().getLogger().info( "Adding tradeSiteCall " 
							 + tradeSiteCall.toString() 
							 + " with result "
							 + returnValue.toString()
							 + " to the tradeSiteCache");
	    }

	    _cachedCalls.put( tradeSiteCall, returnValue);
	}
    }

    /**
     * Cache for a tradesite call.
     */
    class TradeSiteCall {

	// Instance variables

	/**
	 * The arguments of the call.
	 */
	private List<Object> _arguments = null;

	/**
	 * The name of the method.
	 */
	private String _method = null;

	/**
	 * The GMT microsecond timestamp, when the last call took place.
	 */
	private long _timestamp;

	/**
	 * The trade site, that handled the request.
	 */
	private TradeSite _tradeSite = null;


	// Constructors

	/**
	 * Create a new call object with no arguments (the fixed number of arguments is just for convenience).
	 *
	 * @param tradeSite The tradesite, that is called.
	 * @param method The name of the method.
	 */
	public TradeSiteCall( TradeSite tradeSite,  String method) {
	    _tradeSite = tradeSite;
	    _method = method;

	    // Store the current timestamp in the call.
	    updateTimestamp();
	}

	/**
	 * Create a new call object with 1 argument (the fixed number of arguments is just for convenience).
	 *
	 * @param tradeSite The tradesite, that is called.
	 * @param method The name of the method.
	 * @param argument The only argument.
	 */
	public TradeSiteCall( TradeSite tradeSite,  String method, Object argument) {
	    this( tradeSite, method);
	    getArguments().add( argument);  // Add the argument to the list of arguments.
	}

	/**
	 * Create a new call object with 2 arguments (the fixed number of arguments is just for convenience).
	 *
	 * @param tradeSite The tradesite, that is called.
	 * @param method The name of the method.
	 * @param argument1 The first argument.
	 * @param argument2 The second argument.
	 */
	public TradeSiteCall( TradeSite tradeSite,  String method, Object argument1, Object argument2) {
	    this( tradeSite, method, argument1);
	    getArguments().add( argument2);
	}


	// Methods

	/**
	 * Check, if 2 calls are the same (despite the timestamp).
	 *
	 * @param tradeSiteCall The trade site call to compare.
	 *
	 * @return true, if the 2 calls are the same.
	 */
	public boolean equals( TradeSiteCall tradeSiteCall) {

	    // A call is the same, if the trade site, the method and the arguments are equal.

	    // Compare the trade sites.
	    if( ! _tradeSite.getName().equals( tradeSiteCall.getTradeSite().getName())) {
		return false;
	    }

	    // Compare the method name.
	    if( ! _method.equals( tradeSiteCall.getMethod())) {
		return false;
	    }

	    // Compare the number of arguments.
	    if( getArguments().size() != tradeSiteCall.getArguments().size()) {
		return false;
	    }

	    // Now compare the arguments one by one.
	    for( int i = 0; i < getArguments().size(); ++i) {

		if( ! getArguments().get( i).equals( tradeSiteCall.getArguments().get( i))) {

		    return false;
		}
	    }

	    return true;  // The 2 calls are the same.
	}

	/**
	 * Get the list of arguments of this call.
	 *
	 * @return The list of arguments of this call.
	 */
	public final List<Object> getArguments() {
	    if( _arguments == null) {
		_arguments = new ArrayList<Object>();
	    }
	    return _arguments;
	}

	/**
	 * Get the method of this trade site call.
	 *
	 * @return The method of this trade site call.
	 */
	public final String getMethod() {
	    return _method;
	}

	/**
	 * Get the trade site of this method call.
	 *
	 * @return The trade site of this method call.
	 */
	public final TradeSite getTradeSite() {
	    return _tradeSite;
	}

	/**
	 * Check, if this cached is too old.
	 *
	 * @return true, if the cached value is too old and should be removed.
	 */
	public final boolean isDated() {

	    return ( TimeUtils.getInstance().getCurrentGMTTimeMicros() - _timestamp) > getTradeSite().getUpdateInterval();	    
	}

	/**
	 * Compute a rough string representation of this call.
	 *
	 * @return a stringn representation of this call.
	 */
	public final String toString() {
	    return "calling method " 
		+ getMethod()
		+ " of trade site "
		+ getTradeSite().getName()
		+ " at time: " 
		+ TimeUtils.microsToString( _timestamp);
	}

	/**
	 * Set the timestamp to the current time.
	 */
	public final void updateTimestamp() {
	    _timestamp = TimeUtils.getInstance().getCurrentGMTTimeMicros();
	}
    }


    // Static variables

    /**
     * The only instance of this class (singleton pattern).
     */
    private static ChartProvider _instance = null; 

    /**
     * The various log levels.
     */
    private final static int LOGLEVEL_ERROR        = 0;  // Minimal log level. Just log errors.
    private final static int LOGLEVEL_WARNING      = 1;  // More info. Log warnings.
    private final static int LOGLEVEL_NOTIFICATION = 2;  // Notify about cache findings etc.
    private final static int LOGLEVEL_DEBUG        = 4;  // Log more or less everything.

    // Instance variables

    /**
     * A map of cached ticker ojects.
     */
    private List<Ticker> _cachedTickers = null;

    /**
     * A map of trade caches. One for each tradesite.
     */
    private List<TradeCache> _cachedTrades = null;

    /**
     * The current currency to query.
     */
    private Currency _currentCurrency;

    /**
     * The current currency to use.
     */
    private Currency _currentPaymentCurrency;

    /**
     * The current log level.
     */
    private int _logLevel = LOGLEVEL_ERROR;

    /**
     * The offset of this chart provider from GMT.
     */
    private long _timestampOffset = 0;

    /**
     * A database connection to store trade data.
     */
    // CachePersistenceMySQL _cachePersistence = null;

    /**
     * A cache for trade site calls.
     */
    private TradeSiteCache _tradeSiteCache = null;
    
    /**
     * A collection of tradesites.
     */
    private Collection<TradeSite> _tradeSites = null;


    // Constructors

    /**
     * Create a ChartProvider instance.
     * Private constructor for singleton pattern.
     */
    private ChartProvider() {

	// Create a new list of trade sites.
	_tradeSites = new ArrayList<TradeSite>();

	// Create a list of cached ticker objects.
	_cachedTickers = new ArrayList<Ticker>();

	// Create a list of trade caches.
	_cachedTrades = new ArrayList<TradeCache>();

	// Create a cache for trade site calls.
	_tradeSiteCache = new TradeSiteCache();

	// Compute the offset of this user from GMT.
	_timestampOffset = Calendar.getInstance( TimeZone.getTimeZone("GMT")).getTimeInMillis() - System.currentTimeMillis();

	// Set the default currencies.
	_currentCurrency = CurrencyImpl.BTC;     // Query the btc reates.
	_currentPaymentCurrency = CurrencyImpl.USD;  // In usd.
    }


    // Methods

    /**
     * Activate the caching for a given trade site and a currency pair.
     *
     * @param tradeSite The trade site to cache.
     * @param currencyPair The currency pair to cache.
     */
    public void activateCaching( TradeSite tradeSite, CurrencyPair currencyPair) {
	if( ! isCached( tradeSite, currencyPair)) {  // If this combination is not already cached,

	    TradeCache newCache = new TradeCache( tradeSite, currencyPair);  // Create a new trade cache.
	    newCache.startUpdateThread();  // Start the automatic updates of this cache.

	    _cachedTrades.add( newCache);  // And add this cache to the list of caches.
	}
    }

    /**
     * Enable or disable trade data persistence.
     *
     * @param activate If true, activate the persistence. If false, deactivate persistence.
     */
    // private void activatePersistence( boolean activate) {

	// Activate the cache persistence.
	// getCachePersistence().setActive( activate);
    // }

    /**
     * Get the buy rates from all the current tickers.
     *
     * @return all the buy rates from the current tickers.
     */
    public synchronized Map<String, Price> getBuys() {

	// Get the values from the trade sites.
	Map<String, Object> resultBuffer = getTickerMapForKey( "buy");

	// Now convert the prices from Object to Price.
	Map<String, Price> result = new HashMap<String, Price>();

	// Loop over the map and convert each entry... :-( 
	for( Map.Entry<String, Object> entry : resultBuffer.entrySet()) {
	    result.put( entry.getKey(), (Price)entry.getValue());
	}

	return result;
    }

    /**
     * Get the cache persistence handler of the charts.
     *
     * @return The cache persistence handler (or null, if it's not activated at the moment?).
     */
    //public CachePersistence getCachePersistence() {

    //if( _cachePersistence == null) {  // If there is no persistence handler at the moment...
	    
	    // ...create a MySQL handler, since it's the only handler we have available at the moment...
    //	    _cachePersistence = new CachePersistenceMySQL();
    //	}

	// Return the cache persistence handler.
    //	return _cachePersistence;       
    //}

    /**
     * This is mainly a convenience method for the rule engine, so you don't have 
     * to look up trade site and currency pair each time...
     *
     * @param tradeSiteName The name of the trade site as a String object.
     * @param currencyPairName The name of the currency pair as a String object.
     *
     * @return The depth for the given trade site and the given currency pair.
     *
     * @throws TradeDataNotAvailableException if the data are not available for the given parameters.
     */
    public final Depth getDepth( String tradeSiteName, String currencyPairName) throws TradeDataNotAvailableException {

	// Try to find the trade site for the given name.
	TradeSite tradeSite = ModuleLoader.getInstance().getRegisteredTradeSite( tradeSiteName);

	if( tradeSite == null) {  // There is not registered trade site with the given name?
	    throw new TradeDataNotAvailableException( "There is no trade site registered with the name: " + tradeSiteName);
	}
	    
	// Check if this trade site supports the given currency pair.
	CurrencyPair [] supportedCurrencyPairs = tradeSite.getSupportedCurrencyPairs();
	
	CurrencyPair currencyPair = null;  // The default value of the requested currency pair is null (which indicates a not matching name).
	
	for( int index = 0; index < supportedCurrencyPairs.length; ++index) {
	    if( currencyPairName.equals( supportedCurrencyPairs[ index].getName())) {
		currencyPair = supportedCurrencyPairs[ index];  // We've found the requested currency pair!
		break;                                          // No further searching required...
	    }
	}

	// Check, if a currency pair with the given name was found.
	if( currencyPair == null) {
	    throw new TradeDataNotAvailableException( "Tradesite: " + tradeSiteName + " doesn't seem to support the currency pair: " + currencyPairName);
	}

	return getDepth( tradeSite, currencyPair);  // Just return the depth with the given parameters.
    }

    /**
     * Get the (cached) depth of a given trade site.
     *
     * @param t The trade site to query.
     * @param currencyPair The currency pair to query.
     *
     * @return The depth for the given trade site and the given currency pair.
     *
     * @throws TradeDataNotAvailableException if the data are not available for the given parameters.
     */
    public final synchronized Depth getDepth( TradeSite t, CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	// If the user wants logging, add some info on this call to the log.
	if( getLogLevel() > LOGLEVEL_WARNING) {
	    LogUtils.getInstance().getLogger().info( "Requesting depth from ChartProvider for tradesite " 
						     + t.getName() 
						     + " and currency pair "
						     + currencyPair.getName());
	}

	// Create a new TradeSiteCall object for the cache.
	TradeSiteCall tradeSiteCall = new TradeSiteCall( t, "depth", currencyPair);

	Object cachedResult = _tradeSiteCache.getValidCacheResult( tradeSiteCall);

	if( cachedResult != null) {       // If there is a valid cached result,

	    // If we should log this event, post something in the global log file.
	    if( getLogLevel() > LOGLEVEL_WARNING) {
		LogUtils.getInstance().getLogger().info( "Depth object found in ChartProvider-TradeSiteCall-Cache");
	    }
		
	    return (Depth)cachedResult;  // return it.
	}

	// There is no cached result, so we have to call the trade site directly.
	
	// If the user wants logging, let him know about the found cache object.
	if( getLogLevel() > LOGLEVEL_WARNING) {
	    LogUtils.getInstance().getLogger().info( "No matching depth oject found in ChartProvider.TradeSiteCall-Cache");
	}
	
	// Do the actual request.
	Depth callResult = t.getDepth( currencyPair);

	// If this is a valid result, add it to the cache.
	if( callResult != null) {
		
	    // Update the timestamp in the cached call to consider the duration of the request.
	    tradeSiteCall.updateTimestamp();
	    
	    _tradeSiteCache.putCall( tradeSiteCall, callResult);  // Add the new result to the cache.
	    
	    return callResult;   // And return the result of the request.
	}

	throw new TradeDataNotAvailableException( "The depth is not available for the given parameters");
    }

    /**
     * Get the only instance of this class (singleton pattern).
     *
     * @return The only instance of this class.
     */
    public static ChartProvider getInstance() {

	if( _instance == null) {              // If there is no instance yet,

	    _instance = new ChartProvider();  // ..create one.
	}

	return _instance;  // Return the only instance.
    }

    /**
     * Get the current log level.
     *
     * @return the current log level.
     */
    private final int getLogLevel() {
	return _logLevel;
    }

    /**
     * Get the sell rates from all the current tickers.
     *
     * @return all the sell rates from the current tickers.
     */
    public synchronized Map<String, Price> getSells() {

	// Get the values from the trade sites.
	Map<String, Object> resultBuffer = getTickerMapForKey( "sell");

	// Now convert the prices from Object to Price.
	Map<String, Price> result = new HashMap<String, Price>();

	// Loop over the map and convert each entry... :-( 
	for( Map.Entry<String, Object> entry : resultBuffer.entrySet()) {
	    result.put( entry.getKey(), (Price)entry.getValue());
	}

	return result;
    }

    /**
     * Get the current spread for a given trade site and currency pair.
     *
     * @param tradeSite The trade site to query.
     * @param currencyPair The currency pair to query.
     *
     * @return The spread for the depth of the given trade site and the given currency pair.
     *
     * @throws TradeDataNotAvailableException if the data are not available for the given parameters.
     */
    public Price getSpread( TradeSite tradeSite, CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	// Get the current depth for the given parameters.
	Depth currentDepth = getDepth( tradeSite, currencyPair);

	  // If there are buy and sell orders
        if( ( currentDepth.getBuySize() > 0) && ( currentDepth.getSellSize() > 0)) {

            // Compute the current spreadvand return it.
            Price currentSpread = currentDepth.getSell( 0).getPrice().subtract( currentDepth.getBuy( 0).getPrice());
	}

	// If there are not buy _and_ sell orders, throw an exception.
	throw new TradeDataNotAvailableException( "No get and buy orders in the current spread of trade site " 
						  + tradeSite.getName() 
						  + " with currency pair "
						  + currencyPair.getName());
    } 

    /**
     * Get a value from all the tickers and store them in a map along with the site name.
     *
     * @param key The key for the values.
     *
     * @return The map with the results.
     */
    private synchronized Map<String, Object> getTickerMapForKey( String key) {

	HashMap<String, Object> result = new HashMap< String, Object>();

	for( Ticker t : getTickers()) {
	    
	    Object value = t.getValue( key);  // Try to fetch a value with this key.

	    if( value != null) {  // If it was in the ticker,
		result.put( t.getSite().getName(), t.getValue( key));  // add it to the result map.
	    }
	}

	return result;
    }

    /**
     * Get a list of tickers.
     *
     * @return A map with <sitename> hashed tickers.
     */
    public synchronized Collection<Ticker> getTickers() {

	System.out.println( "Ticker requests to the trade sites are not yet limited in ChartProvider.getTickers() !");

	updateTickers();  // Check, if some of the tickers are expired.

	return _cachedTickers;
    }

    /**
     * Get the (cached) trades from a trade site.
     *
     * @param t The trade site to query.
     * @param currencyPair The currency pair to query.
     * @param sinceMicros The timespan to query in micro seconds.
     *
     * @throws TradeDataNotAvailableException if the sma could not be computed with the given parameters.
     */
    public final synchronized Trade [] getTrades( TradeSite t, CurrencyPair currencyPair, long sinceMicros) throws TradeDataNotAvailableException {

	// Create a new TradeSiteCall object for the cache.
	TradeSiteCall tradeSiteCall = new TradeSiteCall( t, "trades", new Long( sinceMicros), currencyPair);

	Object cachedResult = _tradeSiteCache.getValidCacheResult( tradeSiteCall);

	if( cachedResult != null) {       // If there is a valid cached result,
	    return (Trade [])cachedResult;  // return it.
	}

	// There is no cached result, so we have to call the trade site directly.

	// There is no cached result, so we have to call the trade site directly.
	Trade [] callResult = t.getTrades( sinceMicros, currencyPair);

	// If this is a valid result, add it to the cache.
	if( callResult != null) {
		
	    // Update the timestamp in the cached call to consider the duration of the request.
	    tradeSiteCall.updateTimestamp();

	    _tradeSiteCache.putCall( tradeSiteCall, callResult);  // Update the cache with the new result.

	    return callResult;   // And return the result of the request.
	}

	throw new TradeDataNotAvailableException( "Trades for the given parameters not available");
    }

    /**
     * Check, if a given currency pair on a given trade site is already cached.
     *
     * @param tradeSite The trade site for the trades.
     * @param currencyPair The currency pair to cache.
     *
     * @return true, if this currency pair is already cached on the given trade site. false otherwise.
     */
    public boolean isCached( TradeSite tradeSite, CurrencyPair currencyPair) {
	for( TradeCache currentCache : _cachedTrades) {

	    if( tradeSite.equals( currentCache.getTradeSite())              // If this cache is for the given trade site and
		&& currencyPair.equals( currentCache.getCurrencyPair())) {  // currency pair...

		return true;                                                // found it!
	    }
	}

	return false;  // Cache for this site and currency pair not found.
    }

    /**
     * Register a new trade site.
     *
     * @param tradeSite The trade site.
     */
    public void registerTradeSite( TradeSite tradeSite) {
	_tradeSites.add( tradeSite);
    }

    /**
     * Update the cached ticker objects, if necessary.
     */
    private synchronized void updateTickers() {

	// Compute the current epoch time as GMT.
	long currentGMTtime = System.currentTimeMillis() + _timestampOffset;

	// Loop over the cached tickers.
	for( int index = 0; index < _cachedTickers.size(); ) {

	    Ticker t = _cachedTickers.get( index);  // Get the ticker at the current position.

	    if( t.getExpirationTime() <= currentGMTtime) {  // If this ticker is expired.

		// Get a new ticker from this trading site.
		Ticker newTicker = t.getSite().getTicker( t.getCurrencyPair());

		if( newTicker != null) {
		    // And replace the old ticker, while trying to keep the order of ticker objects.
		    _cachedTickers.set( index++, newTicker);
		} else {
		    // remove the outdated ticker object
		    _cachedTickers.remove( index);  // Do not increase index here, since the next object moves one position forward!
		}
	    }
	}

	// If the cache does not contain a ticker for every registered trade site, add new tickers
	if( _cachedTickers.size() < _tradeSites.size()) {
	    for( TradeSite t : _tradeSites) {		
		boolean cached = false;

		for( Ticker ticker : _cachedTickers) {
		    if( ticker.getSite().equals( t)) {
			cached = true;
		    }
		}
		if( ! cached) {  // There's no ticker yet from that site.

		    Ticker newTicker = t.getTicker( new CurrencyPairImpl( _currentCurrency, _currentPaymentCurrency));

		    if( newTicker != null) {
			_cachedTickers.add( newTicker);
		    }
		}
	    }
	}
    }
}
