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
    AAA, AC, ACC, ADT, ALF, ALP, AMC, ANC, APH, ARG, ASC, AUR, AV
	, BANK, BAT, BC, BCC, BCX, BDG, BEA, BELA, BELI, BEN, BET, BITS, BLC, BLTZ, BLU, BNS, BONES, BOST, BQC, BTB, BTC, BTE, BTG, BTL, BUK
	, CACH, CAKE, CAP, CASH, CAT, CATC, CC, CCX, CENT, CESC, CFC, CGA, CGB, CIN, CINNI, CLR, CMC, CNC, CNH, CNY, COL, COMM, CPR, CPTL, CRC, CRY, CSC, CTM, CTZ, CURE, CX
	, DB, DBL, DEM, DGB, DGC, DIME, DIS, DMC, DMD, DOGE, DOJE, DRK, DRM, DTC, DVC
	, EAC, ECC, EFL, ELC, ELP, ELT, EMC2, EMD, ENRG, ERC, EUR, EXE, EZC
	, FAC, FCN, FFC, FLAP, FLO, FLT, FRC, FRK, FRX, FST, FTC, FUR
	, GAC, GDN, GER, GBP, GDC, GIAR, GIV, GIVE, GLC, GLD, GLX, GME, GOAL, GPUC, GRCE, GRN, GRS, GTC, GUN
	, H2O, H5C, HASH, HBN, HC, HIC, HIRO, HKC, HOC, HPC, HVC, HYC
	, ICN, IFC, I0C, IPC, IQD, ISR, ITC, IXC
	, JBG, JKC, JPC, JUG
	, KARM, KDC, KGC, KMC, KRW, KSC
	, LBW, LC, LEAF, LGD, LIM, LIRE, LK7, LKY, LOT, LTB, LTC, LYC
	, MARU, MAST, MAX, MEC, MEG, MEM, MEOW, MINT, MMC, MMXIV, MNC, MN1, MN2, MON, MONA, MOON, MPL, MRO, MRS, MRY, MST, MUL, MUN, MYR, MZC
	, NAN, NAUT, NBL, NC2, NEC, NET, NIB, NLG, NMC, NOBL, NRB, NRS, NUC, NVC, NXT, NYAN, NYC
	, OC, ORB, OSC
	, PAWN, PC, PCC, PCN, PHS, PIG, PLN, PLNC, PLX, Points, POT, PPC, PRT, PT, PTC, PTS, PWC, PXC, PYC
	, Q2C, QBC, QCN, QRK, QTM
	, RATC, RBBT, RBY, RCH, RDD, REC, RED, RPC, RT2, RTC, RUC, RUP, RUR, RYC
	, SAT, SBC, SC, SFR, SHARE, SHC, SHIBE, SLR, SMC, SPA, SPC, SPH, SPN, SPT, SRC, STD, STR, STY, SUM, SUPER, SXC, SYN, SYNC
	, TAG, TAK, TEA, TEK, TGC, THC, TIPS, TIX, TRC, TSC
	, UNB, UNO, URO, USD, USDe, UTC, UVC
	, VGC, VIO, VLC, VLT, VMC, VRC, VTC
	, WATER, WC, WDC, WEST, WIN
	, XBC, XBT, XC, XDG, XDQ, XJO, XLB, XLC, XNC, XPM, XRP, XSI, XSS, XSV, XVN
	, YAC, YANG, YBC, YDC, YC, YIN
	, ZCC, ZED, ZEIT, ZET, ZS
	, n10_to_5, n42, n365, n500, n888;  // These are the numeric currency symbols or names with special characters.


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

	// If this currency code contains special characters, replace them.
	if( hasSpecialCharacters( currencyString)) {

	    return findByString( replaceSpecialCharacters( currencyString));
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

	return currencyCode.matches("^\\d+.*");  // Just check for simple digits (positive integer).
    }

    /**
     * Check, if a coin is made up with special characters.
     *
     * @return true, if the coin contains special characters. False otherwise.
     */
    public static boolean hasSpecialCharacters( String currencyCode) {
	
	return currencyCode.contains( "-");
    }

    /**
     * Replace special characters.
     *
     * @param currencyCode The currency code as a string.
     *
     * @return The currency code with the replaced special characters.
     */
    public static String replaceSpecialCharacters( String currencyCode) {

	return currencyCode.replace( "-", "_to_");
    }

    /**
     * Print a currency code and remove the leading 'n' from numeric coin symbols.
     */
    public String toString() {

	String name = getName();  // Get the name to process.

	// Check if this currency is a numeric coin type.
	if( name.matches( "n\\d+")) {

	    name = name.substring( 1);   // Just return the numeric value in this case.
	}
	
	// If this name contains special characters, unreplace them.
	if( name.contains( "_to_")) {

	    name = name.replace( "_to_", "-");
	}

	return name;  // Just return the name otherwise.
    }
}
