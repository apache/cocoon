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
package org.apache.cocoon.components.container;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.cocoon.components.ChainedConfiguration;
import org.apache.cocoon.components.SitemapConfigurationHolder;
import org.apache.cocoon.environment.internal.EnvironmentHelper;

/**
 *
 * @version $Id$
 */
public final class DefaultSitemapConfigurationHolder 
    implements SitemapConfigurationHolder {

    /** The role of the sitemap component */
    private final String role;
    
    /** The role manager */
//    private final RoleManager roleManager;

    /** The prepared configurations indexed by the ChainedConfiguration */
    private Map preparedConfigurations;
    
    public DefaultSitemapConfigurationHolder(String role/*, RoleManager manager*/) {
        this.role = role;
        //this.roleManager = manager;
    }
    
    protected Map convert(Configuration[] configs, int index) {
        Map sitemapComponentConfigurations;
        
        // do we have configurations?
        final Configuration[] childs = configs[index].getChildren();

        if ( null != childs && childs.length > 0 ) {

            if ( index == configs.length - 1 ) {
                sitemapComponentConfigurations = new HashMap(12);
            } else {
                // copy all configurations from parent
                sitemapComponentConfigurations = new HashMap(this.convert(configs, index+1));
            }

            // and now check for new configurations
            for(int m = 0; m < childs.length; m++) {

                final String r = null;//this.roleManager.getRoleForName(childs[m].getName());
                sitemapComponentConfigurations.put(r, new ChainedConfiguration(childs[m],
                                                                 (ChainedConfiguration)sitemapComponentConfigurations.get(r)));
            }
        } else {
            // we don't have configurations
            if ( index == configs.length - 1 ) {
                sitemapComponentConfigurations = Collections.EMPTY_MAP;
            } else {
                // use configuration from parent
                sitemapComponentConfigurations = this.convert(configs, index+1);
            }
        }
        return sitemapComponentConfigurations;        
    }
    
    /**
     * @see SitemapConfigurationHolder#getConfiguration()
     */
    public ChainedConfiguration getConfiguration() {
        Map confs = this.convert(EnvironmentHelper.getCurrentProcessor().getComponentConfigurations(), 0);
        return (ChainedConfiguration) (confs == null ? null : confs.get(this.role));
    }

    /**
     * @see SitemapConfigurationHolder#getPreparedConfiguration()
     */
    public Object getPreparedConfiguration() {
        if ( null != this.preparedConfigurations ) {
            ChainedConfiguration conf = this.getConfiguration();
            if ( null != conf ) {
                return this.preparedConfigurations.get( conf );
            }
        }
        return null;
    }

    /**
     * @see SitemapConfigurationHolder#setPreparedConfiguration(ChainedConfiguration, java.lang.Object)
     */
    public void setPreparedConfiguration(ChainedConfiguration configuration, 
                                          Object preparedConfig) {
        if ( null == this.preparedConfigurations ) {
            this.preparedConfigurations = new HashMap(5);                                              
        }
        this.preparedConfigurations.put(configuration, preparedConfig);
    }

}
