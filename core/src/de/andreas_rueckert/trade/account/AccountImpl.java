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

package de.andreas_rueckert.trade.account;

import de.andreas_rueckert.trade.Currency;
import java.math.BigDecimal;


/**
 * Base class for accounts.
 */
public class AccountImpl implements Account {

    // Static variables

    
    // Instance variables

    /**
     * The balance.
     */
    private BigDecimal _balance = BigDecimal.ZERO;

    /**
     * The currency of this account.
     */
    private Currency _currency = null;

    /**
     * The id of this account.
     */
    private String _id = null;

    /**
     * The name of this account.
     */
    private String _name = null;


    // Constructors

    /**
     * Create a new account object.
     *
     * @param balance The new balance.
     * @param currency The used currency.
     */
    public AccountImpl( BigDecimal balance, Currency currency) {
	_balance = balance;
	_currency = currency;
    }


    // Methods

    /**
     * Add a value to the current balance,
     *
     * @param value The value to add.
     */
    public void addToBalance( BigDecimal value) {
	_balance = _balance.add( value);
    }

    /**
     * Get the current balance of this account.
     *
     * @return The current balance of this account.
     */
    public BigDecimal getBalance() {
	return _balance;
    }

    /**
     * Get the currency of this account.
     *
     * @return The currency of this account.
     */
    public Currency getCurrency() {
	return _currency;
    }

    /**
     * Get the id of this account.
     *
     * @return The id of this account.
     */
    public String getId() {
	return _id;
    }
    
    /**
     * Get the name of this account.
     *
     * @return The name of this account.
     */
    public String getName() {
	return _name;
    }
    
    /**
     * Set a new balance for this account.
     *
     * @param balance The new balance for this account.
     */
    public void setBalance( BigDecimal balance) {
	_balance = balance;
    }
}