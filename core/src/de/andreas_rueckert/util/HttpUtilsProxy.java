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

package de.andreas_rueckert.util;

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.trade.site.request.NoProxyAvailableException;
import de.andreas_rueckert.trade.site.request.ProxyRequestHandler;
import de.andreas_rueckert.trade.site.request.ProxyRequestResult;
import de.andreas_rueckert.trade.site.request.ProxyRequestResultType;
import de.andreas_rueckert.trade.site.request.RatedProxy;
import de.andreas_rueckert.trade.site.TradeSite;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;
import java.util.Random;


/**
 * These are the 'next generation' HTTP utils.
 * They should use proxies where prossible and
 * optimize the requests for maximum performance.
 */
public class HttpUtilsProxy {

    // Inner classes

    

    // Static variables

    /**
     * The only instance of this class (singleton pattern).
     */
    private static HttpUtilsProxy _instance = null;

    /**
     * The maximum number of attempts to reach a service.
     */
    private final int MAX_ATTEMPTS = 5;


    // Instance variables

    /**
     * Random generator for user agent strings.
     */
    Random _random = new Random();

    /**
     * Some popular user agent strings to select a random one from.
     */
    String [] _userAgentStrings = { "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_1) AppleWebKit/537.73.11 (KHTML, like Gecko) Version/7.0.1 Safari/537.73.11"
				    ,"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.107 Safari/537.36"
				    , "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.107 Safari/537.36"
				    , "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:26.0) Gecko/20100101 Firefox/26.0"
				    , "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.76 Safari/537.36"
				    , "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.102 Safari/537.36"
				    , "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.102 Safari/537.36"
				    , "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0"
				    , "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.77 Safari/537.36"
				    , "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.107 Safari/537.36"
				    , "Mozilla/5.0 (iPhone; CPU iPhone OS 7_0_4 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11B554a Safari/9537.53"};


    // Constructors

    /**
     * Private constructor for singleton pattern.
     */
    private HttpUtilsProxy() {
    }


    // Methods

    /**
     * Get the only instance of this class (singleton pattern).
     *
     * @return The only instance of this class.
     */
    public final static HttpUtilsProxy getInstance() {

	if( _instance == null) {               // If there is no instance yet,

	    _instance = new HttpUtilsProxy();  // create a new one.
	}

	return _instance;  // Return the instance.
    }

