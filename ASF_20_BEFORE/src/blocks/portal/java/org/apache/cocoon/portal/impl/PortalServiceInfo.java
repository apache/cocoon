/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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
 * @version CVS $Id: PortalServiceInfo.java,v 1.2 2004/03/01 03:50:58 antonio Exp $
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
