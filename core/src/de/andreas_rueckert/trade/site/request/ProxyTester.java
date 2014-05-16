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

import de.andreas_rueckert.util.LogUtils;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Vector;


/**
 * This class implements methods to test proxy servers.
 */
class ProxyTester {

    // Inner classes

     /**
     * Order monitor thread.
     */
    class ProxyTestThread extends Thread {

        // Instance variables

	/**
	 * The proxy to test.
	 */
	private RatedProxy _proxy;


        // Constructors

	/**
	 * Create a new thread to test a proxy.
	 *
	 * @param proxy The proxy to test.
	 */
	ProxyTestThread( RatedProxy proxy) {

	    _proxy = proxy;
	}

	
        // Methods

        /**
         * The actual code of the the thread.
         */
        @Override public void run() {

	    if( ProxyTester.this.test( _proxy)) {  // If the proxy passes the test

		ProxyTester.this._resultBuffer.add( _proxy);  // add it to the result buffer.
	    }
	    
	    // Remove this thread from the list of running threads.
	    ProxyTester.this._runningTestThreads.remove( this);
	}	
    }


    // Static variables

    /**
     * A maximal number to concurrent proxy test threads.
     */
    private static final int MAX_TEST_THREADS = 1200;

    /**
     * The URL of a site to connect to, so we can see, if
     * the proxy server works as expected.
     */
    private static String TEST_SITE = "http://blanksite.com/";

    /**
     * The only instance of this class (singleton pattern).
     */
    private static ProxyTester _instance = null;


    // Instance variables

    /**
     * Use a vector for the result buffer, since it is synchronized.
     */
    private Vector< RatedProxy> _resultBuffer = new Vector< RatedProxy>();

    /**
     * Create a new list to store all the running test threads.
     */
    private Vector <ProxyTestThread> _runningTestThreads = new Vector< ProxyTestThread>();


    // Constructors

    
    // Methods

    /**
     * Get the only instance of this class (singleton pattern).
     *
     * @return The only instance of this class.
     */
    public static ProxyTester getInstance() {

	if( _instance == null) {  // If there is no instance yet,

	    _instance = new ProxyTester();  // create one.
	}

	return _instance;
    }

    /** 
     * Get the URL of a test website to connect to.
     *
     * @return The address of a website to connect to.
     */
    public String getTestWebsite() {

	// It might be smart to use a website, that is close to the proxy server,
	// but for now I just use always the same site.
	return TEST_SITE;
    }
    
    /**
     * Test a list of proxies, if they are working.
     *
     * @param proxies The proxies to test.
     *
     * @param A collection with the working proxies as a list.
     */
    public final synchronized Collection<RatedProxy> test( ArrayList<RatedProxy> proxies) {
	
	// Set the number of running threads to 0.
	_runningTestThreads.clear();
	
	// Clear the result buffer.
	_resultBuffer.clear();

	// Loop over the proxies to test.
	for( RatedProxy currentProxy : proxies) {

	    // While there are already too many test threads running.
	    while( _runningTestThreads.size() >= MAX_TEST_THREADS) {

		try {
		    Thread.sleep( 100);   // Wait for test threads to be finished.

		} catch( InterruptedException ie) {

		    // No need to do anything here, I guess.
		}
	    }

	    // Start another test thread.
	    ProxyTestThread newTestThread = new ProxyTestThread( currentProxy);
	    newTestThread.start();

	    // Add this thread to the list of running threads.
	    _runningTestThreads.add( newTestThread);
	    
	    // Wait a bit after every new test thread.
	    // This is just, so we don't overload the internet...and the exchange... :o)
	    try {
		Thread.sleep( 10);  
		    
	    } catch( InterruptedException ie) {
		
		// No need to do anything here, I guess.
	    }
	}

	// Now finish the last running test threads.
	while( _runningTestThreads.size() > 0) {

	    // Just wait a bit until more threads have finished. No join necessary?
	    try {
		Thread.sleep( 10);  
		    
	    } catch( InterruptedException ie) {
		
		// No need to do anything here, I guess.
	    }
	}

	return _resultBuffer;  // Return the result buffer.
    }

    /**
     * Test a proxy server, if it is working.
     *
     * @see http://stackoverflow.com/questions/2793150/how-to-use-java-net-urlconnection-to-fire-and-handle-http-requests
     * @see http://stackoverflow.com/questions/1432961/how-do-i-make-httpurlconnection-use-a-proxy
     * @see http://stackoverflow.com/questions/8030908/how-to-check-if-proxy-is-working-in-java
     *
     * @return true, if the proxy worked. False otherwise.
     */
    public final boolean test( RatedProxy proxy) {
	
	try {

	    // Start with a check, if the proxy address is reachable.
	    if( ! ( (InetSocketAddress)( proxy.address())).getAddress().isReachable(10000)) {  // Started with 15000, but it's quite slow.
		
		return false;  // Cannot connect to proxy.
	    }

	    /* System.out.println( "Using proxy: " 
				+ ((InetSocketAddress)proxy.address()).getAddress() 
				+ " with port " 
				+ ((InetSocketAddress)proxy.address()).getPort()); */

	    // Create a connection to the test URL, so we can see if the proxy works.
	    HttpURLConnection connection = (HttpURLConnection)( new URL( getTestWebsite()).openConnection( proxy));	
       
	    if( connection != null) {
		
		// Set some connection properties, so the connection will most likely go through
		// and will timeout otherwise in an reasonable amount of time.
		connection.setFollowRedirects( false);
		connection.setConnectTimeout( 10 * 1000);
		connection.setReadTimeout( 10 * 1000);
		connection.setRequestMethod("GET");
		connection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");

		// Try to connect to the test website.
		connection.connect();

		// Check, if we are actually using a proxy, or the handler ignored the proxy argument.
		if( ! connection.usingProxy()) {

		    connection.disconnect();  // Terminate the connection.

		    return false;  // Proxy usage failed.
		}

		// Get the response code from the code.
		int responseCode = connection.getResponseCode();
	    
		// Disconnect from the test website.
		connection.disconnect();
		
		// If the response worked, return true. False otherwise.
		return ( responseCode == HttpURLConnection.HTTP_OK);
	    }
	    
	    return false;  // No connection => proy server doesn't seem to work.

	} catch( IOException ioe) {

	    return false;  // Ignore the details of the error. Just return false.
	}
    } 
}