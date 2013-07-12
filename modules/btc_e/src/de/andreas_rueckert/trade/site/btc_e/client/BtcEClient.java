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

package de.andreas_rueckert.trade.site.btc_e.client;

import de.andreas_rueckert.MissingAccountDataException;
import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.persistence.PersistentProperty;
import de.andreas_rueckert.persistence.PersistentPropertyList;
import de.andreas_rueckert.trade.account.CryptoCoinAccount;
import de.andreas_rueckert.trade.account.CryptoCoinAccountImpl;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.account.TradeSiteAccountImpl;
import de.andreas_rueckert.trade.CryptoCoinTrade;
import de.andreas_rueckert.trade.Currency;
import de.andreas_rueckert.trade.CurrencyImpl;
import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.CurrencyPairImpl;
import de.andreas_rueckert.trade.CurrencyNotSupportedException;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.CryptoCoinOrderBook;
import de.andreas_rueckert.trade.order.DepositOrder;
import de.andreas_rueckert.trade.order.OrderNotInOrderBookException;
import de.andreas_rueckert.trade.order.OrderStatus;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.order.WithdrawOrder;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteImpl;
import de.andreas_rueckert.trade.site.TradeSiteRequestType;
import de.andreas_rueckert.trade.Ticker;
import de.andreas_rueckert.trade.TradeDataNotAvailableException;
import de.andreas_rueckert.util.HttpUtils;
import de.andreas_rueckert.util.LogUtils;
import de.andreas_rueckert.util.TimeUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException; 
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.DecoderException;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


/**
 * Main class for the btc-e API.
 *
 * @see https://btc-e.com/page/2
 * @see http://bitcoin.stackexchange.com/questions/1393/does-btc-e-have-an-api-for-alternate-currencies
 */
public class BtcEClient extends TradeSiteImpl implements TradeSite {

    // Static variables

    /**
     * The domain of the service.
     */
    public static String DOMAIN = "btc-e.com";

    /**
     * This is just a dummy, since btc-e fails with nonce=currentTimeMicros() and
     * the millis can't be used, since 2 calls might happen within the same millisecond
     *, but the nonce has to be increased. So I start with a unixtime nonce (secs since
     * the epoch and increase for every request by 1).
     */
    private static long _nonce;

    /**
     * The default user agent.
     */
    private static String USERAGENT = "Mozilla";

    /**
     * The default timeout.
     */
    private static int TIMEOUT = 3000;


    // Instance variables

    /**
     * The username of the user at btc-e.com
     */
    private String _username = null;

    /**
     * The cookies of the current session.
     */
    Map<String, String> _currentCookies = null;

    /**
     * The currency, that the user uses for payments.
     */
    private Currency _currentCurrency = null;

    /**
     * The current token from the btc-e website. Seems to be 
     * constant during a login session?
     */
    private String _currentToken = null;

    /**
     * The customer id (or number) is actually an (currently 4-digit) int ,
     * but this might change in the future.
     */
    private String _customerId = null;

    /**
     * The HTML parser for the btc-e.com website.
     */
    private BtcEHtmlParser _htmlParser = null;

    /**
     * Flag to indicate, if the user is logged in.
     */
    private boolean _isLoggedIn = false;

    /**
     * The API key.
     */
    private String _key = null;

    /**
     * The password of the user at btc-e.com
     */
    private String _password = null;

    /**
     * The API secret.
     */
    private String _secret = null;


    // Constructors

