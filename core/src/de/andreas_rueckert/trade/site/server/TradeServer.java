/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.site.server;

import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.site.TradeSite;


/**
 * An interface for trade servers.
 */
public interface TradeServer extends TradeSite {

    // Variables


    // Methods

    /**
     * Start this trading service.
     */
    public void startService();

    /**
     * Stop this trading service.
     */
    public void stopService();
}
