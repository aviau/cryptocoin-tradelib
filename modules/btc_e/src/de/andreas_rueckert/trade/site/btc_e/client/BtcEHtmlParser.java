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

import de.andreas_rueckert.trade.account.Account;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.account.TradeSiteAccountImpl;
import de.andreas_rueckert.trade.currency.CurrencyImpl;
import de.andreas_rueckert.trade.currency.CurrencyNotSupportedException;
import de.andreas_rueckert.trade.currency.CurrencyProvider;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import net.sf.json.JSONObject;


/**
 * HTML help class to post, fetch and parse the btc-e HTML pages.
 * See https://btc-e.com/js/core.js for the btc-e methods.
 */
class BtcEHtmlParser {

    // Static variables


    // Instance variables

    /**
     * The reference to the btc-e client.
     */
    private BtcEClient _btcEClient = null;


    // Constructors

    /**
     * Create a new HTML parser helper.
     *
     * @param btcEClient The btc-e.com client.
     */
    BtcEHtmlParser( BtcEClient btcEClient) {
	_btcEClient = btcEClient;
    }


    // Methods

    /**
     * Find the balances in a profile page and create account objects for each balance.
     *
     * @param httpResponseBody The html fragment with the balances.
     *
     * @return The account objects for the funds.
     */
    Collection<TradeSiteAccount> findBalances( String httpResponseBody) {

	ArrayList<TradeSiteAccount> result = new ArrayList<TradeSiteAccount>();

	// Try to match strings like: Balance: <b class='red'>0</b> BTC

	String patternString = "Balance: <b class\\='red'>([\\-|0-9|\\.]*)</b> ([A-Z]*)";

	// Create a pattern for the profile 'onClick' link.
	Pattern pattern = Pattern.compile( patternString);

	// Now create a matcher for the customer id.
	Matcher matcher = pattern.matcher( httpResponseBody);

	// Iterate over all the matches.
	while( matcher.find()) {
	    String currency = matcher.group( 2);
	    BigDecimal balance = new BigDecimal( matcher.group( 1));

	    result.add( new TradeSiteAccountImpl( balance, CurrencyProvider.getInstance().getCurrencyForCode( currency), _btcEClient));
	}

	return result;  // Return the funds.
    }

    /**
     * Find a coin address in a HTTP response.
     *
     * @param The HTTP response as a string.
     *
     * @return The found coin address or null, if there was no address found.
     */
    String findCoinAddress( String httpResponseBody) {

	// Use jsoup to find the token,
	Document document = Jsoup.parse( httpResponseBody);

	// btc-e encodes the address like
	// <div id="coin-address" class="coin_adress"> <...address...></div>

	Elements coinDivs = document.select( "div[id=coin-address]");  // Find input tags with id="token"

	if( coinDivs.size() > 0) {  // Matching div tags found?

	    String address = coinDivs.get( 0).text();  // Get the enclosed text from the first matching div tag.

	    if( "".equals( address)) {  // Address found?
		return null;            // Nope => return error...
	    }

	    // System.out.println( "Found address: " + address);

	    return address;  // Return the found address.
	}
	
	return null;  // No coin address found in the HTTP response.
    }

    /**
     * Try to find the customer id in the HTTP response.
     *
     * @param httpResponseBody The HTTP response body as text.
     *
     * @return The customer id as a String object, or null, if no id was found.
     */
    String findCustomerId( String httpResponseBody) {

	// I don't know yet, how to find the Javascript argument with jsoup, so I used
	// Java regex for now...
	
	String patternString = "profile\\(\"cass\", ([0-9]*), 0\\)";
    
	// Create a pattern for the profile 'onClick' link.
	Pattern pattern = Pattern.compile( patternString);

	// Now create a matcher for the customer id.
	Matcher matcher = pattern.matcher( httpResponseBody);

	if( matcher.find()) {  // If the customer id is in the HTTP response,

	    String id =  matcher.group( 1);  // store it in a temp var.

	    // If there was no match, return null and not an empty string!
	    return "".equals( id) ? null : id;
	}

	return null;         // Customer id not found...seems like login failed 
	                      // or btc-e has changed the website.
    }

    /**
     * Try to find the token in the HTTP response.
     *
     * @param httpResponseBody The HTTP response body as text.
     *
     * @return The token as a String object, or null if no token was found.
     */
    String findToken( String httpResponseBody) {

	// Use jsoup to find the token,
	Document document = Jsoup.parse( httpResponseBody);
	
	Elements inputFields = document.select( "input[id=token]");  // Find input tags with id="token"

	if( inputFields.size() > 0) {  // Matching input fields found?

	    String token = inputFields.get( 0).attr( "value");  // Get the value attr. from the first matching tag.

	    if( "".equals( token)) {  // Token found?
		return null;         // Nope => return error...
	    }

	    // System.out.println( "Found token: " + token);

	    return token;  // Return the found token.
	}
	
	// The following code is the Java regex pattern alternative of the jsoup code.

	// Create a pattern for the hidden token input field.
	// Pattern pattern = Pattern.compile( "<input id\\='token' type\\='hidden' value\\='([a-zA-Z0-9]*)' />");

	// Now create a matcher for the token.
	// Matcher matcher = pattern.matcher( httpResponseBody);

	// if( matcher.find()) {  // If the token is in the HTTP response,
	//     _currentToken = matcher.group( 1);  // store it in the token instance var.

	//    return true;  // token found!
	// }

	return null;         // Token not found...seems like login failed 
	                      // or btc-e has changed the website.
    }
}
