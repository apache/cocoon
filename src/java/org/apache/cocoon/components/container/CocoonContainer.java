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

import org.apache.avalon.fortress.impl.AbstractContainer;
import org.apache.avalon.fortress.impl.DefaultECMContainer;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.Constants;

import java.util.Map;

/**
 * Customize the Fortress container to handle Cocoon semantics.
 *
 * @author <a href="bloritsch.at.apache.org">Berin Loritsch</a>
 * @version CVS $ Revision: 1.1 $
 */
public class CocoonContainer extends DefaultECMContainer {
    
    /**
     * Provide some validation for the core Cocoon components
     *
     * @param conf The configuration
     * @throws ConfigurationException if the coniguration is invalid
     */
    public void configure( Configuration conf ) 
    throws ConfigurationException {
        if ( !"cocoon".equals( conf.getName() ) ) {
            throw new ConfigurationException( "Invalid configuration format",
                conf );
        }
        final String confVersion = conf.getAttribute( "version" );

        if ( !Constants.CONF_VERSION.equals( confVersion ) ) {
            throw new ConfigurationException("Uncompatible configuration format", conf );
        }

        super.configure( conf );
    }

    /**
     * Ensure that we return the latest and greatest component for the role/hint combo if possible.
     * Otherwise default to normal behavior.
     *
     * @param role The role of the component we are looking up.
     * @param hint The hint for the component we are looking up.
     * @return The component for the role/hint combo
     * @throws ServiceException if the role/hint combo cannot be resolved.
     */
    public Object get( final String role, final Object hint ) 
    throws ServiceException {
        Object component = null;

        if ( null != hint
             && !AbstractContainer.DEFAULT_ENTRY.equals( hint )
             && !AbstractContainer.SELECTOR_ENTRY.equals( hint ) ) {
            
            Map implementations = (Map) m_mapper.get( role );
            if ( null != implementations ) {
                component = implementations.get( hint );
            }
        }

        if ( null == component ) {
            component = super.get( role, hint );
        }

        return component;
    }
    
    /**
     * Use a different default proxy type
     */
    protected String getDefaultProxyType() {
        return "bcel";
    }
    
}
