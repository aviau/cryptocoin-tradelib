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

package de.andreas_rueckert;

import de.andreas_rueckert.util.LogUtils;
import java.io.File;
import java.io.FileFilter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


/**
 * This class loads modules and registers the supported trade sites.
 * For now, I'll make it a singleton, until we need more than one
 * module loader.
 */
public class ModuleLoader {

    // Inner classes

    /**
     * Jar file filter.
     */
    class JarFilenameFilter implements FileFilter {

	// Instance variables


	// Constructors


	// Methods

	/**
	 * Check, if a file seems to be a jar file (the name ends with '.jar').
	 *
	 * @param file The file to check.
	 *
	 * @return true, if the filename ends with .jar
	 */
	public boolean accept(File file) {

	    // Get the name of the file.
	    String filename = file.getName().toLowerCase();

	    // Check, if it ends with '.jar'.
	    return filename.endsWith(".jar");
	}
    }


    // Static variables

    /**
     * The only instance of this class (Singleton pattern).
     */
    private static ModuleLoader _instance = null;


    // Instance variables


    // Constructors

    /**
     * Private constructor for singleton pattern).
     */
    private ModuleLoader() {

	// Load all the module jars and 
	// loop over the files and load all the classes in them.
	for( File currentJar : getModuleJars()) {

	    // Load all the classes in the jar.
	    loadClassesFromJar( currentJar);
	}
    }


    // Methods

    /**
     * Get the only instance of this class (singleton pattern).
     *
     * @return The only instance of this class.
     */
    public static ModuleLoader getInstance() {
	
	if( _instance == null) {            // If there is no instance yet,

	    _instance = new ModuleLoader(); // create one.
	}

	return _instance;   // Return the only instance.
    }

    /**
     * Get the location of the jar, this ModuleLoader is in.
     * So we can compute the ext/ directory relative to this jar file.
     * @see http://stackoverflow.com/questions/320542/how-to-get-the-path-of-a-running-jar-file
     *
     * @return The location of this loaded jar file.
     */
    private File getJarLocation() {

	// Get the location of this class.
	String path = ModuleLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath();

	// Decode the path, in case there are spaces or special characters in it.
	try {
	    String decodedPath = URLDecoder.decode( path, "UTF-8");
	
	    // Convert the path to a file and return it.
	    return new File( decodedPath);

	    
	} catch( UnsupportedEncodingException uee) {  // Should never happen.
	    LogUtils.getInstance().getLogger().error( "Unsupported encoding in ModuleLoader.getJarLocation() : " + uee);
	}

	return null;  // Should never be reached.
    }

    /**
     * Get all the jars from the modules as an array of files.
     *
     * @return All the module jars as an array of files.
     */
    private File [] getModuleJars() {

	// Create a file to access the directory with the jars.
	File extDir = new File( getJarLocation(), "ext");

	// Get all the jars in the ext directory.
	// Maybe I should check, if this is actually a directory?
	return extDir.listFiles( new JarFilenameFilter());
    }

    /**
     * Load all the classes in a given jar file.
     * @see http://stackoverflow.com/questions/11016092/how-to-load-classes-at-runtime-from-a-folder-or-jar
     *
     * @param jarFile The jar file with the classes.
     */
    private void loadClassesFromJar( File jarFile) {
    }
}