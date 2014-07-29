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

package de.andreas_rueckert.trade.site;

import de.andreas_rueckert.util.ModuleLoader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Date;
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
     * The time, when this account was created.
     */
    private Date _created;

    /**
     * The id of the account.
     */
    private int _id = -1;

    /**
     * A map to store all the parameters in one data structure.
     */
    private Map< String, String> _parameters = new HashMap< String, String>();

    /**
     * The trade site, this account is for.
     */
    private TradeSite _tradeSite = null;


    // Constructors

    /**
     * Create a new account.
     */
    public TradeSiteUserAccount() {
    }

    /**
     * Create a new account for a given trade site.
     *
     * @param tradeSite The trade site, this account is for.
     */
    public TradeSiteUserAccount( TradeSite tradeSite) {

	// Store the trade site in the instance.
	_tradeSite = tradeSite;
    }


    // Methods

    /**
     * Encode this user account as a property value.
     *
     * @return This user account as a property value.
     */
    public final String encodeAsPropertyValue() {

	StringBuffer resultBuffer = new StringBuffer();  // A buffer for the result.

	// Store the activated status.
	resultBuffer.append( URLEncoder.encode( "activated"));
	resultBuffer.append( "=");
	resultBuffer.append( URLEncoder.encode( isActivated() ? "1" : "0"));

	if( getTradeSite() != null) {  // If a trade site is set, encode it.

	    resultBuffer.append( "&");
	    resultBuffer.append( URLEncoder.encode( "tradesite"));
	    resultBuffer.append( "=");
	    resultBuffer.append( URLEncoder.encode( getTradeSite().getName()));
	}

	// Loop over the user accounts.
	for( Map.Entry<String, String> currentParameter : _parameters.entrySet()) {
	    
	    resultBuffer.append( "&");     // Concatenate the next parameter.
	    resultBuffer.append( URLEncoder.encode( currentParameter.getKey()));
	    resultBuffer.append( "=");
	    resultBuffer.append( URLEncoder.encode( currentParameter.getValue()));
	}

	return resultBuffer.toString();  // Return the converted buffer.
    }

    /**
     * Compare this account with another account.
     *
     * @param account The account to compare.
     *
     * @return true, if the accounts are equal. False otherwise.
     */
    public final boolean equals( TradeSiteUserAccount account) {

	// Check the trade site first.
	if( ! getTradeSite().equals( account.getTradeSite())) {

	    return false;  // The 2 accounts are for different trade sites.
	}

	// Just compare the 2 hash maps now.
	return _parameters.equals( account.getParameters());
    }

    /**
     * Create a new user account from an encoded property value.
     *
     * @param propertyValue The property value encoded as a string.
     *
     * @return The decoded property value as a TradeSiteUserAccount object.
     */
    public final static TradeSiteUserAccount fromPropertyValue( String propertyValue) {

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
	    } else if( "tradesite".equalsIgnoreCase( parameterName)) {
		
		// Try to find the trade site with this name.
		TradeSite foundSite =  ModuleLoader.getInstance().getRegisteredTradeSite( parameterValue);

		if( foundSite != null) {     // If there was a trade site found,
		    result._tradeSite = foundSite;  // store it in this instance.
		}

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
    public final String getAccountName() {

	return _parameters.get( "accountName");
    }
    
    /**
     * Get the API key of this account.
     *
     * @return The API key of this account.
     */
    public final String getAPIkey() {

	return _parameters.get( "APIkey");
    }

    /**
     * Get the date and time, when this account was created.
     *
     * @return The date and time, when this account was created.
     */
    public final Date getCreated() {

	return _created;
    }

    /**
     * Get the email address of this account.
     *
     * @return The email address of this account.
     */
    public final String getEmail() {

	return _parameters.get( "email");
    }
    
    /**
     * Get the id of this account.
     */
    public final int getId() {

	return _id;
    }

    /**
     * Get a parameter with a given name.
     *
     * @param fieldname The name of the parameter.
     *
     * @return The value of the parameter or null, if no such parameter exists.
     */
    public final String getParameter( String fieldname) {

	return _parameters.get( fieldname);
    }

    /**
     * Get the entire map of parameters.
     *
     * @return The entire map of parameters.
     */
    public final Map<String,String> getParameters() {

	return _parameters;
    }

    /**
     * Get the password of this account.
     *
     * @return The password of this account.
     */
    public final String getPassword() {

	return _parameters.get( "password");
    }

    /**
     * Get the secret of this account, or null if no secret was set.
     *
     * @return The secret of this account or null if no secret was set.
     */
    public final String getSecret() {

	return _parameters.get( "secret");
    }

    /**
     * Get the trade site for this account.
     *
     * @return The trade site, this account is for.
     */
    public final TradeSite getTradeSite() {

	return _tradeSite;
    }

    /**
     * Get the user ID.
     *
     * @return The user ID.
     */
    public final String getUserId() {

	return _parameters.get( "userId");
    }

    /**
     * Get the hashcode for this user account.
     *
     * @return The hash code for this user account.
     */
    public synchronized int hashCode() {

	// Just use the hash code of the name, if it is available.
	return getTradeSite() != null ? getTradeSite().getName().hashCode() : 0;
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
     * Set the time, when this account was created.
     *
     * @param created The time, when this account was created.
     */
    public final void setCreated( Date created) {
	
	// Store the time in the instance.
	_created = created;
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
    public final void setEmail( String email) {

	_parameters.put( "email", email);
    }

    /**
     * Set a new id for this account.
     *
     * @param id The new id to set.
     */
    public final void setId( int id) {

	_id = id;
    }

    /**
     * Set a parameter with a given name to a given value.
     *
     * @param fieldname The name of the parameter.
     * @param value The new value of the parameter.
     */
    public final void setParameter( String fieldname, String value) {
	
	_parameters.put( fieldname, value);
    }

    /**
     * Set a new password for this account.
     *
     * @param password The new password to set.
     */
    public final void setPassword( String password) {

	_parameters.put( "password", password);
    }

    /**
     * Set a new secret of this account.
     *
     * @param secret The new secret of this account.
     */
    public final void setSecret( String secret) {

	_parameters.put( "secret", secret);
    }

    /**
     * Set a new user ID.
     *
     * @param userId The new user ID.
     */
    public final void setUserId( String userId) {

	_parameters.put( "userId", userId);
    }

}
