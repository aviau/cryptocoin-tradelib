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

package de.andreas_rueckert.trade;


/**
 * This is a minimal currency implementation, that should work
 * across most trading sites.
 */
public enum CurrencyImpl implements Currency {

    /**
     * The values for the minimal currency implementation.
     */
    ALP, AMC, ANC, BET, BQC, BTC, CGB, CIN, CNC, CNH, CNY, COL, CRC, DEM, DGC, DOGE, DTC, DVC, ELC, ELP, EMD, EUR
	, FRC, FRK, FTC, GBP, GDC, GLC, IFC, I0C, IXC, KGC, LTC, MEC, NAN, NET, NMC, NRB, NVC, ORB, PLN, PPC
	, PTS, QRK, REC, RED, RUC, RUR, SBC, SPT, TAG, TRC, UNO, USD, WDC, XNC, XPM, ZET;


    // Methods

    /**
     * Check, if 2 currencies are the same.
     *
     * @param currency The second currency to check for equality.
     *
     * @return true, if the 2 currencies are equal. False otherwise.
     */
    public boolean equals( Currency currency) {
	return getName().equals( currency.getName());
    }

    /**
     * Convert a string to a CurrencyImpl object.
     *
     * @param currencyString The string to convert.
     *
     * @return A CurrencyImpl object or null, if no matching constant was found.
     */
    public static CurrencyImpl findByString( String currencyString) {
	return CurrencyImpl.valueOf( currencyString);
    }

    /**
     * Get the name of this currency.
     *
     * @return The name of this currency.
     */
    public String getName() {
	return name();
    }
}
