/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.cocoon.components.slide.impl;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.slide.util.conf.Configuration;
import org.apache.slide.util.conf.ConfigurationException;

/**
 * The class represent an adapter for the configuration class from jakarta slide
 *
 * @author <a href="mailto:stephan@vern.chem.tu-berlin.de">Stephan Michels</a>
 * @version CVS $Id: SlideConfigurationAdapter.java,v 1.3 2004/03/05 13:02:23 bdelacretaz Exp $
 */
public class SlideConfigurationAdapter implements Configuration {

    private org.apache.avalon.framework.configuration.Configuration configuration;

    /**
     * Create a new adapter to map a Avalon configuration 
     * to a Slide configuration 
     *
     * @param configuration Avalon configuration
     */
    public SlideConfigurationAdapter(org.apache.avalon.framework.configuration.Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Return the name of the node.
     *
     * @return name of the <code>Configuration</code> node.
     */
    public String getName() {
        return this.configuration.getName();
    }

    /**
     * Return a new <code>Configuration</code> instance encapsulating the
     * specified child node.
     *
     * @param child The name of the child node.
     *
     * @return Configuration
     *
     * @throws ConfigurationException If no child with that name exists.
     */
    public Configuration getConfiguration(String child)
      throws ConfigurationException {
        return new SlideConfigurationAdapter(this.configuration.getChild(child));
    }

    /**
     * Return an <code>Enumeration</code> of <code>Configuration<code>
     * elements containing all node children with the specified name.
     *
     * @param name The name of the children to get.
     *
     * @return Enumeration.  The <code>Enumeration</code> will be
     *         empty if there are no nodes by the specified name.
     */
    public Enumeration getConfigurations(String name) {

        Vector configurations = new Vector();
        org.apache.avalon.framework.configuration.Configuration[] childs = this.configuration.getChildren(name);

        for (int i = 0; i<childs.length; i++) {
            configurations.addElement(new SlideConfigurationAdapter(childs[i]));
        }
        return configurations.elements();
    }

    /**
     * Return the value of specified attribute.
     *
     * @param paramName The name of the parameter you ask the value of.
     *
     * @return String value of attribute.
     *
     * @throws ConfigurationException If no attribute with that name exists.
     */
    public String getAttribute(String paramName)
      throws ConfigurationException {

        try {
            return this.configuration.getAttribute(paramName);
        } catch (org.apache.avalon.framework.configuration.ConfigurationException ce) {
            throw new ConfigurationException(ce.getMessage(), this);
        }
    }

    /**
     * Return the <code>int</code> value of the specified attribute contained
     * in this node.
     *
     * @param paramName The name of the parameter you ask the value of.
     *
     * @return int value of attribute
     *
     * @throws ConfigurationException If no parameter with that name exists.
     *                                or if conversion to <code>int</code> fails.
     */
    public int getAttributeAsInt(String paramName)
      throws ConfigurationException {

        try {
            return this.configuration.getAttributeAsInteger(paramName);
        } catch (org.apache.avalon.framework.configuration.ConfigurationException ce) {
            throw new ConfigurationException(ce.getMessage(), this);
        }
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>long</code>.
     *
     *
     * @param name       
     *
     * @return long value of attribute
     *
     * @throws ConfigurationException If no parameter with that name exists.
     *                                or if conversion to <code>long</code> fails.
     */
    public long getAttributeAsLong(String name)
      throws ConfigurationException {

        try {
            return this.configuration.getAttributeAsLong(name);
        } catch (org.apache.avalon.framework.configuration.ConfigurationException ce) {
            throw new ConfigurationException(ce.getMessage(), this);
        }
    }

    /**
     * Return the <code>float</code> value of the specified parameter contained
     * in this node.
     *
     * @param paramName The name of the parameter you ask the value of.
     *
     * @return float value of attribute
     *
     * @throws ConfigurationException If no parameter with that name exists.
     *                                or if conversion to <code>float</code> fails.
     */
    public float getAttributeAsFloat(String paramName)
      throws ConfigurationException {

        try {
            return this.configuration.getAttributeAsFloat(paramName);
        } catch (org.apache.avalon.framework.configuration.ConfigurationException ce) {
            throw new ConfigurationException(ce.getMessage(), this);
        }
    }

    /**
     * Return the <code>boolean</code> value of the specified parameter contained
     * in this node.<br>
     *
     * @param paramName The name of the parameter you ask the value of.
     *
     * @return boolean value of attribute
     *
     * @throws ConfigurationException If no parameter with that name exists.
     *                                   or if conversion to <code>boolean</code> fails.
     */
    public boolean getAttributeAsBoolean(String paramName)
      throws ConfigurationException {

        try {
            return this.configuration.getAttributeAsBoolean(paramName);
        } catch (org.apache.avalon.framework.configuration.ConfigurationException ce) {
            throw new ConfigurationException(ce.getMessage(), this);
        }
    }

    /**
     * Return the <code>String</code> value of the node.
     *
     * @return the value of the node.
     */
    public String getValue() {

        try {
            return this.configuration.getValue();
        } catch (org.apache.avalon.framework.configuration.ConfigurationException ce) {
            return "";
        }
    }

    /**
     * Return the <code>int</code> value of the node.
     *
     * @return the value of the node.
     *
     * @throws ConfigurationException If conversion to <code>int</code> fails.
     */
    public int getValueAsInt() throws ConfigurationException {

        try {
            return this.configuration.getValueAsInteger();
        } catch (org.apache.avalon.framework.configuration.ConfigurationException ce) {
            throw new ConfigurationException(ce.getMessage(), this);
        }
    }

    /**
     * Return the <code>float</code> value of the node.
     *
     * @return the value of the node.
     *
     * @throws ConfigurationException If conversion to <code>float</code> fails.
     */
    public float getValueAsFloat() throws ConfigurationException {

        try {
            return this.configuration.getValueAsFloat();
        } catch (org.apache.avalon.framework.configuration.ConfigurationException ce) {
            throw new ConfigurationException(ce.getMessage(), this);
        }
    }

    /**
     * Return the <code>boolean</code> value of the node.
     *
     * @return the value of the node.
     *
     * @throws ConfigurationException If conversion to <code>boolean</code> fails.
     */
    public boolean getValueAsBoolean() throws ConfigurationException {

        try {
            return this.configuration.getValueAsBoolean();
        } catch (org.apache.avalon.framework.configuration.ConfigurationException ce) {
            throw new ConfigurationException(ce.getMessage(), this);
        }
    }

    /**
     * Return the <code>long</code> value of the node.<br>
     *
     * @return the value of the node.
     *
     * @throws ConfigurationException If conversion to <code>long</code> fails.
     */
    public long getValueAsLong() throws ConfigurationException {

        try {
            return this.configuration.getValueAsLong();
        } catch (org.apache.avalon.framework.configuration.ConfigurationException ce) {
            throw new ConfigurationException(ce.getMessage(), this);
        }
    }

    /**
     * Returns the value of the configuration element as a <code>String</code>.
     * If the configuration value is not set, the default value will be
     * used.
     *
     * @param defaultValue The default value desired.
     *
     * @return String value of the <code>Configuration</code>, or default
     *         if none specified.
     */
    public String getValue(String defaultValue) {

        return this.configuration.getValue(defaultValue);
    }

    /**
     * Returns the value of the configuration element as an <code>int</code>.
     * If the configuration value is not set, the default value will be
     * used.
     *
     * @param defaultValue The default value desired.
     *
     * @return int value of the <code>Configuration</code>, or default
     *         if none specified.
     */
    public int getValueAsInt(int defaultValue) {

        return this.configuration.getValueAsInteger(defaultValue);
    }

    /**
     * Returns the value of the configuration element as a <code>long</code>.
     * If the configuration value is not set, the default value will be
     * used.
     *
     * @param defaultValue The default value desired.
     *
     * @return long value of the <code>Configuration</code>, or default
     *         if none specified.
     */
    public long getValueAsLong(long defaultValue) {

        return this.configuration.getValueAsLong(defaultValue);
    }

    /**
     * Returns the value of the configuration element as a <code>float</code>.
     * If the configuration value is not set, the default value will be
     * used.
     *
     * @param defaultValue The default value desired.
     *
     * @return float value of the <code>Configuration</code>, or default
     *         if none specified.
     */
    public float getValueAsFloat(float defaultValue) {

        return this.configuration.getValueAsFloat(defaultValue);
    }

    /**
     * Returns the value of the configuration element as a <code>boolean</code>.
     * If the configuration value is not set, the default value will be
     * used.
     *
     * @param defaultValue The default value desired.
     *
     * @return boolean value of the <code>Configuration</code>, or default
     *         if none specified.
     */
    public boolean getValueAsBoolean(boolean defaultValue) {

        return this.configuration.getValueAsBoolean(defaultValue);
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>String</code>, or the default value if no attribute by
     * that name exists or is empty.
     *
     * @param name The name of the attribute you ask the value of.
     * @param defaultValue The default value desired.
     *
     * @return String value of attribute. It will return the default
     *         value if the named attribute does not exist, or if
     *         the value is not set.
     */
    public String getAttribute(String name, String defaultValue) {

        return this.configuration.getAttribute(name, defaultValue);
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>int</code>, or the default value if no attribute by
     * that name exists or is empty.
     *
     * @param name The name of the attribute you ask the value of.
     * @param defaultValue The default value desired.
     *
     * @return int value of attribute. It will return the default
     *         value if the named attribute does not exist, or if
     *         the value is not set.
     */
    public int getAttributeAsInt(String name, int defaultValue) {

        return this.configuration.getAttributeAsInteger(name, defaultValue);
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>long</code>, or the default value if no attribute by
     * that name exists or is empty.
     *
     * @param name The name of the attribute you ask the value of.
     * @param defaultValue The default value desired.
     *
     * @return long value of attribute. It will return the default
     *         value if the named attribute does not exist, or if
     *         the value is not set.
     */
    public long getAttributeAsLong(String name, long defaultValue) {

        return this.configuration.getAttributeAsLong(name, defaultValue);
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>float</code>, or the default value if no attribute by
     * that name exists or is empty.
     *
     * @param name The name of the attribute you ask the value of.
     * @param defaultValue The default value desired.
     *
     * @return float value of attribute. It will return the default
     *         value if the named attribute does not exist, or if
     *         the value is not set.
     */
    public float getAttributeAsFloat(String name, float defaultValue) {

        return this.configuration.getAttributeAsFloat(name, defaultValue);
    }

    /**
     * Returns the value of the attribute specified by its name as a
     * <code>boolean</code>, or the default value if no attribute by
     * that name exists or is empty.
     *
     * @param name The name of the attribute you ask the value of.
     * @param defaultValue The default value desired.
     *
     * @return boolean value of attribute. It will return the default
     *         value if the named attribute does not exist, or if
     *         the value is not set.
     */
    public boolean getAttributeAsBoolean(String name, boolean defaultValue) {

        return this.configuration.getAttributeAsBoolean(name, defaultValue);
    }

    /**
     * Return a <code>String</code> indicating the position of this
     * configuration element in a source file or URI.
     *
     * @return String if a source file or URI is specified.  Otherwise
     *         it returns <code>null</code>
     */
    public String getLocation() {

        return this.configuration.getLocation();
    }
}
