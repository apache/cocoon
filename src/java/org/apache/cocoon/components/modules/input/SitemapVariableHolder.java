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
 * @version CVS $Id: SitemapVariableHolder.java,v 1.7 2004/03/08 13:58:30 cziegeler Exp $
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

