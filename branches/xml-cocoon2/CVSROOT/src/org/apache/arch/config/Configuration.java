/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.arch.config;

import java.util.Enumeration;

/**
 * This class represents the basic element for a structured
 * configuration repository used by <code>Configurable</code> classes.
 *
 * @author <a href="mailto:scoobie@betaversion.org">Federico Barbieri</a>
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.2.1 $ $Date: 1999-12-11 23:28:48 $
 */
 
public interface Configuration {

    /*
     * Returns the name of the root configuration element.
     */
	String getName();
	
    /*
     * Returns the value of the configuration element as String.
     * @exception IllegalStateException if value is not present.
     */
	String getValue();

    /*
     * Returns the value of the configuration element as int.
     * @exception IllegalStateException if value is not present.
     * @excpetion NumberFormatException if the value cannot be parsed as int.
     */
    int getValueAsInt();

    /*
     * Returns the value of the configuration element as long.
     * @exception IllegalStateException if value is not present.
     * @excpetion NumberFormatException if the value cannot be parsed as long.
     */
    long getValueAsLong();

    /*
     * Returns the value of the configuration element as float.
     * @exception IllegalStateException if value is not present.
     * @excpetion NumberFormatException if the value cannot be parsed as float.
     */
    float getValueAsFloat();

    /*
     * Returns the value of the configuration element as boolean,
     * returning "true" if the lowered-cased value is the string "true", false
     * otherwise.
     * @exception IllegalStateException if value is not present.
     */
    boolean getValueAsBoolean();
    
    /*
     * Returns the value of the attribute with the given name as String.
     * @exception IllegalStateException if value is not present.
     */
	String getAttribute(String name);
    
    /*
     * Returns the value of the attribute with the given name as int.
     * @exception IllegalStateException if value is not present.
     * @excpetion NumberFormatException if the value cannot be parsed as int.
     */
    int getAttributeAsInt(String name);

    /*
     * Returns the value of the attribute with the given name as long.
     * @exception IllegalStateException if value is not present.
     * @excpetion NumberFormatException if the value cannot be parsed as long.
     */
    long getAttributeAsLong(String name);

    /*
     * Returns the value of the attribute with the given name as float.
     * @exception IllegalStateException if value is not present.
     * @excpetion NumberFormatException if the value cannot be parsed as float.
     */
    float getAttributeAsFloat(String name);

    /*
     * Returns the value of the attribute with the given name as boolean, 
     * returning "true" if the lowered-cased value is the string "true", false
     * otherwise.
     * @exception IllegalStateException if value is not present.
     */
    boolean getAttributeAsBoolean(String name);

    /*
     * Return the Configuration object associated with the given
     * name.
     * @param name the name of the required configuration using the 
     * absolute path of the format "/path/name".
     * @exception IllegalArgumentException no configuration is found
     * for the given name.
     * @exception IllegalStateException more than one configuration is found
     * for the given name. (in this case, the method getConfigurations() should
     * be called instead)
     */
    Configuration getConfiguration(String name);

    /*
     * Return an Enumeration of <code>Configuration</code> objects
     * associated with the given name.
     * @param name the name of the required configurations using the 
     * absolute path of the format "/path/name".
     * @exception IllegalArgumentException no configuration is found
     * for the given name.
     */
	Enumeration getConfigurations(String name);
}