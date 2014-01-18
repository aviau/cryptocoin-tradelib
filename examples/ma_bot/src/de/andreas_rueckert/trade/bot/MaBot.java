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

import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.bot.ui.MaBotUI;
import de.andreas_rueckert.trade.chart.ChartProvider;
import de.andreas_rueckert.trade.Currency;
import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.CurrencyPairImpl;
import de.andreas_rueckert.trade.Depth;
import de.andreas_rueckert.trade.order.CryptoCoinOrderBook;
import de.andreas_rueckert.trade.order.OrderFactory;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.util.LogUtils;
import de.andreas_rueckert.util.ModuleLoader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;


/**
 * This is a simple bot to demonstrate the usage of the cryptocoin tradelib.
 */
public class MaBot implements TradeBot {

    // Static variables

    /**
     * The minimal profit in percent for a trade, compared to the SMA.
     */
    private final static BigDecimal MIN_PROFIT = new BigDecimal( "5");

    /**
     * The minimal trade volume.
     */
    private final static BigDecimal MIN_TRADE_AMOUNT = new Amount( "1");

    /**
     * The interval for the SMA value.
     */
    private final static long SMA_INTERVAL = 3L * 60L * 60L * 1000000L; // 3 hrs for now...

    /**
     * The interval to update the bot activities.
     */
    private final static int UPDATE_INTERVAL = 30;  // 30 seconds for now...


    // Instance variables

    /**
     * The user inface of this bot.
     */
    MaBotUI _botUI = null;

    /**
     * The traded currency pair.
     */
    CurrencyPair _tradedCurrencyPair = null;

    /**
     * The used trade site.
     */
    private TradeSite _tradeSite = null;

    /**
     * The ticker loop.
     */
    private Thread _updateThread = null;


    // Constructors

    /**
     * Create a new bot instance.
     */
    public MaBot() {

	// Set trade site and currency pair to trade.
	_tradeSite = ModuleLoader.getInstance().getRegisteredTradeSite( "BtcE");
	_tradedCurrencyPair = CurrencyPairImpl.findByString( "LTC<=>BTC");
    }
    

    // Methods

   /**
     * Get the funds for a given currency.
     *
     * @param currency The currency to use.
     *
     * @return The balance for this currency (or -1, if no account with this currency was found).
     */
    public BigDecimal getFunds( Currency currency) {

	Collection<TradeSiteAccount> currentFunds = _tradeSite.getAccounts();  // fetch the accounts from the trade site.

        if( currentFunds == null) {
            LogUtils.getInstance().getLogger().error( "MaBot cannot fetch accounts from trade site.");
        } else {
            for( TradeSiteAccount account : currentFunds) {    // Loop over the accounts.
                if( currency.equals( account.getCurrency())) {  // If this accounts has the requested currency.
                    return account.getBalance();                // Return it's balance.
                }
            }
        }

        return null;  // Cannot get any balance. 
    }

    /**
     * Get the name of this bot.
     *
     * @return The name of this bot.
     */
    public String getName() {
        return "MovingAverage";
    }

    /**
     * Get a property value from this bot.
     *
     * @param propertyName The name of the property.
     *
     * @return The value of this property as a String object, or null if it's an unknown property.
     */
    public String getTradeBotProperty( String propertyName) {

        return null;  // Did not find a property with this name.
    }
    
    /**
     * Get the UI for this bot.
     *
     * @return The UI for this bot.
     */
    public MaBotUI getUI() {
        if( _botUI == null) {                    // If there is no UI yet,
            _botUI = new MaBotUI( this);         // create one. This is optional, since the bot
        }                                       // might run in daemon mode.
        return _botUI;
    }

    /**
     * Get the version string of this bot.
     *
     * @return The version string of this bot.
     */
    public String getVersionString() {

        // Get the version of this bot as a string.
        return "0.1.0 ( Janker )";
    }

    /**
     * Check, if the bot is currently stopped.
     *
     * @return true, if the bot is currently stopped. False otherwise.
     */
    public boolean isStopped() {
        return _updateThread == null;
    }

    /**
     * Set some property value in the bot.
     *
     * @param propertyName The name of then property.
     * @param propertyValue The value of the property.
     */
    public void setTradeBotProperty( String propertyName, String propertyValue) {
    }