    /**
     * Create a new connection to the btc-e.com website.
     */
    public BtcEClient() {
	super();

	_name = "BTCe";
	_url = "https://btc-e.com/";

	// Define the supported currency pairs for this trading site.
	_supportedCurrencyPairs = new CurrencyPair[14];
	_supportedCurrencyPairs[0] = new CurrencyPairImpl( CurrencyImpl.BTC, CurrencyImpl.USD);
	_supportedCurrencyPairs[1] = new CurrencyPairImpl( CurrencyImpl.BTC, CurrencyImpl.RUR);
	_supportedCurrencyPairs[2] = new CurrencyPairImpl( CurrencyImpl.BTC, CurrencyImpl.EUR);
	_supportedCurrencyPairs[3] = new CurrencyPairImpl( CurrencyImpl.LTC, CurrencyImpl.BTC);
	_supportedCurrencyPairs[4] = new CurrencyPairImpl( CurrencyImpl.LTC, CurrencyImpl.USD);
	_supportedCurrencyPairs[5] = new CurrencyPairImpl( CurrencyImpl.LTC, CurrencyImpl.RUR);
	_supportedCurrencyPairs[6] = new CurrencyPairImpl( CurrencyImpl.NMC, CurrencyImpl.BTC);
	_supportedCurrencyPairs[7] = new CurrencyPairImpl( CurrencyImpl.USD, CurrencyImpl.RUR);
	_supportedCurrencyPairs[8] = new CurrencyPairImpl( CurrencyImpl.EUR, CurrencyImpl.USD);
	_supportedCurrencyPairs[9] = new CurrencyPairImpl( CurrencyImpl.NVC, CurrencyImpl.BTC);
	_supportedCurrencyPairs[10] = new CurrencyPairImpl( CurrencyImpl.TRC, CurrencyImpl.BTC);
	_supportedCurrencyPairs[11] = new CurrencyPairImpl( CurrencyImpl.PPC, CurrencyImpl.BTC);
	_supportedCurrencyPairs[12] = new CurrencyPairImpl( CurrencyImpl.FTC, CurrencyImpl.BTC);
	_supportedCurrencyPairs[13] = new CurrencyPairImpl( CurrencyImpl.CNC, CurrencyImpl.BTC);

	setCurrentCurrency( CurrencyImpl.USD);

	// Create a new parser for the btc-e.com website.
	_htmlParser = new BtcEHtmlParser( this);

	// Create a unixtime nonce for the new API.
	_nonce = ( TimeUtils.getInstance().getCurrentGMTTimeMicros() / 1000000);
    }


    // Methods

    /**
     * Execute a authenticated query on btc-e.
     *
     * @param method The method to execute.
     * @param arguments The arguments to pass to the server.
     *
     * @return The returned data as JSON or null, if the request failed.
     *
     * @see http://pastebin.com/K25Nk2Sv
     */
    private final JSONObject authenticatedHTTPRequest( String method, Map<String, String> arguments) {
	HashMap<String, String> headerLines = new HashMap<String, String>();  // Create a new map for the header lines.
	Mac mac;
	SecretKeySpec key = null;

	// Check, if _key and _secret are available for the request.
	if( _key == null) {
	    throw new MissingAccountDataException( "Key not available for authenticated request to btc-e");
	}
	if( _secret == null) {
	    throw new MissingAccountDataException( "Secret not available for authenticated request to btc-e");
	}

	if( arguments == null) {  // If the user provided no arguments, just create an empty argument array.
	    arguments = new HashMap<String, String>();
	}
	
	arguments.put( "method", method);  // Add the method to the post data.
	arguments.put( "nonce",  "" + ++_nonce);  // Add the dummy nonce.

	// Convert the arguments into a string to post them.
	String postData = "";

	for( Iterator argumentIterator = arguments.entrySet().iterator(); argumentIterator.hasNext(); ) {
	    Map.Entry argument = (Map.Entry)argumentIterator.next();
	    
	    if( postData.length() > 0) {
		postData += "&";
	    }
	    postData += argument.getKey() + "=" + argument.getValue();
	}

	// Create a new secret key
	try {
	    key = new SecretKeySpec( _secret.getBytes( "UTF-8"), "HmacSHA512" ); 
	} catch( UnsupportedEncodingException uee) {
	    System.err.println( "Unsupported encoding exception: " + uee.toString());
	    return null;
	} 

	// Create a new mac
	try {
	    mac = Mac.getInstance( "HmacSHA512" );
	} catch( NoSuchAlgorithmException nsae) {
	    System.err.println( "No such algorithm exception: " + nsae.toString());
	    return null;
	}

	// Init mac with key.
	try {
	    mac.init( key);
	} catch( InvalidKeyException ike) {
	    System.err.println( "Invalid key exception: " + ike.toString());
	    return null;
	}

	// Add the key to the header lines.
	headerLines.put( "Key", _key);

	// Encode the post data by the secret and encode the result as base64.
	try {
	    headerLines.put( "Sign", Hex.encodeHexString( mac.doFinal( postData.getBytes( "UTF-8"))));
	} catch( UnsupportedEncodingException uee) {
	    System.err.println( "Unsupported encoding exception: " + uee.toString());
	    return null;
	} 
	
	// Now do the actual request
	String requestResult = HttpUtils.httpPost( "https://" + DOMAIN + "/tapi", headerLines, postData);

	if( requestResult != null) {   // The request worked

	    try {
		// Convert the HTTP request return value to JSON to parse further.
		JSONObject jsonResult = JSONObject.fromObject( requestResult);

		// Check, if the request was successful
		int success = jsonResult.getInt( "success");

		if( success == 0) {  // The request failed.
		    String errorMessage = jsonResult.getString( "error");

		    LogUtils.getInstance().getLogger().error( "btc-e.com trade API request failed: " + errorMessage);

		    return null;
		} else {  // Request succeeded!
		    return jsonResult.getJSONObject( "return");
		}

	    } catch( JSONException je) {
		System.err.println( "Cannot parse json request result: " + je.toString());

		return null;  // An error occured...
	    }
	} 

	return null;  // The request failed.
    }

