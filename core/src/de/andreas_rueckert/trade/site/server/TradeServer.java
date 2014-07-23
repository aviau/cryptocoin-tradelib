/**
 * Java implementation of bitcoin trading.
 *
 * @author Andreas Rueckert <a_rueckert@gmx.net>
 *
 * (c) 2014 Andreas Rueckert
 */

package de.andreas_rueckert.trade.site.server;

import de.andreas_rueckert.trade.CurrencyPair;


/**
 * An interface for trade servers.
 */
public interface TradeServer {

    // Variables


    // Methods

    /**
     * Get the name of this trade site.
     *
     * @return The name of this trade site.
     */
    public String getName();

    /**
     * Get the supported currency pairs of this trading site.
     *
     * @return The supported currency pairs of this trading site.
     */
    public CurrencyPair [] getSupportedCurrencyPairs();

    /**
     * Start this trading service.
     */
    public void startService();

    /**
     * Stop this trading service.
     */
    public void stopService();
}