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
import de.andreas_rueckert.trade.Currency;
import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.site.TradeSiteUserAccount;


/**
 * Factory to create orders.
 */
public class OrderFactory {

    // Static variables


    // Instance variables


    // Constructors


    // Methods

    /**
     * Create a new deposit order to send funds to a trade site.
     *
     * @param tradeSite The trade site to send funds, too.
     * @param userAccount The user account to use or null for the default user account (if it exists).
     * @param currency The currency to deposit.
     * @param amount The amount to deposit.
     *
     * @return A deposit order with the given data.
     */
    public static final DepositOrderImpl createCryptoCoinDepositOrder( TradeSite tradeSite
								       , TradeSiteUserAccount userAccount
								       , Currency currency
								       , Amount amount) {
	return new DepositOrderImpl( tradeSite, userAccount, currency, amount);
    }

    /**
     * Create an order on a given trading site.
     *
     * @param tradeSite The trade site to use.
     * @param userAccount The user account to use or null for the default user account (if it exists).
     * @param orderType The order type (buy or sell).
     * @param price The price for the order.
     * @param currencyPair The currency pair for the order (currency to buy and currency for the payment).
     * @param amount The amount to buy or sell.
     *
     * @return The implementation of an order.
     */
    public static final SiteOrderImpl createCryptoCoinTradeOrder( TradeSite tradeSite
								  , TradeSiteUserAccount userAccount
								  , OrderType orderType
								  , Price price
								  , CurrencyPair currencyPair
								  , Amount amount) {
	return new SiteOrderImpl( tradeSite, userAccount, orderType, price, currencyPair, amount);
    }

    /**
     * Withdraw a given amount from a given trade site.
     *
     * @param tradeSite The trade site to use.
     * @param userAccount The user account to use or null for the default user account (if it exists).
     * @param currency The currency to withdraw.
     * @param amount The amount to buy or sell.
     * @param target_account The account, where the money goes. Might be null, as long as the order is not executed.
     *
     * @return A withdraw order for the given data.
     */
    public static final WithdrawOrderImpl createCryptoCoinWithdrawOrder( TradeSite tradeSite
									 , TradeSiteUserAccount userAccount
									 , Currency currency
									 , Amount amount
									 , Account target_account) {

	return new WithdrawOrderImpl( tradeSite, userAccount, currency, amount, target_account);
    }
}