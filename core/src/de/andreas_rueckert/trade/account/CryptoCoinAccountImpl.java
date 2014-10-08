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
import java.math.BigDecimal;


/**
 * Implementation of a cryptocoin account.
 */
public class CryptoCoinAccountImpl extends AccountImpl implements CryptoCoinAccount {

    // Static variables


    // Instance variables

    /**
     * The cryptocoin address of this account (i.e. bitcoin address, litecoin address etc).
     */
    private String _cryptoCoinAddress = null;


    // Constructors

    /**
     * Create a new cryptocoin account object.
     *
     * @param cryptoCoinAddress The cryptocoin address to use for this account.
     * @param balance The balance as nano-coins, or so.
     * @param currency The used currency.
     */
    public CryptoCoinAccountImpl( String cryptoCoinAddress, BigDecimal balance, Currency currency) {
	super( balance, currency);

	setCryptoCoinAddress( cryptoCoinAddress);
    }

    
    // Methods

    /**
     * Get the cryptocoin address of this account.
     *
     * @return The cryptocoin address of this account.
     */
    public String getCryptoCoinAddress() {
	return _cryptoCoinAddress;
    }

    /**
     * Set a new address for this account.
     *
     * @param cryptoCoinAddress The new address for this account.
     */
    public void setCryptoCoinAddress( String cryptoCoinAddress) {
	_cryptoCoinAddress = cryptoCoinAddress;
    }
}
