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

package de.andreas_rueckert.trade.site;

import de.andreas_rueckert.persistence.database.DatabasePersistence;
import java.util.List;


/**
 * This interface defines the methods to load and save user accounts for trade sites.
 */
public interface TradeSiteUserAccountPersistence extends DatabasePersistence {

    // Variables


    // Methods

    /**
     * Add a new account.
     *
     * @param newAccount The new account to be added.
     */
    public void addAccount( TradeSiteUserAccount newAccount);

    /**
     * Delete a user account.
     *
     * @param account The account to be deleted.
     */
    public void delete( TradeSiteUserAccount account);

    /**
     * Get all accounts, that are currently stored.
     *
     * @return all accounts, that are currently stored.
     */
    public List<TradeSiteUserAccount> getAllAccounts();
}