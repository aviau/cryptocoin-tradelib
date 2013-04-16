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

import de.andreas_rueckert.trade.site.TradeSite;
import java.util.Date;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Instances of this class represent a completed trade.
 */
public class TradeImpl implements Comparable<Trade>, Trade {


    // Static variables



    // Instance variables

    /**
     * The time, when the trade happened as a GMT-relative microsecond timestamp.
     */
    protected long _timestamp;

    /**
     * The price of a traded object.
     */
    protected Price _price;

    /**
     * The amount of traded items.
     */
    protected Amount _amount;

    /**
     * The trading id on the trading site.
     */
    protected String _id;

    /**
     * The type of the trade (1 is Sell, 2 is Buy is json format).
     */
    protected TradeType _type = null;

    /**
     * The site, where this trade took place.
     */
    protected TradeSite _site;


    // Constructors

    /**
     * Create a new trade object from a JSONObject.
     *
     * @param site The site, where the trade takes place.
     * @param currencyPair The queried currency pair.
     */
    public TradeImpl() {
    }

    
    // Methods

    /**
     * Compare 2 trades, so they can be sorted via their timestamps.
     *
     */
    public int compareTo( Trade trade2) {
	return getTimestamp() == trade2.getTimestamp() ? 0 : ( getTimestamp() > trade2.getTimestamp() ? 1 : -1);
    }

    /**
     * Quicker check, if 2 trades are equal.
     *
     * @param t The trade to compare.
     *
     * @return true, if the trades are equal.
     */
    public boolean equals( Object t) {
	Trade tr = (Trade)t;

	// Just compare the 2 id's.
	return ( tr.getId().equals( getId()));
    }

    /**
     * Get the amount of traded bitcoins as nano-bitcoins.
     *
     * @return The amount of traded good.
     */
    public Amount getAmount() {
	return _amount;
    }

    /**
     * Get the id of this trade.
     *
     * @return The id of this trade.
     */
    public String getId() {
	return _id;
    }

    /**
     * Get the price of this trade.
     *
     * @return The item price of this trade.
     */
    public Price getPrice() {
	return _price;
    }

    /**
     * Get the timestamp of this trade as microseconds.
     *
     * @return The date of this trade as a microseconds timestamp, that is hopefully GMT-relative. :-)
     */
    public long getTimestamp() {
	return _timestamp;
    }


    /**
     * Get the type (buy or sell) of this trade.
     *
     * @return the type of this trade.
     */
    public TradeType getType() {
	return _type;
    }

    /**
     * Convert this trade to XML.
     *
     * @param document The currently processed document.
     *
     * @return This trade as an XML element.
     */
    public Element toXML( Document document) {

	Element trade = document.createElement( "Trade");  // Create the root element of this trade.

	// Create an element for the id.
	Element idElement = document.createElement( "id");
	idElement.setNodeValue( "" + getId());
	trade.appendChild( idElement);

	// An element for the date.
	Element dateElement = document.createElement( "timestamp");
	dateElement.setNodeValue( "" + getTimestamp());
	trade.appendChild( dateElement);

	// An element for the price.
	Element priceElement = document.createElement( "price");
	priceElement.setNodeValue( "" + getPrice());
	trade.appendChild( priceElement);

	// An element for the amount.
	Element amountElement = document.createElement( "amount");
	amountElement.setNodeValue( "" + getAmount());
	trade.appendChild( amountElement);

	// An element for the type.
	Element typeElement = document.createElement( "type");
	typeElement.setNodeValue( "" + getType());
	trade.appendChild( typeElement);

	return trade;  // Return the created trade XML element.
    }
}