    /**
     * Perform a HTTP get for a given tradesite
     *
     * @param url The URL to connect to.
     * @param headerlines Additional parameters for the HTTP header.
     * @param tradeSite The trade site to connect to.
     *
     * @return The result as a ProxyRequestResult object.
     */
    public ProxyRequestResult httpGet( String url, Map< String, String> headerlines, TradeSite tradeSite) {

	URL requestURL;
	HttpURLConnection connection;

	int attempts = 0;  // The number of attempts to reach the service.

	StringBuffer result = new StringBuffer();  // Create a string buffer for the result;


	// ToDo: wait some random time to give the request some human touch.?

	try { 

	    requestURL = new URL( url);
	    
	} catch( MalformedURLException me) {
	    
	    LogUtils.getInstance().getLogger().error( "URL format error: " + url);
	    
	    return new ProxyRequestResult( ProxyRequestResultType.ILLEGAL_URL_FORMAT);
	}

	// The currently used proxy.
	RatedProxy proxy;


	while( attempts++ < MAX_ATTEMPTS) {  // While we didn't reach the maximum number of attempts.

	    try {  
		
		// Ask the proxy handler to give us a proxy for the given trade site.
		proxy = ProxyRequestHandler.getInstance().recommendProxy( tradeSite);

	    } catch( NoProxyAvailableException npae) {  // The proxy handler cannot return a proxy for this trade site.

		// Return a result, that no proxy is available for this request.
		return new ProxyRequestResult( ProxyRequestResultType.NO_PROXY_AVAILABLE);

	    }

	    // Check the timestamp of the last request of the given proxy and wait
	    // until we can do the actual request.
	    long lastRequestTimestamp = proxy.getLastRequestTimestamp( tradeSite);

	    if( lastRequestTimestamp != -1) {  // If there were requests before, wait until we can do another request.

		// Compute the timestamp for the next possible request.
		long nextRequestTimestamp = lastRequestTimestamp + tradeSite.getMinimumRequestInterval();

		long currentTime = TimeUtils.getInstance().getCurrentGMTTimeMicros();

		if( currentTime < nextRequestTimestamp) {  // If we cannot do a request yet.

		    try {  // Wait the difference (in milliseconds here).
		    
			// To make sure, that we don't run into rounding problems, add 1 millisecond.
			Thread.sleep( ( nextRequestTimestamp - currentTime) / 1000 + 1);
		    
		    } catch( InterruptedException ie) {  // This would be a real problem, 
			// because we don't want to request early and get banned!

			// return new ProxyRequestResult( ProxyRequestResultType.FAILURE, null, "Waiting for the next allow request timestamp failed.");

			continue;  // Do another attempt to fetch the data.
		    }	
		}
	    }

	    try {

		// Start with a check, if the proxy address is reachable.
		if( ! ( (InetSocketAddress)( proxy.address())).getAddress().isReachable(10000)) {  // Started with 15000, but it's quite slow.
			
		    // Get the rating of this proxy.
		    if( proxy.getRating() == RatedProxy.MIN_RATING) {  // If the proxy already has the minimum rating.
			    
			proxy.setActive( false);  // Deactivate the proxy.
			
		    } else {  // This proxy already has a rating better than the minimum rating.

			proxy.rateDown();  // Rate it down.
		    }
		
		    // return new ProxyRequestResult( ProxyRequstResultType.PROXY_NOT_AVAILABLE);  // Cannot connect to proxy.
		    
		    continue;  // Do another attempt with another proxy.
		}

		connection = (HttpURLConnection)requestURL.openConnection( proxy);
		
	    } catch( IOException ioe) {
		
		LogUtils.getInstance().getLogger().error( "Cannot open URL: " + url);
		
		// return new ProxyRequestResult( ProxyRequestResultType.IO_ERROR);

		continue;  // Do another attempt to fetch the data.
	    }
		
	    try {

		connection.setFollowRedirects( false);
		
		connection.setConnectTimeout( 15 * 1000);  // 15 seconds should be enough for a working exchange.
		// @see http://stackoverflow.com/questions/3163693/java-urlconnection-timeout
		connection.setReadTimeout( 10 * 1000);
		    
		connection.setRequestMethod("GET");

		// Add a randomly selected user agent string.
		connection.setRequestProperty( "User-Agent", _userAgentStrings[ _random.nextInt( _userAgentStrings.length)]);
      
		// Add the additional headerlines, if there were any given.
		if( headerlines != null) {
		    for( Map.Entry<String, String> entry : headerlines.entrySet()) {
			connection.setRequestProperty( entry.getKey(), entry.getValue());
		    }
		}

		// Try to connect to the test website.
		connection.connect();

		// Do the request and check before, if the proxy is used in the connection!
		BufferedReader reader = new BufferedReader( new InputStreamReader( connection.getInputStream()));

		String currentLine;  // Buffer for the current input line.

		while( ( currentLine = reader.readLine()) != null) {
		    result.append( currentLine);
		}
		reader.close();
		
	    } catch( ProtocolException pe) {
		
		LogUtils.getInstance().getLogger().error( "Wrong protocol for URL: " + pe.toString());
		
		return new ProxyRequestResult( ProxyRequestResultType.PROTOCOL_NOT_SUPPORTED);
		
	    } catch( IOException ioe) {
		    
		LogUtils.getInstance().getLogger().error( "I/O error while reading from URL: " + url + "\n" + ioe.toString());
		
		/*
		  Scanner scanner = new Scanner( connection.getErrorStream());  // Get a stream for the error message.
		  
		  scanner.useDelimiter("\\Z");
	      
		  String response = scanner.next();  // Get the error message as text.
		  
		  System.out.println( "DEBUG: Server error: " + response); */
		
		// return new ProxyRequestResult( ProxyRequestREsultType.IO_ERROR);
		    
		continue;  // Do another attempt to fetch the data.
		
	    } finally {
		
		if( connection != null) {      // If there is still a connection, 
		    
		    connection.disconnect();   // close it.
			
		}
	    } 

	    // Update the request statistics
	    
	    // Convert the result to a single string.
	    return new ProxyRequestResult( ProxyRequestResultType.SUCCESS, result.toString());
	}

	// After some attempts, we just give up...
	return new ProxyRequestResult( ProxyRequestResultType.OUT_OF_ATTEMPTS);
    }
}
