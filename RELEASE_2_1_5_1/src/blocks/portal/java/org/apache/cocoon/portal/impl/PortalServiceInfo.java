/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.portal.Constants;
import org.apache.cocoon.portal.PortalComponentManager;

/**
 * The portal information for the current request
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: PortalServiceInfo.java,v 1.3 2004/03/05 13:02:13 bdelacretaz Exp $
 */
public class PortalServiceInfo {
    
    private Map portalComponentManagers;

    private Map objectModel;

    protected Map temporaryAttributes = new HashMap();
    
    protected String portalName;

    protected String attributePrefix;
    
    protected PortalComponentManager portalComponentManager;
    
    public void setup(Map objectModel, Map managers) {
        this.objectModel = objectModel;
        this.portalComponentManagers = managers;
		Map context = (Map)objectModel.get(ObjectModelHelper.PARENT_CONTEXT);
		if (context != null) {
			String portalName = (String)context.get(Constants.PORTAL_NAME_KEY);
			if (portalName != null) {
                this.setPortalName(portalName);
			}
		}
    }

    public String getPortalName() {
        return this.portalName;
    }

    public void setPortalName(String value) {
        this.portalName = value;
        this.attributePrefix = this.getClass().getName() + '/' + this.portalName + '/';
        this.portalComponentManager = (PortalComponentManager) this.portalComponentManagers.get(this.portalName);
        if ( this.portalComponentManager == null ) {
            throw new RuntimeException("Portal '"+this.portalName+"' is not configured.");
        }
    }

    public Object getAttribute(String key) {
        final Session session = ObjectModelHelper.getRequest(this.objectModel).getSession(false);
        if (session == null) {
            return null;
        }
        return session.getAttribute( this.attributePrefix + key);
    }

    public void setAttribute(String key, Object value) {
        final Session session = ObjectModelHelper.getRequest(this.objectModel).getSession();
        session.setAttribute( this.attributePrefix + key, value);
    }

    public void removeAttribute(String key) {
        final Session session = ObjectModelHelper.getRequest(this.objectModel).getSession(false);
        if ( session != null ) {
            session.removeAttribute( this.attributePrefix + key);
        }
    }

    public Iterator getAttributeNames() {
        final Session session = ObjectModelHelper.getRequest(this.objectModel).getSession(false);
        if ( session != null ) {
            List names = new ArrayList();
            Enumeration e = session.getAttributeNames();
            final int pos = this.attributePrefix.length() + 1;
            if ( e != null ) {
                while ( e.hasMoreElements() ) {
                    final String name = (String)e.nextElement();
                    if ( name.startsWith( this.attributePrefix )) {
                        names.add( name.substring( pos ) );
                    }
                }
            }
            return names.iterator();
        }
        return Collections.EMPTY_MAP.keySet().iterator();
    }

    public Object getTemporaryAttribute(String key) {
        return this.temporaryAttributes.get( key );
    }
    
    public void setTemporaryAttribute(String key, Object value) {
        this.temporaryAttributes.put( key, value );
    }
    
    public void removeTemporaryAttribute(String key) {
        this.temporaryAttributes.remove( key );
    }
    
    public Iterator getTemporaryAttributeNames() {
        return this.temporaryAttributes.keySet().iterator();
    }

    /**
     * Return the component manager for the current portal
     */
    public PortalComponentManager getComponentManager() {
        return this.portalComponentManager;
    }

}
