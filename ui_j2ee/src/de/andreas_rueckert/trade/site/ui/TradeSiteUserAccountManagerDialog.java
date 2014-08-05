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

package de.andreas_rueckert.trade.site.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;


/**
 * This class displays a dialog to manage the user accounts.
 */
public class TradeSiteUserAccountManagerDialog  extends JDialog implements ActionListener {

    // Inner classes


    // Static variables

    /**
     * The only instance of this class (singleton pattern).
     */
    private static TradeSiteUserAccountManagerDialog _instance = null;


    // Instance variables

    /**
     * The button to add a new account.
     */
    private JButton _addButton;

    /**
     * The button to close the dialog.
     */
    private JButton _closeButton;

    /**
     * Create a map to hold the form field for a new account.
     */
    private Map<String, JComponent> _newAccountMap = new HashMap<String, JComponent>();


    // Constructors

    /**
     * Private constructor for singleton pattern.
     */
    private TradeSiteUserAccountManagerDialog() {

	// Create a window with no parent.
	super( (Frame)null, "User accounts", true);

	// Get the content pane of the dialog.
	Container contentPane = getContentPane(); 

	// Use a GridBagLayout for the many panels.
	contentPane.setLayout( new GridBagLayout());

	// Create the constraints for the layout.
	GridBagConstraints constraints = new GridBagConstraints();

	// Create a panel for the listof user accounts.
	JPanel accountListPanel = new JPanel();

	// Create the table to display the data.
	JTable accountTable = new JTable( new TradeSiteUserAccountTableModel());
	accountTable.setFillsViewportHeight(true);
	accountTable.setGridColor( Color.BLUE);

	// Use a scrollpane to access the whole table.
	accountListPanel.add( new JScrollPane( accountTable)); 

	// Add the table to the center of the dialog with 80% height.
	constraints.gridx = 0;
	constraints.gridwidth = 2;
	constraints.gridy = 0;
	constraints.weighty = 0.8;
	constraints.weightx = 1;
	constraints.fill = GridBagConstraints.BOTH;
	constraints.insets = new Insets( 4, 4, 4, 4);
	contentPane.add( accountListPanel, constraints); 

	// Create a panel for the new account form.
	JPanel newAccountFormPanel = new JPanel();

	// Set a grid layout for the account form.
	newAccountFormPanel.setLayout( new GridLayout( 0, 2, 4, 8));

	// A non-editable text field for the id of the new account.
	newAccountFormPanel.add( new JLabel( "ID"));
	JTextField newAccountId = new JTextField( 4);
	newAccountId.setEditable( false);
	_newAccountMap.put( "id", newAccountId);
	newAccountFormPanel.add( newAccountId);

	// Create a text field for the name.
	newAccountFormPanel.add( new JLabel( "Name"));
	JTextField newAccountName = new JTextField( 16);
	_newAccountMap.put( "name", newAccountName);
	newAccountFormPanel.add( newAccountName);

	// Add a combobox to select the tradesite.
	newAccountFormPanel.add( new JLabel( "Exchange"));
	JComboBox newAccountTradeSite = new JComboBox();
	_newAccountMap.put( "tradeSite", newAccountTradeSite);
	newAccountFormPanel.add( newAccountTradeSite);

	// Add a text field for the mail address.
	newAccountFormPanel.add( new JLabel( "E-Mail"));
	JTextField newAccountEmail = new JTextField( 16);
	_newAccountMap.put( "email", newAccountEmail);
	newAccountFormPanel.add( newAccountEmail);

	// Add a text field for the password.
	newAccountFormPanel.add( new JLabel( "Password"));
	JTextField newAccountPassword = new JTextField( 16);
	_newAccountMap.put( "password", newAccountPassword);
	newAccountFormPanel.add( newAccountPassword);

	// Add a text field for the API key.
	newAccountFormPanel.add( new JLabel( "API key"));
	JTextField newAccountApiKey = new JTextField( 16);
	_newAccountMap.put( "apiKey", newAccountApiKey);
	newAccountFormPanel.add( newAccountApiKey);

	// Add a text field for the secrect.
	newAccountFormPanel.add( new JLabel( "Secret"));
	JTextField newAccountSecret = new JTextField( 16);
	_newAccountMap.put( "secret", newAccountSecret);
	newAccountFormPanel.add( newAccountSecret);

	// Add a checkbox to activate the account.
	newAccountFormPanel.add( new JLabel( "Activated"));
	JCheckBox newAccountActivated = new JCheckBox();
	_newAccountMap.put( "activated", newAccountActivated);
	newAccountFormPanel.add( newAccountActivated);

	// Add the panel to the content pane.
	constraints.gridwidth = 1;
	constraints.gridy = 1;
	constraints.weighty = 0.1;
	constraints.weightx = 0.5;
	constraints.fill = GridBagConstraints.BOTH;
	contentPane.add( newAccountFormPanel, constraints);

	// Create a panel for the buttons.
	JPanel buttonPanel = new JPanel();

	// Create a button to add a new account.
	buttonPanel.add( _addButton = new JButton( "Add"));
	_addButton.addActionListener( this);  // pass actions to this class.

	// Create a button to close the dialog.
	buttonPanel.add( _closeButton = new JButton( "Close"));
	_closeButton.addActionListener( this);  // pass actions to this class.

	// Add the button panel besides the new account form.
	constraints.gridx = 1;
	constraints.gridwidth = 1;
	contentPane.add( buttonPanel, constraints); 

	// Set the window size.
	setSize( 900, 500);
    }


    // Methods

    /**
     * The user clicked on the manager.
     *
     * @param event The created event.
     */
    public void actionPerformed( ActionEvent event) {

	if( event.getSource() == _closeButton) {  // If the user clicken the close button.

	    setVisible( false);  // Hide the dialog.

	    // dispose();  // Remove the dialog.
	}
    }

    /**
     * Get the only instance of this dialog (singleton pattern).
     *
     * @return The only instance of this dialog.
     */
    public static TradeSiteUserAccountManagerDialog getInstance() {

	if( _instance == null) {  // If there is no instance yet,

	    _instance = new TradeSiteUserAccountManagerDialog();  // create one.
	}

	return _instance;  // Return the only instance of this class.
    }
}