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
package org.apache.cocoon.portal.components.modules.input;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.portal.Constants;
import org.apache.cocoon.portal.PortalService;
import org.apache.commons.jxpath.JXPathContext;

/**
 * Makes accessible coplet instance data by using JXPath expressions.<br><br>
 *
 * Example:<br><br>
 * 
 * <pre>&lt;map:action type="foo"&gt;
 * 	&lt;map:parameter name="maxpageable" value="{coplet:copletData/maxpageable}"/&gt;
 * &lt;/map:action&gt;<br></pre>
 * 
 * The module will insert the boolean value specifying whether the coplet is 
 * maxpageable or not as value of attribute "value" in &lt;map:parameter&gt;. 
 * There are two possibilities how the module obtains the information required for 
 * getting the coplet instance data:<br><br>
 * 1) If it is used within a coplet pipeline and this pipeline is called using the "cocoon:" protocol,
 * all required information are passed automatically.<br>
 * 2) Otherwise the portal name and the coplet id must be passed in the object model 
 * which can be done by using the ObjectModelAction:
 *
 * <pre>&lt;map:action type="objectModel"&gt;
 *	&lt;map:parameter name="portalName" value="exampleportal"/&gt;
 *	&lt;map:parameter name="copletId" value="examplecoplet"/&gt;
 *	&lt;map:action type="foo"&gt;
 *		&lt;map:parameter name="maxpageable" value="{coplet:copletData/maxpageable}"/&gt;
 *	&lt;/map:action&gt;
 * &lt;/map:action&gt;</pre>
 *
 * Using the path '#' you get the current copletId: {coplet:#}
 * 
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: CopletModule.java,v 1.11 2004/03/05 13:02:10 bdelacretaz Exp $
 */
public class CopletModule 
implements InputModule, Serviceable, ThreadSafe {
    
    /**
     * The component manager.
     */
    private ServiceManager manager;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * Overridden from superclass.
     */
	public Object getAttribute(String name, Configuration modeConf, Map objectModel) 
    throws ConfigurationException {
        PortalService portalService = null;
        try {

            portalService = (PortalService)this.manager.lookup(PortalService.ROLE);

            // determine coplet id
            String copletId = null;            
            Map context = (Map)objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
            if (context != null) {
                copletId = (String)context.get(Constants.COPLET_ID_KEY);
            } else {
                copletId = (String)objectModel.get(Constants.COPLET_ID_KEY);
                
                if ( copletId != null ) {
                    // set portal name
                    portalService.setPortalName((String)objectModel.get(Constants.PORTAL_NAME_KEY));                    
                }
            }
            
            if (copletId == null) {
                return null;
            }
            
            // return the coplet id
            if ( name.equals("#") ) {
                return copletId;
            }
            JXPathContext jxpathContext = JXPathContext.newContext(portalService.getComponentManager().getProfileManager().getCopletInstanceData(copletId));
            Object value = jxpathContext.getValue(name);
                
            if (value == null) {
                return null;
            }
                
            return value.toString();
            
        } catch (ServiceException e) {
            throw new ConfigurationException("ComponentException ", e);
        } finally {
            this.manager.release(portalService);
        }
	}

    /**
     * Overridden from superclass.
     */
	public Iterator getAttributeNames(Configuration modeConf, Map objectModel) {
        return Collections.EMPTY_LIST.iterator();
	}

    /**
     * Overridden from superclass.
     */
	public Object[] getAttributeValues(String name, Configuration modeConf, Map objectModel)
    throws ConfigurationException {
        Object[] result = new Object[1];
        result[0] = this.getAttribute(name, modeConf, objectModel);
        return result;
	}
}
