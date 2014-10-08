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

package de.andreas_rueckert.trade.ui;

import de.andreas_rueckert.trade.Amount;
import de.andreas_rueckert.trade.currency.CurrencyPair;
import de.andreas_rueckert.trade.currency.CurrencyPairImpl;
import de.andreas_rueckert.trade.order.OrderFactory;
import de.andreas_rueckert.trade.order.OrderType;
import de.andreas_rueckert.trade.order.SiteOrder;
import de.andreas_rueckert.trade.Price;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.util.ModuleLoader;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * This dialog lets the user input order data.
 */
public class OrderDialog extends JDialog implements ActionListener {

    // Static variables


    // Instance variables

    /**
     * The input field to enter the amount.
     */
    private JTextField _amountField;

    /**
     * The button to cancel an the order before the execution.
     */
    private JButton _cancelButton;

    /**
     * The list of traded currency pairs.
     */
    private JComboBox _currencyPairList;

    /**
     * The button to execute the order.
     */
    private JButton _executeButton;

    /**
     * The list of order types.
     */
    private JComboBox _orderTypeList = null;

    /**
     * Create a map from the combo box string and the OrderType to get the selected
     * value a bit easier.
     */
    private Map<String, OrderType> _orderTypeMap = new HashMap<String, OrderType>();

    /**
     * The input field to enter the price.
     */
    private JTextField _priceField;

    /**
     * The resulting order from this dialog. Default is null, so
     * there is no order created, if the user pressed the cancel button.
     */
    private SiteOrder _resultingOrder = null;

    /**
     * The list of trade sites.
     */
    private JComboBox _tradeSiteList = null;


    // Constructors

    /**
     * Create a new dialog to enter order data and trigger the execution of an order.
     *
     * @param parentFrame The parent frame.
     * @param modal Flag to indicate, if the dialog should be modal.
     * @param tradeInfo Some info on the trade.
     * @param tradeSite The trade site to trade on.
     * @param orderType The type of the order (buy, sell etc)
     * @param price The price for the order.
     * @param currencyPair The used currency pair for the order.
     * @param amount The traded amount.
     */
    public OrderDialog( JFrame parentFrame
			, boolean modal
			, String tradeInfo
			, TradeSite tradeSite
			, OrderType orderType
			, Price price
			, CurrencyPair currencyPair
			, Amount amount) {

	super( parentFrame, modal);

	JPanel dialogPanel = new JPanel();  // The main panel for the panel.
	dialogPanel.setLayout( new BorderLayout());

	// Creating a panel with some info on the order.
	JPanel infoPanel = new JPanel();
	infoPanel.add( new JLabel( tradeInfo));
	dialogPanel.add( infoPanel, BorderLayout.NORTH);

	// Create a panel for the order data.
	JPanel orderDataPanel = new JPanel();
	orderDataPanel.setLayout( new GridLayout( 5, 2));

	// Add a list of available trade sites.
	orderDataPanel.add( new JLabel( "Trade site:"));
	orderDataPanel.add( _tradeSiteList = new JComboBox());
	setTradeSites( tradeSite);
	_tradeSiteList.addActionListener( this);
	_tradeSiteList.setEditable( false);

	// Add a list with the available order types.
	orderDataPanel.add( new JLabel( "Order type"));
	orderDataPanel.add( _orderTypeList = new JComboBox());
	setOrderTypes( orderType);
	_orderTypeList.addActionListener( this);
	_orderTypeList.setEditable( false);

	// Add a list with the availabe currency pair.
	orderDataPanel.add( new JLabel( "Currency pair:"));
	orderDataPanel.add( _currencyPairList = new JComboBox());
	setCurrencyPairs( tradeSite, currencyPair);  // Add the available currency pairs to the list.
	_currencyPairList.addActionListener( this);
	_currencyPairList.setEditable( false);

	orderDataPanel.add( new JLabel( "Price:"));
	orderDataPanel.add( _priceField = new JTextField( price.toString()));
	
	orderDataPanel.add( new JLabel( "Amount:"));
	orderDataPanel.add( _amountField = new JTextField( amount.toString()));

	dialogPanel.add( orderDataPanel, BorderLayout.CENTER);

	// Create a panel for the buttons.
	JPanel buttonPanel = new JPanel();
	buttonPanel.add( _executeButton = new JButton( "Execute"));
	_executeButton.addActionListener( this);
	buttonPanel.add( _cancelButton = new JButton( "Cancel"));
	_cancelButton.addActionListener( this);
	dialogPanel.add( buttonPanel, BorderLayout.SOUTH);

	getContentPane().add( dialogPanel);
	pack();
        setLocationRelativeTo( parentFrame);
        setVisible( true);
    }


    // Methods

    /**
     * The user pressed a button.
     *
     * @param e The action event.
     */
    public void actionPerformed( ActionEvent e) {
	
	if( e.getSource() == _executeButton) {  // If the user wants to execute the order...

	    // Create an order from the entered data.
	    createOrder();

	} else if( e.getSource() == _cancelButton) {  // If the user wants to cancel the order...

	    // Do nothing for now...
	}

	setVisible( false);  // Disable the dialog.
	dispose();  // Release the dialog resources. 
    }