    /**
     * Execute an order on the trade site.
     *
     * @param order The order to execute.
     *
     * @return The new status of the order.
     */
    public OrderStatus executeOrder( SiteOrder order) {

	OrderType orderType = order.getOrderType();  // Get the type of this order.

	if( ( orderType == OrderType.BUY) || ( orderType == OrderType.SELL)) {  // If this is a buy or sell order, run the trade code.

	    // The parameters for the HTTP post call.
	    HashMap<String, String> parameter = new HashMap<String, String>();
	    
	    parameter.put( "type", order.getOrderType() == OrderType.BUY ? "buy" : "sell");  // Indicate buy or sell.
	    parameter.put( "amount", formatAmount( order.getAmount()));
	    parameter.put( "rate", formatPrice( order.getPrice(), order.getCurrencyPair()));
	    
	    int currencyPairId = getIdForCurrencies( order.getCurrencyPair());
	    
	    if( currencyPairId == -1) {
		throw new CurrencyNotSupportedException( "This currency pair is not supported in btc-e orders: " 
							 + order.getCurrencyPair().getCurrency().toString() 
							 + " and payment in "
							 + order.getCurrencyPair().getPaymentCurrency());
	    }
	    
	    parameter.put( "pair", order.getCurrencyPair().getCurrency().getName().toLowerCase() + "_" + order.getCurrencyPair().getPaymentCurrency().getName().toLowerCase());  

	    JSONObject jsonResponse = authenticatedHTTPRequest( "Trade", parameter);

	    if( jsonResponse == null) {
		return OrderStatus.ERROR;
	    } else {
		// Try to get and store the site id for the order first, so we can access the order later.
		long btceOrderId = jsonResponse.getLong( "order_id");

		order.setSiteId( "" + btceOrderId);  // Store the id in the order.

		double remains = jsonResponse.getDouble( "remains");
		    
		// Set a new status for this order.
		order.setStatus( remains == 0.0 ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED);

		return order.getStatus();
	    }
	} else if( orderType == OrderType.DEPOSIT) {  // This is a deposit order..

	    DepositOrder depositOrder = (DepositOrder)order;

	    // Get the deposited currency from the order.
	    Currency depositedCurrency = depositOrder.getCurrency();

	    // Check, if this currency is supported yet in this implementation.
	    if( depositedCurrency.equals( CurrencyImpl.BTC)
		|| depositedCurrency.equals( CurrencyImpl.LTC)) {

		// Get the address for a deposit from the trade site.
		String depositAddress = getDepositAddress( depositedCurrency);

		// Attach a new account for depositing to this order.
		depositOrder.setAccount( new CryptoCoinAccountImpl( depositAddress
								    , new BigDecimal( "0")
								    , depositedCurrency));

		// Now return a new order status to indicate, that the order was modified.
		return OrderStatus.DEPOSIT_ADDRESS_GENERATED;

	    } else {  // This currency is not supported yet.
		
		throw new CurrencyNotSupportedException( "Depositing the currency " 
							 + depositedCurrency 
							 + " is not supported yet in this implementation");

	    }

	} else if( orderType == OrderType.WITHDRAW) {  // This is a withdraw order.

	    // Just to avoid multiple typecasts all over the code here...
	    WithdrawOrder withdrawOrder = (WithdrawOrder)order;

	    // For now, make sure that we withdraw to a cryptocoin address
	    if( ! ( withdrawOrder.getAccount() instanceof CryptoCoinAccount)) {
		
		throw new CurrencyNotSupportedException( "Can only withdraw to a cryptocoin account at the moment");
	    }

	    // Get a cryptocoin address for the account to withdraw to...
	    String cryptocoinAddress = ((CryptoCoinAccount)(withdrawOrder.getAccount())).getCryptoCoinAddress();

	    // Get the coin id for the given currency to withdraw
	    short coin_id = getIdForCurrency( withdrawOrder.getCurrency());

	    // It's practically a translation of the following jquery code:
	    // function withdraw_coin(a){
	    //   var b=$("#sum").val(),c=$("#address").val(),d=$("#token").val();
	    //   showLoader();
	    //    $.post(domain+aF+"coins.php",{act:"withdraw",sum:b,address:c,coin_id:a,token:d},function(a){nPopReady(430,70);$("#nPopupCon").html(a);hideLoader()})}

	    String url = "https://" + BtcEClient.DOMAIN + "/coins.php";

	    ensureLogin();  // Make sure, that the user is logged in.
	    
	    if( _customerId == null) {
		throw new MissingBtcECustomerIdException( "getFunds: no customer id received from the btc-e.com website.");
	    }
	
	    if( _currentCookies == null) {
		throw new MissingBtcECookieException( "No current btc-e.com cookie for getFunds! Please login to get one!");
	    }

	    try {
		// Now post the actual withdrar request.
		Response response = Jsoup.connect( url)
		    .data( "act", "withdraw"                   // The parameters for the request.
			   , "sum", withdrawOrder.getAmount().toString()
			   , "address", cryptocoinAddress
			   , "coin_id", "" + coin_id
			   , "token", _currentToken)
		    .method( Method.POST)
		    .cookies( _currentCookies)
		    .userAgent( USERAGENT)
		    .timeout( TIMEOUT)
		    .execute();

		// Check, if the response code signals success.
		// There might be ways to detail out the error, but for now I can live with this binary response...
		return response.statusCode() == 200 ? OrderStatus.FILLED : OrderStatus.ERROR;
		
	    } catch( IOException ioe) {

		LogUtils.getInstance().getLogger().error( "Cannot post profile request to the btc-e.com website: " + ioe.toString());
	    }
	    
	    // throw new NotYetImplementedException( "Executing withdraws is not yet implemented for " + this.getName());
	}

	return null;  // An error occured, or this is an unknow order type?
    }

