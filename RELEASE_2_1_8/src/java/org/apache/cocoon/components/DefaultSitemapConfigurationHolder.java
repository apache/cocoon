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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: DefaultSitemapConfigurationHolder.java,v 1.4 2004/03/05 13:02:45 bdelacretaz Exp $
 */
public final class DefaultSitemapConfigurationHolder 
    implements SitemapConfigurationHolder {

    /** The role of the sitemap component */
    private String role;
    
    /** The prepared configurations indexed by the ChainedConfiguration */
    private Map preparedConfigurations;
    
    public DefaultSitemapConfigurationHolder(String role) {
        this.role = role;
    }
    
    /**
     * @see SitemapConfigurationHolder#getConfiguration()
     */
    public ChainedConfiguration getConfiguration() {
        Map confs = CocoonComponentManager.getCurrentProcessor().getComponentConfigurations();
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
