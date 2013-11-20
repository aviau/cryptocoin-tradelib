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
import de.andreas_rueckert.util.TimeFormatException;
import de.andreas_rueckert.util.TimeUtils;
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
     * Compute the EMA over a timespan before the current time.
     * This timespan is also used for the weight calculation of each price.
     *
     * @see http://www.iexplain.org/ema-how-to-calculate/
     * @see http://stockcharts.com/help/doku.php?id=chart_school:technical_indicators:moving_averages#exponential_moving_a
     *
     * @param trades The list of trades.
     * @param startTime The start time as microseconds.
     * @param endTime The end time as microseconds.
     * @param timePeriod The time period as microseconds (day, hour etc).
     *
     * @return The EMA of the trade prices.
     *
     * @throws NotEnoughTradesException if there are not enough trades in the array to perform the computation.
     * @throws TimeFormatException if the time in the string cannot be parsed.
     */
    public Price ema( Trade [] trades, long startTime, long endTime, long timePeriod) throws NotEnoughTradesException {

	// The interval to check.
	long timeInterval = endTime - startTime;

	// Calculate the number of time periods in the given time interval.
	int nPeriods = (int)( timeInterval / timePeriod);

	// If there are no time periods given, just return null.
	if( nPeriods == 0) {
	    return null;
	}

	// Create an array for the weights (k values) of each time unit.
	// I use an additional array field for the previous weight of the first entry.
	double [] weights = new double[ nPeriods + 1];

	// Now calculate the weight for each time unit, starting from the most recent one.
	weights[ weights.length - 1] = 0.0d;
	for( int currentTimePeriod = weights.length - 2; currentTimePeriod >= 0; --currentTimePeriod) {
	    
	    // This is the translation of the formula: Multiplier: (2 / (Time periods + 1) ) 
	    // but every EMA of the previous period is multiplied with ( 1 - k[previoud time period])
	    // At least, that's the way, I understand it... (A. Rueckert)
	    weights[ currentTimePeriod] = (1.0d - weights[ currentTimePeriod + 1]) * (2.0d / (currentTimePeriod + 2));
	}

	// Create a var to sum up the weighted prices.
	Price totalPrice = new Price( "0");

	// Another var to sum up the weights to scale the price at the end.
	double totalWeight = 0.0d;  

	// Now loop over the trades.
	for( int index = 0; index < trades.length; ++index) {
	    
	    Trade currentTrade = trades[ index];
	    long currentTimestamp = currentTrade.getTimestamp();

	    // Check, if this trade is in the target timespan.
	    if( ( startTime == -1L) || ( currentTimestamp >= startTime)) {
		if( ( endTime == -1L) || ( currentTimestamp <= endTime)) {

		    // Now find the timeunit, this trade is in.
		    
		    // Calculate the distance from the end time.
		    long endDistance = endTime - currentTimestamp;
		    
		    // Compute the time period.
		    int currentTimePeriod = (int)( endDistance / timePeriod);

		    // Get the weight for this price.
		    // weights[0] is the weight for the oldest period! 
		    // weights[ weights.length - 2] is the weight for the most recent period!
		    double weight = weights[ weights.length - 2 - currentTimePeriod];

		    // Now add the weighted price to the total weighted prices.
		    totalPrice = new Price( totalPrice.add( currentTrade.getPrice().multiply( new BigDecimal( weight))));

		    // Add the current weight to the total weight.
		    totalWeight += weight;
		}
	    }
		    
	}

	// Now scale the total of the weighted prices and return this price.
	return new Price( totalPrice.divide( new BigDecimal( totalWeight, MathContext.DECIMAL128)));
	
	// This method is not yet complete.
	// throw new NotYetImplementedException( "EMA is not yet implemented");
    }

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
     * Compute the sma over a timespan before the current time.
     *
     * @param trades The list of trades.
     * @param timespan The timespan as a String, so you can use 12d,5h or 10m as an example.
     *
     * @return The SMA of the trade prices.
     *
     * @throws NotEnoughTradesException if there are not enough trades in the array to perform the computation.
     * @throws TimeFormatException if the time in the string cannot be parsed.
     */
    public Price sma( Trade [] trades, String timespan) throws NotEnoughTradesException, TimeFormatException {

	// Conver the timespan to microseconds.
	long timespanMicros = TimeUtils.microsFromString( timespan);

	// Get the current time as microseconds.
	long currentTimeMicros = TimeUtils.getInstance().getCurrentGMTTimeMicros();

	// Now just call the SMA method to do the actual calculations.
	return sma( trades, currentTimeMicros - timespanMicros, currentTimeMicros);
    }

    /**
     * Compute the sma over a list of trades.
     *
     * @param trades The list of trades.
     * @param startTime All trades with timestamp >= startTime or all trades, if timstamp = -1L.
     * @param endTime and timestamp <= endTime  are processed, or all trades, if timestamp = -1L.
     *        Both times are GMT-relative timestamps (microseconds since 1.1.1970).
     *
     * @return The SMA of the trade prices.
     *
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