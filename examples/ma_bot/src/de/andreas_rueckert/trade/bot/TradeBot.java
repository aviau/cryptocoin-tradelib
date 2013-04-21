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

package de.andreas_rueckert.trade.bot;

import de.andreas_rueckert.trade.bot.ui.TradeBotUI;


/**
 * This is an interface for various trade bots.
 */
public interface TradeBot {


    // Methods

    /**
     * Get the name of this bot.
     *
     * @return The name of this bot.
     */
    public String getName();

    /**
     * Get some property value from the trade bot.
     *
     * @param propertyName The name of the property.
     *
     * @return The value of this property as a String object.
     */
    public String getTradeBotProperty( String propertyName);

    /**
     * Get a trade bot ui.  
     *
     * @return A trade bot user interface.
     */
    public TradeBotUI getUI();

    /**
     * Get the version string of this bot.
     *
     * @return The version string of this bot.
     */
    public String getVersionString();

    /**
     * Check if the bot is stopped.
     *
     * @return true, if the bot is stopped.
     */
    public boolean isStopped();
    
    /**
     * Set some property value in the bot.
     *
     * @param propertyName The name of then property.
     * @param propertyValue The value of the property.
     */
    public void setTradeBotProperty( String propertyName, String propertyValue);
    
    /**
     * Start the bot.
     */
    public void start();

    /**
     * Stop the bot.
     */
    public void stop();
}