    /**
     * Format an amount btc-e compliant.
     * 
     * @param amount The amount to format.
     */
    private final String formatAmount( BigDecimal amount) {

	// The amount has always 8 fraction digits for now.
	DecimalFormat amountFormat = new DecimalFormat( "#####.########");

	return amountFormat.format( amount);
    }


    /**
     * Format the price for a given currency pair.
     *
     * @param price The price to format.
     * @param currencyPair The currency pair to trade.
     */
    private final String formatPrice( BigDecimal price, CurrencyPair currencyPair) {
	if( currencyPair.getCurrency().equals( CurrencyImpl.BTC) 
	    && currencyPair.getPaymentCurrency().equals( CurrencyImpl.USD)) {

	    // btc has only 3 fraction digits for usd.
	    DecimalFormat btcDecimalFormat = new DecimalFormat("#####.###");
	    
	    return btcDecimalFormat.format( price);

	} else if( currencyPair.getCurrency().equals( CurrencyImpl.LTC)) {

	    // btc has 5 fraction digits
	    DecimalFormat ltcDecimalFormat = new DecimalFormat("#####.#####");
	    
	    return ltcDecimalFormat.format( price);
	} else {
	    throw new CurrencyNotSupportedException( "The currency pair " + currencyPair.getName() + " is not supported in formatPrice()");
	}
    }

    /**
     * Get the current funds of the user via the new trade API.
     *
     * @return The accounts with the current balance as a collection of Account objects, or null if the request failed.
     */
    public Collection<TradeSiteAccount> getAccounts() {

	// Try to get some info on the user (including the current funds).
	JSONObject jsonResponse = authenticatedHTTPRequest( "getInfo", null);

	if( jsonResponse != null) {

	    JSONObject jsonFunds = jsonResponse.getJSONObject( "funds");  // Get the JSONObject for the funds.

	    // An array for the parsed funds.
	    ArrayList<TradeSiteAccount> result = new ArrayList<TradeSiteAccount>();

	    // Now iterate over all the currencies in the funds.
	    for( Iterator currencyIterator = jsonFunds.keys(); currencyIterator.hasNext(); ) {
		
		String currentCurrency = (String)currencyIterator.next();  // Get the next currency.
		
		BigDecimal balance = new BigDecimal( jsonFunds.getString( currentCurrency));  // Get the balance for this currency.

		result.add( new TradeSiteAccountImpl( balance, CurrencyImpl.findByString( currentCurrency.toUpperCase()), this));
	    }

	    return result; // Return the array with the accounts.
	}

	return null;  // The request failed.
    }

    /**
     * Get the accounts with the current funds on this trading site. This is done via HTML here, since
     * this was implemented before the new Trade API.
     *
     * @return The accounts with the current balance as a collection of Account objects, or null if the request failed.
     */
    public Collection<TradeSiteAccount> getAccountsViaHTML() {
	
	String url = "https://" + BtcEClient.DOMAIN + "/ajax/" + "profile.php";

	ensureLogin();  // Make sure, that the user is logged in.
	
	if( _customerId == null) {
	    throw new MissingBtcECustomerIdException( "getFunds: no customer id received from the btc-e.com website.");
	}
	
	if( _currentCookies == null) {
	    throw new MissingBtcECookieException( "No current btc-e.com cookie for getFunds! Please login to get one!");
	}

	try {
	    // Now post the actual profile request as jquery json.
	    Response response = Jsoup.connect( url)
		.data("task", "cass", "data", _customerId)
		.method( Method.POST)
		.cookies( _currentCookies)
		.userAgent( USERAGENT)
		.timeout( TIMEOUT)
		.execute();

	    // Now find the balances in the body.
	    return _htmlParser.findBalances( response.body());

	} catch( IOException ioe) {
	    System.err.println( "Cannot post profile request to the btc-e.com website: " + ioe.toString());
	}

	return null;  // Indicate an error.
    }

