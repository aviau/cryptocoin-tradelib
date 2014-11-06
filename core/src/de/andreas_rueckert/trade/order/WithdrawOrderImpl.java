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
 * This class implements a withdraw order.
 */
public class WithdrawOrderImpl extends SiteOrderImpl implements WithdrawOrder {

    // Static variables

    
    // Instance variables

    /**
     * The account to withdraw to.
     */
    private Account _account;


    // Constructors

    /**
     * Create a new withdraw order. It might be better not to base this order on the OrderImpl
     * class, since we don't need a price or a currency pair.
     *
     * @param tradeSite The site to trade on.
     * @param userAccount The tradesite user account to use.
     * @param currency The currency to use for the withdrawal.
     * @param amount The amount to withdraw.
     * @param account The account to withdraw to.
     */
    public WithdrawOrderImpl( TradeSite tradeSite, TradeSiteUserAccount userAccount, Currency currency, Amount amount, Account account) {
	
	this( tradeSite, currency, amount, account);

	_tradeSiteUserAccount = userAccount;  // Store the user account in this instance.
    }

    /**
     * Create a new withdraw order. It might be better not to base this order on the OrderImpl
     * class, since we don't need a price or a currency pair.
     *
     * @param tradeSite tradeSite The site to trade on.
     * @param currency The currency to use for the withdrawal.
     * @param amount The amount to withdraw.
     * @param account The account to withdraw to.
     
     */
    public WithdrawOrderImpl( TradeSite tradeSite, Currency currency, Amount amount, Account account) {

	// Since the currency should not change during the withdraw, just create a currency pair with 2
	// identical currencies.
	// Maybe deposit shouldn't extend SiteOrder, so there are not those unused fields...
	super( tradeSite, OrderType.WITHDRAW, Price.ZERO, new CurrencyPairImpl( currency, currency), amount);

	setAccount( account);
    }

    

    // Methods
    
    /**
     * Get the account to withdraw to.
     *
     * @return The account to withdraw to.
     */
    public Account getAccount() {
	return _account;
    }

    /**
     * Get the withdrawn currency.
     * This is kind of a convenience method to avoid fetching the currency pair each time.
     *
     * @return The currency for the withdrawal.
     */
    public Currency getCurrency() {
	return getCurrencyPair().getCurrency();
    }

    /**
     * Set the account to withdraw to.
     *
     * @param account The account to withdraw to.
     */
    public void setAccount( Account account) {
	_account = account;
    }
}
