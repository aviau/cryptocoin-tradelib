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

import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.trade.account.TradeSiteAccount;
import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.bot.ui.MaBotUI;
import de.andreas_rueckert.trade.chart.ChartProvider;
import de.andreas_rueckert.trade.chart.ChartAnalyzer;
import de.andreas_rueckert.trade.Currency;
import de.andreas_rueckert.trade.CurrencyPair;
import de.andreas_rueckert.trade.CurrencyPairImpl;
import de.andreas_rueckert.trade.Depth;

import de.andreas_rueckert.trade.Trade;
import de.andreas_rueckert.trade.order.CryptoCoinOrderBook;
import de.andreas_rueckert.trade.order.Order;

import de.andreas_rueckert.trade.order.OrderFactory;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.order.OrderStatus;
import de.andreas_rueckert.trade.order.DepthOrder;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.util.LogUtils;
import de.andreas_rueckert.util.ModuleLoader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;
import java.io.IOException;
import java.io.File;

import de.andreas_rueckert.trade.site.btc_e.client.BtcEClient;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;
import de.andreas_rueckert.persistence.PersistentProperty;
import de.andreas_rueckert.persistence.PersistentPropertyList;

/**
 * This is a simple bot to demonstrate the usage of the cryptocoin tradelib.
 */
public class MaBot implements TradeBot {

    // Static variables

    /**
     * The minimal profit in percent for a trade, compared to the SMA.
     */
    private final static BigDecimal MIN_PROFIT = new BigDecimal( "0.2");

    /**
     * The minimal trade volume.
     */
    private final static BigDecimal MIN_TRADE_AMOUNT = new Amount( "0.01");

    /**
     * The interval for the SMA value.
     */
    //private final static long SMA_INTERVAL = 3L * 60L * 60L * 1000000L; // 3 hrs for now

    /**
     * The interval to update the bot activities.
     */
    private final static int UPDATE_INTERVAL = 60;  // 60 seconds for now...
    
    private final static long SMA_CYCLES = 7L; // 124 184 my wife is witch
    private final static long LONG_SMA_CYCLES = 30L;
    private final static long SMA_INTERVAL = SMA_CYCLES * UPDATE_INTERVAL * 1000000L;
    //private final static String EMA_INTERVAL = "420s";
    private final static long LONG_SMA_INTERVAL = LONG_SMA_CYCLES * UPDATE_INTERVAL * 1000000L;
    //private final static String LONG_EMA_INTERVAL = "1800s";

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

    private TradeSiteUserAccount _tradeSiteUserAccount = null;

    private Currency currency;

    private Currency payCurrency;

    private CryptoCoinOrderBook orderBook;

    // Constructors

