/**
 * Java implementation for cryptocoin trading.
 *
 * Copyright (c) 2014 the authors:
 * 
 * @author Andreas Rueckert <mail@andreas-rueckert.de>
 * @author gosucymp <gosucymp@gmail.com>
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

package de.andreas_rueckert.trade.site.kraken.client;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.CryptoCoinTrade;
import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.currency.CurrencyImpl;
import de.andreas_rueckert.trade.currency.CurrencySymbolMapper;
import de.andreas_rueckert.trade.currency.CurrencyNotSupportedException;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.currency.CurrencyPairImpl;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.DepositOrder;
import de.andreas_rueckert.trade.order.OrderStatus;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.order.WithdrawOrder;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteImpl;
import de.andreas_rueckert.trade.site.TradeSiteRequestType;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.trade.Ticker;
import de.andreas_rueckert.trade.Trade;
import de.andreas_rueckert.trade.TradeDataNotAvailableException;
import de.andreas_rueckert.util.HttpUtils;
import de.andreas_rueckert.util.LogUtils;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * Main class for the kraken API.
 *
 * @see <a href="https://www.kraken.com/help/api">Kraken API</a>
 */
public class KrakenClient extends TradeSiteImpl implements TradeSite {

    // Inner classes

    /**
     * Info on the fees for a specific currency pair.
     */
    class CurrencyPairFeeSet {

	// Inner classes

	/**
	 * Class to the volume limits of a given fee.
	 */
	class VolumeFee {

	    // Instance variables

	    /**
	     * The fee as a percentage.
	     */
	    private BigDecimal _fee = null;

	    /**
	     * The volume must be >= than this lower bound.
	     */
	    private Amount _lowerBound = null;


	    // Constructors

	    /**
	     * Create a new fee with bounds.
	     *
	     * @param lowerBound The lower bound for this fee.
	     * @param fee The fee for these bounds.
	     */
	    public VolumeFee( Amount lowerBound, BigDecimal fee) {

		_lowerBound = lowerBound;
		_fee = fee;
	    }


	    // Methods

	    /**
	     * Get the fee for these bounds.
	     *
	     * @return The fee for these bounds.
	     */
	    public BigDecimal getFee() {

		return _fee;
	    }

	    /**
	     * Check if a given volume is within the bounds of this fee.
	     *
	     * @param tradingVolume The given trading volume.
	     *
	     * @return true, if the given amount is within the bounds of this fee.
	     */
	    public boolean isInBounds( Amount tradingVolume) {

		// Check if the volume is >= than the lower bound.
		return ( tradingVolume.compareTo( _lowerBound) >= 0);
	    }
	}

	// Static variables


	// Instance variables

	/**
	 * The currency pair, this fee info if for.
	 */
	private CurrencyPair _currencyPair = null;

	/**
	 * A list of fees with their boundaries.
	 */
	private List< VolumeFee> _volumeFees = new ArrayList< VolumeFee>();

	
	// Constructors

	/**
	 * Create a new fee set for a given currency pair.
	 *
	 * @param currencyPair The currency pair, the fees are for.
	 */
	public CurrencyPairFeeSet( CurrencyPair currencyPair) {

	    _currencyPair = currencyPair;
	}


	// Methods

	/**
	 * Create a new fee for a given range of trading volume.
	 *
	 * @param lowerBound The minimum trading volume.
	 * @param fee The trading fee.
	 */
	public void addFee( Amount lowerBound, BigDecimal fee) {

	    // Create a new fee and add it to the list of fees.
	    _volumeFees.add( new VolumeFee( lowerBound, fee));
	}
	
	/**
	 * Try to find a fee for a given amount.
	 *
	 * @param tradingVolume The given amount.
	 *
	 * @return The found fee or null, if no fee was found.
	 */
	public BigDecimal findFeeForAmount( Amount tradingVolume) {
	    
	    BigDecimal currentBestFee = null;  // The currently best fee.

	    for( VolumeFee currentFee : _volumeFees) {

		// If the amount is in the bounds of this fee...
		if( currentFee.isInBounds( tradingVolume)) {

		    // Use this fee as the new best fee.
		    currentBestFee = currentFee.getFee();
		}
	    }

	    return currentBestFee;  // Return the best fee or null.
	}
    }


    // Static variables

    /**
     * The domain of the service.
     */
    public static String DOMAIN = "kraken.com";
    	
