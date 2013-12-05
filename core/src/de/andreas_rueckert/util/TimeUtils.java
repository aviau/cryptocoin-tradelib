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

package de.andreas_rueckert.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * Some time utility methods.
 */
public class TimeUtils {

    // Static variables

    /**
     * The only instance of this class (singleton pattern). 
     */
    private static TimeUtils _instance = null;


    // Instance variables

    /**
     * The offset to the GMT-relative epoch.
     */
    private long _timestampOffset = 0;


    // Constructors

    /**
     * Private constructor for the time utils (singleton pattern).
     */
    private TimeUtils() {

	// Compute an offset to the GMT epoch, so we can compute the GMT epoch faster later.
	_timestampOffset = Calendar.getInstance( TimeZone.getTimeZone("GMT")).getTimeInMillis() - System.currentTimeMillis();
    }


    // Methods

    /**
     * Get the current GMT relative epoch in approximated microseconds.
     *
     * @return The GMT-relative epoch in microseconds.
     */
    public long getCurrentGMTTimeMicros() {
	return ( System.currentTimeMillis() + _timestampOffset) * 1000L;
    }

    /**
     * Get a past time from a string. The string describes the difference to the current time.
     * So "1d" means 1 day ago etc.
     *
     * @param interval The time interval.
     *
     * @return The point of time in the past with the given interval to the current GMT time.
     *
     * @throws TimeFormatException If the time format is not recognized.
     */
    public static final long getPastGMTTimeFromString( String interval) {

	return getInstance().getCurrentGMTTimeMicros() - microsFromString( interval);
    }

    /**
     * Get the only instance of this class.
     *
     * @return The only instance of this class.
     */
    public static TimeUtils getInstance() {
	if( _instance == null) {          // If there is no class instance yet,
	    _instance = new TimeUtils();  // create one.
	}
	return _instance;                 // Return the only instance of this class.
    }

    /**
     * Convert a time as a String object to microseconds.
     *
     * @param timeToConvert The time to be converted as a String object, ie '2ms', '5s', '1m', '3h' '2d'.
     *
     * @return The converted time as microseconds.
     *
     * @throws TimeFormatException If the time format is not recognized.
     */
    public static final long microsFromString( String timeToConvert) throws TimeFormatException {

	try {

	    if( timeToConvert.endsWith( "ms")) {  // Are these milliseconds?

		return Long.parseLong( timeToConvert.substring( 0, timeToConvert.length() - 2)) * 1000L;

	    } else if( timeToConvert.endsWith( "s")) {  // Are these seconds?

		return Long.parseLong( timeToConvert.substring( 0, timeToConvert.length() - 1)) * 1000000L;

	    } else if( timeToConvert.endsWith( "m")) {  // Are these minutes?

		return Long.parseLong( timeToConvert.substring( 0, timeToConvert.length() - 1)) * 60L * 1000000L; 

	    } else if( timeToConvert.endsWith( "h")) {  // Are these hours?

		return Long.parseLong( timeToConvert.substring( 0, timeToConvert.length() - 1)) * 60L * 60L * 1000000L;	

	    } else if( timeToConvert.endsWith( "d")) {  // Are these days? 

		return Long.parseLong( timeToConvert.substring( 0, timeToConvert.length() - 1)) * 24L * 60L * 60L * 1000000L;

	    } else {  // Maybe this is just a number? Try to parse it.

		return  Long.parseLong( timeToConvert);
	    }

	} catch( NumberFormatException nfe) {

	    throw new TimeFormatException( "Cannot parse time string: " + nfe.toString());
	}
    }

    /**
     * Format a given microsecond timestamp as a date.
     *
     * @param microsTimestamp The timestamp as microseconds.
     *
     * @return The timestamp as GMT-relative user-readable date.
     */
    public static String microsToString( long microsTimestamp) {

	// Convert to milliseconds.
	long millisTimestamp = microsTimestamp / 1000L;

	// Create a Date object to format everything up to the milliseconds.
	Date formattingDateObject = new Date( millisTimestamp);

	// Create a buffer for the result.
	StringBuffer resultBuffer = new StringBuffer();

	// Append the date, formatted from the Date object.
	resultBuffer.append( formattingDateObject.toString());

	// Append the remaining microseconds.
	resultBuffer.append( " " + ( microsTimestamp % 1000L));

	// Convert the buffer to a String object and return it.
	return resultBuffer.toString();
    }
}
