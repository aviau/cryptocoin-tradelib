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

package de.andreas_rueckert.trade.currency;

import de.andreas_rueckert.NotYetImplementedException;


/**
 * This class maps and converts various kinds of currency symbols.
 */
public class CurrencySymbolMapper {

    // Inner classes


    // Static variables


    // Instance variables


    // Contructors


    // Methods

    /**
     * Get a currency object for a ISO 4217 currency name.
     *
     * @see <a href="https://tools.ietf.org/html/draft-stanish-x-iso4217-a3-01">Extended ISO codes draft</a>
     *
     * @param iso4217name The ISO 4217 name of the currency.
     *
     * @return A currency object for this name.
     *
     * @throws CurrencyNotSupportedException if this currency is not supported currently.
     */
    public static final Currency getCurrencyForIso4217Name( String iso4217name) {

	String currencyName = iso4217name;  // Just use the full name as the default value.

	if( iso4217name.startsWith( "Z")) {  // This is a FIAT currency it seems.

	    currencyName = iso4217name.substring( 1);

	} else if( iso4217name.startsWith( "X")) {  // This is unofficial symbol, so most likely a crypto currency.

	    currencyName = iso4217name.substring( 1);

	    // Convert the XBT notation to BTC for now to keep the notation consistent for all exchanges.
	    if( currencyName.equals( "XBT")) {

		currencyName = "BTC";
	    }

	    // Same for Dogecoin
	    if( currencyName.equals( "XDG")) {

		currencyName = "DOGE";
	    }
	}

	Currency currency = CurrencyProvider.getInstance().getCurrencyForCode( currencyName);

	if( currency == null) {

	    throw new CurrencyNotSupportedException( iso4217name + " doesn't seem to be a known currency name");
	    
	} 

	return currency;
    }

    /**
     * Get the ISO 4217 name of a currency.
     *
     * @param currency The currency to convert.
     *
     * @return The ISO 417 name of the currency.
     *
     * @see <a href="https://tools.ietf.org/html/draft-stanish-x-iso4217-a3-01">Extended ISO codes draft</a>
     */
    public String getISO4217Name( Currency currency) {

	throw new NotYetImplementedException( "Converting a currency to ISO 4217 is not yet implemented");
    }
}
