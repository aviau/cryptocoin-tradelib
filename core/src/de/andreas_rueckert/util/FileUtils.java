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

package de.andreas_rueckert.util;


/**
 * Class with utility methods for files and filenames.
 */
public class FileUtils {
    
    // Inner classes


    // Static variables

    
    // Instance variables


    // Constructors


    // Methods

    /**
     * Get the suffix of a file including the leading dot.
     *
     * @param filepath The path of the file.
     *
     * @return The suffix of the file or null if it has no suffix.
     *
     * @see http://stackoverflow.com/questions/3571223/how-do-i-get-the-file-extension-of-a-file-in-java
     */
    public static final String getFileSuffix( String filepath) {

	// Get the last dot in the name.
	int lastDot = filepath.lastIndexOf('.');

	// Get the last file separator
	int lastFileSeparator = Math.max( filepath.lastIndexOf( '/'), filepath.lastIndexOf( '\\'));
	
	// If the dot is after the last separator, return the suffix.
	if( lastDot > lastFileSeparator) {
	    
	    return filepath.substring( lastDot);
	}

	return null;  // No suffix found.
    }
}