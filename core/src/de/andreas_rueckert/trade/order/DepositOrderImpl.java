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

package de.andreas_rueckert.trade.order;

import de.andreas_rueckert.trade.account.Account;
import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.currency.CurrencyPairImpl;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;


/**
 * Implementation of a deposit order.
 */
public class DepositOrderImpl extends SiteOrderImpl implements DepositOrder {

    // Static variables


    // Instance variables

    /**
     * The account to deposit to.
     */
    private Account _account = null;
    

    // Constructors

    /**
     * Create a new deposit order implementation object.
     *
     * @param tradeSite The trade site to deposit funds to.
     * @param userAccount The tradesite user account to use.
     * @param currency The currency to use.
     * @param amount The amount to deposit.
     */
    public DepositOrderImpl( TradeSite tradeSite, TradeSiteUserAccount userAccount, Currency currency, Amount amount) {

	this( tradeSite, currency, amount);

	_tradeSiteUserAccount = userAccount;  // Store the user account in this instance.
    }

    /**
     * Create a new deposit order implementation object.
     *
     * @param tradeSite The trade site to deposit funds to.
     * @param currency The currency to use.
     * @param amount The amount to deposit.
     */
    public DepositOrderImpl( TradeSite tradeSite, Currency currency, Amount amount) {

	// Since the currency should not change during the withdraw, just create a currency pair with 2
	// identical currencies.
	// Maybe deposit shouldn't extend SiteOrder, so there are not those unused fields...
	super( tradeSite, OrderType.DEPOSIT, Price.ZERO, new CurrencyPairImpl( currency, currency), amount);
    }


    // Methods

    /**
     * Get the account, where the money is deposited, or null if there is no account yet.
     *
     * @return The cryptocoin account, where the money is deposited, or null.
     */
    public Account getAccount() {
	return _account;
    }

    /**
     * Get the deposited currency.
     * This is mainly a convenience method.
     *
     * @return The currency for the deposit.
     */
    public Currency getCurrency() {
	
	// The currency is stored on both sides of the currency pair, so just use one of those
	// currencies.
	return getCurrencyPair().getCurrency();
    }

    /**
     * Set a new account for this deposit order.
     *
     * @param account The new account for this deposit.
     */
    public void setAccount( Account account) {

	// Store the new account in this order instance.
	_account = account;
    }
}
