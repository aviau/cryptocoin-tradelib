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


/**
 * This class handles all the data for a trade site user account.
 * Most exchanges will only use _some_ of the data provided. Other will
 * remain empty for those sites.
 */
public class TradeSiteUserAccount {

    // Static variables


    // Instance variables

    /**
     * The name of this account. Not(!) the nickname of the user at the trade site.
     * This name could be used to identify this account in bots etc.
     */
    private String _accountName = null;

    /**
     * An API key. Often used with a secret to identify bots.
     */
    private String _APIkey = null;

    /**
     * The email address of the users. Some sites use it for login.
     */
    private String _email = null;

    /**
     * A password for a user. Often used for user login.
     */
    private String _password = null;

    /**
     * A secret for an API key. Often used to authenticate bots at an exchange.
     */
    private String _secret = null;

    /**
     * A user ID.
     */
    private String _userId;


    // Constructors


    // Methods

    /**
     * Get the name of this account.
     *
     * @return The name of this account.
     */
    public String getAccountName() {

	return _accountName;
    }
    
    /**
     * Get the API key of this account.
     *
     * @return The API key of this account.
     */
    public String getAPIkey() {

	return _APIkey;
    }

    /**
     * Get the email address of this account.
     *
     * @return The email address of this account.
     */
    public String getEmail() {

	return _email;
    }

    /**
     * Get the password of this account.
     *
     * @return The password of this account.
     */
    public String getPassword() {

	return _password;
    }

    /**
     * Get the secret of this account, or null if no secret was set.
     *
     * @return The secret of this account or null if no secret was set.
     */
    public String getSecret() {

	return _secret;
    }

    /**
     * Get the user ID.
     *
     * @return The user ID.
     */
    public String getUserId() {

	return _userId;
    }

    /**
     * Set a new name for this account.
     *
     * @param accountName The new name of this account.
     */
    public void setAccountName( String accountName) {

	_accountName = accountName;
    }

    /**
     * Set a new API key for this account.
     *
     * @param APIkey The new API key for this account.
     */
    public void setAPIkey( String APIkey) {

	_APIkey = APIkey;
    }

    /**
     * Set a new email address for this account.
     *
     * @param email The new email address for this account.
     */
    public void setEmail( String email) {

	_email = email;
    }

    /**
     * Set a new password for this account.
     *
     * @param password The new password to set.
     */
    public void setPassword( String password) {

	_password = password;
    }

    /**
     * Set a new secret of this account.
     *
     * @param secret The new secret of this account.
     */
    public void setSecret( String secret) {

	_secret = secret;
    }

    /**
     * Set a new user ID.
     *
     * @param userId The new user ID.
     */
    public void setUserId( String userId) {

	_userId = userId;
    }

}
