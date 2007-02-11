/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: ChainedConfiguration.java,v 1.2 2003/03/19 15:21:19 cziegeler Exp $
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

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getAttribute(java.lang.String, java.lang.String)
     */
    public String getAttribute(String arg0, String arg1) {
        return this.wrappedConfiguration.getAttribute(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getAttribute(java.lang.String)
     */
    public String getAttribute(String arg0) throws ConfigurationException {
        return this.wrappedConfiguration.getAttribute(arg0);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getAttributeAsBoolean(java.lang.String, boolean)
     */
    public boolean getAttributeAsBoolean(String arg0, boolean arg1) {
        return this.wrappedConfiguration.getAttributeAsBoolean(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getAttributeAsBoolean(java.lang.String)
     */
    public boolean getAttributeAsBoolean(String arg0)
        throws ConfigurationException {
        return this.wrappedConfiguration.getAttributeAsBoolean(arg0);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getAttributeAsFloat(java.lang.String, float)
     */
    public float getAttributeAsFloat(String arg0, float arg1) {
        return this.wrappedConfiguration.getAttributeAsFloat(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getAttributeAsFloat(java.lang.String)
     */
    public float getAttributeAsFloat(String arg0)
        throws ConfigurationException {
        return this.wrappedConfiguration.getAttributeAsFloat(arg0);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getAttributeAsInteger(java.lang.String, int)
     */
    public int getAttributeAsInteger(String arg0, int arg1) {
        return this.wrappedConfiguration.getAttributeAsInteger(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getAttributeAsInteger(java.lang.String)
     */
    public int getAttributeAsInteger(String arg0)
        throws ConfigurationException {
        return this.wrappedConfiguration.getAttributeAsInteger(arg0);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getAttributeAsLong(java.lang.String, long)
     */
    public long getAttributeAsLong(String arg0, long arg1) {
        return this.wrappedConfiguration.getAttributeAsLong(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getAttributeAsLong(java.lang.String)
     */
    public long getAttributeAsLong(String arg0) throws ConfigurationException {
        return this.wrappedConfiguration.getAttributeAsLong(arg0);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getAttributeNames()
     */
    public String[] getAttributeNames() {
        return this.wrappedConfiguration.getAttributeNames();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getChild(java.lang.String, boolean)
     */
    public Configuration getChild(String arg0, boolean arg1) {
        return this.wrappedConfiguration.getChild(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getChild(java.lang.String)
     */
    public Configuration getChild(String arg0) {
        return this.wrappedConfiguration.getChild(arg0);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getChildren()
     */
    public Configuration[] getChildren() {
        return this.wrappedConfiguration.getChildren();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getChildren(java.lang.String)
     */
    public Configuration[] getChildren(String arg0) {
        return this.wrappedConfiguration.getChildren(arg0);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getLocation()
     */
    public String getLocation() {
        return this.wrappedConfiguration.getLocation();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getName()
     */
    public String getName() {
        return this.wrappedConfiguration.getName();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getNamespace()
     */
    public String getNamespace() throws ConfigurationException {
        return this.wrappedConfiguration.getNamespace();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getValue()
     */
    public String getValue() throws ConfigurationException {
        return this.wrappedConfiguration.getValue();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getValue(java.lang.String)
     */
    public String getValue(String arg0) {
        return this.wrappedConfiguration.getValue(arg0);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getValueAsBoolean()
     */
    public boolean getValueAsBoolean() throws ConfigurationException {
        return this.wrappedConfiguration.getValueAsBoolean();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getValueAsBoolean(boolean)
     */
    public boolean getValueAsBoolean(boolean arg0) {
        return this.wrappedConfiguration.getValueAsBoolean(arg0);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getValueAsFloat()
     */
    public float getValueAsFloat() throws ConfigurationException {
        return this.wrappedConfiguration.getValueAsFloat();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getValueAsFloat(float)
     */
    public float getValueAsFloat(float arg0) {
        return this.wrappedConfiguration.getValueAsFloat(arg0);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getValueAsInteger()
     */
    public int getValueAsInteger() throws ConfigurationException {
        return this.wrappedConfiguration.getValueAsInteger();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getValueAsInteger(int)
     */
    public int getValueAsInteger(int arg0) {
        return this.wrappedConfiguration.getValueAsInteger(arg0);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getValueAsLong()
     */
    public long getValueAsLong() throws ConfigurationException {
        return this.wrappedConfiguration.getValueAsLong();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configuration#getValueAsLong(long)
     */
    public long getValueAsLong(long arg0) {
        return this.wrappedConfiguration.getValueAsLong(arg0);
    }

}
