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

package de.andreas_rueckert.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException; 
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * Class to handle HTTP requests.
 */
public class HttpUtils {

    // Static variables

    /**
     * Flag to indicate, if the SSL certs shouldn't be checked,
     * The Intersango cert is causing issues, that's this code is for.
     */
    private static boolean TRUST_ALL_SSL_CERTS = true;


    // Instance variables

    /**
     * A hostname verifier, that accepts all host names.
     */
    private static HostnameVerifier _allHostsValid = null;

    /**
     * A trust manager, that doesn't check SSL certs.
     */
    private static TrustManager [] _trustAllCerts = null;


    // Constructors


    // Methods

    /**
     * Perform a HTTP get request.
     *
     * @param url The url to query.
     *
     * @return The reply as a string, or null if an error occured.
     */
    public synchronized static String httpGet( String url) {

	// Execute HTTP GET request with no further header lines.
	return httpGet( url, null);
    }

    /**
     * Perform a HTTP get request.
     *
     * @param url The url to query.
     * @param headerlines Optional header lines for the request.
     *
     * @return The reply as a string, or null if an error occured.
     */
    public synchronized static String httpGet( String url, Map< String, String> headerlines) {
      URL requestURL;
      HttpURLConnection connection;
      String agent = "Mozilla/4.0";  // Bitstamp seems to require this as an example.
      BufferedReader reader;
      String currentLine;
      StringBuffer result = new StringBuffer();

      // Check, if we should trust all SSL certs and enable the fix if necessary.
      if( TRUST_ALL_SSL_CERTS && ( _trustAllCerts == null)) {
	  installAllCertsTruster();
      }

      try { 
         requestURL = new URL( url);
      } catch( MalformedURLException me) {
	  System.err.println( "URL format error: " + url);

	  return null;
      }

      try {
         connection = (HttpURLConnection)requestURL.openConnection();
      } catch( IOException ioe) {
	  System.err.println( "Cannot open URL: " + url);

	  return null;
      }

      connection.setRequestProperty( "User-Agent", agent );
      
      // Add the additional headerlines, if there were any given.
      if( headerlines != null) {
	  for( Map.Entry<String, String> entry : headerlines.entrySet()) {
	      connection.setRequestProperty( entry.getKey(), entry.getValue());
	  }
      }
	
      try {
	  connection.setRequestMethod("GET");
 
	  reader = new BufferedReader( new InputStreamReader( connection.getInputStream()));

	  while( ( currentLine = reader.readLine()) != null) {
	      result.append( currentLine);
	  }
	  reader.close();

      } catch( ProtocolException pe) {
	  System.err.println( "Wrong protocol for URL: " + pe.toString());

	  result = null;  // return null

      } catch( IOException ioe) {

	  System.err.println( "I/O error while reading from URL: " + url + "\n" + ioe.toString());

	  Scanner scanner = new Scanner( connection.getErrorStream());  // Get a stream for the error message.

	  scanner.useDelimiter("\\Z");

	  String response = scanner.next();  // Get the error message as text.

	  System.out.println( "DEBUG: Server error: " + response);

	  result = null;  // return null

      } finally {

	  if( connection != null) { 
	      connection.disconnect();
	  }
      } 

      return result != null ? result.toString() : null;
   }


