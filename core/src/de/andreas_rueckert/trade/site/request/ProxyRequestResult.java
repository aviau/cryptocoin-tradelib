/**
 * Java implementation for cryptocoin trading.
 *
 * Copyright (c) 2014 the authors:
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

package de.andreas_rueckert.trade.site.request;


/**
 * This class holds the request result, that a proxy request
 * resulted in.
 */
public class ProxyRequestResult {

    // Static variables


    // Instance variables
    
    /**
     * A potential error message, if the request failed.
     */
    private String _errorMessage = null;

    /**
     * The type of the result (failure, success etc).
     */
    private ProxyRequestResultType _type;

    /**
     * The request return value as a string. Null, if the request failed.
     */
    private String _returnValue = null;


    // Constructors

    /**
     * Create a new proxy request result.
     *
     * @param type The type of the result.
     */
    public ProxyRequestResult( ProxyRequestResultType type) {

	_type = type;  // Store the type in the instance.
    }
    
    /**
     * Create a new proxy request result.
     *
     * @param type The type of the result.
     * @param returnValue The return value as a string.
     */
    public ProxyRequestResult( ProxyRequestResultType type, String returnValue) {

	this( type);

	_returnValue = returnValue;  // Store the return value in the instance.
    }
    
    /**
     * Create a new proxy request result.
     *
     * @param type The type of the result.
     * @param returnValue The return value as a string.
     * @param errorMessage An error message with the details.
     */
    public ProxyRequestResult( ProxyRequestResultType type, String returnValue, String errorMessage) {

	this( type, returnValue);

	_errorMessage = errorMessage;  // Store the error message in the instance.
    }
    

    // Methods

    /**
     * Get the type of the request result.
     *
     * @return The type of the request result.
     */
    public final ProxyRequestResultType getType() {

	return _type;
    }
}