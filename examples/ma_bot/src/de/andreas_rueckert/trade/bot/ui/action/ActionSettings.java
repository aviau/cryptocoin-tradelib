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

package de.andreas_rueckert.trade.bot.ui.action;

import de.andreas_rueckert.persistence.PersistentProperties;
import de.andreas_rueckert.persistence.PersistentProperty;
import de.andreas_rueckert.persistence.PersistentPropertyList;
import de.andreas_rueckert.trade.site.TradeSite;
import de.andreas_rueckert.util.ModuleLoader;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;


/**
 * Show the settings dialog.
 */
public class ActionSettings extends AbstractAction {

    // Static variables

    /**
     * The only instance of this action ( singleton pattern).
     */
    private static ActionSettings _instance = null;


    // Instance variables

    /**
     * The save button for the settings.
     */
    private JButton _saveButton = null;

    /**
     * The panel with the settings.
     */
    private JPanel _settingsPanel = null;

    /**
     * The mapping from the swing components to the object keys.
     */
    private Map<PersistentProperties,Map<String,JTextField>> _inputFieldMapping = null;


    // Constructors


    // Methods

    /**
     * The user wants to edit the settings.
     *
     * @param e The action event.
     */
    public void actionPerformed( ActionEvent e) {

        if( e.getSource() == _saveButton) {  // The user wants to save the settings.
            saveSettings();
        }
    }

    /**
     * Get the only instance of this action.
     *
     * @return The only instance of this action.
     */
    public static ActionSettings getInstance() {

        if( _instance == null) {               // If there is no instance yet,
            _instance = new ActionSettings();  // create a new one.
        }

        return _instance;  // Return the only instance of this action.
    }

    /**
     * Get the settings panel.
     *
     * @return The settings panel.
     */
    private JPanel getSettingsPanel() {

        if( _settingsPanel == null) {       // If there is no settings panel yet
            _settingsPanel = new JPanel();  // create one.

            _settingsPanel.setLayout( new BorderLayout());  // Use the border layout for the settings.

            JTabbedPane tabbedPane = new JTabbedPane();

	    for( TradeSite t : ModuleLoader.getInstance().getRegisteredTradeSites().values()) {
            
                PersistentPropertyList settingsList = t.getSettings();

                JPanel currentSettings = new JPanel();

                Map<String, JTextField> currentTabMapping = new HashMap<String, JTextField>();

                currentSettings.setLayout( new GridLayout( settingsList.size(), 2));

                // Sort the list according to their priorities. 
                Collections.sort( settingsList);
                Collections.reverse( settingsList);

                for( PersistentProperty setting : settingsList) {
                    
                    currentSettings.add( new JLabel( ( setting.getTitle() != null ? setting.getTitle() : setting.getName()) + ":"));

                    JTextField currentField = new JTextField( setting.getValue() != null ? setting.getValue().toString() : "", 30);

                    currentSettings.add( currentField);

                    // Put the field to the mapping of this tab.
                    currentTabMapping.put( setting.getName(), currentField);
                }

                tabbedPane.add( t.getName(), currentSettings);

                _inputFieldMapping.put( t, currentTabMapping);  // Add the tab mapping to the global mapping.
            }

            _settingsPanel.add( tabbedPane, BorderLayout.CENTER);

            _saveButton = new JButton( "Save");

            _saveButton.addActionListener( this);

            _settingsPanel.add( _saveButton, BorderLayout.SOUTH);
        }

        return _settingsPanel;
    }

    /**
     * Save the settings panel settings to the objects.
     */
    private void saveSettings() {

        // Loop over the property objects.
        for( Map.Entry<PersistentProperties,Map<String,JTextField>> propertyObject : _inputFieldMapping.entrySet()) {
            PersistentProperties pObject = propertyObject.getKey();

            // Get a list with properties from the object and modify them according to the GUI entries.
            // We cannot(!) just create new PersistentProperty objects, because we would lose some
            // fields, like title or priority. That's why we have to modify the fetched properties here.
            PersistentPropertyList newSettings = pObject.getSettings();

            // Get the list of keys and textfields.
            for( Map.Entry<String,JTextField> fieldMapping : propertyObject.getValue().entrySet()) {

                // Loop the current settings to find the currently checked UI element.
                for( PersistentProperty property : newSettings) {

                    // If this property fits this UI field.
                    if( property.getName().equals( fieldMapping.getKey())) {

                        if( fieldMapping.getValue() instanceof JTextField) {

                            String inputFieldText = ((JTextField)fieldMapping.getValue()).getText();

                            // Set the new value from the UI input field.
                            // Check if this is an encrypted password, and encrypt the input if necessary.
                            property.setValue( inputFieldText);
                            
                        }
                    }
                }
            }

            pObject.setSettings( newSettings);  // Set the new settings for the object.
        }
    }
}