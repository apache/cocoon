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
 *         (Betaversion Productions)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 *         (Apache Software Foundation)
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-02-27 01:19:57 $
 */
public interface Configuration {

    /**
     * Returns the name of this configuration element.
     */
	public String getName();

    /**
     * Returns the value of the configuration element as a <code>String</code>.
     *
     * @exception ConfigurationException If the value is not present.
     */
	public String getValue()
	throws ConfigurationException;

    /**
     * Returns the value of the configuration element as an <code>int</code>.
     *
     * @exception ConfigurationException If the value is not present or it
     *                                   cannot be represented as an
     *                                   <code>int</code>.
     */
    public int getValueAsInt()
	throws ConfigurationException;

    /**
     * Returns the value of the configuration element as a <code>long</code>.
     *
     * @exception ConfigurationException If the value is not present or it
     *                                   cannot be represented as a
     *                                   <code>long</code>.
     */
    public long getValueAsLong()
	throws ConfigurationException;

    /**
     * Returns the value of the configuration element as a <code>float</code>.
     *
     * @exception ConfigurationException If the value is not present or it
     *                                   cannot be represented as a
     *                                   <code>float</code>.
     */
    public float getValueAsFloat()
	throws ConfigurationException;

    /**
     * Returns the value of the configuration element as a <code>boolean</code>.
     * <br>
     * This method returns <b>true</b> if the value of the this configuration
     * element equals the lowered-case <code>String</code> &quot;true&quot;,
     * <b>false</b> if it equals the lowered-case <code>String</code>
     * &quot;false&quot;, or it throws a <code>ConfigurationException</code>.
     *
     * @exception ConfigurationException If the value is not present or it
     *                                   cannot be represented as a
     *                                   <code>boolean</code>.
     */
    public boolean getValueAsBoolean()
	throws ConfigurationException;
    
    /**
     * Returns the value of the attribute specified by its name as a
     * <code>String</code>.
     *
     * @exception ConfigurationException If the attribute is not present.
     */
	public String getAttribute(String name)
	throws ConfigurationException;
    
    /**
     * Returns the value of the attribute specified by its name as an
     * <code>int</code>.
     *
     * @exception ConfigurationException If the attribute is not present or
     *                                   its value cannot be represented as an
     *                                   <code>int</code>.
     */
    public int getAttributeAsInt(String name)
	throws ConfigurationException;

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>long</code>.
     *
     * @exception ConfigurationException If the attribute is not present or
     *                                   its value cannot be represented as a
     *                                   <code>long</code>.
     */
    public long getAttributeAsLong(String name)
	throws ConfigurationException;

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>float</code>.
     *
     * @exception ConfigurationException If the attribute is not present or
     *                                   its value cannot be represented as a
     *                                   <code>float</code>.
     */
    public float getAttributeAsFloat(String name)
	throws ConfigurationException;

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>boolean</code>.
     * <br>
     * This method returns <b>true</b> if the value of the the specified
     * attribute equals the lowered-case <code>String</code> &quot;true&quot;,
     * <b>false</b> if it equals the lowered-case <code>String</code>
     * &quot;false&quot;, or it throws a <code>ConfigurationException</code>.
     *
     * @exception ConfigurationException If the attribute is not present or
     *                                   its value cannot be represented as a
     *                                   <code>boolean</code>.
     */
    public boolean getAttributeAsBoolean(String name)
	throws ConfigurationException;

    /**
     * Returns the value of the configuration element as a <code>String</code>.
     */
	public String getValue(String defaultValue);

    /**
     * Returns the value of the configuration element as an <code>int</code>.
     */
    public int getValueAsInt(int defaultValue);

    /**
     * Returns the value of the configuration element as a <code>long</code>.
     */
    public long getValueAsLong(long defaultValue);

    /**
     * Returns the value of the configuration element as a <code>float</code>.
     */
    public float getValueAsFloat(float defaultValue);

    /**
     * Returns the value of the configuration element as a <code>boolean</code>.
     */
    public boolean getValueAsBoolean(boolean defaultValue);
    
    /**
     * Returns the value of the attribute specified by its name as a
     * <code>String</code>.
     */
	public String getAttribute(String name, String defaultValue);
    
    /**
     * Returns the value of the attribute specified by its name as an
     * <code>int</code>.
     */
    public int getAttributeAsInt(String name, int defaultValue);

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>long</code>.
     */
    public long getAttributeAsLong(String name, long defaultValue);

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>float</code>.
     */
    public float getAttributeAsFloat(String name, float defaultValue);

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>boolean</code>.
     */
    public boolean getAttributeAsBoolean(String name, boolean defaultValue);

    /**
     * Return the first <code>Configuration</code> object child of this
     * associated with the given name or <b>null</b>.
     *
     * @param name The name of the required child <code>Configuration</code>.
     */
    public Configuration getConfiguration(String name);

    /**
     * Return an <code>Enumeration</code> of <code>Configuration</code> objects
     * children of this associated with the given name.
     * <br>
     * The returned <code>Enumeration</code> may be empty.
     *
     * @param name The name of the required children <code>Configuration</code>.
     */
	public Enumeration getConfigurations(String name);
	
	/**
	 * Return a <code>String</code> indicating the position of this
	 * configuration element in a source file or URI or <b>null</b>.
	 */
	public String getLocation();
}