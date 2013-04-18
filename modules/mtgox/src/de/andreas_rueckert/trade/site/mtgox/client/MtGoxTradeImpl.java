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

package de.andreas_rueckert.trade.site.mtgox.client;


import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.CryptoCoinTradeImpl;
import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.TradeType;
import de.andreas_rueckert.util.LogUtils;
import java.text.ParseException;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * This class implements a crypto coin trade on the MtGox trade site.
 */
public class MtGoxTradeImpl extends CryptoCoinTradeImpl {

    // Static variables


    // Instance variables

    /**
     * Flag to indicate, if this is the primary representation of this trade.
     * It could happen, that the same trade appears in multiple currencies. Only
     * use the primary trade info and ignore all the other representation of this trade.
     */
    private boolean _primary;


    // Constructors

    /**
     * Create a new MtGox trade object.
     *
     * @param jsonTrade The trade as a JSONObject.
     * @param tradeSite The trade site, where the trade takes place.
     * @param currencyPair The queried currency pair.
     *
     * @throws ParseException if the data couldn't be parsed.
     */
    public MtGoxTradeImpl( JSONObject jsonTrade, TradeSite tradeSite, CurrencyPair currencyPair) throws ParseException {

	super( tradeSite, currencyPair);

	// Parse the date.
	try {
	    _timestamp = jsonTrade.getLong( "tid");
	    
	    if( ( _timestamp - ( jsonTrade.getLong( "date") * 1000000)) > 1000000) {  // Check, if this is an old timestamp
		_timestamp =  jsonTrade.getLong( "date") * 1000000;  // Use the date then...
	    }
	} catch( JSONException je) {
	    throw new NumberFormatException( "Date is not a proper long variable");
	}

	// Parse the price
	try {
	    _price = new Price( jsonTrade.getString( "price"));
	} catch( JSONException je) {
	    throw new NumberFormatException( "Price is not in proper decimal format");
	}
	
	// Parse the amount
	try {
	    _amount = new Amount( jsonTrade.getString( "amount"));
	} catch( JSONException je) {
	    throw new NumberFormatException( "Amount is not in proper decimal format");
	}

	// Parse the id
	try {
	    _id = jsonTrade.getString( "tid");
	} catch( JSONException je) {
	    LogUtils.getInstance().getLogger().error( "Cannot parse Mt.Gox id");
	}
	
	// Parse the trade type
	try {
	    String typeString = jsonTrade.getString( "trade_type");

	    if( typeString.isEmpty()) {  // Check, if the trade is too old to
		_type = TradeType.Buy;                           // have assigned a trade type already.
	    } else {
		_type = typeString.equalsIgnoreCase( "bid" ) ? TradeType.Buy : TradeType.Sell;
	    }
	} catch( JSONException je) {
	    throw new ParseException( "Trade type not in proper int format", -1);
	}

	// Parse the primary flag of this trade
	try {
	    String primaryString = jsonTrade.getString( "primary");
	    
	    if( "Y".equalsIgnoreCase( primaryString)) { _primary = true; }
	    else if( "N".equalsIgnoreCase( primaryString)) { _primary = false; }
	    else { throw new ParseException( "Primary flag is not in proper format", -1); }
	} catch( JSONException je) {
	    throw new ParseException( "Primary flag not in proper string format", -1);
	}
    }

    
    // Methods

    /**
     * Check, if this trade representation is the main representation of this 
     * trade, or if it's an alternative currency.
     *
     * @return true, if it's the primary representation of this trade.
     */
    public boolean isPrimary() {
	return _primary;
    }
}