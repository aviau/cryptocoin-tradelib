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

package de.andreas_rueckert.persistence.database;

import java.util.List;


/**
 * Interface for some class that uses database persistence.
 */
public interface DatabasePersistence {

    // Variables


    // Methods

    /**
     * Get the SQL statements to create the tables for this persistent object.
     *
     * @return The list of SQL statements to create the tables for this persistent object.
     */
    public List<String> getCreateTableStatements();

    /**
     * Get the SQL statements to drop the tables for this persistent object.
     *
     * @return The list of SQL statements to drop the tables for this persistent object.
     */
    public List<String> getDropTableStatements();

    /**
     * Load all the data (when the program was is started usually).
     */
    public void loadAll();

    /**
     * Save all data (when the program is terminated usually).
     */
    public void saveAll();

    /**
     * Store a reference to the persistence manager in the persistent object.
     *
     * @param dbPersistenceManager The database persistence manager.
     */
    public void setDatabasePersistenceManager( DatabasePersistenceManager dbPersistenceManager);
}