    /**
     * Create an order from the entered data.
     */
    private final void createOrder() {

	// Get the selected trade site.
	TradeSite tradeSite = ModuleLoader.getInstance().getRegisteredTradeSite( (String)_tradeSiteList.getSelectedItem());

	// Get the order type.
	OrderType orderType = _orderTypeMap.get( (String)_orderTypeList.getSelectedItem());

	// Get the selected currency pair.
	// As long as the unmodified toString() representation of the currency pairs is used, we should
	// not need a map to find the correspondig currency pair.
	CurrencyPair currencyPair = CurrencyPairImpl.getCurrencyPairForCode( (String)_currencyPairList.getSelectedItem());

	// Get the price from the text field.
	// Maybe the buttons should be greyed out, until a reasonable price is entered?
	Price price = new Price( _priceField.getText());

	// Get the amount from the text field.
	Amount amount = new Amount( _amountField.getText());

	// Now create the order via the order factory.
	// Leave the account away for now (ugly hack).
	_resultingOrder = OrderFactory.createCryptoCoinTradeOrder( tradeSite, null, orderType, price, currencyPair, amount);
    }

    /**
     * Get the content of the amount input field.
     *
     * @return The content of the amount input field.
     */
    public String getAmount() {
	return _amountField.getText();
    }

    /**
     * Get the resulting order or null, if the user canceled the dialog.
     *
     * @return The created order or null, if the user canceled the dialog.
     */
    public final SiteOrder getOrder() {
	
	return _resultingOrder;
    }

    /**
     * Get the content of the price input field.
     *
     * @return The content of the price input field.
     */
    public String getPrice() {
	return _priceField.getText();
    }

    /**
     * Set the available trade site in the list.
     *
     * @param defaultTradeSite The selected trade site, or null if no specific trade site should be selected.
     */
    private final void setTradeSites( TradeSite defaultTradeSite) {

	// Remove all the current trade sites from the list.
	_tradeSiteList.removeAllItems();

	// Get all the registered trade sites and loop over them.
	for( TradeSite currentTradeSite : ModuleLoader.getInstance().getRegisteredTradeSites().values()) {

	    // Add the name of the current trade site to the list.
	    _tradeSiteList.addItem( currentTradeSite.getName());

	    // If the user wants a specific site selected and the current site is this site,
	    // select it.
	    if( ( defaultTradeSite != null) && defaultTradeSite.equals( currentTradeSite)) {

		// Select the last added trade site.
		_tradeSiteList.setSelectedIndex( _tradeSiteList.getItemCount() - 1);
	    }
	}
    }

    /**
     * Set the available currency pairs in the list. 
     *
     * @param tradeSite The site, we want to trade on.
     * @param defaultCurrencyPair The selected currency pair, or null, if no specific currency pair is selected by default.
     */
    private final void setCurrencyPairs( TradeSite tradeSite, CurrencyPair defaultCurrencyPair) {

	// Remove all the current currency pairs from the list.
	_currencyPairList.removeAllItems();
	
	// Get all the supported currency pairs from the tradesite.
	CurrencyPair [] _supportedCurrencyPairs = tradeSite.getSupportedCurrencyPairs();

	// Add all the currency pairs to the list.
	for( int currentPairIndex = 0; currentPairIndex < _supportedCurrencyPairs.length; ++currentPairIndex) {

	    // Add a string representation of the current currency pair to the list.
	    _currencyPairList.addItem( _supportedCurrencyPairs[ currentPairIndex].toString());

	    // If the user wants a currency pair selected, check if it equals the current currency pair.
	    if( ( defaultCurrencyPair != null) && defaultCurrencyPair.equals( _supportedCurrencyPairs[ currentPairIndex])) {

		// Set the current index as the selected item.
		_currencyPairList.setSelectedIndex( currentPairIndex);
	    }
	}
    }

    /**
     * Set the list of order types with a default order type (eventually).
     *
     * @param orderType The default order type, or null, if no order type is specifically selected.
     */
    private final void setOrderTypes( OrderType orderType) {

	// Remove all the order types from the list.
	_orderTypeList.removeAllItems();
	
	// Clear the map, too.
	_orderTypeMap.clear();
	    

	// Add all the available order types to the list.
	// Or better just add buy or sell?
	for( OrderType currentType : OrderType.values()){

	    // Store the used name of the type.
	    String orderTypeName = currentType.name().toLowerCase();

	    // Only add buy or sell for now...
	    if( orderTypeName.equalsIgnoreCase( "buy")
		|| orderTypeName.equalsIgnoreCase( "sell")) {

		// Add the current item to the list.
		_orderTypeList.addItem( orderTypeName);

		// Add the type to the map, too. So we can retrieve it later.
		_orderTypeMap.put( orderTypeName, currentType);
		
		// If the user wants this type to be selected, select it.
		if( ( orderType != null) && orderType.equals( currentType)) {
		
		    // Set the last added item as selected-
		    _orderTypeList.setSelectedIndex( _orderTypeList.getItemCount() - 1);
		}
	    }
	}
    }
}
