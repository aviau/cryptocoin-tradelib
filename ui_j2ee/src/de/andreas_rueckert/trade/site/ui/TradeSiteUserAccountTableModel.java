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

import de.andreas_rueckert.trade.site.TradeSiteUserAccount;
import de.andreas_rueckert.trade.site.TradeSiteUserAccountPersistence ;
import de.andreas_rueckert.trade.site.TradeSiteUserAccountPersistenceSQLImpl;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.table.AbstractTableModel;


/**
 * A table model to display the existing user accounts.
 */
class TradeSiteUserAccountTableModel extends AbstractTableModel { 

    // Inner classes


    // Static variables


    // Instance variables

    /**
     * The names of the account table columns.
     */
    private final String [] _columnNames = { "id", "name", "site", "email", "password", "api key", "secret", "active", "created"};

    /**
     * The current list of user accounts.
     */
    private List<TradeSiteUserAccount> _accountList = null;

    /**
     * This class creates an access to the default database.
     */
    private TradeSiteUserAccountPersistence _accountPersistence = null;  // Just a dummy for now.


    // Constructors

    /**
     * Create a new instance for the user account manager.
     */
    public TradeSiteUserAccountTableModel() {

	// Get the account list from the user account persistence.
	_accountList = TradeSiteUserAccountPersistenceSQLImpl.getInstance().getAllAccounts();
    }


    // Methods

    /**
     * Get the number of columns of this table.
     *
     * @return The number of columns of this table.
     */
    public int getColumnCount() {
	
	return _columnNames.length;
    }

    /**
     * Get the name of the column with a given index.
     *
     * @param columnIndex The index of the column.
     *
     * @return The name of the column.
     */
    public String getColumnName( int columnIndex) {

        return _columnNames[ columnIndex];
    }

    /**
     * Get the number of rows.
     *
     * @return The number of rows.
     */
    public int getRowCount() {

	// If there is a list of accounts, return it's length.
	return _accountList == null ? 0 : _accountList.size();  
    }

    /**
     * Get the value at a given table index.
     *
     * @param rowIndex The index of the row.
     * @param columnIndex The index of the column.
     *
     * @return The table value of the given index.
     */
    public Object getValueAt( int rowIndex, int columnIndex) {

	// Get the account of this record.
	TradeSiteUserAccount currentAccount = _accountList.get( rowIndex);

	// Get the requested column.
	switch( columnIndex) {
	case 0: return "" + currentAccount.getId();
	case 1: return currentAccount.getAccountName();
	case 2: return currentAccount.getTradeSite().getName();
	case 3: return currentAccount.getEmail();
	case 4: return currentAccount.getPassword();
	case 5: return currentAccount.getAPIkey();
	case 6: return currentAccount.getSecret();
	case 7: return ( currentAccount.isActivated() ? "active" : "deactivated");
	case 8: return new SimpleDateFormat().format( currentAccount.getCreated());
	}

	return null;  // Should never be reached.
    }
}