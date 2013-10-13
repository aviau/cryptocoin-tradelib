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

package de.andreas_rueckert.trade.site;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


/**
 * This class handles all the data for a trade site user account.
 * Most exchanges will only use _some_ of the data provided. Other will
 * remain empty for those sites.
 */
public class TradeSiteUserAccount {

    // Static variables


    // Instance variables

    /**
     * Flag to indicate, if this user account is activated. A trading app could honor
     * this flag and stop trading, if the account is deactivated.
     */
    private boolean _activated = true;

    /**
     * A map to store all the parameters in one data structure.
     */
    private Map< String, String> _parameters = new HashMap< String, String>();


    // Constructors


    // Methods

    /**
     * Encode this user account as a property value.
     *
     * @return This user account as a property value.
     */
    public String encodeAsPropertyValue() {

	StringBuffer resultBuffer = new StringBuffer();  // A buffer for the result.

	// Store the activated status.
	resultBuffer.append( URLEncoder.encode( "activated"));
	resultBuffer.append( "=");
	resultBuffer.append( URLEncoder.encode( isActivated() ? "1" : "0"));

	// Loop over the user accounts.
	for( Map.Entry<String, String> currentParameter : _parameters.entrySet()) {

	    if( resultBuffer.length() > 0) {   // If the buffer already holds parameters
		resultBuffer.append( "&");     // Concatenate the next parameter (should always happen with the activated flag.
	    }                                  // This is just added in case the activated flag is removed).
	    
	    resultBuffer.append( URLEncoder.encode( currentParameter.getKey()));
	    resultBuffer.append( "=");
	    resultBuffer.append( URLEncoder.encode( currentParameter.getValue()));
	}

	return resultBuffer.toString();  // Return the converted buffer.
    }

    /**
     * Create a new user account from an encoded property value.
     *
     * @param propertyValue The property value encoded as a string.
     *
     * @return The decoded property value as a TradeSiteUserAccount object.
     */
    public static TradeSiteUserAccount fromPropertyValue( String propertyValue) {

	// Create a buffer for the result.
	TradeSiteUserAccount result = new TradeSiteUserAccount();

	// Split the encoded value into key<=>value pairs.
	for( String currentParameter : propertyValue.split( "&")) {

	    // Now split and decode the key<=>value pairs.
	    // ToDo: check, if the array has 2 elements?
	    String [] value = currentParameter.split( "=");

	    String parameterName = URLDecoder.decode( value[0]).trim();
	    String parameterValue = URLDecoder.decode( value[1]).trim();

	    if( "activated".equalsIgnoreCase( parameterName)) {              // If the account was activated,
		result.setActivated( "1".equalsIgnoreCase( parameterValue));  // activate it again...
	    } else {	// Store this parameter in the parameter map.
		result.setParameter( URLDecoder.decode( value[0]), URLDecoder.decode( value[1]));
	    }
	}

	// Return the created account.
	return result;
    }

    /**
     * Get the name of this account.
     *
     * @return The name of this account.
     */
    public String getAccountName() {

	return _parameters.get( "accountName");
    }
    
    /**
     * Get the API key of this account.
     *
     * @return The API key of this account.
     */
    public String getAPIkey() {

	return _parameters.get( "APIkey");
    }

    /**
     * Get the email address of this account.
     *
     * @return The email address of this account.
     */
    public String getEmail() {

	return _parameters.get( "email");
    }

    /**
     * Get the password of this account.
     *
     * @return The password of this account.
     */
    public String getPassword() {

	return _parameters.get( "password");
    }

    /**
     * Get the secret of this account, or null if no secret was set.
     *
     * @return The secret of this account or null if no secret was set.
     */
    public String getSecret() {

	return _parameters.get( "secret");
    }

    /**
     * Get the user ID.
     *
     * @return The user ID.
     */
    public String getUserId() {

	return _parameters.get( "userId");
    }

    /**
     * Check, if this user account is activated at the moment.
     *
     * @return true, if the user account is activated at the moment. False otherwise.
     */
    public final boolean isActivated() {

	return _activated;
    }

    /**
     * Set a new name for this account.
     *
     * @param accountName The new name of this account.
     */
    public void setAccountName( String accountName) {

	_parameters.put( "accountName", accountName);
    }

    /**
     * Activate or deactivate this user account.
     *
     * @param activated true to activate the account, false to deactivate it.
     */
    public final void setActivated( boolean activated) {

	_activated = activated;
    }

    /**
     * Set a new API key for this account.
     *
     * @param APIkey The new API key for this account.
     */
    public void setAPIkey( String APIkey) {

	_parameters.put( "APIkey", APIkey);
    }

    /**
     * Set a new email address for this account.
     *
     * @param email The new email address for this account.
     */
    public void setEmail( String email) {

	_parameters.put( "email", email);
    }

    /**
     * Set a parameter with a given name to a given value.
     *
     * @param fieldname The name of the parameter.
     * @param value The new value of the parameter.
     */
    public void setParameter( String fieldname, String value) {
	
	_parameters.put( fieldname, value);
    }

    /**
     * Set a new password for this account.
     *
     * @param password The new password to set.
     */
    public void setPassword( String password) {

	_parameters.put( "password", password);
    }

    /**
     * Set a new secret of this account.
     *
     * @param secret The new secret of this account.
     */
    public void setSecret( String secret) {

	_parameters.put( "secret", secret);
    }

    /**
     * Set a new user ID.
     *
     * @param userId The new user ID.
     */
    public void setUserId( String userId) {

	_parameters.put( "userId", userId);
    }

}
