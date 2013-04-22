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

package de.andreas_rueckert.trade.bot;

import de.andreas_rueckert.util.LogUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * This class runs bots with an UI or as a service.
 */
public class BotRunner {
    
    // Static variables


    // Instance variables

    /**
     * A list of registered trade bots.
     */
    private Map<String, TradeBot> _registeredTradeBots = new HashMap<String, TradeBot>();

    /**
     * Flag to indicate the server mode (no GUI then).
     */
    private boolean _serverMode = false;


    // Constructors

    /**
     * Create a new BotRunner instance.
     *
     * @param args The commandline arguments.
     */
    private BotRunner( String [] args) {

	// Parse the commandline.
	if( parseCommandLine( args)) {
	}
    }


    // Methods

    /**
     * The main method to create the bot application.
     *
     * @param args The commandline arguments of the bot application.
     */
    public static void main(String [] args) {

	// Create a new instance of this class.
	BotRunner runner = new BotRunner( args);
    }

    /**
     * Parse the commandline arguments.
     *
     * @param args The commandline arguments as an array of String objects.
     *
     * @return true, if the commandline was successfully parsed. False in case of an error.
     */
    private final boolean parseCommandLine( String [] args) {
	
	// Loop over the command line arguments.
	for( int currentArgumentIndex = 0; currentArgumentIndex < args.length; ++currentArgumentIndex) {

	    // Get the current argument.
	    String currentArgument = args[ currentArgumentIndex];

	    if( "-server".equalsIgnoreCase( currentArgument)) {
		
		// Activate the server more.
		_serverMode = true;
		
	    } else {  // This is an unknown argument.

		LogUtils.getInstance().getLogger().error( "BotRunner: unknown commandline argument: " + currentArgument);

		return false;
	    }
	}

	return true;  // Commandline successfully parsed.
    }

    /**
     * Register a new trade bot.
     *
     * @param tradeBot The trade bot to register.
     */
    private void registerTradeBot( TradeBot tradeBot) {
        _registeredTradeBots.put( tradeBot.getName(), tradeBot);
    }
}