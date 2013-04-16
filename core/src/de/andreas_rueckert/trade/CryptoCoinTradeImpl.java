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
import net.sf.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Implementation of a crypto coin trade.
 */
public class CryptoCoinTradeImpl extends TradeImpl implements CryptoCoinTrade {

    // Static variables


    // Instance variables

    /**
     * The currency pair of the trade.
     */
    private CurrencyPair _currencyPair = null;

    /**
     * The site, where this trade took place.
     */
    private TradeSite _site;


    // Constructors

    /**
     * Create a new crypto coin trade object.
     *
     * @param site The site, where the trade takes place.
     * @param currencyPair The queried currency pair.
     */
    public CryptoCoinTradeImpl( TradeSite site, CurrencyPair currencyPair) {
	super();

	setSite( site);  // Store the trade site.

	_currencyPair = currencyPair;  // Store the currency pair in the trade.
    }


    // Methods

    /**
     * Quicker check, if 2 trades are equal. Since we have a trade from a trade site now,
     * and the tid's might be the same on different trade sites, we have to compare the
     * name of the tradesite here too!
     *
     * @param t The trade to compare.
     *
     * @return true, if the trades are equal.
     */
    public boolean equals( Object t) {

	CryptoCoinTrade tr = (CryptoCoinTrade)t;

	// Now compare the trade sites and(!) the id!
	return ( getSite().equals( tr.getSite()) && getId().equals( tr.getId()));
    }


    /**
     * Get the currency of this trade.
     *
     * @return The currency of this trade.
     */
    public CurrencyPair getCurrencyPair() {
	return _currencyPair;
    }

    /**
     * Get the site, where this trade takes place.
     *
     * @return The site, where this trade takes place.
     */
    public TradeSite getSite() {
	return _site;
    }

    /**
     * Store the site, where this trade takes place.
     */
    private void setSite( TradeSite site) {
	_site = site;
    }

    /**
     * Convert this trade to XML.
     *
     * @param document The currently processed document.
     *
     * @return This trade as an XML element.
     */
    public Element toXML( Document document) {
	Element trade = super.toXML( document);

		// An element for the currency.
	Element currencyElement = document.createElement( "currency");
	currencyElement.setNodeValue( "" + getCurrencyPair().getCurrency());
	trade.appendChild( currencyElement);

	// An element for the payment currency
	Element paymentCurrencyElement = document.createElement( "payment_currency");
	paymentCurrencyElement.setNodeValue( "" + getCurrencyPair().getPaymentCurrency());
	trade.appendChild( paymentCurrencyElement);

	return trade;
    }
}