    /**
     * Get a page, that requires a logged in user.
     *
     * @param URL The URL of the page.
     *
     * @see http://jsoup.org/cookbook/input/load-document-from-url
     */
    private void getAuthenticatedPage( String URL) {

	try {
	    // Do a HTTP post with the user data to fetch the page.
	    Document doc = Jsoup.connect("https://" + BtcEClient.DOMAIN).data("query", "Java")
		.userAgent( USERAGENT)
		.cookie("auth", "token")
		.timeout( TIMEOUT)
		.post();
	} catch( IOException ioe) {
	    System.err.println( "Cannot post authenticated page to btc-e.com: " + ioe.toString());
	}
    }

    /**
     * Return the current reference currency of the user.
     *
     * @return The current reference currency.
     */
    public Currency getCurrentCurrency() {
	return _currentCurrency;
    }

    /**
     * Get the btc-e string representation of a currency pair.
     *
     * @param currencyPair The currency pair to convert.
     *
     * @return The currency pair as a btc-e string.
     */
    private String getCurrencyPairString( CurrencyPair currencyPair) {
	return currencyPair.getCurrency().getName().toLowerCase() + "_" + currencyPair.getPaymentCurrency().getName().toLowerCase();
    }

    /**
     * Get an address to deposit coins at btc-e.
     *
     * @param currency The currency to deposit.
     *
     * @return The deposit address as a string.
     */
    private String getDepositAddress( Currency currency) {

	// The URL to request the address from.
	String url = null;

	// Check, if the currency is a FIAT currency.
	if( currency.equals( CurrencyImpl.RUR)
	    || currency.equals( CurrencyImpl.EUR)
	    || currency.equals( CurrencyImpl.USD)) {

	    // The FIAT URLs differ from the cryptocoin URLs...
	    url = "https://btc-e.com/profile#funds/deposit/" + currency.getName().toLowerCase();

	} else {

	    // Compute the cryptocoin URL from the currency id.
	    url = "https://btc-e.com/profile#funds/deposit_coin/" + getIdForCurrency( currency);
	}
	
	ensureLogin();  // Make sure, that the user is logged in.
	
	if( _customerId == null) {
	    throw new MissingBtcECustomerIdException( "getDepositAddress: no customer id received from the btc-e.com website.");
	}
	
	if( _currentCookies == null) {
	    throw new MissingBtcECookieException( "No current btc-e.com cookie for getDepositAddress! Please login to get one!");
	}


	// Do a authenticate HTTP post request.
	try {
	    // Now post the actual profile request as jquery json.
	    Response response = Jsoup.connect( url)
		.method( Method.GET)
		.cookies( _currentCookies)
		.userAgent( USERAGENT)
		.timeout( TIMEOUT)
		.execute();

	    // Now find the balances in the body.
	    return _htmlParser.findCoinAddress( response.body());

	} catch( IOException ioe) {
	    System.err.println( "Cannot get deposit address from the btc-e.com website: " + ioe.toString());
	}

	return null;  // Indicate an error.
    }

    /**
     * Get the market depth as a Depth object.
     *
     * @param currency The currency to query
     *
     * @return The market depth.
     *
     * @throws TradeDataNotAvailableException if the depth is not available.
     */
    public Depth getDepth( Currency currency) throws TradeDataNotAvailableException {
	return getDepth( new CurrencyPairImpl( currency, CurrencyImpl.BTC));
    }

    /**
     * Get the market depth as a Depth object.
     *
     * @param currencyPair The queried currency pair.
     *
     * @throws TradeDataNotAvailableException if the depth is not available.
     */
    public Depth getDepth( CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	if( ! isSupportedCurrencyPair( currencyPair)) {
	    throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on Btc-E");
	}

	String url = "https://" + DOMAIN + "/api/2/" 
	    + getCurrencyPairString( currencyPair) 
	    + "/depth";

	String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // Request sucessful?
	    try {
		// Convert the HTTP request return value to JSON to parse further.
		return new BtcEDepth( JSONObject.fromObject( requestResult), currencyPair, this);
	    } catch( JSONException je) {
		System.err.println( "Cannot parse btc-e depth return: " + je.toString());

		throw new TradeDataNotAvailableException( "cannot parse data from btc-e");
	    }
	}
	