    /**
     * Flag to indicate if the traded volumes should be tracked.
     */
    private final static boolean TRACK_VOLUMES = false;
    

    // Instance variables

    /**
     * A map holding the all the fees for each currency pair.
     */
    private Map< CurrencyPair, CurrencyPairFeeSet> _currencyPairFees = new HashMap< CurrencyPair, CurrencyPairFeeSet>();

    /**
     * A mapping from currency pair objects to the kraken specific names of currency pairs.
     */
    private Map< CurrencyPair, String> _registeredCurrencyPairNames = new HashMap< CurrencyPair, String>();

    /**
     * Create a map, how much volume each trader has traded with each currency pair, so we can figure the correct
     * fee for it's trades.
     */
    private Map< TradeSiteUserAccount, Map< CurrencyPair, Amount>> _tradedVolume = new HashMap< TradeSiteUserAccount, Map< CurrencyPair, Amount>>();


    // Constructors

    /**
     * Create a new connection to the btc-e.com website.
     */
    public KrakenClient() {

	super();

	_name = "Kraken";
	_url = "https://api.kraken.com/";

	// Fetch the supported currency pairs.
	if( requestSupportedCurrencyPairs()) {

	    // Generate the array of supported currency pairs from the generated mapping.
	    _supportedCurrencyPairs = _registeredCurrencyPairNames.keySet().toArray( new CurrencyPair[ _registeredCurrencyPairNames.size()]);

	} else {

	    // Log the problem.
	    LogUtils.getInstance().getLogger().error( "Cannot fetch the supported currency pairs from " + _name);

	    // Just use a array with a length of 0 as a dummy.
	    _supportedCurrencyPairs = new CurrencyPair[ 0];
	}
    }


    // Methods

    /**
     * Add the name for a new currency pair.
     *
     * @param currencyPair The currency pair.
     * @param krakenName The name of the pair at kraken.
     */
    private final void addCurrencyPairName( CurrencyPair currencyPair, String krakenName) {

	// Add the name of the pair to the map.
	_registeredCurrencyPairNames.put( currencyPair, krakenName);
    }

    /**
     * Add a traded volume for a given user account and currency pair.
     *
     * @param userAccount The account of the user.
     * @param currencyPait The traded currency pair.
     * @param amount The traded amount.
     */
    private final void addTradedVolume( TradeSiteUserAccount userAccount, CurrencyPair currencyPair, Amount amount) {

	// .. Get the vald map and add the amount there...

	// Try to get the hashmap for the given currency pair and add new hash maps as they are required.
	Map< CurrencyPair, Amount> currencyPairMapping = _tradedVolume.get( userAccount);

	// If this mapping is null, we have to add it.
	if( currencyPairMapping == null) {

	    // Create a new mapping for this user.
	    currencyPairMapping = new HashMap< CurrencyPair, Amount>();

	    // And add it to the list of maps for the given user.
	    _tradedVolume.put( userAccount, currencyPairMapping);
	}

	// Now check, if there is a amount for the given currency pair.
	Amount currencyPairAmount = currencyPairMapping.get( currencyPair);

	// If there is no amount yet, create a new one with a value of 0.
	if( currencyPairAmount == null) {

	    currencyPairAmount = Amount.ZERO;
	}

	// Create a new amount with added trade volume.
	Amount newAmount = currencyPairAmount.add( amount);

	// Put this new amount in the hash map.
	// Since the pair mapping was reference in the mapping, it should be
	// modified and doesn't have to be put in the map again.
	currencyPairMapping.put( currencyPair, newAmount);
    }

    /**
     * Cancel an order on the trade site.
     *
     * @param order The order to cancel.
     *
     * @return true, if the order was canceled. False otherwise.
     */
    public boolean cancelOrder( SiteOrder order) {

	// If the order was successful, subtract the traded volume
	if( TRACK_VOLUMES) {  // <-- Add check for successful cancellation!

	    // Get the data from the order and subtract the traded volume, if the order was canceled successfully(!)
	    subtractTradedVolume( order.getTradeSiteUserAccount(), order.getCurrencyPair(), order.getAmount());
	}

	throw new NotYetImplementedException( "Cancelling an order is not yet implemented for " + _name);
    }

