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
import org.apache.excalibur.source.SourceResolver;


/**
 *  This is a utility class managing the authentication handlers
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: DefaultHandlerManager.java,v 1.4 2004/01/31 08:50:44 antonio Exp $
*/
public final class DefaultHandlerManager {

    /**
     * Get the current handler configuration
     */
    static public Map prepareHandlerConfiguration(SourceResolver resolver,
                                                       Map            objectModel,
                                                       SitemapConfigurationHolder holder)
    throws ConfigurationException {
        Map configs = (Map)holder.getPreparedConfiguration();
        if ( null == configs ) {
            ChainedConfiguration chainedConfig = holder.getConfiguration();
            configs = prepare( resolver, objectModel, holder, chainedConfig );
        }
        return configs;
    }
    /**
     * Prepare the handler configuration
     */
    static private Map prepare(SourceResolver resolver,
                                 Map            objectModel,
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
            values = prepare( resolver, objectModel, holder, parent );
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

                addHandler( resolver, objectModel, handlers[i], values );
            }
        }
        holder.setPreparedConfiguration( conf, values );
        
        return values;
    }

    /**
     * Add one handler configuration
     */
    static private void addHandler(SourceResolver resolver,
                                     Map            objectModel,
                                     Configuration  configuration,
                                     Map            values)
    throws ConfigurationException {
        // get handler name
        final String name = configuration.getAttribute("name");

        // create handler
        HandlerConfiguration currentHandler = new HandlerConfiguration(name);

        try {
            currentHandler.configure(resolver, ObjectModelHelper.getRequest(objectModel), configuration);
        } catch (ProcessingException se) {
            throw new ConfigurationException("Exception during configuration of handler: " + name, se);
        } catch (org.xml.sax.SAXException se) {
            throw new ConfigurationException("Exception during configuration of handler: " + name, se);
        } catch (java.io.IOException se) {
            throw new ConfigurationException("Exception during configuration of handler: " + name, se);
        }
        values.put( name, currentHandler );
    }


}
