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

import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.portal.Constants;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.profile.ProfileManager;
import org.apache.commons.jxpath.JXPathContext;

/**
 * Makes accessible coplet instance data by using JXPath expressions.
 *
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Björn Lütkemeier</a>
 * @version CVS $Id: CopletModule.java,v 1.1 2003/05/08 11:54:01 cziegeler Exp $
 */
public class CopletModule 
implements InputModule, Composable, ThreadSafe {
    
    /**
     * The component manager.
     */
    private ComponentManager manager;
    
    /**
     * Obtains the component manager.
     */
    public void compose(ComponentManager manager) {
        this.manager = manager;
    }

    /**
     * Overridden from superclass.
     */
	public Object getAttribute(String name, Configuration modeConf, Map objectModel) 
    throws ConfigurationException {
        ProfileManager profileManager = null;
        try {
            profileManager = (ProfileManager)this.manager.lookup(ProfileManager.ROLE);

            // determine coplet id
            String copletId = null;            
            Map context = (Map)objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
            if (context != null) {
                copletId = (String)context.get(Constants.COPLET_ID_KEY);
            } else {
                copletId = (String)objectModel.get(Constants.COPLET_ID_KEY);
                
                // set portal name
                PortalService portalService = null;
                try {
                    portalService = (PortalService)this.manager.lookup(PortalService.ROLE);
                    portalService.setPortalName((String)objectModel.get(Constants.PORTAL_NAME_KEY));
                } finally {
                    this.manager.release(portalService);
                }
            }
            
            if (copletId == null) {
                throw new ConfigurationException("copletId must be passed in the object model either directly (e.g. by using ObjectModelAction) or within the parent context.");
            }
            
            JXPathContext jxpathContext = JXPathContext.newContext(profileManager.getCopletInstanceData(copletId));
            Object value = jxpathContext.getValue(name);
                
            if (value == null) {
                throw new ConfigurationException("Could not find value for expression "+name);
            }
                
            return value.toString();
            
        } catch (ComponentException e) {
            throw new ConfigurationException("ComponentException ", e);
        } finally {
            this.manager.release(profileManager);
        }
	}

    /**
     * Overridden from superclass.
     */
	public Iterator getAttributeNames(Configuration modeConf, Map objectModel) {
        return new Vector().iterator();
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
