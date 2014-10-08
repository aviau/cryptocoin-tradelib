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

import de.andreas_rueckert.trade.currency.Currency;
import de.andreas_rueckert.trade.site.TradeSite;
import java.math.BigDecimal;


/**
 * This is the implementation for accounts on trade sites.
 */
public class TradeSiteAccountImpl extends AccountImpl implements TradeSiteAccount {

    // Static variables


    // Instance variables

    /**
     * The trade site, this account is on.
     */
    private TradeSite _tradeSite = null;


    // Constructors


    /**
     * Create a new trade site account object.
     *
     * @param balance The balance as nano-coins, or so.
     * @param currency The used currency.
     * @param tradeSite The trade site, this account is on.
     */
    public TradeSiteAccountImpl( BigDecimal balance, Currency currency, TradeSite tradeSite) {

	super( balance, currency);

	_tradeSite = tradeSite;  // Store the trade site in the object.
    }


    // Methods

    /**
     * Get the trade site, this account is on.
     *
     * @return The trade site, this account is on.
     */
    public TradeSite getTradeSite() {
	return _tradeSite;
    }
}
