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

package de.andreas_rueckert.trade.chart;

import de.andreas_rueckert.trade.NotEnoughTradesException;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.Trade;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * This class analyzes lists of trades in various ways.
 */
class ChartAnalyzer {

    // Static variables

    /**
     * The only instance of this analyzer (singleton pattern).
     */
    private static ChartAnalyzer _instance = null;


    // Instance variables


    // Constructors

    /**
     * Private constructor (for singleton pattern).
     */
    private ChartAnalyzer() {
    }


    // Methods

    /**
     * Get the only instance of this class (singleton pattern).
     *
     * @return The only instance of this class.
     */
    public static ChartAnalyzer getInstance() {
	if( _instance == null) {
	    _instance = new ChartAnalyzer();
	}
	return _instance;
    }

    /**
     * Compute the maximum of a list of trades.
     *
     * @param trades The list of trades.
     * @param startTime All trades with timestamp >= startTime, or all trades, if timestamp == -1L.
     * @param endTime and timestamp <= endTime  are processed, or all trades, if timestamp == -1L.
     *        Both times are GMT-relative timestamps (microseconds since 1.1.1970).
     * @throws a NotEnoughTradesException if there are not enough trades in the array to perform the computation.
     */
    public Price max( Trade [] trades, long startTime, long endTime) throws NotEnoughTradesException {

	if( ( trades == null) || ( trades.length == 0)) {
	    throw new NotEnoughTradesException( "There are not enough trades to compute the sma");
	}

	Price currentMax = new Price( "-1");  // Since the price should always be >= 0, this is a good way to indicate, that no max was checked yet.

	for( int index = 0; index < trades.length; ++index) {
	    
	    Trade currentTrade = trades[ index];
	    long currentTimestamp = currentTrade.getTimestamp();

	    if( ( startTime == -1L) || ( currentTimestamp >= startTime)) {
		if( ( endTime == -1L) || ( currentTimestamp <= endTime)) {
		    
		   Price currentPrice = currentTrade.getPrice();

		   if( currentPrice.compareTo( currentMax) > 0) {
		       currentMax = currentPrice;
		   }
		} else {
		    break;  // We assume the trades are sorted, so we can abort the addition here.
		}
	    }
	}

	return currentMax;  // Return the computed maximum.
    }

    /**
     * Compute the minimum of a list of trades.
     *
     * @param trades The list of trades.
     * @param startTime All trades with timestamp >= startTime, or all trades, if timestamp = -1L.
     * @param endTime and timestamp <= endTime  are processed, or all trades, if timestamp == -1L.
     *        Both times are GMT-relative timestamps (microseconds since 1.1.1970).
     * @throws a NotEnoughTradesException if there are not enough trades in the array to perform the computation.
     */
    public Price min( Trade [] trades, long startTime, long endTime) throws NotEnoughTradesException {

	if( ( trades == null) || ( trades.length == 0)) {
	    throw new NotEnoughTradesException( "There are not enough trades to compute the sma");
	}

	Price currentMin = new Price( "" + Long.MAX_VALUE);  // Since the price should always be < MAX_VALUE, this is a good way to indicate, that no min was checked yet.

	for( int index = 0; index < trades.length; ++index ) {
	    
	    Trade currentTrade = trades[ index];
	    long currentTimestamp = currentTrade.getTimestamp();

	    if( ( startTime == -1L) || ( currentTimestamp >= startTime)) {
		if( ( endTime == -1L) || ( currentTimestamp <= endTime)) {
		    
		   Price currentPrice = currentTrade.getPrice();

		   if( currentPrice.compareTo( currentMin) < 0) {
		       currentMin = currentPrice;
		   }
		} else {
		    break;  // We assume the trades are sorted, so we can abort the addition here.
		}
	    }
	}

	return currentMin;  // Return the computed maximum.
    }

    /**
     * Compute the sma over a list of trades.
     *
     * @param trades The list of trades.
     * @param startTime All trades with timestamp >= startTime or all trades, if timstamp = -1L.
     * @param endTime and timestamp <= endTime  are processed, or all trades, if timestamp = -1L.
     *        Both times are GMT-relative timestamps (microseconds since 1.1.1970).
     * @throws a NotEnoughTradesException if there are not enough trades in the array to perform the computation.
     */
    public Price sma( Trade [] trades, long startTime, long endTime) throws NotEnoughTradesException {

	long nTrades = 0;
	Price currentSum = new Price( "0");

	if( ( trades == null) || ( trades.length == 0)) {
	    throw new NotEnoughTradesException( "There are not enough trades to compute the sma");
	}

	for( int index = 0; index < trades.length; ++index) {
	    
	    Trade currentTrade = trades[ index];
	    long currentTimestamp = currentTrade.getTimestamp();

	    if( ( startTime == -1L) || ( currentTimestamp >= startTime)) {  // <= the -1L check is an ugly hack. Better do different loops for different parameters?
		if( ( endTime == -1L) || ( currentTimestamp <= endTime)) {
		    currentSum = currentSum.add( currentTrade.getPrice());
		    ++nTrades;
		} else {
		    break;  // We assume the trades are sorted, so we can abort the addition here.
		}
	    }
	}

	return new Price( currentSum.divide( new BigDecimal( nTrades), MathContext.DECIMAL128));  // Return the average of the trade prices.
    }
}