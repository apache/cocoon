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
package org.apache.cocoon.webapps.authentication.components;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ChainedConfiguration;
import org.apache.cocoon.components.SitemapConfigurationHolder;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.webapps.authentication.configuration.HandlerConfiguration;


/**
 *  This is a utility class managing the authentication handlers
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: DefaultHandlerManager.java,v 1.6 2004/03/19 13:59:22 cziegeler Exp $
*/
public final class DefaultHandlerManager {

    /**
     * Get the current handler configuration
     */
    static public Map prepareHandlerConfiguration(Map            objectModel,
                                                  SitemapConfigurationHolder holder)
    throws ConfigurationException {
        Map configs = (Map)holder.getPreparedConfiguration();
        if ( null == configs ) {
            ChainedConfiguration chainedConfig = holder.getConfiguration();
            configs = prepare( objectModel, holder, chainedConfig );
        }
        return configs;
    }
    /**
     * Prepare the handler configuration
     */
    static private Map prepare( Map            objectModel,
                                SitemapConfigurationHolder holder,
                                ChainedConfiguration conf) 
    throws ConfigurationException {
        // test for handlers
        boolean found = false;
        Configuration[] handlers = null;
        Configuration handlersWrapper = conf.getChild("handlers", false);
        if ( null != handlersWrapper ) {
            handlers = handlersWrapper.getChildren("handler");
            if ( null != handlers && handlers.length > 0) {
                found = true;
            }
        }

        Map values = null;
        final ChainedConfiguration parent = conf.getParent();
        if ( null != parent ) {
            values = prepare( objectModel, holder, parent );
            if ( found ) {
                values = new HashMap( values );
            }
        } else if ( found ){
            values = new HashMap(10);
        } else {
            values = Collections.EMPTY_MAP;
        }

        if ( found ) {
            for(int i=0; i<handlers.length;i++) {
                // check unique name
                final String name = handlers[i].getAttribute("name");
                if ( null != values.get(name) ) {
                    throw new ConfigurationException("Handler names must be unique: " + name);
                }

                addHandler( objectModel, handlers[i], values );
            }
        }
        holder.setPreparedConfiguration( conf, values );
        
        return values;
    }

    /**
     * Add one handler configuration
     */
    static private void addHandler(Map            objectModel,
                                   Configuration  configuration,
                                   Map            values)
    throws ConfigurationException {
        // get handler name
        final String name = configuration.getAttribute("name");

        // create handler
        HandlerConfiguration currentHandler = new HandlerConfiguration(name);

        try {
            currentHandler.configure(ObjectModelHelper.getRequest(objectModel), configuration);
        } catch (ProcessingException se) {
            throw new ConfigurationException("Exception during configuration of handler: " + name, se);
        }
        values.put( name, currentHandler );
    }


}
