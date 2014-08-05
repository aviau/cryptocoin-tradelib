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

package de.andreas_rueckert.trade.site.request.ui;

import java.io.File;
import javax.swing.filechooser.FileFilter;


/**
 * File filter for proxy server lists.
 */
public class ProxyServerListFileFilter extends FileFilter {

    // Static variables

    /**
     * The only instance of this filter.
     */
    private static ProxyServerListFileFilter _instance = null;


    // Instance variables


    // Constructors

    /**
     * Create a new file filter for proxy server lists.
     */
    private ProxyServerListFileFilter() {
        super();
    }


    // Methods

    /**
     * Accept all directories and files with the proxy server list suffix.
     *
     * @param file The file to check for acceptance.
     *
     * @return true, if the file was accepted, or false otherwise.
     */
    public boolean accept(File file) {

        if( file.isDirectory()) { return true; }

        String extension = getFileExtension(file);

        return ( extension != null) && ( extension.equals( "proxylist") 
                                         || extension.equals( "csv"));
    }

    /**
     * The description of this filter filter.
     *
     * @return The description of this file filter.
     */
    public String getDescription() {
        return "A file filter for proxy server list files.";
    }

    /**
     * Get the extension of a given file.
     *
     * @param file The given file.
     *
     * @return The extension of the file.
     */
    private String getFileExtension( File file) {

        String filename = file.getName();
        int lastSeparatorIndex = filename.lastIndexOf( '.');

        return ( lastSeparatorIndex > 0) && ( lastSeparatorIndex < filename.length() - 1) ? filename.substring( ++lastSeparatorIndex).toLowerCase() : null;
    }

    /**
     * Get the only instance of this file filter.
     *
     * @return The only instance of this file filter.
     */
    public static ProxyServerListFileFilter getInstance() {

        if( _instance == null) {
            _instance = new ProxyServerListFileFilter();
        }
        return _instance;
    }
}

