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

import de.andreas_rueckert.trade.bot.TradeBot;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * This class provides a user inface for the ma bot.
 */
public class MaBotUI implements TradeBotUI {

    // Static variables


    // Instance variables

    /**
     * The bot that uses this user interface.
     */
    TradeBot _bot = null;

    /**
     * A text field to display the current buy price from the depth.
     */
    JTextField _buyPriceField;

    /**
     * A text field to display the current sell price from the depth.
     */
    JTextField _sellPriceField;
    
    /**
     * A text field to display the current SMA.
     */
    JTextField _smaField;

    /**
     * A button to start and stop the bot.
     */
    JButton _startStopButton;

    /**
     * The panel for the user interface.
     */
    JPanel _uiPanel = null;

    
    // Constructors

    /**
     * Create a new user interface instance.
     *
     * @param bot The bot, that uses this user interface.
     */
    public MaBotUI( TradeBot bot) {
	
	// Store a reference to the bot in this instance.
	_bot = bot;
    }


    // Methods

    /**
     * Get the JPanel with the user interface for the bot.
     *
     * @return The JPanel with the user interface for the bot.
     */
    public JPanel getUIPanel() {

        if( _uiPanel == null) {       // if there is no UI panel yet,
            _uiPanel = new JPanel();  // create one.

	    _uiPanel.setLayout( new BorderLayout());

	    // Create a very simple status of the bot.
	    JPanel statusPanel = new JPanel();
	    statusPanel.setLayout( new GridLayout( 3, 2));

	    // Add a text field to display the current SMA.
	    statusPanel.add( new JLabel( "Current SMA:"));
	    statusPanel.add( _smaField = new JTextField( 16));

	    // Add a text field to display the current buy price from the depth.
	    statusPanel.add( new JLabel( "Current buy price:"));
	    statusPanel.add( _buyPriceField = new JTextField( 16));

	    // Add a text field to display the current sell price from the depth.
	    statusPanel.add( new JLabel( "Current sell price:"));
	    statusPanel.add( _sellPriceField = new JTextField( 16));

	    _uiPanel.add( statusPanel, BorderLayout.CENTER);

	    // Create a panel for the button.
	    JPanel buttonPanel = new JPanel();

	    // Add a button to start and stop the bot.
	    buttonPanel.add( _startStopButton = new JButton( "Start"));

	    _uiPanel.add( buttonPanel, BorderLayout.SOUTH);
	}

	return _uiPanel;  // Return the UI panel for the bot.
    }

    /**
     * Update the shown values.
     *
     * @param inputValues The input values for the calculation.
     * @param outputValues The output values of the calculation.
     */
    public void updateValues( BigDecimal [] inputValues, BigDecimal [] outputValues) {
    }
}