    /** 
     * Send a HTTP Post request with some post data and return the response as a string.
     *
     * @param url The post to post the data to.
     * @param headerlines Additional lines for the HTTP Post header or null, if no lines should be added.
     * @param postData The data to send to the server.
     *
     * @return The response as a string or null, of the request failed.
     */
    public synchronized static String httpPost( String url, Map<String, String> headerlines, String postData) {
	URL requestURL;
	HttpURLConnection connection;
	String agent = "Mozilla/4.0";
	String type = "application/x-www-form-urlencoded; charset=UTF-8";
	String encodedData;
	String currentLine;
	StringBuffer result = new StringBuffer();

	// Check, if we should trust all SSL certs and enable the fix if necessary.
	if( TRUST_ALL_SSL_CERTS && ( _trustAllCerts == null)) {
	    installAllCertsTruster();
	}

	try {
	    encodedData = URLEncoder.encode( postData, "UTF-8" );
	} catch( UnsupportedEncodingException uee) {
	    System.err.println( "Cannot encode post data as UTF-8: " + uee.toString());
	    return null;
	}

	try { 
	    requestURL = new URL( url);
	} catch( MalformedURLException me) {
	    System.err.println( "URL format error: " + url);

	    return null;
	}

	try {
	    connection = (HttpURLConnection)requestURL.openConnection();
	} catch( IOException ioe) {
	    System.err.println( "Cannot open URL: " + url);

	    return null;
	}

	try {
	    connection.setRequestMethod( "POST" );
	    connection.setRequestProperty( "User-Agent", agent );
	    connection.setRequestProperty( "Content-Type", type );
	    connection.setRequestProperty( "Content-Length", "" + encodedData.length());

	    // Add the additional headerlines, if there were any given.
	    if( headerlines != null) {
		for( Map.Entry<String, String> entry : headerlines.entrySet()) {
		    connection.setRequestProperty( entry.getKey(), entry.getValue());
		}
	    }
	
	    connection.setUseCaches( false);
	    connection.setDoInput( true);
	    connection.setDoOutput( true);

	    OutputStream os = connection.getOutputStream();
	    os.write( postData.getBytes( "UTF-8") );
	    os.flush();
	    os.close();
	} catch( ProtocolException pe) {
	    System.err.println( "Cannot set protocol to HTTP POST: " + pe.toString());
	    result = null;
	} catch( IOException ioe) {
	    System.err.println( "Cannot write HTTP post data to output stream: " + ioe.toString());
	    result = null;
	} finally {
	    if( connection != null) {
		connection.disconnect();
	    }
	}

	if( result == null) {  // Check for errors so far...
	    return null;     // An error occured...
	}

	try {
	    int rc = connection.getResponseCode();

	    if( rc == 200) {

		//Get Response	
		InputStream is = connection.getInputStream();
		BufferedReader reader = new BufferedReader( new InputStreamReader(is));
		while((currentLine = reader.readLine()) != null) {
		    result.append( currentLine);
		}
		reader.close();
	    } else {
		result = null;  // Posting resulted in an error.
	    }
	} catch( IOException ioe) {
	    System.err.println( "Cannot read HTTP POST response: " + ioe.toString());
	    result = null;
	} finally {
	    if(connection != null) {
		connection.disconnect(); 
	    }
	}

	return result != null ? result.toString() : null;
    }

    /**
     * Don't check SSL certs anymore.
     *
     * @see http://www.rgagnon.com/javadetails/java-fix-certificate-problem-in-HTTPS.html
     */
    private static void installAllCertsTruster() {
	
	_trustAllCerts = new TrustManager [] {
	    new X509TrustManager() {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		    return null;
		}
		
		public void checkClientTrusted(X509Certificate[] certs, String authType) {  }
		
		public void checkServerTrusted(X509Certificate[] certs, String authType) {  }
		
	    }
	};


	try {
	    SSLContext sc = SSLContext.getInstance( "SSL");
	    sc.init ( null, _trustAllCerts, new java.security.SecureRandom());
	    HttpsURLConnection.setDefaultSSLSocketFactory( sc.getSocketFactory());
	} catch( KeyManagementException kme) {
	    System.err.println( "Can't get key in SSL fix installer: " + kme.toString());
	    System.exit( 1);
	} catch( NoSuchAlgorithmException nsae) {
	    System.err.println( "Can't get algorithm in SSL fix installer: " + nsae.toString());
	    System.exit( 1);
	}

	// Create all-trusting host name verifier
	_allHostsValid = new HostnameVerifier() {
		public boolean verify( String hostname, SSLSession session) {
		    return true;
		}
	    };
	
	// Install the all-trusting host verifier
	HttpsURLConnection.setDefaultHostnameVerifier( _allHostsValid);
    }
}
