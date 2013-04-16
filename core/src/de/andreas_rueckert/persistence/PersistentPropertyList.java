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

package de.andreas_rueckert.persistence;

import java.util.ArrayList;


/**
 * This class represents a list of persistent property objects.
 * It's main purpose is, to encapsulate search methods for those 
 * property objects.
 */
public class PersistentPropertyList extends ArrayList<PersistentProperty> {

    // Static variables


    // Instance variables


    // Constructors


    // Methods

    /**
     * Get a property, that is represented as a String object.
     *
     * @param propertyName The name of the property.
     *
     * @return The value of the property as a String object, or null, if the property is not in the list.
     */
    public String getStringProperty( String propertyName) {
	for( PersistentProperty property : this) {
	    if( propertyName.equals( property.getName())) {

		Object currentValue = property.getValue();  // Try to get a value for this property.
  
		if( ( currentValue != null) && ( currentValue instanceof String) && ( ! "null".equals( (String)currentValue))) {
		    return (String)currentValue;
		}
	    }
	}
	return null;  // Found no String value for this property name.
    }
}