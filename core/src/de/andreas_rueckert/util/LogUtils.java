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

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;


/**
 * Class to encapsulate the logging related stuff.
 */
public class LogUtils {

    // Static variables

    /**
     * The only instance of this class (singleton pattern).
     */
    private static LogUtils _instance = null;

    /**
     * The logger for the tradelib.
     */
    private static Logger _logger = Logger.getLogger( LogUtils.class);


    // Instance variables

    
    // Constructors

    /**
     * Private constructor for singleton pattern.
     */
    private LogUtils() {

        // Configure Log4j 
        BasicConfigurator.configure();
    }


    // Methods

    /**
     * Get the only instance of this class (singleton pattern).
     *
     * @return The only instance of this class.
     */
    public final static LogUtils getInstance() {

	if( _instance == null) {  // If there is no instance yet,
	    
	    _instance = new LogUtils();  // create one.
	}

	return _instance;  // Return the only instance of this class.
    }

    /**
     * Get the logger object.
     *
     * @return The logger object.
     */
    public static Logger getLogger() {

        return _logger;
    }
}