    /**
     * Execute an order on the trade site.
     * Synchronize this method, since several users might execute orders in parallel via an API implementation instance.
     *
     * @param order The order to execute.
     *
     * @return The new status of the order.
     */
    public synchronized OrderStatus executeOrder( SiteOrder order) {

	// If the order was successful, add the traded volume
	if( TRACK_VOLUMES) {  // <-- Add check for successful execution!

	    // Get the data from the order and add up the traded volume.
	    addTradedVolume( order.getTradeSiteUserAccount(), order.getCurrencyPair(), order.getAmount());
	}

	throw new NotYetImplementedException( "Executing an order is not yet implemented for " + _name);
    }

    /**
     * Get the current funds of the user via the API.
     *
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The accounts with the current balance as a collection of Account objects, or null if the request failed.
     */
    public Collection<TradeSiteAccount> getAccounts( TradeSiteUserAccount userAccount) {

	throw new NotYetImplementedException( "Getting the accounts is not yet implemented for " + _name);
    }

    /**
     * Get the Kraken name for a given currency pair.
     *
     * @param currencyPair The currency pair.
     *
     * @return The Kraken name for the given pair or null, if it is not known.
     */
    private final String getCurrencyPairName( CurrencyPair currencyPair) {

	// Get the Kraken name of the pair from the map of registered names.
	return _registeredCurrencyPairNames.get( currencyPair);
    }
    
    /**
     * Get the market depth as a Depth object.
     *
     * @param currencyPair The queried currency pair.
     *
     * @throws TradeDataNotAvailableException if the depth is not available.
     */
    public Depth getDepth( CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	// System.out.println( "DEBUG: getting depth for " + currencyPair.toString());

	if( ! isSupportedCurrencyPair( currencyPair)) {
	    throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on " + _name);
	}

	// Get the Kraken name for the currency pair.
	String krakenPairName = getCurrencyPairName( currencyPair);

	// The URL for the depth request.
	String url = "https://api.kraken.com/" + "0/public/Depth?pair=" + krakenPairName;

	// Do the actual request.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?

	    try {

		// Convert the result to JSON.
		JSONObject requestResultObj = (JSONObject)JSONObject.fromObject( requestResult);

		// Check for errors.
		JSONArray errors = requestResultObj.getJSONArray( "error");

		// If there are errors
		if( errors.size() > 0) {

		    // Write the first error to the log for now. Hopefully it should explain the problem.
		    LogUtils.getInstance().getLogger().error( "Error while getting the Kraken depth. Error 0 is: " + errors.get( 0));
		    
		    // and return null.
		    return null;
		}

		// Get the JSON object with the ask and bid arrays from the JSON result.
		return new KrakenDepth( requestResultObj.getJSONObject( "result").getJSONObject( krakenPairName), currencyPair, this);

	    } catch( JSONException je) {

		System.err.println( "Cannot parse " + this._name + " depth return: " + je.toString());

		throw new TradeDataNotAvailableException( "cannot parse data from " + this._name);
	    }
	}
	
