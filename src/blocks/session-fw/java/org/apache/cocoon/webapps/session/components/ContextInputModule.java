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

package org.apache.cocoon.webapps.session.components;

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.webapps.session.ContextManager;
import org.apache.cocoon.webapps.session.context.SessionContext;
import org.apache.cocoon.xml.dom.DOMUtil;
import org.w3c.dom.DocumentFragment;

/**
 * This input module provides access to the information of a session
 * context using an XPath. The XPath expression that can be used
 * is the same as for the session transformer.
 * The first information in the path is the context, so for example
 * {session-context:authentication/authentication/ID} delivers the ID of the
 * current user and therefore delivers the same information as:
 * &lt;session:getxml context="authentication" path="/authentication/ID"/&gt;
 * using the session transformer.
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: ContextInputModule.java,v 1.3 2004/03/05 13:02:22 bdelacretaz Exp $
 */
public class ContextInputModule
    implements ThreadSafe, Serviceable, Disposable, InputModule {

    protected ServiceManager manager;
    
    protected ContextManager contextManager;
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.components.modules.input.InputModule#getAttribute(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Object getAttribute(String name,
                               Configuration modeConf,
                               Map objectModel)
    throws ConfigurationException {
        if ( name == null ) {
            return null;
        }
        int pos = name.indexOf('/');
        if ( pos != -1 ) {
            final String contextName = name.substring(0, pos);
            final String path = name.substring(pos);
            
            try {
                SessionContext context = this.contextManager.getContext(contextName);
                if ( context != null ) {
                    DocumentFragment frag = context.getXML(path);
                    return DOMUtil.getValueOfNode(frag);
                }
            } catch (ProcessingException pe) {
                throw new ConfigurationException("Unable to get information from context.", pe);
            }
            
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.modules.input.InputModule#getAttributeNames(org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Iterator getAttributeNames(Configuration modeConf, Map objectModel)
    throws ConfigurationException {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.modules.input.InputModule#getAttributeValues(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Object[] getAttributeValues(String name,
                                        Configuration modeConf,
                                        Map objectModel)
    throws ConfigurationException {
        final Object value = this.getAttribute(name, modeConf, objectModel);
        if ( value != null ) {
            return new Object[] {value};
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.contextManager = (ContextManager) this.manager.lookup(ContextManager.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.contextManager);
            this.contextManager = null;
            this.manager = null;
        }

    }

}
