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

import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.util.LogUtils;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.Map;


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

    /**
     * A map of interfaces to trade sites. Since exchanges are often searched
     * by their name, this is a hashmap< name, tradesite>.
     */
    private HashMap<String, TradeSite> _registeredTradeSites = new HashMap< String, TradeSite>();


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
     * Get a registered trade site from it's name.
     *
     * @param siteName The name of the trading site.
     *
     * @return The trading site, or null, if no such site was registered.
     */
    public TradeSite getRegisteredTradeSite( String siteName) {
        return _registeredTradeSites.get( siteName);
    }

    /**
     * Get all the registered trade sites.
     *
     * @return The registered trade sites.
     */
    public Map<String,TradeSite> getRegisteredTradeSites() {
        return _registeredTradeSites;
    }

    /**
     * Load all the classes in a given jar file.
     * @see http://stackoverflow.com/questions/11016092/how-to-load-classes-at-runtime-from-a-folder-or-jar
     *
     * @param file The jar file with the classes.
     */
    private void loadClassesFromJar( File file) {
	
	try {

	    // Create a jar file from the given file.
	    JarFile jarFile = new JarFile( file);
	    
	    // Create an URL array for the class loader.
	    URL [] urls = { new URL("jar:file:" + file.getCanonicalPath() +"!/") };
	    
	    // Now we need a class loader.
	    ClassLoader classLoader = URLClassLoader.newInstance( urls);

	    // Loop over the entries of the jar file.
	    for( Enumeration<JarEntry> enumeration = jarFile.entries(); enumeration.hasMoreElements(); ) {
	    
		// Get the next jar entry from the enumeration.
		JarEntry jarEntry = enumeration.nextElement();
		
		if( !jarEntry.isDirectory() && jarEntry.getName().endsWith(".class")) {

		    // -6 because of .class
		    String className = jarEntry.getName().substring( 0, jarEntry.getName().length() - 6);
		    
		    // Convert path to package convention.
		    className = className.replace( '/', '.');
		    
		    // Now load the class.
		    Class loadedClass = classLoader.loadClass( className);
		    
		    // Now check, if this class is an exchange client implementation.
		    if( loadedClass.isAnnotationPresent( TradeSite.class)) {
			
			// Create a client instance from the class.
			// Since the client should have 0 arguments, this might work.
			// But it might be better to loop over the available
			// constructors and call the appropriate one?
			TradeSite tradeSite = (TradeSite)( loadedClass.newInstance());

			// And add it to the list of registered trade sites.
			registerTradeSite( tradeSite);
		    }
		}
	    }
	} catch( IOException ioe) {

	    // Log the raised error, while trying to access the file.
	    LogUtils.getInstance().getLogger().error( "Cannot access jarfile in ModuleLoader: " + ioe);

	} catch( ClassNotFoundException cnfe) {

	    // Log, that the class loader cannot load a class.
	    LogUtils.getInstance().getLogger().error( "ClassLoader in ModuleLoader cannot load a class: " + cnfe);

	} catch( InstantiationException ie) {

	    // The class loader cannot instantiate a loaded class.
	    LogUtils.getInstance().getLogger().error( "ClassLoader in ModuleLoader cannot create exchange client implementation: " + ie);

	} catch( IllegalAccessException iae) {

	    // The class loader cannot a client implementation constructor (might be private?).
	    LogUtils.getInstance().getLogger().error( "ClassLoader in ModuleLoader cannot access implementation constructor: " + iae);

	}
    }

    /**
     * Register a new trade site client.
     *
     * @param site The new trade site client to add to the list of sites.
     */
    private void registerTradeSite( TradeSite site) {

        _registeredTradeSites.put( site.getName(), site);  // Add the trade site to the list of sites.
        
        // _chartProvider.registerTradeSite( site);  // Tell the chart provider about the new trade site.

        // Keep the properties of the trade sites persistent.
        // getAppProperties().registerPersistentPropertyObject( site);

        /* if( ! _daemonMode) {
            addTradeTablePanel( new TradeTable( site));  // Create UI elements for this trade site.
	    } */

	// Log the loading of the exchange client.
        LogUtils.getInstance().getLogger().info( "Registered " + site.getName() + " interface");
    }
}