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
    AC, ADT, ALF, ALP, AMC, ANC, ARG, ASC, AUR
	, BAT, BC, BCX, BEN, BET, BQC, BTB, BTC, BTE, BTG, BUK
	, CACH, CAP, CASH, CAT, CENT, CGB, CIN, CINNI, CLR, CMC, CNC, CNH, CNY, COL, COMM, CPR, CRC, CSC, CTM
	, DBL, DEM, DGB, DGC, DMC, DMD, DOGE, DRK, DTC, DVC
	, EAC, ELC, ELP, EMC2, EMD, EUR, EXE, EZC
	, FFC, FLAP, FLO, FLT, FRC, FRK, FST, FTC
	, GBP, GDC, GLC, GLD, GLX, GME
	, HBN, HVC, HYC
	, IFC, I0C, IXC
	, JKC
	, KARM, KDC, KGC
	, LBW, LEAF, LK7, LKY, LOT, LTC, LYC
	, MAX, MEC, MEM, MEOW, MINT, MNC, MN1, MN2, MOON, MRY, MST, MYR, MZC
	, NAN, NBL, NEC, NET, NIB, NMC, NRB, NUC, NVC, NXT, NYAN
	, ORB, OSC
	, PHS, PLN, Points, POT, PPC, PTS, PWC, PXC, PYC
	, QRK
	, RBBT, RCH, RDD, REC, RED, RPC, RUC, RUR, RYC
	, SAT, SBC, SMC, SPA, SPT, SRC, STR, SXC
	, TAG, TAK, TEK, TGC, TIPS, TIX, TRC
	, UNO, USD, UTC 
	, VLC, VTC
	, WC, WDC
	, XJO, XNC, XPM
	, YAC, YBC
	, ZCC, ZED, ZEIT, ZET
	, n42;  // These are the numeric currency symbols.


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

	// Since java doesn't allow numeric enum constants, I'll add a leading 'n' to the string.
	if( isNumeric( currencyString)) {

	    return findByString( "n" + currencyString);
	}

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

    /**
     * Check, if this currency symbol is a number (42 coin).
     *
     * @param currencyCode The code of the currency.
     *
     * @return true, if this currency symbol is a number.
     */
    public static boolean isNumeric( String currencyCode) {

	return currencyCode.matches("\\d+");  // Just check for simple digits (positive integer).
    }

    /**
     * Print a currency code and remove the leading 'n' from numeric coin symbols.
     */
    public String toString() {

	// Check if this currency is a numeric coin type.
	if( getName().matches( "n\\d+")) {

	    return getName().substring( 1);   // Just return the numeric value in this case.
	}
	
	return getName();  // Just return the name otherwise.
    }
}