	throw new TradeDataNotAvailableException( this._name + " server did not respond to depth request");
    }

    /**
     * Get the fee for an order in the resulting currency.
     * Synchronize this method, since several users might use this method with different
     * accounts and therefore different fees via a single API implementation instance.
     *
     * @param order The order to use for the fee computation.
     *
     * @return The fee in the resulting currency (currency value for buy, payment currency value for sell).
     *
     * @see <a href="https://www.kraken.com/help/new-fees-info">Kraken fees</a>
     */
    public synchronized Price getFeeForOrder( SiteOrder order) {

	if( order instanceof WithdrawOrder) {   // This is a withdraw order

	    // It seems, one cannot request the current withdrawal fees from the 
	    // server, so I just hardcoded some of them according to this list
	    // (as of 2014/05/21) A. Rueckert <a_rueckert@gmx.net>
	    // @see: https://www.kraken.com/help/faq#withdrawal-methods

	    // Get the withdrawn currency.
	    Currency withdrawnCurrency = order.getCurrencyPair().getCurrency();
	    
	    // Now switch the fees according to the currency.
	    if( withdrawnCurrency.hasCode( "BTC")) {

		return new Price( "0.0005", withdrawnCurrency);

	    } else if( withdrawnCurrency.hasCode( "LTC")) {

		return new Price( "0.02", withdrawnCurrency);

	    } else if( withdrawnCurrency.hasCode( "DOGE")) {

		return new Price( "2", withdrawnCurrency);

	    } else if( withdrawnCurrency.hasCode( "NMC")) {

		return new Price( "0.005", withdrawnCurrency);

	    } else {

		// The fees for this currency are not yet implemented.
		throw new CurrencyNotSupportedException( "Cannot compute withdraw fee for the currency: " 
							 + withdrawnCurrency.toString()
							 + " . Please check the sources and the Kraken fee FAQ");
	    }

	} else if( order instanceof DepositOrder) {  // This is a deposit order

	    // There's a one-time 50 XRP fee for the wallet opening and a
	    // 26000 KRW fee for Korean swift deposits. Since I cannot
	    // really say, if one of those cases apply, I'll just assume a fee
	    // of 0 for now...
	    // @see: https://www.kraken.com/help/faq#deposit-methods
	    return new Price( "0", order.getCurrencyPair().getCurrency());

	} else if(( order.getOrderType() == OrderType.BUY) || ( order.getOrderType() == OrderType.SELL)) {

	    // The total fee is the amount * <fee in percent> / 100 
	    return new Price( order.getAmount().multiply( getFeeForUserAndPair( order.getTradeSiteUserAccount(), order.getCurrencyPair())).divide( new BigDecimal( "100"), MathContext.DECIMAL128)
			      , order.getCurrencyPair().getCurrency());

	} else {  // This is an unknown order type?

	    return null;  // Should never happen...
	}
    }
    
    /**
     * Get the trading fee (as percentage) for a given user account and currency pair. The user account might
     * be null. Then, the highest (default) fee is returned.
     *
     * @param userAccount The account of the exchange or null, if the user is not known.
     * @param currencyPair The currency pair, we want to trade.
     *
     * @return The fee as percent or null, of no fee for this pair is found.
     */
    private final BigDecimal getFeeForUserAndPair( TradeSiteUserAccount userAccount, CurrencyPair currencyPair) {

	// No need to check, if it's a supported currency pair, because this is done in the getFeeForVolumeAndPair
	// method.

	// If no user account is given, don't try to get the traded volume.
	if( userAccount == null) {

	    return getFeeForVolumeAndPair( null, currencyPair);

	} else {

	    // Get the traded volume of this user and then request the fee for this trading volume.
	    return getFeeForVolumeAndPair( getTradedVolumeForUser( userAccount, currencyPair), currencyPair);
	}
    }

    /**
     * Get the fee as percent for a traded volume and a currency pair. The traded volume might be null,
     * if it is not known. Then a volume of 0 is assumed and the highest fee is returned.
     *
     * @param tradedVolume The traded volume of the user or null if it is not known.
     * @param currencyPair The currency pair to trade.
     *
     * @return The fee for this volume, the maximum fee, if no volume was given or null if no fee is known for this currency pair.
     */
    private final BigDecimal getFeeForVolumeAndPair( Amount tradedVolume, CurrencyPair currencyPair) {

	// Defensive programming all the way...
	if( ! isSupportedCurrencyPair( currencyPair)) {

	    // The fees for this currency are not yet implemented.
	    throw new CurrencyNotSupportedException( "Cannot compute trading fee for currency pair " 
						     + currencyPair.toString()
						     + " , because this currency pair doesn't seem to be traded on "
						     + this._name);

	}
       
	// If no volume is given, just use the default volume of 0.
	if( tradedVolume == null) {

	    tradedVolume = Amount.ZERO;
	}

	// Try to get a currency pair fee set for the given currency pair.
	CurrencyPairFeeSet currentFeeSet = _currencyPairFees.get( currencyPair);

	// If there is no fee set, we have a problem...
	if( currentFeeSet == null) {
	    
	    // The fees for this currency were not available, when the traded assets were requested.
	    throw new CurrencyNotSupportedException( "Cannot get trading fee set for currency pair " 
						     + currencyPair.toString()
						     + " , because this currency pair is not in the map of currency pair fees on "
						     + this._name);
	}

	// Try to get a fee for the given amount.
	BigDecimal currentFee = currentFeeSet.findFeeForAmount( tradedVolume);

	// Check, if a fee was found.
	if( currentFee == null) {
	    
	    // There is no fee for this amount in the fee set.
	    throw new CurrencyNotSupportedException( "Cannot get trading fee for this amount in the fee set for "
						     + currencyPair.toString()
						     + " on "
						     + this._name);
	}

	// Return the found fee.
	return currentFee;
    }

    /**
     * Get the shortest allowed requet interval in microseconds.
     *
     * @return The shortest allowed request interval in microseconds.
     */
    public final long getMinimumRequestInterval() {

	// The actual minimum request interval is 5s, but I use 6 just to make sure...
	// @see http://www.reddit.com/r/Bitcoin/comments/1rztum/release_the_kraken_part_2/
	return 6L * 1000000L;
    }

    /**
     * Get the open orders on this trade site.
     *
     * @param userAccount The account of the user on the exchange. Null, if the default account should be used.
     *
     * @return The open orders as a collection, or null if the request failed.
     */
    public Collection<SiteOrder> getOpenOrders( TradeSiteUserAccount userAccount) {

	throw new NotYetImplementedException( "Getting the open orders is not yet implemented for " + _name);
    }

    /**
     * Get the section name in the global property file.
     *
     * @return The name of the property section as a String.
     */
    public String getPropertySectionName() {

	return _name;
    }

    /**
     * Get the current ticker from the API.
     *
     * @param currencyPair The currency pair to query.
     *
     * @return The current ticker.
     *
     * @throws TradeDataNotAvailableException if the ticker is not available.
     */
    public Ticker getTicker( CurrencyPair currencyPair) throws TradeDataNotAvailableException {
	
	if( ! isSupportedCurrencyPair( currencyPair)) {
	    throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on " + _name);
	}

	// Get the Kraken name for the currency pair.
	String krakenPairName = getCurrencyPairName( currencyPair);

	// The URL for the ticker request.
	String url = "https://api.kraken.com/" + "0/public/Ticker?pair=" + krakenPairName;

	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?

	    try {

		// Convert the result to JSON.
		JSONObject requestResultObj = (JSONObject)JSONObject.fromObject( requestResult);

		// Check for errors.
		JSONArray errors = requestResultObj.getJSONArray( "errors!");
		
		// If there are errors
		if( errors.size() > 0) {

		    // Write the first error to the log for now. Hopefully it should explain the problem.
		    LogUtils.getInstance().getLogger().error( "Error while getting the Kraken depth. Error 0 is: " + errors.get( 0));
		    
		    // and return null.
		    return null;
		}

		// Convert the HTTP request return value to JSON to parse further.
		return new KrakenTicker( requestResultObj.getJSONObject( "result").getJSONObject( krakenPairName), currencyPair, this);

	    } catch( JSONException je) {

		LogUtils.getInstance().getLogger().error( "Cannot parse ticker object: " + je.toString());
	    }
	}
	
	throw new TradeDataNotAvailableException( "The " + _name + " ticker request failed");
    }

    /**
     * Get the traded volume for a given user account.
     *
     * @param userAccount The account of the user.
     * @param currencyPair The currency pair, that was traded.
     *
     * @return The traded volume of the user. An amount of 0 is the default, if no other data are available.
     */
    private final Amount getTradedVolumeForUser( TradeSiteUserAccount userAccount, CurrencyPair currencyPair) {

	// Try to get the map of traded volumes for each currency pair.
	Map< CurrencyPair, Amount> tradedVolumeForPairs = _tradedVolume.get( userAccount);

	// If there is no map, return the default value of 0.
	if( tradedVolumeForPairs == null) {

	    return Amount.ZERO;  // No traded volume stored.
	}

	// Get the traded volume for the specific currency pair.
	Amount tradedVolume = tradedVolumeForPairs.get( currencyPair);

	// Check, if the amount is null (not available) and return
	// an amount of 0 then.
	return tradedVolume == null ? Amount.ZERO : tradedVolume;
    }

    /**
     * Get a list of recent trades.
     *
     * @param since_micros The GMT-relative epoch in microseconds.
     * @param currencyPair The currency pair to query.
     *
     * @return The trades as a list of Trade objects.
     *
     * @throws TradeDataNotAvailableException if the ticker is not available.
     */
    public List<Trade> getTrades( long since_micros, CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	throw new NotYetImplementedException( "Getting the trades is not yet implemented for " + _name);
    }

    /**
     * Get the interval, in which the trade site updates it's depth, ticker etc. 
     * in microseconds.
     *
     * @return The update interval in microseconds.
     */
    public final long getUpdateInterval() {
	return 15L * 1000000L;  // 15s should work for most exchanges. Dont't know the actual frequency (a_rueckert).
    }

    /**
     * Check, if some request type is allowed at the moment. Most
     * trade site have limits on the number of request per time interval.
     *
     * @param requestType The type of request (trades, depth, ticker, order etc).
     *
     * @return true, if the given type of request is possible at the moment.
     */
    public boolean isRequestAllowed( TradeSiteRequestType requestType) {

	return true;  // Just a dummy for now.
    }

    /**
     * Check, if a given currency pair is supported on this site.
     *
     * @param currencyPair The currency pair to check for this site.
     *
     * @return true, if the currency pair is supported. False otherwise.
     */
    public boolean isSupportedCurrencyPair( CurrencyPair currencyPair) {

	// If we have a Kraken name for this pair, it should be supported.
	return _registeredCurrencyPairNames.containsKey( currencyPair);
    }

    /**
     * Fetch the supported currency pairs from the Kraken server.
     */
    private final boolean requestSupportedCurrencyPairs() {

	String url = _url + "0/public/AssetPairs";  // The URL for fetching the traded pairs.

	// Request info on the traded pairs from the server.
	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // If the server returned a response.

	    // Try to parse the response.
	    JSONObject jsonResult = JSONObject.fromObject( requestResult);

	    // Test, if there is a result returned (might be missing, if an error occurred).
	    if( jsonResult.containsKey( "result")) {

		// The pairs are returned as a JSON object.
		JSONObject pairListJSON = jsonResult.getJSONObject( "result");

		// Iterate over the entries of this object.
		for( Iterator keyIterator = pairListJSON.keys(); keyIterator.hasNext(); ) {

		    // The key is the name of the pair in kraken style...
		    String krakenPairName = (String)keyIterator.next(); 

		    // Get the next currency pair as a JSON object.
		    JSONObject currentCurrencyPairJSON = pairListJSON.getJSONObject( krakenPairName);

		    Currency currency = CurrencySymbolMapper.getCurrencyForIso4217Name( currentCurrencyPairJSON.getString( "base"));

		    Currency paymentCurrency = CurrencySymbolMapper.getCurrencyForIso4217Name( currentCurrencyPairJSON.getString( "quote"));

		    // Create a pair from the currencies.
		    CurrencyPair currentPair = new CurrencyPairImpl( currency, paymentCurrency);

		    // Add the pair with it's kraken name to the map of pair names.
		    addCurrencyPairName( currentPair, krakenPairName);

		    // System.out.println( "DEBUG: Kraken: found currency pair " + currentPair.toString());

		    //////////////////////////////////////////////////
		    // Parse the currency fees for each currency pair.
		    //////////////////////////////////////////////////
		    JSONArray jsonFees = currentCurrencyPairJSON.getJSONArray( "fees");

		    // Create a new fee set for the currency pair.
		    CurrencyPairFeeSet feeSet = new CurrencyPairFeeSet( currentPair);

		    // Now loop over all the given fees with volumes.
		    for( int currentFeeIndex = 0; currentFeeIndex < jsonFees.size(); ++currentFeeIndex) {

			// Get the current volume fee as an array.
			JSONArray jsonCurrentFee = jsonFees.getJSONArray( currentFeeIndex);

			// The format of the fees is [<min volume>, <fee>]
			feeSet.addFee( new Amount( jsonCurrentFee.getDouble( 0)), new BigDecimal( jsonCurrentFee.getDouble( 1)));

			// System.out.println( "DEBUG: adding fee " + jsonCurrentFee.getDouble( 1) + " for currency pair " + currentPair.toString());
		    }

		    // Put the set of fees into the map of fees for currency pairs.
		    _currencyPairFees.put( currentPair, feeSet);

		    //////////////////////////////////////////////////
		    // End of fee parsing 
		    //////////////////////////////////////////////////

		    // ToDo: also parse decimals for precision etc?

		    
		}

		return true;  // Parsing worked.
	    }
	}

	return false;   // Fetching the traded currency pairs failed.
    }

    /**
     * Subtract a traded volume for a given user account and currency pair.
     *
     * @param userAccount The account of the user.
     * @param currencyPait The traded currency pair.
     * @param amount The traded amount.
     */
    private final void subtractTradedVolume( TradeSiteUserAccount userAccount, CurrencyPair currencyPair, Amount amount) {

	// Since I'm unwilling to write (and debug) the whole code twice, I just use the add method for the volumes here,
	// and add the negative amount to the traded volume.
	addTradedVolume( userAccount, currencyPair, new Amount( amount.multiply( new BigDecimal( "-1"))));
    }
}
