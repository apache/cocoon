/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.components;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * This configuration object is used for {@link SitemapConfigurable} 
 * components. It 'extends' {@link Configuration} by a parent.
 *
 * @since 2.1
 *
 * @version $Id$
 */
public final class ChainedConfiguration implements Configuration {

    private Configuration wrappedConfiguration;

    private ChainedConfiguration parentConfiguration;

    /**
     * Constructor
     */
    public ChainedConfiguration(Configuration wrapped,
                                ChainedConfiguration parent) {
        this.wrappedConfiguration = wrapped;
        this.parentConfiguration = parent;
    }

    /** 
     * Get the parent configuration
     * @return the parent configuration or null.
     */
    public ChainedConfiguration getParent() {
        return this.parentConfiguration;
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getAttribute(java.lang.String, java.lang.String)
     */
    public String getAttribute(String arg0, String arg1) {
        return this.wrappedConfiguration.getAttribute(arg0, arg1);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getAttribute(java.lang.String)
     */
    public String getAttribute(String arg0) throws ConfigurationException {
        return this.wrappedConfiguration.getAttribute(arg0);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getAttributeAsBoolean(java.lang.String, boolean)
     */
    public boolean getAttributeAsBoolean(String arg0, boolean arg1) {
        return this.wrappedConfiguration.getAttributeAsBoolean(arg0, arg1);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getAttributeAsBoolean(java.lang.String)
     */
    public boolean getAttributeAsBoolean(String arg0)
        throws ConfigurationException {
        return this.wrappedConfiguration.getAttributeAsBoolean(arg0);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getAttributeAsFloat(java.lang.String, float)
     */
    public float getAttributeAsFloat(String arg0, float arg1) {
        return this.wrappedConfiguration.getAttributeAsFloat(arg0, arg1);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getAttributeAsFloat(java.lang.String)
     */
    public float getAttributeAsFloat(String arg0)
        throws ConfigurationException {
        return this.wrappedConfiguration.getAttributeAsFloat(arg0);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getAttributeAsInteger(java.lang.String, int)
     */
    public int getAttributeAsInteger(String arg0, int arg1) {
        return this.wrappedConfiguration.getAttributeAsInteger(arg0, arg1);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getAttributeAsInteger(java.lang.String)
     */
    public int getAttributeAsInteger(String arg0)
        throws ConfigurationException {
        return this.wrappedConfiguration.getAttributeAsInteger(arg0);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getAttributeAsLong(java.lang.String, long)
     */
    public long getAttributeAsLong(String arg0, long arg1) {
        return this.wrappedConfiguration.getAttributeAsLong(arg0, arg1);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getAttributeAsLong(java.lang.String)
     */
    public long getAttributeAsLong(String arg0) throws ConfigurationException {
        return this.wrappedConfiguration.getAttributeAsLong(arg0);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getAttributeNames()
     */
    public String[] getAttributeNames() {
        return this.wrappedConfiguration.getAttributeNames();
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getChild(java.lang.String, boolean)
     */
    public Configuration getChild(String arg0, boolean arg1) {
        return this.wrappedConfiguration.getChild(arg0, arg1);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getChild(java.lang.String)
     */
    public Configuration getChild(String arg0) {
        return this.wrappedConfiguration.getChild(arg0);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getChildren()
     */
    public Configuration[] getChildren() {
        return this.wrappedConfiguration.getChildren();
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getChildren(java.lang.String)
     */
    public Configuration[] getChildren(String arg0) {
        return this.wrappedConfiguration.getChildren(arg0);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getLocation()
     */
    public String getLocation() {
        return this.wrappedConfiguration.getLocation();
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getName()
     */
    public String getName() {
        return this.wrappedConfiguration.getName();
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getNamespace()
     */
    public String getNamespace() throws ConfigurationException {
        return this.wrappedConfiguration.getNamespace();
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getValue()
     */
    public String getValue() throws ConfigurationException {
        return this.wrappedConfiguration.getValue();
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getValue(java.lang.String)
     */
    public String getValue(String arg0) {
        return this.wrappedConfiguration.getValue(arg0);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getValueAsBoolean()
     */
    public boolean getValueAsBoolean() throws ConfigurationException {
        return this.wrappedConfiguration.getValueAsBoolean();
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getValueAsBoolean(boolean)
     */
    public boolean getValueAsBoolean(boolean arg0) {
        return this.wrappedConfiguration.getValueAsBoolean(arg0);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getValueAsFloat()
     */
    public float getValueAsFloat() throws ConfigurationException {
        return this.wrappedConfiguration.getValueAsFloat();
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getValueAsFloat(float)
     */
    public float getValueAsFloat(float arg0) {
        return this.wrappedConfiguration.getValueAsFloat(arg0);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getValueAsInteger()
     */
    public int getValueAsInteger() throws ConfigurationException {
        return this.wrappedConfiguration.getValueAsInteger();
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getValueAsInteger(int)
     */
    public int getValueAsInteger(int arg0) {
        return this.wrappedConfiguration.getValueAsInteger(arg0);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getValueAsLong()
     */
    public long getValueAsLong() throws ConfigurationException {
        return this.wrappedConfiguration.getValueAsLong();
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getValueAsLong(long)
     */
    public long getValueAsLong(long arg0) {
        return this.wrappedConfiguration.getValueAsLong(arg0);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getAttributeAsDouble(java.lang.String, double)
     */
    public double getAttributeAsDouble(String arg0, double arg1) {
        return this.wrappedConfiguration.getAttributeAsDouble(arg0, arg1);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getAttributeAsDouble(java.lang.String)
     */
    public double getAttributeAsDouble(String arg0) throws ConfigurationException {
        return this.wrappedConfiguration.getAttributeAsDouble(arg0);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getValueAsDouble()
     */
    public double getValueAsDouble() throws ConfigurationException {
        return this.wrappedConfiguration.getValueAsDouble();
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configuration#getValueAsDouble(double)
     */
    public double getValueAsDouble(double arg0) {
        return this.wrappedConfiguration.getValueAsDouble(arg0);
    }
}
