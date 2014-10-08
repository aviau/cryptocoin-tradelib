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

package de.andreas_rueckert.trade.site.vircurex.client;

import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.CryptoCoinTradeImpl;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.TradeType;
import de.andreas_rueckert.util.LogUtils;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * This class implements a crypto coin trade on the vircurex trade site.
 */
public class VircurexTradeImpl extends CryptoCoinTradeImpl {

    // Static variables


    // Instance variables


    // Constructors

    /**
     * Create a new vircurex trade object.
     *
     * @param jsonTrade The trade as a JSONObject.
     * @param tradeSite The trade site, where the trade takes place.
     * @param currencyPair The queried currency pair.
     */
    public VircurexTradeImpl(  JSONObject jsonTrade, TradeSite tradeSite, CurrencyPair currencyPair) {

	super( tradeSite, currencyPair);

	// Parse the date.
	try {
	    // btc-e seems to have only the date as seconds.
	    _timestamp = jsonTrade.getLong( "date") * 1000000;
	} catch( JSONException je) {
	    throw new NumberFormatException( "Date is not a proper long variable");
	}

	// Parse the price
	try {
	    _price = new Price( jsonTrade.getString( "price"));
	} catch( JSONException je) {
	    throw new NumberFormatException( "Price is not in proper long format");
	}
	
	// Parse the amount
	try {
	    _amount = new Amount( jsonTrade.getString( "amount"));
	} catch( JSONException je) {
	    throw new NumberFormatException( "Amount is not in proper long format");
	}

	// Parse the id
	try {
	    _id = jsonTrade.getString( "tid");
	} catch( JSONException je) {
	    LogUtils.getInstance().getLogger().error( "Cannot get trade id");
	}
	
	// Vircurex has no trade type in the trades yet (as of 20120919).
	_type = TradeType.Unknown;
    }


    // Methods
}
