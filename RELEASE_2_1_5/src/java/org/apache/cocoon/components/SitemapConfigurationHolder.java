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

/**
 * A {@link SitemapConfigurable} component gets the sitemap configuration
 * using this object.
 * 
 * @since 2.1
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: SitemapConfigurationHolder.java,v 1.4 2004/03/05 13:02:45 bdelacretaz Exp $
 */
public interface SitemapConfigurationHolder {

    /**
     * Get the  configuration for the current sitemap
     * @return The configuration
     */
    ChainedConfiguration getConfiguration();
    
    /**
     * Get the prepared configuration for the current sitemap
     * @return The configuration or null if no prepared is available
     */
    Object getPreparedConfiguration();
    
    /**
     * Set the prepared configuration for the current sitemap.
     * After it is set by a component, it can be get using
     * {@link #getPreparedConfiguration()}.
     * 
     * @param preparedConfig The prepared configuration
     */
    void setPreparedConfiguration(ChainedConfiguration configuration, Object preparedConfig);
}