    /**
     * Start the bot.
     */
    public void start() {
        
        // Create a ticker thread.
        _updateThread = new Thread() {

     
                /**
                 * The main bot thread.
                 */
                @Override public void run() {

		    // The factors for buy and sell prices.
		    BigDecimal buyFactor = ( new BigDecimal( "100")).subtract( MIN_PROFIT).divide( ( new BigDecimal( "100")), MathContext.DECIMAL128);
		    BigDecimal sellFactor = ( new BigDecimal( "100")).add( MIN_PROFIT).divide( ( new BigDecimal( "100")), MathContext.DECIMAL128);

		    
		    while( _updateThread == this) {  // While the bot thread is not stopped...

			// Get the SMA of the selected interval.
			Price sma = ChartProvider.getInstance().getSMA( _tradeSite, _tradedCurrencyPair, SMA_INTERVAL);

			// Get the current depth.
			Depth depth = ChartProvider.getInstance().getDepth( _tradeSite, _tradedCurrencyPair);

			// Now compare buy and sells the SMA.
			// Actually the trade fee should considered here, too.
			// But to keep things simple, I'll just ignore it for now... :-)

			// Check, if there is an opportunity to buy something, and the volume of the
			// order is higher than the minimum trading volume.
			if( ( depth.getSell( 0).getPrice().multiply( buyFactor).compareTo( sma) < 0)
			    && ( depth.getSell( 0).getAmount().compareTo( MIN_TRADE_AMOUNT) >= 0)) {

			    // Now check, if we have any funds to buy something.
			    Amount buyAmount = new Amount( getFunds( _tradedCurrencyPair.getPaymentCurrency()).divide( depth.getSell( 0).getPrice(), MathContext.DECIMAL128));

			    // If the volume is bigger than the min volume, do the actual trade.
			    if( buyAmount.compareTo( MIN_TRADE_AMOUNT) >= 0) {

				// Compute the actual amount to trade.
				Amount orderAmount = depth.getSell( 0).getAmount().compareTo( buyAmount) < 0 
				    ? depth.getSell( 0).getAmount()
				    : buyAmount;

				// Create a buy order...
				CryptoCoinOrderBook.getInstance().add( OrderFactory.createCryptoCoinTradeOrder( _tradeSite
														, OrderType.BUY
														, depth.getSell( 0).getPrice()
														, _tradedCurrencyPair
														, orderAmount));

			    }
			    
                        }
			    
			// Check, if there is an opportunity to sell some funds, and the volume of the order
			// is higher than the minimum trading volume.
			if( ( depth.getBuy( 0).getPrice().multiply( sellFactor).compareTo( sma) > 0)
			    && ( depth.getBuy( 0).getAmount().compareTo( MIN_TRADE_AMOUNT) >= 0)) {

			    // Now check, if we have any funds to sell.
			    Amount sellAmount = new Amount( getFunds( _tradedCurrencyPair.getCurrency()));

			    // If the volume is bigger than the min volume, do the actual trade.
			    if( sellAmount.compareTo( MIN_TRADE_AMOUNT) >= 0) {

				// Compute the actual amount to trade.
				Amount orderAmount = depth.getBuy( 0).getAmount().compareTo( sellAmount) < 0 
				    ? depth.getBuy( 0).getAmount()
				    : sellAmount;

				// Create a sell order...
				CryptoCoinOrderBook.getInstance().add( OrderFactory.createCryptoCoinTradeOrder( _tradeSite
														, OrderType.SELL
														, depth.getBuy( 0).getPrice()
														, _tradedCurrencyPair
														, orderAmount));
			    }
			}

			try {
                            sleep( UPDATE_INTERVAL * 1000);  // Wait for the next loop.
                        } catch( InterruptedException ie) {
                            System.err.println( "Ticker or depth loop sleep interrupted: " + ie.toString());
                        }
		    }
		}
	    };

	 _updateThread.start();  // Start the update thread.
    }
    
    /**
     * Stop the bot.
     */
    public void stop() {
	
        Thread updateThread = _updateThread;  // So we can join the thread later.
        
        _updateThread = null;  // Signal the thread to stop.
        
        try {
            updateThread.join();  // Wait for the thread to end.

        } catch( InterruptedException ie)  {
            System.err.println( "Ticker stop join interrupted: " + ie.toString());
        }
    }
}