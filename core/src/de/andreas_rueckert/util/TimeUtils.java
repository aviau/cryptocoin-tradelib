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
