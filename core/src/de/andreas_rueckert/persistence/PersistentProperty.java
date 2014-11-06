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


/**
 * This class represents one persistent property.
 */
public class PersistentProperty implements Comparable<PersistentProperty> {

    // Static variables


    // Instance variables

    /**
     * Flag to indicate, that the value of this property is an
     * encrypted password.
     */
    private boolean _encryptedPasswordFlag = false;

    /**
     * This is the name of this property. It is used to identify it in the
     * trade site implementation or the settings UI.
     */
    private String _name;

    /**
     * This is the priority of this property. It is used in the settings UI to
     * sort the properties. If the priority is higher, the UI will move this
     * property more up in the list.
     */
    private int _priority = 0;

    /**
     * This is a title, that could be used in the UI for this property. It can be
     * null, if the name should be used for this property. Sometimes the name is
     * not very descriptive, so additional explaination might be helpful then.
     */
    private String _title;

    /**
     * The value of the setting. Could be an object, like a String or a Double.
     */
    private Object _value;


    // Constructors

    /**
     * Create a new property instance with some given values.
     *
     * @param name The name of the property.
     * @param title The title for the property (could be null).
     * @param value The value of the property.
     * @param priority The priority of the property.
     */
    public PersistentProperty( String name, String title, Object value, int priority) {

	// Store the values in the instance.
	_name = name;
	_title = title;
	setValue( value);
	_priority = priority;
    }

    /**
     * Create a new property instance with some given values.
     *
     * @param name The name of the property.
     * @param title The title for the property (could be null).
     * @param value The value of the property.
     * @param priority The priority of the property.
     * @param encryptedPasswordFlag Flag to indicate, that the entered string should be encrypted as a password. 
     */
    public PersistentProperty( String name, String title, Object value, int priority, boolean encryptedPasswordFlag) {
	
	// Use the default constructor to store the parameters in the instance.
	this( name, title, value, priority);

	// Store the password encryption flag.
	_encryptedPasswordFlag = encryptedPasswordFlag;
    }


    // Methods

    /**
     * Compare 2 properties according to thier priority.
     *
     * @param property The property to compare.
     *
     * @return < 0, 0 or > 0 as the result of the comparison.
     */
    public int compareTo( PersistentProperty property) {
	return getPriority() - property.getPriority();
    }

    /**
     * Get the name of this property.
     *
     * @return The name of this property.
     */
    public final String getName() {
	return _name;
    }

    /**
     * Get the priority of this property to sort the entries in the UI.
     *
     * @return The priority of this property.
     */
    public final int getPriority() {
	return _priority;
    }

    /**
     * Get an additional title for the UI, if one was set, or just null, if the name
     * should be used.
     *
     * @return And additional UI title for this property, or null if the name should be used.
     */
    public final String getTitle() {
	return _title;
    }

    /**
     * Get the value of this property, or null if there was no value set.
     *
     * @return The value of this property or null, if there was no value set yet.
     */
    public final Object getValue() {
	return _value;
    }

    /**
     * Check, if this property is an encrypted password.
     *
     * @return true, if this property is an encrypted password. False otherwise.
     */
    public final boolean isEncryptedPassword() {
	return _encryptedPasswordFlag;
    }
    
    /**
     * Set a new value for this property.
     *
     * @param value The new value for this property.
     */
    public final void setValue( Object value) {
	_value = value;
    }
}
