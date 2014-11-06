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

import de.andreas_rueckert.NotYetImplementedException;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.util.LogUtils;
import de.andreas_rueckert.util.TimeUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


/**
 * A RequestHandler implementation using proxy servers to do many requests in parallel.
 */
public class ProxyRequestHandler {

    // Inner classes


    // Static variables

    /**
     * The only instance of this class (singleton pattern).
     */
    private static ProxyRequestHandler _instance = null;


    // Instance variables

    /**
     * A map to assign a proxy list to each trade site.
     */
    private Map< TradeSite, TradeSiteProxyList> _tradeSiteProxies = new HashMap< TradeSite, TradeSiteProxyList>();

    /**
     * A list of available proxy servers.
     */
    private ArrayList<RatedProxy> _proxyServers = new ArrayList<RatedProxy>();


    // Constructors

    /**
     * Private constructor for singleton pattern.
     */
    private ProxyRequestHandler() {
    }


    // Methods

    /**
     * Add a new proxy server to the list of available proxy servers.
     *
     * @param newProxyServer The new proxy server to add.
     */
    public final void addProxyServer( RatedProxy newProxyServer) {

	// Check against the complete list of proxies, if this proxy
	// was not already made available to the handler.
	if( ! _proxyServers.contains( newProxyServer)) {

	    // Add the new server to the list of proxy servers.
	    _proxyServers.add( newProxyServer);

	    // Make the new proxy server available for all the trade site.
	    for( TradeSiteProxyList proxyList  : _tradeSiteProxies.values()) {

		proxyList.addProxy( newProxyServer);
	    }
	}
    }

    /**
     * Add a new trade site to deliver proxies for.
     *
     * @param tradeSite The new trade site add.
     */
    public final void addTradeSite( TradeSite tradeSite) {

	// Create a new proxy list for this trade site.
	TradeSiteProxyList proxyList = new TradeSiteProxyList( tradeSite);

	// Make the already available proxies available to this trade site.
	for( RatedProxy proxy : _proxyServers) {

	    proxyList.addProxy( proxy);
	}

	// Add a new list for the proxies of this trade site.
	_tradeSiteProxies.put( tradeSite, proxyList);

    }

    /**
     * Get the only instance of this class (singleton pattern).
     *
     * @return The only instance of this class (singleton pattern.
     */
    public static ProxyRequestHandler getInstance() {

	if( _instance == null) {   // If there is no instance yet,

	    _instance = new ProxyRequestHandler();  // create one.
	}

	return _instance;  // And return it.
    }

    /**
     * Check, if a trade site is already supported by the handler.
     *
     * @param tradeSite The trade site to check.
     *
     * @return true, if the trade site is already supported. False otherwise.
     */
    public boolean isSupportedTradeSite( TradeSite tradeSite) {

	// If we have a proxy list for this trade site, it's supported.
	return _tradeSiteProxies.containsKey( tradeSite);
    }

    /**
     * Get a proxy for a given trade site.
     *
     * @param tradeSite The trade site, we want to query.
     *
     * @return A proxy to use for this trade site.
     *
     * @throws NoProxyAvailableException if no proxy for this trade site is avaiable.
     */
    public RatedProxy recommendProxy( TradeSite tradeSite) throws NoProxyAvailableException {

	// Get the proxy list for this trade site.
	TradeSiteProxyList proxyList = _tradeSiteProxies.get( tradeSite);

	// Check if this trade site is already supported.
	if( proxyList == null) {

	    // Nope => just add the trade site quietly.
	    addTradeSite( tradeSite);

	    // Now do another attempt to get a proxy list.
	    proxyList = _tradeSiteProxies.get( tradeSite);

	    // If it still doesn't work, give up.
	    if( proxyList == null) {

		throw new NoProxyAvailableException( "There is no proxy list for " 
						     + tradeSite.getName()
						     + " in the ProxyRequestHandler and I cannot create one. Giving up!");
	    }
	}

	// Get a recommendation from the proxy list. This might also throw an exception, if we
	// cannot find a proxy, that works.
	return proxyList.recommendProxy();
    }