	throw new TradeDataNotAvailableException( "btc-e server did not respond to depth request");
	
	// return null;  // The depth request failed.
    }

    /**
     * Get the fee for an order in the resulting currency.
     *
     * @param order The order to use for the fee computation.
     *
     * @return The fee in the resulting currency (currency value for buy, payment currency value for sell).
     */
    public Price getFeeForOrder( SiteOrder order) {
	
	if( order instanceof WithdrawOrder) {

	    if( order.getCurrencyPair().getCurrency().equals( CurrencyImpl.BTC)) {
		return new Price( "0.01");  // Withdrawal in btc seem to cost always 0.01 btc ?
	    } else {
		System.out.println( "Compute withdaw fees for currencies other than btc");

		throw new CurrencyNotSupportedException( "Cannot compute fee for this order: " + order.toString());
	    }
	} else if( order instanceof DepositOrder) {

	    Currency depositedCurrency = ((DepositOrder)order).getCurrency();
	    
	    if( depositedCurrency.equals( CurrencyImpl.BTC)) {
		
		// BTC deposits are free as far as I know.
		return new Price( "0.0", CurrencyImpl.BTC);
	    
	} else {

		throw new NotYetImplementedException( "Deposit fees are not implemented for trade site " 
						      + getName() 
						      + " and currency " 
						      + depositedCurrency.getName());
	    }

	} else {  // Just the default implementation for the other order forms.

	    return super.getFeeForOrder( order);
	}
    }

    /**
     * Get id for a pair of currencies.
     * Look at http://bitcoin.stackexchange.com/questions/1393/does-btc-e-have-an-api-for-alternate-currencies
     * for more info.
     *
     * @param currency The first currency to trade.
     * @param paymentCurrency The currency to use for payment.
     *
     * @return The id for this currency pair, or -1 if the id is not known.
     */
    /*    private short getIdForCurrencies( BtcECurrency currency, BtcECurrency paymentCurrency) {

	if( paymentCurrency == BtcECurrency.BTC) {
	    switch( currency) {
	    case IXC: return 2;
	    case I0C: return 4;
	    case SC: return 5;
	    case GG: return 7;
	    case TBX: return 8;
	    case FBX: return 9;
	    case LTC: return 10;
	    case RUC: return 11;
	    case NMC: return 13;
	    case CLC: return 15;
	    case DVC: return 16;
	    }
	} else if( paymentCurrency == BtcECurrency.USD) {
	    switch( currency){
	    case BTC: return 1;
	    case SC: return 6;
	    case RUC: return 12;
	    case LTC: return 14;
	    }
	}

	return -1;
	} */

    /**
     * Get id for a pair of currencies.
     * Look at http://bitcoin.stackexchange.com/questions/1393/does-btc-e-have-an-api-for-alternate-currencies
     * for more info.
     *
     * @param currencyPair The currency pair to trade.
     *
     * @return The id for this currency pair, or -1 if the id is not known.
     */
    private short getIdForCurrencies( CurrencyPair currencyPair) {

	if( (CurrencyImpl)currencyPair.getPaymentCurrency() == CurrencyImpl.BTC) {
	    switch( (CurrencyImpl)currencyPair.getCurrency()) {
	    case LTC: return 10;
	    }
	} else if( (CurrencyImpl)currencyPair.getPaymentCurrency() == CurrencyImpl.USD) {
	    switch( (CurrencyImpl)currencyPair.getCurrency()){
	    case BTC: return 1;
	    case LTC: return 14;
	    }
	}

	throw new CurrencyNotSupportedException( "Currency pair: " 
						 + currencyPair.getCurrency().getName() 
						 + " with payment currency: " 
						 + currencyPair.getPaymentCurrency().getName() 
						 + " not supported in BtcEClient.getIdForCurrencies");

	// return -1;
    }

    /**
     * Get the id of the combination of given currency and current currency.
     *
     * @param currency The currency to query.
     */
    private final short getIdForCurrency( Currency currency) {

	switch( (CurrencyImpl)currency) {
	case BTC: return 1;
	case LTC: return 8;
	case RUC: return 9;
	case NMC: return 10;
	case NVC: return 13;
	}

	throw new CurrencyNotSupportedException( "Currency: " 
						 + currency.getName() 
						 + " not supported in BtcEClient.getIdForCurrency");
    }

    /**
     * Get the shortest allowed requet interval in microseconds.
     *
     * @return The shortest allowed request interval in microseconds.
     */
    public long getMinimumRequestInterval() {
	return getUpdateInterval();
    }

    /**
     * Get the open orders on this trade site.
     *
     * @return The open orders as a collection, or null if the request failed.
     */
    public Collection<SiteOrder> getOpenOrders() {

	// Set the parameters for the order list request.
	Map< String, String> parameters = new HashMap< String, String>();

	parameters.put( "active", "1");  // This is actually the default anyway, but it can't hurt...

	// Try to get some info on the open orders.
	JSONObject jsonResponse = authenticatedHTTPRequest( "OrderList", parameters);

	if( jsonResponse != null) {  // If the request succeeded.

	    // Create a buffer for the result.
	    ArrayList<SiteOrder> result = new ArrayList<SiteOrder>();

	    // The answer is an assoc array with the site id's as the key and a json object with order details as the values.
	    for( Iterator keyIterator = jsonResponse.keys(); keyIterator.hasNext(); ) {

		// Get the next site id from the iterator.
		String currentSiteId = (String)( keyIterator.next());

		// Since we know the tradesite and the site id now, we can query the order book for the order.
		SiteOrder currentOrder = CryptoCoinOrderBook.getInstance().getOrder( this, currentSiteId);

		if( currentOrder != null) {     // If the order book returned an order,
		    result.add( currentOrder);  // add it to the result buffer.
		} else {  // It seems, this order is not in the order book. I can consider this an error at the moment,
		          // since every order should go through the order book.

		    throw new OrderNotInOrderBookException( "Error: btc-e order with site id " + currentSiteId + " is not in order book!");
		}
	    }

	    return result;  // Return the buffer with the orders.	    
	} 

	return null;  // An error occured.
    }

    /**
     * Get the btc-e password of the user.
     *
     * @return The btc-e password of the user.
     */
    public String getPassword() {
	return _password;
    }

    /**
     * Get the section name in the global property file.
     */
    public String getPropertySectionName() {
	return "BtcE";
    }

    /**
     * Get the settings of the btc-e client.
     *
     * @return The setting of the btc-e client as a list.
     */
    public PersistentPropertyList getSettings() {

	// Get the settings from the base class.
	PersistentPropertyList result = super.getSettings();

	result.add( new PersistentProperty( "Username", null, _username, 8));
	result.add( new PersistentProperty( "Password", null, _password, 7));
	result.add( new PersistentProperty( "Key", null, _key, 6));        // The key
	result.add( new PersistentProperty( "Secret", null, _secret, 5));  // and secret for the new trade API.

	return result;
    }

    /**
     * Get the current ticker from the btc-e API.
     *
     * @param currencyPair The currency pair to query.
     * @param paymentCurrency The currency for the payments.
     *
     * @return The current btc-e ticker.
     *
     * @throws TradeDataNotAvailableException if the ticker is not available.
     */
    public BtcETicker getTicker( CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	if( ! isSupportedCurrencyPair( currencyPair)) {
	    throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on Btc-E");
	}
	
	String url = "https://" + DOMAIN + "/api/2/" 
	    + getCurrencyPairString( currencyPair)
	    + "/ticker";

	String requestResult = HttpUtils.httpGet( url);
	
	if( requestResult != null) {  // Request sucessful?
	    try {
		// Convert the HTTP request return value to JSON to parse further.
		return new BtcETicker( JSONObject.fromObject( requestResult), currencyPair, this);
	    } catch( JSONException je) {
		System.err.println( "Cannot parse ticker object: " + je.toString());
	    }
	}
	
	throw new TradeDataNotAvailableException( "The btc-e ticker request failed");
	// return null;  // The ticker request failed.
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
    public CryptoCoinTrade [] getTrades( long since_micros, CurrencyPair currencyPair) throws TradeDataNotAvailableException {

	if( ! isSupportedCurrencyPair( currencyPair)) {
	    throw new CurrencyNotSupportedException( "Currency pair: " + currencyPair.toString() + " is currently not supported on Btc-E");
	}

	String url = "https://" + DOMAIN + "/api/2/" 
	    + getCurrencyPairString( currencyPair)
	    + "/trades";

	// System.out.println( "Fetching btc-e trades from: " + url);

	CryptoCoinTrade [] tempResult =  getTradesFromURL( url, currencyPair);

	if( tempResult != null) {
	    // Now filter the trades for the timespan.
	    ArrayList<CryptoCoinTrade> resultBuffer = new ArrayList<CryptoCoinTrade>();
	    for( int i = 0; i < tempResult.length; ++i) {
		if( tempResult[i].getTimestamp() > since_micros) {
		    resultBuffer.add( tempResult[i]);
		}
	    }
	
	    // Now convert the buffer back to an array and return it.
	    return resultBuffer.toArray( new CryptoCoinTrade[ resultBuffer.size()]);
	}

	throw new TradeDataNotAvailableException( "trades request on btc-e failed");
    }

    /**
     * Get a list of trades from a URL.
     *
     * @param url The url to fetch the trades from.
     * @param currencyPair The requested currency pair.
     *
     * @return A list of trades or null, if an error occurred.
     */
    private CryptoCoinTrade [] getTradesFromURL( String url, CurrencyPair currencyPair) {
	ArrayList<CryptoCoinTrade> trades = new ArrayList<CryptoCoinTrade>();

        String requestResult = HttpUtils.httpGet( url);

	if( requestResult != null) {  // If the HTTP request worked ok.
	    try {
		// Convert the result to an JSON array.
		JSONArray resultArray = JSONArray.fromObject( requestResult);
		
		// Iterate over the array and convert each trade from json to a Trade object.
		for( int i = 0; i < resultArray.size(); i++) {
		    JSONObject tradeObject = resultArray.getJSONObject(i);
		    
		    trades.add( new BtcETradeImpl( tradeObject, this, currencyPair));  // Add the new Trade object to the list.
		}

		CryptoCoinTrade [] tradeArray = trades.toArray( new CryptoCoinTrade[ trades.size()]);  // Convert the list to an array.
		
		return tradeArray;  // And return the array.

	    } catch( JSONException je) {
		System.err.println( "Cannot parse trade object: " + je.toString());
	    }
	}

	return null;  // An error occured.
    }

    /**
     * Get the interval, in which the trade site updates it's depth, ticker etc. 
     * in microseconds.
     *
     * @return The update interval in microseconds.
     */
    public long getUpdateInterval() {
	return 15L * 1000000L;  // The default btc-e update happens every 15s, I think.
    }

    /**
     * Get the btc-e username of the user.
     *
     * @return The btc-e username of the user.
     */
    public String getUsername() {
	return _username;
    }

    /**
     * Login the user to btc-e.
     */
    private boolean doUserLogin( String username, String password) {

	if( ( username == null) || "".equals( username)) {
	    throw new MissingBtcELoginDataException( "username is null or empty in doUserLogin");
	}

	if( ( password == null) || "".equals( password)) {
	    throw new MissingBtcELoginDataException( "password is null or empty in doUserLogin");
	}

	try {
	    // Get the btc-e response to posting the login form.
	    Response res = Jsoup.connect( "https://" + BtcEClient.DOMAIN + "/login")
		.data( "login", username, "password", password)
		.method( Method.POST)
		.timeout( TIMEOUT)
		.userAgent( USERAGENT)
		.execute();

	    // Get the current cookies from the response.
	    _currentCookies = res.cookies();

	    // Find the customer id in the response.
	    if( ( _customerId = _htmlParser.findCustomerId( res.body())) == null) {  // If we cannot find the customer id in the
		return false;                                                        // response, the login has failed most likely...
	    }

	    // Find the token in the response.
	    if( ( _currentToken =_htmlParser.findToken( res.body())) == null) {  // If the cannot find the token in the response,
		return false;                                                    // the login has failed most likely...
	    }
 
	    // Just print the reponse for further hacking.
	    // System.out.println( "Login reponse: " + res.body());

	    _isLoggedIn = true;  // It seems the login went ok.

	    return true;

	} catch( IOException ioe) {
	    System.err.println( "Cannot connect to the btc-e.com server: " + ioe.toString());
	}	

	return false;  // Login failed.
    }

    /**
     * Make sure, that the user is logged in.
     *
     * @return true, if the login was succesful, false otherwise.
     */
    private boolean ensureLogin() {

	if( ! _isLoggedIn) {  // if we are not logged in yet.

	    doUserLogin( getUsername(), getPassword());
	}

	return false;  // Login failed.
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

	return true;  // Just a dummy for now, but btc-e is quite relaxed on request limits...
    }

    /**
     * Set a new reference currency for the user.
     *
     * @param currency The new currency to use for display of data.
     */
    public void setCurrentCurrency( Currency currency) {
	_currentCurrency = currency;
    }

    /**
     * Set new settings for the btc-e client.
     *
     * @param settings The new settings for the btc-e client.
     */
    public void setSettings( PersistentPropertyList settings) {
	
	super.setSettings( settings);
	
	String key = settings.getStringProperty( "Key");
	if( key != null) {
	    _key = key;  // Get the API key from the settings.
	}
	String secret =  settings.getStringProperty( "Secret");
	if( secret != null) {
	    _secret = secret;  // Get the secret from the settings.
	}
	String user = settings.getStringProperty( "Username");
	if( user != null) {
	    _username = user;  // Get the username from the settings.
	}
	String password = settings.getStringProperty( "Password");
	if( password != null) {
	    _password = password;  // Get the password from the settings.
	}
    }

    /**
     * Return a string for this site (just a name for now).
     * To be used in the project tree.
     */
    public String toString() {
	return getName();
    }
}
