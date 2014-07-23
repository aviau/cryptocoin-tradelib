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

package de.andreas_rueckert.trade;


/**
 * This is a minimal currency implementation, that should work
 * across most trading sites.
 */
public enum CurrencyImpl implements Currency {

    /**
     * The values for the minimal currency implementation.
     */
    A3C, AAA, AC, ACC, ADT, AERO, AGS, ALF, ALP, AMC, ANC, APEX, APH, AR, ARCH, ARG, ASC, ASCE, ATH, ATP, AUD, AUR, AV, AXIS
	, BANK, BAT, BBL, BBR, BBTC, BC, BCAT, BCC, BCH, BCT, BCX, BDG, BEA, BEL, BELA, BELI, BEN, BET, BITS, BLC, BLTZ, BLU, BLUECHIP, BN, BNS, BONES, BOST, BOUN, BQC, BRIT, BTB, BTC, BTCD, BTCS, BTCX, BTE, BTG, BTL, BUK, BURN, BWC
	, C2, CACH, CAD, CAIX, CAKE, CAP, CASH, CAT, CATC, CC, CCN, CCS, CCX, CENT, CESC, CFC, CFC2, CGA, CGB, CHCC, CHF, CIN, CINNI, CLOAK, CLR, CMC, CNC, CNH, CNL, CNY, COCO, COIN2, COL, COMM, COOL, CPR, CPTL, CRC, CRT, CRY, CRYPT, CSC, CSO, CT, CTM, CTZ, CURE, CX, CYC
	, DB, DBL, DCM, DEM, DGB, DGC, DIG, DIME, DIS, DMC, DMD, DOGE, DOJE, DOLP, DOPE, DRK, DRM, DRS, DTC, DVC, DVK
	, EAC, ECC, EFL, ELC, ELP, ELT, EMC2, EMD, EMO, ENC, ENRG, ERC, EUR, EXE, EZC
	, FAC, FBC , FCN, FFC, FIRE, FLAP, FLASH, FLO, FLT, FOOD, FRAC, FRC, FRK, FRSH, FRX, FSC, FST, FTC, FUEL, FUR
	, GAC, GDN, GER, GBP, GDC, GIAR, GIV, GIVE, GLC, GLD, GLX, GLYPH, GME, GML, GOAL, GPUC, GRCE, GRK, GRN, GRS, GRUMP, GTC, GUE, GUN
	, H2O, H5C, HASH, HBN, HC, HIC, HIRO, HKC, HKD, HMY, HOC, HPC, HPY, HVC, HYC, HYPER
	, ICB, ICG, ICN, IFC, I0C, IPC, IQD, ISIS, ISR, ITC, IVC, IXC
	, JBG, JKC, JPC, JPY, JUDGE, JUG
	, KARM, KDC, KEY, KGC, KIWI, KMC, KORE, KRW, KSC, KTK
	, LAT, LBW, LC, LEAF, LGD, LIM, LIMX, LION, LIRE, LIT, LK7, LKY, LOL, LOT, LTB, LTC, LTCX, LYC
	, MACD, MAIA, MAMM, MARU, MAST, MAX, MEC, MED, MEG, MEM, MEOW, METH, MHYC, MIN, MINT, MMC, MMXIV, MNC, MNR, MN1, MN2, MON, MONA, MOON, MPL, MRC, MRO, MRS, MRY, MST, MUGA, MUL, MUN, MYC, MYR, MZC
	, N5X, NAN, NAUT, NAV, NBL, NC2, NEB, NEC, NET, NIB, NJA, NL, NLG, NMC, NOAH, NOBL, NRB, NRS, NTC, NTR, NUC, NUD, NVC, NXT, NYAN, NYC, NZD
	, OC, OLY, OPC, ORB, OSC, OTC
	, PANDA, PAWN, PC, PCC, PCN, PENG, PER, PES, PHO, PHS, PI, PIG, PIGGY, PLC, PLN, PLNC, PLX, PND, POINTS, POT, PPC, PRIMIO, PRT, PSEUD, PT, PTC, PTS, PURE, PWC, PXC, PYC
	, Q2C, QBC, QCN, QRK, QTL, QTM, QTM2
	, RATC, RBBT, RBY, RC, RCH, RDD, REC, RED, RIC, RIN, RIOT, RODS, ROM, ROX, RPC, RT2, RTC, RUC, RUP, RUR, RYC, RZR
	, SAT, SAT2, SBC, SC, SFR, SGD, SHARE, SHC, SHIBE, SHLD, SING, SIS, SHOP, SKC, SLOTH, SLR, SMC, SNCX, SPA, SPATA, SPC, SPH, SPN, SPT, SRC, START, STD, STR, STY, SUN, SUM, SUM2, SUPER, SVC, SVR, SXC, SYN, SYNC
	, TAC, TAG, TAK, TEA, TECH, TEK, TES, TGC, THC, TIPS, TIX, TOP, TOR, TRC, TRI, TRK, TRUST, TSC, TTY
	, UNB, UNO, UNVC, URO, USD, USDE, UTC, UVC
	, VAST, VC, VEIL, VENT, VGC, VIA, VIO, VLC, VLT, VMC, VOOT, VRC, VTA, VTC, VVV
	, WATER, WC, WDC, WEST, WIFI, WIN, WKC, WVS
	, X13C, X15C, XAN, XBC, XBL, XBT, XC, XDG, XDN, XDQ, XFC, XHC, XJO, XLB, XLC, XMR, XNC, XPM, XRP, XSI, XSS, XST, XSV, XVN, XXL, XXX
	, YAC, YANG, YBC, YDC, YC, YIN, YMC
	, ZCC, ZED, ZEIT, ZET, ZIP, ZS
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