    /**
     * Get a number of recommended proxies for the next requests to a given trading site.
     * If not enough working proxies aer available, the length of the array might be < numberOfProxies!
     *
     * @param tradeSite The trade site, we want to connect to.
     * @param numberOfProxies The number of proxies, we need for our requests.
     *
     * @return An array with proxies. The number might be smaller than the requested number of proxies.
     *         And the requesting code must check the timestamp of the last request and wait eventually
     *         until the next request is done!
     */
    public RatedProxy [] recommendProxies( TradeSite tradeSite, int numberOfProxies) {

	// Record the returned proxies in the request list, although we don't really know yet,
	// if they are used actually. But the client might request more proxies even before
	// they are used, and the handler should return different proxies then.

	// Sort the proxies according to their last requests, so the first proxy is 
	// the one, that could be used first.

	throw new NotYetImplementedException( "Getting a proxy is not yet implemented");
    }

    /**
     * Read a CSV file from checkedproxylists.org.
     *
     * @param file The CSV file.
     *
     * @return true, if the file was successfully parsed. False otherwise.
     */
    public boolean read_csv_checkedproxylists_org( File file) {
	
	String delimiter = ";";

	try {
	    BufferedReader reader = new BufferedReader( new FileReader( file));

	    String nextLine = reader.readLine();  // Get the first line.
	    int lineNumber = 0;

	    while( nextLine != null){
		
		if( lineNumber > 0) {  // The first line just holds a description
		    
		    String [] fields = nextLine.split( delimiter);

		    // The IP address is the first field.
		    // The port address is the second field.		   

		    // Parse the port.
		    int parsedPort = Integer.parseInt( fields[1]);

		    // The proxy type is the 3rd field.

		    Proxy.Type type = null;

		    if( "Socks4".equalsIgnoreCase( fields[2]) || "Socks5".equalsIgnoreCase( fields[2])) {
			
			type = Proxy.Type.SOCKS;
		    }

		    if( "Transparent".equalsIgnoreCase( fields[2])) {
			
			type = Proxy.Type.HTTP;  // Java has no type for transparent proxies?
		    } 

		    if( type != null) {  // Only add the proxy, if all required data are available.

			// Check if a proxy with this IP address is already registered.
			// ToDo: better remove the older (registered) proxy?

			try {
			    InetAddress currentAddress = InetAddress.getByName( fields[0]);

			    // If a proxy with this IP is not already registered
			    if( searchProxy( currentAddress) == null) {

				// add it to the list.
				addProxyServer( new RatedProxy( type, new InetSocketAddress( fields[0], parsedPort)));
			    }

			} catch( UnknownHostException uhe) {  // Cannot resolve this proxy host?

			    // Since these proxy addresses are very volatile, I do not consider this an actual error.
			    LogUtils.getInstance().getLogger().info( "Cannot resolve (and therefore add) proxy with address '" 
								     + fields[0]
								     + "' : " 
								     + uhe);
			}
		    }
		}

		nextLine = reader.readLine();  // Read the next line for the next loop.
		
		++lineNumber;
	    }

	    reader.close();  // Close the CSV file.

	    return true;  // Reading successful!

	} catch( FileNotFoundException fnfe) {

	    LogUtils.getInstance().getLogger().error( "CSV file not found in ProxyRequestHandler: " + fnfe);

	    return false;

	} catch( IOException ioe) {
	    
	    LogUtils.getInstance().getLogger().error( "I/O error in ProxyRequestHandler while reading the CSV file: " +ioe);

	    return false;
	}
    }

    /**
     * Search a proxy with a given IP address in the list of registered
     * proxies. Ignore the port for now, since the exchanges sort (and ban)
     * the users by IP address.
     * 
     * @param address The IP address to look for.
     *
     * @return A proxy with the given IP address or null, if no such proxy is found.
     */
    public RatedProxy searchProxy( InetAddress address) {

	// Just llop over the registered servers and compare the IP address.
	for( RatedProxy currentProxy : _proxyServers) {

	    if( ( (InetSocketAddress)currentProxy.address()).getAddress().equals( address)) {

		return currentProxy;  // Return the proxy with the matching IP address.
	    }
	}

	return null;  // No matching proxy server found.
    }

    /**
     * Test all the registered proxy servers.
     */
    public void testRegisteredProxies() {

	System.out.println( "Testing " + _proxyServers.size() + " proxy servers");

	Collection result = ProxyTester.getInstance().test( _proxyServers);

	System.out.println( "Found " + result.size() + " working after test");
    }
}