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
package org.apache.cocoon.components.modules.input;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ChainedConfiguration;
import org.apache.cocoon.components.SitemapConfigurable;
import org.apache.cocoon.components.SitemapConfigurationHolder;

/**
 * This "component" is a trick to get global variables on a per
 * sitemap base
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: SitemapVariableHolder.java,v 1.6 2004/01/07 11:18:49 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=SitemapVariableHolder
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=global-variables
 */
public final class SitemapVariableHolder
    extends AbstractLogEnabled
    implements Configurable, SitemapConfigurable, ThreadSafe
{
 
    public static final String ROLE = SitemapVariableHolder.class.getName();
    
    /**
     * Stores (global) configuration parameters as <code>key</code> /
     * <code>value</code> pairs from the component configuration
     */
    private Map globalValues;

    /** Manager for sitemap/sub sitemap configuration */
    private SitemapConfigurationHolder holder;

    /**
     * Configures the database access helper.
     *
     * Takes all elements nested in component declaration and stores
     * them as key-value pairs in <code>settings</code>. Nested
     * configuration option are not catered for. This way global
     * configuration options can be used.
     *
     * For nested configurations override this function.
     * */
    public void configure(Configuration conf) 
    throws ConfigurationException {
        final Configuration[] parameters = conf.getChildren();
        this.globalValues = new HashMap(parameters.length);
        for (int i = 0; i < parameters.length; i++) {
            final String key = parameters[i].getName();
            final String value = parameters[i].getValue();
            this.globalValues.put(key, value);
        }
    }

    /**
     * Set the <code>Configuration</code> from a sitemap
     */
    public void configure(SitemapConfigurationHolder holder)
    throws ConfigurationException {
        this.holder = holder;
    }

    /**
     * Get a value
     */
    public Object get(String key) {
        return this.getValues().get(key);
    }
    
    /**
     * Get keys
     */
    public Iterator getKeys() {
        return this.getValues().keySet().iterator();
    }
    
    protected Map getValues() {
        Map values = (Map)this.holder.getPreparedConfiguration();
        if ( null == values ) {
            values = new HashMap(this.globalValues);
            ChainedConfiguration conf = this.holder.getConfiguration();
            if ( conf != null ) {
                this.prepare(conf, values);
                this.holder.setPreparedConfiguration(conf, values);
            }
        }
        return values;
    }
    
    protected void prepare(ChainedConfiguration conf, Map values) {
        ChainedConfiguration parent = conf.getParent();
        if ( null != parent) {
            this.prepare(parent, values);
        }
        final Configuration[] parameters = conf.getChildren();
        final int len = parameters.length;
        for ( int i = 0; i < len; i++) {
            final String key = parameters[i].getName();
            final String value = parameters[i].getValue("");
            if ( key != null && value != null) {
                values.put(key, value);
            }
        }
    }
    
}

