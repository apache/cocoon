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
 * 	&lt;map:parameter name="maxpageable" value="{coplet:copletData.maxpageable}"/&gt;
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
 *		&lt;map:parameter name="maxpageable" value="{coplet:copletData.maxpageable}"/&gt;
 *	&lt;/map:action&gt;
 * &lt;/map:action&gt;</pre>
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @version CVS $Id: CopletModule.java,v 1.8 2003/12/12 16:34:21 cziegeler Exp $
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