    /**
     * Create a new bot instance.
     */
    public MaBot() {

	// Set trade site and currency pair to trade.
	//_tradeSite = ModuleLoader.getInstance().getRegisteredTradeSite( "BtcE");
    StringBuilder configLine = new StringBuilder();
    try
    {
        Scanner s = new Scanner(new File("mabot.cfg"));
        if (s.hasNextLine())
        {
            configLine.append(s.nextLine());
        }
    }
    catch (IOException e)
    {
        System.exit(-1);
    }
    _tradeSiteUserAccount = TradeSiteUserAccount.fromPropertyValue(configLine.toString());
    _tradeSite = new BtcEClient();
    PersistentPropertyList settings = new PersistentPropertyList();
    settings.add(new PersistentProperty("Key", null, _tradeSiteUserAccount.getAPIkey(), 0));
    settings.add(new PersistentProperty("Secret", null, _tradeSiteUserAccount.getSecret(), 0));
    _tradeSite.setSettings(settings);
	_tradedCurrencyPair = CurrencyPairImpl.findByString("LTC<=>USD");
    payCurrency = _tradedCurrencyPair.getPaymentCurrency();                
    currency = _tradedCurrencyPair.getCurrency();
    orderBook = (CryptoCoinOrderBook) CryptoCoinOrderBook.getInstance();
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

	Collection<TradeSiteAccount> currentFunds = _tradeSite.getAccounts(_tradeSiteUserAccount);  // fetch the accounts from the trade site.

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
    public void start() 
    {

        final Logger logger = LogUtils.getInstance().getLogger();
        logger.setLevel(Level.INFO);
        logger.info("MABot started");
        
        // Create a ticker thread.
        _updateThread = new Thread() 
        {

            Price sma = null;
            Price longSma = null;
            boolean shortSmaAbove;
            Order order;
            Order lastDeal;
            BigDecimal longSmaToBuy;
            BigDecimal longSmaToSell;
            Depth depth;

            /**
            * The main bot thread.
            */
            @Override public void run() 
            {
                ChartAnalyzer analyzer = null;
                BigDecimal sellFactor = (new BigDecimal("100")).subtract(MIN_PROFIT).divide((new BigDecimal( "100")), MathContext.DECIMAL128);
		        BigDecimal buyFactor = (new BigDecimal("100")).add(MIN_PROFIT).divide((new BigDecimal( "100")), MathContext.DECIMAL128);

                try
                {
                    analyzer = ChartAnalyzer.getInstance(); 
                    sma = analyzer.getSMA(_tradeSite, _tradedCurrencyPair, SMA_INTERVAL);
                    longSma = analyzer.getSMA(_tradeSite, _tradedCurrencyPair, LONG_SMA_INTERVAL);
                }
                catch (Exception e)
                {
                    logger.error(e);
                    System.exit(-1);
                }

                longSmaToBuy = longSma.multiply(buyFactor);
                longSmaToSell = longSma.multiply(sellFactor);
                shortSmaAbove = sma.compareTo(longSmaToBuy) > 0;
                lastDeal = null;

                while( _updateThread == this) 
                {  // While the bot thread is not stopped...
                   
                    long t1 = System.currentTimeMillis();
                    boolean toBuy = false;
                    boolean toSell = false;

                    try
                    {
                        //check if there are pending orders to cancel
                        /*
                        Map<String, Order> allOrders = orderBook.getOrders();
                        for (int orderIndex = 0; orderIndex < allOrders.size(); ++orderIndex) 
                        {
                            String orderKey = (String) (allOrders.keySet().toArray()[orderIndex]);
                            Order currentOrder = allOrders.get(orderKey);
                            if (currentOrder.getStatus() != OrderStatus.FILLED)
                            {
                                if (currentOrder.getOrderType() == OrderType.BUY)
                                {
                                    toBuy = true;
                                }
                                else
                                {
                                    toSell = true;
                                }
                                orderBook.cancelOrder(currentOrder);
                            }
                        }*/

                        sma = analyzer.getSMA(_tradeSite, _tradedCurrencyPair, SMA_INTERVAL);
                        longSma = analyzer.getSMA(_tradeSite, _tradedCurrencyPair, LONG_SMA_INTERVAL);
  	    	            depth = ChartProvider.getInstance().getDepth(_tradeSite, _tradedCurrencyPair);
                        longSmaToBuy = longSma.multiply(buyFactor);
                        longSmaToSell = longSma.multiply(sellFactor);
                        boolean downsideUp = !shortSmaAbove && sma.compareTo(longSmaToBuy) > 0;
                        boolean upsideDown = shortSmaAbove && sma.compareTo(longSmaToSell) < 0;

                        System.out.println("buy (sma > longSmaToBuy) :  " + (sma.compareTo(longSmaToBuy) > 0));
                        System.out.println("sell (sma < longSmaToSell): " + (sma.compareTo(longSmaToSell) < 0));

                        order = null;
                        if (toBuy || downsideUp) 
                        {
                            shortSmaAbove = true;
                            toBuy = false;
 			                order = buyCurrency(depth);
                        }
                        else if (toSell || upsideDown) 
                        {
                            shortSmaAbove = false;
                            toSell = false;
 			                order = sellCurrency(depth); 
                        }
                        /*if (order != null)
                        {
                            OrderStatus status = orderBook.checkOrder(order.get());
                            System.out.println(order);
                            System.out.println(status);
                        }*/
                        reportCycleSummary();
                        
                    }
                    catch (Exception e)
                    {
                        logger.error(e);
                    }
                    finally
                    {
                        sleepUntilNextCycle(t1);
                    } 
		        }
		    }

            private void reportCycleSummary()
            {
                logger.info(String.format("trend     | [ %s ] ", shortSmaAbove ? "+" : "-"));
                if (order != null)
                {
                    logger.info(String.format("current   | %s", order));
                    logger.info(String.format(" \\-status | %s", order.getStatus()));
                    lastDeal = order;
                }
                else
                {
                    logger.info("current   |");
                }
                if (lastDeal != null)
                {
                    logger.info(String.format("last deal | %s", lastDeal));
                    logger.info(String.format(" \\-status | %s", lastDeal.getStatus()));
                }
                else
                {
                    logger.info("last deal |");
                }
                logger.info(String.format("sma%3d    | %12f  = %12f = %12f", SMA_CYCLES, sma, sma, sma));
                logger.info(String.format("sma%3d    | %12f* < %12f < %12f*", LONG_SMA_CYCLES, longSmaToSell, longSma, longSmaToBuy));
                logger.info(String.format("buy       |                 %12f         ^       |", depth.getBuy(0).getPrice()));
                logger.info(String.format("sell      |       ^         %12f                 |", depth.getSell(0).getPrice()));
                logger.info(              "----------+---------------------------------------------+");
            }

            private void sleepUntilNextCycle(long t1)
            {
                long t2 = System.currentTimeMillis();
                long sleepTime = (UPDATE_INTERVAL * 1000 - (t2 - t1)); 
                if (sleepTime > 0)
                {
			        try 
                    {
                        sleep(sleepTime);  // Wait for the next loop.
                    } 
                    catch( InterruptedException ie) 
                    {
                        System.err.println( "Ticker or depth loop sleep interrupted: " + ie.toString());
                    }
                }
            }
	    };

	    _updateThread.start();  // Start the update thread.
    }
    
    private Order buyCurrency(Depth depth)
    {
        // Check, if there is an opportunity to buy something, and the volume of the
		// order is higher than the minimum trading volume.

        DepthOrder depthOrder = depth.getSell(0);
        Amount availableAmount = depthOrder.getAmount();
 		if (availableAmount.compareTo(MIN_TRADE_AMOUNT) >= 0) 
        {
		    // Now check, if we have any funds to buy something.
            Price sellPrice = depthOrder.getPrice();
			Amount buyAmount = new Amount(getFunds(payCurrency).divide(sellPrice, MathContext.DECIMAL128));

			// If the volume is bigger than the min volume, do the actual trade.
			if (buyAmount.compareTo(MIN_TRADE_AMOUNT) >= 0) 
            {

			    // Compute the actual amount to trade.
				Amount orderAmount = availableAmount.compareTo(buyAmount) < 0 ? availableAmount : buyAmount;

				// Create a buy order...
			    String orderId = orderBook.add(OrderFactory.createCryptoCoinTradeOrder(
                        _tradeSite, _tradeSiteUserAccount, OrderType.BUY, sellPrice, _tradedCurrencyPair, orderAmount));
		        return orderBook.getOrder(orderId);
            }
        }        
        return null;
    }

    private Order sellCurrency(Depth depth)
    {
        // Check, if there is an opportunity to sell some funds, and the volume of the order
        // is higher than the minimum trading volume.
        // 
        DepthOrder depthOrder = depth.getBuy(0);
        Amount availableAmount = depthOrder.getAmount();
        if (availableAmount.compareTo(MIN_TRADE_AMOUNT) >= 0) 
        {
		    // Now check, if we have any funds to sell.
			Amount sellAmount = new Amount(getFunds(currency));

			// If the volume is bigger than the min volume, do the actual trade.
            if (sellAmount.compareTo(MIN_TRADE_AMOUNT) >= 0) 
            {

                // Compute the actual amount to trade.
	            Amount orderAmount = availableAmount.compareTo(sellAmount) < 0 ? availableAmount : sellAmount;

	            // Create a sell order...
                Price buyPrice = depthOrder.getPrice();
		        String orderId = orderBook.add(OrderFactory.createCryptoCoinTradeOrder(
                       _tradeSite, _tradeSiteUserAccount, OrderType.SELL, buyPrice, _tradedCurrencyPair, orderAmount));
                return orderBook.getOrder(orderId);
            }
		}
        return null;
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
