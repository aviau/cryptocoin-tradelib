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

package de.andreas_rueckert.trade.site.okcoin.client;


import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.CryptoCoinTradeImpl;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.trade.TradeType;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;


/**
 * This class implements a crypto coin trade on the OKCoin trade site.
 */
public class OKCoinTrade extends CryptoCoinTradeImpl {

    // Inner classes


    // Static variables


    // Instance variables


    // Constructors

    /**
     * Create a new OKCoin trade object.
     *
     * @param jsonTrade The trade as a JSONObject.
     * @param tradeSite The trade site, where the trade takes place.
     * @param currencyPair The queried currency pair.
     *
     * @throws JSONException if the data couldn't be parsed.
     */
    public OKCoinTrade( JSONObject jsonTrade, TradeSite tradeSite, CurrencyPair currencyPair) throws JSONException {

	super( tradeSite, currencyPair);

	// Parse the date.
	_timestamp =  jsonTrade.getLong( "date") * 1000000;
	

	// Parse the price
	_price = new Price( jsonTrade.getString( "price"));

	
	// Parse the amount
	_amount = new Amount( jsonTrade.getString( "amount"));

	// Parse the id
	_id = "" + jsonTrade.getLong( "tid");

	
	// Parse the trade type
	String typeString = jsonTrade.getString( "type");

	if( typeString.isEmpty()) {  // Check, if the trade is too old to
	    _type = TradeType.Buy;                           // have assigned a trade type already.
	} else {
	    _type = typeString.equalsIgnoreCase( "buy" ) ? TradeType.Buy : TradeType.Sell;
	}
    }


    // Methods
}
