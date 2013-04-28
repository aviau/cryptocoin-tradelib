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

package de.andreas_rueckert.trade.bot.ui;

import de.andreas_rueckert.trade.bot.MaBot;
import de.andreas_rueckert.trade.bot.TradeBot;
import de.andreas_rueckert.trade.bot.ui.action.ActionSettings;
import de.andreas_rueckert.util.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;


/**
 * This class runs bots with an UI or as a service.
 */
public class BotRunner {

    // Inner classes

    
    // Static variables


    // Instance variables

    /**
     * The panel with the bot list.
     */
    JPanel _botListPanel = null;

    /**
     * A list of the bots to start.
     */
    ArrayList<String> _botsToStart = new ArrayList<String>();

    /**
     * The main frame.
     */
    JFrame _mainFrame;

    /**
     * The menu bar for the app.
     */
    JMenuBar _menuBar = null;

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

	// Register trade bots.
	registerTradeBot( new MaBot());

	// Parse the commandline.
	if( parseCommandLine( args)) {

	    // If the app is not in server mode, create a GUI.
	    if( ! _serverMode) {

		createGUI();
	    }
	}
    }


    // Methods

    /**
     * Create a graphical user interface.
     */
    private final void createGUI() {

	_mainFrame = new JFrame( "Trade-Bot Runner"); // Create the main frame.

	_mainFrame.setJMenuBar( getMenuBar());

	_mainFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE);

	// Create UI elements.
	_mainFrame.getContentPane().add( getBotListPanel());
	
	_mainFrame.pack();  // Resize frame.
	
	_mainFrame.setVisible( true);  // Display main frame.
    }

    /**
     * Create a panel to list the bots.
     *
     * @return A panel to list the bots.
     */
    private JPanel getBotListPanel() {

	_botListPanel = new JPanel();

	// Add a list of the bots.
	_botListPanel.add( new JList( getRegisteredTradeBots().keySet().toArray( new String[0])));

	return _botListPanel;  // Return the panel with the bot list.
    }

    /**
     * Get the menu bar for the app.
     *
     * @return The menu bar for the app.
     */
    private final JMenuBar getMenuBar() {
	
	if( _menuBar == null) {  // If there is no menu bar yet..

	    _menuBar = new JMenuBar();  // ..create one.
	    
	    // Create an edit menu.
	    JMenu editMenu = new JMenu( "Edit");
	    editMenu.add( new JMenuItem( ActionSettings.getInstance()));

	    // Add the edit menu to the menu bar.
	    _menuBar.add( editMenu);

	}

	return _menuBar;  // Return the menu bar for the app.
    }

    /**
     * Get the registerd trade bots.
     *
     * @return The registered trade bots as a map.
     */
    Map<String, TradeBot> getRegisteredTradeBots() {
	return _registeredTradeBots;
    }

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
		
		// Activate the server mode.
		_serverMode = true;
		

	    } else if( "-startbot".equalsIgnoreCase( currentArgument)) {  // The user wants to start a bot.
		
                if( ++currentArgumentIndex >= args.length) {

                    LogUtils.getInstance().getLogger().error( "-startbot switch given but no trade bot name to start.");

		    return false;  // Indicate an error.

                } else {

		    // add the bot name to the list of bots to start.
                    _botsToStart.add( args[ currentArgumentIndex]);
                }
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

    /**
     * Set a new panel in the bot runner.
     *
     * @param newPanel The new panel to set.
     */
    public void setContentPanel( JPanel newPanel) {

        while( _mainFrame.getContentPane().getComponentCount() > 1) {  // Remove all content panels, but the bot list.
            _mainFrame.getContentPane().remove( _mainFrame.getContentPane().getComponentCount() - 1); 
        }
        
        if( newPanel != null) {  // If there was actually a new panel given (newPanel might be null otherwise).
            _mainFrame.getContentPane().add( newPanel);  // Add the new panel.
            
            _mainFrame.getContentPane().invalidate();  // Re-layout the UI.
            _mainFrame.getContentPane().validate();
            
            _mainFrame.pack();  // Pack the frame.
        }
    }
}